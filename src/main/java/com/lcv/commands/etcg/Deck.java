package com.lcv.commands.etcg;

import java.io.Serializable;
import java.util.*;

public class Deck implements Serializable
{
    private final String id;
    private String name;
    private final List<String> cards;

    public Deck(String id, String name)
    {
        this.id = id;
        this.name = name;
        this.cards = new ArrayList<>();
    }

    public String getId()
    {
        return id;
    }
    public String getName()
    {
        return name;
    }
    public List<String> getCards()
    {
        return cards;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    public void addCard(String cardId)
    {
        cards.add(cardId);
        Collections.sort(cards);
    }
    public void removeCard(String cardId)
    {
        cards.remove(cardId);
        Collections.sort(cards);
    }
}
