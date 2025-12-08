package com.lcv.commands.etcg;

import org.apache.commons.collections4.map.HashedMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Player implements Serializable
{
    private final String id;
    private String name;
    private int experience;
    private int shards;
    private List<String> achievements;
    private List<Deck> decks;
    private Map<String, Quest> quests;

    public Player(String id, String name)
    {
        this.id = id;
        this.name = name;
        this.experience = 0;
        this.shards = 0;
        this.achievements = new ArrayList<>();
        this.decks = new ArrayList<>();
        this.quests = new HashMap<>();
    }

    public String getId()
    {
        return id;
    }
    public String getName()
    {
        return name;
    }
    public int getExperience()
    {
        return experience;
    }
    public int getShards()
    {
        return shards;
    }
    public List<String> getAchievements()
    {
        return achievements;
    }
    public Map<String, Quest> getQuests()
    {
        return quests;
    }
    public List<Deck> getDecks()
    {
        return decks;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    public void addExperience(int experience)
    {
        this.experience += experience;
    }
    public void addShards(int shards)
    {
        this.shards += shards;
    }
    public void subtractShards(int shards)
    {
        this.shards -= shards;
    }
    public void addDeck(Deck deck)
    {
        decks.add(deck);
    }

    public void removeDeck(Deck deck)
    {
        decks.remove(deck);
    }

    public void addAchievement(String achievement)
    {
        achievements.add(achievement);
    }

    public void addQuest(Quest quest)
    {
        quests.put(quest.getId(), quest);
    }

    public void removeQuest(String id)
    {
        quests.remove(id);
    }

    public Quest getQuest(String id)
    {
        return quests.get(id);
    }
}
