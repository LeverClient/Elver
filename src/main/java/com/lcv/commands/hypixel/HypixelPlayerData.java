package com.lcv.commands.hypixel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class HypixelPlayerData
{
    protected JsonObject player;

    protected JsonObject stats;

    public HypixelPlayerData(JsonObject json)
    {
        player = json.get("player").getAsJsonObject();
        stats = player.get("stats").getAsJsonObject();
        System.out.println("wow!");
    }

    // TODO: dedicated readers for each game here maybe?
}
