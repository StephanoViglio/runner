package com.runner.assinador.presentation.in.cli.input;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ResourceEntryInput {

    private final String fullUrl;
    private final String resourceJson;

    @JsonCreator
    public ResourceEntryInput(
            @JsonProperty("fullUrl") String fullUrl,
            @JsonProperty("resourceJson") String resourceJson) {
        this.fullUrl = fullUrl;
        this.resourceJson = resourceJson;
    }

    public String getFullUrl() { return fullUrl; }
    public String getResourceJson() { return resourceJson; }
}