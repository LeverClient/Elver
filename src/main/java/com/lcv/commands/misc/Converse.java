package com.lcv.commands.misc;

import com.lcv.chat.ChatResponse;
import com.lcv.commands.CommandMeta;
import com.lcv.commands.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.Set;

import static net.dv8tion.jda.api.interactions.InteractionContextType.*;
import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

@CommandMeta(name = "converse", description = "Say something to Elver!", contexts = {GUILD, BOT_DM, PRIVATE_CHANNEL})
public class Converse implements ICommand
{
    @Override
    public void execute(SlashCommandInteractionEvent event)
    {
        // TODO: allow replying?

        String msg = event.getOption("message").getAsString();

        String context = ChatResponse.formatUser(event.getUser(), event.isGuildCommand() ? event.getGuild() : null);

        event.reply(ChatResponse.getResponse(context, msg, event.getUser()))
                .addActionRow(Button.primary("replybutton", "reply"))
                .queue();
    }

    @Override
    public void addFields(SlashCommandData data)
    {
        data.addOption(STRING, "message", "What you say to elver", true);
    }
}
