package org.kimprun.batch.common.ratelimit;

/**
 * Rate Limit 결과를 담는 클래스
 */
public class RateLimitResult {
    private final boolean allowed;
    private final long remainingRequests;
    private final int limit;
    private final int windowSeconds;

    public RateLimitResult(boolean allowed, long remainingRequests, int limit, int windowSeconds) {
        this.allowed = allowed;
        this.remainingRequests = remainingRequests;
        this.limit = limit;
        this.windowSeconds = windowSeconds;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public long getRemainingRequests() {
        return remainingRequests;
    }

    public int getLimit() {
        return limit;
    }

    public int getWindowSeconds() {
        return windowSeconds;
    }

    @Override
    public String toString() {
        return String.format("RateLimit{allowed=%s, remaining=%d/%d, window=%ds}",
            allowed, remainingRequests, limit, windowSeconds);
    }
}
