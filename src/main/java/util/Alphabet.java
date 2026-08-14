package util;


public final class Alphabet {

    public static final String ENGLISH = "abcdefghijklmnopqrstuvwxyz";
    // الفبای فارسی بدون همزه/تشدید، ۳۲ حرف رایج
    public static final String PERSIAN =
            "ابپتثجچحخدذرزژسشصضطظعغفقکگلمنوهی" ; // 33 حرف؛ در صورت نیاز دقیقاً به 32 کاهش می‌یابد

    private final String letters;
    private final int n;

    public Alphabet(String letters) {
        this.letters = letters;
        this.n = letters.length();
    }

    public static Alphabet english() {
        return new Alphabet(ENGLISH);
    }

    public static Alphabet persian() {
        return new Alphabet(PERSIAN);
    }

    public int size() {
        return n;
    }

    // آیا این کاراکتر عضو الفبا است (بدون حساسیت به بزرگی/کوچکی حروف لاتین)
    public boolean contains(char c) {
        return indexOf(c) >= 0;
    }

    public int indexOf(char c) {
        char lower = Character.toLowerCase(c);
        return letters.indexOf(lower);
    }

    public char charAt(int index, boolean upperCase) {
        int m = Math.floorMod(index, n);
        char c = letters.charAt(m);
        return upperCase ? Character.toUpperCase(c) : c;
    }

    public String letters() {
        return letters;
    }
}
