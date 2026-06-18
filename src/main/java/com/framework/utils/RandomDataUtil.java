package com.framework.utils;

import java.util.Random;
import java.util.UUID;

/**
 * Utility class for generating random test data values.
 *
 * <pre>
 *   String name  = RandomDataUtil.alpha(8);          // "AbCdEfGh"
 *   String email = RandomDataUtil.email();            // "user_xk7q@test.com"
 *   String phone = RandomDataUtil.numericString(10);  // "0432198765"
 * </pre>
 */
public final class RandomDataUtil {

    private static final String ALPHA     = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String LOWER     = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS    = "0123456789";
    private static final String ALPHANUM  = ALPHA + DIGITS;
    private static final Random RND       = new Random();

    private RandomDataUtil() {}

    /** Random alphabetic string of given length. */
    public static String alpha(int length) {
        return build(ALPHA, length);
    }

    /** Random lowercase alphabetic string. */
    public static String lower(int length) {
        return build(LOWER, length);
    }

    /** Random numeric string of given length (may start with 0). */
    public static String numericString(int length) {
        return build(DIGITS, length);
    }

    /** Random alphanumeric string. */
    public static String alphanumeric(int length) {
        return build(ALPHANUM, length);
    }

    /** Positive random integer in [min, max]. */
    public static int intBetween(int min, int max) {
        return min + RND.nextInt(max - min + 1);
    }

    /** A unique UUID string. */
    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    /** A test-safe random email address. */
    public static String email() {
        return "user_" + lower(6) + "@automation.test";
    }

    /** A random 10-digit phone number string. */
    public static String phone() {
        return "0" + numericString(9);
    }

    /** Random container number in ISO 6346 format prefix (4 letters + 7 digits). */
    public static String containerNumber() {
        return alpha(4).toUpperCase() + numericString(7);
    }

    // ── Internal builder ─────────────────────────────────────────────────

    private static String build(String chars, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(RND.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
