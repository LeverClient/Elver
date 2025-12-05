package com.lcv.util;

import com.lcv.Main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageUtil
{
    public static final BufferedImage ELVER_ICON = loadImage("/images/elver_icon.png");
    public static final BufferedImage NULL_TEXTURE = loadImage("/images/null.png");

    public static final Font MINECRAFT_FONT = loadFont(Font.TRUETYPE_FONT, "/fonts/minecraft.ttf");

    public static ExecutorService ASYNC = Executors.newCachedThreadPool();

    public static BufferedImage loadImage(String path)
    {
        try
        {
            BufferedImage image = ImageIO.read(Main.class.getResource(path));
            System.out.println(image.getWidth() + " " + image.getHeight());
            if (image.getType() == BufferedImage.TYPE_INT_ARGB)
                return image;
            else
                return copyImage(image);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static Font loadFont(int fontFormat, String path)
    {
        try
        {
            return Font.createFont(fontFormat, Main.class.getResourceAsStream(path));
        }
        catch (FontFormatException | IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static BufferedImage copyImage(BufferedImage image)
    {
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = copy.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return copy;
    }

    public static int[] fitToArea(BufferedImage image, int areaWidth, int areaHeight)
    {
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
}
