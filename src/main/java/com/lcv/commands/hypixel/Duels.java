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
import java.util.HashMap;
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

        Map<String, Double> stats = getStats(hypixelData);
        return null;
    }

    public static Map<String, Double> getStats(HypixelPlayerData hypixelData)
    {
        JSONObject duelsJson = hypixelData.stats.getJSONObject("Duels");

        BiFunction<JSONObject, String, Double> getDouble = (json, s) -> json.has(s) && !json.isNull(s) ? json.getDouble(s) : 0;
        BiFunction<Double, Double, Double> getRatio = (num, den) -> den == 0 ? 0 : num / den;
        BiFunction<Double, Double, Double> getPercentage = (num, total) -> {
            double p = total != 0 ? (num / total) * 100.0 : 0.0;
            return p < 10.0 ? Math.round(p * 10.0) / 10.0 : Math.round(p);
        };

        Map<String, Double> stats = new HashMap<>();

        stats.put("wins", getDouble.apply(duelsJson,"wins"));
        stats.put("losses", getDouble.apply(duelsJson, "losses"));
        stats.put("wl", getRatio.apply(stats.get("wins"), stats.get("losses")));

        stats.put("kills", getDouble.apply(duelsJson, "kills"));
        stats.put("deaths", getDouble.apply(duelsJson, "deaths"));
        stats.put("kd", getRatio.apply(stats.get("kills"), stats.get("deaths")));

        stats.put("bow_shot", getDouble.apply(duelsJson, "bow_shots"));
        stats.put("bow_hit", getDouble.apply(duelsJson, "bow_hits"));
        stats.put("bow_accuracy", getPercentage.apply(stats.get("bow_hit"), stats.get("bow_hit") + stats.get("bow_shot")));

        stats.put("sword_swing", getDouble.apply(duelsJson, "melee_swings"));
        stats.put("sword_hit", getDouble.apply(duelsJson, "melee_hits"));
        stats.put("sword_accuracy", getPercentage.apply(stats.get("sword_hit"), stats.get("sword_hit") + stats.get("sword_swing")));

        stats.put("heal", getDouble.apply(duelsJson, "health_regenerated"));
        stats.put("damage", getDouble.apply(duelsJson, "damage_dealt"));
        stats.put("coins", getDouble.apply(duelsJson, "coins"));

        stats.put("ping", getDouble.apply(duelsJson, "pingPreference"));
        stats.put("networkXP", getDouble.apply(hypixelData.player, "networkExp"));
        return stats;
    }

    @Override
    public void addFields(SlashCommandData data)
    {
        data.addOption(STRING, "name", "Name of Player", true);
    }
}
