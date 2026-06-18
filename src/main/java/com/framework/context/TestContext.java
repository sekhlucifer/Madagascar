package com.framework.context;

import java.util.HashMap;
import java.util.Map;

/**
 * Thread-local key/value store for sharing data between test steps within
 * the same test execution thread.
 *
 * <pre>
 *   TestContext.put("bookingId", "BKG-001");
 *   String id = TestContext.get("bookingId");
 * </pre>
 */
public final class TestContext {

    private static final ThreadLocal<Map<String, Object>> STORE =
        ThreadLocal.withInitial(HashMap::new);

    private TestContext() {}

    public static void put(String key, Object value) {
        STORE.get().put(key, value);
    }

    public static Object get(String key) {
        return STORE.get().get(key);
    }

    public static String getString(String key) {
        Object val = STORE.get().get(key);
        return val != null ? val.toString() : null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getAs(String key) {
        return (T) STORE.get().get(key);
    }

    public static boolean contains(String key) {
        return STORE.get().containsKey(key);
    }

    /** Clears the store for the current thread — call in {@code @AfterMethod}. */
    public static void clear() {
        STORE.get().clear();
    }

    /** Removes the thread-local entirely to prevent memory leaks in thread pools. */
    public static void remove() {
        STORE.remove();
    }
}
