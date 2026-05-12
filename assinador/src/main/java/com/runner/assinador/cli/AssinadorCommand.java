package com.runner.assinador.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "assinador",
        description = "Assinador digital FHIR JAdES/JWS — SES-GO",
        mixinStandardHelpOptions = true,
        subcommands = {
                SignCommand.class,
                ValidateCommand.class
        }
)
public class AssinadorCommand implements Runnable {

    @Override
    public void run() {
        new CommandLine(this).usage(System.out);
    }
}