package com.lcv.util;

import com.lcv.Main;
import com.lcv.commands.etcg.cards.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ETCGUtil {
    public static final ArrayList<BufferedImage> backgrounds = new ArrayList<>();

    private static final Random rand = new Random();

    public static DB db;
    public static HTreeMap<String, CardTemplate> templates;

    public static void loadBackgrounds() {
        ArrayList<Future<BufferedImage>> futureList = new ArrayList<>();
        for (int i = 0;;i++) {
            final InputStream resource = Main.class.getResourceAsStream(String.format("/ETCG/assets/backgrounds/background%d.png", i));
            if (resource == null) break;
            futureList.add(ImageUtil.ASYNC.submit(() -> {
                try {
                    BufferedImage image = ImageIO.read(resource);
                    Graphics2D g2d = image.createGraphics();
                    g2d.drawImage(ImageUtil.ELVER_ICON, 25, 25, 226, 226, null);
                    g2d.dispose();
                    return image;
                } catch (IOException e) {
                    return ImageUtil.NULL_TEXTURE;
                }
            }));
        }
        for (Future<BufferedImage> future : futureList)
        {
            try {
                backgrounds.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                backgrounds.add(ImageUtil.NULL_TEXTURE);
            }
        }
    }

    public static BufferedImage getRandomBackground()
    {
        return ImageUtil.copyImage(backgrounds.get(rand.nextInt(backgrounds.size())));
    }

    @SuppressWarnings("unchecked")
    public static void setupDB()
    {
        db = DBMaker.fileDB("etgc.db").make();
        Runtime.getRuntime().addShutdownHook(new Thread(db::close));
        templates = db
                .hashMap("templates", Serializer.STRING, Serializer.JAVA)
                .createOrOpen();
        try
        {
            registerCardTemplates();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void registerCardTemplates() throws IOException
    {
        byte[] packBytes = Main.class.getResourceAsStream("/ETCG/cards/packs.json").readAllBytes();
        JSONArray packArr = new JSONArray(new String(packBytes, StandardCharsets.UTF_8));
        for (Object packObj : packArr)
        {
            JSONObject packJson = (JSONObject) packObj;
            String packId = packJson.getString("id");

            byte[] cardBytes = Main.class.getResourceAsStream("/ETCG/cards/" + packId + ".json").readAllBytes();
            JSONArray cardArr = new JSONArray(new String(cardBytes, StandardCharsets.UTF_8));
            for (Object cardObj : cardArr)
            {
                JSONObject cardJson = (JSONObject) cardObj;
                CardTemplate card = switch(cardJson.getString("type"))
                {
                    case "Monster" -> new MonsterCardTemplate(
                            cardJson.getString("id"),
                            cardJson.getString("name"),
                            packId,
                            cardJson.getString("description"),
                            Rarity.valueOf(cardJson.getString("rarity")),
                            cardJson.getInt("attack"),
                            cardJson.getInt("defense"),
                            cardJson.getInt("level")
                    );
                    case "Spell" -> new SpellCardTemplate(
                            cardJson.getString("id"),
                            cardJson.getString("name"),
                            packId,
                            cardJson.getString("description"),
                            Rarity.valueOf(cardJson.getString("rarity")),
                            cardJson.getString("spellType")
                    );
                    case "Trap" -> new TrapCardTemplate(
                            cardJson.getString("id"),
                            cardJson.getString("name"),
                            packId,
                            cardJson.getString("description"),
                            Rarity.valueOf(cardJson.getString("rarity")),
                            cardJson.getString("trapType")
                    );
                    default -> new MonsterCardTemplate(
                            "UNKNOWN",
                            "UNKNOWN",
                            "UNKNOWN",
                            "UNKNOWN",
                            Rarity.COMMON,
                            -1,
                            -1,
                            -1
                    );
                };
                templates.put(card.getId(), card);
            }
        }
        db.commit();
    }
}
