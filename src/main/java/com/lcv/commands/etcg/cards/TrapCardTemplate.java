package com.lcv.commands.etcg.cards;

public class TrapCardTemplate extends CardTemplate {
    private final String trapType;

    public TrapCardTemplate(String id, String name, String pack, String description, Rarity rarity, String trapType)
    {
        super(id, name, CardType.TRAP, pack, description, rarity);
        this.trapType = trapType;
    }

    public String getTrapType()
    {
        return trapType;
    }
}
