package com.lcv;

import com.lcv.commands.Command;
import com.lcv.commands.hypixel.Bedwars;
import com.lcv.util.HypixelPlayerData;
import com.lcv.commands.misc.Hello;
import com.lcv.commands.misc.Image;
import com.lcv.window.GLFWHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

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
    private static List<Command> commands;

    public static BufferedImage botProfile;

    public static BufferedImage botProfileScaled;

    public static Font minecraftFont;

    public static BufferedImage nullTexture;

    public static void main(String[] args) throws URISyntaxException, IOException, FontFormatException, InterruptedException {
        // null texture
        nullTexture = ImageIO.read(new File(Objects.requireNonNull(Main.class.getResource("/images/Items/null.png")).toURI()));

        // cache bot profile for faster image making
        botProfile = ImageIO.read(new File(Objects.requireNonNull(Main.class.getResource("/images/botProfile.png")).toURI()));
        botProfileScaled = new BufferedImage(226, 226, BufferedImage.TYPE_INT_ARGB);
        {
            Graphics2D g2d = botProfileScaled.createGraphics();
            g2d.drawImage(botProfile, 0, 0, 226, 226, null);
            g2d.dispose();
        }

        // read font
        minecraftFont = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(Main.class.getResourceAsStream("/fonts/test.ttf")));

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

            //return;
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

//        // debug thing
//        if (System.getenv("debugfile") != null) {
//            String name = System.getenv("debugfile");
//            byte[] debugFileBytes;
//            try (InputStream stream = Main.class.getResourceAsStream("/" + name)) {
//                assert stream != null;
//                debugFileBytes = stream.readAllBytes();
//            }
//
//            JSONObject debugJson = new JSONObject(new String(debugFileBytes));
//
//            String debugName = debugJson.getString("Name");
//            String debugId = debugJson.getString("UUID");
//            JSONObject hypixelJson = debugJson.getJSONObject("Api");
//
//            HypixelPlayerData hypixelData = new HypixelPlayerData(hypixelJson);
//
//            JSONObject bwjson = hypixelData.stats.getJSONObject("Bedwars");
//
//            BufferedImage statsImage;
//            try {
//                statsImage = ((Bedwars) commands.get(1)).generateStatsImage(hypixelData);
//            } catch (IllegalArgumentException e) {
//                throw new RuntimeException(e);
//            }
//
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            ImageIO.write(statsImage, "png", outputStream);
//            FileUpload f = FileUpload.fromData(new ByteArrayInputStream(outputStream.toByteArray()), String.format("debug bedwars stats for %s meowmewomemwmeowmemrmrmemwo.png", name));
//
//            jda.awaitReady();
//            Guild testingGuild = jda.getGuildById(System.getenv("debugserver"));
//            assert testingGuild != null;
//
//            TextChannel channel = testingGuild.getTextChannelById(System.getenv("debugchannel"));
//            assert channel != null;
//
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            channel.sendFiles().queue();
//        }
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
        for (Command command : commands)
        {
            if (event.getName().equals("bedwars"))
            {
                // todo: write thing to access mojang api maybe?
            }
        }
    }
}