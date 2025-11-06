package com.se347.apigateway.utils;

import java.security.MessageDigest;
import java.util.Base64;

public final class HashUtil {

    private HashUtil() {}

    public static String base64Sha256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(md.digest(data));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
