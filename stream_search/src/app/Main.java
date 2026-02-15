package app;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Stream 検索・マッチング操作のデモ ===\n");

        // ===== findFirst() と findAny() =====
        demonstrateFindOperations();

        // ===== match系メソッド =====
        demonstrateMatchOperations();

        // ===== 短絡評価の動作確認 =====
        demonstrateShortCircuit();

        // ===== 空ストリームでの挙動 =====
        demonstrateEmptyStream();

        // ===== 並列ストリームでのfindAny =====
        demonstrateParallelFindAny();
    }

    /**
     * findFirst() と findAny() のデモ
     * - findFirst(): ストリームの最初の要素を返す
     * - findAny(): ストリームの任意の要素を返す（並列処理で効率的）
     */
    private static void demonstrateFindOperations() {
        System.out.println("【1. findFirst() と findAny()】");

        List<String> names = Arrays.asList("太郎", "花子", "次郎", "春子", "三郎");

        // findFirst(): 必ず最初の要素
        Optional<String> first = names.stream()
                .filter(name -> name.endsWith("郎"))
                .findFirst();
        System.out.println("findFirst() 結果: " + first.orElse("なし")); // 太郎

        // findAny(): 順次ストリームでは通常最初の要素だが、保証はない
        Optional<String> any = names.stream()
                .filter(name -> name.endsWith("郎"))
                .findAny();
        System.out.println("findAny() 結果: " + any.orElse("なし"));

        // 要素が見つからない場合はOptional.empty()
        Optional<String> notFound = names.stream()
                .filter(name -> name.length() > 10)
                .findFirst();
        System.out.println("見つからない場合: " + notFound.orElse("該当なし"));

        System.out.println();
    }

    /**
     * match系メソッドのデモ
     * - anyMatch(): 1つでもマッチすればtrue
     * - allMatch(): 全てマッチすればtrue
     * - noneMatch(): 1つもマッチしなければtrue
     */
    private static void demonstrateMatchOperations() {
        System.out.println("【2. match系メソッド】");

        List<Integer> numbers = Arrays.asList(2, 4, 6, 8, 10);

        // anyMatch: 1つでも条件を満たせばtrue
        boolean hasEven = numbers.stream()
                .anyMatch(n -> n % 2 == 0);
        System.out.println("偶数が1つでもある？: " + hasEven); // true

        boolean hasLarge = numbers.stream()
                .anyMatch(n -> n > 100);
        System.out.println("100より大きい数がある？: " + hasLarge); // false

        // allMatch: 全ての要素が条件を満たせばtrue
        boolean allEven = numbers.stream()
                .allMatch(n -> n % 2 == 0);
        System.out.println("全て偶数？: " + allEven); // true

        boolean allLarge = numbers.stream()
                .allMatch(n -> n > 5);
        System.out.println("全て5より大きい？: " + allLarge); // false

        // noneMatch: 1つも条件を満たさなければtrue
        boolean noOdd = numbers.stream()
                .noneMatch(n -> n % 2 != 0);
        System.out.println("奇数が1つもない？: " + noOdd); // true

        boolean noSmall = numbers.stream()
                .noneMatch(n -> n < 1);
        System.out.println("1未満の数がない？: " + noSmall); // true

        System.out.println();
    }

    /**
     * 短絡評価（short-circuit）のデモ
     * match系メソッドやfind系メソッドは、結果が確定したら残りの処理をスキップする
     */
    private static void demonstrateShortCircuit() {
        System.out.println("【3. 短絡評価の動作】");

        // anyMatch: 1つでもマッチしたら残りは評価しない
        System.out.println("--- anyMatch の短絡評価 ---");
        boolean anyResult = Stream.of(1, 2, 3, 4, 5)
                .peek(n -> System.out.println("  処理中: " + n))
                .anyMatch(n -> n > 2);
        System.out.println("結果: " + anyResult + " (3で短絡)\n");

        // allMatch: 1つでもマッチしなかったら残りは評価しない
        System.out.println("--- allMatch の短絡評価 ---");
        boolean allResult = Stream.of(2, 4, 6, 7, 8)
                .peek(n -> System.out.println("  処理中: " + n))
                .allMatch(n -> n % 2 == 0);
        System.out.println("結果: " + allResult + " (7で短絡)\n");

        // noneMatch: 1つでもマッチしたら残りは評価しない
        System.out.println("--- noneMatch の短絡評価 ---");
        boolean noneResult = Stream.of(1, 3, 5, 6, 7)
                .peek(n -> System.out.println("  処理中: " + n))
                .noneMatch(n -> n % 2 == 0);
        System.out.println("結果: " + noneResult + " (6で短絡)\n");

        // findFirst: 最初の要素が見つかったら終了
        System.out.println("--- findFirst の短絡評価 ---");
        Optional<Integer> firstResult = Stream.of(10, 20, 30, 40, 50)
                .peek(n -> System.out.println("  処理中: " + n))
                .filter(n -> n > 15)
                .findFirst();
        System.out.println("結果: " + firstResult.get() + " (20で短絡)\n");

        System.out.println();
    }

    /**
     * 空ストリームでのmatch系メソッドの挙動
     * - allMatch: true（全ての要素が条件を満たす = 反例がない）
     * - anyMatch: false（1つも条件を満たす要素がない）
     * - noneMatch: true（1つも条件を満たす要素がない）
     */
    private static void demonstrateEmptyStream() {
        System.out.println("【4. 空ストリームでの挙動】");

        Stream<Integer> emptyStream1 = Stream.empty();
        boolean allMatch = emptyStream1.allMatch(n -> n > 0);
        System.out.println("空ストリームで allMatch: " + allMatch); // true（重要！）

        Stream<Integer> emptyStream2 = Stream.empty();
        boolean anyMatch = emptyStream2.anyMatch(n -> n > 0);
        System.out.println("空ストリームで anyMatch: " + anyMatch); // false

        Stream<Integer> emptyStream3 = Stream.empty();
        boolean noneMatch = emptyStream3.noneMatch(n -> n > 0);
        System.out.println("空ストリームで noneMatch: " + noneMatch); // true

        Stream<Integer> emptyStream4 = Stream.empty();
        Optional<Integer> findFirst = emptyStream4.findFirst();
        System.out.println("空ストリームで findFirst: " + findFirst.orElse(null)); // null

        System.out.println("\n※重要: allMatch は空ストリームで true を返す！");
        System.out.println("  理由: 「全ての要素が条件を満たす」= 「反例が存在しない」\n");

        System.out.println();
    }

    /**
     * 並列ストリームでのfindAny()の動作
     * 並列処理では、findAny()がfindFirst()より効率的
     */
    private static void demonstrateParallelFindAny() {
        System.out.println("【5. 並列ストリームでの findAny()】");

        List<Integer> largeList = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);

        // 順次ストリーム: findFirst と findAny は同じ結果になりがち
        System.out.println("--- 順次ストリーム ---");
        for (int i = 0; i < 3; i++) {
            Optional<Integer> result = largeList.stream()
                    .filter(n -> n > 5)
                    .findAny();
            System.out.println("  試行" + (i + 1) + ": " + result.get());
        }

        // 並列ストリーム: findAny は毎回異なる結果になる可能性がある
        System.out.println("\n--- 並列ストリーム ---");
        for (int i = 0; i < 5; i++) {
            Optional<Integer> result = largeList.parallelStream()
                    .filter(n -> n > 5)
                    .findAny();
            System.out.println("  試行" + (i + 1) + ": " + result.get());
        }

        System.out.println("\n※並列ストリームでは、findAny()の結果は不定");
        System.out.println("  どのスレッドが最初に要素を見つけるかに依存する");
        System.out.println("  順序が重要でない場合は findAny() を使うと効率的\n");
    }
}
