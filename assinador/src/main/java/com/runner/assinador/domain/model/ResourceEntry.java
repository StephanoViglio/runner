package com.runner.assinador.domain.model;

public class ResourceEntry {

    private final String fullUrl;
    private final String resourceJson;

    public ResourceEntry(String fullUrl, String resourceJson) {
        if (fullUrl == null || fullUrl.isBlank())
            throw new IllegalArgumentException("fullUrl é obrigatório");
        if (!fullUrl.matches("^urn:uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"))
            throw new IllegalArgumentException("fullUrl deve seguir o formato urn:uuid:<UUID RFC 4122>");
        if (resourceJson == null || resourceJson.isBlank())
            throw new IllegalArgumentException("resourceJson é obrigatório");

        this.fullUrl = fullUrl;
        this.resourceJson = resourceJson;
    }

    public String getFullUrl() { return fullUrl; }
    public String getResourceJson() { return resourceJson; }
}