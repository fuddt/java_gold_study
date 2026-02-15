package app;

import java.util.*;
import java.util.stream.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Stream中間操作のサンプル ===\n");

        // 1. map() - 要素の変換
        System.out.println("【1. map() - 要素の変換】");
        List<String> names = Arrays.asList("apple", "banana", "cherry");
        List<String> upperNames = names.stream()
            .map(String::toUpperCase)  // 各要素を大文字に変換
            .collect(Collectors.toList());
        System.out.println("元のリスト: " + names);
        System.out.println("大文字変換: " + upperNames);

        // 数値の変換例
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> squared = numbers.stream()
            .map(n -> n * n)  // 各要素を二乗
            .collect(Collectors.toList());
        System.out.println("元の数値: " + numbers);
        System.out.println("二乗した値: " + squared + "\n");

        // 2. peek() - デバッグ用の中間処理
        System.out.println("【2. peek() - デバッグ用】");
        System.out.println("※重要: peekは終端操作がないと実行されない！");

        // 正しい使い方（終端操作あり）
        List<String> result = Stream.of("a", "b", "c")
            .peek(s -> System.out.println("処理中: " + s))  // デバッグ用
            .map(String::toUpperCase)
            .peek(s -> System.out.println("変換後: " + s))  // 変換後の確認
            .collect(Collectors.toList());
        System.out.println("最終結果: " + result);

        // 間違った使い方（終端操作なし - 何も実行されない）
        System.out.println("\n終端操作がない場合:");
        Stream.of("x", "y", "z")
            .peek(s -> System.out.println("これは表示されない: " + s));  // 実行されない！
        System.out.println("↑何も表示されていないことに注目\n");

        // 3. flatMap() - ネストした構造の平坦化
        System.out.println("【3. flatMap() - ネストした構造の平坦化】");

        // List<List<String>>をList<String>に変換
        List<List<String>> nestedList = Arrays.asList(
            Arrays.asList("a", "b"),
            Arrays.asList("c", "d"),
            Arrays.asList("e", "f")
        );
        System.out.println("ネストしたリスト: " + nestedList);

        List<String> flatList = nestedList.stream()
            .flatMap(list -> list.stream())  // 各リストをStreamに変換して平坦化
            .collect(Collectors.toList());
        System.out.println("平坦化したリスト: " + flatList);

        // 文字列を文字のStreamに分解する例
        List<String> words = Arrays.asList("hello", "world");
        List<String> chars = words.stream()
            .flatMap(word -> Arrays.stream(word.split("")))  // 各文字に分解
            .collect(Collectors.toList());
        System.out.println("文字列リスト: " + words);
        System.out.println("全文字: " + chars);

        // mapとflatMapの違い
        System.out.println("\n【map vs flatMap の違い】");
        List<String> fruits = Arrays.asList("apple", "banana");

        // mapを使った場合 - Stream<Stream<String>>になってしまう（コンパイルエラー回避のためコメント）
        // Stream<Stream<String>> mapResult = fruits.stream()
        //     .map(s -> Arrays.stream(s.split("")));

        // flatMapを使った場合 - Stream<String>になる
        List<String> flatMapResult = fruits.stream()
            .flatMap(s -> Arrays.stream(s.split("")))
            .collect(Collectors.toList());
        System.out.println("flatMapで文字分解: " + flatMapResult + "\n");

        // 4. filter() - 要素の絞り込み
        System.out.println("【4. filter() - 要素の絞り込み】");
        List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<Integer> evenNums = nums.stream()
            .filter(n -> n % 2 == 0)  // 偶数のみ
            .collect(Collectors.toList());
        System.out.println("元のリスト: " + nums);
        System.out.println("偶数のみ: " + evenNums);

        // 複数の条件を組み合わせ
        List<Integer> filtered = nums.stream()
            .filter(n -> n > 3)        // 3より大きい
            .filter(n -> n % 2 != 0)   // かつ奇数
            .collect(Collectors.toList());
        System.out.println("3より大きい奇数: " + filtered + "\n");

        // 5. sorted() - ソート
        System.out.println("【5. sorted() - ソート】");

        // 自然順序でのソート
        List<Integer> unsorted = Arrays.asList(5, 2, 8, 1, 9, 3);
        List<Integer> sortedAsc = unsorted.stream()
            .sorted()  // 自然順序（昇順）
            .collect(Collectors.toList());
        System.out.println("元のリスト: " + unsorted);
        System.out.println("昇順ソート: " + sortedAsc);

        // Comparatorを使った降順ソート
        List<Integer> sortedDesc = unsorted.stream()
            .sorted(Comparator.reverseOrder())  // 降順
            .collect(Collectors.toList());
        System.out.println("降順ソート: " + sortedDesc);

        // 文字列の長さでソート
        List<String> words2 = Arrays.asList("apple", "pie", "banana", "cherry");
        List<String> sortedByLength = words2.stream()
            .sorted(Comparator.comparing(String::length))  // 長さでソート
            .collect(Collectors.toList());
        System.out.println("文字列リスト: " + words2);
        System.out.println("長さでソート: " + sortedByLength);

        // 複数条件でのソート
        List<String> sortedMulti = words2.stream()
            .sorted(Comparator.comparing(String::length)
                   .thenComparing(Comparator.naturalOrder()))  // 長さ→辞書順
            .collect(Collectors.toList());
        System.out.println("長さ→辞書順: " + sortedMulti + "\n");

        // 6. distinct() - 重複除去
        System.out.println("【6. distinct() - 重複除去】");
        List<Integer> duplicates = Arrays.asList(1, 2, 2, 3, 3, 3, 4, 5, 5);
        List<Integer> unique = duplicates.stream()
            .distinct()  // 重複を除去（equalsで判定）
            .collect(Collectors.toList());
        System.out.println("重複あり: " + duplicates);
        System.out.println("重複除去: " + unique + "\n");

        // 7. limit() - 先頭からN個取得
        System.out.println("【7. limit() - 先頭からN個取得】");
        List<Integer> limited = Stream.iterate(1, n -> n + 1)  // 無限ストリーム
            .limit(5)  // 最初の5個だけ取得
            .collect(Collectors.toList());
        System.out.println("最初の5個: " + limited + "\n");

        // 8. skip() - 先頭からN個スキップ
        System.out.println("【8. skip() - 先頭からN個スキップ】");
        List<Integer> range = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<Integer> skipped = range.stream()
            .skip(5)  // 最初の5個をスキップ
            .collect(Collectors.toList());
        System.out.println("元のリスト: " + range);
        System.out.println("最初の5個をスキップ: " + skipped);

        // skipとlimitの組み合わせ（ページネーション）
        List<Integer> page2 = range.stream()
            .skip(3)   // 最初の3個をスキップ
            .limit(3)  // その後3個取得
            .collect(Collectors.toList());
        System.out.println("3個スキップして3個取得: " + page2 + "\n");

        // 9. 複合例 - 実践的な使い方
        System.out.println("【9. 複合例 - すべてを組み合わせる】");
        List<String> data = Arrays.asList(
            "apple", "BANANA", "cherry", "APPLE", "date", "banana", "elderberry"
        );

        List<String> processed = data.stream()
            .peek(s -> System.out.println("  入力: " + s))
            .map(String::toLowerCase)        // 小文字に統一
            .peek(s -> System.out.println("  小文字化: " + s))
            .distinct()                      // 重複除去
            .peek(s -> System.out.println("  重複除去後: " + s))
            .filter(s -> s.length() > 4)     // 5文字以上のみ
            .peek(s -> System.out.println("  フィルタ後: " + s))
            .sorted()                        // ソート
            .peek(s -> System.out.println("  ソート後: " + s))
            .collect(Collectors.toList());

        System.out.println("最終結果: " + processed + "\n");

        // 10. よくある間違い・試験ポイント
        System.out.println("【10. 試験でよく出る注意点】");

        // 注意1: 中間操作は遅延評価
        System.out.println("注意1: 中間操作は遅延評価される");
        Stream<String> stream1 = Stream.of("a", "b", "c")
            .map(s -> {
                System.out.println("  map実行: " + s);
                return s.toUpperCase();
            });
        System.out.println("ここまでmapは実行されていない");
        stream1.forEach(s -> System.out.println("  結果: " + s));  // ここで初めて実行される

        // 注意2: Streamは再利用できない
        System.out.println("\n注意2: Streamは再利用できない");
        Stream<String> stream2 = Stream.of("x", "y", "z");
        stream2.forEach(System.out::println);
        try {
            stream2.forEach(System.out::println);  // IllegalStateException
        } catch (IllegalStateException e) {
            System.out.println("エラー: " + e.getMessage());
        }

        // 注意3: sortedはステートフル操作（全要素を保持する必要がある）
        System.out.println("\n注意3: sortedは全要素を見る必要がある（ステートフル）");
        System.out.println("limitの前後でsortedの位置が重要:");
        List<Integer> nums2 = Arrays.asList(5, 2, 8, 1, 9, 3, 7, 4, 6);

        // sortedの後にlimit - 全体をソートしてから最初の3個
        List<Integer> sortThenLimit = nums2.stream()
            .sorted()
            .limit(3)
            .collect(Collectors.toList());
        System.out.println("sorted→limit: " + sortThenLimit + " (ソート後の先頭3個)");

        // limitの後にsorted - 最初の3個だけソート
        List<Integer> limitThenSort = nums2.stream()
            .limit(3)
            .sorted()
            .collect(Collectors.toList());
        System.out.println("limit→sorted: " + limitThenSort + " (元の先頭3個をソート)");

        System.out.println("\n=== 完了 ===");
    }
}
