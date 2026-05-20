package com.runner.assinador.domain.model;

import java.util.List;

public class ProvenanceData {

    private final List<String> targets;

    public ProvenanceData(List<String> targets) {
        if (targets == null || targets.isEmpty())
            throw new IllegalArgumentException("provenance deve ter ao menos um target");

        this.targets = List.copyOf(targets);
    }

    public List<String> getTargets() { return targets; }
}