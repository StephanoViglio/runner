package com.runner.assinador;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runner.assinador.cli.*;
import com.runner.assinador.service.SignatureService;
import com.runner.assinador.service.impl.FakeSignatureService;
import com.runner.assinador.validation.SignRequestValidator;
import com.runner.assinador.validation.VerifyRequestValidator;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

public class AssinadorCli {

    public static void main(String[] args) {
        ObjectMapper objectMapper              = new ObjectMapper();
        SignRequestValidator signValidator      = new SignRequestValidator(objectMapper);
        VerifyRequestValidator verifyValidator  = new VerifyRequestValidator(objectMapper);
        SignatureService service                = new FakeSignatureService(signValidator, verifyValidator);
        CliOutputFormatter formatter            = new CliOutputFormatter(objectMapper);
        CliInputValidator inputValidator        = new CliInputValidator();
        CliFileParser fileParser                = new CliFileParser(objectMapper);

        SignCommand signCommand         = new SignCommand(service, formatter, inputValidator, fileParser);
        ValidateCommand validateCommand = new ValidateCommand(service, formatter, inputValidator, fileParser);
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