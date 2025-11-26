package com.lcv.commands.tcg;

import com.lcv.commands.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.Set;

import static com.lcv.Main.ALL_CONTEXTS;
import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

public class TCG implements ICommand
{
    @Override
    public String getName()
    {
        return "tcg";
    }

    @Override
    public String getDescription()
    {
        return "TCG for Elver";
    }

    @Override
    public Set<InteractionContextType> getContexts() {
        return ALL_CONTEXTS;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event)
    {
        event.replyEmbeds(new Gui("Main Menu").setDescription("Placeholder Description").get())
                .addActionRow(
                        Button.primary("profile", "Profile"),
                        Button.primary("packs", "Packs"),
                        Button.primary("quests", "Quests"),
                        Button.primary("battle", "Battle")
                )
                .queue();
    }

    @Override
    public void addFields(SlashCommandData data) {
        data.addOption(STRING, "menu", "Menu Subcommands", false, true);
    }
}
