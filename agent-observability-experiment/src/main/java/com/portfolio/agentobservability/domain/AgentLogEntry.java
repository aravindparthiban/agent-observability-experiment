package com.portfolio.agentobservability.domain;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One entry in the agent's log. The "fields" map is deliberately open-ended —
 * different stages log different things, and part of the experiment is
 * noticing what's absent as much as what's present.
 */
public record AgentLogEntry(
        Instant timestamp,
        String stage,
        Map<String, Object> fields
) {
    public static AgentLogEntry of(String stage, Map<String, Object> fields) {
        return new AgentLogEntry(Instant.now(), stage, new LinkedHashMap<>(fields));
    }
}
