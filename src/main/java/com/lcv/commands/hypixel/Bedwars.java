package com.lcv.commands.hypixel;

import com.lcv.Main;
import com.lcv.commands.Command;
import com.lcv.commands.Embed;
import com.lcv.elverapi.apis.hypixelplayer.BedwarsAPI;
import com.lcv.elverapi.apis.hypixelplayer.HypixelPlayerAPI;
import com.lcv.elverapi.apis.mojang.MojangProfileLookupAPI;
import com.lcv.util.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.FileUpload;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.BiFunction;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

import static com.lcv.util.ImageUtil.loadItemImage;
import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

public class Bedwars implements Command
{
    private static final Logger log = LoggerFactory.getLogger(Bedwars.class);

    public static final int PRESTIGE_PROGRESS_BAR_LENGTH = 20;
    public static final int LEVEL_PROGRESS_BAR_LENGTH = 30;
    public static final double XP_PER_PRESTIGE = 487000;

    private static final String API_KEY_HYPIXEL = System.getenv("API_KEY_HYPIXEL");

    private static final Random rand = new Random();
    public static ArrayList<BufferedImage> backgroundImages = new ArrayList<>();
    public static FontRenderer fontRenderer = new FontRenderer(null, new Font[]{
            Main.minecraftFont.deriveFont(144f),
            Main.minecraftFont.deriveFont(96f),
            Main.minecraftFont.deriveFont(72f),
            Main.minecraftFont.deriveFont(40f)
    });

    public final int availableBackgrounds = ImageUtil.getBackgrounds(backgroundImages, "overlay_separate_hotbar", (g2d) -> {
        g2d.drawImage(Main.botProfileScaled, 25, 25, 226, 226, null);
        g2d.drawImage(ImageUtil.RESOURCE_IRON_INGOT, 100, 1830, null);
        g2d.drawImage(ImageUtil.RESOURCE_GOLD_INGOT, 355, 1830, null);
        g2d.drawImage(ImageUtil.RESOURCE_DIAMOND, 610, 1820, null);
        g2d.drawImage(ImageUtil.RESOURCE_EMERALD, 850, 1830, null);
    });
    @Override
    public String getName()
    {
        return "bedwars";
    }

    @Override
    public String getDescription()
    {
        return "Gets Bedwars Stats";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event)
    {
        event.deferReply().queue();
        InteractionHook interactionHook = event.getHook();

        String name = event.getOption("name").getAsString();

        MojangProfileLookupAPI mojang = new MojangProfileLookupAPI(name);
        HypixelPlayerAPI player = new HypixelPlayerAPI(mojang.getId(), API_KEY_HYPIXEL);

//        JSONObject mojangJson = HTTPRequest.getHTTPRequest("https://api.mojang.com/users/profiles/minecraft/" + name);
//        if (mojangJson == null || mojangJson.isEmpty())
//        {
//            Embed embed = new Embed().setTitle("Failed Operation :(").setDescription("Mojang: No player found");
//            interactionHook.sendMessageEmbeds(embed.get()).queue();
//            return;
//        }
//
//        String UUID = mojangJson.getString("id");
//        JSONObject hypixelJson = HTTPRequest.getHTTPRequest("https://api.hypixel.net/v2/player?key=" + API_KEY_HYPIXEL + "&uuid=" + UUID);
//        if (hypixelJson == null || hypixelJson.isEmpty())
//        {
//            Embed embed = new Embed().setTitle("Failed Operation :(").setDescription("Hypixel: No player found");
//            interactionHook.sendMessageEmbeds(embed.get()).queue();
//            return;
//        }
//
//        HypixelPlayerData hypixelData = new HypixelPlayerData(hypixelJson);
//        HypixelPlayerAPI player = new HypixelPlayerAPI(UUID, API_KEY_HYPIXEL);

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
            FileUpload file =  FileUpload.fromData(new ByteArrayInputStream(baos.toByteArray()), String.format("bedwars stats for %s meow.png", name));
            interactionHook.sendFiles(file).queue();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addFields(SlashCommandData data)
    {
        data.addOption(STRING, "name", "Name of Player", true);
    }

    public BufferedImage generateStatsImage(HypixelPlayerAPI player) throws IllegalArgumentException {
        long startTime = System.nanoTime();

        // should probably figure out how to handle this eventually
//        if (!hypixelData.valid)
//        {
//            throw new IllegalArgumentException("Hypixel: No player data found");
//        }
//
//        if (!hypixelData.stats.has("Bedwars") || hypixelData.stats.isNull("Bedwars"))
//        {
//            throw new IllegalArgumentException("Hypixel: No bedwars stats");
//        }

        BedwarsAPI bedwars = player.getBedwarsApi();

        BiFunction<JSONObject, String, String> getString = (json, s) -> json.has(s) && !json.isNull(s) ? json.getString(s) : null;

        DecimalFormat bigFormat = new DecimalFormat("###,###");

        int xpRankOverflow = bedwars.getXp() - BedwarsAPI.calculateXp(bedwars.getLevel());
        int xpRankReq = BedwarsAPI.calculateXp(bedwars.getLevel() + 1) - BedwarsAPI.calculateXp(bedwars.getLevel());

        int levelProgressBarCompletion = (int) (LEVEL_PROGRESS_BAR_LENGTH * bedwars.getLevelPercentage());
        String levelProgressBar = "|".repeat(levelProgressBarCompletion) + "§c" + "|".repeat(LEVEL_PROGRESS_BAR_LENGTH - levelProgressBarCompletion);

        int prestigeProgressBarCompletion = (int) (PRESTIGE_PROGRESS_BAR_LENGTH * bedwars.getPrestigePercentage());
        String prestigeProgressBar = "|".repeat(prestigeProgressBarCompletion) + "§c" + "|".repeat(PRESTIGE_PROGRESS_BAR_LENGTH - prestigeProgressBarCompletion);

        String favoriteSlotsString = getString.apply(bwJson,"favorite_slots");
        String quickBuy = getString.apply(bwJson,"favourites_2"); // favourites! definitely not worrying that this has _2 on it.. (foreshadowing)

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

        fontRenderer.drawString(bedwars.getLevelFormatted(), image.getWidth()/2, 1275, FontRenderer.CenterXAligned); // 1350
        fontRenderer.drawString(String.format("§a%s", levelProgressBar), image.getWidth()/2, 1275+148, FontRenderer.CenterXAligned);
        fontRenderer.drawString(String.format("§a%d §r/ §c%d", xpRankOverflow, xpRankReq), image.getWidth()/2, 1275+148*2, FontRenderer.CenterXAligned);

        fontRenderer.drawString("§aLevel:", 1440, 1785);
        fontRenderer.drawString("§c" + (int) player.getLevel(), 1440, 1890);

        // progress bar
        fontRenderer.drawString(String.format("§a%s  §r>>>  §!%s", prestigeProgressBar, bedwars.getNextPrestigeFormatted()), 540, 1625-9, FontRenderer.CenterXAligned);

        Function<Integer, String> numAbbrev = num ->
        {
            String[] arr = {"", "K", "M", "B", "T"};
            int i = 0;
            while(num > 999)
            {
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
        String[] quickBuys = quickBuy != null ? quickBuy.split(",")  : null;
        String[] favoriteSlots = favoriteSlotsString != null ? favoriteSlotsString.split(",") : null;

        double quickBuyItemSpacingX = 141.42;
        double quickBuyItemSpacingY = 140;
        int quickBuyItemSize = (int) quickBuyItemSpacingX-10;

        double slotItemSpacingX = 110;
        int slotItemSize = (int) slotItemSpacingX-10;

        System.out.println("I wanna die");

        if (quickBuys != null) {
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 7; x++) {
                    int i = y * 7 + x;
                    String item = quickBuys[i];
                    String itemIcon = switch (item) {
                        case "stick_(knockback_i)" -> "stick";
                        case "bow_(power_i)" -> "bow_POW1";
                        case "bow_(power_i__punch_i)" -> "bow_PUN1";

                        case "wooden_pickaxe" -> "wood_pickaxe";
                        case "wooden_axe" -> "wood_axe";

                        case "wool" -> "wool_colored_white";
                        case "wood", "oak_wood_planks" -> "planks_oak";
                        case "blast-proof_glass" -> "glass_white";
                        case "hardened_clay" -> "hardened_clay_stained_white";

                        case "speed_ii_potion_(45_seconds)" -> "potion_bottle_speed";
                        case "jump_v_potion_(45_seconds)" -> "potion_bottle_jump";
                        case "invisibility_potion_(30_seconds)" -> "potion_bottle_invis";

                        case "bridge_egg" -> "egg";
                        case "water_bucket" -> "bucket_water";
                        case "magic_milk" -> "bucket_milk";
                        case "golden_apple" -> "apple_golden";
                        case "dream_defender" -> "golem_egg";
                        case "compact_pop-up_tower" -> "popup_tower";

                        case "", " ", "null" -> "no_item";
                        default -> item;
                    };

                    BufferedImage itemImage = loadItemImage(itemIcon);

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
                String slotType = favoriteSlots[x];
                String slotIcon = switch (slotType) {
                    case "Melee" -> "wood_sword";
                    case "Ranged" -> "bow";
                    case "Utility" -> "fireball";
                    case "Tools" -> "wood_pickaxe";
                    case "Blocks" -> "wool_colored_white";

                    default -> "no_Slot";
                };

                BufferedImage itemImage = loadItemImage(slotIcon);

                int iconX = 1850 + (int) (x * slotItemSpacingX);
                int iconY = 2122 - slotItemSize;

                g2d.setColor(new Color(36, 36, 36, 96));
                g2d.fillRoundRect(iconX, iconY, slotItemSize, slotItemSize, 36, 36);
                g2d.drawImage(itemImage, iconX, iconY, slotItemSize, slotItemSize, null);
            }
        }

        System.out.printf("generated bedwars stats image without player in %dms%n", (System.nanoTime()-startTime)/1000000);

        // draw player images (last cause we were doing this on another thread)
        BufferedImage playerFull = Main.nullTexture;
        BufferedImage playerTop = Main.nullTexture;

        try {
            playerFull = playerFuture.get();
            playerTop = playerTopFuture.get();
        } catch (InterruptedException ignored) {}
        catch (ExecutionException e) {
            System.err.println("Failed to get player icons: " + e.getMessage());
            e.printStackTrace(System.err);
        }

        int[] playerSize = ImageUtil.fitToArea(playerFull, 670, 850);
        int[] playerTopSize = ImageUtil.fitToArea(playerTop, Integer.MAX_VALUE, 300);

        g2d.drawImage(playerFull, 1440 - (playerSize[0] / 2), 325 + ((850 - playerSize[1]) / 2), playerSize[0], playerSize[1], null);
        g2d.drawImage(playerTop, 1155, 1785, playerTopSize[0], playerTopSize[1], null);

        // output and return image
        g2d.dispose();

        System.out.printf("generated bedwars stats image in %dms%n", (System.nanoTime()-startTime)/1000000);

        return image;
    }

    public static Map<String, Double> getStats(HypixelPlayerData hypixelData)
    {
        JSONObject bwJson = hypixelData.stats.getJSONObject("Bedwars");

        BiFunction<JSONObject, String, Double> getDouble = (json, s) -> json.has(s) && !json.isNull(s) ? json.getDouble(s) : 0;
        BiFunction<Double, Double, Double> getRatio = (num, den) -> den == 0 ? 0 : num / den;

        Map<String, Double> stats = new HashMap<>();

        stats.put("iron", getDouble.apply(bwJson, "iron_resources_collected_bedwars"));
        stats.put("gold", getDouble.apply(bwJson, "gold_resources_collected_bedwars"));
        stats.put("diamond", getDouble.apply(bwJson, "diamond_resources_collected_bedwars"));
        stats.put("emerald", getDouble.apply(bwJson, "emerald_resources_collected_bedwars"));

        stats.put("wins", getDouble.apply(bwJson, "wins_bedwars"));
        stats.put("losses", getDouble.apply(bwJson, "losses_bedwars"));
        stats.put("wl", getRatio.apply(stats.get("wins"),stats.get("losses")));

        stats.put("finalKills", getDouble.apply(bwJson, "final_kills_bedwars"));
        stats.put("finalDeaths", getDouble.apply(bwJson, "final_deaths_bedwars"));
        stats.put("fkdr", getRatio.apply(stats.get("finalKills"), stats.get("finalDeaths")));

        stats.put("kills", getDouble.apply(bwJson, "kills_bedwars"));
        stats.put("deaths", getDouble.apply(bwJson, "deaths_bedwars"));
        stats.put("kd", getRatio.apply(stats.get("kills"), stats.get("deaths")));

        stats.put("bedsBroken", getDouble.apply(bwJson, "beds_broken_bedwars"));
        stats.put("bedsLost", getDouble.apply(bwJson, "beds_lost_bedwars"));
        stats.put("bblr", getRatio.apply(stats.get("bedsBroken"), stats.get("bedsLost")));

        stats.put("bedwarsXP", getDouble.apply(bwJson, "Experience"));
        stats.put("networkXP", getDouble.apply(hypixelData.player, "networkExp"));

        return stats;
    }

    // bedwars stars. %x$s = (x+3)th character of the level. including star. 0 = full string, 1 = full level, 2 = star. (i think)
    public final static String[] bedwarsPrestigeColors = {
            "§7 [ %1$s %2$s", // stone 0
            "§f [ %1$s %2$s", // iron 100
            "§6 [ %1$s %2$s", // gold 200
            "§b [ %1$s %2$s", // diamond 300
            "§2 [ %1$s %2$s", // emerald 400
            "§3 [ %1$s %2$s", // sapphire 500
            "§4 [ %1$s %2$s", // ruby 600
            "§d [ %1$s %2$s", // crystal 700
            "§9 [ %1$s %2$s", // opal 800
            "§5 [ %1$s %2$s", // amethyst 900
            "§c [ §6 %3$s §e %4$s §a %5$s §b %6$s §d %2$s §5", // rainbow 1000

            "§7 [ §f %1$s §7 %2$s §7", // iron prime 1100
            "§7 [ §e %1$s §6 %2$s §7", // gold prime 1200
            "§7 [ §b %1$s §3 %2$s §7", // diamond prime 1300
            "§7 [ §a %1$s §2 %2$s §7", // emerald prime 1400
            "§7 [ §3 %1$s §9 %2$s §7", // sapphire prime 1500
            "§7 [ §c %1$s §4 %2$s §7", // ruby prime 1600
            "§7 [ §d %1$s §5 %2$s §7", // crystal prime 1700
            "§7 [ §9 %1$s §1 %2$s §7", // opal prime 1800
            "§7 [ §5 %1$s §8 %2$s §7", // amethyst prime 1900

            "§8 [ §7 %3$s §f %4$s %5$s §7 %6$s %2$s §8", // mirror 2000
            "§f [ %3$s §e %4$s %5$s §6 %6$s %2$s", // light 2100
            "§6 [ %3$s §7 %4$s %5$s §b %6$s §3 %2$s", // dawn 2200
            "§5 [ %3$s §d %4$s %5$s §6 %6$s §e %2$s", // dusk 2300
            "§b [ %3$s §f %4$s %5$s §7 %6$s %2$s", // air 2400
            "§f [ %3$s §a %4$s %5$s §2 %6$s %2$s", // wind 2500
            "§4 [ %3$s §c %4$s %5$s §d %6$s %2$s", // nebula 2600
            "§e [ %3$s §f %4$s %5$s §8 %6$s %2$s", // thunder 2700
            "§a [ %3$s §2 %4$s %5$s §6 %6$s %2$s", // earth 2800
            "§b [ %3$s §3 %4$s %5$s §9 %6$s %2$s", // water 2900
            "§e [ %3$s §6 %4$s %5$s §c %6$s %2$s", // fire 3000

            "§9 [ %3$s §3 %4$s %5$s §6 %6$s %2$s §e", // 3100
            "§c [ §4 %3$s §7 %4$s %5$s §4 %6$s §c %2$s", // 3200
            "§9 [ %3$s %4$s §d %5$s §c %6$s %2$s §4", // 3300
            "§2 [ §a %3$s §d %4$s %5$s §5 %6$s %2$s §2", // 3400
            "§c [ %3$s §4 %4$s %5$s §2 %6$s §a %2$s", // 3500
            "§a [ %3$s %4$s §b %5$s §9 %6$s %2$s §1", // 3600
            "§4 [ %3$s §c %4$s %5$s §b %6$s §3 %2$s", // 3700
            "§1 [ %3$s §9 %4$s §5 %5$s %6$s §d %2$s §1", // 3800
            "§c [ %3$s §a %4$s %5$s §3 %6$s §9 %2$s", // 3900
            "§5 [ %3$s §c %4$s %5$s §6 %6$s %2$s §e", // 4000
            "§e [ %3$s §6 %4$s §c %5$s §d %6$s %2$s §5", // 4100
            "§1 [ §9 %3$s §3 %4$s §b %5$s §f %6$s §7 %2$s", // 4200
            "§0 [ §5 %3$s §8 %4$s %5$s §5 %6$s %2$s §0", // 4300
            "§2 [ %3$s §a %4$s §e %5$s §6 %6$s §5 %2$s §d", // 4400
            "§f [ %3$s §b %4$s %5$s §3 %6$s %2$s", // 4500
            "§3 [ §b %3$s §e %4$s %5$s §6 %6$s §d %2$s §5", // 4600
            "§f [ §4 %3$s §c %4$s %5$s §9 %6$s §1 %2$s §9", // 4700
            "§5 [ %3$s §c %4$s §6 %5$s §e %6$s §b %2$s §3", // 4800
            "§2 [ §a %3$s §f %4$s %5$s §a %6$s %2$s §2", // 4900
            "§4 [ %3$s §5 %4$s §9 %5$s %6$s §1 %2$s §0", // 5000
    };

    // bedwars stars. each index in the array corresponds to 1000 stars higher
    public final static String[] bedwarsPrestigeStars = {
            "✫",
            "✪",
            "⚝",
            "✥"
    };

    /**
     * @return a pretty bedwars level! colors and everything!
     */
    public static String getFormattedLevel(int level) {
        int prestiges = level / 100;
        int bigger_prestiges = (level - 100) / 1000; // what are these called anyway?

        // get star color & suffix
        String color = bedwarsPrestigeColors[Math.min(prestiges, bedwarsPrestigeColors.length - 1)];
        String suffix = bedwarsPrestigeStars[Math.min(bigger_prestiges, bedwarsPrestigeStars.length - 1)];
        String levelStr = Integer.toString(level);
        int levelLen = levelStr.length();

        // list of only numbers
        // groups[0] is the entire number
        // groups[1] is the star
        // after that it's just each character of the number

        Object[] groups = new String[levelLen + 2];
        groups[0] = levelStr;
        groups[1] = suffix;
        for (int i = 0; i < levelLen; i++) {
            groups[i + 2] = levelStr.substring(i, i + 1);
        }

        // sob sob 5 digit sob
        if (levelLen > 4) {
            groups[3 + 2] = levelStr.substring(3);
        }

        // format the correct regex with each character.
        try {
            return String.format(color, groups).replace(" ", "") + "]";
        } catch (Exception e) {
            System.err.println(Arrays.toString(e.getStackTrace()));
            return "§c[ERR]";
        }
    }

    public static int getXpForPrestige(double level) {
        int levelInt = (int) level;
        int respectPrestige = levelInt % 100;
        int levelXp = (int) ((level - levelInt)*5000);

        if (respectPrestige > 4) {
            return (100 - levelInt) * 5000 - levelXp;
        }

        int extraXpNeeded = 0;

        for (int i = respectPrestige; i < 5; i++) {
            extraXpNeeded += getBWExpForLevel(i);
        }

        return (100 - 4) * 5000 + extraXpNeeded - levelXp;
    }

    // stole this code from plancke's github. i don't know how it works
    // actually i might've found it on the hypixel forums, but it's originally from here. i don't remember if i ported it to java myself
    // https://github.com/Plancke/hypixel-php/blob/2303c4bdedb650acc8315393885284dba59fdd79/src/util/games/bedwars/ExpCalculator.php
    public static int getBWExpForLevel(int level) {
        return switch (level % 100) {
            case 0 -> 0;
            case 1 -> 500;
            case 2 -> 1000;
            case 3 -> 2000;
            case 4 -> 3500;
            default -> 5000;
        };
    }

    public static double getLevelForExp(int exp) {
        int prestiges = (int) (exp / XP_PER_PRESTIGE);
        int level = prestiges * 100;
        int expWithoutPrestiges = (int) (exp - (prestiges * XP_PER_PRESTIGE));

        for (int i = 1; i <= 4; i++) {
            int expForEasyLevel = getBWExpForLevel(i);
            if (expWithoutPrestiges < expForEasyLevel) break;
            level++;
            expWithoutPrestiges -= expForEasyLevel;
        }

        return level + (double) expWithoutPrestiges / 5000;
    }

    public static int getBedwarsLevel(int xp)
    {
        int level = (xp / 487000) * 100;
        for (int[] easyXP = {500, 1000, 2000, 3500}; (xp %= 487000) >= (level % 100 < 4 ? easyXP[level % 100] : 5000); xp -= level % 100 < 4 ? easyXP[level % 100] : 5000, level++);
        return level;
    }
}
