package com.framework.browser;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.Playwright;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static com.framework.config.ConfigurationManager.config;

/**
 * Enum-based factory that creates a {@link Browser} for each supported browser type.
 *
 * <p>Thread-safe remote-debugging ports are allocated dynamically so that
 * parallel test threads do not collide.
 */
public enum BrowserFactory {

    CHROME {
        @Override
        public Browser launch(Playwright playwright) {
            return playwright.chromium().launch(
                new LaunchOptions()
                    .setChannel(config().browser())           // "chrome" → real Chrome
                    .setArgs(Arrays.asList(
                        "--remote-debugging-port=" + nextDebugPort(),
                        "--ignore-certificate-errors",
                        "--no-sandbox",
                        "--disable-setuid-sandbox",
                        "--start-maximized"
                    ))
                    .setSlowMo(config().slowMotion())
                    .setHeadless(config().headless())
            );
        }
    },

    CHROMIUM {
        @Override
        public Browser launch(Playwright playwright) {
            return playwright.chromium().launch(defaultOptions());
        }
    },

    FIREFOX {
        @Override
        public Browser launch(Playwright playwright) {
            return playwright.firefox().launch(defaultOptions());
        }
    },

    EDGE {
        @Override
        public Browser launch(Playwright playwright) {
            return playwright.chromium().launch(
                defaultOptions().setChannel("msedge")
            );
        }
    },

    WEBKIT {
        @Override
        public Browser launch(Playwright playwright) {
            return playwright.webkit().launch(defaultOptions());
        }
    };

    // ── Abstract factory method ────────────────────────────────────────────
    public abstract Browser launch(Playwright playwright);

    // ── Port counter for remote-debugging (parallel-safe) ─────────────────
    private static final AtomicInteger PORT_COUNTER = new AtomicInteger(9222);

    private static int nextDebugPort() {
        return PORT_COUNTER.getAndIncrement();
    }

    // ── Shared launch options ──────────────────────────────────────────────
    private static LaunchOptions defaultOptions() {
        return new LaunchOptions()
            .setHeadless(config().headless())
            .setSlowMo(config().slowMotion())
            .setArgs(Collections.singletonList("--start-maximized"));
    }

    // ── Convenience resolver ───────────────────────────────────────────────
    /**
     * Resolves the factory from a browser name string (case-insensitive).
     * Falls back to {@link #CHROMIUM} for unknown names.
     */
    public static BrowserFactory from(String browserName) {
        return switch (browserName.toLowerCase()) {
            case "chrome"   -> CHROME;
            case "firefox"  -> FIREFOX;
            case "edge"     -> EDGE;
            case "safari", "webkit" -> WEBKIT;
            default         -> CHROMIUM;
        };
    }
}
