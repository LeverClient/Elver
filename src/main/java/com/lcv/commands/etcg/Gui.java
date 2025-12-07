package com.lcv.commands.etcg;

import com.lcv.commands.Embed;
import com.lcv.util.ETCGUtil;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Gui
{
    protected List<ActionRow> rows = new ArrayList<>();
    protected final BufferedImage image;

    public Gui(BufferedImage image, ActionRow... rows)
    {
        this.image = image;
        this.rows.addAll(Arrays.asList(rows));
    }

    public BufferedImage get()
    {
        return image;
    }
    public Gui add(Button... buttons)
    {
        rows.add(ActionRow.of(buttons));
        return this;
    }
    public Gui add(ActionRow actionRow)
    {
        rows.add(actionRow);
        return this;
    }
    public List<ActionRow> buttons()
    {
        return rows;
    }
}
