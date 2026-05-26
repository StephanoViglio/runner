package com.runner.assinador.adapter.shared.outcome;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CodeableConcept {
    private List<Coding> coding;
    private String text;
}