package com.portfolio.agentobservability.service;

import com.portfolio.agentobservability.domain.DetectionResult;
import com.portfolio.agentobservability.domain.SystemState;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Flags dangerous side effects by comparing system state before/after an
 * action — but ONLY for fields we already know to watch. This is the
 * Week 6 finding made literal in code: detection works for known-sensitive
 * fields (rateLimitActive), and has zero visibility into anything that
 * isn't represented in SystemState at all. An indirect consequence outside
 * this model is undetectable by construction, not by a bug.
 */
@Service
public class SideEffectDetector {

    public DetectionResult detect(SystemState before, SystemState after) {
        List<String> flagged = new ArrayList<>();

        if (before.rateLimitActive() && !after.rateLimitActive()) {
            flagged.add("rateLimitActive: true -> false");
        }

        return flagged.isEmpty() ? DetectionResult.clean() : new DetectionResult(true, flagged);
    }
}