package com.lcv.util;

import com.lcv.Main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class ImageUtil
{
    public static final String[] PLAYER_SKIN_FULL = {"default", "marching", "walking", "crouching", "crossed", "criss_cross", "ultimate", "cheering", "relaxing", "trudging", "cowering", "pointing", "lunging", "dungeons", "facepalm", "sleeping", "archer", "kicking", "mojavatar", "reading"};
    public static final String[] PLAYER_SKIN_TOP = {"default", "crossed", "ultimate", "dungeons"};
    public static Random rand = new Random();

    public static BufferedImage copyImage(BufferedImage image)
    {
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        Graphics2D g2d = copy.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return copy;
    }

    public static BufferedImage getPlayerSkinFull(String uuid)
    {
        String skinType = PLAYER_SKIN_FULL[rand.nextInt(0, PLAYER_SKIN_FULL.length)];
        if (uuid.equals("ddf13e436ccc4790bb49912913bf7d77")) skinType = "mojavatar";

        for (int i = 0; i < 3; i++)
        {
            try
            {
                return ImageIO.read(new URL(String.format("https://starlightskins.lunareclipse.studio/render/%s/%s/full", skinType, uuid)));
            }
            catch (IOException e)
            {
                e.printStackTrace(System.err);
            }
        }
        throw new RuntimeException("Failed to get full player skin (probably a 502 from the api)");
    }

    public static BufferedImage getPlayerSkinTop(String uuid)
    {
        String skinType = PLAYER_SKIN_TOP[rand.nextInt(0, PLAYER_SKIN_TOP.length)];
        for (int i = 0; i < 3; i++)
        {
            try
            {
                return ImageIO.read(new URL(String.format("https://starlightskins.lunareclipse.studio/render/%s/%s/bust", skinType, uuid)));
            }
            catch (IOException e)
            {
                e.printStackTrace(System.err);
            }
        }
        throw new RuntimeException("Failed to get top player skin (probably a 502 from the api)");
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

    public static HashMap<String, BufferedImage> itemIconCache = new HashMap<>();
    public static String[] itemIconDirectories = new String[]{
            "/images/Items/",
            "/images/Blocks/",
            "/images/"
    };
    public static BufferedImage loadItemImage(String item) {
        if (itemIconCache.containsKey(item)) return itemIconCache.get(item);

        URL itemIconResource = null;
        for (String directory : itemIconDirectories) {
            itemIconResource = Main.class.getResource(directory + item + ".png");
            if (itemIconResource != null) break;
        }

        if (itemIconResource == null) {
            System.err.println("Couldn't find texture for " + item);
            return Main.nullTexture;
        }

        try {
            BufferedImage image = ImageIO.read(itemIconResource);
            itemIconCache.put(item, image);

            return image;
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return Main.nullTexture;
        }
    }

    public static HashMap<String, BufferedImage> overlayCache = new HashMap<>();
    public static int getBackgrounds(ArrayList<BufferedImage> backgrounds, String overlay, Consumer<Graphics2D> action)
    {
        int availableBackgrounds = 0;

        try
        {
            for (;;availableBackgrounds++)
            {
                URL resource = Main.class.getResource(String.format("/images/Backgrounds/bedwarsBackground%s.png", availableBackgrounds));
                if (resource == null) break;

                BufferedImage image = ImageIO.read(resource);

                BufferedImage overlayImg = overlayCache.get(overlay);
                if (overlayImg == null) {
                    overlayImg = ImageIO.read(Main.class.getResource("/images/" + overlay + ".png"));
                    overlayCache.put(overlay, overlayImg);
                } // TODO: purge cache after we're done?

                // draw overlay on background
                Graphics2D g2d = image.createGraphics();

                g2d.drawImage(overlayImg, 0, 0, null);

                action.accept(g2d);

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
        return availableBackgrounds;
    }
}
