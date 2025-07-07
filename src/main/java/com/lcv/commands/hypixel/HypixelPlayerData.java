package com.lcv.commands.hypixel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.json.JSONObject;

public class HypixelPlayerData
{
    protected JSONObject player;

    protected JSONObject stats;

    boolean valid = true;

    public HypixelPlayerData(JSONObject json)
    {
        if (!json.has("player")) {
            valid = false;
            return;
        }

        player = json.getJSONObject("player");

        if (!player.has("stats")) {
            valid = false;
            return;
        }

        stats = player.getJSONObject("stats");
        System.out.println("wow!");

    }

    // TODO: dedicated readers for each game here maybe?
}
