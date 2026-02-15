package app;

import java.util.*;
import java.util.stream.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Stream Sort 学習用サンプル ===\n");

        // サンプルデータの準備
        List<Person> people = Arrays.asList(
            new Person("田中", 25),
            new Person("佐藤", 30),
            new Person("鈴木", 25),
            new Person("高橋", 35),
            new Person("伊藤", 20),
            new Person(null, 28),  // null名前のテスト用
            new Person("渡辺", 25)
        );

        System.out.println("元のリスト:");
        people.forEach(System.out::println);
        System.out.println();

        // 1. Comparable を使った自然順序ソート
        demonstrateComparable();

        // 2. Comparator.comparing() の基本
        demonstrateComparingBasic(people);

        // 3. Comparator.comparingInt() の使用
        demonstrateComparingInt(people);

        // 4. thenComparing() で複数条件ソート
        demonstrateThenComparing(people);

        // 5. reversed() で逆順ソート
        demonstrateReversed(people);

        // 6. Comparator.naturalOrder() と reverseOrder()
        demonstrateNaturalOrder();

        // 7. null の扱い（nullsFirst / nullsLast）
        demonstrateNullHandling(people);

        // 8. Collections.sort() と List.sort()
        demonstrateCollectionsSort(people);

        // 9. ラムダ式での直接比較
        demonstrateLambdaComparator(people);

        // 10. 複雑なチェーン例
        demonstrateComplexChain(people);
    }

    // 1. Comparable を使った自然順序ソート
    static void demonstrateComparable() {
        System.out.println("=== 1. Comparable（自然順序）===");
        List<Integer> numbers = Arrays.asList(5, 2, 8, 1, 9);

        // Integer は Comparable<Integer> を実装している
        List<Integer> sorted = numbers.stream()
            .sorted()  // 自然順序（昇順）
            .collect(Collectors.toList());

        System.out.println("昇順: " + sorted);

        // String も Comparable を実装
        List<String> names = Arrays.asList("Charlie", "Alice", "Bob");
        List<String> sortedNames = names.stream()
            .sorted()  // 辞書順
            .collect(Collectors.toList());

        System.out.println("辞書順: " + sortedNames);
        System.out.println();
    }

    // 2. Comparator.comparing() の基本
    static void demonstrateComparingBasic(List<Person> people) {
        System.out.println("=== 2. Comparator.comparing() の基本 ===");

        // 名前でソート（nullを除外）
        List<Person> sortedByName = people.stream()
            .filter(p -> p.getName() != null)
            .sorted(Comparator.comparing(Person::getName))
            .collect(Collectors.toList());

        System.out.println("名前順:");
        sortedByName.forEach(System.out::println);
        System.out.println();
    }

    // 3. Comparator.comparingInt() の使用
    static void demonstrateComparingInt(List<Person> people) {
        System.out.println("=== 3. Comparator.comparingInt() ===");

        // 年齢でソート（プリミティブ型の最適化版）
        List<Person> sortedByAge = people.stream()
            .sorted(Comparator.comparingInt(Person::getAge))
            .collect(Collectors.toList());

        System.out.println("年齢順（昇順）:");
        sortedByAge.forEach(System.out::println);

        // comparingLong(), comparingDouble() も同様に使える
        System.out.println();
    }

    // 4. thenComparing() で複数条件ソート
    static void demonstrateThenComparing(List<Person> people) {
        System.out.println("=== 4. thenComparing() で複数条件ソート ===");

        // 年齢でソート → 同じ年齢なら名前でソート
        List<Person> sorted = people.stream()
            .filter(p -> p.getName() != null)
            .sorted(Comparator.comparingInt(Person::getAge)
                .thenComparing(Person::getName))
            .collect(Collectors.toList());

        System.out.println("年齢順 → 名前順:");
        sorted.forEach(System.out::println);

        // thenComparingInt() も使える
        System.out.println();
    }

    // 5. reversed() で逆順ソート
    static void demonstrateReversed(List<Person> people) {
        System.out.println("=== 5. reversed() で逆順ソート ===");

        // 年齢の降順
        List<Person> reversedAge = people.stream()
            .sorted(Comparator.comparingInt(Person::getAge).reversed())
            .collect(Collectors.toList());

        System.out.println("年齢順（降順）:");
        reversedAge.forEach(System.out::println);

        // 複数条件の逆順
        List<Person> reversed = people.stream()
            .filter(p -> p.getName() != null)
            .sorted(Comparator.comparingInt(Person::getAge)
                .thenComparing(Person::getName)
                .reversed())  // 全体を逆順に
            .collect(Collectors.toList());

        System.out.println("\n年齢順→名前順 の逆順:");
        reversed.forEach(System.out::println);
        System.out.println();
    }

    // 6. Comparator.naturalOrder() と reverseOrder()
    static void demonstrateNaturalOrder() {
        System.out.println("=== 6. naturalOrder() と reverseOrder() ===");

        List<String> words = Arrays.asList("banana", "apple", "cherry");

        // naturalOrder() - Comparable の自然順序
        List<String> natural = words.stream()
            .sorted(Comparator.naturalOrder())
            .collect(Collectors.toList());

        System.out.println("naturalOrder(): " + natural);

        // reverseOrder() - 自然順序の逆
        List<String> reverse = words.stream()
            .sorted(Comparator.reverseOrder())
            .collect(Collectors.toList());

        System.out.println("reverseOrder(): " + reverse);
        System.out.println();
    }

    // 7. null の扱い（nullsFirst / nullsLast）
    static void demonstrateNullHandling(List<Person> people) {
        System.out.println("=== 7. null の扱い（nullsFirst / nullsLast）===");

        // nullsFirst - null を最初に
        List<Person> nullsFirst = people.stream()
            .sorted(Comparator.nullsFirst(
                Comparator.comparing(Person::getName,
                    Comparator.nullsFirst(Comparator.naturalOrder()))))
            .collect(Collectors.toList());

        System.out.println("nullsFirst（名前がnullの人を最初に）:");
        nullsFirst.forEach(System.out::println);

        // nullsLast - null を最後に
        List<Person> nullsLast = people.stream()
            .sorted(Comparator.comparing(Person::getName,
                Comparator.nullsLast(Comparator.naturalOrder())))
            .collect(Collectors.toList());

        System.out.println("\nnullsLast（名前がnullの人を最後に）:");
        nullsLast.forEach(System.out::println);
        System.out.println();
    }

    // 8. Collections.sort() と List.sort()
    static void demonstrateCollectionsSort(List<Person> people) {
        System.out.println("=== 8. Collections.sort() と List.sort() ===");

        // Collections.sort() - 元のリストを変更
        List<Integer> numbers1 = new ArrayList<>(Arrays.asList(5, 2, 8, 1));
        Collections.sort(numbers1);
        System.out.println("Collections.sort(): " + numbers1);

        // Comparator を指定
        List<Integer> numbers2 = new ArrayList<>(Arrays.asList(5, 2, 8, 1));
        Collections.sort(numbers2, Comparator.reverseOrder());
        System.out.println("Collections.sort(逆順): " + numbers2);

        // List.sort() - Java 8 から追加（推奨）
        List<Person> peopleCopy = new ArrayList<>(people);
        peopleCopy.removeIf(p -> p.getName() == null);
        peopleCopy.sort(Comparator.comparing(Person::getName));

        System.out.println("List.sort():");
        peopleCopy.forEach(System.out::println);
        System.out.println();
    }

    // 9. ラムダ式での直接比較
    static void demonstrateLambdaComparator(List<Person> people) {
        System.out.println("=== 9. ラムダ式での直接比較 ===");

        // ラムダ式で Comparator を直接書く
        List<Person> sorted = people.stream()
            .filter(p -> p.getName() != null)
            .sorted((p1, p2) -> p1.getName().compareTo(p2.getName()))
            .collect(Collectors.toList());

        System.out.println("ラムダ式で名前順:");
        sorted.forEach(System.out::println);

        // 複雑な比較ロジック
        List<Person> customSorted = people.stream()
            .filter(p -> p.getName() != null)
            .sorted((p1, p2) -> {
                // 年齢が30以上の人を優先
                boolean p1Over30 = p1.getAge() >= 30;
                boolean p2Over30 = p2.getAge() >= 30;
                if (p1Over30 != p2Over30) {
                    return p1Over30 ? -1 : 1;
                }
                // 同じグループ内では名前順
                return p1.getName().compareTo(p2.getName());
            })
            .collect(Collectors.toList());

        System.out.println("\nカスタムロジック（30歳以上優先→名前順）:");
        customSorted.forEach(System.out::println);
        System.out.println();
    }

    // 10. 複雑なチェーン例
    static void demonstrateComplexChain(List<Person> people) {
        System.out.println("=== 10. 複雑なチェーン例 ===");

        // 年齢降順 → 名前昇順（nullは最後）
        List<Person> complex = people.stream()
            .sorted(Comparator.comparingInt(Person::getAge).reversed()
                .thenComparing(Person::getName,
                    Comparator.nullsLast(Comparator.naturalOrder())))
            .collect(Collectors.toList());

        System.out.println("年齢降順 → 名前昇順（null最後）:");
        complex.forEach(System.out::println);
        System.out.println();
    }

    // Person クラス（内部クラス）
    static class Person {
        private String name;
        private int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int age() {
            return age;
        }

        public int getAge() {
            return age;
        }

        @Override
        public String toString() {
            return String.format("Person{name='%s', age=%d}",
                name == null ? "null" : name, age);
        }
    }
}
