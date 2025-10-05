package com.lcv.commands.misc;

import com.lcv.commands.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class Hello implements ICommand
{
    @Override
    public String getName()
    {
        return "hello";
    }

    @Override
    public String getDescription()
    {
        return "Says hello!";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event)
    {
        event.reply("Hewo!").queue();
    }

    @Override
    public void addFields(SlashCommandData data)
    {

    }
}
