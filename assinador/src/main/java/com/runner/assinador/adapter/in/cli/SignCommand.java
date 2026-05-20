package com.runner.assinador.adapter.in.cli;

import com.runner.assinador.adapter.in.cli.input.SignInput;
import com.runner.assinador.adapter.in.cli.mapper.CliSignatureMapper;
import com.runner.assinador.adapter.shared.factory.OperationOutcomeFactory;
import com.runner.assinador.adapter.shared.OperationOutcomeCode;
import com.runner.assinador.application.command.SignDocumentCommand;
import com.runner.assinador.domain.exception.SignatureException;
import com.runner.assinador.domain.model.SignatureResult;
import com.runner.assinador.domain.port.in.SignDocumentUseCase;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;

@Command(
        name = "sign",
        description = "Cria uma assinatura digital JAdES/JWS a partir de um arquivo JSON.",
        mixinStandardHelpOptions = true
)
public class SignCommand implements Runnable {

    @Option(names = {"-i", "--input"}, description = "Caminho para o arquivo JSON de entrada.", required = true)
    private File inputFile;

    private final SignDocumentUseCase signDocumentUseCase;
    private final CliOutputFormatter outputFormatter;
    private final CliFileParser fileParser;
    private final CliSignatureMapper mapper;

    public SignCommand(SignDocumentUseCase signDocumentUseCase,
                       CliOutputFormatter outputFormatter,
                       CliFileParser fileParser,
                       CliSignatureMapper mapper) {
        this.signDocumentUseCase = signDocumentUseCase;
        this.outputFormatter     = outputFormatter;
        this.fileParser          = fileParser;
        this.mapper              = mapper;
    }

    @Override
    public void run() {
        try {
            SignInput input            = fileParser.parse(inputFile, SignInput.class);
            SignDocumentCommand command = mapper.toSignCommand(input);
            SignatureResult result      = signDocumentUseCase.execute(command);
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