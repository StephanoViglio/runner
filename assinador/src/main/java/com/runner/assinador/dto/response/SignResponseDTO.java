package com.runner.assinador.dto.response;

import com.runner.assinador.utils.TimestampStrategy;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignResponseDTO {

    private String signatureJson;

    private String algorithm;

    private TimestampStrategy strategyUsed;

    private Long signingTimestamp;
}