package com.lcv.util;

import com.lcv.Main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageUtil
{
    public static ExecutorService ASYNC = Executors.newCachedThreadPool();

    public static BufferedImage loadImage(String name)
    {
        try
        {
            BufferedImage image = ImageIO.read(Main.class.getResource(name));
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

    public static BufferedImage copyImage(BufferedImage image)
    {
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = copy.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return copy;
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
}
