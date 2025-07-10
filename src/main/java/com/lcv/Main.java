package com.lcv;

import com.lcv.commands.Command;
import com.lcv.commands.hypixel.Bedwars;
import com.lcv.commands.misc.Hello;
import com.lcv.commands.misc.Image;
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
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main extends ListenerAdapter
{
    private static List<Command> commands;

    public static BufferedImage botProfile;

    public static BufferedImage botProfileScaled;

    public static Font minecraftFont;

    public static void main(String[] args) throws URISyntaxException, IOException, FontFormatException {
        // cache bot profile for faster image making
        botProfile = ImageIO.read(new File(Objects.requireNonNull(Main.class.getResource("/images/botProfile.png")).toURI()));
        botProfileScaled = new BufferedImage(226, 226, BufferedImage.TYPE_INT_ARGB);
        {
            Graphics2D g2d = botProfileScaled.createGraphics();
            g2d.drawImage(botProfile, 0, 0, 226, 226, null);
            g2d.dispose();
        }

        // read font
        minecraftFont = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(Main.class.getResourceAsStream("/fonts/minecraft.ttf")));

        JDA jda = JDABuilder.create(
                System.getenv("BOT_KEY"),
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_MESSAGES)
                .addEventListeners(new Main())
                .build();

        commands = List.of(
                new Hello(),
                new Bedwars(),
                new Image()
        );

        List<SlashCommandData> slashData = new ArrayList<>();

        for (Command command : commands)
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
        for (Command command : commands)
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
        /*for (Command command : commands)
        {
            if (event.getName().equals("bedwars"))
            {
                // todo: write thing to access hypixel api maybe?
            }
        }*/
    }
}