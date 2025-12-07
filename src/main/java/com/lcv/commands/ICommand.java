package com.lcv.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Set;

public interface ICommand
{
    default String getName()
    {
        return this.getClass().getAnnotation(CommandMeta.class).name();
    }
    default String getDescription()
    {
        return this.getClass().getAnnotation(CommandMeta.class).description();
    }
    default Set<InteractionContextType> getContexts() {
        return Set.of(this.getClass().getAnnotation(CommandMeta.class).contexts());
    }

    void execute(SlashCommandInteractionEvent event);
    void addFields(SlashCommandData data);
}
