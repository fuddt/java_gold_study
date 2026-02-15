package app;

import java.io.*;

/**
 * Java Gold 例外処理の総合デモ
 * - try-with-resources
 * - カスタム例外
 * - マルチキャッチ
 * - assert文
 * - 抑制された例外
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Java Gold 例外処理デモ ===\n");

        // 1. try-with-resourcesの基本
        demonstrateTryWithResources();

        // 2. 複数リソースのclose順序
        demonstrateMultipleResourcesCloseOrder();

        // 3. 抑制された例外（Suppressed Exceptions）
        demonstrateSuppressedExceptions();

        // 4. カスタム例外
        demonstrateCustomExceptions();

        // 5. マルチキャッチ
        demonstrateMultiCatch();

        // 6. Effectively final in catch
        demonstrateEffectivelyFinalInCatch();

        // 7. assert文のデモ
        demonstrateAssert();

        // 8. 例外の階層
        demonstrateExceptionHierarchy();

        // 9. finally と return の罠
        demonstrateFinallyReturnTrap();
    }

    // 1. try-with-resourcesの基本
    static void demonstrateTryWithResources() {
        System.out.println("--- 1. try-with-resourcesの基本 ---");

        // AutoCloseableを実装したリソースは自動でclose()される
        try (MyResource resource = new MyResource("リソース1")) {
            resource.use();
            System.out.println("リソースを使用中");
        } catch (Exception e) {
            System.out.println("例外キャッチ: " + e.getMessage());
        }
        // ここでリソースは自動的にcloseされている

        System.out.println();
    }

    // 2. 複数リソースのclose順序
    static void demonstrateMultipleResourcesCloseOrder() {
        System.out.println("--- 2. 複数リソースのclose順序（逆順） ---");

        // 複数のリソースは宣言と逆順でcloseされる
        try (
            MyResource resource1 = new MyResource("リソース1");
            MyResource resource2 = new MyResource("リソース2");
            MyResource resource3 = new MyResource("リソース3")
        ) {
            System.out.println("全リソース使用中");
        } catch (Exception e) {
            System.out.println("例外: " + e.getMessage());
        }
        // close順序: リソース3 → リソース2 → リソース1

        System.out.println();
    }

    // 3. 抑制された例外（Suppressed Exceptions）
    static void demonstrateSuppressedExceptions() {
        System.out.println("--- 3. 抑制された例外 ---");

        try (ProblematicResource resource = new ProblematicResource()) {
            resource.causeException();
        } catch (Exception e) {
            System.out.println("メイン例外: " + e.getMessage());

            // 抑制された例外を取得
            Throwable[] suppressed = e.getSuppressed();
            if (suppressed.length > 0) {
                System.out.println("抑制された例外の数: " + suppressed.length);
                for (Throwable t : suppressed) {
                    System.out.println("  - " + t.getMessage());
                }
            }
        }

        System.out.println();
    }

    // 4. カスタム例外
    static void demonstrateCustomExceptions() {
        System.out.println("--- 4. カスタム例外 ---");

        // Checked Exception（Exceptionを継承）
        try {
            validateAge(-5);
        } catch (InvalidAgeException e) {
            System.out.println("Checked例外: " + e.getMessage());
        }

        // Unchecked Exception（RuntimeExceptionを継承）
        try {
            processPayment(-1000);
        } catch (InvalidPaymentException e) {
            System.out.println("Unchecked例外: " + e.getMessage());
        }

        System.out.println();
    }

    // 5. マルチキャッチ
    static void demonstrateMultiCatch() {
        System.out.println("--- 5. マルチキャッチ ---");

        // パターン1: 異なる例外を同時にキャッチ
        try {
            int random = (int)(Math.random() * 3);
            switch (random) {
                case 0: throw new IOException("IO例外");
                case 1: throw new IllegalArgumentException("引数例外");
                case 2: throw new NullPointerException("null例外");
            }
        } catch (IOException | IllegalArgumentException e) {
            // マルチキャッチ: eは暗黙的にfinal
            System.out.println("IOまたは引数例外: " + e.getMessage());
            // e = new Exception(); // コンパイルエラー！finalなので再代入不可
        } catch (NullPointerException e) {
            System.out.println("null例外: " + e.getMessage());
        }

        System.out.println();
    }

    // 6. Effectively final in catch
    static void demonstrateEffectivelyFinalInCatch() {
        System.out.println("--- 6. catchブロックの変数はeffectively final ---");

        try {
            throw new RuntimeException("テスト例外");
        } catch (RuntimeException e) {
            // catchした例外変数eはeffectively final
            Runnable r = () -> {
                System.out.println("ラムダ内から例外参照: " + e.getMessage());
            };
            r.run();

            // e = new RuntimeException(); // これは不可（再代入できない）
        }

        System.out.println();
    }

    // 7. assert文のデモ
    static void demonstrateAssert() {
        System.out.println("--- 7. assert文 ---");
        System.out.println("注: assertを有効化するには -ea フラグが必要");

        int value = 10;

        // assert文: 条件がfalseならAssertionErrorがスローされる
        assert value > 0 : "値は正でなければならない";
        System.out.println("assert成功: value = " + value);

        // assertが無効の場合は以下も実行される
        // 有効化: java -ea app.Main
        try {
            assert false : "これは意図的な失敗";
            System.out.println("assertが無効なので、この行も実行される");
        } catch (AssertionError e) {
            System.out.println("AssertionError: " + e.getMessage());
        }

        System.out.println();
    }

    // 8. 例外の階層
    static void demonstrateExceptionHierarchy() {
        System.out.println("--- 8. 例外の階層 ---");
        System.out.println("Throwable");
        System.out.println("  ├─ Error (深刻なエラー、通常はキャッチしない)");
        System.out.println("  │   └─ OutOfMemoryError, StackOverflowError等");
        System.out.println("  └─ Exception");
        System.out.println("      ├─ IOException, SQLException等（Checked）");
        System.out.println("      └─ RuntimeException（Unchecked）");
        System.out.println("          └─ NullPointerException, IllegalArgumentException等");

        System.out.println("\nChecked例外: コンパイル時にcatchまたはthrowsが必須");
        System.out.println("Unchecked例外: RuntimeExceptionとError、catchやthrowsは任意");

        System.out.println();
    }

    // 9. finally と return の罠
    static void demonstrateFinallyReturnTrap() {
        System.out.println("--- 9. finally と return の罠 ---");

        System.out.println("結果1: " + methodWithFinallyReturn());
        System.out.println("結果2: " + methodWithFinallyModification());
        System.out.println("結果3: " + methodWithExceptionInFinally());

        System.out.println();
    }

    // finallyでreturnすると、tryのreturnは無視される
    static int methodWithFinallyReturn() {
        try {
            return 1; // これは無視される！
        } finally {
            return 2; // finallyのreturnが優先される
        }
    }

    // finallyで変数を変更してもreturn値には影響しない（プリミティブの場合）
    static int methodWithFinallyModification() {
        int value = 1;
        try {
            return value; // ここで1が評価される
        } finally {
            value = 2; // これはreturn値に影響しない
        }
    }

    // finallyで例外がスローされると、tryの例外は失われる
    static String methodWithExceptionInFinally() {
        try {
            try {
                throw new RuntimeException("try例外");
            } finally {
                throw new RuntimeException("finally例外"); // この例外が優先される
            }
        } catch (RuntimeException e) {
            return e.getMessage(); // "finally例外"が返される
        }
    }

    // 年齢検証メソッド
    static void validateAge(int age) throws InvalidAgeException {
        if (age < 0) {
            throw new InvalidAgeException("年齢は0以上でなければなりません: " + age);
        }
    }

    // 支払い処理メソッド
    static void processPayment(int amount) {
        if (amount < 0) {
            throw new InvalidPaymentException("支払額は0以上でなければなりません: " + amount);
        }
    }
}

// AutoCloseableを実装したカスタムリソース
class MyResource implements AutoCloseable {
    private String name;

    public MyResource(String name) {
        this.name = name;
        System.out.println(name + " を開きました");
    }

    public void use() {
        System.out.println(name + " を使用しています");
    }

    @Override
    public void close() throws Exception {
        System.out.println(name + " をcloseしました");
    }
}

// 問題のあるリソース（closeで例外をスロー）
class ProblematicResource implements AutoCloseable {
    public void causeException() throws Exception {
        throw new Exception("リソース使用中の例外");
    }

    @Override
    public void close() throws Exception {
        throw new Exception("close中の例外（抑制される）");
    }
}

// カスタムChecked例外（Exceptionを継承）
class InvalidAgeException extends Exception {
    public InvalidAgeException(String message) {
        super(message);
    }
}

// カスタムUnchecked例外（RuntimeExceptionを継承）
class InvalidPaymentException extends RuntimeException {
    public InvalidPaymentException(String message) {
        super(message);
    }
}
