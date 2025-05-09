package no4j.extensions;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

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

    @Test
    public void testAtMostEveryMillisecond() throws InterruptedException {
        LogSite site = new LogSite();
        int calls = 0;
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            if (site.atMostEvery(1, TimeUnit.MILLISECONDS)) {
                calls++;
            }
        }
        long end = System.currentTimeMillis();
        // This loop should have been executed under a millisecond. If not, the test is inconclusive.
        if (end - start > 1) {
            return;
        }
        CountDownLatch waiter = new CountDownLatch(1);
        assertFalse(waiter.await(1, TimeUnit.MILLISECONDS));
        if (site.atMostEvery(1, TimeUnit.MILLISECONDS)) {
            calls++;
        }
        assertEquals(2, calls);
    }

    @Test
    public void testAtMostNoTimeLimit() {
        LogSite site = new LogSite();
        int calls = 0;
        for (int i = 0; i < 10; i++) {
            if (site.atMostEvery(0, TimeUnit.HOURS)) {
                calls++;
            }
        }
        assertEquals(10, calls);
    }

    @Test
    public void testEveryAndAtMost() {
        LogSite site = new LogSite();
        int calls = 0;
        final int every = 3, atMost = 5;
        int iterations = 13;
        int expectedCalls = Math.min(atMost, (int)Math.ceil((double) iterations /every));
        for (int i = 0; i < iterations; i++) {
            // C s s C s s C s s C s s C
            if (site.every(every) && site.atMost(atMost)) {
                calls++;
            }
        }
        assertEquals(expectedCalls, calls);
    }
}
