package com.runner.assinador.presentation.in.cli.input;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class BundleInput {

    private final List<ResourceEntryInput> entry;

    @JsonCreator
    public BundleInput(@JsonProperty("entry") List<ResourceEntryInput> entry) {
        this.entry = entry;
    }

    public List<ResourceEntryInput> getEntry() { return entry; }
}