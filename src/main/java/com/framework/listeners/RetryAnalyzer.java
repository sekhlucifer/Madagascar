package com.framework.listeners;

import com.framework.utils.LogUtils;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import static com.framework.config.ConfigurationManager.config;

/**
 * Retries failed tests up to the configured {@code retry.count} limit.
 *
 * <p>Activate per-test via annotation:
 * <pre>
 *   \@Test(retryAnalyzer = RetryAnalyzer.class)
 *   public void myFlakeyTest() { ... }
 * </pre>
 *
 * Or register globally via a {@link org.testng.IAnnotationTransformer}.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private int attempts = 0;

    @Override
    public boolean retry(ITestResult result) {
        int maxRetries = config().retryCount();
        if (attempts < maxRetries) {
            attempts++;
            LogUtils.warn(String.format(
                "Retrying [%s] — attempt %d/%d",
                result.getMethod().getMethodName(), attempts, maxRetries));
            return true;
        }
        return false;
    }
}
