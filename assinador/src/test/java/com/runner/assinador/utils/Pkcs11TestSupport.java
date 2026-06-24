package com.runner.assinador.utils;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;

public final class Pkcs11TestSupport {

    private Pkcs11TestSupport() {}

    public record KeyMaterial(PrivateKey privateKey, X509Certificate certificate) {
        public String certificateBase64Der() {
            try {
                return Base64.getEncoder().encodeToString(certificate.getEncoded());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static KeyMaterial generateRsaKeyMaterial(String commonName) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            return new KeyMaterial(keyPair.getPrivate(), selfSign(keyPair, commonName, "SHA256withRSA"));
        } catch (Exception e) {
            throw new RuntimeException("Falha ao gerar par de chaves RSA de teste", e);
        }
    }

    public static KeyMaterial generateEcKeyMaterial(String commonName) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(256);
            KeyPair keyPair = generator.generateKeyPair();
            return new KeyMaterial(keyPair.getPrivate(), selfSign(keyPair, commonName, "SHA256withECDSA"));
        } catch (Exception e) {
            throw new RuntimeException("Falha ao gerar par de chaves EC de teste", e);
        }
    }

    private static X509Certificate selfSign(KeyPair keyPair, String commonName, String signatureAlgorithm)
            throws Exception {
        X500Name subject = new X500Name("CN=" + commonName);
        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
        Date notBefore = new Date(System.currentTimeMillis() - 60_000L);
        Date notAfter = new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000);

        X509v3CertificateBuilder builder = new X509v3CertificateBuilder(
                subject, serial, notBefore, notAfter, subject,
                org.bouncycastle.asn1.x509.SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded()));

        ContentSigner signer = new JcaContentSignerBuilder(signatureAlgorithm).build(keyPair.getPrivate());
        return new JcaX509CertificateConverter().getCertificate(builder.build(signer));
    }
}
