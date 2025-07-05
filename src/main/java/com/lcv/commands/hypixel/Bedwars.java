package com.lcv.commands.hypixel;

import com.google.gson.JsonObject;
import com.lcv.commands.Command;
import com.lcv.util.HTTPRequest;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Objects;

import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

public class Bedwars implements Command
{

    @Override
    public String getName()
    {
        return "bedwars";
    }

    @Override
    public String getDescription()
    {
        return "Gets Bedwars Stats";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event)
    {
        String name = Objects.requireNonNull(event.getOption("name")).getAsString();
        JsonObject json = HTTPRequest.getHTTPRequest("https://api.mojang.com/user/profile/" + name);
        if (json == null || json.isEmpty())
        {
            event.reply("not real player").queue();
            return;
        }

        String key = System.getenv("HYPIXEL_KEY");
        String UUID = json.get("id").getAsString();
        json = HTTPRequest.getHTTPRequest("https://api.hypixel.net/v2/player?key=" + key + "&uuid=" + UUID);
        if (json == null || json.isEmpty())
        {
            event.reply("not real player").queue();
            return;
        }

        HypixelPlayerData player = new HypixelPlayerData(json);
        event.reply("name = " + name + " uuid = " + UUID).queue();
    }

    @Override
    public void addFields(SlashCommandData data)
    {
        data.addOption(STRING, "name", "Name of Player", true);
    }
}
