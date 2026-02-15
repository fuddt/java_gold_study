package app;

import java.util.*;
import java.util.stream.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Parallel Stream 学習プログラム ===\n");

        // 1. Parallel Streamの作成方法
        createParallelStreams();

        // 2. forEach vs forEachOrdered
        forEachVsForEachOrdered();

        // 3. 順序保証の問題
        orderPreservationIssues();

        // 4. reduce()での並列処理（重要：combinerが使われる！）
        reduceWithParallelStreams();

        // 5. collect()での並列処理
        collectWithParallelStreams();

        // 6. findAny()の動作の違い
        findAnyBehavior();

        // 7. 状態を持つ操作は危険
        statefulOperationsWarning();

        // 8. パフォーマンスの考慮
        performanceConsiderations();

        // 9. 並列処理が逆効果になるケース
        whenNotToUseParallel();
    }

    // 1. Parallel Streamの作成方法
    static void createParallelStreams() {
        System.out.println("【1. Parallel Streamの作成方法】");

        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);

        // 方法1: parallelStream()を使う
        Stream<Integer> parallelStream1 = list.parallelStream();
        System.out.println("parallelStream()で作成: " + parallelStream1.isParallel());

        // 方法2: stream().parallel()を使う
        Stream<Integer> parallelStream2 = list.stream().parallel();
        System.out.println("stream().parallel()で作成: " + parallelStream2.isParallel());

        // 通常のstreamはシーケンシャル
        Stream<Integer> sequentialStream = list.stream();
        System.out.println("通常のstream(): " + sequentialStream.isParallel());

        // parallel()からsequential()に戻すこともできる
        Stream<Integer> backToSequential = list.parallelStream().sequential();
        System.out.println("parallel()→sequential(): " + backToSequential.isParallel());

        System.out.println();
    }

    // 2. forEach vs forEachOrdered
    static void forEachVsForEachOrdered() {
        System.out.println("【2. forEach vs forEachOrdered】");

        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // forEach: 順序が保証されない（並列処理）
        System.out.print("forEach（並列）: ");
        list.parallelStream().forEach(n -> System.out.print(n + " "));
        System.out.println();

        // forEachOrdered: 順序が保証される（但し並列のメリットが減る）
        System.out.print("forEachOrdered（順序保証）: ");
        list.parallelStream().forEachOrdered(n -> System.out.print(n + " "));
        System.out.println();

        // シーケンシャルストリームではforEachでも順序が保証される
        System.out.print("forEach（シーケンシャル）: ");
        list.stream().forEach(n -> System.out.print(n + " "));
        System.out.println("\n");
    }

    // 3. 順序保証の問題
    static void orderPreservationIssues() {
        System.out.println("【3. 順序保証の問題】");

        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // mapやfilterは順序を保持する（結果コレクションの順序）
        List<Integer> result1 = list.parallelStream()
            .map(n -> n * 2)
            .collect(Collectors.toList());
        System.out.println("map後のcollect（順序保持）: " + result1);

        // しかしforEachでの処理順序は保証されない
        System.out.print("map後のforEach（順序不定）: ");
        list.parallelStream()
            .map(n -> n * 2)
            .forEach(n -> System.out.print(n + " "));
        System.out.println("\n");
    }

    // 4. reduce()での並列処理（重要ポイント！）
    static void reduceWithParallelStreams() {
        System.out.println("【4. reduce()での並列処理】");

        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // シーケンシャルでのreduce: combinerは使われない
        System.out.println("--- シーケンシャルストリーム ---");
        int sum1 = list.stream().reduce(
            0,  // identity
            (a, b) -> {
                System.out.println("  accumulator: " + a + " + " + b);
                return a + b;
            },
            (a, b) -> {
                System.out.println("  combiner: " + a + " + " + b + " (呼ばれない)");
                return a + b;
            }
        );
        System.out.println("結果: " + sum1);
        System.out.println();

        // パラレルでのreduce: combinerが実際に使われる！
        System.out.println("--- パラレルストリーム ---");
        int sum2 = list.parallelStream().reduce(
            0,  // identity
            (a, b) -> {
                System.out.println("  accumulator: " + a + " + " + b + " [thread: " +
                    Thread.currentThread().getName() + "]");
                return a + b;
            },
            (a, b) -> {
                System.out.println("  combiner: " + a + " + " + b + " (部分結果の結合)");
                return a + b;
            }
        );
        System.out.println("結果: " + sum2);
        System.out.println();

        // combinerが正しくないと間違った結果になる例
        System.out.println("--- combinerが間違っている例 ---");
        int wrong = list.parallelStream().reduce(
            0,
            (a, b) -> a + b,
            (a, b) -> a - b  // 間違ったcombiner（足すべきなのに引いている）
        );
        System.out.println("間違った結果: " + wrong + " (正しくは55)");
        System.out.println();

        // 文字列の結合での例
        System.out.println("--- 文字列結合の例 ---");
        List<String> words = Arrays.asList("Java", "Gold", "Parallel", "Stream");
        String result = words.parallelStream().reduce(
            "",  // identity
            (s1, s2) -> {
                System.out.println("  accumulator: \"" + s1 + "\" + \"" + s2 + "\"");
                return s1 + s2;
            },
            (s1, s2) -> {
                System.out.println("  combiner: \"" + s1 + "\" + \"" + s2 + "\"");
                return s1 + s2;
            }
        );
        System.out.println("結果: " + result);
        System.out.println();
    }

    // 5. collect()での並列処理
    static void collectWithParallelStreams() {
        System.out.println("【5. collect()での並列処理】");

        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // Collectors.toList()は並列処理に対応している
        List<Integer> result1 = list.parallelStream()
            .map(n -> n * 2)
            .collect(Collectors.toList());
        System.out.println("toList(): " + result1);

        // Collectors.groupingByも並列処理に対応
        Map<Boolean, List<Integer>> result2 = list.parallelStream()
            .collect(Collectors.groupingBy(n -> n % 2 == 0));
        System.out.println("groupingBy（偶数/奇数）: " + result2);

        // Collectors.summingIntも安全
        int sum = list.parallelStream()
            .collect(Collectors.summingInt(Integer::intValue));
        System.out.println("summingInt: " + sum);

        System.out.println();
    }

    // 6. findAny()の動作の違い
    static void findAnyBehavior() {
        System.out.println("【6. findAny()の動作の違い】");

        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // シーケンシャル: findAny()でも通常は最初の要素を返す
        System.out.println("シーケンシャルでのfindAny()を5回実行:");
        for (int i = 0; i < 5; i++) {
            Optional<Integer> result = list.stream()
                .filter(n -> n > 3)
                .findAny();
            System.out.println("  " + result.orElse(-1));
        }

        // パラレル: findAny()は本当に「どれか」を返す（実行ごとに変わる可能性）
        System.out.println("パラレルでのfindAny()を5回実行:");
        for (int i = 0; i < 5; i++) {
            Optional<Integer> result = list.parallelStream()
                .filter(n -> n > 3)
                .findAny();
            System.out.println("  " + result.orElse(-1));
        }

        // findFirst()は並列でも最初の要素を返す
        System.out.println("パラレルでのfindFirst()を5回実行:");
        for (int i = 0; i < 5; i++) {
            Optional<Integer> result = list.parallelStream()
                .filter(n -> n > 3)
                .findFirst();
            System.out.println("  " + result.orElse(-1));
        }

        System.out.println();
    }

    // 7. 状態を持つ操作は危険（アンチパターン）
    static void statefulOperationsWarning() {
        System.out.println("【7. 状態を持つ操作は危険】");

        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // NG例1: 外部変数への書き込み（スレッドセーフでない）
        System.out.println("--- NG例: 外部変数への書き込み ---");
        List<Integer> resultList = new ArrayList<>();  // スレッドセーフでない！
        list.parallelStream()
            .forEach(n -> resultList.add(n * 2));  // 危険！
        System.out.println("結果の要素数: " + resultList.size() + " (10になるべきだが...)");
        System.out.println("実際: " + resultList);
        System.out.println("※ データ競合により要素が失われる可能性がある");
        System.out.println();

        // OK例: collect()を使う
        System.out.println("--- OK例: collect()を使う ---");
        List<Integer> safeResult = list.parallelStream()
            .map(n -> n * 2)
            .collect(Collectors.toList());  // スレッドセーフ
        System.out.println("結果の要素数: " + safeResult.size());
        System.out.println("実際: " + safeResult);
        System.out.println();

        // NG例2: 順序依存の操作
        System.out.println("--- NG例: 順序依存の操作 ---");
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= 10; i++) numbers.add(i);

        // skip()やlimit()は並列処理でパフォーマンスが悪化
        List<Integer> limited = numbers.parallelStream()
            .limit(5)
            .collect(Collectors.toList());
        System.out.println("limit(5)の結果: " + limited + " (順序は保証されるが遅い)");
        System.out.println();
    }

    // 8. パフォーマンスの考慮
    static void performanceConsiderations() {
        System.out.println("【8. パフォーマンスの考慮】");

        // 小さいデータセット: 並列化のオーバーヘッドが大きい
        System.out.println("--- 小さいデータセット（10要素）---");
        List<Integer> smallList = IntStream.range(1, 11)
            .boxed()
            .collect(Collectors.toList());

        long start1 = System.nanoTime();
        int sum1 = smallList.stream()
            .map(n -> n * 2)
            .reduce(0, Integer::sum);
        long time1 = System.nanoTime() - start1;

        long start2 = System.nanoTime();
        int sum2 = smallList.parallelStream()
            .map(n -> n * 2)
            .reduce(0, Integer::sum);
        long time2 = System.nanoTime() - start2;

        System.out.println("シーケンシャル: " + time1 + " ns");
        System.out.println("パラレル: " + time2 + " ns");
        System.out.println("※ 小さいデータではパラレルが遅い場合が多い");
        System.out.println();

        // 大きいデータセット: 並列化のメリットが出る
        System.out.println("--- 大きいデータセット（100万要素）---");
        List<Integer> largeList = IntStream.range(1, 1_000_001)
            .boxed()
            .collect(Collectors.toList());

        long start3 = System.nanoTime();
        long sum3 = largeList.stream()
            .mapToLong(n -> n * 2L)
            .sum();
        long time3 = System.nanoTime() - start3;

        long start4 = System.nanoTime();
        long sum4 = largeList.parallelStream()
            .mapToLong(n -> n * 2L)
            .sum();
        long time4 = System.nanoTime() - start4;

        System.out.println("シーケンシャル: " + time3 / 1_000_000 + " ms");
        System.out.println("パラレル: " + time4 / 1_000_000 + " ms");
        System.out.println("高速化率: " + String.format("%.2f", (double)time3 / time4) + "倍");
        System.out.println();
    }

    // 9. 並列処理が逆効果になるケース
    static void whenNotToUseParallel() {
        System.out.println("【9. 並列処理が逆効果になるケース】");

        System.out.println("以下の場合は並列処理を使うべきでない:");
        System.out.println("1. データ量が少ない（数百要素以下）");
        System.out.println("2. 処理が軽い（単純な計算のみ）");
        System.out.println("3. 順序が重要（limit, skip, findFirst等）");
        System.out.println("4. 状態を変更する操作がある");
        System.out.println("5. スレッドセーフでないコレクションを使う");
        System.out.println("6. I/O処理が含まれる（ファイル読み書き等）");
        System.out.println();

        // 例: I/O処理がある場合（シミュレーション）
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);

        System.out.println("--- I/O処理のシミュレーション ---");
        long start1 = System.nanoTime();
        list.stream()
            .map(n -> {
                try { Thread.sleep(10); } catch (InterruptedException e) {}
                return n * 2;
            })
            .collect(Collectors.toList());
        long time1 = System.nanoTime() - start1;

        long start2 = System.nanoTime();
        list.parallelStream()
            .map(n -> {
                try { Thread.sleep(10); } catch (InterruptedException e) {}
                return n * 2;
            })
            .collect(Collectors.toList());
        long time2 = System.nanoTime() - start2;

        System.out.println("シーケンシャル: " + time1 / 1_000_000 + " ms");
        System.out.println("パラレル: " + time2 / 1_000_000 + " ms");
        System.out.println("※ I/O待ちがある場合は並列化でも高速化される");
        System.out.println();

        // 良い使い方の例
        System.out.println("【並列処理が効果的な例】");
        System.out.println("- 大量のデータ（数千〜数百万要素）");
        System.out.println("- CPU負荷の高い処理（複雑な計算、暗号化等）");
        System.out.println("- 独立した要素の処理（副作用なし）");
        System.out.println("- map, filter, reduce等のステートレス操作");

        System.out.println("\n=== プログラム終了 ===");
    }
}
