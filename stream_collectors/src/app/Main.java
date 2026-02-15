package app;

import java.util.*;
import java.util.stream.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Stream Collectors 完全ガイド ===\n");

        // テスト用データ
        List<Person> people = Arrays.asList(
            new Person("太郎", 25, "東京", 5000),
            new Person("花子", 30, "大阪", 7000),
            new Person("次郎", 25, "東京", 6000),
            new Person("美咲", 35, "東京", 8000),
            new Person("健太", 30, "大阪", 5500),
            new Person("さくら", 25, "福岡", 4500)
        );

        // 1. toList(), toSet(), toUnmodifiableList()
        demonstrateBasicCollectors(people);

        // 2. toMap() - キー重複の扱い
        demonstrateToMap(people);

        // 3. groupingBy() - 単一レベルと多階層
        demonstrateGroupingBy(people);

        // 4. partitioningBy() - Map<Boolean, List>を返す
        demonstratePartitioningBy(people);

        // 5. joining() - 区切り文字、接頭辞、接尾辞
        demonstrateJoining(people);

        // 6. counting(), summarizingInt()
        demonstrateStatistics(people);

        // 7. mapping() - downstreamコレクターとして
        demonstrateMappingAsDownstream(people);
    }

    // 1. 基本的なコレクター
    static void demonstrateBasicCollectors(List<Person> people) {
        System.out.println("【1. 基本的なコレクター】");

        // toList() - 変更可能なリスト
        List<String> namesList = people.stream()
            .map(Person::getName)
            .collect(Collectors.toList());
        System.out.println("toList(): " + namesList);
        namesList.add("追加可能"); // 変更可能
        System.out.println("変更後: " + namesList);

        // toSet() - 重複排除
        Set<Integer> ageSet = people.stream()
            .map(Person::getAge)
            .collect(Collectors.toSet());
        System.out.println("toSet() (重複排除): " + ageSet);

        // toUnmodifiableList() - 変更不可リスト (Java 10+)
        List<String> immutableList = people.stream()
            .map(Person::getName)
            .collect(Collectors.toUnmodifiableList());
        System.out.println("toUnmodifiableList(): " + immutableList);
        try {
            immutableList.add("追加不可"); // UnsupportedOperationException
        } catch (UnsupportedOperationException e) {
            System.out.println("→ 変更不可！例外: " + e.getClass().getSimpleName());
        }

        System.out.println();
    }

    // 2. toMap() - キー重複時の挙動
    static void demonstrateToMap(List<Person> people) {
        System.out.println("【2. toMap() - キー重複の扱い】");

        // 基本的なtoMap (キーが一意の場合)
        Map<String, Integer> nameToAge = people.stream()
            .filter(p -> p.getAge() != 25) // 重複を避けるため
            .collect(Collectors.toMap(
                Person::getName,  // キー
                Person::getAge    // 値
            ));
        System.out.println("基本的なtoMap: " + nameToAge);

        // キー重複時 - merge関数を指定しないと例外！
        System.out.println("\n【重要】キー重複時の挙動:");
        try {
            // 年齢をキーにすると重複が発生
            Map<Integer, String> ageToName = people.stream()
                .collect(Collectors.toMap(
                    Person::getAge,   // キー (重複あり)
                    Person::getName
                ));
        } catch (IllegalStateException e) {
            System.out.println("→ 例外発生！: " + e.getClass().getSimpleName());
            System.out.println("   メッセージ: Duplicate key (重複キーで例外)");
        }

        // merge関数で重複を解決
        Map<Integer, String> ageToNameMerged = people.stream()
            .collect(Collectors.toMap(
                Person::getAge,           // キー
                Person::getName,          // 値
                (existing, replacement) -> existing + ", " + replacement  // merge関数
            ));
        System.out.println("merge関数で重複解決: " + ageToNameMerged);

        // 最後の値を採用する場合
        Map<Integer, String> ageToNameLast = people.stream()
            .collect(Collectors.toMap(
                Person::getAge,
                Person::getName,
                (old, newVal) -> newVal  // 新しい値を採用
            ));
        System.out.println("新しい値を採用: " + ageToNameLast);

        System.out.println();
    }

    // 3. groupingBy() - グループ化
    static void demonstrateGroupingBy(List<Person> people) {
        System.out.println("【3. groupingBy() - グループ化】");

        // 単一レベルのグループ化
        Map<String, List<Person>> byCity = people.stream()
            .collect(Collectors.groupingBy(Person::getCity));
        System.out.println("都市別グループ化:");
        byCity.forEach((city, persons) -> {
            System.out.println("  " + city + ": " + persons.stream()
                .map(Person::getName)
                .collect(Collectors.joining(", ")));
        });

        // downstreamコレクターでカウント
        Map<String, Long> cityCount = people.stream()
            .collect(Collectors.groupingBy(
                Person::getCity,
                Collectors.counting()  // downstream
            ));
        System.out.println("\n都市別人数: " + cityCount);

        // downstreamコレクターで合計給与
        Map<String, Integer> citySalarySum = people.stream()
            .collect(Collectors.groupingBy(
                Person::getCity,
                Collectors.summingInt(Person::getSalary)
            ));
        System.out.println("都市別給与合計: " + citySalarySum);

        // 多階層グループ化 (都市 → 年齢)
        Map<String, Map<Integer, List<Person>>> multiLevel = people.stream()
            .collect(Collectors.groupingBy(
                Person::getCity,                    // 第1階層
                Collectors.groupingBy(Person::getAge)  // 第2階層
            ));
        System.out.println("\n多階層グループ化 (都市→年齢):");
        multiLevel.forEach((city, ageMap) -> {
            System.out.println("  " + city + ":");
            ageMap.forEach((age, persons) -> {
                System.out.println("    " + age + "歳: " + persons.stream()
                    .map(Person::getName)
                    .collect(Collectors.joining(", ")));
            });
        });

        System.out.println();
    }

    // 4. partitioningBy() - 2つに分割
    static void demonstratePartitioningBy(List<Person> people) {
        System.out.println("【4. partitioningBy() - 二分割】");

        // 年齢30歳以上/未満で分割
        Map<Boolean, List<Person>> partitioned = people.stream()
            .collect(Collectors.partitioningBy(p -> p.getAge() >= 30));

        System.out.println("30歳以上/未満で分割:");
        System.out.println("  true (30歳以上): " +
            partitioned.get(true).stream()
                .map(Person::getName)
                .collect(Collectors.joining(", ")));
        System.out.println("  false (30歳未満): " +
            partitioned.get(false).stream()
                .map(Person::getName)
                .collect(Collectors.joining(", ")));

        // downstreamコレクターと組み合わせ
        Map<Boolean, Long> partitionedCount = people.stream()
            .collect(Collectors.partitioningBy(
                p -> p.getSalary() >= 6000,
                Collectors.counting()
            ));
        System.out.println("\n給与6000以上/未満の人数: " + partitionedCount);

        // 重要: partitioningByは必ず2つのキー(true/false)を持つ
        Map<Boolean, List<Person>> alwaysBothKeys = people.stream()
            .filter(p -> p.getAge() > 100)  // 誰もマッチしない
            .collect(Collectors.partitioningBy(p -> p.getAge() >= 30));
        System.out.println("\n誰もマッチしない場合でも両方のキーが存在:");
        System.out.println("  " + alwaysBothKeys);  // {false=[], true=[]}

        System.out.println();
    }

    // 5. joining() - 文字列結合
    static void demonstrateJoining(List<Person> people) {
        System.out.println("【5. joining() - 文字列結合】");

        // 区切り文字のみ
        String names = people.stream()
            .map(Person::getName)
            .collect(Collectors.joining(", "));
        System.out.println("区切り文字のみ: " + names);

        // 区切り文字 + 接頭辞 + 接尾辞
        String namesWithBrackets = people.stream()
            .map(Person::getName)
            .collect(Collectors.joining(
                ", ",      // 区切り文字
                "[",       // 接頭辞
                "]"        // 接尾辞
            ));
        System.out.println("接頭辞・接尾辞付き: " + namesWithBrackets);

        // 空のストリームでも接頭辞・接尾辞は付く
        String empty = people.stream()
            .filter(p -> p.getAge() > 100)
            .map(Person::getName)
            .collect(Collectors.joining(", ", "[", "]"));
        System.out.println("空のストリーム: '" + empty + "'");  // "[]"

        System.out.println();
    }

    // 6. 統計系コレクター
    static void demonstrateStatistics(List<Person> people) {
        System.out.println("【6. 統計系コレクター】");

        // counting() - 要素数
        long count = people.stream()
            .filter(p -> p.getCity().equals("東京"))
            .collect(Collectors.counting());
        System.out.println("東京在住者数: " + count);

        // summarizingInt() - 統計情報をまとめて取得
        IntSummaryStatistics salaryStats = people.stream()
            .collect(Collectors.summarizingInt(Person::getSalary));
        System.out.println("\n給与統計:");
        System.out.println("  件数: " + salaryStats.getCount());
        System.out.println("  合計: " + salaryStats.getSum());
        System.out.println("  平均: " + salaryStats.getAverage());
        System.out.println("  最小: " + salaryStats.getMin());
        System.out.println("  最大: " + salaryStats.getMax());

        // averagingInt() - 平均値
        double avgSalary = people.stream()
            .collect(Collectors.averagingInt(Person::getSalary));
        System.out.println("\n平均給与: " + avgSalary);

        // summingInt() - 合計
        int totalSalary = people.stream()
            .collect(Collectors.summingInt(Person::getSalary));
        System.out.println("給与合計: " + totalSalary);

        System.out.println();
    }

    // 7. mapping() - downstreamコレクターとして使用
    static void demonstrateMappingAsDownstream(List<Person> people) {
        System.out.println("【7. mapping() - downstreamコレクター】");

        // groupingByと組み合わせて、グループごとに名前のリストを取得
        Map<String, List<String>> cityToNames = people.stream()
            .collect(Collectors.groupingBy(
                Person::getCity,
                Collectors.mapping(     // downstream
                    Person::getName,    // マッピング関数
                    Collectors.toList() // さらにdownstream
                )
            ));
        System.out.println("都市別名前リスト:");
        cityToNames.forEach((city, names) ->
            System.out.println("  " + city + ": " + names));

        // partitioningByと組み合わせ
        Map<Boolean, Set<String>> partitionedCities = people.stream()
            .collect(Collectors.partitioningBy(
                p -> p.getAge() >= 30,
                Collectors.mapping(
                    Person::getCity,
                    Collectors.toSet()  // 重複排除
                )
            ));
        System.out.println("\n年齢30歳以上/未満の都市リスト (重複排除):");
        System.out.println("  " + partitionedCities);

        // 複雑な例: グループ化 + mapping + joining
        Map<Integer, String> ageToNames = people.stream()
            .collect(Collectors.groupingBy(
                Person::getAge,
                Collectors.mapping(
                    Person::getName,
                    Collectors.joining(", ")  // 文字列結合
                )
            ));
        System.out.println("\n年齢別名前 (結合):");
        ageToNames.forEach((age, names) ->
            System.out.println("  " + age + "歳: " + names));

        System.out.println();
    }
}

// データクラス
class Person {
    private final String name;
    private final int age;
    private final String city;
    private final int salary;

    public Person(String name, int age, String city, int salary) {
        this.name = name;
        this.age = age;
        this.city = city;
        this.salary = salary;
    }

    public String getName() { return name; }
    public int getAge() { return age; }
    public String getCity() { return city; }
    public int getSalary() { return salary; }

    @Override
    public String toString() {
        return name + "(" + age + "歳, " + city + ", " + salary + ")";
    }
}
