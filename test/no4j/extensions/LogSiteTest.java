package no4j.extensions;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LogSiteTest {
    @Test
    public void testAtMost3() {
        LogSite site = new LogSite();
        int calls = 0;
        for (int i = 0; i < 10; i++) {
            if (site.atMost(3)) {
                calls++;
            }
        }

        assertEquals(3, calls);
    }

    @Test
    public void testAtMostNone() {
        LogSite site = new LogSite();
        int calls = 0;
        for (int i = 0; i < 10; i++) {
            if (site.atMost(0)) {
                calls++;
            }
        }

        assertEquals(0, calls);
    }

    @Test
    public void testEvery5th() {
        LogSite site = new LogSite();
        int calls = 0;
        for (int i = 0; i < 6; i++) {
            if (site.every(5)) {
                calls++;
            }
        }
        assertEquals(2, calls);
    }

    @Test
    public void testEvery1() {
        LogSite site = new LogSite();
        int calls = 0;
        for (int i = 0; i < 10; i++) {
            if (site.every(1)) {
                calls++;
            }
        }
        assertEquals(10, calls);
    }

    @Test
    public void testEveryNonPositive() {
        LogSite site = new LogSite();
        int calls = 0;
        for (int i = 0; i < 10; i++) {
            if (site.every(0)) {
                calls++;
            }
            if (site.every(-5)) {
                calls++;
            }
        }
        assertEquals(0, calls);
    }
}
