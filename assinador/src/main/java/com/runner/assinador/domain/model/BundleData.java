package com.runner.assinador.domain.model;

import java.util.List;

public class BundleData {

    private final List<ResourceEntry> entries;

    public BundleData(List<ResourceEntry> entries) {
        if (entries == null || entries.isEmpty())
            throw new IllegalArgumentException("bundle deve ter ao menos uma entry");

        this.entries = List.copyOf(entries);
    }

    public List<ResourceEntry> getEntries() { return entries; }
}