package com.lcv.commands.hypixel;

import com.lcv.Main;
import com.lcv.commands.ICommand;
import com.lcv.commands.Embed;
import com.lcv.elverapi.apis.hypixelplayer.DuelsAPI;
import com.lcv.elverapi.apis.hypixelplayer.HypixelPlayerAPI;
import com.lcv.elverapi.apis.mojang.MojangProfileLookupAPI;
import com.lcv.util.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.lcv.Main.ALL_CONTEXTS;
import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

public class Duels implements ICommand
{
    private static final String API_KEY_HYPIXEL = System.getenv("API_KEY_HYPIXEL");
    private static final int RANK_PROGRESS_BAR_LENGTH = 30;
    private static final int PRESTIGE_PROGRESS_BAR_LENGTH = 40;
    public final int availableBackgrounds = HypixelUtil.getBackgrounds(backgroundImages, "duels/duels_overlay", (g2d) ->
    {
        g2d.drawImage(ImageUtil.ELVER_ICON, 25, 25, 226, 226, null);
        g2d.drawImage(HypixelUtil.DUELS_BOW, 1915, 300, 100, 100, null);
        g2d.drawImage(HypixelUtil.DUELS_SWORD, 2455, 300, 100, 100, null);
        g2d.drawImage(HypixelUtil.DUELS_HEALTH, 75, 1600, 150, 150, null);
        g2d.drawImage(HypixelUtil.DUELS_DAMAGE, 65, 1765, 175, 175, null);
        g2d.drawImage(HypixelUtil.DUELS_COIN, 75, 1935, 150, 150, null);
    });
    public static ArrayList<BufferedImage> backgroundImages = new ArrayList<>();
    public static FontRenderer fontRenderer = new FontRenderer(null, new Font[]{ImageUtil.MINECRAFT_FONT.deriveFont(144f), ImageUtil.MINECRAFT_FONT.deriveFont(96f), ImageUtil.MINECRAFT_FONT.deriveFont(64f), ImageUtil.MINECRAFT_FONT.deriveFont(56f), ImageUtil.MINECRAFT_FONT.deriveFont(40f)});
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
    public Set<InteractionContextType> getContexts() {
        return ALL_CONTEXTS;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event)
    {
        event.deferReply().queue();
        InteractionHook interactionHook = event.getHook();

        String name = event.getOption("name").getAsString();

        MojangProfileLookupAPI mojang = new MojangProfileLookupAPI(name);
        HypixelPlayerAPI player = new HypixelPlayerAPI(mojang.getUUID(), API_KEY_HYPIXEL);

        if (!player.hasData())
        {
            Embed embed = new Embed().setTitle("Failed Operation :(").setDescription("Hypixel: No player found");
            interactionHook.sendMessageEmbeds(embed.get()).queue();
            return;
        }

        BufferedImage statsImage;
        try
        {
            statsImage = generateStatsImage(player);
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
            FileUpload file = FileUpload.fromData(new ByteArrayInputStream(baos.toByteArray()), String.format("duels stats for %s meow.png", name));
            interactionHook.sendFiles(file).queue();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public BufferedImage generateStatsImage(HypixelPlayerAPI player)
    {
        DuelsAPI duels = player.getDuelsApi();
        if (!duels.exists())
        {
            throw new IllegalArgumentException("Hypixel: No duels stats");
        }

        DecimalFormat bigFormat = new DecimalFormat("###,###");

        int winRankOverflow = duels.getWins();
        int winRankReq = DuelsAPI.PRESTIGE_WIN_LIST[0];
        for (int i = 0; i < DuelsAPI.PRESTIGE_LIST.length; i++)
        {
            if (duels.getWins() < DuelsAPI.PRESTIGE_WIN_LIST[i])
                break;
            winRankOverflow = duels.getWins() - DuelsAPI.PRESTIGE_WIN_LIST[i];
            winRankOverflow = winRankOverflow % DuelsAPI.RANK_WIN_LIST[i + 1];
            winRankReq = DuelsAPI.RANK_WIN_LIST[i + 1];
        }

        int rankProgressBarCompletion = (int) (RANK_PROGRESS_BAR_LENGTH * duels.getRankPercentage() / 100);
        String rankProgressBar = "|".repeat(rankProgressBarCompletion) + "§c" + "|".repeat(RANK_PROGRESS_BAR_LENGTH - rankProgressBarCompletion);

        int prestigeProgressBarCompletion = (int) (PRESTIGE_PROGRESS_BAR_LENGTH * duels.getPrestigePercentage() / 100);
        String prestigeProgressBar = "|".repeat(prestigeProgressBarCompletion) + "§c" + "|".repeat(PRESTIGE_PROGRESS_BAR_LENGTH - prestigeProgressBarCompletion);

        int chosenBackground = availableBackgrounds <= 1 ? 0 : rand.nextInt(0, availableBackgrounds);
        BufferedImage image = ImageUtil.copyImage(backgroundImages.get(chosenBackground));

        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // set up font renderer
        fontRenderer.switchFont(0);
        fontRenderer.setGraphics(g2d);

        // apply skin (if we should)
        StatsSkins.Skin userSpecificSkin = StatsSkins.userSkins.get(player.getUUID());
        if (userSpecificSkin == null)
        {
            StatsSkins.none.apply(fontRenderer);
        }
        else
        {
            userSpecificSkin.apply(fontRenderer);
        }

        Future<BufferedImage> playerFuture = HypixelUtil.getPlayerSkinFull(player.getUUID());
        Future<BufferedImage> playerTopFuture = HypixelUtil.getPlayerSkinTop(player.getUUID());

        fontRenderer.useDefaultColors = true;
        fontRenderer.drawString(player.getNameFormatted(), 1440 - (g2d.getFontMetrics().stringWidth((FontRenderer.removeFormatting(player.getNameFormatted()))) / 2), 75);
        fontRenderer.useDefaultColors = false;

        fontRenderer.switchFont(1);
        fontRenderer.drawString(String.format("§aWins: %s", bigFormat.format(duels.getWins())), 75, 325);
        fontRenderer.drawString(String.format("§cLosses: %s", bigFormat.format(duels.getLosses())), 75, 510);
        fontRenderer.drawString(String.format("§aW§cL: §r%.2f", duels.getWLR()), 75, 700);

        fontRenderer.drawString(String.format("§aKills: %s", bigFormat.format(duels.getKills())), 75, 962);
        fontRenderer.drawString(String.format("§cDeaths: %s", bigFormat.format(duels.getLosses())), 75, 1147);
        fontRenderer.drawString(String.format("§aK§cD: §r%.2f", duels.getKDR()), 75, 1337);

        fontRenderer.switchFont(3);
        fontRenderer.drawString(String.format("§a%.1f%%", duels.getBowAccuracy()), 2035, 325);

        fontRenderer.drawString(String.format("§a%.1f%%", duels.getSwordAccuracy()), 2580, 325);

        fontRenderer.switchFont(2);
        fontRenderer.drawString("§cShots:", 2070, 435, FontRenderer.CenterXAligned);
        fontRenderer.drawString(String.format("§c%s", bigFormat.format(duels.getBowShot())), 2070, 525, FontRenderer.CenterXAligned);
        fontRenderer.drawString("§aHits:", 2070, 650, FontRenderer.CenterXAligned);
        fontRenderer.drawString(String.format("§a%s", bigFormat.format(duels.getBowHit())), 2070, 750, FontRenderer.CenterXAligned);

        fontRenderer.drawString("§cSwings:", 2610, 435, FontRenderer.CenterXAligned);
        fontRenderer.drawString(String.format("§c%s", bigFormat.format(duels.getSwordSwung())), 2610, 525, FontRenderer.CenterXAligned);
        fontRenderer.drawString("§aHits:", 2610, 650, FontRenderer.CenterXAligned);
        fontRenderer.drawString(String.format("§a%s", bigFormat.format(duels.getSwordHit())), 2610, 750, FontRenderer.CenterXAligned);

        fontRenderer.switchFont(1);
        fontRenderer.useDefaultColors = true;
        fontRenderer.drawString(String.format("§a%s", bigFormat.format(duels.getHealthHealed())), 275, 1625);
        fontRenderer.drawString(String.format("§c%s", bigFormat.format(duels.getDamageDealt())), 275, 1800);
        fontRenderer.drawString(String.format("§e%s", bigFormat.format(duels.getCoins())), 275, 1975);
        fontRenderer.useDefaultColors = false;

        fontRenderer.switchFont(2);
        for (int i = 0; i < duels.getRecentlyPlayed().length; i++)
        {
            if (duels.getRecentlyPlayed().length == 1 && duels.getRecentlyPlayed()[0].isEmpty())
            {
                fontRenderer.switchFont(2);
                fontRenderer.drawString("NO RECENTLY PLAYED", 2300, 1200, FontRenderer.CenterXAligned);
                fontRenderer.switchFont(2);
                break;
            }
            String[] duel = switch(duels.getRecentlyPlayed()[i])
            {
                case "BEDWARS_TWO_ONE_DUELS" -> new String[]{"Bedwars", ""};
                case "BEDWARS_TWO_ONE_DUELS_RUSH" -> new String[]{"Bedrush", ""};

                case "BRIDGE_DUEL" -> new String[]{"Bridge", ""};
                case "BRIDGE_DOUBLES" -> new String[]{"Bridge", "Doubles"};
                case "BRIDGE_THREES" -> new String[]{"Bridge", "Threes"};
                case "BRIDGE_TEAMS" -> new String[]{"Bridge", "Teams"};

                case "CLASSIC_DUEL" -> new String[]{"Classic", ""};
                case "CLASSIC_DOUBLES" -> new String[]{"Classic", "Doubles"};

                case "OP_DUEL" -> new String[]{"Op", ""};
                case "OP_DOUBLES" -> new String[]{"Op", "Doubles"};

                case "SKYWARS_DUEL" -> new String[]{"Skywars", ""};
                case "SKYWARS_DOUBLES" -> new String[]{"Skywars", "Doubles"};

                case "UHC_DUEL" -> new String[]{"Uhc", ""};
                case "UHC_DOUBLES" -> new String[]{"Uhc", "Doubles"};
                case "UHC_TEAMS" -> new String[]{"Uhc", "Teams"};
                case "UHC_DEATHMATCH" -> new String[]{"Uhc", "Deathmatch"};

                case "BLITZ_DUEL" -> new String[]{"Blitz", ""};
                case "BOW_DUEL" -> new String[]{"Bow", ""};
                case "BOW_SPLEEF_DUEL" -> new String[]{"Bow_Spleef", ""};
                case "BOXING_DUEL" -> new String[]{"Boxing", ""};
                case "COMBO_DUEL" -> new String[]{"Combo", ""};
                case "DUEL_ARENA" -> new String[]{"Duel_Arena", "  "};
                case "MW_DUEL" -> new String[]{"Mega_Wall", ""};
                case "NODEBUFF_DUEL" -> new String[]{"NoDebuff", ""};
                case "PARKOUR_DUEL" -> new String[]{"Parkour", " "};
                case "QUAKE_DUEL" -> new String[]{"Quakecraft", ""};
                case "SPLEEF_DUEL" -> new String[]{"Spleef", ""};
                case "SUMO_DUEL" -> new String[]{"Sumo", ""};

                default -> new String[]{duels.getRecentlyPlayed()[i], ""};
            };
            BufferedImage duelImage = HypixelUtil.loadImage("/duels/recently_played/", duel[0].toLowerCase(), null);
            g2d.drawImage(duelImage, 1900, 800 + 725 * (i + 1) / duels.getRecentlyPlayed().length, 100, 100, null);
            StringBuilder str = new StringBuilder();
            for (String s : duel)
                str.append(s).append(" ");
            fontRenderer.drawString(str.toString().trim(), 2065, 825 + 725 * (i + 1) / duels.getRecentlyPlayed().length);

            fontRenderer.switchFont(3);
            String num = switch (duel[1])
            {
                case "Doubles" -> "2";
                case "Threes" -> "3";
                case "Deathmatch", " " -> "8";
                case "  " -> "40";

                default -> "";
            };
            fontRenderer.drawString(num, 2050, 855 + 725 * (i + 1) / duels.getRecentlyPlayed().length, FontRenderer.RightAligned);
            fontRenderer.switchFont(2);
        }

        // level info
        fontRenderer.drawString(duels.getRankFormatted(), 1440, 1275, FontRenderer.CenterXAligned);
        fontRenderer.drawString(String.format("§a%s", rankProgressBar), 1440, 1418, FontRenderer.CenterXAligned);
        fontRenderer.drawString(String.format("§a%d §r/ §c%d", winRankOverflow, winRankReq), 1440, 1275 + 148 * 2, FontRenderer.CenterXAligned);

        fontRenderer.drawString("§aLevel:", 1450, 1785);
        fontRenderer.drawString("§c" + player.getLevel(), 1450, 1890);

        fontRenderer.switchFont(1);
        fontRenderer.drawString(String.format("§a%s", prestigeProgressBar), 2344, 1800, FontRenderer.CenterXAligned);
        fontRenderer.drawString(duels.getNextPrestigeFormatted(), 2344, 1975, FontRenderer.CenterXAligned);

        // ping
        fontRenderer.switchFont(3);
        fontRenderer.drawString("§a±§c" + duels.getPingPreference() + "ms", 1450, 2015);

        // draw player images (last cause we were doing this on another thread)
        BufferedImage playerFull = ImageUtil.NULL_TEXTURE;
        BufferedImage playerTop = ImageUtil.NULL_TEXTURE;

        try
        {
            playerFull = playerFuture.get();
            playerTop = playerTopFuture.get();
        }
        catch (InterruptedException ignored)
        {
        }
        catch (ExecutionException e)
        {
            System.err.println("Failed to get player icons: " + e.getMessage());
            e.printStackTrace(System.err);
        }

        int[] playerSize = ImageUtil.fitToArea(playerFull, 670, 850);
        int[] playerTopSize = ImageUtil.fitToArea(playerTop, Integer.MAX_VALUE, 300);

        g2d.drawImage(playerFull, 1440 - (playerSize[0] / 2), 325 + ((850 - playerSize[1]) / 2), playerSize[0], playerSize[1], null);
        g2d.drawImage(playerTop, 1155, 1785, playerTopSize[0], playerTopSize[1], null);

        // output and return image
        g2d.dispose();

        return image;
    }

    @Override
    public void addFields(SlashCommandData data)
    {
        data.addOption(STRING, "name", "Name of Player", true);
    }
}
