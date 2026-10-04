package com.portfolio.agentobservability.service;

import com.portfolio.agentobservability.domain.AgentLog;
import com.portfolio.agentobservability.dto.AuditResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Answers the actual investigation question: given the agent's log, which
 * of these "what happened and why" questions can we honestly reconstruct?
 */
@Service
public class ReconstructionAuditService {

    private record Question(String text, String stage) {
    }

    private static final List<Question> QUESTIONS = List.of(
            new Question("What did the agent observe?", "observe"),
            new Question("What tool did it call, and when?", "act"),
            new Question("What was the outcome after each action?", "act"),
            new Question("Did the agent verify the result before continuing?", "verify"),
            new Question("Why did it pick that tool over the alternatives?", null),
            new Question("Did it check the previous outcome before acting again?", null),
            new Question("Was there a side effect worth flagging (e.g. rate limiter disabled)?", null)
    );

    public List<AuditResult> audit(AgentLog log) {
        var stagesPresent = log.stagesPresent();
        return QUESTIONS.stream()
                .map(q -> new AuditResult(
                        q.text(),
                        q.stage() != null && stagesPresent.contains(q.stage())
                ))
                .toList();
    }
}
