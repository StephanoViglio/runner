package com.runner.assinador.cli;

import com.runner.assinador.dto.outcome.OperationOutcomeDTO;
import com.runner.assinador.dto.request.SignRequestDTO;
import com.runner.assinador.dto.response.SignResponseDTO;
import com.runner.assinador.exception.SignatureException;
import com.runner.assinador.factory.OperationOutcomeFactory;
import com.runner.assinador.service.SignatureService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;

@Command(
        name = "sign",
        description = "Cria uma assinatura digital JAdES/JWS a partir de um arquivo JSON.",
        mixinStandardHelpOptions = true
)
public class SignCommand implements Runnable {

    @Option(
            names = {"-i", "--input"},
            description = "Caminho para o arquivo JSON de entrada (SignRequestDTO).",
            required = true
    )
    private File inputFile;

    private final SignatureService signatureService;
    private final CliOutputFormatter outputFormatter;
    private final CliInputValidator inputValidator;
    private final CliFileParser fileParser;

    public SignCommand(SignatureService signatureService,
                       CliOutputFormatter outputFormatter,
                       CliInputValidator inputValidator,
                       CliFileParser fileParser) {
        this.signatureService = signatureService;
        this.outputFormatter  = outputFormatter;
        this.inputValidator   = inputValidator;
        this.fileParser       = fileParser;
    }

    @Override
    public void run() {
        SignRequestDTO request = fileParser.parse(inputFile, SignRequestDTO.class);

        try {
            inputValidator.validate(request);
            SignResponseDTO response = signatureService.sign(request);
            outputFormatter.printSuccess(response);
        } catch (IllegalArgumentException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        } catch (SignatureException ex) {
            OperationOutcomeDTO outcome = OperationOutcomeFactory.of(
                    ex.getOutcomeCode(),
                    ex.getDiagnostics());
            outputFormatter.printError(outcome);
            System.exit(1);
        } catch (Exception ex) {
            System.err.println("Erro inesperado: " + ex.getMessage());
            System.exit(2);
        }
    }
}