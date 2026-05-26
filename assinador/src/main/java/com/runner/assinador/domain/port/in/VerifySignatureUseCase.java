package com.runner.assinador.domain.port.in;

import com.runner.assinador.domain.model.VerificationResult;

public interface VerifySignatureUseCase {

    VerificationResult execute(VerifySignatureCommand command);
}