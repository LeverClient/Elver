package com.lcv.commands.tcg;

import com.lcv.commands.Embed;

public class Gui extends Embed
{
    public Gui(String title)
    {
        this.embedBuilder.setTitle(String.format("Elver TCG - %s", title));
    }
}
