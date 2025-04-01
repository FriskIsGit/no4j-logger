package no4j;

import no4j.core.Console;
import no4j.core.Logger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class Mocks {

    public static ByteArrayOutputStream mockStdout(Console console) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream outStream = new PrintStream(buffer, true);
        console.setStdOut(outStream);
        return buffer;
    }

    public static ByteArrayOutputStream mockStdout(Logger logger) {
        Console console = logger.getConsole();
        return mockStdout(console);
    }

    public static ByteArrayOutputStream mockStderr(Logger logger) {
        Console console = logger.getConsole();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream outStream = new PrintStream(buffer, true);
        console.setStdErr(outStream);
        return buffer;
    }
}
