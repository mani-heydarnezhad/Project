package substitution;

import util.Alphabet;

import java.util.Random;


public final class HillClimbingAttack {

    private final Alphabet alphabet;
    private final NGramModel model;
    private final Random random;

    public HillClimbingAttack(Alphabet alphabet, NGramModel model, long seed) {
        this.alphabet = alphabet;
        this.model = model;
        this.random = new Random(seed);
    }

    public static final class Result {
        public final int[] key;      // key[cipherIdx] = plaintextIdx
        public final double score;
        public final String plaintext;

        Result(int[] key, double score, String plaintext) {
            this.key = key;
            this.score = score;
            this.plaintext = plaintext;
        }
    }

    
    public Result attack(String ciphertext, int[] initialKey, int maxIterationsPerRestart, int restarts) {
        int n = alphabet.size();
        Result globalBest = null;

        for (int r = 0; r < restarts; r++) {
            int[] currentKey = (r == 0 && initialKey != null)
                    ? initialKey.clone()
                    : SubstitutionCipher.randomKey(n, random);

            String currentPlain = decryptWithKey(ciphertext, currentKey);
            double currentScore = model.scoreText(currentPlain);

            int[] bestKey = currentKey.clone();
            double bestScore = currentScore;

            for (int iter = 0; iter < maxIterationsPerRestart; iter++) {
                int i = random.nextInt(n);
                int j = random.nextInt(n);
                if (i == j) continue;

                int[] candidateKey = currentKey.clone();
                int tmp = candidateKey[i];
                candidateKey[i] = candidateKey[j];
                candidateKey[j] = tmp;

                String candidatePlain = decryptWithKey(ciphertext, candidateKey);
                double candidateScore = model.scoreText(candidatePlain);

                if (candidateScore > currentScore) {
                    currentKey = candidateKey;
                    currentScore = candidateScore;
                    if (currentScore > bestScore) {
                        bestKey = currentKey.clone();
                        bestScore = currentScore;
                    }
                }
                // در صورت عدم بهبود، تغییر رد می‌شود (نسخه‌ی ساده بدون شبیه‌سازی تبرید)
            }

            if (globalBest == null || bestScore > globalBest.score) {
                globalBest = new Result(bestKey, bestScore, decryptWithKey(ciphertext, bestKey));
            }
        }
        return globalBest;
    }

    private String decryptWithKey(String ciphertext, int[] key) {
        StringBuilder sb = new StringBuilder(ciphertext.length());
        for (int i = 0; i < ciphertext.length(); i++) {
            char c = ciphertext.charAt(i);
            int idx = alphabet.indexOf(c);
            if (idx < 0) {
                sb.append(c);
                continue;
            }
            int newIdx = key[idx];
            boolean upper = Character.isUpperCase(c);
            sb.append(alphabet.charAt(newIdx, upper));
        }
        return sb.toString();
    }
}
