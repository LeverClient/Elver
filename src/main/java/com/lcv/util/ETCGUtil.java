package com.lcv.util;

import com.lcv.Main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ETCGUtil {
    public static final ArrayList<BufferedImage> backgrounds = new ArrayList<>();
    private static final Random rand = new Random();

    public static int loadBackgrounds() {
        ArrayList<Future<BufferedImage>> futureList = new ArrayList<>();
        for (int i = 0;;i++) {
            final URL resource = Main.class.getResource(String.format("/ETCG/assets/backgrounds/background%d.jpg", i));
            if (resource == null) break;
            futureList.add(ImageUtil.ASYNC.submit(() -> {
                try {
                    BufferedImage image = ImageIO.read(resource);
                    Graphics2D g2d = image.createGraphics();
                    g2d.drawImage(Main.botProfileScaled, 25, 25, 226, 226, null);
                    g2d.dispose();
                    return image;
                } catch (IOException e) {
                    return Main.nullTexture;
                }
            }));
        }
        for (Future<BufferedImage> future : futureList)
        {
            try {
                backgrounds.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                backgrounds.add(Main.nullTexture);
            }
        }
        return futureList.size();
    }

    public static BufferedImage getRandomBackground()
    {
        return backgrounds.get(rand.nextInt(backgrounds.size()));
    }
}
