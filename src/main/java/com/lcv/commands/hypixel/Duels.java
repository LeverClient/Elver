package com.lcv.commands.hypixel;

import com.lcv.Main;
import com.lcv.commands.ICommand;
import com.lcv.commands.Embed;
import com.lcv.elverapi.apis.hypixelplayer.DuelsAPI;
import com.lcv.elverapi.apis.hypixelplayer.HypixelPlayerAPI;
import com.lcv.elverapi.apis.mojang.MojangProfileLookupAPI;
import com.lcv.util.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.lcv.util.ImageUtil.loadImage;
import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

public class Duels implements ICommand
{
    private static final String API_KEY_HYPIXEL = System.getenv("API_KEY_HYPIXEL");
    private static final int RANK_PROGRESS_BAR_LENGTH = 30;
    private static final int PRESTIGE_PROGRESS_BAR_LENGTH = 40;
    public final int availableBackgrounds = ImageUtil.getBackgrounds(backgroundImages, "duels/duels_overlay", (g2d) ->
    {
        g2d.drawImage(Main.botProfileScaled, 25, 25, 226, 226, null);
        g2d.drawImage(ImageUtil.DUELS_BOW, 1915, 300, 100, 100, null);
        g2d.drawImage(ImageUtil.DUELS_SWORD, 2455, 300, 100, 100, null);
        g2d.drawImage(ImageUtil.DUELS_HEALTH, 75, 1600, 150, 150, null);
        g2d.drawImage(ImageUtil.DUELS_DAMAGE, 65, 1765, 175, 175, null);
        g2d.drawImage(ImageUtil.DUELS_COIN, 75, 1935, 150, 150, null);
    });
    public static ArrayList<BufferedImage> backgroundImages = new ArrayList<>();
    public static FontRenderer fontRenderer = new FontRenderer(null, new Font[]{Main.minecraftFont.deriveFont(144f), Main.minecraftFont.deriveFont(96f), Main.minecraftFont.deriveFont(64f), Main.minecraftFont.deriveFont(56f)});
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

        Future<BufferedImage> playerFuture = ImageUtil.getPlayerSkinFull(player.getUUID());
        Future<BufferedImage> playerTopFuture = ImageUtil.getPlayerSkinTop(player.getUUID());

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
            String duel = switch (duels.getRecentlyPlayed()[i])
            {
                case "BOW_DUEL" -> "Bow";
                case "CLASSIC_DUEL" -> "Classic";
                case "OP_DUEL" -> "Op";
                case "UHC_DUEL" -> "Uhc";
                case "NODEBUFF_DUEL" -> "NoDebuff";
                case "MW_DUEL" -> "Mega_Wall";
                case "BLITZ_DUEL" -> "Blitz";
                case "SKYWARS_DUEL" -> "Skywars";
                case "COMBO_DUEL" -> "Combo";
                case "BOW_SPLEEF_DUEL" -> "Bow_Spleef";
                case "SPLEEF_DUEL" -> "Spleef";
                case "SUMO_DUEL" -> "Sumo";
                case "QUAKE_DUEL" -> "Quakecraft";
                case "BOXING_DUEL" -> "Boxing";
                case "BRIDGE_DUEL" -> "Bridge";
                case "BEDWARS_TWO_ONE_DUELS" -> "Bedwars";
                case "BEDWARS_TWO_ONE_DUELS_RUSH" -> "Bedrush";

                default -> duels.getRecentlyPlayed()[i];
            };
            BufferedImage duelImage = loadImage("/duels/recently_played/", duel.toLowerCase(), null);
            g2d.drawImage(duelImage, 1900, 800 + 725 * (i + 1) / duels.getRecentlyPlayed().length, 100, 100, null);
            fontRenderer.drawString(duel, 2065, 825 + 725 * (i + 1) / duels.getRecentlyPlayed().length);
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
        BufferedImage playerFull = Main.nullTexture;
        BufferedImage playerTop = Main.nullTexture;

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
