package com.example.rc4_des_project;

import java.io.*;

public class RC4 {

    private static int[] initializeKeySchedule(String key) {
        int[] S = new int[256];
        byte[] keyBytes = key.getBytes();

        for (int i = 0; i < 256; i++) {
            S[i] = i;
        }

        int j = 0;
        for (int i = 0; i < 256; i++) {
            j = (j + S[i] + keyBytes[i % keyBytes.length]) % 256;
            int temp = S[i];
            S[i] = S[j];
            S[j] = temp;
        }

        return S;
    }

    private static byte[] applyRC4(byte[] data, int[] S) {
        int i = 0, j = 0;
        byte[] result = new byte[data.length];

        for (int k = 0; k < data.length; k++) {
            i = (i + 1) % 256;
            j = (j + S[i]) % 256;


            int temp = S[i];
            S[i] = S[j];
            S[j] = temp;

            int t = (S[i] + S[j]) % 256;
            result[k] = (byte) (data[k] ^ S[t]);
        }

        return result;
    }

    public static void processFile(File inputFile, String key, boolean encrypt) throws IOException {
        File outputFile = new File(inputFile.getParent(), (encrypt ? "encrypted_" : "decrypted_") + inputFile.getName());
        int[] S = initializeKeySchedule(key); // Initialize the key schedule

        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile)) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] output = applyRC4(buffer, S); // Apply RC4 to the current buffer
                fos.write(output, 0, bytesRead);
            }
        }
    }

    public static void encryptFile(File inputFile, String key) throws IOException {
        processFile(inputFile, key, true);
    }

    public static void decryptFile(File inputFile, String key) throws IOException {
        processFile(inputFile, key, false);
    }
}
