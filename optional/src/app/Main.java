package app;

import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Optionalの基本 ===");
        basicOptional();

        System.out.println("\n=== get()の危険性 ===");
        dangerousGet();

        System.out.println("\n=== orElse vs orElseGet の違い ===");
        orElseVsOrElseGet();

        System.out.println("\n=== orElseThrow ===");
        orElseThrowExample();

        System.out.println("\n=== ifPresent と ifPresentOrElse ===");
        ifPresentExample();

        System.out.println("\n=== map と flatMap ===");
        mapAndFlatMap();

        System.out.println("\n=== isPresent と isEmpty ===");
        isPresentAndIsEmpty();
    }

    // Optionalの基本的な生成方法
    static void basicOptional() {
        // Optional.of() - 値がnullでない場合に使用
        Optional<String> opt1 = Optional.of("Hello");
        System.out.println("Optional.of(): " + opt1);

        // Optional.ofNullable() - 値がnullかもしれない場合に使用
        String nullableValue = null;
        Optional<String> opt2 = Optional.ofNullable(nullableValue);
        System.out.println("Optional.ofNullable(null): " + opt2);

        Optional<String> opt3 = Optional.ofNullable("World");
        System.out.println("Optional.ofNullable(\"World\"): " + opt3);

        // Optional.empty() - 空のOptionalを明示的に作成
        Optional<String> opt4 = Optional.empty();
        System.out.println("Optional.empty(): " + opt4);

        // 重要: Optional.of(null) は NullPointerException を投げる！
        try {
            Optional<String> opt5 = Optional.of(null); // ← これは例外が発生する
        } catch (NullPointerException e) {
            System.out.println("Optional.of(null) → NullPointerException発生！");
        }
    }

    // get()の危険性を示す
    static void dangerousGet() {
        Optional<String> present = Optional.of("存在する");
        Optional<String> empty = Optional.empty();

        // 値が存在する場合はOK
        System.out.println("present.get(): " + present.get());

        // 値が存在しない場合はNoSuchElementExceptionが発生！
        try {
            String value = empty.get(); // ← 危険！
        } catch (java.util.NoSuchElementException e) {
            System.out.println("empty.get() → NoSuchElementException発生！");
            System.out.println("→ get()は使わず、orElse系メソッドを使うべき");
        }
    }

    // orElse vs orElseGet の重要な違い（試験頻出！）
    static void orElseVsOrElseGet() {
        System.out.println("--- 値が存在する場合 ---");
        Optional<String> present = Optional.of("存在する値");

        // orElse: 値が存在してもdefault値の評価が実行される（常に評価される）
        String result1 = present.orElse(expensiveOperation("orElse"));
        System.out.println("結果: " + result1);

        // orElseGet: 値が存在する場合は評価されない（遅延評価）
        String result2 = present.orElseGet(() -> expensiveOperation("orElseGet"));
        System.out.println("結果: " + result2);

        System.out.println("\n--- 値が存在しない場合 ---");
        Optional<String> empty = Optional.empty();

        // orElse: default値が返される
        String result3 = empty.orElse(expensiveOperation("orElse"));
        System.out.println("結果: " + result3);

        // orElseGet: Supplierが実行されてdefault値が返される
        String result4 = empty.orElseGet(() -> expensiveOperation("orElseGet"));
        System.out.println("結果: " + result4);
    }

    // 重い処理のシミュレーション
    static String expensiveOperation(String caller) {
        System.out.println("  [" + caller + "] 重い処理が実行された！");
        return "デフォルト値";
    }

    // orElseThrow: 値がない場合に例外を投げる
    static void orElseThrowExample() {
        Optional<String> present = Optional.of("値あり");
        Optional<String> empty = Optional.empty();

        // 値が存在する場合
        String value1 = present.orElseThrow();
        System.out.println("present.orElseThrow(): " + value1);

        // カスタム例外を指定することも可能
        String value2 = present.orElseThrow(() -> new IllegalStateException("値がありません"));
        System.out.println("present.orElseThrow(custom): " + value2);

        // 値が存在しない場合は例外が発生
        try {
            String value3 = empty.orElseThrow();
        } catch (java.util.NoSuchElementException e) {
            System.out.println("empty.orElseThrow() → NoSuchElementException発生");
        }

        try {
            String value4 = empty.orElseThrow(() -> new IllegalStateException("カスタム例外"));
        } catch (IllegalStateException e) {
            System.out.println("empty.orElseThrow(custom) → " + e.getMessage());
        }
    }

    // ifPresent と ifPresentOrElse
    static void ifPresentExample() {
        Optional<String> present = Optional.of("こんにちは");
        Optional<String> empty = Optional.empty();

        // ifPresent: 値が存在する場合のみアクションを実行
        System.out.print("present.ifPresent(): ");
        present.ifPresent(value -> System.out.println(value + "！"));

        System.out.print("empty.ifPresent(): ");
        empty.ifPresent(value -> System.out.println(value + "！")); // 何も出力されない
        System.out.println("(何も出力されない)");

        // ifPresentOrElse: 値がある場合とない場合の両方を処理
        System.out.print("present.ifPresentOrElse(): ");
        present.ifPresentOrElse(
            value -> System.out.println("値: " + value),
            () -> System.out.println("値なし")
        );

        System.out.print("empty.ifPresentOrElse(): ");
        empty.ifPresentOrElse(
            value -> System.out.println("値: " + value),
            () -> System.out.println("値なし")
        );
    }

    // map と flatMap
    static void mapAndFlatMap() {
        // map: 値を変換する
        Optional<String> name = Optional.of("yamada");
        Optional<String> upperName = name.map(String::toUpperCase);
        System.out.println("map(toUpperCase): " + upperName.orElse("N/A"));

        Optional<Integer> length = name.map(String::length);
        System.out.println("map(length): " + length.orElse(0));

        // 空のOptionalにmapを適用しても空のまま
        Optional<String> empty = Optional.empty();
        Optional<String> mapped = empty.map(String::toUpperCase);
        System.out.println("empty.map(): " + mapped);

        // flatMap: Optionalを返すメソッドの結果を平坦化する
        Optional<String> result1 = name.flatMap(Main::findEmail);
        System.out.println("flatMap(findEmail): " + result1.orElse("メールなし"));

        // mapを使うとOptional<Optional<String>>になってしまう
        Optional<Optional<String>> nested = name.map(Main::findEmail);
        System.out.println("map(findEmail): " + nested); // Optional[Optional[...]]

        // flatMapはネストを解消してくれる
        Optional<String> result2 = Optional.of("tanaka")
            .flatMap(Main::findEmail);
        System.out.println("flatMap(tanaka): " + result2.orElse("メールなし"));
    }

    // メールアドレスを検索する（Optionalを返す）
    static Optional<String> findEmail(String name) {
        if ("yamada".equals(name)) {
            return Optional.of("yamada@example.com");
        }
        return Optional.empty();
    }

    // isPresent と isEmpty
    static void isPresentAndIsEmpty() {
        Optional<String> present = Optional.of("値あり");
        Optional<String> empty = Optional.empty();

        // isPresent: 値が存在するか確認
        System.out.println("present.isPresent(): " + present.isPresent()); // true
        System.out.println("empty.isPresent(): " + empty.isPresent()); // false

        // isEmpty: 値が存在しないか確認（Java 11+）
        System.out.println("present.isEmpty(): " + present.isEmpty()); // false
        System.out.println("empty.isEmpty(): " + empty.isEmpty()); // true

        // アンチパターン: isPresentでチェックしてからgetするのは良くない
        if (present.isPresent()) {
            System.out.println("アンチパターン: " + present.get());
        }
        // 代わりにこう書くべき
        System.out.println("ベストプラクティス: " + present.orElse("デフォルト"));

        // または
        present.ifPresent(value -> System.out.println("ベストプラクティス2: " + value));
    }
}
