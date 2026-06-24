package com.runner.assinador.infrastructure.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Provider;
import java.security.Security;

@Configuration
@ConditionalOnProperty(prefix = "assinador.signature-provider", name = "type", havingValue = "pkcs11")
public class Pkcs11Config {

    @Value("${assinador.pkcs11.config-path}")
    private String configPath;

    @Bean
    public Provider pkcs11Provider() {
        Provider template = Security.getProvider("SunPKCS11");
        if (template == null) {
            throw new IllegalStateException(
                    "Provedor SunPKCS11 indisponível nesta JVM. Verifique a versão do JDK utilizada.");
        }

        Provider configured = template.configure(configPath);
        Security.addProvider(configured);
        return configured;
    }
}
