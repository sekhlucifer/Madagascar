package com.framework.config;

import org.aeonbits.owner.ConfigCache;

public final class TestDataManager {
    private TestDataManager() {}

    public static TestData testData() {
        return ConfigCache.getOrCreate(TestData.class);
    }
}
