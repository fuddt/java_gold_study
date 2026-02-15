package app;

import java.util.*;
import java.util.function.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Bi系関数型インターフェース ===\n");

        // BiFunction<T, U, R> - 2つの引数を受け取り、結果を返す
        biFunctionExample();

        // BiConsumer<T, U> - 2つの引数を受け取り、戻り値なし
        biConsumerExample();

        // BiPredicate<T, U> - 2つの引数を受け取り、boolean返す
        biPredicateExample();

        // UnaryOperator<T> と BinaryOperator<T>
        operatorExample();

        // プリミティブ特殊化 - Int系
        intPrimitiveExample();

        // プリミティブ特殊化 - ToXxx系
        toIntFunctionExample();

        // プリミティブ特殊化 - ObjXxxConsumer系
        objIntConsumerExample();

        // Map操作との組み合わせ
        mapOperationsExample();
    }

    // BiFunction<T, U, R> の例
    private static void biFunctionExample() {
        System.out.println("--- BiFunction<T, U, R> ---");

        // 2つの文字列を連結する
        BiFunction<String, String, String> concat = (s1, s2) -> s1 + s2;
        System.out.println("concat: " + concat.apply("Hello", "World")); // HelloWorld

        // 2つの数値を計算する
        BiFunction<Integer, Integer, Integer> multiply = (a, b) -> a * b;
        System.out.println("multiply: " + multiply.apply(5, 3)); // 15

        // andThenで連鎖できる（結果に対してさらに関数を適用）
        BiFunction<String, String, String> concatAndUpper = concat.andThen(String::toUpperCase);
        System.out.println("concatAndUpper: " + concatAndUpper.apply("hello", "world")); // HELLOWORLD

        System.out.println();
    }

    // BiConsumer<T, U> の例
    private static void biConsumerExample() {
        System.out.println("--- BiConsumer<T, U> ---");

        // 2つの引数を受け取り、出力する
        BiConsumer<String, Integer> printKeyValue = (key, value) ->
            System.out.println(key + " = " + value);
        printKeyValue.accept("age", 25);

        // Map.forEachで使用（これが典型的な用途）
        Map<String, Integer> scores = new HashMap<>();
        scores.put("Alice", 85);
        scores.put("Bob", 92);
        scores.put("Charlie", 78);

        System.out.println("scores:");
        scores.forEach((name, score) -> System.out.println("  " + name + ": " + score));

        // andThenで連鎖できる
        BiConsumer<String, Integer> logAndPrint =
            ((BiConsumer<String, Integer>) (k, v) -> System.out.print("Logging... "))
            .andThen((k, v) -> System.out.println(k + "=" + v));
        logAndPrint.accept("test", 100);

        System.out.println();
    }

    // BiPredicate<T, U> の例
    private static void biPredicateExample() {
        System.out.println("--- BiPredicate<T, U> ---");

        // 2つの数値を比較する
        BiPredicate<Integer, Integer> isGreater = (a, b) -> a > b;
        System.out.println("10 > 5: " + isGreater.test(10, 5)); // true
        System.out.println("3 > 7: " + isGreater.test(3, 7)); // false

        // 文字列の長さを比較
        BiPredicate<String, Integer> isLongerThan = (str, len) -> str.length() > len;
        System.out.println("'Hello' > 3: " + isLongerThan.test("Hello", 3)); // true

        // and, or, negateで論理演算できる
        BiPredicate<Integer, Integer> isEqual = (a, b) -> a.equals(b);
        BiPredicate<Integer, Integer> isGreaterOrEqual = isGreater.or(isEqual);
        System.out.println("5 >= 5: " + isGreaterOrEqual.test(5, 5)); // true
        System.out.println("3 >= 5: " + isGreaterOrEqual.test(3, 5)); // false

        System.out.println();
    }

    // UnaryOperator と BinaryOperator の例
    private static void operatorExample() {
        System.out.println("--- UnaryOperator<T> と BinaryOperator<T> ---");

        // UnaryOperator<T> は Function<T, T> の特殊化
        // 引数と戻り値が同じ型
        UnaryOperator<String> toUpper = s -> s.toUpperCase();
        System.out.println("UnaryOperator: " + toUpper.apply("hello")); // HELLO

        UnaryOperator<Integer> square = n -> n * n;
        System.out.println("square: " + square.apply(5)); // 25

        // BinaryOperator<T> は BiFunction<T, T, T> の特殊化
        // 2つの引数と戻り値が同じ型
        BinaryOperator<Integer> add = (a, b) -> a + b;
        System.out.println("add: " + add.apply(10, 20)); // 30

        BinaryOperator<String> concat = (s1, s2) -> s1 + " " + s2;
        System.out.println("concat: " + concat.apply("Hello", "World")); // Hello World

        // maxBy, minByで比較器を使った選択ができる
        BinaryOperator<Integer> max = BinaryOperator.maxBy(Integer::compareTo);
        System.out.println("max: " + max.apply(5, 10)); // 10

        BinaryOperator<String> min = BinaryOperator.minBy(String::compareTo);
        System.out.println("min: " + min.apply("apple", "banana")); // apple

        System.out.println();
    }

    // プリミティブ特殊化 - Int系の例
    private static void intPrimitiveExample() {
        System.out.println("--- プリミティブ特殊化 (Int系) ---");

        // IntConsumer - intを受け取り、戻り値なし
        IntConsumer printInt = i -> System.out.println("  IntConsumer: " + i);
        printInt.accept(42);

        // IntFunction<R> - intを受け取り、Rを返す
        IntFunction<String> intToString = i -> "Number: " + i;
        System.out.println("  IntFunction: " + intToString.apply(100));

        // IntPredicate - intを受け取り、booleanを返す
        IntPredicate isEven = i -> i % 2 == 0;
        System.out.println("  IntPredicate (4): " + isEven.test(4)); // true
        System.out.println("  IntPredicate (5): " + isEven.test(5)); // false

        // IntSupplier - 引数なしでintを返す
        IntSupplier randomInt = () -> (int)(Math.random() * 100);
        System.out.println("  IntSupplier: " + randomInt.getAsInt());

        // IntUnaryOperator - intを受け取り、intを返す
        IntUnaryOperator doubleIt = i -> i * 2;
        System.out.println("  IntUnaryOperator (5): " + doubleIt.applyAsInt(5)); // 10

        // IntBinaryOperator - 2つのintを受け取り、intを返す
        IntBinaryOperator sum = (a, b) -> a + b;
        System.out.println("  IntBinaryOperator (3, 7): " + sum.applyAsInt(3, 7)); // 10

        // LongやDoubleも同様に存在する
        // LongConsumer, LongFunction, LongPredicate, LongSupplier, LongUnaryOperator, LongBinaryOperator
        // DoubleConsumer, DoubleFunction, DoublePredicate, DoubleSupplier, DoubleUnaryOperator, DoubleBinaryOperator

        System.out.println();
    }

    // ToIntFunction, ToDoubleFunction, ToLongFunction の例
    private static void toIntFunctionExample() {
        System.out.println("--- ToXxxFunction ---");

        // ToIntFunction<T> - Tを受け取り、intを返す
        ToIntFunction<String> stringLength = s -> s.length();
        System.out.println("  ToIntFunction: " + stringLength.applyAsInt("Hello")); // 5

        // ToDoubleFunction<T> - Tを受け取り、doubleを返す
        ToDoubleFunction<Integer> intToDouble = i -> i * 1.5;
        System.out.println("  ToDoubleFunction: " + intToDouble.applyAsDouble(10)); // 15.0

        // ToLongFunction<T> - Tを受け取り、longを返す
        ToLongFunction<String> parseToLong = Long::parseLong;
        System.out.println("  ToLongFunction: " + parseToLong.applyAsLong("12345")); // 12345

        // ToIntBiFunction<T, U> - TとUを受け取り、intを返す
        ToIntBiFunction<String, String> combinedLength = (s1, s2) -> s1.length() + s2.length();
        System.out.println("  ToIntBiFunction: " + combinedLength.applyAsInt("Hello", "World")); // 10

        // ToDoubleBiFunction, ToLongBiFunctionも同様に存在する

        System.out.println();
    }

    // ObjIntConsumer, ObjDoubleConsumer, ObjLongConsumer の例
    private static void objIntConsumerExample() {
        System.out.println("--- ObjXxxConsumer ---");

        // ObjIntConsumer<T> - TとintのペアでmutableMapのような操作に便利
        ObjIntConsumer<String> printWithNumber = (str, num) ->
            System.out.println("  " + str + ": " + num);
        printWithNumber.accept("Count", 42);

        // ObjDoubleConsumer<T> - Tとdoubleを受け取る
        ObjDoubleConsumer<String> printPrice = (item, price) ->
            System.out.println("  " + item + " costs $" + price);
        printPrice.accept("Apple", 1.99);

        // ObjLongConsumer<T> - Tとlongを受け取る
        ObjLongConsumer<String> printTimestamp = (event, timestamp) ->
            System.out.println("  " + event + " at " + timestamp);
        printTimestamp.accept("Login", System.currentTimeMillis());

        System.out.println();
    }

    // Map操作との組み合わせ
    private static void mapOperationsExample() {
        System.out.println("--- Map操作との組み合わせ ---");

        Map<String, Integer> inventory = new HashMap<>();
        inventory.put("Apple", 10);
        inventory.put("Banana", 5);
        inventory.put("Orange", 8);

        System.out.println("初期在庫:");
        inventory.forEach((item, count) -> System.out.println("  " + item + ": " + count));

        // replaceAll - BiFunction<K, V, V> を使用して全ての値を変換
        System.out.println("\n全商品を2倍に:");
        inventory.replaceAll((item, count) -> count * 2);
        inventory.forEach((item, count) -> System.out.println("  " + item + ": " + count));

        // compute - 指定されたキーに対して計算を実行
        // BiFunction<K, V, V> を使用（既存の値がnullの場合もある）
        System.out.println("\nAppleに3を追加:");
        inventory.compute("Apple", (item, count) -> count == null ? 3 : count + 3);
        System.out.println("  Apple: " + inventory.get("Apple")); // 23

        // computeIfPresent - キーが存在する場合のみ計算
        System.out.println("\nBananaが存在すれば5を追加:");
        inventory.computeIfPresent("Banana", (item, count) -> count + 5);
        System.out.println("  Banana: " + inventory.get("Banana")); // 15

        // computeIfAbsent - キーが存在しない場合のみ計算（Function<K, V>）
        System.out.println("\nGrapeが存在しなければ初期値20を設定:");
        inventory.computeIfAbsent("Grape", item -> 20);
        System.out.println("  Grape: " + inventory.get("Grape")); // 20

        // merge - BiFunction<V, V, V> を使用して値をマージ
        // 第2引数は新しい値、BiFunctionは(oldValue, newValue) -> result
        System.out.println("\nOrangeに7をマージ（加算）:");
        inventory.merge("Orange", 7, (oldVal, newVal) -> oldVal + newVal);
        System.out.println("  Orange: " + inventory.get("Orange")); // 23

        // mergeで新しいキーを追加
        System.out.println("\nMelonを追加（存在しないので第2引数の値がそのまま使われる）:");
        inventory.merge("Melon", 12, (oldVal, newVal) -> oldVal + newVal);
        System.out.println("  Melon: " + inventory.get("Melon")); // 12

        System.out.println("\n最終在庫:");
        inventory.forEach((item, count) -> System.out.println("  " + item + ": " + count));

        // forEach - BiConsumer<K, V> の最も一般的な使用例
        System.out.println("\n合計在庫数:");
        final int[] total = {0}; // 実質的final変数を使う
        inventory.forEach((item, count) -> total[0] += count);
        System.out.println("  Total: " + total[0]);

        System.out.println();
    }
}
