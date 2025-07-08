package com.lcv.commands.misc;

import com.lcv.Main;
import com.lcv.commands.Command;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class Image implements Command
{

    @Override
    public String getName()
    {
        return "image";
    }

    @Override
    public String getDescription()
    {
        return "Sends an image";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event)
    {
        event.deferReply().queue();
        FileUpload image = getImage();
        event.getHook().editOriginalAttachments(image).queue();
    }

    public FileUpload getImage()
    {
        try
        {
            File image = new File(Main.class.getClassLoader().getResource("images/Backgrounds/bedwarsBackground0.png").toURI());
            Font minecraftFont = Font.createFont(Font.TRUETYPE_FONT, Main.class.getClassLoader().getResourceAsStream("fonts/minecraft.ttf"));
            BufferedImage bufferedImage = ImageIO.read(image);
            Graphics2D g2d = bufferedImage.createGraphics();
            g2d.setFont(minecraftFont.deriveFont(60f));
            g2d.setColor(Color.BLACK);
            g2d.drawImage(ImageIO.read(new File(Main.class.getClassLoader().getResource("images/overlayNoText.png").toURI())), 0, 0, null);
            g2d.drawString("im writing this at 0, 120 i think", 0, 120);
            g2d.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            FileUpload fileUpload = FileUpload.fromData(new ByteArrayInputStream(baos.toByteArray()), "image.png");
            return fileUpload;
        }
        catch (URISyntaxException | IOException | FontFormatException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addFields(SlashCommandData data)
    {

    }
}
