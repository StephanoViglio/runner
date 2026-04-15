package com.runner.assinador.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SignRequestDTO {

    private BundleDTO bundle;

    private CryptographicDTO cryptographicMaterial;

    private List<String> certificateChain;

    private Long referenceTimestamp;

    private String timestampStrategy;

    private String policyUri;
}