package util;

/**
 * نگاشت الفبا به اعداد صحیح (Z_n) برای عملیات پیمانه‌ای رمزهای کلاسیک.
 * دو الفبا پشتیبانی می‌شود: انگلیسی (n = 26) و فارسی (n = 32).
 *
 * این کلاس پیاده‌سازی مستقیم مدل ریاضی بخش ۲ گزارش است:
 *   E(x) = (x + k) mod n
 */
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

    /** اندازه الفبا n (مطابق فرمول |K| = n در گزارش، بخش ۲.۲) */
    public int size() {
        return n;
    }

    /** آیا این کاراکتر عضو الفبا است (بدون حساسیت به بزرگی/کوچکی حروف لاتین) */
    public boolean contains(char c) {
        return indexOf(c) >= 0;
    }

    /** نگاشت حرف -> عدد در Z_n ؛ اگر عضو الفبا نباشد -1 برمی‌گرداند */
    public int indexOf(char c) {
        char lower = Character.toLowerCase(c);
        return letters.indexOf(lower);
    }

    /** نگاشت عدد در Z_n -> حرف؛ حالت حروف بزرگ لاتین اصلی حفظ می‌شود */
    public char charAt(int index, boolean upperCase) {
        int m = Math.floorMod(index, n);
        char c = letters.charAt(m);
        return upperCase ? Character.toUpperCase(c) : c;
    }

    public String letters() {
        return letters;
    }
}
