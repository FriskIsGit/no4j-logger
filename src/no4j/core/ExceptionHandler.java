package no4j.core;

import java.io.IOException;

/**
 * Handler for IO exceptions that occur internally, such as during:
 * <ul>
 *     <li>writing to a file</li>
 *     <li>compression</li>
 *     <li>reading from a file</li>
 * </ul>
 */
public interface ExceptionHandler {
    void handle(IOException e);
}
