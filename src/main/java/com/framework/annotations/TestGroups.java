package com.framework.annotations;

/**
 * String constants for TestNG {@code groups} attributes.
 *
 * <pre>
 *   \@Test(groups = {TestGroups.SMOKE, TestGroups.REGRESSION})
 *   public void loginTest() { ... }
 * </pre>
 */
public final class TestGroups {

    private TestGroups() {}

    public static final String SMOKE      = "smoke";
    public static final String SANITY     = "sanity";
    public static final String REGRESSION = "regression";
    public static final String API        = "api";
    public static final String UI         = "ui";
    public static final String DATABASE   = "database";
    public static final String SLOW       = "slow";
    public static final String FLAKY      = "flaky";
}
