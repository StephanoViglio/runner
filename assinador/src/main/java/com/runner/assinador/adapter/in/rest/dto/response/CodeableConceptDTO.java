package com.runner.assinador.adapter.in.rest.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CodeableConceptDTO {
    private List<CodingDTO> coding;
    private String text;
}