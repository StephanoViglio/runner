package com.runner.assinador.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyResponseDTO {

    private boolean valid;

    private Long signingTimestamp;

    private String algorithm;

    private String policyUri;
}