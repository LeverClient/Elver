package com.lcv.commands.audio;

import com.lcv.Main;
import com.lcv.commands.ICommand;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.managers.AudioManager;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

public class PlayAudio implements ICommand
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
        if (!event.getMember().getId().equals("585958966232875018"))
        {
            event.reply("no you're not leverclient").queue();
            return;
        }
        String audioQuery = createAudioQuery(event.getOption("text").getAsString());
        byte[] audioBytes = synthesizeAudio(audioQuery);

        AudioPlayer audioPlayer = Main.audioPlayerManager.createPlayer();

        audioPlayer.playTrack(new AudioTrack());
        AudioManager audioManager = event.getGuild().getAudioManager();
        VoiceChannel channel = event.getMember().getVoiceState().getChannel().asVoiceChannel();
        audioManager.setSendingHandler(new TestHandler(audioPlayer));
        audioManager.openAudioConnection(channel);
        event.reply("Joining " + channel.getName()).queue();
    }

    private String createAudioQuery(String text)
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
