package com.bhavesh.learn.cachepurge.domain;

public class TimedValue<V> {
    private final V value;
    private final long expireAt;

    public TimedValue(V value, long ttlMillis) {
        this.value = value;
        this.expireAt = System.currentTimeMillis() + ttlMillis;
    }

    public V getValue() {
        return value;
    }

    public long getExpireAt() {
        return expireAt;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expireAt;
    }

    @Override
    public String toString() {
        return "TimedValue{" +
                "value=" + value +
                ", expireAt=" + expireAt +
                '}';
    }
}
