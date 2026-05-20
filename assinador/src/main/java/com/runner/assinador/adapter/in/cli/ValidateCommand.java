package com.runner.assinador.adapter.in.cli;

import com.runner.assinador.adapter.in.cli.input.VerifyInput;
import com.runner.assinador.adapter.in.cli.mapper.CliSignatureMapper;
import com.runner.assinador.adapter.shared.OperationOutcomeCode;
import com.runner.assinador.adapter.shared.factory.OperationOutcomeFactory;
import com.runner.assinador.application.command.VerifySignatureCommand;
import com.runner.assinador.domain.exception.SignatureException;
import com.runner.assinador.domain.model.VerificationResult;
import com.runner.assinador.domain.port.in.VerifySignatureUseCase;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;

@Command(
        name = "validate",
        description = "Valida uma assinatura digital JAdES/JWS a partir de um arquivo JSON.",
        mixinStandardHelpOptions = true
)
public class ValidateCommand implements Runnable {

    @Option(names = {"-i", "--input"}, description = "Caminho para o arquivo JSON de entrada.", required = true)
    private File inputFile;

    private final VerifySignatureUseCase verifySignatureUseCase;
    private final CliOutputFormatter outputFormatter;
    private final CliFileParser fileParser;
    private final CliSignatureMapper mapper;

    public ValidateCommand(VerifySignatureUseCase verifySignatureUseCase,
                           CliOutputFormatter outputFormatter,
                           CliFileParser fileParser,
                           CliSignatureMapper mapper) {
        this.verifySignatureUseCase = verifySignatureUseCase;
        this.outputFormatter        = outputFormatter;
        this.fileParser             = fileParser;
        this.mapper                 = mapper;
    }

    @Override
    public void run() {
        try {
            VerifyInput input             = fileParser.parse(inputFile, VerifyInput.class);
            VerifySignatureCommand command = mapper.toVerifyCommand(input);
            VerificationResult result      = verifySignatureUseCase.execute(command);
            outputFormatter.printSuccess(result);
        } catch (IllegalArgumentException ex) {
            System.err.println("Entrada inválida: " + ex.getMessage());
            System.exit(1);
        } catch (SignatureException ex) {
            OperationOutcomeCode code = OperationOutcomeCode.fromCode(ex.getErrorCode().getCode());
            outputFormatter.printError(OperationOutcomeFactory.of(code, ex.getDiagnostics()));
            System.exit(1);
        } catch (Exception ex) {
            System.err.println("Erro inesperado: " + ex.getMessage());
            System.exit(2);
        }
    }
}