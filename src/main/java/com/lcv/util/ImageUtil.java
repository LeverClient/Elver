package com.lcv.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Random;

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
}
