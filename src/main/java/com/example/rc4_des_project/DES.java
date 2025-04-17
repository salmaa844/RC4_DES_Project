package com.example.rc4_des_project;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class DES {

    private static final int BLOCK_SIZE = 8;

    public static byte[] generateKeyFromPassword(String password) {
        byte[] key = new byte[BLOCK_SIZE];
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);

        for (int i = 0; i < BLOCK_SIZE; i++) {
            key[i] = passwordBytes[i % passwordBytes.length];
        }
        return key;
    }

    public static byte[] encryptBlock(byte[] data, byte[] key) {
        byte[] encrypted = new byte[BLOCK_SIZE];

        for (int i = 0; i < BLOCK_SIZE; i++) {
            encrypted[i] = (byte) (data[i] ^ key[i]); // XOR with key
        }

        return encrypted;
    }

    public static byte[] decryptBlock(byte[] data, byte[] key) {
        return encryptBlock(data, key); // XOR is its own inverse
    }

    public static String encryptPassword(String password, String keyPassword) {
        try {
            byte[] key = generateKeyFromPassword(keyPassword);
            byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);

            int paddedLength = ((passwordBytes.length + BLOCK_SIZE - 1) / BLOCK_SIZE) * BLOCK_SIZE;
            byte[] paddedData = new byte[paddedLength];
            System.arraycopy(passwordBytes, 0, paddedData, 0, passwordBytes.length);

            byte[] encryptedData = new byte[paddedData.length];
            for (int i = 0; i < paddedData.length; i += BLOCK_SIZE) {
                byte[] block = new byte[BLOCK_SIZE];
                System.arraycopy(paddedData, i, block, 0, BLOCK_SIZE);
                byte[] encryptedBlock = encryptBlock(block, key);
                System.arraycopy(encryptedBlock, 0, encryptedData, i, BLOCK_SIZE);
            }

            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            throw new RuntimeException("Error during encryption", e);
        }
    }

    public static String decryptPassword(String encryptedPassword, String keyPassword) {
        try {
            byte[] key = generateKeyFromPassword(keyPassword);
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedPassword);

            byte[] decryptedData = new byte[encryptedBytes.length];
            for (int i = 0; i < encryptedBytes.length; i += BLOCK_SIZE) {
                byte[] block = new byte[BLOCK_SIZE];
                System.arraycopy(encryptedBytes, i, block, 0, BLOCK_SIZE);
                byte[] decryptedBlock = decryptBlock(block, key);
                System.arraycopy(decryptedBlock, 0, decryptedData, i, BLOCK_SIZE);
            }

            int end = decryptedData.length;
            while (end > 0 && decryptedData[end - 1] == 0) {
                end--;
            }

            return new String(decryptedData, 0, end, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error during decryption", e);
        }
    }
}
