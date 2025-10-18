package gui.styles;

import java.awt.*;

public class Fonts {
    private static Font primaryFont;
    private static Font boldFont;

    static {
        Font baseFont = new Font("Georgia", Font.PLAIN, 14);

        primaryFont = baseFont.deriveFont(Font.PLAIN, 14);
        boldFont = baseFont.deriveFont(Font.BOLD, 14);
    }

    public static Font getPrimaryFont() {
        return primaryFont;
    }
    public static Font getBoldFont() {
        return boldFont;
    }

    public static Font getTitleFont() {
        return boldFont.deriveFont(Font.BOLD, 24);
    }

    public static Font getHeaderFont() {
        return boldFont.deriveFont(Font.BOLD, 16);
    }

    public static Font getButtonFont() {
        return boldFont.deriveFont(Font.BOLD, 13);
    }

    public static Font getTableFont() {
        return primaryFont.deriveFont(Font.PLAIN, 13);
    }

    public static Font getInputFont() {
        return primaryFont.deriveFont(Font.PLAIN, 14);
    }
}