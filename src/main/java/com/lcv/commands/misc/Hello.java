package com.lcv.commands.misc;

import com.lcv.Main;
import com.lcv.commands.ICommand;
import com.lcv.util.FontRenderer;
import com.lcv.util.ImageUtil;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import static com.lcv.Main.ALL_CONTEXTS;

public class Hello implements ICommand
{
    @Override
    public String getName()
    {
        return "hello";
    }

    @Override
    public String getDescription()
    {
        return "Says hello!";
    }

    @Override
    public Set<InteractionContextType> getContexts() {
        return ALL_CONTEXTS;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event)
    {
        BufferedImage image = new BufferedImage(2500, 3500, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.drawImage(ImageUtil.loadImage("/ETCG/assets/trap_card.png"), 0, 0, null);
        g2d.drawImage(ImageUtil.loadImage("/ETCG/assets/starter_pack/blue_eyes_white_dragon.png"), 100, 100, null);

        FontRenderer f = new FontRenderer(g2d, new Font[]{Main.minecraftFont.deriveFont(120f)});
        f.drawString("Blue-Eyes", 150, 3050);
        f.drawString("White Dragon", 150, 3200);

        f.drawString("ATK: 3000", 1690, 3050);
        f.drawString("DEF: 2500", 1690, 3200);

        f.drawString("Description Placeholder", 1250, 2250, FontRenderer.CenterXAligned);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            ImageIO.write(image, "png", baos);
            FileUpload file = FileUpload.fromData(new ByteArrayInputStream(baos.toByteArray()), "test.png");
            event.replyFiles(file).queue();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addFields(SlashCommandData data)
    {

    }
}
