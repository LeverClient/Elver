package com.lcv.commands.etcg.cards;

public class SpellCardTemplate extends CardTemplate
{
    private final String spellType;

    public SpellCardTemplate(String id, String name, String pack, String description, Rarity rarity, String spellType)
    {
        super(id, name, CardType.SPELL, pack, description, rarity);
        this.spellType = spellType;
    }

    public String getSpellType()
    {
        return spellType;
    }
}
