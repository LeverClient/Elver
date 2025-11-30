package com.lcv.commands.etcg;

import com.lcv.Main;
import com.lcv.commands.ICommand;
import com.lcv.commands.etcg.cards.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import static com.lcv.Main.ALL_CONTEXTS;
import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

public class ETCG implements ICommand
{
    public static DB db;
    public static HTreeMap<String, CardTemplate> templates;

    @Override
    public String getName()
    {
        return "tcg";
    }

    @Override
    public String getDescription()
    {
        return "TCG for Elver";
    }

    @Override
    public Set<InteractionContextType> getContexts() {
        return ALL_CONTEXTS;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event)
    {
        event.replyEmbeds(new Gui("Main Menu").setDescription("Placeholder Description").get())
                .addActionRow(
                        Button.primary("profile", "Profile"),
                        Button.primary("packs", "Packs"),
                        Button.primary("quests", "Quests"),
                        Button.primary("battle", "Battle")
                )
                .queue();
    }

    @Override
    public void addFields(SlashCommandData data) {
        data.addOption(STRING, "menu", "Menu Subcommands", false, true);
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
