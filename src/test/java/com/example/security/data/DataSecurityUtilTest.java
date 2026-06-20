package com.example.security.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataSecurityUtilTest {

    private DataSecurityUtil dataSecurityUtil;
    private final String testKey = "1234567890123456"; // 16 bytes key for AES-128

    @BeforeEach
    void setUp() {
        dataSecurityUtil = new DataSecurityUtil(testKey);
    }

    @Test
    void testEncryptDecryptHappyPath() {
        String originalData = "sensitiveData123";
        String encryptedData = dataSecurityUtil.encryptField(originalData);

        assertNotNull(encryptedData);
        assertNotEquals(originalData, encryptedData);

        String decryptedData = dataSecurityUtil.decryptField(encryptedData);
        assertEquals(originalData, decryptedData);
    }

    @Test
    void testEncryptDecryptNullHandling() {
        assertNull(dataSecurityUtil.encryptField(null));
        assertNull(dataSecurityUtil.decryptField(null));
    }

    @Test
    void testEncryptDecryptEmptyString() {
        String originalData = "";
        String encryptedData = dataSecurityUtil.encryptField(originalData);

        assertNotNull(encryptedData);
        assertNotEquals(originalData, encryptedData);

        String decryptedData = dataSecurityUtil.decryptField(encryptedData);
        assertEquals(originalData, decryptedData);
    }

    @Test
    void testEncryptDecryptSpecialCharacters() {
        String originalData = "¡Hola! ¿Cómo estás? 漢字 😊";
        String encryptedData = dataSecurityUtil.encryptField(originalData);

        assertNotNull(encryptedData);
        assertNotEquals(originalData, encryptedData);

        String decryptedData = dataSecurityUtil.decryptField(encryptedData);
        assertEquals(originalData, decryptedData);
    }

    @Test
    void testDecryptInvalidBase64() {
        String invalidBase64 = "This is not valid Base64!";
        assertThrows(RuntimeException.class, () -> dataSecurityUtil.decryptField(invalidBase64));
    }

    @Test
    void testDecryptTamperedData() {
        String originalData = "sensitiveData123";
        String encryptedData = dataSecurityUtil.encryptField(originalData);

        // Tamper with the encrypted data by changing the last character
        String tamperedData = encryptedData.substring(0, encryptedData.length() - 1) + (encryptedData.endsWith("A") ? "B" : "A");

        assertThrows(RuntimeException.class, () -> dataSecurityUtil.decryptField(tamperedData));
    }
}
