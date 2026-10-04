package com.portfolio.agentobservability.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AgentLog {

    private final List<AgentLogEntry> entries = new ArrayList<>();

    public void record(String stage, Map<String, Object> fields) {
        entries.add(AgentLogEntry.of(stage, fields));
    }

    public List<AgentLogEntry> entries() {
        return List.copyOf(entries);
    }

    public Set<String> stagesPresent() {
        return entries.stream().map(AgentLogEntry::stage).collect(Collectors.toSet());
    }
}
