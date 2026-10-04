package com.portfolio.agentobservability.domain;

import java.util.List;

public record DetectionResult(
        boolean flagged,
        List<String> flaggedFields
) {
    public static DetectionResult clean() {
        return new DetectionResult(false, List.of());
    }
}