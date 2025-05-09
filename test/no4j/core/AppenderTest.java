package no4j.core;

import org.junit.Test;

import static org.junit.Assert.*;

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

    @Test
    public void testManyAppenders() {
        Logger logger = Logger.getLoggerWithLevel("many-emit", Level.INFO);

        final int[] emitted = new int[3];
        Appender appender1 = newMessage -> emitted[0]++;
        Appender appender2 = newMessage -> emitted[1]++;
        Appender appender3 = newMessage -> emitted[2]++;
        logger.addAppender(appender1);
        logger.addAppender(appender2);
        logger.addAppender(appender3);
        logger.info("Three emits");
        logger.info("Six emits");
        assertEquals(2, emitted[0]);
        assertEquals(2, emitted[1]);
        assertEquals(2, emitted[2]);
    }
}
