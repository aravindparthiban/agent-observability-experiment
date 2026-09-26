package com.portfolio.agentobservability.domain;

/**
 * Stands in for real production metrics (CPU, latency, error rate).
 * rateLimitActive is the field nobody thinks to watch — it's the hidden
 * side effect that surfaces two hours later in the Week 3 post's incident.
 */
public record SystemState(
        double errorRate,
        double latencyMs,
        boolean rateLimitActive
) {
}
