package com.portfolio.agentobservability.service;

import com.portfolio.agentobservability.domain.AgentLog;
import com.portfolio.agentobservability.domain.SystemState;
import com.portfolio.agentobservability.domain.Tool;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simulates a rule-based incident-response agent — deliberately NOT an LLM
 * agent, matching this series' "understand the system before reaching for
 * ML" stance.
 *
 * The logging in this class is intentionally a MIX of complete and
 * incomplete, on purpose — see the inline notes. This isn't sloppy logging;
 * it's the experiment. The point is to find out, after the fact, which
 * questions about "what did the agent do and why" survive in the trail,
 * and which quietly disappear.
 */
@Service
public class IncidentSimulatorService {

    public AgentLog runIncident(int maxSteps) {
        AgentLog log = new AgentLog();
        SystemState state = observe(log);

        for (int i = 0; i < maxSteps; i++) {
            Tool tool = decide(state, log);
            SystemState next = act(tool, log);
            boolean resolved = verify(state, next, log);
            state = next;
            if (resolved && state.errorRate() < 0.05) {
                break;
            }
        }
        return log;
    }

    private SystemState observe(AgentLog log) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        SystemState state = new SystemState(
                round(rnd.nextDouble(0.05, 0.25)),
                round(rnd.nextDouble(400, 900)),
                true
        );
        // GOOD: exactly what was observed is logged.
        log.record("observe", Map.of(
                "errorRate", state.errorRate(),
                "latencyMs", state.latencyMs()
        ));
        return state;
    }

    private Tool decide(SystemState state, AgentLog log) {
        Tool chosen;
        if (state.errorRate() > 0.2) {
            chosen = Tool.RESTART_SERVICE;
        } else if (state.latencyMs() > 700) {
            chosen = Tool.SCALE_INSTANCES;
        } else {
            chosen = Tool.CHANGE_CONFIG;
        }
        // INCOMPLETE ON PURPOSE: the alternatives considered, and why they
        // were rejected, are never captured — only the final choice is.
        log.record("decide", Map.of("chosenTool", chosen));
        return chosen;
    }

    private SystemState act(Tool tool, AgentLog log) {
        SystemState next = switch (tool) {
            // Hidden side effect: silently disables the rate limiter.
            // This is the thing that surfaces two hours later.
            case CHANGE_CONFIG -> new SystemState(0.03, 350, false);
            case RESTART_SERVICE -> new SystemState(0.04, 500, true);
            case SCALE_INSTANCES -> new SystemState(0.05, 450, true);
        };

        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("tool", tool);
        fields.put("resultErrorRate", next.errorRate());
        fields.put("resultLatencyMs", next.latencyMs());
        // INCOMPLETE ON PURPOSE: rateLimitActive is never logged here —
        // nobody thought to treat it as a metric worth watching.
        log.record("act", fields);
        return next;
    }

    private boolean verify(SystemState previous, SystemState current, AgentLog log) {
        boolean improved = current.errorRate() < previous.errorRate()
                && current.latencyMs() < previous.latencyMs();
        // INCOMPLETE ON PURPOSE: whether this check happened BEFORE the next
        // decision (vs. the agent barrelling ahead) isn't captured anywhere.
        log.record("verify", Map.of("improved", improved));
        return improved;
    }

    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}
