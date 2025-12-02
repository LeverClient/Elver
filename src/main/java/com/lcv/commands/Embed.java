package com.lcv.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.ArrayList;

public class Embed
{
    protected EmbedBuilder embedBuilder;
    protected ArrayList<Button> buttons;
    public Embed()
    {
        embedBuilder = new EmbedBuilder();
        setColor(Color.PINK);
        setFooter("Made with ♡ by Lever and syl ✿", "https://i.imgur.com/8NsvrKm.png");
        buttons = new ArrayList<>();
    }

    public Embed addButton(Button button)
    {
        buttons.add(button);
        return this;
    }

    public ArrayList<Button> getButtons()
    {
        return buttons;
    }

    public Embed setColor(Color color)
    {
        embedBuilder.setColor(color);
        return this;
    }

    public Embed setFooter(String text)
    {
        embedBuilder.setFooter(text);
        return this;
    }

    public Embed setFooter(String text, String iconURL)
    {
        embedBuilder.setFooter(text, iconURL);
        return this;
    }

    public Embed setTitle(String title)
    {
        embedBuilder.setTitle(title);
        return this;
    }

    public Embed setDescription(String text)
    {
        embedBuilder.setDescription(text);
        return this;
    }

    public Embed addField(String name, String value, boolean inLine)
    {
        embedBuilder.addField(name, value, inLine);
        return this;
    }

    public MessageEmbed get()
    {
        return embedBuilder.build();
    }
}
