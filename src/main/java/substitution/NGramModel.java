package substitution;

import util.Alphabet;

import java.util.HashMap;
import java.util.Map;


public final class NGramModel {

    private final Alphabet alphabet;
    private final int n;                 
    private final int order;             
    private final Map<String, Long> counts = new HashMap<>();
    private long totalCount = 0;

    public NGramModel(Alphabet alphabet, int order) {
        if (order != 2 && order != 3) {
            throw new IllegalArgumentException("Only 2-gram or 3-gram is supported.");
        }
        this.alphabet = alphabet;
        this.order = order;
        this.n = alphabet.size();
    }

    
    public void train(String corpus) {
        StringBuilder cleaned = new StringBuilder();
        for (int i = 0; i < corpus.length(); i++) {
            if (alphabet.indexOf(corpus.charAt(i)) >= 0) {
                cleaned.append(Character.toLowerCase(corpus.charAt(i)));
            }
        }
        String text = cleaned.toString();
        for (int i = 0; i + order <= text.length(); i++) {
            String gram = text.substring(i, i + order);
            counts.merge(gram, 1L, Long::sum);
            totalCount++;
        }
    }

    
    public double logProbability(String gram) {
        long c = counts.getOrDefault(gram, 0L);
        double denomStates = Math.pow(n, order);
        double p = (c + 1.0) / (totalCount + denomStates);
        return Math.log(p);
    }

    public double scoreText(String text) {
        StringBuilder cleaned = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            if (alphabet.indexOf(text.charAt(i)) >= 0) {
                cleaned.append(Character.toLowerCase(text.charAt(i)));
            }
        }
        String t = cleaned.toString();
        double score = 0.0;
        for (int i = 0; i + order <= t.length(); i++) {
            score += logProbability(t.substring(i, i + order));
        }
        return score;
    }

    public int order() {
        return order;
    }
}
