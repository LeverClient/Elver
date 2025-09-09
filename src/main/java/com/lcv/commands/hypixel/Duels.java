package com.lcv.commands.hypixel;

import com.lcv.commands.Command;
import com.lcv.commands.Embed;
import com.lcv.util.HTTPRequest;
import com.lcv.util.HypixelPlayerData;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.FileUpload;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.function.BiFunction;

import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

public class Duels implements Command
{
    private static final String API_KEY_HYPIXEL = System.getenv("API_KEY_HYPIXEL");
    @Override
    public String getName()
    {
        return "duels";
    }

    @Override
    public String getDescription()
    {
        return "Gets Duels Stats";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event)
    {
        event.deferReply().queue();
        InteractionHook interactionHook = event.getHook();

        String name = event.getOption("name").getAsString();
        JSONObject mojangJson = HTTPRequest.getHTTPRequest("https://api.mojang.com/users/profiles/minecraft/" + name);
        if (mojangJson == null || mojangJson.isEmpty())
        {
            Embed embed = new Embed().setTitle("Failed Operation :(").setDescription("Mojang: No player found");
            interactionHook.sendMessageEmbeds(embed.get()).queue();
            return;
        }

        String UUID = mojangJson.getString("id");
        JSONObject hypixelJson = HTTPRequest.getHTTPRequest("https://api.hypixel.net/v2/player?key=" + API_KEY_HYPIXEL + "&uuid=" + UUID);
        if (hypixelJson == null || hypixelJson.isEmpty())
        {
            Embed embed = new Embed().setTitle("Failed Operation :(").setDescription("Hypixel: No player found");
            interactionHook.sendMessageEmbeds(embed.get()).queue();
            return;
        }

        HypixelPlayerData hypixelData = new HypixelPlayerData(hypixelJson);
        BufferedImage statsImage;
        try
        {
            statsImage = generateStatsImage(hypixelData);
        }
        catch (IllegalArgumentException e)
        {
            Embed embed = new Embed().setTitle("Failed Operation :(").setDescription(e.getMessage() == null ? "Unsure" : e.getMessage());
            interactionHook.sendMessageEmbeds(embed.get()).queue();
            return;
        }

        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(statsImage, "png", baos);
            FileUpload file =  FileUpload.fromData(new ByteArrayInputStream(baos.toByteArray()), String.format("duels stats for %s meow.png", name));
            interactionHook.sendFiles(file).queue();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public BufferedImage generateStatsImage(HypixelPlayerData hypixelData)
    {
        if (!hypixelData.valid)
        {
            throw new IllegalArgumentException("Hypixel: No player data found");
        }

        if (!hypixelData.stats.has("Duels") || hypixelData.stats.isNull("Duels"))
        {
            throw new IllegalArgumentException("Hypixel: No duels stats");
        }

        JSONObject duelsJSON = hypixelData.stats.getJSONObject("Duels");

        BiFunction<JSONObject, String, String> getString = (json, s) -> json.has(s) && !json.isNull(s) ? json.getString(s) : null;

        DecimalFormat bigFormat = new DecimalFormat("###,###");


        return null;
    }

    @Override
    public void addFields(SlashCommandData data)
    {
        data.addOption(STRING, "name", "Name of Player", true);
    }
}
