# پروژه پایانی ریاضیات گسسته — موضوع ۹: رمزنگاری و رمزشکنی کلاسیک با تحلیل فراوانی

## اجرا
```
mvn compile
mvn exec:java -Dexec.mainClass="caesar.CaesarDemo"
mvn test
```

## ساختار
- `src/main/java/util/Alphabet.java` — نگاشت الفبا به Z_n (فارسی/انگلیسی)
- `src/main/java/util/FrequencyTable.java` — استخراج و جدول‌های فراوانی مرجع
- `src/main/java/util/KeySpaceCalculator.java` — محاسبه اندازه فضای کلید هر رمز
- `src/main/java/caesar/CaesarCipher.java` — رمزگذاری/رمزگشایی سزار
- `src/main/java/caesar/CaesarCryptanalyzer.java` — شکست با brute-force + χ²
- `src/main/java/caesar/CaesarDemo.java` — اجرای نمایشی
- `src/test/java/caesar/CaesarCipherTest.java` — تست‌های واحد (JUnit 5)
- `data/corpus_english_sample.txt` — پیکره نمونه برای استخراج فراوانی

## اعضا و مشارکت
- **مانی حیدرنژاد** ماژول رمز سزار کامل (پیاده‌سازی، شکست χ²، فضای کلید)، زیرساخت پروژه، مستندسازی نهایی
- **محسن حبیبی** رمز جانشینی تک‌حرفی + تحلیل فراوانی n-gram
- **حسن بور بور** رمز ویژنر + Kasiski/IC + بخش‌های امتیازی (هیل، آنتروپی)
