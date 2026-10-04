package com.portfolio.agentobservability.dto;

import com.portfolio.agentobservability.domain.AgentLogEntry;

import java.util.List;

public record ExperimentRunResponse(
        List<AgentLogEntry> log,
        List<AuditResult> audit
) {
}
