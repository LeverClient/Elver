package com.lcv.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface ICommand
{
    String getName();
    String getDescription();
    void execute(SlashCommandInteractionEvent event);
    void addFields(SlashCommandData data);
}
