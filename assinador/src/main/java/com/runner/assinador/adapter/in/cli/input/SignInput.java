package com.runner.assinador.adapter.in.cli.input;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.runner.assinador.domain.model.CryptographicStrategy;
import com.runner.assinador.domain.model.TimestampStrategy;

import java.util.List;

public class SignInput {

    @JsonProperty("bundle")
    private BundleInput bundle;

    @JsonProperty("provenance")
    private ProvenanceInput provenance;

    @JsonProperty("cryptographicMaterial")
    private CryptographicInput cryptographicMaterial;

    @JsonProperty("certificateChain")
    private List<String> certificateChain;

    @JsonProperty("referenceTimestamp")
    private Long referenceTimestamp;

    @JsonProperty("timestampStrategy")
    private TimestampStrategy timestampStrategy;

    @JsonProperty("policyUri")
    private String policyUri;

    public BundleInput getBundle() { return bundle; }
    public ProvenanceInput getProvenance() { return provenance; }
    public CryptographicInput getCryptographicMaterial() { return cryptographicMaterial; }
    public List<String> getCertificateChain() { return certificateChain; }
    public Long getReferenceTimestamp() { return referenceTimestamp; }
    public TimestampStrategy getTimestampStrategy() { return timestampStrategy; }
    public String getPolicyUri() { return policyUri; }

    public static class BundleInput {
        @JsonProperty("entry")
        private List<ResourceEntryInput> entry;
        public List<ResourceEntryInput> getEntry() { return entry; }
    }

    public static class ResourceEntryInput {
        @JsonProperty("fullUrl")
        private String fullUrl;
        @JsonProperty("resourceJson")
        private String resourceJson;
        public String getFullUrl() { return fullUrl; }
        public String getResourceJson() { return resourceJson; }
    }

    public static class ProvenanceInput {
        @JsonProperty("target")
        private List<String> target;
        public List<String> getTarget() { return target; }
    }

    public static class CryptographicInput {
        @JsonProperty("cryptographicStrategy")
        private CryptographicStrategy cryptographicStrategy;
        @JsonProperty("pin")
        private String pin;
        @JsonProperty("identifier")
        private String identifier;
        @JsonProperty("slotId")
        private Integer slotId;
        @JsonProperty("tokenLabel")
        private String tokenLabel;
        public CryptographicStrategy getCryptographicStrategy() { return cryptographicStrategy; }
        public String getPin() { return pin; }
        public String getIdentifier() { return identifier; }
        public Integer getSlotId() { return slotId; }
        public String getTokenLabel() { return tokenLabel; }
    }
}