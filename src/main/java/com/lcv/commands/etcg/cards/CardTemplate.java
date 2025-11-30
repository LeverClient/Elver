package com.lcv.commands.etcg.cards;

import java.io.Serializable;

public abstract class CardTemplate implements Serializable
{
    private final String id;
    private final String name;
    private final CardType type;
    private final String pack;
    private final String description;
    private final Rarity rarity;

    public CardTemplate(String id, String name, CardType type, String pack, String description, Rarity rarity)
    {
        this.id = id;
        this.name = name;
        this.type = type;
        this.pack = pack;
        this.description = description;
        this.rarity = rarity;
    }

    public String getId()
    {
        return id;
    }
    public String getName()
    {
        return name;
    }
    public CardType getType()
    {
        return type;
    }
    public String getPack()
    {
        return pack;
    }
    public String getDescription()
    {
        return description;
    }
    public Rarity getRarity()
    {
        return rarity;
    }
}
