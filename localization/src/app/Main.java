package app;

import java.text.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

/**
 * Java Gold ローカライゼーション学習
 * - Locale
 * - ResourceBundle（概念説明）
 * - DateTimeFormatter
 * - NumberFormat / DecimalFormat
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Java Gold ローカライゼーション ===\n");

        // 1. Locale の基本
        demonstrateLocale();

        // 2. ResourceBundle の検索順序
        demonstrateResourceBundleConcept();

        // 3. DateTimeFormatter
        demonstrateDateTimeFormatter();

        // 4. NumberFormat
        demonstrateNumberFormat();

        // 5. DecimalFormat
        demonstrateDecimalFormat();

        // 6. Locale.Category
        demonstrateLocaleCategory();
    }

    // === 1. Locale の基本 ===
    static void demonstrateLocale() {
        System.out.println("--- 1. Locale の基本 ---");

        // Locale の生成方法
        // ① 定数を使う
        Locale japan = Locale.JAPAN;       // ja_JP
        Locale us = Locale.US;             // en_US
        Locale japanese = Locale.JAPANESE; // ja（言語のみ）

        System.out.println("Locale.JAPAN: " + japan);       // ja_JP
        System.out.println("Locale.US: " + us);             // en_US
        System.out.println("Locale.JAPANESE: " + japanese); // ja

        // ② コンストラクタを使う
        Locale locale1 = new Locale("fr", "FR"); // フランス語_フランス
        Locale locale2 = new Locale("de");        // ドイツ語（国なし）
        System.out.println("new Locale(\"fr\", \"FR\"): " + locale1);
        System.out.println("new Locale(\"de\"): " + locale2);

        // ③ Locale.Builder を使う（推奨）
        Locale locale3 = new Locale.Builder()
                .setLanguage("it")
                .setRegion("IT")
                .build();
        System.out.println("Locale.Builder: " + locale3);

        // ④ forLanguageTag を使う
        Locale locale4 = Locale.forLanguageTag("ko-KR");
        System.out.println("forLanguageTag(\"ko-KR\"): " + locale4);

        // デフォルトロケール
        Locale defaultLocale = Locale.getDefault();
        System.out.println("\nデフォルトロケール: " + defaultLocale);

        // Locale のメソッド
        System.out.println("\n--- Locale のメソッド（JAPAN） ---");
        System.out.println("getLanguage(): " + japan.getLanguage());       // ja
        System.out.println("getCountry(): " + japan.getCountry());         // JP
        System.out.println("getDisplayLanguage(): " + japan.getDisplayLanguage()); // 日本語
        System.out.println("getDisplayCountry(): " + japan.getDisplayCountry());   // 日本
        System.out.println();
    }

    // === 2. ResourceBundle の検索順序（概念説明） ===
    static void demonstrateResourceBundleConcept() {
        System.out.println("--- 2. ResourceBundle の検索順序 ---");

        System.out.println("ResourceBundle.getBundle(\"Messages\", new Locale(\"fr\", \"FR\"))");
        System.out.println("の場合の検索順序:");
        System.out.println();
        System.out.println("  1. Messages_fr_FR.properties  ← 言語_国（完全一致）");
        System.out.println("  2. Messages_fr.properties      ← 言語のみ");
        System.out.println("  3. Messages_ja_JP.properties   ← デフォルトロケール(言語_国)");
        System.out.println("  4. Messages_ja.properties      ← デフォルトロケール(言語)");
        System.out.println("  5. Messages.properties          ← デフォルト（ベースバンドル）");
        System.out.println("  6. MissingResourceException!    ← 何も見つからない場合");
        System.out.println();
        System.out.println("※ Java Classも同じ順序で検索される（.classが.propertiesより優先）");
        System.out.println();

        // 実際にMissingResourceExceptionを発生させてみる
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("NonExistent", Locale.US);
        } catch (MissingResourceException e) {
            System.out.println("MissingResourceException発生: " + e.getMessage());
        }

        System.out.println();

        // キーの検索順序（親バンドルへのフォールバック）
        System.out.println("キーの検索順序（フォールバック）:");
        System.out.println("  Messages_fr_FR で見つからない");
        System.out.println("    → Messages_fr で探す");
        System.out.println("      → Messages で探す");
        System.out.println("        → MissingResourceException");
        System.out.println();
    }

    // === 3. DateTimeFormatter ===
    static void demonstrateDateTimeFormatter() {
        System.out.println("--- 3. DateTimeFormatter ---");

        LocalDate date = LocalDate.of(2025, 12, 25);
        LocalTime time = LocalTime.of(14, 30, 45);
        LocalDateTime dateTime = LocalDateTime.of(date, time);

        // ① 定義済みフォーマッタ
        System.out.println("■ 定義済みフォーマッタ");
        System.out.println("ISO_LOCAL_DATE: " +
                date.format(DateTimeFormatter.ISO_LOCAL_DATE));       // 2025-12-25
        System.out.println("ISO_LOCAL_TIME: " +
                time.format(DateTimeFormatter.ISO_LOCAL_TIME));       // 14:30:45
        System.out.println("ISO_LOCAL_DATE_TIME: " +
                dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)); // 2025-12-25T14:30:45

        // ② ロケール依存のフォーマッタ
        System.out.println("\n■ ロケール依存フォーマッタ");

        // ofLocalizedDate
        DateTimeFormatter jpDateFull = DateTimeFormatter
                .ofLocalizedDate(FormatStyle.FULL)
                .withLocale(Locale.JAPAN);
        DateTimeFormatter usDateFull = DateTimeFormatter
                .ofLocalizedDate(FormatStyle.FULL)
                .withLocale(Locale.US);
        System.out.println("日本(FULL): " + date.format(jpDateFull));
        System.out.println("米国(FULL): " + date.format(usDateFull));

        DateTimeFormatter jpDateShort = DateTimeFormatter
                .ofLocalizedDate(FormatStyle.SHORT)
                .withLocale(Locale.JAPAN);
        DateTimeFormatter usDateShort = DateTimeFormatter
                .ofLocalizedDate(FormatStyle.SHORT)
                .withLocale(Locale.US);
        System.out.println("日本(SHORT): " + date.format(jpDateShort));
        System.out.println("米国(SHORT): " + date.format(usDateShort));

        // ③ カスタムパターン
        System.out.println("\n■ カスタムパターン（ofPattern）");
        DateTimeFormatter custom1 = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        DateTimeFormatter custom2 = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.US);
        DateTimeFormatter custom3 = DateTimeFormatter.ofPattern("yyyy年MM月dd日(E)", Locale.JAPAN);

        System.out.println("yyyy/MM/dd: " + date.format(custom1));
        System.out.println("dd-MMM-yyyy (US): " + date.format(custom2));
        System.out.println("yyyy年MM月dd日(E) (JP): " + date.format(custom3));

        // ④ パース（文字列→日付）
        System.out.println("\n■ パース");
        String dateStr = "2025/12/25";
        LocalDate parsed = LocalDate.parse(dateStr, custom1);
        System.out.println("パース結果: " + parsed);

        // ⑤ FormatStyle の種類
        System.out.println("\n■ FormatStyle の種類");
        for (FormatStyle style : FormatStyle.values()) {
            try {
                DateTimeFormatter f = DateTimeFormatter
                        .ofLocalizedDate(style).withLocale(Locale.JAPAN);
                System.out.println("  " + style + ": " + date.format(f));
            } catch (Exception e) {
                System.out.println("  " + style + ": (使用不可)");
            }
        }

        // ⑥ 型の不一致でDateTimeException
        System.out.println("\n■ 型の不一致");
        try {
            // LocalDateに時刻フォーマッタを使うとエラー
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            date.format(timeFormatter);
        } catch (java.time.DateTimeException e) {
            System.out.println("LocalDateにHH:mm:ss → DateTimeException: " +
                    e.getMessage().substring(0, Math.min(60, e.getMessage().length())));
        }

        System.out.println();
    }

    // === 4. NumberFormat ===
    static void demonstrateNumberFormat() {
        System.out.println("--- 4. NumberFormat ---");

        double number = 1234567.89;
        int percent = 75;

        // ① 数値フォーマット
        System.out.println("■ 数値フォーマット");
        NumberFormat jpNum = NumberFormat.getInstance(Locale.JAPAN);
        NumberFormat usNum = NumberFormat.getInstance(Locale.US);
        NumberFormat deNum = NumberFormat.getInstance(Locale.GERMANY);
        System.out.println("日本: " + jpNum.format(number));   // 1,234,567.89
        System.out.println("米国: " + usNum.format(number));   // 1,234,567.89
        System.out.println("ドイツ: " + deNum.format(number)); // 1.234.567,89

        // ② 通貨フォーマット
        System.out.println("\n■ 通貨フォーマット");
        NumberFormat jpCur = NumberFormat.getCurrencyInstance(Locale.JAPAN);
        NumberFormat usCur = NumberFormat.getCurrencyInstance(Locale.US);
        NumberFormat deCur = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        System.out.println("日本: " + jpCur.format(number));   // ￥1,234,568
        System.out.println("米国: " + usCur.format(number));   // $1,234,567.89
        System.out.println("ドイツ: " + deCur.format(number)); // 1.234.567,89 €

        // ③ パーセントフォーマット
        System.out.println("\n■ パーセントフォーマット");
        NumberFormat jpPct = NumberFormat.getPercentInstance(Locale.JAPAN);
        NumberFormat usPct = NumberFormat.getPercentInstance(Locale.US);
        System.out.println("日本 0.75: " + jpPct.format(0.75));  // 75%
        System.out.println("米国 0.75: " + usPct.format(0.75));  // 75%
        System.out.println("注意: 0.75を渡すと75%になる（100倍される）");

        // ④ パース
        System.out.println("\n■ パース");
        try {
            Number parsed = usNum.parse("1,234,567.89");
            System.out.println("パース結果: " + parsed);       // 1234567.89
            System.out.println("型: " + parsed.getClass());     // Double
        } catch (ParseException e) {
            System.out.println("パースエラー: " + e.getMessage());
        }

        // ⑤ 最大/最小桁数の設定
        System.out.println("\n■ 桁数設定");
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(1);
        nf.setMinimumIntegerDigits(5);
        System.out.println("MaxFrac=1, MinInt=5: " + nf.format(42.789)); // 00,042.8

        System.out.println();
    }

    // === 5. DecimalFormat ===
    static void demonstrateDecimalFormat() {
        System.out.println("--- 5. DecimalFormat ---");

        // パターン記号
        // # → 数字（ゼロなら表示しない）
        // 0 → 数字（ゼロでも表示する）
        // . → 小数点
        // , → グループ区切り
        // % → パーセント（100倍して%付加）

        double value = 1234.5;

        System.out.println("■ DecimalFormat パターン");
        System.out.println("#,###.## → " + new DecimalFormat("#,###.##").format(value));
        System.out.println("000000.000 → " + new DecimalFormat("000000.000").format(value));
        System.out.println("#.## → " + new DecimalFormat("#.##").format(value));
        System.out.println("0.00 → " + new DecimalFormat("0.00").format(value));

        System.out.println("\n■ # と 0 の違い");
        double small = 0.5;
        System.out.println("# で 0.5: " + new DecimalFormat("#.#").format(small));  // .5
        System.out.println("0 で 0.5: " + new DecimalFormat("0.0").format(small));  // 0.5

        System.out.println("\n■ パーセント");
        System.out.println("#% で 0.75: " + new DecimalFormat("#%").format(0.75));   // 75%
        System.out.println("0.0% で 0.756: " + new DecimalFormat("0.0%").format(0.756)); // 75.6%

        System.out.println();
    }

    // === 6. Locale.Category ===
    static void demonstrateLocaleCategory() {
        System.out.println("--- 6. Locale.Category ---");

        // Locale.Category.DISPLAY → 表示言語用（メニュー、ラベル等）
        // Locale.Category.FORMAT  → 数値・日付のフォーマット用

        Locale originalDefault = Locale.getDefault();
        Locale originalDisplay = Locale.getDefault(Locale.Category.DISPLAY);
        Locale originalFormat = Locale.getDefault(Locale.Category.FORMAT);

        System.out.println("現在のデフォルト: " + originalDefault);
        System.out.println("DISPLAY カテゴリ: " + originalDisplay);
        System.out.println("FORMAT カテゴリ: " + originalFormat);

        // カテゴリ別にロケールを設定できる
        System.out.println("\n■ カテゴリ別ロケール設定の概念");
        System.out.println("Locale.setDefault(Locale.Category.DISPLAY, Locale.US)");
        System.out.println("  → UIの表示言語は英語");
        System.out.println("Locale.setDefault(Locale.Category.FORMAT, Locale.JAPAN)");
        System.out.println("  → 数値・日付フォーマットは日本式");

        // 実際のデモ（元に戻すので安全）
        try {
            Locale.setDefault(Locale.Category.FORMAT, Locale.US);
            NumberFormat nf = NumberFormat.getCurrencyInstance(); // FORMATカテゴリを使用
            System.out.println("\nFORMAT=US での通貨: " + nf.format(1234.56));

            Locale.setDefault(Locale.Category.FORMAT, Locale.JAPAN);
            NumberFormat nf2 = NumberFormat.getCurrencyInstance();
            System.out.println("FORMAT=JP での通貨: " + nf2.format(1234.56));
        } finally {
            // デフォルトに戻す
            Locale.setDefault(originalDefault);
            Locale.setDefault(Locale.Category.DISPLAY, originalDisplay);
            Locale.setDefault(Locale.Category.FORMAT, originalFormat);
        }

        System.out.println("\n=== ローカライゼーション学習完了 ===");
    }
}
