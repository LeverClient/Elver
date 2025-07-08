package com.lcv.util;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

public class FontRenderer {
    public static Color[] colors = {
            new Color(0x000000), // 0
            new Color(0x0000AA), // 1
            new Color(0x00AA00), // 2
            new Color(0x00AAAA), // 3
            new Color(0xAA0000), // 4
            new Color(0xAA00AA), // 5
            new Color(0xFFAA00), // 6
            new Color(0xAAAAAA), // 7
            new Color(0x555555), // 8
            new Color(0x5555FF), // 9
            new Color(0x55FF55), // a
            new Color(0x55FFFF), // b
            new Color(0xFF5555), // c
            new Color(0xFF55FF), // d
            new Color(0xFFFF55), // e
            new Color(0xFFFFFF), // f
    };

    public static HashMap<Color, Color> shadowCache = new HashMap<>();

    private Graphics2D g2d;

    private final Font[] fonts;

    private final Font[] extraFonts; // bold offset = 0, italic offset = 1, both offset = 2

    private Font selectedFont;

    private int selectedFontIndex;

    public static Color getShadowColor(Color c) {
        if (shadowCache.containsKey(c))  {
            return shadowCache.get(c);
        }

        Color shadow = new Color(c.getRed()/4, c.getGreen()/4, c.getBlue()/4);
        shadowCache.put(c, shadow);
        return shadow;
    }

    public FontRenderer(Graphics2D g2d, Font[] fonts) {
        this.g2d = g2d;
        this.fonts = fonts;

        if (fonts.length > 0) {
            switchFont(0);
        }

        this.extraFonts = new Font[fonts.length*3];
        for (int i = 0; i < fonts.length; i++) {
            Font font = fonts[i];
            Font bold = font.deriveFont(Font.BOLD);
            Font italic = font.deriveFont(Font.ITALIC);
            Font boldItalic = font.deriveFont(Font.BOLD & Font.ITALIC);

            extraFonts[i*3] = bold;
            extraFonts[i*3 + 1] = italic;
            extraFonts[i*3 + 2] = boldItalic;
        }
    }

    public void switchFont(int font) {
        if (font > fonts.length) {
            throw new IndexOutOfBoundsException(String.format("Font index %d is out of bounds (>%d)", font, fonts.length-1));
        }

        selectedFont = fonts[font];
        selectedFontIndex = font;
        if (g2d != null) g2d.setFont(selectedFont);
    }

    public Font getFont(int font) {
        if (font > fonts.length) {
            throw new IndexOutOfBoundsException(String.format("Font index %d is out of bounds (>%d)", font, fonts.length-1));
        }

        return fonts[font];
    }

    public Font getSelectedFont() {
        return selectedFont;
    }

    public void setGraphics(Graphics2D g2d) {
        this.g2d = g2d;
        g2d.setFont(selectedFont);
    }

    public Graphics2D getGraphics() {
       return g2d;
    }

    public void drawString(String txt, int x, int y, boolean shadow, Color col) {
        if (g2d == null) throw new IllegalStateException("Attempt to DrawString without Graphics set");

        Color originalColor = g2d.getColor();
        if (col != null) {
            g2d.setColor(col);
        }

        String[] segments = txt.split("§");
        FontRenderSegment[] renderSegments = new FontRenderSegment[segments.length];
        renderSegments[0] = new FontRenderSegment(segments[0], col, selectedFont);

        boolean bold = false;
        boolean italic = false;
        FontRenderSegment currentRenderSegment = null;
        int renderSegmentIndex = 1;
        for (int i = 1; i < segments.length; i++) {
            String segment = segments[i];
            FontRenderSegment renderSegment = currentRenderSegment != null ? currentRenderSegment : new FontRenderSegment(null, col, selectedFont);
            char code = segment.charAt(0);

            // modify render segment based on character
            switch(code) {
                case 'l' -> {
                    int extraFontIndex = italic ? selectedFontIndex*3 + 2 : selectedFontIndex*3;
                    renderSegment.font = extraFonts[extraFontIndex];
                    bold = true;
                }

                case 'o' -> {
                    int extraFontIndex = bold ? selectedFontIndex*3 + 2 : selectedFontIndex*3 + 1;
                    renderSegment.font = extraFonts[extraFontIndex];
                    italic = true;
                }

                case 'r' -> {
                    renderSegment.color = col;
                    renderSegment.font = selectedFont;
                    renderSegment.underline = renderSegment.strikethrough = false;
                    bold = italic = false;
                }

                case 'n' -> {
                    renderSegment.underline = true;
                }

                case 'm' -> {
                    renderSegment.strikethrough = true;
                }

                default -> {
                    int colorCode = "0123456789abcdef".indexOf(code);
                    if (colorCode != -1) {
                        renderSegment.color = colors[colorCode];
                    }
                }
            }

            // only apply if next segment isn't another change
            if (segment.length() > 1) {
                renderSegment.text = segment.substring(1);
                renderSegments[renderSegmentIndex++] = renderSegment;
                currentRenderSegment = null;
            } else currentRenderSegment = renderSegment;
        }

        // draw shadow
        if (shadow) {
            int currentX = x;
            int offset = selectedFont.getSize()/48;
            for (FontRenderSegment segment : renderSegments) {
                if (segment == null) break;

                currentX += segment.draw(currentX + offset, y +offset, true);
            }
        }

        // draw not shadow
        int currentX = x;
        for (FontRenderSegment segment : renderSegments) {
            if (segment == null) break;

            currentX += segment.draw(currentX, y, false);
        }


        g2d.setColor(originalColor);
    }

    public class FontRenderSegment {
        public static FontRenderContext fontRendererContext = new FontRenderContext(null, false, false);
        public boolean underline = false;
        public boolean strikethrough = false;
        public Color color;
        public Font font;
        public String text;

        public FontRenderSegment(String txt, Color color, Font font) {
            this.text = txt;
            this.font = font;
            this.color = color;
        }

        public int draw(int x, int y, boolean isShadow) {
            Rectangle2D bounds = font.getStringBounds(text, fontRendererContext);
            int width = (int) Math.ceil(bounds.getWidth());
            int fontSize = (int) font.getSize2D();

            g2d.setColor(isShadow ? getShadowColor(color) : color);
            g2d.drawString(text, x, y);

            if (underline) {
                g2d.fillRect(x, y + (int) (bounds.getMaxY()/2), width, fontSize/24);
            }

            if (strikethrough) {
                g2d.fillRect(x, y + (int) (bounds.getCenterY()/1.2), width, fontSize/24);
            }

            return width;
        }
    }
}
