package com.lcv.commands.hypixel;

import com.lcv.Main;
import com.lcv.commands.Command;
import com.lcv.commands.Embed;
import com.lcv.util.FontRenderer;
import com.lcv.util.HTTPRequest;
import com.lcv.util.StatsSkins;
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
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;

import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

public class Bedwars implements Command
{
    private static final Logger log = LoggerFactory.getLogger(Bedwars.class);

    public static int prestigeProgressBarLength = 20;
    public static int levelProgressBarLength = 30;
    public static double xpPerPrestige = getXpForPrestige(1);

    private static final Random rand = new Random();
    ArrayList<BufferedImage> backgroundImages;
    static FontRenderer fontRenderer = new FontRenderer(null, new Font[]{
            Main.minecraftFont.deriveFont(144f),
            Main.minecraftFont.deriveFont(96f),
            Main.minecraftFont.deriveFont(72f),
            Main.minecraftFont.deriveFont(40f)
    });
    public Bedwars() {
        backgroundImages = getBackgrounds();
    }

    public static BufferedImage copyImage(BufferedImage image) {
        BufferedImage clone = new BufferedImage(image.getWidth(),
                image.getHeight(), image.getType());
        Graphics2D g2d = clone.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return clone;
    }

    int availableBackgrounds = 0;
    public ArrayList<BufferedImage> getBackgrounds()
    {
        ArrayList<BufferedImage> backgrounds = new ArrayList<>(8);
        try
        {
            for (;;availableBackgrounds++)
            {
                URL resource = Main.class.getResource(String.format("/images/Backgrounds/bedwarsBackground%s.png", availableBackgrounds));
                if (resource == null) break;

                BufferedImage image = ImageIO.read(new File(resource.toURI()));

                // draw overlay on background
                Graphics2D g2d = image.createGraphics();

                g2d.drawImage(ImageIO.read(new File(Objects.requireNonNull(Main.class.getResource("/images/overlay_separate_hotbar.png")).toURI())), 0, 0, null);
                //g2d.fillRect(1850, 1995, 980, 5);

                g2d.drawImage(ImageIO.read(new File(Objects.requireNonNull(Main.class.getResource("/images/Resources/iron_ingot.png")).toURI())), 100, 1830, null);
                g2d.drawImage(ImageIO.read(new File(Objects.requireNonNull(Main.class.getResource("/images/Resources/gold_ingot.png")).toURI())), 355, 1830, null);
                g2d.drawImage(ImageIO.read(new File(Objects.requireNonNull(Main.class.getResource("/images/Resources/diamond.png")).toURI())), 610, 1820, null);
                g2d.drawImage(ImageIO.read(new File(Objects.requireNonNull(Main.class.getResource("/images/Resources/emerald.png")).toURI())), 850, 1830, null);

                g2d.dispose();


                // save background
                backgrounds.add(image);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        System.out.printf("Loaded %d backgrounds%n", availableBackgrounds);
        return backgrounds;
    }

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

        String name = Objects.requireNonNull(event.getOption("name")).getAsString();
        JSONObject mojangJson = HTTPRequest.getHTTPRequest("https://api.mojang.com/users/profiles/minecraft/" + name);
        if (mojangJson == null || mojangJson.isEmpty())
        {
            Embed embed = new Embed().setTitle("Failed Operation :(").setDescription("Mojang: No player found");
            interactionHook.editOriginalEmbeds(embed.get()).queue();
            return;
        }

        String key = System.getenv("HYPIXEL_KEY");
        String UUID = mojangJson.getString("id");

        JSONObject hypixelJson = HTTPRequest.getHTTPRequest("https://api.hypixel.net/v2/player?key=" + key + "&uuid=" + UUID);
        if (hypixelJson == null || hypixelJson.isEmpty())
        {
            Embed embed = new Embed().setTitle("Failed Operation :(").setDescription("Hypixel: No player found");
            interactionHook.editOriginalEmbeds(embed.get()).queue();
            return;
        }

        HypixelPlayerData hypixelData = new HypixelPlayerData(hypixelJson);

        BufferedImage statsImage;
        try {
            statsImage = generateStatsImage(hypixelData);
        } catch (IllegalArgumentException e) {
            Embed embed = new Embed().setTitle("Failed Operation :(").setDescription(e.getMessage() ==  null ? "Unsure" : e.getMessage());
            interactionHook.editOriginalEmbeds(embed.get()).queue();
            return;
        }

        try
        {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(statsImage, "png", outputStream);
            FileUpload f =  FileUpload.fromData(new ByteArrayInputStream(outputStream.toByteArray()), String.format("bedwars stats for %s meow.png", name));
            interactionHook.sendFiles(f).queue();
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

    public BufferedImage generateStatsImage(HypixelPlayerData hypixelData) throws IllegalArgumentException {
        long startTime = System.nanoTime();

        if (!hypixelData.valid)
        {
            throw new IllegalArgumentException("Hypixel: No player data found");
        }


        if (!hypixelData.stats.has("Bedwars") || hypixelData.stats.isNull("Bedwars"))
        {
            throw new IllegalArgumentException("Hypixel: No bedwars stats");
        }

        JSONObject bwJson = hypixelData.stats.getJSONObject("Bedwars");
//        if (bwJson == null || !bwJson.has("kills_bedwars") || bwJson.isNull("kills_bedwars"))
//        {
//            throw new IllegalArgumentException("Hypixel: No bedwars stats");
//        }

        Function<String, Integer> getInt = s -> bwJson.has(s) && !bwJson.isNull(s) ? bwJson.getInt(s) : 0;
        Function<String, Double> getDouble = s -> bwJson.has(s) && !bwJson.isNull(s) ? bwJson.getDouble(s) : 0;
        Function<String, String> getString = s -> bwJson.has(s) && !bwJson.isNull(s) ? bwJson.getString(s) : null;

        DecimalFormat bigFormat = new DecimalFormat("###,###");

        double iron = getDouble.apply("iron_resources_collected_bedwars");
        double gold = getDouble.apply("gold_resources_collected_bedwars");
        double diamond = getDouble.apply("diamond_resources_collected_bedwars");
        double emerald = getDouble.apply("emerald_resources_collected_bedwars");

        double losses = getDouble.apply("losses_bedwars");
        double wins = getDouble.apply("wins_bedwars");
        double finalKills = getDouble.apply("final_kills_bedwars");
        double finalDeaths = getDouble.apply("final_deaths_bedwars");
        double kills = getDouble.apply("kills_bedwars");
        double deaths = getDouble.apply("deaths_bedwars");
        double bedsBroken = getDouble.apply("beds_broken_bedwars");
        double bedsLost = getDouble.apply("beds_lost_bedwars");

        double WL = wins / losses;
        double fkdr = finalKills / finalDeaths;
        double kdr = kills / deaths;
        double bblr = bedsBroken / bedsLost;

        int xp = getInt.apply("Experience");
        double networkXP = hypixelData.player.has("networkExp") && !hypixelData.player.isNull("networkExp") ? hypixelData.player.getInt("networkExp") : 0;

        int networkLevel = (int) (1 + ((-8750 + Math.sqrt(8750 * 8750 + 5000 * networkXP)) / 2500));

        double level_d = getLevelForExp(xp);
        int level = (int) level_d;
        int nextLevel = level + 1;
        int currentPrestige = (int) Math.floor(level_d / 100)*100;
        int nextPrestige = (int) Math.ceil(level_d / 100)*100;

        int xpReq = getBWExpForLevel(level);
        int xpPastLevel = (int) Math.round(((level_d - level) * xpReq)/5)*5;

        int levelProgressBars = xpPastLevel / (xpReq / levelProgressBarLength);
        String levelProgressBarString = "|".repeat(levelProgressBars) + "§c" + "|".repeat(Math.max(0, levelProgressBarLength - levelProgressBars));

        double xpUntilPrestige = getXpForPrestige(level_d - currentPrestige);
        int prestigeProgressBars = (int) ((xpPerPrestige - xpUntilPrestige) / (xpPerPrestige / prestigeProgressBarLength));
        String prestigeProgressBarString = "|".repeat(prestigeProgressBars) + "§c" + "|".repeat(Math.max(0, prestigeProgressBarLength - prestigeProgressBars));

        String formattedLevel = getFormattedLevel(level);
        String formattedNextLevel = getFormattedLevel(nextLevel);
        String formattedNextPrestige = getFormattedLevel(nextPrestige);

        String favoriteSlotsString = getString.apply("favorite_slots");
        String quickBuy = getString.apply("favourites_2"); // favourites! definitely not worrying that this has _2 on it.. (foreshadowing)

        int chosenBackground = availableBackgrounds <= 1 ? 0 : rand.nextInt(0, availableBackgrounds);
        BufferedImage image = copyImage(backgroundImages.get(chosenBackground));

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
        } else {
            userSpecificSkin.apply(fontRenderer);
        }

        // draw bot profile
        g2d.drawImage(Main.botProfileScaled, 25, 25, 226, 226, null);

        try
        {
            String[] renderList = {"default", "marching", "walking", "crouching", "crossed", "criss_cross", "ultimate", "cheering", "relaxing", "trudging", "cowering", "pointing", "lunging", "dungeons", "facepalm", "sleeping", "archer", "kicking", "mojavatar", "reading"};
            String renderType = renderList[rand.nextInt(0, renderList.length)];
            if (hypixelData.uuid.equals("ddf13e436ccc4790bb49912913bf7d77")) {
                renderType = "mojavatar";
            }

            BufferedImage player = ImageIO.read(new URL(String.format("https://starlightskins.lunareclipse.studio/render/%s/%s/full", renderType, hypixelData.uuid)));

            double playerWidth = player.getWidth();
            double playerHeight = player.getHeight();
            double areaWidth = 670;
            double areaHeight = 850;
            double ratio;

            if ((areaWidth / playerWidth) * playerHeight < areaHeight)
            {
                ratio = (areaWidth / playerWidth);
            }
            else
            {
                ratio = (areaHeight / playerHeight);
            }

            int width = (int) (playerWidth * ratio);
            int height = (int) (playerHeight * ratio);
            g2d.drawImage(player, 1440 - (width / 2), 325 + ((850 - height) / 2), width, height, null);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        try
        {
            String[] renderList = {"default", "crossed", "ultimate", "dungeons"};
            String renderType = renderList[rand.nextInt(0, renderList.length)];

            BufferedImage player = ImageIO.read(new URL(String.format("https://starlightskins.lunareclipse.studio/render/%s/%s/bust", renderType, hypixelData.uuid)));

            double playerWidth = player.getWidth();
            double playerHeight = player.getHeight();
            double areaHeight = 300;
            double ratio = areaHeight / playerHeight;
            int width = (int) (playerWidth * ratio);
            int height = (int) (playerHeight * ratio);

            g2d.drawImage(player, 1155, 1785, width, height, null);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        // draw player name
        String nameWithRank = hypixelData.getPlayerNameRankFormat();
        fontRenderer.drawString(nameWithRank, 1440 - (g2d.getFontMetrics().stringWidth((FontRenderer.removeFormatting(nameWithRank))) / 2), 75);

        fontRenderer.switchFont(1);
        fontRenderer.drawString(String.format("§aWins: %s", bigFormat.format(wins)), 75, 325);
        fontRenderer.drawString(String.format("§cLosses: %s", bigFormat.format(losses)), 75, 510);
        fontRenderer.drawString(String.format("§aW§cL: §r%.2f", WL), 75, 700);

        fontRenderer.drawString(String.format("§aBB: %s", bigFormat.format(bedsBroken)), 75, 962);
        fontRenderer.drawString(String.format("§cBL: %s", bigFormat.format(bedsLost)), 75, 1147);
        fontRenderer.drawString(String.format("§aBB§cLR: §r%.2f", bblr), 75, 1337);

        fontRenderer.drawString(String.format("§aKills: %s", bigFormat.format(kills)), 1875, 325);
        fontRenderer.drawString(String.format("§cDeaths: %s", bigFormat.format(deaths)), 1875, 510);
        fontRenderer.drawString(String.format("§aK§cD: §r%.2f", kdr), 1875, 700);

        fontRenderer.drawString(String.format("§aFK: %s", bigFormat.format(finalKills)), 1875, 962);
        fontRenderer.drawString(String.format("§cFD: %s", bigFormat.format(finalDeaths)), 1875, 1147);
        fontRenderer.drawString(String.format("§aFK§cDR: §r%.2f", fkdr), 1875, 1337);

        // level info
        fontRenderer.switchFont(2);
        fontRenderer.drawString(formattedLevel, image.getWidth()/2, 1275, FontRenderer.CenterXAligned); // 1350
        fontRenderer.drawString(String.format("§a%s", levelProgressBarString), image.getWidth()/2, 1275+148, FontRenderer.CenterXAligned);
        fontRenderer.drawString(String.format("§a%d §r/ §c%d", xpPastLevel, xpReq), image.getWidth()/2, 1275+148*2, FontRenderer.CenterXAligned);
        //fontRenderer.drawString(formattedNextLevel, image.getWidth()/2, 1275+148*2, FontRenderer.CenterXAligned);


        fontRenderer.drawString("§aLevel:", 1440, 1785);
        fontRenderer.drawString("§c" + networkLevel, 1440, 1890);

        // progress bar
        fontRenderer.drawString(String.format("§a%s  §r>>>  %s", prestigeProgressBarString, formattedNextPrestige), 540, 1625-9, FontRenderer.CenterXAligned);

        Function<Double, String> numAbbrev = num ->
        {
            String[] arr = {"", "K", "M", "B", "T"};
            int i = 0;
            while(num > 999)
            {
                i++;
                num /= 1000;
            }
            return String.format("%.2f%s", num, arr[i]);
        };

        fontRenderer.switchFont(3);
        fontRenderer.drawString(numAbbrev.apply(iron), 180, 2025, FontRenderer.CenterXAligned);
        fontRenderer.drawString(numAbbrev.apply(gold), 435, 2025, FontRenderer.CenterXAligned);
        fontRenderer.drawString(numAbbrev.apply(diamond), 690, 2025, FontRenderer.CenterXAligned);
        fontRenderer.drawString(numAbbrev.apply(emerald), 930, 2025, FontRenderer.CenterXAligned);

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
                        case "oak_wood_planks" -> "planks_oak";
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

        // output and return image
        g2d.dispose();

        System.out.printf("generated bedwars stats image in %dms%n", (System.nanoTime()-startTime)/1000000);

        return image;
    }

    public HashMap<String, BufferedImage> itemIconCache = new HashMap<>();
    public BufferedImage loadItemImage(String item) {
        if (itemIconCache.containsKey(item)) return itemIconCache.get(item);

        URL itemIconResource = Main.class.getResource("/images/Items/" + item + ".png");
        if (itemIconResource == null) {
            itemIconResource = Main.class.getResource("/images/Blocks/" + item + ".png");
            if (itemIconResource == null) {
                itemIconResource = Main.class.getResource("/images/" + item + ".png");
                if (itemIconResource == null)
                {
                    System.err.println("Couldn't find texture for " + item);
                    return Main.nullTexture;
                }
            }
        }

        try {
            BufferedImage image = ImageIO.read(new File(itemIconResource.toURI()));
            itemIconCache.put(item, image);

            return image;
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace(System.err);
            return Main.nullTexture;
        }
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
        int respectPrestige = getLevelRespectingPrestige(levelInt);
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
    public static final int EASY_LEVELS = 4;
    public static final int EASY_LEVELS_XP = 7000;
    public static final int XP_PER_PRESTIGE = 96 * 5000 + EASY_LEVELS_XP;
    public static final int LEVELS_PER_PRESTIGE = 100;
    public static final int HIGHEST_PRESTIGE = 10;
    public static int getBWExpForLevel(int level) {
        if (level == 0) return 0;

        int respectedLevel = getLevelRespectingPrestige(level);

        return switch (respectedLevel) {
            case 1 -> 500;
            case 2 -> 1000;
            case 3 -> 2000;
            case 4 -> 3500;
            default -> 5000;
        };
    }

    public static int getLevelRespectingPrestige(int level) {
        /*if (level > HIGHEST_PRESTIGE * LEVELS_PER_PRESTIGE) {
            return level - HIGHEST_PRESTIGE * LEVELS_PER_PRESTIGE;
        } else {
            return level % LEVELS_PER_PRESTIGE;
        }*/

        return level % LEVELS_PER_PRESTIGE;
    }

    public static double getLevelForExp(int exp) {
        int prestiges = exp / XP_PER_PRESTIGE;
        int level = prestiges * LEVELS_PER_PRESTIGE;
        int expWithoutPrestiges = exp - (prestiges * XP_PER_PRESTIGE);

        for (int i = 1; i <= EASY_LEVELS; i++) {
            int expForEasyLevel = getBWExpForLevel(i);
            if (expWithoutPrestiges < expForEasyLevel) break;
            level++;
            expWithoutPrestiges -= expForEasyLevel;
        }

        return level + (double) expWithoutPrestiges / 5000;
    }
}
