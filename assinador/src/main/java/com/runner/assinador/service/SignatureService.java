package com.runner.assinador.service;

import com.runner.assinador.dto.outcome.OperationOutcomeDTO;
import com.runner.assinador.dto.request.SignRequestDTO;
import com.runner.assinador.dto.request.VerifyRequestDTO;
import com.runner.assinador.dto.response.SignResponseDTO;

public interface SignatureService {

    SignResponseDTO sign(SignRequestDTO request);

    OperationOutcomeDTO verify(VerifyRequestDTO request);
}