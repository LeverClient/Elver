package com.lcv;

import com.lcv.chat.ChatResponse;
import com.lcv.commands.CommandMeta;
import com.lcv.commands.ICommand;
import com.lcv.commands.audio.Say;
import com.lcv.commands.hypixel.Bedwars;
import com.lcv.commands.hypixel.Duels;
import com.lcv.commands.misc.Converse;
import com.lcv.commands.misc.Hello;
import com.lcv.commands.misc.Lever;
import com.lcv.commands.misc.lever.Wordle;
import com.lcv.commands.etcg.ETCG;
import com.lcv.util.ETCGUtil;
import com.lcv.util.ImageUtil;
import com.lcv.window.GLFWHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static net.dv8tion.jda.api.interactions.InteractionContextType.*;

public class Main extends ListenerAdapter
{
    public static final String ELVER_ID = "1140399630622924971";
    public static final List<ICommand> commands = new ArrayList<>();

    public static JDA jda;

    public static void main(String[] args) throws URISyntaxException, IOException, FontFormatException, InterruptedException
    {
        if (true)
        {
            new Thread(() ->
            {
                GLFWHandler glfwHandler = new GLFWHandler();

                try
                {
                    glfwHandler.init(576, 432);
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
                glfwHandler.loop();
            }).start();
        }

        ETCGUtil.setupDB();
        ETCGUtil.loadBackgrounds();

        jda = JDABuilder.create(System.getenv("BOT_KEY"), GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES).addEventListeners(new Main()).build();
        registerCommands();
    }

    private static void registerCommands()
    {
        Reflections reflections = new Reflections("com.lcv.commands", Scanners.TypesAnnotated);
        for (Class<?> cls : reflections.getTypesAnnotatedWith(CommandMeta.class))
        {
            if (!ICommand.class.isAssignableFrom(cls)) continue;
            try
            {
                ICommand command = (ICommand) cls.getDeclaredConstructor().newInstance();
                commands.add(command);
            }
            catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
            {
                throw new RuntimeException(e);
            }
        }
        List<SlashCommandData> slashCommandData = new ArrayList<>();
        commands.forEach(command -> {
            SlashCommandData scd = Commands.slash(command.getName(), command.getDescription());
            scd.setContexts(command.getContexts());
            if (command.getContexts().contains(PRIVATE_CHANNEL)) scd.setIntegrationTypes(IntegrationType.ALL);
            command.addFields(scd);
            slashCommandData.add(scd);
        });
        jda.updateCommands().addCommands(slashCommandData).queue();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event)
    {
        for (ICommand command : commands)
        {
            if (event.getName().equals(command.getName()))
            {
                command.execute(event);
                return;
            }
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event)
    {
        Message message = event.getMessage();

        // is this a bad check? (nah its fine for the most part, id rewrite it diff tho)
        if (message.getAuthor() != jda.getSelfUser())
        {
            boolean botMentioned = message.getMentions().getUsers().contains(event.getJDA().getSelfUser());
            boolean botReplied = message.getReferencedMessage() != null && message.getReferencedMessage().getAuthor().getId().equals(ELVER_ID);
            if (botMentioned || botReplied)
            {
                message.reply(ChatResponse.getResponse(message)).queue();
            }
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event)
    {
        switch (event.getComponentId())
        {
            case "replybutton":
            {
                Modal modal = Modal.create("replymodal", "Reply").addActionRow(TextInput.create("replyfield", "Message", TextInputStyle.PARAGRAPH).setPlaceholder("Message here!! whatever u put here will be shown to everyone btw").setRequired(true).build()).build();

                event.replyModal(modal).queue();
                break;
            }
            case "profile":
            {
                event.editMessageEmbeds(ETCG.profile(event).get()).queue();
            }
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event)
    {
        switch (event.getModalId())
        {
            case "replymodal":
                String msg = event.getValue("replyfield").getAsString();
                String context = ChatResponse.formatUser(event.getUser(), null); // TODO: fix guild thingy here (probably do with context)
                String replyPrefix = String.format("<@%s>: \"%s\"%n", event.getUser().getId(), msg);
                event.reply(replyPrefix + ChatResponse.getResponse(context, msg, event.getUser())).addActionRow(Button.primary("replybutton", "reply")).queue();
                break;
            case "wordle":
                event.replyEmbeds(new Wordle(event.getValue("word").getAsString()).getWords()).queue();
                break;
            default:
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event)
    {
        switch (event.getName())
        {
            case "bedwars":
            {
                // todo: write thing to access mojang api maybe?
                break;
            }
            case "lever":
            {
                event.replyChoiceStrings(Arrays
                        .stream(new String[]{"Wordle"})
                        .filter(s -> s.startsWith(event.getFocusedOption().getValue()))
                        .collect(Collectors.toList()))
                        .queue();
                break;
            }
            case "gui":
            {
                switch (event.getFocusedOption().getName())
                {
                    case "menu":
                    {
                        event.replyChoiceStrings(Arrays
                                        .stream(new String[]{"Profile", "Packs", "Quests", "Battle"})
                                        .filter(s -> s.startsWith(event.getFocusedOption().getValue()))
                                        .collect(Collectors.toList()))
                                .queue();
                    }
                }
            }
        }
    }
}