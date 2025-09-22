package com.lcv;

import com.lcv.commands.ICommand;
import com.lcv.commands.hypixel.Bedwars;
import com.lcv.commands.hypixel.Duels;
import com.lcv.commands.misc.Hello;
import com.lcv.commands.misc.Image;
import com.lcv.window.GLFWHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main extends ListenerAdapter
{
    private static List<ICommand> commands;

    public static BufferedImage botProfile;

    public static BufferedImage botProfileScaled;

    public static Font minecraftFont;

    public static BufferedImage nullTexture;

    public static void main(String[] args) throws URISyntaxException, IOException, FontFormatException, InterruptedException {
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

        if (true) {
            new Thread(() -> {
                GLFWHandler glfwHandler = new GLFWHandler();

                try {
                    glfwHandler.init(576, 432);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                glfwHandler.loop();
            }).start();
        }

        JDA jda = JDABuilder.create(
                System.getenv("BOT_KEY"),
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_MESSAGES)
                .addEventListeners(new Main())
                .build();

        commands = List.of(
                new Hello(),
                new Bedwars(),
                new Image(),
                new Duels()
        );

        List<SlashCommandData> slashData = new ArrayList<>();

        for (ICommand command : commands)
        {
            SlashCommandData data = Commands.slash(command.getName(), command.getDescription());
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
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event)
    {
        for (ICommand command : commands)
        {
            if (event.getName().equals("bedwars"))
            {
                // todo: write thing to access mojang api maybe?
            }
        }
    }
}