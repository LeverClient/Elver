package com.lcv.commands.hypixel;

import com.lcv.Main;
import com.lcv.commands.ICommand;
import com.lcv.commands.Embed;
import com.lcv.elverapi.apis.hypixelplayer.BedwarsAPI;
import com.lcv.elverapi.apis.hypixelplayer.HypixelPlayerAPI;
import com.lcv.elverapi.apis.mojang.MojangProfileLookupAPI;
import com.lcv.util.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

import static com.lcv.util.ImageUtil.loadImage;
import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

public class Bedwars implements ICommand
{
    private static final Logger log = LoggerFactory.getLogger(Bedwars.class);

    public static final int PRESTIGE_PROGRESS_BAR_LENGTH = 20;
    public static final int LEVEL_PROGRESS_BAR_LENGTH = 30;

    private static final String API_KEY_HYPIXEL = System.getenv("API_KEY_HYPIXEL");

    private static final Random rand = new Random();
    public static ArrayList<BufferedImage> backgroundImages = new ArrayList<>();
    public static FontRenderer fontRenderer = new FontRenderer(null, new Font[]{Main.minecraftFont.deriveFont(144f), Main.minecraftFont.deriveFont(96f), Main.minecraftFont.deriveFont(72f), Main.minecraftFont.deriveFont(40f)});

    public final int availableBackgrounds = ImageUtil.getBackgrounds(backgroundImages, "bedwars/bedwars_overlay", (g2d) ->
    {
        g2d.drawImage(Main.botProfileScaled, 25, 25, 226, 226, null);
        g2d.drawImage(ImageUtil.BEDWARS_IRON_INGOT, 100, 1830, null);
        g2d.drawImage(ImageUtil.BEDWARS_GOLD_INGOT, 355, 1830, null);
        g2d.drawImage(ImageUtil.BEDWARS_DIAMOND, 610, 1820, null);
        g2d.drawImage(ImageUtil.BEDWARS_EMERALD, 850, 1830, null);
    });

    @Override
    public String getName() {
        return "bedwars";
    }

    @Override
    public String getDescription() {
        return "Gets Bedwars Stats";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        InteractionHook interactionHook = event.getHook();

        String name = event.getOption("name").getAsString();

        MojangProfileLookupAPI mojang = new MojangProfileLookupAPI(name);
        HypixelPlayerAPI player = new HypixelPlayerAPI(mojang.getUUID(), API_KEY_HYPIXEL);

        if (!player.hasData()) {
            Embed embed = new Embed().setTitle("Failed Operation :(").setDescription("Hypixel: No player found");
            interactionHook.sendMessageEmbeds(embed.get()).queue();
            return;
        }

        BufferedImage statsImage;
        try {
            statsImage = generateStatsImage(player);
        } catch (IllegalArgumentException e) {
            Embed embed = new Embed().setTitle("Failed Operation :(").setDescription(e.getMessage() == null ? "Unsure" : e.getMessage());
            interactionHook.sendMessageEmbeds(embed.get()).queue();
            return;
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(statsImage, "png", baos);
            FileUpload file = FileUpload.fromData(new ByteArrayInputStream(baos.toByteArray()), String.format("bedwars stats for %s meow.png", name));
            interactionHook.sendFiles(file).queue();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addFields(SlashCommandData data) {
        data.addOption(STRING, "name", "Name of Player", true);
    }

    public BufferedImage generateStatsImage(HypixelPlayerAPI player) throws IllegalArgumentException {
        long startTime = System.nanoTime();

        BedwarsAPI bedwars = player.getBedwarsApi();
        if (!bedwars.exists()) {
            throw new IllegalArgumentException("Hypixel: No bedwars stats");
        }

        DecimalFormat bigFormat = new DecimalFormat("###,###");

        int xpRankOverflow = bedwars.getXp() - BedwarsAPI.calculateXp(bedwars.getLevel());
        int xpRankReq = BedwarsAPI.calculateXp(bedwars.getLevel() + 1) - BedwarsAPI.calculateXp(bedwars.getLevel());

        int levelProgressBarCompletion = (int) (LEVEL_PROGRESS_BAR_LENGTH * bedwars.getLevelPercentage() / 100);
        String levelProgressBar = "|".repeat(levelProgressBarCompletion) + "§c" + "|".repeat(LEVEL_PROGRESS_BAR_LENGTH - levelProgressBarCompletion);

        int prestigeProgressBarCompletion = (int) (PRESTIGE_PROGRESS_BAR_LENGTH * bedwars.getPrestigePercentage() / 100);
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
        if (userSpecificSkin == null) {
            StatsSkins.none.apply(fontRenderer);
        } else {
            userSpecificSkin.apply(fontRenderer);
        }

        // start getting player images
        Future<BufferedImage> playerFuture = ImageUtil.getPlayerSkinFull(player.getUUID());
        Future<BufferedImage> playerTopFuture = ImageUtil.getPlayerSkinTop(player.getUUID());

        fontRenderer.useDefaultColors = true;
        fontRenderer.drawString(player.getNameFormatted(), 1440 - (g2d.getFontMetrics().stringWidth((FontRenderer.removeFormatting(player.getNameFormatted()))) / 2), 75);
        fontRenderer.useDefaultColors = false;

        fontRenderer.switchFont(1);

        fontRenderer.drawString(String.format("§aWins: %s", bigFormat.format(bedwars.getWins())), 75, 325);
        fontRenderer.drawString(String.format("§cLosses: %s", bigFormat.format(bedwars.getLosses())), 75, 510);
        fontRenderer.drawString(String.format("§aW§cL: §r%.2f", bedwars.getWLR()), 75, 700);

        fontRenderer.drawString(String.format("§aBB: %s", bigFormat.format(bedwars.getBedsBroken())), 75, 962);
        fontRenderer.drawString(String.format("§cBL: %s", bigFormat.format(bedwars.getBedsLost())), 75, 1147);
        fontRenderer.drawString(String.format("§aBB§cLR: §r%.2f", bedwars.getBBLR()), 75, 1337);

        fontRenderer.drawString(String.format("§aKills: %s", bigFormat.format(bedwars.getKills())), 1875, 325);
        fontRenderer.drawString(String.format("§cDeaths: %s", bigFormat.format(bedwars.getDeaths())), 1875, 510);
        fontRenderer.drawString(String.format("§aK§cD: §r%.2f", bedwars.getKDR()), 1875, 700);

        fontRenderer.drawString(String.format("§aFK: %s", bigFormat.format(bedwars.getFinalKills())), 1875, 962);
        fontRenderer.drawString(String.format("§cFD: %s", bigFormat.format(bedwars.getFinalDeaths())), 1875, 1147);
        fontRenderer.drawString(String.format("§aFK§cDR: §r%.2f", bedwars.getFKDR()), 1875, 1337);

        // level info
        fontRenderer.switchFont(2);

        fontRenderer.drawString(bedwars.getLevelFormatted(), image.getWidth() / 2, 1275, FontRenderer.CenterXAligned); // 1350
        fontRenderer.drawString(String.format("§a%s", levelProgressBar), image.getWidth() / 2, 1275 + 143, FontRenderer.CenterXAligned);
        fontRenderer.drawString(String.format("§a%d §r/ §c%d", xpRankOverflow, xpRankReq), image.getWidth() / 2, 1275 + 148 * 2, FontRenderer.CenterXAligned);

        fontRenderer.drawString("§aLevel:", 1450, 1785);
        fontRenderer.drawString("§c" + (int) player.getLevel(), 1450, 1890);

        // progress bar
        fontRenderer.drawString(String.format("§a%s  §r>>>  §!%s", prestigeProgressBar, bedwars.getNextPrestigeFormatted()), 540, 1625 - 9, FontRenderer.CenterXAligned);

        Function<Integer, String> numAbbrev = num ->
        {
            String[] arr = {"", "K", "M", "B", "T"};
            int i = 0;
            while (num > 999) {
                i++;
                num /= 1000;
            }
            return new DecimalFormat("#.##").format(num) + arr[i];
        };

        fontRenderer.switchFont(3);
        fontRenderer.drawString(numAbbrev.apply(bedwars.getIron()), 180, 2025, FontRenderer.CenterXAligned);
        fontRenderer.drawString(numAbbrev.apply(bedwars.getGold()), 435, 2025, FontRenderer.CenterXAligned);
        fontRenderer.drawString(numAbbrev.apply(bedwars.getDiamond()), 690, 2025, FontRenderer.CenterXAligned);
        fontRenderer.drawString(numAbbrev.apply(bedwars.getEmerald()), 930, 2025, FontRenderer.CenterXAligned);

        // quick buy
        // 1850, 1574; Size 980x537 980/(21/3) = 140px per slot horizontal
        // 358/3 = 119.3334px per slot vertical
        // ^ 358 because we're including quick buy and adding padding space for it

        // 980/9 = 108.8889px per quick buy item

        // 20px padding between each item?
        String[] quickBuys = bedwars.getQuickbuy();
        String[] favoriteSlots = bedwars.getHotbar();

        double quickBuyItemSpacingX = 141.42;
        double quickBuyItemSpacingY = 140;
        int quickBuyItemSize = (int) quickBuyItemSpacingX - 10;

        double slotItemSpacingX = 110;
        int slotItemSize = (int) slotItemSpacingX - 10;

        if (quickBuys != null) {
            String[] subdirectory = {"armor/", "blocks/", "melee/", "potions/", "ranged/", "tools/", "utility/"};
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 7; x++) {
                    int i = y * 7 + x;
                    BufferedImage itemImage = loadImage("bedwars/quickbuy/", quickBuys[i], subdirectory);

                    int iconX = 1850 + (int) (x * quickBuyItemSpacingX);
                    int iconY = 1562 + (int) (y * quickBuyItemSpacingY);

                    g2d.setColor(new Color(36, 36, 36, 128));
                    g2d.fillRoundRect(iconX, iconY, quickBuyItemSize, quickBuyItemSize, 36, 36);
                    g2d.drawImage(itemImage, iconX, iconY, quickBuyItemSize, quickBuyItemSize, null);
                }
            }
        }

        if (favoriteSlots != null) {
            for (int x = 0; x < favoriteSlots.length; x++) {
                BufferedImage itemImage = loadImage("bedwars/favorite_slots/", favoriteSlots[x], null);

                int iconX = 1850 + (int) (x * slotItemSpacingX);
                int iconY = 2122 - slotItemSize;

                g2d.setColor(new Color(36, 36, 36, 96));
                g2d.fillRoundRect(iconX, iconY, slotItemSize, slotItemSize, 36, 36);
                g2d.drawImage(itemImage, iconX, iconY, slotItemSize, slotItemSize, null);
            }
        }

        System.out.printf("generated bedwars stats image without player in %dms%n", (System.nanoTime() - startTime) / 1000000);

        // draw player images (last cause we were doing this on another thread)
        BufferedImage playerFull = Main.nullTexture;
        BufferedImage playerTop = Main.nullTexture;

        try {
            playerFull = playerFuture.get();
            playerTop = playerTopFuture.get();
        } catch (InterruptedException ignored) {
        } catch (ExecutionException e) {
            System.err.println("Failed to get player icons: " + e.getMessage());
            e.printStackTrace(System.err);
        }

        int[] playerSize = ImageUtil.fitToArea(playerFull, 670, 850);
        int[] playerTopSize = ImageUtil.fitToArea(playerTop, Integer.MAX_VALUE, 300);

        g2d.drawImage(playerFull, 1440 - (playerSize[0] / 2), 325 + ((850 - playerSize[1]) / 2), playerSize[0], playerSize[1], null);
        g2d.drawImage(playerTop, 1155, 1785, playerTopSize[0], playerTopSize[1], null);

        // output and return image
        g2d.dispose();

        System.out.printf("generated bedwars stats image in %dms%n", (System.nanoTime() - startTime) / 1000000);

        return image;
    }
}
