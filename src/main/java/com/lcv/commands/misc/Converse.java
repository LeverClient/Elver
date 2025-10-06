package com.lcv.commands.misc;

import com.lcv.chat.ChatResponse;
import com.lcv.commands.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.Set;

import static com.lcv.Main.ALL_CONTEXTS;
import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

public class Converse implements ICommand
{
    @Override
    public String getName()
    {
        return "converse";
    }

    @Override
    public String getDescription()
    {
        return "Say something to elver!";
    }

    @Override
    public Set<InteractionContextType> getContexts() {
        return ALL_CONTEXTS;
    }

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
