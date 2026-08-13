package vigenere;

import util.Alphabet;

/**
 * رمز ویژنر (Vigenère Cipher) — تعمیم چندالفبایی رمز سزار.
 * مدل ریاضی (بخش ۴.۱ گزارش):
 *   Ei = (xi + k_{(i mod L)}) mod n
 * که K = k1 k2 ... kL کلید به طول L و n اندازه‌ی الفبا است.
 *
 * کلید به صورت رشته‌ی متنی داده می‌شود و هر حرف آن به عدد در Z_n نگاشت
 * می‌شود (مثلاً برای انگلیسی: 'a' -> 0, 'b' -> 1, ...).
 */
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
            throw new IllegalArgumentException("کلید باید حداقل یک حرف معتبر از الفبا داشته باشد.");
        }
        StringBuilder sb = new StringBuilder(text.length());
        int keyPos = 0; // فقط برای حروف موجود در الفبا پیش می‌رود
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

    /** فضای کلید n^L برای طول کلید مشخص L (بخش ۴.۱ گزارش) */
    public java.math.BigInteger keySpaceSize(int keyLength) {
        return java.math.BigInteger.valueOf(alphabet.size()).pow(keyLength);
    }

    public Alphabet alphabet() {
        return alphabet;
    }
}
