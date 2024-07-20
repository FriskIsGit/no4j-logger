package no4j.core;

import java.io.PrintStream;

/**
 * The <code>Console</code> is a class capable of printing using ANSI colors as specified by the
 * SGR - 'Select Graphic Rendition' standard. It is part of CSI - Control Sequence Introducer.
 *
 * <p>Colors are specified in the following format: <code>ESC[FG;BGm</code>
 * <ul>
 *   <li>FG - foreground string value in range 30-37</li>
 *   <li>BG - background string value in range 40-47</li>
 * </ul>
 */

public class Console {
    private volatile boolean useColor = false;
    private final PrintStream stdOut;
    private final PrintStream stdErr;

    public Color unreachable = Color.of(Color.FG_BRIGHT_WHITE, Color.BG_BLACK);
    public Color fatal = Color.fgUnderline(Color.FG_RED);
    public Color error = Color.fg(Color.FG_BRIGHT_RED);
    public Color warning = Color.fg(Color.FG_YELLOW);
    public Color info = Color.fg(Color.FG_CYAN);
    public Color debug = Color.fg(Color.FG_MAGENTA);
    public Color custom = Color.fg(Color.FG_GREEN);

    private Console(PrintStream stdOut, PrintStream stdErr) {
        this.stdOut = stdOut;
        this.stdErr = stdErr;
    }

    public static Console newDefault() {
        return new Console(System.out, System.err);
    }

    public void outPrint(String msg, Level level) {
        print(msg, level, stdOut);
    }

    public void errPrint(String msg, Level level) {
        print(msg, level, stdErr);
    }

    void print(String msg, Level level, PrintStream stream) {
        if (!useColor) {
            stream.print(msg);
            return;
        }
        switch (level.value) {
            case Level.UNREACHABLE_VALUE:
                stream.print(unreachable + msg + Color.RESET);
                break;
            case Level.FATAL_VALUE:
                stream.print(fatal + msg + Color.RESET);
                break;
            case Level.ERROR_VALUE:
                stream.print(error + msg + Color.RESET);
                break;
            case Level.WARN_VALUE:
                stream.print(warning + msg + Color.RESET);
                break;
            case Level.INFO_VALUE:
                stream.print(info + msg + Color.RESET);
                break;
            case Level.DEBUG_VALUE:
                stream.print(debug + msg + Color.RESET);
                break;
            default:
                // synonymous with CUSTOM
                stream.print(custom + msg + Color.RESET);
                break;
        }
    }

    public void enableColor(boolean enabled) {
        useColor = enabled;
    }

    public void setUnreachable(Color unreachable) {
        this.unreachable = unreachable;
    }

    public void setFatal(Color fatal) {
        this.fatal = fatal;
    }

    public void setError(Color error) {
        this.error = error;
    }

    public void setWarning(Color warning) {
        this.warning = warning;
    }

    public void setInfo(Color info) {
        this.info = info;
    }

    public void setDebug(Color debug) {
        this.debug = debug;
    }

    public void setCustom(Color custom) {
        this.custom = custom;
    }

    public void resetPrint() {
        System.out.print(Color.RESET);
    }
}
