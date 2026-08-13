package vigenere;

import util.Alphabet;


public final class VigenereCipher {

    private final Alphabet alphabet;

    public VigenereCipher(Alphabet alphabet) {
        this.alphabet = alphabet;
    }

    public String encrypt(String plaintext, String key) {
        return shift(plaintext, key, +1);
    }

    public String decrypt(String ciphertext, String key) {
        return shift(ciphertext, key, -1);
    }

    private String shift(String text, String key, int sign) {
        int[] keyIndices = keyToIndices(key);
        if (keyIndices.length == 0) {
            throw new IllegalArgumentException("The key must contain at least one valid alphabetic character.");
        }
        StringBuilder sb = new StringBuilder(text.length());
        int keyPos = 0; 
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            int idx = alphabet.indexOf(c);
            if (idx < 0) {
                sb.append(c);
                continue;
            }
            int k = keyIndices[keyPos % keyIndices.length];
            int newIdx = Math.floorMod(idx + sign * k, alphabet.size());
            boolean upper = Character.isUpperCase(c);
            sb.append(alphabet.charAt(newIdx, upper));
            keyPos++;
        }
        return sb.toString();
    }

    private int[] keyToIndices(String key) {
        java.util.List<Integer> list = new java.util.ArrayList<>();
        for (int i = 0; i < key.length(); i++) {
            int idx = alphabet.indexOf(key.charAt(i));
            if (idx >= 0) list.add(idx);
        }
        int[] arr = new int[list.size()];
        for (int i = 0; i < arr.length; i++) arr[i] = list.get(i);
        return arr;
    }

    
    public java.math.BigInteger keySpaceSize(int keyLength) {
        return java.math.BigInteger.valueOf(alphabet.size()).pow(keyLength);
    }

    public Alphabet alphabet() {
        return alphabet;
    }
}
