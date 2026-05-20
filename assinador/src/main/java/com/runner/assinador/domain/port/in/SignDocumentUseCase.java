package com.runner.assinador.domain.port.in;

import com.runner.assinador.application.command.SignDocumentCommand;
import com.runner.assinador.domain.model.SignatureResult;

public interface SignDocumentUseCase {

    SignatureResult execute(SignDocumentCommand command);
}