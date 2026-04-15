package com.runner.assinador.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VerifyRequestDTO {

    private String signatureBase64;

    private Long referenceTimestamp;

    private String policyReferenceURI;

}