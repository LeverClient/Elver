package com.lcv.commands.misc;

import com.lcv.commands.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Set;

import static com.lcv.Main.ALL_CONTEXTS;
import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

public class Lever implements ICommand
{
    @Override
    public String getName()
    {
        return "lever";
    }

    @Override
    public String getDescription()
    {
        return "Experimental Commands";
    }

    @Override
    public Set<InteractionContextType> getContexts()
    {
        return ALL_CONTEXTS;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event)
    {
        event.reply("test").queue();
    }

    @Override
    public void addFields(SlashCommandData data)
    {
        data.addOption(STRING, "command", "Experimental Commands", true, true);
    }
}
