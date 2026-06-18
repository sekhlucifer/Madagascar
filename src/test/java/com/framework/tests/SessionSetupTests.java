package com.framework.tests;

import com.framework.browser.BrowserManager;
import com.framework.utils.LogUtils;
import org.testng.annotations.Test;
import java.io.File;

import static com.framework.config.ConfigurationManager.config;

/**
 * Utility test to open the browser pointing to the samarth.patel profile (Profile 4),
 * pause execution to let you manually log in once, and save the cookies to configs/user.json.
 */
public class SessionSetupTests extends BaseTest {

    @Test(description = "Open Chrome, wait for manual login, and save cookies")
    public void captureAndSaveSessionState() {
        LogUtils.info("=== Opening Chrome for Manual Login ===");
        
        // Navigate to the patient lookup portal
        BrowserManager.getPage().navigate(config().baseUrl());
        
        LogUtils.info("=== WAITING FOR MANUAL LOGIN ===");
        LogUtils.info("Please log in manually on the opened Google Chrome window.");
        LogUtils.info("Complete your Microsoft login and MFA on your phone.");
        LogUtils.info("Once you are fully logged in and see the Patient Lookup page, the script will automatically capture cookies and close.");
        
        long startTime = System.currentTimeMillis();
        long timeoutMs = 180000; // 3 minutes to log in and do MFA
        boolean loggedIn = false;
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                String currentUrl = BrowserManager.getPage().url();
                // Check if we are on the patient lookup portal and NOT on the login page anymore
                if (currentUrl.contains("Patient-Lookup") && !currentUrl.contains("login.microsoftonline.com")) {
                    // Check if search bar or header element is visible to confirm we are fully loaded in
                    boolean hasHeader = BrowserManager.getPage().isVisible("//h1[contains(.,'Patient Lookup')] | //h2[contains(.,'Patient Lookup')]");
                    boolean hasSearch = BrowserManager.getPage().isVisible("//input[contains(@id,'search') or contains(@class,'search')]");
                    
                    if (hasHeader || hasSearch) {
                        LogUtils.info("SUCCESS: Login detected! Automatically saving session state...");
                        loggedIn = true;
                        break;
                    }
                }
            } catch (Exception e) {
                LogUtils.warn("Browser window or connection issue during wait loop: " + e.getMessage());
                break;
            }
            
            // Sleep for 2 seconds before checking again
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                break;
            }
        }
        
        if (!loggedIn) {
            LogUtils.warn("Timeout reached or login not completed. We will save whatever session state is currently active.");
        }
        
        String statePath = config().browserStorageStatePath();
        if (statePath == null || statePath.isEmpty()) {
            statePath = "configs/user.json";
        }
        
        File file = new File(statePath);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        // Save the cookies and localStorage
        try {
            BrowserManager.getContext().storageState(
                new com.microsoft.playwright.BrowserContext.StorageStateOptions().setPath(java.nio.file.Paths.get(statePath))
            );
            LogUtils.info("=== Session State Successfully Captured and Saved to: " + file.getAbsolutePath() + " ===");
        } catch (Exception e) {
            LogUtils.error("Failed to capture storage state: " + e.getMessage());
        }
    }
}
