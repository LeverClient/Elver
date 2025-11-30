package com.lcv.commands.etcg.cards;

import java.io.Serializable;

public class CardInstance implements Serializable
{
    private final String id;
    private int level;
    private int count;

    public CardInstance(String id)
    {
        this.id = id;
        this.level = 1;
        this.count = 1;
    }

    public String getId()
    {
        return id;
    }
    public int getLevel()
    {
        return level;
    }
    public int getCount()
    {
        return count;
    }
    public void addCopies(int amount)
    {
        count += amount;
    }
    public void removeCopies(int amount)
    {
        count -= amount;
    }
    public void upgradeLevel()
    {
        level++;
    }
}
