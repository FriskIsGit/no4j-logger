package no4j.core;

import org.junit.Test;

import static org.junit.Assert.*;

public class LevelTest {

    @Test
    public void testCustomLevel() {
        int value = (Level.WARN.value + Level.INFO.value) / 2;
        Level mildWarn = Level.custom(value, "MILD WARN");
        assertNotNull(mildWarn);
        assertEquals(mildWarn.value, value);
    }

    @Test
    public void testInvalidCustomLevel() {
        Level invalid = Level.custom(-1, "INVALID");
        assertNull(invalid);
    }

    @Test
    public void testInvalidLevelName() {
        Level invalid = Level.byName("INVALID");
        assertNull(invalid);
    }

    @Test
    public void testInvalidLevelValue() {
        Level invalid = Level.toLevel(-1);
        assertNull(invalid);
    }

    @Test
    public void testLevelByName() {
        assertEquals(Level.OFF, Level.byName(Level.OFF.name));
        assertEquals(Level.UNREACHABLE, Level.byName(Level.UNREACHABLE.name));
        assertEquals(Level.FATAL, Level.byName(Level.FATAL.name));
        assertEquals(Level.ERROR, Level.byName(Level.ERROR.name));
        assertEquals(Level.WARN, Level.byName(Level.WARN.name));
        assertEquals(Level.INFO, Level.byName(Level.INFO.name));
        assertEquals(Level.DEBUG, Level.byName(Level.DEBUG.name));
    }

    @Test
    public void testValueToLevel() {
        assertEquals(Level.OFF, Level.toLevel(Level.OFF.value));
        assertEquals(Level.UNREACHABLE, Level.toLevel(Level.UNREACHABLE.value));
        assertEquals(Level.FATAL, Level.toLevel(Level.FATAL.value));
        assertEquals(Level.ERROR, Level.toLevel(Level.ERROR.value));
        assertEquals(Level.WARN, Level.toLevel(Level.WARN.value));
        assertEquals(Level.INFO, Level.toLevel(Level.INFO.value));
        assertEquals(Level.DEBUG, Level.toLevel(Level.DEBUG.value));
    }
}
