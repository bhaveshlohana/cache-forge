package com.bhavesh.learn.cacheforge.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TimedValueTest {

    @Test
    void shouldStoreValueAndExpiry() {
        TimedValue<String> timedValue = new TimedValue<>("Hello", 5000);

        assertEquals("Hello", timedValue.getValue());
        assertTrue(timedValue.getExpireAt() > System.currentTimeMillis());
    }

    @Test
    void shouldNotBeExpiredImmediately() {
        TimedValue<String> timedValue = new TimedValue<>("Hello", 10000);

        assertFalse(timedValue.isExpired());
    }

    @Test
    void shouldBeExpiredAfterTTL() throws InterruptedException {
        TimedValue<String> timedValue = new TimedValue<>("Hello", 50); // 50ms TTL

        Thread.sleep(100); // Wait for expiry

        assertTrue(timedValue.isExpired());
    }

    @Test
    void shouldWorkWithNullValue() {
        TimedValue<String> timedValue = new TimedValue<>(null, 5000);

        assertNull(timedValue.getValue());
        assertFalse(timedValue.isExpired());
    }

    @Test
    void shouldReturnCorrectToString() {
        TimedValue<String> timedValue = new TimedValue<>("Test", 5000);

        String result = timedValue.toString();
        assertTrue(result.contains("value=Test"));
        assertTrue(result.contains("expireAt="));
    }

    @Test
    void shouldWorkWithVeryLargeTTL() {
        TimedValue<Integer> timedValue = new TimedValue<>(42, Long.MAX_VALUE / 2);

        assertEquals(42, timedValue.getValue());
        // May overflow, but should not throw
    }

    @Test
    void shouldWorkWithMinimalTTL() {
        TimedValue<String> timedValue = new TimedValue<>("fast", 1);

        assertEquals("fast", timedValue.getValue());
        // Very short TTL - may or may not be expired depending on timing
    }
}
