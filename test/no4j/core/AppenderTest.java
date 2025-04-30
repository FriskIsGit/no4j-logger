package no4j.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class AppenderTest {

    @Test
    public void testEmittingMessages() {
        Logger logger = Logger.getLoggerWithLevel("emit", Level.ALL);
        String expected = "short text";

        final String[] actual = { "" };
        Appender appender = newMessage -> actual[0] = newMessage.message;
        logger.addAppender(appender);
        logger.info(expected);
        assertEquals(expected, actual[0]);
    }

    @Test
    public void testNoneEmitted() {
        Logger logger = Logger.getLoggerWithLevel("none-emit", Level.OFF);

        final boolean[] emitted = { false };
        Appender appender = newMessage -> emitted[0] = true;
        logger.addAppender(appender);
        logger.fatal("Don't emit that");
        assertFalse(emitted[0]);
    }
}
