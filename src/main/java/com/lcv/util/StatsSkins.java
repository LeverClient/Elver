package com.lcv.util;

import com.lcv.Main;
import net.dv8tion.jda.internal.utils.IOUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class StatsSkins {

    public static int color_main = 10;

    public static int color_secondary = 12;

    public static HashMap<String, Skin> userSkins = new HashMap<>();


    public static Skin none = new Skin();

    public static Skin parseSkin(JSONObject skin) {
        JSONArray users = skin.getJSONArray("Users");
        Object primaryColor = skin.opt("Primary");
        Object secondaryColor = skin.opt("Secondary");
        Object defaultColor = skin.opt("Default");

        Skin statsSkin = new Skin(primaryColor, secondaryColor, defaultColor);

        if (skin.has("Colors") && !skin.isNull("Colors")) {
            JSONArray colors = skin.getJSONArray("Colors");

            int i = 0;
            for (Object color : colors) {
                statsSkin.setColor(i++, color);
            }
        }

        // apply users
        for (Object user : users) {
            String uuid = ((JSONArray) user).getString(0);

            statsSkin.applyToUser(uuid);
        }

        return statsSkin;
    }

    static {
        JSONObject skinsJson;
        try (InputStream skinsStream = Main.class.getResourceAsStream("/Skins.json")) {
            assert skinsStream != null;

            skinsJson = new JSONObject(new String(skinsStream.readAllBytes()));
        } catch (IOException e) {
            System.out.println("failure to do thing");
            throw new RuntimeException(e);
        }

        ArrayList<Skin> skins = new ArrayList<>();
        for (String key : skinsJson.keySet()) {
            JSONObject skinJson = skinsJson.getJSONObject(key);

            Skin skin = parseSkin(skinJson);
            skins.add(skin);
        }

        System.out.println("skins done!");
    }

    public static class Skin {
        public Color[] colors = new Color[16];

        public Skin() {}

        public Skin(Object main, Object secondary) {
            colors[color_main] = getColorFromGeneric(main);
            colors[color_secondary] = getColorFromGeneric(secondary);
        }

        public Skin(Object main, Object secondary, Object def) {
            colors[color_main] = getColorFromGeneric(main);
            colors[color_secondary] = getColorFromGeneric(secondary);
            colors[colors.length-1] = getColorFromGeneric(def);
        }

        private Color getColorFromGeneric(Object gen) {
            if (gen instanceof Integer) {
                gen = FontRenderer.defaultColors[(int) gen];
            }

            else if (gen instanceof int[] gen_arr) {
                gen = new Color(gen_arr[0], gen_arr[1], gen_arr[2]);
            }

            else if (gen instanceof JSONArray gen_arr) {
                gen = new Color(gen_arr.getInt(0), gen_arr.getInt(1), gen_arr.getInt(2));
            } else if (gen == JSONObject.NULL) {
                gen = null;
            }

            else if (gen != null && !(gen instanceof Color)) {
                throw new IllegalArgumentException("Invalid generic type for getColorFromGeneric(). Must be either Integer, Color, int[], or null");
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
