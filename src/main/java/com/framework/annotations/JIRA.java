package com.framework.annotations;

import java.lang.annotation.*;

/**
 * Links a test method to one or more Jira / Xray issue IDs.
 *
 * <pre>
 *   \@Test(description = "User can log in with valid credentials")
 *   \@JIRA(id = "AUTH-101")
 *   public void loginWithValidCredentials() { ... }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface JIRA {

    /** Primary Jira issue ID (e.g. "AUTH-101"). */
    String id() default "";

    /**
     * Set to {@code true} when the Jira ID is supplied at runtime via a
     * data-provider rather than hard-coded in the annotation.
     */
    boolean dataProvider() default false;

    /**
     * Zero-based index of the Jira ID within the data-provider parameter array.
     * Used only when {@link #dataProvider()} is {@code true}.
     */
    int indexOfJiraInDataProvider() default 0;
}
