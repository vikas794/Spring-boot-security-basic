package com.example.security.data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class DataSecurityUtil {

    private final byte[] keyBytes;

    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    // Inject encryption key from properties, provide a default 16-byte key for demo purposes
    public DataSecurityUtil(@Value("${security.data.encryption.key:1234567890123456}") String aesKey) {
        this.keyBytes = aesKey.getBytes();
    }

    public String encryptField(String data) {
        if (data == null) return null;
        try {
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            byte[] encryptedData = cipher.doFinal(data.getBytes("UTF-8"));

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedData.length);
            byteBuffer.put(iv);
            byteBuffer.put(encryptedData);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            throw new RuntimeException("Error while encrypting", e);
        }
    }

    public String decryptField(String encryptedData) {
        if (encryptedData == null) return null;
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedData);

            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            return new String(cipher.doFinal(cipherText), "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Error while decrypting", e);
        }
    }
}
