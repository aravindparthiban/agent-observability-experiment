# Agent Observability Experiment

Supporting code for the "Engineering Investigation" series:
**can we reconstruct what an AI agent did — and why — from its logs, and
can we automatically catch when its actions have dangerous side effects?**

## What this is

A simulated, rule-based incident-response agent (no real LLM, no real
infrastructure — deliberately, matching this series' "understand the system
before reaching for ML" stance). It runs through:

```
Observe → Decide → Act → Detect → Verify → Record
```

...against mocked production signals, logging each stage. The logging is
**intentionally a mix of complete and incomplete**, and the detection is
**intentionally limited to known fields** — both are the actual experiment,
not a gap to apologize for.

## Week 4 — Reconstruction

Some questions about "what did the agent do and why" reconstruct cleanly
from the log. Some don't, on purpose:

| Question | Reconstructable? |
|---|---|
| What did the agent observe? | ✅ |
| What tool did it call, and when? | ✅ |
| What was the outcome after each action? | ✅ |
| Did it verify the result before continuing? | ✅ |
| Why did it pick that tool over the alternatives? | ❌ never logged |
| Did it check the previous outcome before acting again? | ❌ never logged |
| Was there a side effect worth flagging (e.g. rate limiter disabled)? | ❌ never logged |

One of the simulated actions (`CHANGE_CONFIG`) "resolves" the incident by
silently disabling a rate limiter as a side effect. Nothing in the Week 4
log says so — which is exactly the kind of gap that turns into a confusing
incident two hours later.

`ReconstructionAuditServiceTest` turns this from an anecdote into a
repeatable assertion.

## Week 5 — Record

Extended the execution record to capture context around each action — what
was observed, the decision, the action, the outcome, verification, and next
step — so an engineer can reconstruct the story without guessing what
happened between log lines.

## Week 6 — Detection

Added a `Detect` stage, via `SideEffectDetector`, that compares system state
before/after an action and flags changes to known-sensitive fields —
closing part of the Week 4 gap by catching the rate-limiter side effect
automatically instead of relying on a person to notice it.

**The finding:** detection works for side effects on fields you already
know to watch (a before/after comparison on `rateLimitActive`). It does
**not** work for consequences outside that model — an indirect side effect
the detector was never told to look for is invisible to it, not because of
a bug, but because detection can only check what it's been told to check.

`SideEffectDetectorTest` documents both sides of this directly: one test
proves the known-field case is caught, the other proves an unmodeled
consequence is correctly (and unhelpfully) silent.

## Running it

```bash
mvn spring-boot:run
```

Then open `http://localhost:8080` — a bundled page lets you click **Run
experiment**, which calls the real backend endpoint and renders the
incident timeline, including each step's detection result.

Or call the endpoint directly:

```bash
curl -X POST "http://localhost:8080/api/v1/experiments/agent-observability/run?maxSteps=3"
```

## Running the tests

```bash
mvn test
```

- `ReconstructionAuditServiceTest` — what's reconstructable from the log, and what isn't.
- `SideEffectDetectorTest` — what the detector catches, and what it structurally can't.

## Structure

```
domain/      → SystemState, Tool, AgentLog, AgentLogEntry, DetectionResult
service/     → IncidentSimulatorService (the agent), ReconstructionAuditService (Week 4 audit),
               SideEffectDetector (Week 6 detection)
controller/  → REST endpoint tying it together
resources/static/ → a small page that exercises the endpoint live
```
