package com.lcv.commands.misc;

import com.lcv.commands.CommandMeta;
import com.lcv.commands.Embed;
import com.lcv.commands.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.util.Set;

import static net.dv8tion.jda.api.interactions.InteractionContextType.*;
import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

@CommandMeta(name = "lever", description = "Test", contexts = {GUILD, BOT_DM, PRIVATE_CHANNEL})
public class Lever implements ICommand
{
    @Override
    public void execute(SlashCommandInteractionEvent event)
    {
        switch (event.getOption("command").getAsString())
        {
            case "Wordle" -> event.replyModal(Modal
                            .create("wordle", "Wordle")
                            .addActionRow(TextInput.create("word", "Word", TextInputStyle.SHORT)
                                    .setPlaceholder("Word")
                                    .setRequired(true)
                                    .setRequiredRange(5, 5).build())
                            .build())
                    .queue();
            default -> event.replyEmbeds(new Embed().setTitle("Failed Operation :(").setDescription("Invalid Subcommand").get()).queue();
        }
    }

    @Override
    public void addFields(SlashCommandData data)
    {
        data.addOption(STRING, "command", "Experimental Commands", true, true);
    }
}
