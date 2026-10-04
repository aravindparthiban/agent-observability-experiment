package com.portfolio.agentobservability.service;

import com.portfolio.agentobservability.domain.DetectionResult;
import com.portfolio.agentobservability.domain.SystemState;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SideEffectDetectorTest {

    private final SideEffectDetector detector = new SideEffectDetector();

    @Test
    void catchesKnownSensitiveFieldChange() {
        SystemState before = new SystemState(0.15, 650, true);
        SystemState after = new SystemState(0.03, 350, false); // rate limiter disabled

        DetectionResult result = detector.detect(before, after);

        assertThat(result.flagged()).isTrue();
        assertThat(result.flaggedFields()).contains("rateLimitActive: true -> false");
    }

    @Test
    void missesConsequencesOutsideTheTrackedModel() {
        // This test documents the Week 6 finding directly: SystemState has
        // no field for "downstream queue depth," "cache staleness," or any
        // other indirect consequence — so the detector can't flag what it
        // was never told to look at. Nothing to assert on the detector
        // itself here; the absence of a field IS the finding.
        SystemState before = new SystemState(0.15, 650, true);
        SystemState after = new SystemState(0.15, 650, true); // looks identical

        DetectionResult result = detector.detect(before, after);

        assertThat(result.flagged()).isFalse(); // correctly silent — and that's the limitation, not a bug
    }
}