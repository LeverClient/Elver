package com.lcv.commands.misc;

import com.lcv.commands.Command;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class Hello implements Command
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
        event.reply("Hello!").queue();
    }

    @Override
    public void addFields(SlashCommandData data)
    {

    }
}
