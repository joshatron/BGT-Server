package io.joshatron.tak.server.utils;

import java.util.UUID;

public class IdUtils {

    private IdUtils() {
        throw new IllegalStateException("This is a utility class");
    }

    public static String generateId() {
        return UUID.randomUUID().toString();
    }
}
