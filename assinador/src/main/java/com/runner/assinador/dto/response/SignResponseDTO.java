package com.runner.assinador.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignResponseDTO {

    private List<SignatureCodingDTO> type;

    private String when;

    private SignatureWhoDTO who;

    private String targetFormat;

    private String sigFormat;

    private String data;
}