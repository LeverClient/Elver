package com.lcv;

import com.lcv.chat.ChatResponse;
import com.lcv.commands.ICommand;
import com.lcv.commands.audio.Say;
import com.lcv.commands.hypixel.Bedwars;
import com.lcv.commands.hypixel.Duels;
import com.lcv.commands.misc.Converse;
import com.lcv.commands.misc.Hello;
import com.lcv.commands.misc.Lever;
import com.lcv.commands.misc.lever.Wordle;
import com.lcv.commands.etcg.ETCG;
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
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.NotNull;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static net.dv8tion.jda.api.interactions.InteractionContextType.*;

public class Main extends ListenerAdapter
{
    public static final String ELVER_ID = "1140399630622924971";

    public static final Set<InteractionContextType> ALL_CONTEXTS = Helpers.unmodifiableEnumSet(GUILD, BOT_DM, PRIVATE_CHANNEL);

    private static List<ICommand> commands;

    public static BufferedImage botProfile;

    public static BufferedImage botProfileScaled;

    public static Font minecraftFont;

    public static BufferedImage nullTexture;

    public static JDA jda;

    public static void main(String[] args) throws URISyntaxException, IOException, FontFormatException, InterruptedException
    {
        // null texture
        nullTexture = ImageIO.read(new File(Objects.requireNonNull(Main.class.getResource("/images/null.png")).toURI()));

        // cache bot profile for faster image making
        botProfile = ImageIO.read(new File(Objects.requireNonNull(Main.class.getResource("/images/elver.png")).toURI()));
        botProfileScaled = new BufferedImage(226, 226, BufferedImage.TYPE_INT_ARGB);
        {
            Graphics2D g2d = botProfileScaled.createGraphics();
            g2d.drawImage(botProfile, 0, 0, 226, 226, null);
            g2d.dispose();
        }

        // read font
        minecraftFont = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(Main.class.getResourceAsStream("/fonts/minecraft.ttf")));

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

        ETCG.setupDB();

        jda = JDABuilder.create(System.getenv("BOT_KEY"), GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES).addEventListeners(new Main()).build();
        commands = List.of(new Hello(), new Bedwars(), new Duels(), new Converse(), new Say(), new Lever(), new ETCG());

        List<SlashCommandData> slashData = new ArrayList<>();

        for (ICommand command : commands)
        {
            SlashCommandData data = Commands.slash(command.getName(), command.getDescription());
            data.setContexts(command.getContexts());
            if (command.getContexts().contains(PRIVATE_CHANNEL))
            {
                data.setIntegrationTypes(IntegrationType.ALL);
            }

            command.addFields(data);
            slashData.add(data);
        }

        jda.updateCommands().addCommands(slashData).queue();


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