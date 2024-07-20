package no4j.core;

public class Color {
    private static final String ESC = "\033[";

    public static final Color RESET = new Color(ESC + 'm');

    public static final String FG_BLACK   = "30";
    public static final String FG_RED     = "31";
    public static final String FG_GREEN   = "32";
    public static final String FG_YELLOW  = "33";
    public static final String FG_BLUE    = "34";
    public static final String FG_MAGENTA = "35";
    public static final String FG_CYAN    = "36";
    public static final String FG_WHITE   = "37";

    public static final String FG_BRIGHT_BLACK   = "90";
    public static final String FG_BRIGHT_RED   = "91";
    public static final String FG_BRIGHT_GREEN   = "92";
    public static final String FG_BRIGHT_YELLOW   = "93";
    public static final String FG_BRIGHT_BLUE  = "94";
    public static final String FG_BRIGHT_MAGENTA  = "95";
    public static final String FG_BRIGHT_CYAN = "96";
    public static final String FG_BRIGHT_WHITE = "97";

    public static final String BG_BLACK   = "40";
    public static final String BG_RED     = "41";
    public static final String BG_GREEN   = "42";
    public static final String BG_YELLOW  = "43";
    public static final String BG_BLUE    = "44";
    public static final String BG_MAGENTA = "45";
    public static final String BG_CYAN    = "46";
    public static final String BG_WHITE   = "47";

    public final String sgr;

    public Color(String fg, String bg) {
        this.sgr = ESC + fg + ';' + bg + 'm';
    }

    public static Color of(String fg, String bg) {
        return new Color(fg, bg);
    }

    public static Color fg(String fg) {
        return new Color(ESC + fg + 'm');
    }

    public static Color fgBold(String fg) {
        return new Color(ESC + "1;" + fg + 'm');
    }

    public static Color fgUnderline(String fg) {
        return new Color(ESC + "4;" + fg + 'm');
    }

    public static Color fgReverse(String fg) {
        return new Color(ESC + "7;" + fg + 'm');
    }

    private Color(String sgr) {
        this.sgr = sgr;
    }

    @Override
    public String toString() {
        return sgr;
    }
}
