package com.lcv.util;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageUtil
{
    public static BufferedImage copyImage(BufferedImage image)
    {
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        Graphics2D g2d = copy.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return copy;
    }
}
