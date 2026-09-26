# Agent Observability Experiment

Supporting code for the "Engineering Investigation" series (Week 3–4):
**can we reconstruct what an AI agent did — and why — from its logs?**

## What this is

A simulated, rule-based incident-response agent (no real LLM, no real
infrastructure — deliberately, matching this series' "understand the system
before reaching for ML" stance). It runs through:

```
observe → decide → act → verify
```

...against mocked production signals, and logs each stage. The logging is
**intentionally a mix of complete and incomplete** — that's the actual
experiment. Some questions about "what did the agent do and why" reconstruct
cleanly from the log. Some don't, on purpose:

| Question | Reconstructable? |
|---|---|
| What did the agent observe? | ✅ |
| What tool did it call, and when? | ✅ |
| What was the outcome after each action? | ✅ |
| Did it verify the result before continuing? | ✅ |
| Why did it pick that tool over the alternatives? | ❌ never logged |
| Did it check the previous outcome before acting again? | ❌ never logged |
| Was there a side effect worth flagging (e.g. rate limiter disabled)? | ❌ never logged |

The last three are the point of the post: an agent that "resolves" an
incident by calling `change_config` silently disables a rate limiter as a
side effect. Nothing in the log says so — which is exactly the kind of gap
that turns into a confusing incident two hours later.

`ReconstructionAuditServiceTest` turns this from an anecdote into a
repeatable assertion — the "not reconstructable" findings are unit-tested,
not just observed once.

## Running it

```bash
mvn spring-boot:run
```

Then open `http://localhost:8080` — a bundled page lets you click **Run
experiment**, which calls the real backend endpoint and renders the
incident timeline and audit result.

Or call the endpoint directly:

```bash
curl -X POST "http://localhost:8080/api/v1/experiments/agent-observability/run?maxSteps=3"
```

## Running the tests

```bash
mvn test
```

`ReconstructionAuditServiceTest` verifies both what's reconstructable and
what deliberately isn't — including a direct assertion that no `act` log
entry ever mentions `rateLimitActive`, which is the hidden side effect the
whole investigation is about.

## Structure

```
domain/      → SystemState, Tool, AgentLog, AgentLogEntry
service/     → IncidentSimulatorService (the agent), ReconstructionAuditService (the audit)
controller/  → REST endpoint tying it together
resources/static/ → a small page that exercises the endpoint live
```
