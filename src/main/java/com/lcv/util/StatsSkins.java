package com.lcv.util;

import java.awt.*;
import java.util.HashMap;

public class StatsSkins {

    public static int color_main = 10;

    public static int color_secondary = 12;

    public static HashMap<String, Skin> userSkins = new HashMap<>();


    public static Skin none = new Skin();

    public static Skin pink = new Skin(new Color(255, 180, 235), 15)
            .applyToUser("ddf13e436ccc4790bb49912913bf7d77"); // syl127 (me)

    public static Skin idk = new Skin(11, 8)
            .applyToUser("f7a757d51d6d408eb54ea7230b95ae15"); // lever client


    public static class Skin {
        public Color[] colors = new Color[16];

        public Skin() {}

        public Skin(Object main, Object secondary) {
            colors[color_main] = getColorFromGeneric(main);
            colors[color_secondary] = getColorFromGeneric(secondary);
        }

        private Color getColorFromGeneric(Object gen) {
            if (gen instanceof Integer) {
                gen = FontRenderer.defaultColors[(int) gen];
            }

            if (gen != null && !(gen instanceof Color)) {
                throw new IllegalArgumentException("Invalid generic type for getColorFromGeneric(). Must be either Integer, Color, or null");
            }

            return (Color) gen;
        }

        public Skin setColor(int index, Object color) {
            colors[index] = getColorFromGeneric(color);

            return this;
        }

        public Skin applyToUser(String uuid) {
            userSkins.put(uuid, this);

            return this;
        }

        public void apply(FontRenderer renderer) {
            renderer.appliedColors = FontRenderer.defaultColors.clone();

            for (int i = 0; i < colors.length; i++) {
                Color color = colors[i];
                if (color == null) continue;

                System.out.println("Applying color " + i + " | " + color);
                renderer.appliedColors[i] = color;
            }
        }
    }
}
