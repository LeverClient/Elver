package com.lcv.commands.etcg;

import com.lcv.Main;
import com.lcv.commands.CommandMeta;
import com.lcv.commands.Embed;
import com.lcv.commands.ICommand;
import com.lcv.commands.etcg.cards.*;
import com.lcv.util.ETCGUtil;
import com.lcv.util.FontRenderer;
import com.lcv.util.ImageUtil;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;
import static net.dv8tion.jda.api.interactions.InteractionContextType.*;

@CommandMeta(name = "etcg", description = "TCG for Elver", contexts = {GUILD, BOT_DM, PRIVATE_CHANNEL})
public class ETCG implements ICommand
{
    public static DB db;
    public static HTreeMap<String, CardTemplate> templates;

    @Override
    public void execute(SlashCommandInteractionEvent event)
    {
        event.deferReply().queue();
        try
        {
            Gui mainMenu = mainMenu(event);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(mainMenu.get(), "png", baos);
            FileUpload file = FileUpload.fromData(new ByteArrayInputStream(baos.toByteArray()), "test.png");
            event.getHook().sendFiles(file).queue();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static Gui mainMenu(GenericInteractionCreateEvent event) throws IOException
    {
        BufferedImage background = ETCGUtil.getRandomBackground();
        Graphics2D g2d = background.createGraphics();
        FontRenderer f = new FontRenderer(g2d, new Font[]{ImageUtil.MINECRAFT_FONT.deriveFont(144f)});
        BufferedImage avatar = ImageUtil.toCircle(ImageIO.read(new URL(event.getUser().getEffectiveAvatarUrl() + "?size=512")));
        g2d.drawImage(avatar, 25, 300, 512, 512, null);

        f.drawString(event.getUser().getName(), 1440 - (g2d.getFontMetrics().stringWidth(event.getUser().getName()) / 2), 75);

        g2d.dispose();
        return new Gui(background)
                .add(Button.primary("profile", "Profile"))
                .add(Button.primary("packs", "Packs"))
                .add(Button.primary("quests", "Quests"))
                .add(Button.primary("battle", "Battle"));
    }

    public static Embed profile(GenericInteractionCreateEvent event)
    {
        return null;
    }

    @Override
    public void addFields(SlashCommandData data) {
        data.addOption(STRING, "menu", "Menu Subcommands", false, true);
    }
}
