package app;

import java.util.*;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== ジェネリクスとコレクションの学習 ===\n");

        // ========== 1. ジェネリッククラスの定義と使用 ==========
        System.out.println("【1. ジェネリッククラス】");
        Box<String> stringBox = new Box<>("Hello");
        Box<Integer> intBox = new Box<>(42);
        System.out.println("String Box: " + stringBox.get());
        System.out.println("Integer Box: " + intBox.get());

        // ダイアモンド演算子（型推論）
        Box<String> diamondBox = new Box<>("Diamond Operator");
        System.out.println("Diamond: " + diamondBox.get());
        System.out.println();

        // ========== 2. ジェネリックメソッド ==========
        System.out.println("【2. ジェネリックメソッド】");
        String[] strArray = {"A", "B", "C"};
        Integer[] intArray = {1, 2, 3};
        printArray(strArray);
        printArray(intArray);
        System.out.println();

        // ========== 3. ワイルドカード - ? extends T（上限境界） ==========
        System.out.println("【3. ワイルドカード - ? extends T（Producer Extends）】");
        List<Integer> intList = Arrays.asList(1, 2, 3);
        List<Double> doubleList = Arrays.asList(1.5, 2.5, 3.5);
        System.out.println("Integer sum: " + sumNumbers(intList));
        System.out.println("Double sum: " + sumNumbers(doubleList));
        // ? extends Numberは「読み取り専用」- addはできない（nullを除く）
        // これはコンパイルエラー: extendsWildcardList.add(5);
        System.out.println();

        // ========== 4. ワイルドカード - ? super T（下限境界） ==========
        System.out.println("【4. ワイルドカード - ? super T（Consumer Super）】");
        List<Number> numberList = new ArrayList<>();
        List<Object> objectList = new ArrayList<>();
        addIntegers(numberList);
        addIntegers(objectList);
        System.out.println("Number list: " + numberList);
        System.out.println("Object list: " + objectList);
        // ? super Integerは「書き込み可能」- Integerを追加できる
        System.out.println();

        // ========== 5. PECS原則の実践 ==========
        System.out.println("【5. PECS原則（Producer Extends, Consumer Super）】");
        List<Integer> source = Arrays.asList(1, 2, 3);
        List<Number> destination = new ArrayList<>();
        copyElements(source, destination);
        System.out.println("Copied: " + destination);
        System.out.println();

        // ========== 6. 非境界ワイルドカード（?） ==========
        System.out.println("【6. 非境界ワイルドカード（?）】");
        List<String> stringList = Arrays.asList("A", "B", "C");
        List<Integer> integerList = Arrays.asList(1, 2, 3);
        printListSize(stringList);
        printListSize(integerList);
        System.out.println();

        // ========== 7. 型消去（Type Erasure）の影響 ==========
        System.out.println("【7. 型消去（Type Erasure）】");
        demonstrateTypeErasure();
        System.out.println();

        // ========== 8. List実装の比較 ==========
        System.out.println("【8. List実装 - ArrayList vs LinkedList】");
        List<String> arrayList = new ArrayList<>();
        arrayList.add("Apple");
        arrayList.add("Banana");
        arrayList.add("Cherry");
        System.out.println("ArrayList: " + arrayList);
        System.out.println("Get by index: " + arrayList.get(1));

        List<String> linkedList = new LinkedList<>();
        linkedList.add("Dog");
        linkedList.add("Cat");
        linkedList.addFirst("Bird"); // LinkedListのメソッド
        System.out.println("LinkedList: " + linkedList);
        System.out.println();

        // ========== 9. Set実装の比較 ==========
        System.out.println("【9. Set実装 - HashSet, LinkedHashSet, TreeSet】");

        // HashSet: 順序保証なし、高速
        Set<String> hashSet = new HashSet<>();
        hashSet.add("C");
        hashSet.add("A");
        hashSet.add("B");
        hashSet.add("A"); // 重複は無視される
        System.out.println("HashSet（順序不定）: " + hashSet);

        // LinkedHashSet: 挿入順を保持
        Set<String> linkedHashSet = new LinkedHashSet<>();
        linkedHashSet.add("C");
        linkedHashSet.add("A");
        linkedHashSet.add("B");
        System.out.println("LinkedHashSet（挿入順）: " + linkedHashSet);

        // TreeSet: 自然順序でソート（Comparableが必要）
        Set<String> treeSet = new TreeSet<>();
        treeSet.add("C");
        treeSet.add("A");
        treeSet.add("B");
        System.out.println("TreeSet（ソート済み）: " + treeSet);
        System.out.println();

        // ========== 10. Queue/Deque実装 ==========
        System.out.println("【10. Queue/Deque - ArrayDeque, PriorityQueue】");

        // ArrayDeque: 両端キュー（スタックとしても使える）
        Deque<String> deque = new ArrayDeque<>();
        deque.offerLast("First");
        deque.offerLast("Second");
        deque.offerLast("Third");
        System.out.println("Deque（キューとして）: " + deque);
        System.out.println("Poll: " + deque.pollFirst());

        // スタックとして使う
        Deque<String> stack = new ArrayDeque<>();
        stack.push("A");
        stack.push("B");
        stack.push("C");
        System.out.println("Stack: " + stack);
        System.out.println("Pop: " + stack.pop());

        // PriorityQueue: 優先順位キュー（自然順序）
        Queue<Integer> priorityQueue = new PriorityQueue<>();
        priorityQueue.offer(30);
        priorityQueue.offer(10);
        priorityQueue.offer(20);
        System.out.println("PriorityQueue poll: " + priorityQueue.poll()); // 10（最小値）
        System.out.println();

        // ========== 11. Map実装の比較 ==========
        System.out.println("【11. Map実装 - HashMap, LinkedHashMap, TreeMap】");

        // HashMap: 順序保証なし、高速
        Map<String, Integer> hashMap = new HashMap<>();
        hashMap.put("C", 3);
        hashMap.put("A", 1);
        hashMap.put("B", 2);
        System.out.println("HashMap（順序不定）: " + hashMap);

        // LinkedHashMap: 挿入順を保持
        Map<String, Integer> linkedHashMap = new LinkedHashMap<>();
        linkedHashMap.put("C", 3);
        linkedHashMap.put("A", 1);
        linkedHashMap.put("B", 2);
        System.out.println("LinkedHashMap（挿入順）: " + linkedHashMap);

        // TreeMap: キーの自然順序でソート
        Map<String, Integer> treeMap = new TreeMap<>();
        treeMap.put("C", 3);
        treeMap.put("A", 1);
        treeMap.put("B", 2);
        System.out.println("TreeMap（キーでソート）: " + treeMap);
        System.out.println();

        // ========== 12. コレクション操作 ==========
        System.out.println("【12. コレクション操作】");
        List<String> fruits = new ArrayList<>();
        fruits.add("Apple");
        fruits.add("Banana");
        fruits.add("Cherry");

        System.out.println("Contains 'Apple': " + fruits.contains("Apple"));
        System.out.println("Size: " + fruits.size());

        // 反復処理
        System.out.print("拡張forループ: ");
        for (String fruit : fruits) {
            System.out.print(fruit + " ");
        }
        System.out.println();

        // Iterator
        System.out.print("Iterator: ");
        Iterator<String> iterator = fruits.iterator();
        while (iterator.hasNext()) {
            System.out.print(iterator.next() + " ");
        }
        System.out.println();

        fruits.remove("Banana");
        System.out.println("After remove: " + fruits);
        System.out.println();

        // ========== 13. TreeSetとComparable ==========
        System.out.println("【13. TreeSetとComparable】");
        Set<Person> personSet = new TreeSet<>();
        personSet.add(new Person("Alice", 30));
        personSet.add(new Person("Bob", 25));
        personSet.add(new Person("Charlie", 35));
        System.out.println("TreeSet<Person>（年齢順）:");
        for (Person p : personSet) {
            System.out.println("  " + p);
        }
        System.out.println();

        System.out.println("=== 完了 ===");
    }

    // ========== ジェネリッククラスの定義 ==========
    static class Box<T> {
        private T value;

        public Box(T value) {
            this.value = value;
        }

        public T get() {
            return value;
        }

        public void set(T value) {
            this.value = value;
        }
    }

    // ========== ジェネリックメソッド ==========
    public static <T> void printArray(T[] array) {
        System.out.print("Array: ");
        for (T element : array) {
            System.out.print(element + " ");
        }
        System.out.println();
    }

    // ========== ? extends T（Producer Extends） ==========
    // Numberまたはそのサブクラスのリストを受け取る（読み取り専用）
    public static double sumNumbers(List<? extends Number> list) {
        double sum = 0.0;
        for (Number num : list) {
            sum += num.doubleValue();
        }
        // list.add(new Integer(5)); // コンパイルエラー！extendsは読み取り専用
        return sum;
    }

    // ========== ? super T（Consumer Super） ==========
    // Integerまたはそのスーパークラスのリストを受け取る（書き込み可能）
    public static void addIntegers(List<? super Integer> list) {
        list.add(10);
        list.add(20);
        list.add(30);
        // Integer num = list.get(0); // コンパイルエラー！superは取得時にObject型になる
        Object obj = list.get(0); // これはOK
    }

    // ========== PECS原則のデモ ==========
    // Producer Extends, Consumer Super
    public static <T> void copyElements(List<? extends T> source, List<? super T> destination) {
        for (T element : source) {
            destination.add(element);
        }
    }

    // ========== 非境界ワイルドカード ==========
    public static void printListSize(List<?> list) {
        System.out.println("List size: " + list.size());
        // list.add("String"); // コンパイルエラー！型がわからないので追加不可（nullを除く）
        list.add(null); // nullはOK
    }

    // ========== 型消去のデモ ==========
    public static void demonstrateTypeErasure() {
        List<String> stringList = new ArrayList<>();
        List<Integer> integerList = new ArrayList<>();

        // 実行時には両方ともjava.util.ArrayListになる
        System.out.println("String List class: " + stringList.getClass().getName());
        System.out.println("Integer List class: " + integerList.getClass().getName());
        System.out.println("Same class? " + (stringList.getClass() == integerList.getClass()));

        // これはコンパイル時にチェックされる
        // stringList.add(123); // コンパイルエラー

        // 型消去により、以下はできない：
        // if (stringList instanceof List<String>) {} // コンパイルエラー
        // new ArrayList<T>() のような配列は作れない（型パラメータで配列を作れない）
    }

    // ========== Comparableを実装したクラス ==========
    static class Person implements Comparable<Person> {
        private String name;
        private int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public int compareTo(Person other) {
            return Integer.compare(this.age, other.age);
        }

        @Override
        public String toString() {
            return name + " (" + age + ")";
        }
    }
}
