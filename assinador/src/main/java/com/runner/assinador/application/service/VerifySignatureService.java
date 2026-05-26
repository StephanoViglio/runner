package com.runner.assinador.application.service;

import com.runner.assinador.domain.exception.DomainErrorCode;
import com.runner.assinador.domain.exception.SignatureException;
import com.runner.assinador.domain.model.VerificationRequest;
import com.runner.assinador.domain.model.VerificationResult;
import com.runner.assinador.domain.port.in.VerifySignatureCommand;
import com.runner.assinador.domain.port.in.VerifySignatureUseCase;
import com.runner.assinador.domain.port.out.SignatureProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerifySignatureService implements VerifySignatureUseCase {

    private static final long TIMESTAMP_TOLERANCE_SECONDS = 300L;

    private final SignatureProvider signatureProvider;

    @Override
    public VerificationResult execute(VerifySignatureCommand command) {
        log.info("VerifySignatureService.execute() — iniciando validações");

        validateTimestampWindow(command.getReferenceTimestamp());

        VerificationRequest request = new VerificationRequest(
                command.getSignatureData(),
                command.getReferenceTimestamp(),
                command.getPolicyUri(),
                command.getBundle(),
                command.getProvenance()
        );

        log.info("Validações concluídas — delegando para SignatureProvider");

        return signatureProvider.verify(request);
    }

    private void validateTimestampWindow(Long referenceTimestamp) {
        long now  = Instant.now().getEpochSecond();
        long diff = Math.abs(referenceTimestamp - now);
        if (diff > TIMESTAMP_TOLERANCE_SECONDS) {
            throw new SignatureException(
                    DomainErrorCode.TIMESTAMP_OUT_OF_TOLERANCE_WINDOW,
                    String.format(
                            "referenceTimestamp (%d) fora da janela de tolerância de ±%d segundos " +
                                    "em relação ao servidor (%d). Diferença: %d segundos.",
                            referenceTimestamp, TIMESTAMP_TOLERANCE_SECONDS, now, diff));
        }
    }
}