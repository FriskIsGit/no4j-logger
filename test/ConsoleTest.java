import no4j.core.Color;
import no4j.core.Console;
import no4j.core.Level;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class ConsoleTest {
    private static final boolean PRINT_TEXT = true;

    @Test
    public void testOutPrint() {
        Console console = Console.newDefault();
        OutputStream buffer = mockStdout(console);
        console.outPrint("Plain text", Level.ALL);
        String actual = buffer.toString();
        if (PRINT_TEXT) {
            System.out.println(actual);
        }
        assertEquals("Plain text", actual);
    }

    @Test
    public void testGreenForegroundColor() {
        Color green = Color.fg(Color.FG_GREEN);

        Console console = Console.newDefault();
        console.enableColor(true);
        console.setInfo(green);

        ByteArrayOutputStream buffer = mockStdout(console);
        console.outPrint("Green foreground", Level.INFO);
        String actual = buffer.toString();
        if (PRINT_TEXT) {
            System.out.println(actual);
        }
        assertEquals(green + "Green foreground" + Color.RESET, actual);
    }

    @Test
    public void testYellowForegroundUnderline() {
        Color yellowUnderline = Color.fgUnderline(Color.FG_YELLOW);

        Console console = Console.newDefault();
        console.enableColor(true);
        console.setDebug(yellowUnderline);

        ByteArrayOutputStream buffer = mockStdout(console);
        console.outPrint("Yellow underline", Level.DEBUG);
        String actual = buffer.toString();
        if (PRINT_TEXT) {
            System.out.println(actual);
        }
        assertEquals(yellowUnderline + "Yellow underline" + Color.RESET, actual);
    }

    @Test
    public void testBlackForegroundWhiteBackground() {
        Color mixed = Color.of(Color.FG_BLACK, Color.BG_WHITE);

        Console console = Console.newDefault();
        console.enableColor(true);
        console.setFatal(mixed);

        ByteArrayOutputStream buffer = mockStdout(console);
        console.outPrint("Black and white", Level.FATAL);
        String actual = buffer.toString();
        if (PRINT_TEXT) {
            System.out.println(actual);
        }
        assertEquals(mixed + "Black and white" + Color.RESET, actual);
    }

    @Test
    public void testCyanFgPurpleBgRGB() {
        Color cyanPurple = Color.rgb(33, 214, 220, 149, 55, 206);

        Console console = Console.newDefault();
        console.enableColor(true);
        console.setWarning(cyanPurple);

        ByteArrayOutputStream buffer = mockStdout(console);
        console.outPrint("Cyan and purple RGB", Level.WARN);
        String actual = buffer.toString();
        if (PRINT_TEXT) {
            System.out.println(actual);
        }
        assertEquals(cyanPurple + "Cyan and purple RGB" + Color.RESET, actual);
    }

    private static ByteArrayOutputStream mockStdout(Console console) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream outStream = new PrintStream(buffer);
        console.setStdOut(outStream);
        return buffer;
    }
}
