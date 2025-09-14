package com.lcv.commands.hypixel;

import com.lcv.Main;
import com.lcv.commands.Command;
import com.lcv.commands.Embed;
import com.lcv.util.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.FileUpload;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.BiFunction;

import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

public class Duels implements Command
{
    private static final String API_KEY_HYPIXEL = System.getenv("API_KEY_HYPIXEL");
    private static final int LEVEL_PROGRESS_BAR_LENGTH = 30;
    private static final int PRESTIGE_PROGRESS_BAR_LENGTH = 20;
    private static final String[] prestigeList = {
            "all_modes_ascended_title_prestige",
            "all_modes_divine_title_prestige",
            "all_modes_celestial_title_prestige",
            "all_modes_godlike_title_prestige",
            "all_modes_grandmaster_title_prestige",
            "all_modes_legend_title_prestige",
            "all_modes_master_title_prestige",
            "all_modes_diamond_title_prestige",
            "all_modes_gold_title_prestige",
            "all_modes_iron_title_prestige",
            "all_modes_rookie_title_prestige",
    };
    private static final String[] prestigeColorList = {
            "§7", // none?
            "§7", // rookie
            "§f", // iron
            "§6", // gold
            "§2", // master
            "§4", // legend
            "§e", // grandmaster
            "§5", // godlike
            "§b", // celestial
            "§d", // divine
            "§c", // ascended
    };
    private static final int[] nextRankAmountList = {
            100, // none?
            20, // rookie
            60, // iron
            100, // gold
            200, // diamond
            400, // master
            1200, // legend
            2000, // grandmaster
            6000, // godlike
            10000, // celestial
            20000, // divine
            20000, // ascended
    };
    private static final int[] nextPrestigeAmountList = {
            100, // none?
            200, // rookie
            500, // iron
            1000, // gold
            2000, // diamond
            4000, // master
            10000, // legend
            20000, // grandmaster
            50000, // godlike
            100000, // celestial
            200000, // divine
            1180000, // ascended
    };
    public final int availableBackgrounds = ImageUtil.getBackgrounds(backgroundImages, "duelOverlay", (g2d) -> {
        g2d.drawImage(Main.botProfileScaled, 25, 25, 226, 226, null);
        g2d.drawImage(ImageUtil.ITEM_BOW, 1915, 300, 100, 100, null);
        g2d.drawImage(ImageUtil.ITEM_SWORD, 2455, 300, 100, 100, null);
    });
    public static ArrayList<BufferedImage> backgroundImages = new ArrayList<>();
    public static FontRenderer fontRenderer = new FontRenderer(null, new Font[]{
            Main.minecraftFont.deriveFont(144f),
            Main.minecraftFont.deriveFont(96f),
            Main.minecraftFont.deriveFont(72f),
            Main.minecraftFont.deriveFont(56f)
    });
    private static final Random rand = new Random();
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

        int networkLevel = (int) (1 + ((Math.sqrt(8750 * 8750 + 5000 * stats.get("networkXP")) - 8750) / 2500));

        String nameWithRank = hypixelData.getPlayerNameRankFormat();

        String prestige = "";
        String nextPrestige = "";
        String rank = "";

        int rankWinReq = nextRankAmountList[stats.get("rank").intValue() / 5];
        System.out.println(rankWinReq);

        for (int i = 0; i < 11; i++)
        {
            if (stats.get("rank") - (5 * i) > 5)
                continue;
            prestige = prestigeList[10 - i].split("_")[2];
            prestige = prestigeColorList[stats.get("rank").intValue() / 5] + prestige.substring(0, 1).toUpperCase() + prestige.substring(1);
            nextPrestige = i == 10 ? "L" : prestigeList[9 - i].split("_")[2].toUpperCase();
            rank = switch (stats.get("rank").intValue() - (5 * i))
            {
                case 1 -> "I";
                case 2 -> "II";
                case 3 -> "III";
                case 4 -> "IV";
                case 5 -> "V";
                default -> "rank died :(";
            };
            break;
        }

        int chosenBackground = availableBackgrounds <= 1 ? 0 : rand.nextInt(0, availableBackgrounds);
        BufferedImage image = ImageUtil.copyImage(backgroundImages.get(chosenBackground));

        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // set up font renderer
        fontRenderer.switchFont(0);
        fontRenderer.setGraphics(g2d);

        // apply skin (if we should)
        StatsSkins.Skin userSpecificSkin = StatsSkins.userSkins.get(hypixelData.uuid);
        if (userSpecificSkin == null) {
            StatsSkins.none.apply(fontRenderer);
        }
        else {
            userSpecificSkin.apply(fontRenderer);
        }

        Future<BufferedImage> playerFuture = ImageUtil.getPlayerSkinFull(hypixelData.uuid);
        Future<BufferedImage> playerTopFuture = ImageUtil.getPlayerSkinTop(hypixelData.uuid);

        fontRenderer.useDefaultColors = true;
        fontRenderer.drawString(nameWithRank, 1440 - (g2d.getFontMetrics().stringWidth((FontRenderer.removeFormatting(nameWithRank))) / 2), 75);
        fontRenderer.useDefaultColors = false;

        fontRenderer.switchFont(1);
        fontRenderer.drawString(String.format("§aWins: %s", bigFormat.format(stats.get("wins"))), 75, 325);
        fontRenderer.drawString(String.format("§cLosses: %s", bigFormat.format(stats.get("losses"))), 75, 510);
        fontRenderer.drawString(String.format("§aW§cL: §r%.2f", stats.get("wl")), 75, 700);

        fontRenderer.drawString(String.format("§aKills: %s", bigFormat.format(stats.get("kills"))), 75, 962);
        fontRenderer.drawString(String.format("§cDeaths: %s", bigFormat.format(stats.get("deaths"))), 75, 1147);
        fontRenderer.drawString(String.format("§aK§cD: §r%.2f", stats.get("kd")), 75, 1337);

        fontRenderer.switchFont(3);
        fontRenderer.drawString(String.format("§a%s%%", stats.get("bow_accuracy")), 2035, 325);

        fontRenderer.drawString(String.format("§a%s%%", stats.get("sword_accuracy")), 2580, 325);

        fontRenderer.switchFont(2);
        fontRenderer.drawString("§cShots:", 2070, 435, FontRenderer.CenterXAligned);
        fontRenderer.drawString(String.format("§c%s", bigFormat.format(stats.get("bow_shot"))), 2070, 525, FontRenderer.CenterXAligned);
        fontRenderer.drawString("§aHits:", 2070, 650, FontRenderer.CenterXAligned);
        fontRenderer.drawString(String.format("§a%s", bigFormat.format(stats.get("bow_hit"))), 2070, 750, FontRenderer.CenterXAligned);

        fontRenderer.drawString("§cSwings:", 2610, 435, FontRenderer.CenterXAligned);
        fontRenderer.drawString(String.format("§c%s", bigFormat.format(stats.get("sword_swing"))), 2610, 525, FontRenderer.CenterXAligned);
        fontRenderer.drawString("§aHits:", 2610, 650, FontRenderer.CenterXAligned);
        fontRenderer.drawString(String.format("§a%s", bigFormat.format(stats.get("sword_hit"))), 2610, 750, FontRenderer.CenterXAligned);

        /* leave this for recent played / favorite games (bottom right)
        fontRenderer.drawString(String.format("§aFK: %s", bigFormat.format(stats.get("finalKills"))), 1875, 962);
        fontRenderer.drawString(String.format("§cFD: %s", bigFormat.format(stats.get("finalDeaths"))), 1875, 1147);
        fontRenderer.drawString(String.format("§aFK§cDR: §r%.2f", stats.get("fkdr")), 1875, 1337);
        */

        // level info
        fontRenderer.switchFont(2);

        fontRenderer.drawString(prestige + " " + rank, 1440, 1275, FontRenderer.CenterXAligned);
        //fontRenderer.drawString(String.format("§a%s", levelProgressBarString), image.getWidth()/2, 1275+148, FontRenderer.CenterXAligned);
        //fontRenderer.drawString(String.format("§a%d §r/ §c%d", xpUntilLevel, xpReq), image.getWidth()/2, 1275+148*2, FontRenderer.CenterXAligned);

        fontRenderer.drawString("§aLevel:", 1440, 1785);
        fontRenderer.drawString("§c" + networkLevel, 1440, 1890);

        //fontRenderer.drawString(String.format("§a%s  §r>>>  §!%s", prestigeProgressBarString, formattedNextPrestige), 540, 1625-9, FontRenderer.CenterXAligned);

        // ping
        fontRenderer.switchFont(3);
        fontRenderer.drawString("§a±§c" + stats.get("ping").intValue() + "ms", 1440, 2015);

        // draw player images (last cause we were doing this on another thread)
        BufferedImage player = Main.nullTexture;
        BufferedImage playerTop = Main.nullTexture;

        try {
            player = playerFuture.get();
            playerTop = playerTopFuture.get();
        } catch (InterruptedException ignored) {}
        catch (ExecutionException e) {
            System.err.println("Failed to get player icons: " + e.getMessage());
            e.printStackTrace(System.err);
        }

        int[] playerSize = ImageUtil.fitToArea(player, 670, 850);
        int[] playerTopSize = ImageUtil.fitToArea(playerTop, Integer.MAX_VALUE, 300);

        g2d.drawImage(player, 1440 - (playerSize[0] / 2), 325 + ((850 - playerSize[1]) / 2), playerSize[0], playerSize[1], null);
        g2d.drawImage(playerTop, 1155, 1785, playerTopSize[0], playerTopSize[1], null);

        // output and return image
        g2d.dispose();

        return image;
    }

    public static Map<String, Double> getStats(HypixelPlayerData hypixelData)
    {
        JSONObject duelsJson = hypixelData.stats.getJSONObject("Duels");
        double rank = 0;

        BiFunction<JSONObject, String, Double> getDouble = (json, s) -> json.has(s) && !json.isNull(s) ? json.getDouble(s) : 0;
        BiFunction<Double, Double, Double> getRatio = (num, den) -> den == 0 ? 0 : num / den;
        BiFunction<Double, Double, Double> getPercentage = (num, total) -> {
            double p = total != 0 ? (num / total) * 100.0 : 0.0;
            return p < 10.0 ? Math.round(p * 10.0) / 10.0 : Math.round(p);
        };
        for (int i = 0; i < prestigeList.length; i++)
        {
            if (!duelsJson.has(prestigeList[i]))
                continue;
            rank = (5 * (10 - i)) + getDouble.apply(duelsJson, prestigeList[i]);
            break;
        }

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

        stats.put("rank", rank);
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
