package app;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== メソッド参照の4つのパターン ===\n");

        // 1. 静的メソッド参照: ClassName::staticMethod
        demonstrateStaticMethodReference();

        // 2. 特定のオブジェクトのインスタンスメソッド参照: instance::method
        demonstrateInstanceMethodReference();

        // 3. 任意のオブジェクトのインスタンスメソッド参照: ClassName::instanceMethod
        demonstrateArbitraryObjectMethodReference();

        // 4. コンストラクタ参照: ClassName::new
        demonstrateConstructorReference();

        // Stream操作での実践例
        demonstrateStreamOperations();
    }

    // 1. 静的メソッド参照: ClassName::staticMethod
    private static void demonstrateStaticMethodReference() {
        System.out.println("【1. 静的メソッド参照】");

        // ラムダ式での書き方
        Function<String, Integer> lambdaParser = s -> Integer.parseInt(s);
        System.out.println("Lambda: " + lambdaParser.apply("123"));

        // メソッド参照での書き方
        Function<String, Integer> methodRefParser = Integer::parseInt;
        System.out.println("Method Reference: " + methodRefParser.apply("456"));

        // Streamでの使用例
        List<String> numbers = Arrays.asList("10", "20", "30");
        List<Integer> parsed = numbers.stream()
                                      .map(Integer::parseInt)  // Integer::parseIntを使用
                                      .collect(Collectors.toList());
        System.out.println("Parsed numbers: " + parsed);

        // Math.absの例
        IntUnaryOperator absOperator = Math::abs;
        System.out.println("Math.abs(-42): " + absOperator.applyAsInt(-42));

        System.out.println();
    }

    // 2. 特定のオブジェクトのインスタンスメソッド参照: instance::method
    private static void demonstrateInstanceMethodReference() {
        System.out.println("【2. 特定のオブジェクトのインスタンスメソッド参照】");

        // 特定のオブジェクト（System.out）のメソッドを参照
        Consumer<String> lambdaPrinter = s -> System.out.println(s);
        lambdaPrinter.accept("Lambda printer");

        Consumer<String> methodRefPrinter = System.out::println;
        methodRefPrinter.accept("Method reference printer");

        // カスタムオブジェクトの例
        Printer myPrinter = new Printer("【カスタム】");
        Consumer<String> customPrinter = myPrinter::print;
        customPrinter.accept("This is custom printer");

        // Streamでの使用例
        List<String> words = Arrays.asList("Apple", "Banana", "Cherry");
        System.out.println("Using forEach with method reference:");
        words.forEach(System.out::println);  // System.out::printlnを使用

        System.out.println();
    }

    // 3. 任意のオブジェクトのインスタンスメソッド参照: ClassName::instanceMethod
    // これが一番紛らわしいパターン！
    private static void demonstrateArbitraryObjectMethodReference() {
        System.out.println("【3. 任意のオブジェクトのインスタンスメソッド参照（重要！）】");

        // ラムダ式: 引数で受け取ったオブジェクトのメソッドを呼ぶ
        Function<String, String> lambdaLower = s -> s.toLowerCase();
        System.out.println("Lambda: " + lambdaLower.apply("HELLO"));

        // メソッド参照: 第一引数がレシーバーになる
        Function<String, String> methodRefLower = String::toLowerCase;
        System.out.println("Method Reference: " + methodRefLower.apply("WORLD"));

        // 引数が2つの場合の例
        // ラムダ式: (str, prefix) -> str.startsWith(prefix)
        BiFunction<String, String, Boolean> lambdaStartsWith =
            (str, prefix) -> str.startsWith(prefix);
        System.out.println("Lambda startsWith: " + lambdaStartsWith.apply("Hello", "He"));

        // メソッド参照: 第一引数がレシーバー、第二引数がメソッドの引数
        BiFunction<String, String, Boolean> methodRefStartsWith = String::startsWith;
        System.out.println("Method Reference startsWith: " + methodRefStartsWith.apply("Hello", "He"));

        // Streamでの実践例
        List<String> words = Arrays.asList("APPLE", "BANANA", "CHERRY");
        List<String> lowerWords = words.stream()
                                       .map(String::toLowerCase)  // 各要素のtoLowerCase()を呼ぶ
                                       .collect(Collectors.toList());
        System.out.println("Lowercase words: " + lowerWords);

        // Comparatorでの例
        List<String> names = Arrays.asList("John", "alice", "Bob");
        names.sort(String::compareToIgnoreCase);  // 各要素同士を比較
        System.out.println("Sorted names: " + names);

        System.out.println();
    }

    // 4. コンストラクタ参照: ClassName::new
    private static void demonstrateConstructorReference() {
        System.out.println("【4. コンストラクタ参照】");

        // ラムダ式でコンストラクタを呼ぶ
        Supplier<List<String>> lambdaListCreator = () -> new ArrayList<>();
        List<String> list1 = lambdaListCreator.get();
        list1.add("Created by lambda");
        System.out.println("Lambda: " + list1);

        // メソッド参照でコンストラクタを呼ぶ
        Supplier<List<String>> methodRefListCreator = ArrayList::new;
        List<String> list2 = methodRefListCreator.get();
        list2.add("Created by method reference");
        System.out.println("Method Reference: " + list2);

        // 引数ありコンストラクタの例
        Function<String, Person> lambdaPersonCreator = name -> new Person(name);
        Person person1 = lambdaPersonCreator.apply("Alice");
        System.out.println("Lambda: " + person1);

        Function<String, Person> methodRefPersonCreator = Person::new;
        Person person2 = methodRefPersonCreator.apply("Bob");
        System.out.println("Method Reference: " + person2);

        // 配列のコンストラクタ参照
        IntFunction<String[]> arrayCreator = String[]::new;
        String[] array = arrayCreator.apply(5);
        System.out.println("Created array length: " + array.length);

        // Streamでの使用例
        List<String> nameList = Arrays.asList("Charlie", "Diana", "Eve");
        List<Person> people = nameList.stream()
                                      .map(Person::new)  // Person::newでコンストラクタ呼び出し
                                      .collect(Collectors.toList());
        System.out.println("Created people: " + people);

        // toArray()でコンストラクタ参照を使う重要パターン
        String[] nameArray = nameList.stream()
                                     .toArray(String[]::new);  // 配列生成
        System.out.println("Array: " + Arrays.toString(nameArray));

        System.out.println();
    }

    // Stream操作での実践例
    private static void demonstrateStreamOperations() {
        System.out.println("【Streamでの実践例】");

        List<String> fruits = Arrays.asList("apple", "BANANA", "Cherry", "date", "ELDERBERRY");

        // 複数のメソッド参照を組み合わせる
        System.out.println("Filter and transform:");
        fruits.stream()
              .filter(s -> s.length() > 4)           // lambdaを使う（複雑な条件）
              .map(String::toLowerCase)               // メソッド参照（シンプル）
              .sorted()
              .forEach(System.out::println);          // メソッド参照

        // 数値データの処理
        List<String> numberStrings = Arrays.asList("1", "2", "3", "4", "5");
        int sum = numberStrings.stream()
                               .map(Integer::parseInt)      // 静的メソッド参照
                               .mapToInt(Integer::intValue) // インスタンスメソッド参照
                               .sum();
        System.out.println("\nSum: " + sum);

        // Collectorsでのメソッド参照
        List<Person> people = Arrays.asList(
            new Person("Alice"),
            new Person("Bob"),
            new Person("Charlie")
        );

        String allNames = people.stream()
                                .map(Person::getName)  // Getterをメソッド参照で呼ぶ
                                .collect(Collectors.joining(", "));
        System.out.println("\nAll names: " + allNames);

        // メソッド参照が使えないケース
        System.out.println("\n【メソッド参照が使えないケース】");

        // 引数の加工が必要な場合 → ラムダ式を使う
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        numbers.stream()
               .map(n -> n * 2)  // メソッド参照不可（演算が入る）
               .forEach(System.out::println);

        // 複数の引数を渡す場合
        fruits.stream()
              .map(s -> s.substring(0, 3))  // メソッド参照不可（引数が固定値）
              .forEach(System.out::println);

        System.out.println();
    }
}

// ヘルパークラス
class Printer {
    private String prefix;

    public Printer(String prefix) {
        this.prefix = prefix;
    }

    public void print(String message) {
        System.out.println(prefix + " " + message);
    }
}

class Person {
    private String name;

    public Person(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Person{name='" + name + "'}";
    }
}
