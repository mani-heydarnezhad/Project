# پروژه پایانی ریاضیات گسسته — موضوع ۹: رمزنگاری و رمزشکنی کلاسیک با تحلیل فراوانی

## اجرا

### منوی تعاملی خط فرمان (توصیه‌شده)
```bash
mvn compile
mvn exec:java
```
با اجرای دستور بالا منوی تعاملی (`cli.Main`) باز می‌شود و می‌توانید از داخل
همان کنسول، عملیات مورد نظر (رمزگذاری/رمزگشایی/شکست خودکار برای هر یک از
چهار رمز، و ابزارهای کمکی) را با وارد کردن شماره‌ی گزینه انتخاب کنید و نتیجه
را همان‌جا ببینید.

### اجرای نسخه‌ی نمایشی قدیمی (خروجی ثابت، بدون ورودی تعاملی)
```bash
mvn exec:java -Dexec.mainClass="caesar.CaesarDemo"
```

### تست‌ها
```bash
mvn test
```

## ساختار
```
src/main/java/
├── util/
│   ├── Alphabet.java            نگاشت الفبا به Z_n (فارسی/انگلیسی)
│   ├── FrequencyTable.java      استخراج و جدول‌های فراوانی مرجع
│   ├── KeySpaceCalculator.java  محاسبه اندازه فضای کلید هر رمز
│   └── ShannonEntropy.java      آنتروپی شانون (امتیازی)
├── caesar/
│   ├── CaesarCipher.java        رمزگذاری/رمزگشایی سزار
│   ├── CaesarCryptanalyzer.java شکست با brute-force + χ²
│   └── CaesarDemo.java          اجرای نمایشی
├── substitution/
│   ├── SubstitutionCipher.java  رمز جانشینی تک‌حرفی
│   ├── FrequencyAnalysisAttack.java  حدس اولیه کلید با فراوانی
│   ├── NGramModel.java          مدل زبانی دوگرام/سه‌گرام
│   └── HillClimbingAttack.java  شکست با تپه‌نوردی (امتیازی)
├── vigenere/
│   ├── VigenereCipher.java      رمزگذاری/رمزگشایی ویژنر
│   ├── KasiskiExamination.java  آزمون کاسیسکی
│   ├── IndexOfCoincidence.java  شاخص تطابق (IC)
│   └── VigenereCryptanalyzer.java  بازیابی کامل کلید
├── hill/
│   ├── HillCipher.java              رمز هیل (امتیازی)
│   └── HillKnownPlaintextAttack.java  حمله KPA (امتیازی)
└── cli/
    └── Main.java                 منوی تعاملی خط فرمان برای همه‌ی ماژول‌ها

src/test/java/    — تست‌های JUnit 5 متناظر با هر ماژول بالا
data/             — پیکره نمونه برای استخراج فراوانی
docs/             — اثبات‌های ریاضی و یادداشت‌های تکمیلی
benchmarks/       — نتایج آزمایش‌های تجربی و نمودارها
```


## اعضا و مشارکت
- **مانی حیدرنژاد** ماژول رمز سزار کامل (پیاده‌سازی، شکست χ²، فضای کلید)، زیرساخت پروژه، مستندسازی نهایی
- **محسن حبیبی** رمز جانشینی تک‌حرفی + تحلیل فراوانی n-gram
- **حسن بور بور** رمز ویژنر + Kasiski/IC + بخش‌های امتیازی (هیل، آنتروپی)


## وضعیت تست‌ها
مجموعاً ۲۷ تست واحد/یکپارچگی در چهار کلاس تست (`CaesarCipherTest`،
`SubstitutionCipherTest`، `VigenereCipherTest`، `HillCipherTest`،
`ShannonEntropyTest`) نوشته شده است. تمام منطق الگوریتم‌ها پیش از commit با
شبیه‌سازی معادل تأیید صحت شده (نتایج در تاریخچه توسعه ثبت است).