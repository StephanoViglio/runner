package com.runner.assinador.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class SignatureDataUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private SignatureDataUtils() {}

    public static String base64Padrao(String json) {
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    public static String base64Url(String json) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    public static String base64InvalidoNaoDecodificavel() {
        return "ISSO!!@@##NAO%%EH^^BASE64";
    }

    public static String signatureDataComJsonMalformado() {
        return base64Padrao("{isso nao eh json valido");
    }

    public static String signatureDataSemPayload() {
        return base64Padrao("{\"signatures\":[{\"protected\":\"abc\",\"signature\":\"sig\"}]}");
    }

    public static String signatureDataSemSignaturesArray() {
        return base64Padrao("{\"payload\":\"data\"}");
    }

    public static String signatureDataComSignaturesArrayVazio() {
        return base64Padrao("{\"payload\":\"data\",\"signatures\":[]}");
    }

    public static String signatureDataSemProtectedHeader() {
        return base64Padrao("{\"payload\":\"data\",\"signatures\":[{\"signature\":\"sig\"}]}");
    }

    public static String signatureDataSemSignatureField() {
        return base64Padrao("{\"payload\":\"data\",\"signatures\":[{\"protected\":\"abc\"}]}");
    }

    public static String signatureDataComProtectedHeaderNaoBase64Url() {
        return base64Padrao("{\"payload\":\"data\","
                + "\"signatures\":[{\"protected\":\"!!!NAO@@@EH###BASE64URL\","
                + "\"signature\":\"sig\"}]}");
    }

    public static String signatureDataComProtectedHeaderSemAlg() {
        String protectedHeader = base64Url("{\"x5c\":[\"cert\"],\"sigPId\":{\"id\":\"policy\"}}");
        return montarJws(protectedHeader, "data", "sig");
    }

    public static String signatureDataComAlgNaoSuportado() {
        String protectedHeader = base64Url(
                "{\"alg\":\"HS256\",\"x5c\":[\"cert\"],\"sigPId\":{\"id\":\"policy\"}}");
        return montarJws(protectedHeader, "data", "sig");
    }

    public static String signatureDataComProtectedHeaderSemX5c() {
        String protectedHeader = base64Url(
                "{\"alg\":\"RS256\",\"sigPId\":{\"id\":\"policy\"}}");
        return montarJws(protectedHeader, "data", "sig");
    }

    public static String signatureDataComProtectedHeaderX5cVazio() {
        String protectedHeader = base64Url(
                "{\"alg\":\"RS256\",\"x5c\":[],\"sigPId\":{\"id\":\"policy\"}}");
        return montarJws(protectedHeader, "data", "sig");
    }

    public static String signatureDataComProtectedHeaderSemSigPId() {
        String protectedHeader = base64Url(
                "{\"alg\":\"RS256\",\"x5c\":[\"cert\"]}");
        return montarJws(protectedHeader, "data", "sig");
    }

    public static String signatureDataComProtectedHeaderSemSigPIdId() {
        String protectedHeader = base64Url(
                "{\"alg\":\"RS256\",\"x5c\":[\"cert\"],\"sigPId\":{}}");
        return montarJws(protectedHeader, "data", "sig");
    }

    public static String signatureDataValido() {
        String protectedHeader = base64Url(
                "{\"alg\":\"RS256\",\"x5c\":[\"cert\"],\"sigPId\":{\"id\":\"policy\"}}");
        return montarJws(protectedHeader, "qualquer-payload", "sig");
    }

    public static String signatureDataComPayloadAlterado(String payloadFalso) {
        String protectedHeader = base64Url(
                "{\"alg\":\"RS256\",\"x5c\":[\"cert\"],\"sigPId\":{\"id\":\"policy\"}}");
        return montarJws(protectedHeader, payloadFalso, "sig");
    }

    private static String montarJws(String protectedHeaderB64Url, String payload, String signature) {
        try {
            ObjectNode root = OBJECT_MAPPER.createObjectNode();
            root.put("payload", payload);
            ArrayNode signatures = root.putArray("signatures");
            ObjectNode sig = signatures.addObject();
            sig.put("protected", protectedHeaderB64Url);
            sig.put("signature", signature);
            return base64Padrao(OBJECT_MAPPER.writeValueAsString(root));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}