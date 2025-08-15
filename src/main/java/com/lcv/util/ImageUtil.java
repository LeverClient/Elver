package com.lcv.util;

import com.lcv.Main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class ImageUtil
{
    public static final String[] PLAYER_SKIN_FULL = {"default", "marching", "walking", "crouching", "crossed", "criss_cross", "ultimate", "cheering", "relaxing", "trudging", "cowering", "pointing", "lunging", "dungeons", "facepalm", "sleeping", "archer", "kicking", "mojavatar", "reading"};
    public static final String[] PLAYER_SKIN_TOP = {"default", "crossed", "ultimate", "dungeons"};
    public static final String[] ITEM_ICON_DIRECTORIES = {"/images/Items/", "/images/Blocks/"};
    public static final BufferedImage RESOURCE_IRON_INGOT = loadImage("/images/Resources/iron_ingot.png");
    public static final BufferedImage RESOURCE_GOLD_INGOT = loadImage("/images/Resources/gold_ingot.png");
    public static final BufferedImage RESOURCE_DIAMOND = loadImage("/images/Resources/diamond.png");
    public static final BufferedImage RESOURCE_EMERALD = loadImage("/images/Resources/emerald.png");
    public static HashMap<String, BufferedImage> BACKGROUND_OVERLAYS = new HashMap<>();
    public static final HashMap<String, BufferedImage> ITEM_ICONS = new HashMap<>();
    public static Random rand = new Random();
    public static ExecutorService asyncExecutorService = Executors.newCachedThreadPool();

    public static BufferedImage loadImage(String name)
    {
        try
        {
            return ImageIO.read(Main.class.getResource(name));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static BufferedImage copyImage(BufferedImage image)
    {
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        Graphics2D g2d = copy.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return copy;
    }

    public static Future<BufferedImage> getPlayerSkinFull(String uuid) {
        return asyncExecutorService.submit(() -> {
            String skinType = PLAYER_SKIN_FULL[rand.nextInt(PLAYER_SKIN_FULL.length)];
            if (uuid.equals("ddf13e436ccc4790bb49912913bf7d77")) {
                skinType = "mojavatar";
            }

            for (int i = 0; i < 3; i++) {
                try {
                    return ImageIO.read(new URL(String.format("https://starlightskins.lunareclipse.studio/render/%s/%s/full", skinType, uuid)));
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }
            throw new RuntimeException("Failed to get full player skin (probably a 502 from the api)");
        });
    }

    public static Future<BufferedImage> getPlayerSkinTop(String uuid) {
        return asyncExecutorService.submit(() -> {
            String skinType = PLAYER_SKIN_TOP[rand.nextInt(PLAYER_SKIN_TOP.length)];
            for (int i = 0; i < 3; i++) {
                try {
                    return ImageIO.read(new URL(String.format("https://starlightskins.lunareclipse.studio/render/%s/%s/bust", skinType, uuid)));
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }
            throw new RuntimeException("Failed to get top player skin (probably a 502 from the api)");
        });
    }

    public static int[] fitToArea(BufferedImage image, int areaWidth, int areaHeight) {
        double imgWidth = image.getWidth();
        double imgHeight = image.getHeight();
        double ratio;

        if ((areaHeight / imgHeight) * imgWidth < areaWidth)
        {
            ratio = (areaHeight / imgHeight);
        }
        else
        {
            ratio = (areaWidth / imgWidth);
        }

        int width = (int) (imgWidth * ratio);
        int height = (int) (imgHeight * ratio);

        return new int[]{width, height};
    }

    public static BufferedImage loadItemImage(String item) {
        if (ITEM_ICONS.containsKey(item)) return ITEM_ICONS.get(item);

        URL itemIconResource = null;
        for (String directory : ITEM_ICON_DIRECTORIES) {
            itemIconResource = Main.class.getResource(directory + item + ".png");
            if (itemIconResource != null) break;
        }

        if (itemIconResource == null) {
            System.err.println("Couldn't find texture for " + item);
            return Main.nullTexture;
        }

        try {
            BufferedImage image = ImageIO.read(itemIconResource);
            ITEM_ICONS.put(item, image);

            return image;
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return Main.nullTexture;
        }
    }

    public static int getBackgrounds(ArrayList<BufferedImage> backgrounds, String overlay, Consumer<Graphics2D> action)
    {
        int availableBackgrounds = 0;
        ArrayList<Future<BufferedImage>> backgroundFutures = new ArrayList<>();

        long startTime = System.nanoTime();

        BufferedImage overlayImg = BACKGROUND_OVERLAYS.get(overlay);
        if (overlayImg == null) {
            System.out.println("Reading background overlay: "  + overlay);
            try {
                overlayImg = ImageIO.read(Main.class.getResource("/images/" + overlay + ".png"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            BACKGROUND_OVERLAYS.put(overlay, overlayImg);
        } // TODO: purge cache after we're done? maybe in Main..

        System.out.println("pizza hut");

        final BufferedImage overlayImgThreadSafe = overlayImg;
        try
        {
            for (;;availableBackgrounds++)
            {
                URL resource = Main.class.getResource(String.format("/images/Backgrounds/bedwarsBackground%s.png", availableBackgrounds));
                if (resource == null) break;

                backgroundFutures.add(asyncExecutorService.submit(() -> {
                    BufferedImage image = ImageIO.read(resource);

                    // draw overlay on background
                    Graphics2D g2d = image.createGraphics();

                    g2d.drawImage(overlayImgThreadSafe, 0, 0, null);
                    if (action != null) action.accept(g2d);

                    g2d.dispose();

                    // save background
                    return image;
                }));
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        System.out.println("i hate java");

        backgroundFutures.forEach((future) -> {
            BufferedImage img = Main.nullTexture;
            try {
                img = future.get();
            } catch (InterruptedException ignored) {} catch (ExecutionException e) {
                System.err.println("Failed to load background: " + e.getMessage());
                e.printStackTrace(System.err);
            }

            backgrounds.add(img);
        });

        System.out.printf("Loaded %d backgrounds in %dms%n", availableBackgrounds, (System.nanoTime()-startTime)/1000000);
        return availableBackgrounds;
    }
}
