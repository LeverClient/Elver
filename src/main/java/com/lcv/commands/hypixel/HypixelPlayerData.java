package com.lcv.commands.hypixel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class HypixelPlayerData
{
    protected JsonObject player;

    protected JsonObject stats;

    boolean valid = true;

    public HypixelPlayerData(JsonObject json)
    {
        if (!json.has("player")) {
            valid = false;
            return;
        }

        player = json.get("player").getAsJsonObject();

        if (!player.has("stats")) {
            valid = false;
            return;
        }

        stats = player.get("stats").getAsJsonObject();
        System.out.println("wow!");

    }

    // TODO: dedicated readers for each game here maybe?
}
