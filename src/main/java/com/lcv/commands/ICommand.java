package com.lcv.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.internal.utils.Helpers;

import java.util.Collections;
import java.util.Set;

public interface ICommand
{
    String getName();
    String getDescription();
    default Set<InteractionContextType> getContexts() {
        return Collections.singleton(InteractionContextType.GUILD);
    }

    void execute(SlashCommandInteractionEvent event);
    void addFields(SlashCommandData data);
}
