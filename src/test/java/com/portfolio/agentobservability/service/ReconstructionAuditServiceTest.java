package com.portfolio.agentobservability.service;

import com.portfolio.agentobservability.domain.AgentLog;
import com.portfolio.agentobservability.dto.AuditResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ReconstructionAuditServiceTest {

    private final IncidentSimulatorService simulator = new IncidentSimulatorService();
    private final ReconstructionAuditService auditService = new ReconstructionAuditService();

    @Test
    void observedToolCallsAndOutcomesAreReconstructable() {
        AgentLog log = simulator.runIncident(3);
        List<AuditResult> results = auditService.audit(log);

        assertThat(resultFor(results, "What did the agent observe?").reconstructable()).isTrue();
        assertThat(resultFor(results, "What tool did it call, and when?").reconstructable()).isTrue();
        assertThat(resultFor(results, "What was the outcome after each action?").reconstructable()).isTrue();
    }

    @Test
    void reasoningAndSideEffectsAreNotReconstructable() {
        // This is the actual finding of the experiment, expressed as a test:
        // the current logging format cannot answer these questions, no
        // matter how many times the incident is run. If this test ever
        // starts failing, it means the logging was improved — which is
        // exactly the kind of change this experiment is meant to motivate.
        AgentLog log = simulator.runIncident(3);
        List<AuditResult> results = auditService.audit(log);

        assertThat(resultFor(results, "Why did it pick that tool over the alternatives?").reconstructable()).isFalse();
        assertThat(resultFor(results, "Did it check the previous outcome before acting again?").reconstructable()).isFalse();
        assertThat(resultFor(results, "Was there a side effect worth flagging (e.g. rate limiter disabled)?").reconstructable()).isFalse();
    }

    @Test
    void changeConfigSilentlyDisablesTheRateLimiterWithoutLoggingIt() {
        // Directly demonstrates the hidden side effect the whole
        // investigation is about: the log for an "act" entry never
        // mentions rateLimitActive at all.
        AgentLog log = new AgentLog();
        // Force a low-error, low-latency state so decide() picks CHANGE_CONFIG.
        var simulatorInternals = new IncidentSimulatorService();
        AgentLog realLog = simulatorInternals.runIncident(1);

        boolean anyActEntryMentionsRateLimit = realLog.entries().stream()
                .filter(e -> e.stage().equals("act"))
                .anyMatch(e -> e.fields().containsKey("rateLimitActive"));

        assertThat(anyActEntryMentionsRateLimit).isFalse();
    }

    private AuditResult resultFor(List<AuditResult> results, String question) {
        return results.stream()
                .filter(r -> r.question().equals(question))
                .findFirst()
                .orElseThrow();
    }
}
