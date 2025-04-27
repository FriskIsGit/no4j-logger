package no4j.core;

import java.io.PrintStream;

/**
 * <code>Console</code> holds output streams and color configurations for each log level.
 * The default sinks - STDOUT and STDERR can be changed which streamlines debugging or mocking.
 */

public class Console {
    private volatile boolean useColor = false;
    protected PrintStream stdOut;
    protected PrintStream stdErr;

    private Color unreachable = Color.of(Color.FG_BRIGHT_WHITE, Color.BG_BLACK);
    private Color fatal = Color.fgUnderline(Color.FG_RED);
    private Color error = Color.fg(Color.FG_BRIGHT_RED);
    private Color warning = Color.fg(Color.FG_YELLOW);
    private Color info = Color.fg(Color.FG_CYAN);
    private Color debug = Color.fg(Color.FG_MAGENTA);
    private Color custom = Color.fg(Color.FG_GREEN);

    private Console(PrintStream stdOut, PrintStream stdErr) {
        this.stdOut = stdOut;
        this.stdErr = stdErr;
    }

    public void setStdOut(PrintStream out) {
        stdOut = out;
    }

    public void setStdErr(PrintStream outErr) {
        stdErr = outErr;
    }

    public static Console newDefault() {
        return new Console(System.out, System.err);
    }

    public void outPrint(String text) {
        print(text, stdOut);
    }

    public void errPrint(String text) {
        print(text, stdErr);
    }

    void print(String text, PrintStream stream) {
        stream.print(text);
    }

    public Color getColorByLevel(Level level) {
        switch (level.value) {
            case Level.UNREACHABLE_VALUE:
                return unreachable;
            case Level.FATAL_VALUE:
                return fatal;
            case Level.ERROR_VALUE:
                return error;
            case Level.WARN_VALUE:
                return warning;
            case Level.INFO_VALUE:
                return info;
            case Level.DEBUG_VALUE:
                return debug;
            default:
                // synonymous with CUSTOM
                return custom;
        }
    }

    public void enableColor(boolean enabled) {
        useColor = enabled;
    }

    public boolean isColorEnabled() {
        return useColor;
    }

    public void setUnreachable(Color unreachable) {
        if (unreachable == null) return;
        this.unreachable = unreachable;
    }

    public void setFatal(Color fatal) {
        if (fatal == null) return;
        this.fatal = fatal;
    }

    public void setError(Color error) {
        if (error == null) return;
        this.error = error;
    }

    public void setWarning(Color warning) {
        if (warning == null) return;
        this.warning = warning;
    }

    public void setInfo(Color info) {
        if (info == null) return;
        this.info = info;
    }

    public void setDebug(Color debug) {
        if (debug == null) return;
        this.debug = debug;
    }

    public void setCustom(Color custom) {
        if (custom == null) return;
        this.custom = custom;
    }

    public void inheritColors(Console console) {
        unreachable = console.unreachable;
        fatal = console.fatal;
        error = console.error;
        warning = console.warning;
        info = console.info;
        debug = console.debug;
        custom = console.custom;
    }

    public void resetPrint() {
        stdOut.print(Color.RESET);
    }
}
