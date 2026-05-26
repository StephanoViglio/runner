package com.runner.assinador;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runner.assinador.adapter.in.cli.*;
import com.runner.assinador.adapter.in.cli.mapper.CliSignatureMapper;
import com.runner.assinador.adapter.out.signature.FakeSignatureAdapter;
import com.runner.assinador.application.service.SignDocumentService;
import com.runner.assinador.application.service.VerifySignatureService;
import com.runner.assinador.domain.port.in.SignDocumentUseCase;
import com.runner.assinador.domain.port.in.VerifySignatureUseCase;
import com.runner.assinador.domain.port.out.SignatureProvider;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

public class AssinadorCli {

    public static void main(String[] args) {
        ObjectMapper objectMapper       = new ObjectMapper();
        SignatureProvider provider       = new FakeSignatureAdapter(objectMapper);
        SignDocumentUseCase signUseCase  = new SignDocumentService(provider);
        VerifySignatureUseCase verifyUseCase = new VerifySignatureService(provider);

        CliOutputFormatter formatter    = new CliOutputFormatter(objectMapper);
        CliFileParser fileParser        = new CliFileParser(objectMapper);
        CliSignatureMapper mapper        = new CliSignatureMapper();

        SignCommand signCommand         = new SignCommand(signUseCase, formatter, fileParser, mapper);
        ValidateCommand validateCommand = new ValidateCommand(verifyUseCase, formatter, fileParser, mapper);
        AssinadorCommand rootCommand    = new AssinadorCommand();

        IFactory factory = new IFactory() {
            @Override
            public <K> K create(Class<K> cls) throws Exception {
                if (cls == SignCommand.class)     return cls.cast(signCommand);
                if (cls == ValidateCommand.class) return cls.cast(validateCommand);
                return CommandLine.defaultFactory().create(cls);
            }
        };

        System.exit(new CommandLine(rootCommand, factory).execute(args));
    }
}