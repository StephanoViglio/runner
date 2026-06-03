package com.runner.assinador.presentation.in.cli.input;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ProvenanceInput {

    private final List<String> target;

    @JsonCreator
    public ProvenanceInput(@JsonProperty("target") List<String> target) {
        this.target = target;
    }

    public List<String> getTarget() { return target; }
}