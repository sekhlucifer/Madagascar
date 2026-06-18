package com.framework.listeners;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Applies {@link RetryAnalyzer} to every {@code @Test} method globally,
 * so individual tests don't need to declare it explicitly.
 *
 * <p>Register in {@code testng.xml}:
 * <pre>
 *   &lt;listeners&gt;
 *     &lt;listener class-name="com.framework.listeners.RetryTransformer"/&gt;
 *   &lt;/listeners&gt;
 * </pre>
 */
public class RetryTransformer implements IAnnotationTransformer {

    @Override
    public void transform(ITestAnnotation annotation,
                          Class testClass,
                          Constructor testConstructor,
                          Method testMethod) {
        annotation.setRetryAnalyzer(RetryAnalyzer.class);
    }
}
