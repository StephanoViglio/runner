package com.runner.assinador.service;

import com.runner.assinador.dto.request.SignRequestDTO;
import com.runner.assinador.dto.request.VerifyRequestDTO;
import com.runner.assinador.dto.response.SignResponseDTO;
import com.runner.assinador.dto.response.VerifyResponseDTO;

public interface SignatureService {

    SignResponseDTO sign(SignRequestDTO request);

    VerifyResponseDTO verify(VerifyRequestDTO request);
}