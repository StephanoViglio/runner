package com.runner.assinador.domain.port.out;

import com.runner.assinador.domain.model.SignatureRequest;
import com.runner.assinador.domain.model.SignatureResult;
import com.runner.assinador.domain.model.VerificationRequest;
import com.runner.assinador.domain.model.VerificationResult;

public interface SignatureProvider {

    SignatureResult sign(SignatureRequest request);

    VerificationResult verify(VerificationRequest request);
}