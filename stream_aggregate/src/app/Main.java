package app;

import java.util.*;
import java.util.stream.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Stream 集約操作のサンプル ===\n");

        // ========================================
        // 1. count() - 要素数をカウント
        // ========================================
        System.out.println("【1. count()】");
        List<String> fruits = Arrays.asList("apple", "banana", "cherry", "date");
        long count = fruits.stream().count();
        System.out.println("果物の数: " + count); // 4

        long countFiltered = fruits.stream()
            .filter(s -> s.length() > 5)
            .count();
        System.out.println("5文字超の果物: " + countFiltered); // 2 (banana, cherry)
        System.out.println();

        // ========================================
        // 2. max() / min() - Comparatorを使って最大/最小を取得
        // 戻り値はOptional<T>
        // ========================================
        System.out.println("【2. max() / min()】");
        List<Integer> numbers = Arrays.asList(5, 2, 8, 1, 9, 3);

        Optional<Integer> max = numbers.stream().max(Comparator.naturalOrder());
        System.out.println("最大値: " + max.orElse(0)); // 9

        Optional<Integer> min = numbers.stream().min(Comparator.naturalOrder());
        System.out.println("最小値: " + min.orElse(0)); // 1

        // 文字列の長さで比較
        Optional<String> longest = fruits.stream()
            .max(Comparator.comparing(String::length));
        System.out.println("最長の果物: " + longest.orElse("なし")); // banana or cherry
        System.out.println();

        // ========================================
        // 3. IntStream, LongStream, DoubleStream
        // プリミティブ型専用のStream
        // ========================================
        System.out.println("【3. プリミティブストリーム】");

        // IntStream の生成
        IntStream intStream1 = IntStream.of(1, 2, 3, 4, 5);
        System.out.println("IntStream.of: " + intStream1.sum()); // 15

        IntStream intStream2 = IntStream.range(1, 5); // 1,2,3,4 (5は含まない)
        System.out.println("IntStream.range(1,5): " + intStream2.sum()); // 10

        IntStream intStream3 = IntStream.rangeClosed(1, 5); // 1,2,3,4,5 (5を含む)
        System.out.println("IntStream.rangeClosed(1,5): " + intStream3.sum()); // 15

        // LongStream
        LongStream longStream = LongStream.of(100L, 200L, 300L);
        System.out.println("LongStream.sum: " + longStream.sum()); // 600

        // DoubleStream
        DoubleStream doubleStream = DoubleStream.of(1.5, 2.5, 3.5);
        System.out.println("DoubleStream.sum: " + doubleStream.sum()); // 7.5
        System.out.println();

        // ========================================
        // 4. sum() / average() on プリミティブストリーム
        // sum() -> int/long/double
        // average() -> OptionalDouble (要素がない場合があるため)
        // ========================================
        System.out.println("【4. sum() / average()】");

        IntStream scores = IntStream.of(80, 90, 75, 85, 95);
        int total = scores.sum();
        System.out.println("合計点: " + total); // 425

        // average()はOptionalDoubleを返す
        IntStream scores2 = IntStream.of(80, 90, 75, 85, 95);
        OptionalDouble average = scores2.average();
        System.out.println("平均点: " + average.orElse(0.0)); // 85.0

        // 空のストリームの場合
        OptionalDouble emptyAverage = IntStream.empty().average();
        System.out.println("空ストリームの平均: " + emptyAverage.orElse(0.0)); // 0.0
        System.out.println();

        // ========================================
        // 5. mapToInt() / mapToLong() / mapToDouble()
        // Stream<T>をプリミティブストリームに変換
        // ========================================
        System.out.println("【5. mapToXxx()でプリミティブストリーム変換】");

        List<String> words = Arrays.asList("Java", "Stream", "API");

        // Stream<String> -> IntStream (文字列長)
        int totalLength = words.stream()
            .mapToInt(String::length)
            .sum();
        System.out.println("全文字列の長さ合計: " + totalLength); // 4+6+3=13

        // Stream<Integer> -> IntStream
        List<Integer> prices = Arrays.asList(100, 200, 300, 400);
        double avgPrice = prices.stream()
            .mapToInt(Integer::intValue) // またはi -> i
            .average()
            .orElse(0.0);
        System.out.println("平均価格: " + avgPrice); // 250.0

        // mapToLong()
        long sumLong = words.stream()
            .mapToLong(s -> s.length() * 100L)
            .sum();
        System.out.println("長さ×100の合計: " + sumLong); // 1300

        // mapToDouble()
        double sumDouble = numbers.stream()
            .mapToDouble(n -> n * 1.5)
            .sum();
        System.out.println("1.5倍の合計: " + sumDouble);
        System.out.println();

        // ========================================
        // 6. max() / min() on プリミティブストリーム
        // 戻り値はOptionalInt/OptionalLong/OptionalDouble
        // ========================================
        System.out.println("【6. プリミティブストリームのmax/min】");

        OptionalInt maxInt = IntStream.of(5, 2, 8, 1, 9).max();
        System.out.println("IntStream.max: " + maxInt.orElse(0)); // 9

        OptionalInt minInt = IntStream.of(5, 2, 8, 1, 9).min();
        System.out.println("IntStream.min: " + minInt.orElse(0)); // 1

        OptionalDouble maxDouble = DoubleStream.of(1.5, 2.8, 1.2).max();
        System.out.println("DoubleStream.max: " + maxDouble.orElse(0.0)); // 2.8
        System.out.println();

        // ========================================
        // 7. summaryStatistics()
        // count, sum, min, average, maxを一度に取得
        // ========================================
        System.out.println("【7. summaryStatistics()】");

        IntStream testScores = IntStream.of(80, 90, 75, 85, 95, 70);
        IntSummaryStatistics stats = testScores.summaryStatistics();

        System.out.println("件数: " + stats.getCount());      // 6
        System.out.println("合計: " + stats.getSum());        // 495
        System.out.println("平均: " + stats.getAverage());    // 82.5
        System.out.println("最小: " + stats.getMin());        // 70
        System.out.println("最大: " + stats.getMax());        // 95

        // LongSummaryStatistics, DoubleSummaryStatistics も同様に使える
        DoubleSummaryStatistics doubleStats = DoubleStream.of(1.5, 2.5, 3.5)
            .summaryStatistics();
        System.out.println("Double平均: " + doubleStats.getAverage()); // 2.5
        System.out.println();

        // ========================================
        // 8. reduce() - 汎用的な集約操作
        // reduce(identity, accumulator)
        // reduce(accumulator) -> Optional<T>
        // reduce(identity, accumulator, combiner) - 並列ストリーム用
        // ========================================
        System.out.println("【8. reduce()】");

        // 8-1. reduce(identity, accumulator)
        // identity: 初期値
        // accumulator: (累積値, 要素) -> 新しい累積値
        List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5);

        int sum = nums.stream()
            .reduce(0, (acc, n) -> acc + n);
        System.out.println("合計(reduce): " + sum); // 15

        int product = nums.stream()
            .reduce(1, (acc, n) -> acc * n);
        System.out.println("積(reduce): " + product); // 120

        // 8-2. reduce(accumulator) - 初期値なし、戻り値はOptional
        Optional<Integer> sumOpt = nums.stream()
            .reduce((a, b) -> a + b);
        System.out.println("合計(Optional): " + sumOpt.orElse(0)); // 15

        // 空のストリームの場合
        Optional<Integer> emptySum = Stream.<Integer>empty()
            .reduce((a, b) -> a + b);
        System.out.println("空ストリーム: " + emptySum.orElse(0)); // 0

        // 8-3. 文字列の連結
        List<String> strs = Arrays.asList("Java", "Gold", "Stream");
        String concatenated = strs.stream()
            .reduce("", (acc, s) -> acc + s);
        System.out.println("連結: " + concatenated); // JavaGoldStream

        String withComma = strs.stream()
            .reduce("", (acc, s) -> acc.isEmpty() ? s : acc + "," + s);
        System.out.println("カンマ区切り: " + withComma); // Java,Gold,Stream

        // 8-4. reduce(identity, accumulator, combiner)
        // combinerは並列ストリームで複数のスレッドの結果を結合する際に使用
        int parallelSum = nums.parallelStream()
            .reduce(
                0,                      // identity
                (acc, n) -> acc + n,   // accumulator
                (acc1, acc2) -> acc1 + acc2  // combiner
            );
        System.out.println("並列合計: " + parallelSum); // 15

        // combinerの動作を確認
        System.out.println("\n--- combinerの動作確認 ---");
        int result = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8).parallelStream()
            .reduce(
                0,
                (acc, n) -> {
                    System.out.println("  accumulator: " + acc + " + " + n + " = " + (acc + n));
                    return acc + n;
                },
                (acc1, acc2) -> {
                    System.out.println("  combiner: " + acc1 + " + " + acc2 + " = " + (acc1 + acc2));
                    return acc1 + acc2;
                }
            );
        System.out.println("結果: " + result);
        System.out.println();

        // ========================================
        // 9. Stream<Integer> vs IntStream の違い
        // ========================================
        System.out.println("【9. Stream<Integer> vs IntStream】");

        // Stream<Integer> - ボクシング型のストリーム
        Stream<Integer> boxedStream = Stream.of(1, 2, 3, 4, 5);
        // sum()メソッドは存在しない！
        // int sum1 = boxedStream.sum(); // コンパイルエラー

        // mapToInt()でIntStreamに変換する必要がある
        int sum1 = Stream.of(1, 2, 3, 4, 5)
            .mapToInt(Integer::intValue)
            .sum();
        System.out.println("Stream<Integer>->IntStream: " + sum1);

        // IntStream - プリミティブint型のストリーム
        IntStream primitiveStream = IntStream.of(1, 2, 3, 4, 5);
        int sum2 = primitiveStream.sum(); // sum()が直接使える
        System.out.println("IntStream: " + sum2);

        // IntStream -> Stream<Integer> (boxed())
        Stream<Integer> boxed = IntStream.of(1, 2, 3, 4, 5).boxed();
        List<Integer> list = boxed.collect(Collectors.toList());
        System.out.println("boxed: " + list);
        System.out.println();

        // ========================================
        // 10. 試験対策: よくある引っかけ
        // ========================================
        System.out.println("【10. 試験対策ポイント】");

        // ポイント1: average()の戻り値はOptionalDouble
        IntStream s1 = IntStream.of(10, 20, 30);
        OptionalDouble avg = s1.average(); // double型ではない！
        System.out.println("average戻り値: OptionalDouble = " + avg.getAsDouble());

        // ポイント2: max()/min()はComparatorが必要(プリミティブストリームは不要)
        Stream<Integer> s2 = Stream.of(10, 20, 30);
        Optional<Integer> max2 = s2.max(Comparator.naturalOrder()); // Comparator必須
        System.out.println("Stream<T>.max: " + max2.get());

        IntStream s3 = IntStream.of(10, 20, 30);
        OptionalInt max3 = s3.max(); // Comparator不要
        System.out.println("IntStream.max: " + max3.getAsInt());

        // ポイント3: reduce()の初期値なしバージョンはOptionalを返す
        Optional<Integer> reduced = Stream.of(1, 2, 3)
            .reduce((a, b) -> a + b); // Optional<Integer>
        System.out.println("reduce(accumulator): Optional = " + reduced.get());

        // ポイント4: sum()はプリミティブストリームのみ
        // Stream<Integer>.sum() // コンパイルエラー
        // IntStream.sum() // OK

        // ポイント5: summaryStatisticsはプリミティブストリームのみ
        IntSummaryStatistics stats2 = IntStream.of(1, 2, 3).summaryStatistics();
        System.out.println("summaryStatistics: count=" + stats2.getCount());

        System.out.println("\n=== 完了 ===");
    }
}
