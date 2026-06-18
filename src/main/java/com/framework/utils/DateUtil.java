package com.framework.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Date and time utility methods for generating dynamic test-data values.
 */
public final class DateUtil {

    private static final DateTimeFormatter DD_MM_YYYY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private DateUtil() {
    }

    /** Today's date in {@code MM/dd/yyyy} format. */
    public static String today() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
    }

    /** Date {@code daysOffset} days from today, in {@code dd/MM/yyyy} format. */
    public static String dateWithOffset(int daysOffset) {
        return LocalDate.now().plusDays(daysOffset).format(DD_MM_YYYY);
    }

    /** Date in ISO format {@code yyyy-MM-dd}. */
    public static String isoDate(int daysOffset) {
        return LocalDate.now().plusDays(daysOffset).format(YYYY_MM_DD);
    }

    /**
     * Current timestamp string suitable for file names ({@code yyyyMMdd_HHmmss}).
     */
    public static String timestamp() {
        return LocalDateTime.now().format(TIMESTAMP);
    }

    /**
     * Checks whether {@code dateStr} (in {@code dd/MM/yyyy}) falls between
     * {@code from} and {@code to} (inclusive).
     */
    public static boolean isBetween(String from, String to, String dateStr) {
        if (dateStr == null || dateStr.isBlank())
            return true;
        LocalDate f = LocalDate.parse(from, DD_MM_YYYY);
        LocalDate t = LocalDate.parse(to, DD_MM_YYYY);
        LocalDate d = LocalDate.parse(dateStr, DD_MM_YYYY);
        return !d.isBefore(f) && !d.isAfter(t);
    }

    public static boolean isValidDate(String date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate.parse(date, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
