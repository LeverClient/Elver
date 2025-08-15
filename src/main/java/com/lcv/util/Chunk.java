package com.lcv.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public class Chunk extends Thread
{
    public final FontRenderer fontRenderer;
    private final Consumer<Graphics2D> action;
    private final BufferedImage image;

    public Chunk(FontRenderer fontRenderer, int width, int height, Consumer<Graphics2D> action)
    {
        this.fontRenderer = fontRenderer;
        this.action = action;
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    public void run()
    {
        Graphics2D g2d = image.createGraphics();
        fontRenderer.setGraphics(g2d);
        action.accept(g2d);
    }
}
