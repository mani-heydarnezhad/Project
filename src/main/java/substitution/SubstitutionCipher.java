package substitution;

import util.Alphabet;


public final class SubstitutionCipher {

    private final Alphabet alphabet;

    public SubstitutionCipher(Alphabet alphabet) {
        this.alphabet = alphabet;
    }

   
    public void validateKey(int[] key) {
        int n = alphabet.size();
        if (key.length != n) {
            throw new IllegalArgumentException("The key length must be equal to the alphabet size (" + n + ").");
        }
        boolean[] seen = new boolean[n];
        for (int v : key) {
            if (v < 0 || v >= n || seen[v]) {
                throw new IllegalArgumentException("The key is not a valid permutation (duplicate value or out of range).");
            }
            seen[v] = true;
        }
    }

    public String encrypt(String plaintext, int[] key) {
        validateKey(key);
        return applyPermutation(plaintext, key);
    }

    public String decrypt(String ciphertext, int[] key) {
        validateKey(key);
        int[] inverse = invert(key);
        return applyPermutation(ciphertext, inverse);
    }

    private String applyPermutation(String text, int[] permutation) {
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            int idx = alphabet.indexOf(c);
            if (idx < 0) {
                sb.append(c);
                continue;
            }
            int newIdx = permutation[idx];
            boolean upper = Character.isUpperCase(c);
            sb.append(alphabet.charAt(newIdx, upper));
        }
        return sb.toString();
    }

    
    public static int[] invert(int[] permutation) {
        int[] inverse = new int[permutation.length];
        for (int i = 0; i < permutation.length; i++) {
            inverse[permutation[i]] = i;
        }
        return inverse;
    }

    
    public static int[] randomKey(int n, java.util.Random random) {
        int[] key = new int[n];
        for (int i = 0; i < n; i++) key[i] = i;
        for (int i = n - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = key[i];
            key[i] = key[j];
            key[j] = tmp;
        }
        return key;
    }

    public Alphabet alphabet() {
        return alphabet;
    }
}
