package com.lcv.commands.misc;

import com.lcv.Main;
import com.lcv.commands.Command;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
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
        File image = getImage();
        event.replyFiles(FileUpload.fromData(image)).queue();
    }

    public File getImage()
    {
        try
        {
            File image = new File(Main.class.getClassLoader().getResource("images/bedwarsBackground.png").toURI());
            return image;
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addFields(SlashCommandData data)
    {

    }
}
