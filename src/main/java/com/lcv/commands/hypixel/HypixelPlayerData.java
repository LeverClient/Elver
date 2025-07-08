package com.lcv.commands.hypixel;

import org.json.JSONObject;

import java.util.HashMap;

public class HypixelPlayerData
{
    protected JSONObject player;

    protected JSONObject stats;

    public String rank = "UNKNOWN";

    public String plusColor = "DARK_GRAY";

    public String monthlyRankColor = "DARK_GRAY";

    public String name;

    public String uuid;

    boolean valid = true;

    public static HashMap<String, String> colorLookup = new HashMap<>();

    public static HashMap<String, String[]> rankLookup = new HashMap<>();

    static {
        // ranks
        rankLookup.put("VIP", new String[]{"VIP"});
        rankLookup.put("VIP_PLUS", new String[]{"VIP", "+"});
        rankLookup.put("MVP", new String[]{"MVP"});
        rankLookup.put("MVP_PLUS", new String[]{"MVP", "+"});
        rankLookup.put("SUPERSTAR", new String[]{"MVP", "++"});


        // rank colors
        colorLookup.put("VIP", "§a");
        colorLookup.put("VIP_PLUS", "§a");
        colorLookup.put("MVP", "§b");
        colorLookup.put("MVP_PLUS", "§b");
        colorLookup.put("SUPERSTAR", "-");

        // just colors
        colorLookup.put("BLACK", "§0");
        colorLookup.put("DARK_BLUE", "§1");
        colorLookup.put("DARK_GREEN", "§2");
        colorLookup.put("DARK_AQUA", "§3");
        colorLookup.put("DARK_RED", "§4");
        colorLookup.put("DARK_PURPLE", "§5");
        colorLookup.put("GOLD", "§6");
        colorLookup.put("GRAY", "§7");
        colorLookup.put("DARK_GRAY", "§8");
        colorLookup.put("BLUE", "§9");
        colorLookup.put("GREEN", "§a");
        colorLookup.put("AQUA", "§b");
        colorLookup.put("RED", "§c");
        colorLookup.put("LIGHT_PURPLE", "§d");
        colorLookup.put("YELLOW", "§e");
        colorLookup.put("WHITE", "§f");
    }

    static public String getPlayerNameRankFormat(String name, String rank, String plusColor, String monthlyRankColor) {
        StringBuilder nameFormatted = new StringBuilder();
        String rankColor;
        if (rank.equals("SUPERSTAR")) {
            nameFormatted.append(rankColor = colorLookup.get(monthlyRankColor));
        } else {
            nameFormatted.append(rankColor = colorLookup.get(rank));
        }

        String[] rankName = rankLookup.get(rank);
        if (rankName == null) rankName = new String[]{rank};

        nameFormatted.append('[');

        nameFormatted.append(rankName[0]);
        if (rankName.length > 1) {
            nameFormatted.append(colorLookup.getOrDefault(plusColor, plusColor));
            nameFormatted.append(rankName[1]);
            nameFormatted.append(rankColor);
        }

        nameFormatted.append("] ");
        nameFormatted.append(name);

        return nameFormatted.toString();
    }

    public String getPlayerNameRankFormat() {
        return getPlayerNameRankFormat(name, rank, plusColor, monthlyRankColor);
    }

    public HypixelPlayerData(JSONObject json)
    {
        if (!json.has("player") || json.isNull("player")) {
            valid = false;
            return;
        }

        player = json.getJSONObject("player");

        if (!player.has("stats") || json.isNull("player")) {
            valid = false;
            return;
        }

        name = player.getString("displayname");
        uuid = player.getString("uuid");

        if (player.has("monthlyPackageRank") && !player.isNull("monthlyPackageRank")) {
            rank = player.getString("monthlyPackageRank");
        }

        if ((rank == null || rank.equals("NONE") || rank.equals("UNKNOWN")) && (player.has("newPackageRank") && !player.isNull("newPackageRank"))) {
            rank = player.getString("newPackageRank");
        } else if (player.has("packageRank") && !player.isNull("packageRank")) {
            rank = player.getString("packageRank");
        }

        if (player.has("rankPlusColor") && !player.isNull("rankPlusColor")) {
            plusColor = player.getString("rankPlusColor");
        }

        if (player.has("monthlyRankColor") && !player.isNull("monthlyRankColor")) {
            monthlyRankColor = player.getString("monthlyRankColor");
        }

        String rankName = getPlayerNameRankFormat(name, rank, plusColor, monthlyRankColor);

        stats = player.getJSONObject("stats");
        System.out.println("wow!");

    }

    // TODO: dedicated readers for each game here maybe?
}
