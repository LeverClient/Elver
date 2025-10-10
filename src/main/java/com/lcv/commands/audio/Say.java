package com.lcv.commands.audio;

import com.lcv.commands.ICommand;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.managers.AudioManager;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

public class Say implements ICommand
{
    @Override
    public String getName()
    {
        return "say";
    }

    @Override
    public String getDescription()
    {
        return "Say something cool";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event)
    {
        try
        {
            event.deferReply().queue();
            String audioQuery = getAudioQuery(event.getOption("text").getAsString());
            byte[] audioBytes = synthesizeAudio(audioQuery);

            File tempFile = File.createTempFile("voicevox", ".wav");
            tempFile.deleteOnExit();
            Files.write(tempFile.toPath(), audioBytes);

            AudioManager audioManager = event.getGuild().getAudioManager();
            VoiceChannel channel = event.getMember().getVoiceState().getChannel().asVoiceChannel();

            AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
            AudioSourceManagers.registerLocalSource(playerManager);

            AudioPlayer player = playerManager.createPlayer();
            audioManager.openAudioConnection(channel);
            audioManager.setSendingHandler(new AudioHandler(player));

            playerManager.loadItem(tempFile.getAbsolutePath(), new AudioLoadResultHandler()
            {
                @Override
                public void trackLoaded(AudioTrack audioTrack)
                {
                    player.playTrack(audioTrack);
                }

                @Override
                public void playlistLoaded(AudioPlaylist audioPlaylist)
                {

                }

                @Override
                public void noMatches()
                {

                }

                @Override
                public void loadFailed(FriendlyException e)
                {

                }
            });
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        event.getHook().sendMessage("playing").queue();
    }

    private String getAudioQuery(String text)
    {
        try
        {
            URL url = new URL("http://localhost:50021/audio_query?text=" + URLEncoder.encode(text, StandardCharsets.UTF_8) + "&speaker=24&enable_katakana_english=true");

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.getOutputStream().write("{}".getBytes(StandardCharsets.UTF_8));

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                stringBuilder.append(line);
            reader.close();
            connection.disconnect();
            return stringBuilder.toString();
        }
        catch(IOException ignored)
        {
            return null;
        }
    }

    private byte[] synthesizeAudio(String audioQuery)
    {
        try
        {
            URL url = new URL("http://localhost:50021/synthesis?speaker=24&enable_interrogative_upspeak=true");

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.getOutputStream().write(audioQuery.getBytes(StandardCharsets.UTF_8));

            byte[] audio = connection.getInputStream().readAllBytes();
            connection.disconnect();

            return audio;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addFields(SlashCommandData data)
    {
        data.addOption(STRING, "text", "text", true);
    }
}
