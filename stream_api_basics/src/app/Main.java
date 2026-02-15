package app;

import java.util.*;
import java.util.stream.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Stream API 基本編 ===\n");

        // ===== 1. Streamパイプラインの概念 =====
        System.out.println("【1. Streamパイプラインの基本構造】");
        // パイプライン: ソース → 中間操作 → 終端操作
        List<String> fruits = Arrays.asList("apple", "banana", "cherry", "date");

        // 完全なパイプラインの例
        long count = fruits.stream()          // ソース：Streamの生成
                .filter(s -> s.length() > 5)  // 中間操作：フィルタリング
                .map(String::toUpperCase)     // 中間操作：変換
                .peek(System.out::println)    // 中間操作：副作用（確認用）
                .count();                     // 終端操作：結果を取得
        System.out.println("結果件数: " + count + "\n");


        // ===== 2. 様々なStream生成方法 =====
        System.out.println("【2. Stream生成方法】");

        // 2-1. Stream.of() - 可変長引数から生成
        System.out.println("■ Stream.of()");
        Stream<String> stream1 = Stream.of("Java", "Python", "JavaScript");
        stream1.forEach(s -> System.out.println("  " + s));

        // 2-2. Collection.stream() - コレクションから生成
        System.out.println("\n■ Collection.stream()");
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        numbers.stream()
                .map(n -> n * 2)
                .forEach(n -> System.out.println("  " + n));

        // 2-3. Arrays.stream() - 配列から生成
        System.out.println("\n■ Arrays.stream()");
        String[] array = {"Tokyo", "Osaka", "Kyoto"};
        Arrays.stream(array)
                .forEach(s -> System.out.println("  " + s));

        // 配列の一部からStreamを生成（開始インデックス, 終了インデックス）
        System.out.println("\n■ Arrays.stream() - 範囲指定");
        int[] intArray = {10, 20, 30, 40, 50};
        Arrays.stream(intArray, 1, 4)  // インデックス1〜3（4は含まない）
                .forEach(n -> System.out.println("  " + n));

        // 2-4. Stream.generate() - 無限ストリーム（Supplierを使用）
        System.out.println("\n■ Stream.generate() - 無限ストリーム");
        // limitで制限しないと無限に生成される！
        Stream.generate(() -> "Hello")
                .limit(3)  // 最初の3つだけ取得
                .forEach(s -> System.out.println("  " + s));

        // ランダムな値を生成
        System.out.println("\n■ Stream.generate() - ランダム値");
        Stream.generate(Math::random)
                .limit(3)
                .forEach(n -> System.out.println("  " + n));

        // 2-5. Stream.iterate() - 初期値と関数で無限ストリーム生成
        System.out.println("\n■ Stream.iterate() - 連続値生成");
        // iterate(初期値, 次の値を生成する関数)
        Stream.iterate(1, n -> n + 1)  // 1, 2, 3, 4, ...
                .limit(5)
                .forEach(n -> System.out.println("  " + n));

        // Java 9以降：条件付きiterate
        System.out.println("\n■ Stream.iterate() - 条件付き（Java 9+）");
        Stream.iterate(1, n -> n <= 5, n -> n + 1)  // 条件を満たす間だけ生成
                .forEach(n -> System.out.println("  " + n));

        // 2-6. IntStream.range() / rangeClosed()
        System.out.println("\n■ IntStream.range() - 終端を含まない");
        IntStream.range(1, 5)  // 1, 2, 3, 4（5は含まない）
                .forEach(n -> System.out.println("  " + n));

        System.out.println("\n■ IntStream.rangeClosed() - 終端を含む");
        IntStream.rangeClosed(1, 5)  // 1, 2, 3, 4, 5（5も含む）
                .forEach(n -> System.out.println("  " + n));

        // 2-7. IntStream, LongStream, DoubleStream
        System.out.println("\n■ プリミティブ型特化Stream");
        IntStream.of(10, 20, 30)
                .average()  // プリミティブ型Streamは統計メソッドが豊富
                .ifPresent(avg -> System.out.println("  平均値: " + avg));


        // ===== 3. Streamの遅延評価 =====
        System.out.println("\n【3. Streamの遅延評価】");
        System.out.println("■ 中間操作だけでは実行されない例");
        Stream<String> lazyStream = Stream.of("a", "b", "c")
                .filter(s -> {
                    System.out.println("  filterが実行された: " + s);
                    return true;
                })
                .map(s -> {
                    System.out.println("  mapが実行された: " + s);
                    return s.toUpperCase();
                });
        System.out.println("→ まだ何も実行されていない！");

        System.out.println("\n■ 終端操作を呼ぶと実行される");
        lazyStream.forEach(s -> System.out.println("  結果: " + s));


        // ===== 4. Streamは使い捨て（再利用不可） =====
        System.out.println("\n【4. Streamは使い捨て】");
        Stream<String> singleUseStream = Stream.of("one", "two", "three");

        // 1回目の使用
        System.out.println("■ 1回目の使用:");
        singleUseStream.forEach(s -> System.out.println("  " + s));

        // 2回目の使用を試みる → 例外が発生！
        System.out.println("\n■ 2回目の使用を試みる:");
        try {
            singleUseStream.forEach(s -> System.out.println("  " + s));
        } catch (IllegalStateException e) {
            System.out.println("  ❌ エラー: " + e.getClass().getSimpleName());
            System.out.println("  メッセージ: " + e.getMessage());
        }

        System.out.println("\n■ 再度使いたい場合は新しいStreamを生成する必要がある");
        List<String> data = Arrays.asList("one", "two", "three");
        data.stream().forEach(s -> System.out.println("  1回目: " + s));
        data.stream().forEach(s -> System.out.println("  2回目: " + s));  // OK!


        // ===== 5. Stream操作の分類 =====
        System.out.println("\n【5. Stream操作の分類】");

        // 中間操作の例（Streamを返す）
        System.out.println("■ 中間操作（複数連鎖可能）:");
        System.out.println("  - filter(): 条件でフィルタリング");
        System.out.println("  - map(): 要素を変換");
        System.out.println("  - flatMap(): 要素を平坦化");
        System.out.println("  - distinct(): 重複を除去");
        System.out.println("  - sorted(): ソート");
        System.out.println("  - peek(): 各要素に副作用を実行（デバッグ用）");
        System.out.println("  - limit(): 要素数を制限");
        System.out.println("  - skip(): 先頭n個をスキップ");

        // 終端操作の例（Streamを返さない）
        System.out.println("\n■ 終端操作（パイプラインの終わり）:");
        System.out.println("  - forEach(): 各要素に処理を実行");
        System.out.println("  - count(): 要素数をカウント");
        System.out.println("  - collect(): コレクションに収集");
        System.out.println("  - reduce(): 要素を集約");
        System.out.println("  - anyMatch()/allMatch()/noneMatch(): 条件チェック");
        System.out.println("  - findFirst()/findAny(): 要素を検索");
        System.out.println("  - min()/max(): 最小値/最大値");


        // ===== 6. 実践的な例 =====
        System.out.println("\n【6. 実践的な例】");
        List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        System.out.println("■ 偶数のみを2倍にして合計を求める");
        int sum = nums.stream()
                .filter(n -> n % 2 == 0)      // 偶数のみ
                .map(n -> n * 2)              // 2倍
                .reduce(0, Integer::sum);     // 合計
        System.out.println("  結果: " + sum);

        System.out.println("\n■ プリミティブStreamで統計情報を取得");
        IntSummaryStatistics stats = nums.stream()
                .mapToInt(Integer::intValue)
                .summaryStatistics();
        System.out.println("  件数: " + stats.getCount());
        System.out.println("  合計: " + stats.getSum());
        System.out.println("  平均: " + stats.getAverage());
        System.out.println("  最小: " + stats.getMin());
        System.out.println("  最大: " + stats.getMax());

        System.out.println("\n=== 完了 ===");
    }
}
