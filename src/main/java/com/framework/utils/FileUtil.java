package com.framework.utils;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * File system helpers used by tests for managing screenshots, downloads,
 * and test-data files.
 */
public final class FileUtil {

    private FileUtil() {}

    /** Creates the directory (and all parents) if it does not already exist. */
    public static void mkdirs(String dirPath) {
        new File(dirPath).mkdirs();
    }

    /**
     * Returns the most recently modified file in {@code dirPath} matching
     * any of the given extensions (e.g. {@code ".csv", ".xlsx"}).
     *
     * @return the file, or {@code null} if none found
     */
    public static File latestFile(String dirPath, String... extensions) {
        File dir = new File(dirPath);
        if (!dir.exists()) return null;
        File[] files = dir.listFiles(f -> {
            String name = f.getName().toLowerCase();
            for (String ext : extensions) {
                if (name.endsWith(ext.toLowerCase())) return true;
            }
            return false;
        });
        if (files == null || files.length == 0) return null;
        return Arrays.stream(files)
            .max(Comparator.comparingLong(File::lastModified))
            .orElse(null);
    }

    /** Reads the entire content of a text file as a single string. */
    public static String readText(String filePath) {
        try {
            return Files.readString(Path.of(filePath));
        } catch (IOException e) {
            throw new RuntimeException("Cannot read file: " + filePath, e);
        }
    }

    /** Writes {@code content} to a file, creating parent directories as needed. */
    public static void writeText(String filePath, String content) {
        try {
            Path path = Path.of(filePath);
            Files.createDirectories(path.getParent());
            Files.writeString(path, content);
        } catch (IOException e) {
            throw new RuntimeException("Cannot write file: " + filePath, e);
        }
    }

    /** Deletes all files inside a directory (non-recursive). */
    public static void cleanDir(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) return;
        Arrays.stream(Objects.requireNonNull(dir.listFiles()))
            .filter(File::isFile)
            .forEach(File::delete);
    }

    /** Lists all files in {@code dirPath} with the given extension. */
    public static List<File> listFiles(String dirPath, String extension) {
        File dir = new File(dirPath);
        if (!dir.exists()) return Collections.emptyList();
        File[] files = dir.listFiles(f -> f.getName().toLowerCase().endsWith(extension.toLowerCase()));
        return files == null ? Collections.emptyList() : Arrays.asList(files);
    }
}
