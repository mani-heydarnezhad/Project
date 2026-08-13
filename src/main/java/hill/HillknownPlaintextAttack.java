package hill;

import util.Alphabet;


public final class HillKnownPlaintextAttack {

    private final Alphabet alphabet;
    private final int blockSize;

    public HillKnownPlaintextAttack(Alphabet alphabet, int blockSize) {
        this.alphabet = alphabet;
        this.blockSize = blockSize;
    }

    
    public int[][] recoverKey(int[][] plaintextBlocks, int[][] ciphertextBlocks) {
        int n = alphabet.size();
        int m = blockSize;

        int[][] xInverse = HillCipher.invertMatrixModN(plaintextBlocks, n);

        int[][] key = new int[m][m];
        for (int row = 0; row < m; row++) {
            for (int col = 0; col < m; col++) {
                long sum = 0;
                for (int k = 0; k < m; k++) {
                    sum += (long) ciphertextBlocks[row][k] * xInverse[k][col];
                }
                key[row][col] = (int) Math.floorMod(sum, n);
            }
        }
        return key;
    }

    
    public int[][][] extractBlockMatrices(String plaintext, String ciphertext) {
        int[] pVec = toVector(plaintext);
        int[] cVec = toVector(ciphertext);
        int m = blockSize;
        int numBlocks = pVec.length / m;
        if (numBlocks < m) {
            throw new IllegalArgumentException(
                    "To recover the " + m + "×" + m + " block key, at least " + m + " independent blocks are required.");
        }
        int[][] X = new int[m][m];
        int[][] Y = new int[m][m];
        for (int b = 0; b < m; b++) {
            for (int row = 0; row < m; row++) {
                X[row][b] = pVec[b * m + row];
                Y[row][b] = cVec[b * m + row];
            }
        }
        return new int[][][]{X, Y};
    }

    private int[] toVector(String text) {
        java.util.List<Integer> list = new java.util.ArrayList<>();
        for (int i = 0; i < text.length(); i++) {
            int idx = alphabet.indexOf(text.charAt(i));
            if (idx >= 0) list.add(idx);
        }
        int[] arr = new int[list.size()];
        for (int i = 0; i < arr.length; i++) arr[i] = list.get(i);
        return arr;
    }
}
