package com.runner.assinador.domain.port.out;

import com.runner.assinador.application.command.SignDocumentCommand;
import com.runner.assinador.application.command.VerifySignatureCommand;
import com.runner.assinador.domain.model.SignatureResult;
import com.runner.assinador.domain.model.VerificationResult;

public interface SignatureProvider {

    SignatureResult sign(SignDocumentCommand command);

    VerificationResult verify(VerifySignatureCommand command);
}