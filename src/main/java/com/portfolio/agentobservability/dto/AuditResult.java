package com.portfolio.agentobservability.dto;

public record AuditResult(
        String question,
        boolean reconstructable
) {
}
