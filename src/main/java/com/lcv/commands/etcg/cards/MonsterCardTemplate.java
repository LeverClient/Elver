package com.lcv.commands.etcg.cards;

public class MonsterCardTemplate extends CardTemplate
{
    private final int attack;
    private final int defense;
    private final int level;

    public MonsterCardTemplate(String id, String name, String pack, String description, Rarity rarity, int attack, int defense, int level)
    {
        super(id, name, CardType.MONSTER, pack, description, rarity);
        this.attack = attack;
        this.defense = defense;
        this.level = level;
    }

    public int getAttack()
    {
        return attack;
    }
    public int getDefense()
    {
        return defense;
    }
    public int getLevel()
    {
        return level;
    }
}
