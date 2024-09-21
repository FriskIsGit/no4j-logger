package no4j.extensions;

import java.util.concurrent.TimeUnit;

/**
 * This class facilitates logging per site.
 * Due to design limitations and for lack of a better alternative this is an extension that can be used separately
 * to enable logging per site while incurring minimal overhead.
 * <p>
 * It's expensive to fetch the log site on each log call to uniquely identify a line of code.
 * Designing a complex, yet efficient and error-proof architecture to allow for this is out of the scope for this project.
 * Storing log site data per logger is not viable because any logger can be used in two different places.
 * </p>
 * Example usage:
 * <pre><code>
 * // Store it as a field
 * LogSite debugSite = new LogSite();
 * ...
 * if (debugSite.every(500) && debugSite.atMost(20)) {
 *     log.debug("Logging with a log site!");
 * }
 * </code></pre>
 * A log site can be disabled by setting <var>rateLimit</var> to false
 */
public class LogSite {
    private boolean rateLimit = true;

    private boolean firstCall = true;
    private long everyCalls = 0;
    private long atMostCalls = 0;
    private long lastCallNanoTime = 0;

    public LogSite() {
    }

    public static LogSite New() {
        return new LogSite();
    }

    /**
     * Tracks invocations to permit a call every <code>n</code>th time, the exception is the first call
     * which returns true.
     * When <var>rateLimit</var> is set to <code>false</code> the call count is not affected.
     *
     * @param n number of calls to skip before allowing the next call
     * @return true if it's the <code>n</code>th call, false otherwise
     * @see LogSite#atMost(int)
     */
    public boolean every(int n) {
        if (!rateLimit) {
            return true;
        }
        if (firstCall) {
            firstCall = false;
            return true;
        }
        if (++everyCalls < n) {
            return false;
        }
        everyCalls = 0;
        return true;
    }

    /**
     * Tracks invocations to permit only <code>n</code> number of calls after which the log site will return false
     * indefinitely until the site is reset.
     * When <var>rateLimit</var> is set to <code>false</code> the call count is not affected.
     *
     * @param n the number of calls to skip before allowing the next call
     * @return true until the <code>n</code>th call occurs, false afterwards
     * @see LogSite#atMostEvery(int, TimeUnit)
     */
    public boolean atMost(int n) {
        if (!rateLimit) {
            return true;
        }
        return atMostCalls++ < n;
    }

    /**
     * Permits calls based on the time passed since the previous call.
     * It limits the frequency of calls allowing one call per <var>duration = n * unit</var>
     *
     * @param n    the minimum number of time units that must pass to permit the next call
     * @param unit the time unit
     * @return true if the duration elapsed since the previous call, otherwise false
     */
    public boolean atMostEvery(int n, TimeUnit unit) {
        if (!rateLimit) {
            return true;
        }
        long nanoNow = System.nanoTime();
        if (nanoNow >= lastCallNanoTime + unit.toNanos(n)) {
            lastCallNanoTime = nanoNow;
            return true;
        }
        return false;
    }

    /**
     * When <var>rateLimit</var> is set to <code>false</code> every rate limiting method returns <code>true</code>
     *
     * @see LogSite#every(int)
     * @see LogSite#atMost(int)
     * @see LogSite#atMostEvery(int, TimeUnit)
     */
    public void enableRateLimit(boolean enabled) {
        rateLimit = enabled;
    }

    /**
     * Resets this log site, including all state related to:
     * {@link LogSite#every}, {@link LogSite#atMost}, {@link LogSite#atMostEvery}
     */
    public void reset() {
        firstCall = true;
        everyCalls = 0;
        atMostCalls = 0;
        lastCallNanoTime = 0;
    }

    public void resetEvery() {
        firstCall = true;
        everyCalls = 0;
    }

    public void resetAtMost() {
        atMostCalls = 0;
    }

    public void resetAtMostEvery() {
        lastCallNanoTime = 0;
    }
}
