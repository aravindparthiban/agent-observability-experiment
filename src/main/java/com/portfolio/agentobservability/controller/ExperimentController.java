package com.portfolio.agentobservability.controller;

import com.portfolio.agentobservability.domain.AgentLog;
import com.portfolio.agentobservability.dto.AuditResult;
import com.portfolio.agentobservability.dto.ExperimentRunResponse;
import com.portfolio.agentobservability.service.IncidentSimulatorService;
import com.portfolio.agentobservability.service.ReconstructionAuditService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ExperimentController {

    private final IncidentSimulatorService simulator;
    private final ReconstructionAuditService auditService;

    public ExperimentController(IncidentSimulatorService simulator, ReconstructionAuditService auditService) {
        this.simulator = simulator;
        this.auditService = auditService;
    }

    @PostMapping("/api/v1/experiments/agent-observability/run")
    public ExperimentRunResponse run(@RequestParam(defaultValue = "3") int maxSteps) {
        AgentLog log = simulator.runIncident(maxSteps);
        List<AuditResult> audit = auditService.audit(log);
        return new ExperimentRunResponse(log.entries(), audit);
    }
}
