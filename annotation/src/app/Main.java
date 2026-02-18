package app;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.Arrays;

/**
 * Java Gold アノテーション学習
 * - 組み込みアノテーション
 * - カスタムアノテーション
 * - メタアノテーション
 * - リフレクションでの読み取り
 */

// === カスタムアノテーション定義 ===

// 基本的なカスタムアノテーション（value要素）
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface MyAnnotation {
    String value(); // 要素名が"value"の場合、@MyAnnotation("test")と省略可能
}

// 複数要素を持つアノテーション
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@interface Info {
    String author();
    String date();
    int version() default 1;       // デフォルト値あり
    String[] tags() default {};     // 配列型の要素
}

// @Inheritedの例（サブクラスに継承される）
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface InheritedAnnotation {
    String value();
}

// @Repeatableの例（同じアノテーションを複数回付けられる）
@Repeatable(Schedules.class) // コンテナアノテーションを指定
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Schedule {
    String day();
    String time();
}

// @Repeatableのコンテナアノテーション（必須！）
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Schedules {
    Schedule[] value(); // 要素名は"value"でなければならない
}

// アノテーション要素に使える型のデモ
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface FieldInfo {
    int intValue();                  // プリミティブ型
    String stringValue();            // String型
    Class<?> classValue();           // Class型
    ElementType enumValue();         // enum型
    int[] arrayValue();              // 配列型
}

// @FunctionalInterfaceのデモ
@FunctionalInterface
interface MyFunction {
    void execute();
    // default と static はOK（抽象メソッドとしてカウントされない）
    default void defaultMethod() {}
    static void staticMethod() {}
}

// === 親クラス（@Inheritedの検証用） ===
@InheritedAnnotation("親クラスのアノテーション")
class ParentClass {
    @Deprecated
    public void oldMethod() {
        System.out.println("この旧メソッドは非推奨です");
    }
}

// === メインクラス ===
@Info(author = "Java太郎", date = "2026-02-16", version = 2, tags = {"study", "annotation"})
public class Main extends ParentClass {

    public static void main(String[] args) {
        System.out.println("=== アノテーション学習デモ ===\n");

        Main main = new Main();

        // 1. 組み込みアノテーション
        demonstrateBuiltinAnnotations(main);

        // 2. カスタムアノテーション
        main.customAnnotationExample();
        main.scheduledMethod();

        // 3. リフレクションでアノテーションを読み取る
        readAnnotationsWithReflection();

        // 4. @FunctionalInterface
        demonstrateFunctionalInterface();

        // 5. アノテーション要素に使える型
        demonstrateAnnotationElementTypes();

        System.out.println("\n=== デモ終了 ===");
    }

    // --- 1. 組み込みアノテーション ---
    static void demonstrateBuiltinAnnotations(Main main) {
        System.out.println("--- 1. 組み込みアノテーション ---");

        // @Override: スーパークラスのメソッドをオーバーライドしていることを明示
        System.out.println("@Override: " + main.toString());

        // @Deprecated: 非推奨メソッドの呼び出し（警告が出る）
        main.oldMethod();

        // @SuppressWarnings: コンパイラ警告を抑制
        main.suppressWarningsExample();

        // @SafeVarargs: 可変長引数の型安全性警告を抑制
        main.safeVarargsExample(
                Arrays.asList("A", "B"),
                Arrays.asList("C", "D"));

        System.out.println();
    }

    // @Override の例
    @Override
    public String toString() {
        return "Main クラスのインスタンス";
    }

    // @Deprecated の例
    @Deprecated
    public void deprecatedMethod() {
        System.out.println("このメソッドは非推奨です");
    }

    // @SuppressWarnings の例
    @SuppressWarnings("unchecked")
    public void suppressWarningsExample() {
        // raw型の警告が抑制される
        @SuppressWarnings("rawtypes")
        java.util.List list = new java.util.ArrayList();
        list.add("警告が抑制される");
        System.out.println("@SuppressWarnings: " + list);
    }

    // @SafeVarargs の例（final, static, privateメソッドにのみ付けられる）
    @SafeVarargs
    public final void safeVarargsExample(java.util.List<String>... lists) {
        for (java.util.List<String> list : lists) {
            System.out.println("@SafeVarargs リスト: " + list);
        }
    }

    // --- 2. カスタムアノテーション ---
    // valueの省略記法
    @MyAnnotation("テストメソッド") // @MyAnnotation(value = "テストメソッド")と同じ
    @Info(author = "Java次郎", date = "2026-02-16", tags = {"test", "custom"})
    public void customAnnotationExample() {
        System.out.println("\n--- 2. カスタムアノテーション ---");
        System.out.println("カスタムアノテーションが付いたメソッド");
    }

    // @Repeatable の例（同じアノテーションを複数回）
    @Schedule(day = "月曜日", time = "10:00")
    @Schedule(day = "水曜日", time = "14:00")
    @Schedule(day = "金曜日", time = "16:00")
    public void scheduledMethod() {
        System.out.println("スケジュール設定されたメソッド");
    }

    // --- 3. リフレクションでアノテーション読み取り ---
    static void readAnnotationsWithReflection() {
        System.out.println("\n--- 3. リフレクションでアノテーション読み取り ---");

        try {
            Class<Main> clazz = Main.class;

            // クラスレベルのアノテーション
            System.out.println("■ クラスのアノテーション");
            if (clazz.isAnnotationPresent(Info.class)) {
                Info info = clazz.getAnnotation(Info.class);
                System.out.println("  author: " + info.author());
                System.out.println("  date: " + info.date());
                System.out.println("  version: " + info.version());
                System.out.println("  tags: " + Arrays.toString(info.tags()));
            }

            // @Inherited で継承されたアノテーション
            if (clazz.isAnnotationPresent(InheritedAnnotation.class)) {
                InheritedAnnotation inherited = clazz.getAnnotation(InheritedAnnotation.class);
                System.out.println("  @Inherited継承: " + inherited.value());
            }

            // メソッドのアノテーション
            System.out.println("\n■ customAnnotationExampleメソッドのアノテーション");
            Method method1 = clazz.getMethod("customAnnotationExample");
            MyAnnotation myAnnot = method1.getAnnotation(MyAnnotation.class);
            if (myAnnot != null) {
                System.out.println("  MyAnnotation.value: " + myAnnot.value());
            }
            Info methodInfo = method1.getAnnotation(Info.class);
            if (methodInfo != null) {
                System.out.println("  Info.author: " + methodInfo.author());
                System.out.println("  Info.tags: " + Arrays.toString(methodInfo.tags()));
            }

            // @Repeatable アノテーションの読み取り
            System.out.println("\n■ scheduledMethodの@Repeatableアノテーション");
            Method method2 = clazz.getMethod("scheduledMethod");

            // getAnnotationsByType: 個別のアノテーションを配列で取得
            Schedule[] schedules = method2.getAnnotationsByType(Schedule.class);
            for (int i = 0; i < schedules.length; i++) {
                System.out.println("  スケジュール" + (i + 1) + ": " +
                        schedules[i].day() + " " + schedules[i].time());
            }

            // getAnnotation: コンテナアノテーションとして取得
            Schedules container = method2.getAnnotation(Schedules.class);
            if (container != null) {
                System.out.println("  (コンテナアノテーション経由でも取得可能)");
            }

            // すべてのアノテーション一覧
            System.out.println("\n■ メソッドの全アノテーション");
            Annotation[] allAnnotations = method1.getAnnotations();
            for (Annotation ann : allAnnotations) {
                System.out.println("  " + ann.annotationType().getSimpleName());
            }

            // getDeclaredAnnotations vs getAnnotations
            System.out.println("\n■ getDeclaredAnnotations vs getAnnotations");
            System.out.println("  getAnnotations(): " + clazz.getAnnotations().length +
                    "個（@Inherited含む）");
            System.out.println("  getDeclaredAnnotations(): " +
                    clazz.getDeclaredAnnotations().length + "個（直接付けたもののみ）");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- 4. @FunctionalInterface ---
    static void demonstrateFunctionalInterface() {
        System.out.println("\n--- 4. @FunctionalInterface ---");
        MyFunction func = () -> System.out.println("ラムダで実装した関数型インターフェース");
        func.execute();
        System.out.println("抽象メソッドが1つだけ → ラムダ式で使える");
    }

    // --- 5. アノテーション要素に使える型 ---
    static void demonstrateAnnotationElementTypes() {
        System.out.println("\n--- 5. アノテーション要素に使える型 ---");
        System.out.println("■ 使える型");
        System.out.println("  1. プリミティブ型（int, long, double, boolean等）");
        System.out.println("  2. String");
        System.out.println("  3. Class（Class<?>）");
        System.out.println("  4. enum型");
        System.out.println("  5. アノテーション型");
        System.out.println("  6. 上記の配列");

        System.out.println("\n■ 使えない型（コンパイルエラー！）");
        System.out.println("  × Object");
        System.out.println("  × Integer等のラッパー型");
        System.out.println("  × List<String>等のジェネリック型");
        System.out.println("  × int[][]等の多次元配列");
    }
}
