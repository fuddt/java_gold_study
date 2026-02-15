# ジェネリクスとコレクション - Java Gold試験対策

## 1. ジェネリクスの基本概念

ジェネリクスは型の安全性を提供する仕組みだよね。コンパイル時に型チェックができるから、実行時のClassCastExceptionを防げるんだ。

### 基本的な定義

```java
// ジェネリッククラス
public class Box<T> {
    private T value;

    public Box(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }
}

// 使用例
Box<String> stringBox = new Box<>("Hello");
Box<Integer> intBox = new Box<>(42);
```

### ダイアモンド演算子（<>）

Java 7以降、右辺の型パラメータは省略できるんだ。コンパイラが型推論してくれるからね。

```java
// Java 7以降
List<String> list = new ArrayList<>();  // ◯ 推奨

// 古い書き方
List<String> list = new ArrayList<String>();  // ◯ でも冗長

// raw type（非推奨！）
List list = new ArrayList();  // △ 警告が出る
```

**試験ポイント**: raw typeは非推奨だけど、コンパイルは通るよ。でも警告が出る。

---

## 2. ワイルドカード（?）の使い分け

ワイルドカードは3種類あって、それぞれ使い道が違うんだよね。

### 2.1 ? extends T（上限境界ワイルドカード）

**意味**: TまたはTのサブクラス

**特徴**: **読み取り専用**（Producer）

```java
public double sumNumbers(List<? extends Number> list) {
    double sum = 0.0;
    for (Number num : list) {  // 読み取りはOK
        sum += num.doubleValue();
    }
    // list.add(new Integer(5));  // NG! コンパイルエラー
    // list.add(new Double(5.0));  // NG! コンパイルエラー
    list.add(null);  // nullだけはOK
    return sum;
}

// 使用例
List<Integer> intList = Arrays.asList(1, 2, 3);
List<Double> doubleList = Arrays.asList(1.5, 2.5);
sumNumbers(intList);    // OK
sumNumbers(doubleList); // OK
```

**なぜaddできないの？**
- `List<? extends Number>`は`List<Integer>`かもしれないし、`List<Double>`かもしれない
- どの型か確定しないから、安全のため追加はブロックされる
- でも読み取る分には、「Numberまたはそのサブクラス」って分かってるからOK

### 2.2 ? super T（下限境界ワイルドカード）

**意味**: TまたはTのスーパークラス

**特徴**: **書き込み可能**（Consumer）

```java
public void addIntegers(List<? super Integer> list) {
    list.add(10);      // OK! Integerを追加できる
    list.add(20);      // OK!

    // 読み取りは制限される
    // Integer num = list.get(0);  // NG! コンパイルエラー
    Object obj = list.get(0);      // OK（Object型になる）
}

// 使用例
List<Integer> intList = new ArrayList<>();
List<Number> numberList = new ArrayList<>();
List<Object> objectList = new ArrayList<>();
addIntegers(intList);    // OK
addIntegers(numberList); // OK
addIntegers(objectList); // OK
```

**なぜ読み取りが制限されるの？**
- `List<? super Integer>`は`List<Integer>`かもしれないし、`List<Number>`や`List<Object>`かもしれない
- 取得した要素がIntegerとは限らないから、安全のためObject型になる
- でもIntegerを追加するのは安全（どのスーパークラスでも受け入れられるから）

### 2.3 ?（非境界ワイルドカード）

**意味**: 任意の型

**特徴**: 読み取りも書き込みも制限される（nullを除く）

```java
public void printListSize(List<?> list) {
    System.out.println("Size: " + list.size());  // OK

    // list.add("String");  // NG! コンパイルエラー
    list.add(null);         // nullだけはOK

    Object obj = list.get(0);  // OK（Object型）
}
```

---

## 3. PECS原則（超重要！）

**PECS = Producer Extends, Consumer Super**

これはJava Goldで頻出だから、しっかり覚えておこうぜ。

### 原則の意味

- **Producer（生産者）**: データを提供する側 → `? extends T` を使う
- **Consumer（消費者）**: データを受け取る側 → `? super T` を使う

### 実例

```java
// PECSの典型例
public <T> void copy(List<? extends T> source,    // Producer
                     List<? super T> destination) { // Consumer
    for (T element : source) {
        destination.add(element);
    }
}

// 使用例
List<Integer> source = Arrays.asList(1, 2, 3);
List<Number> destination = new ArrayList<>();
copy(source, destination);  // OK!
```

**なぜこうなるの？**
- `source`からは読み取るだけ → Producer → `extends`
- `destination`には書き込むだけ → Consumer → `super`

### 試験の引っかけポイント

```java
// これはどっち？
public void process(List<? extends Number> list) {
    list.add(new Integer(5));  // NG! コンパイルエラー
}

public void process(List<? super Integer> list) {
    Integer num = list.get(0);  // NG! コンパイルエラー
    Object obj = list.get(0);   // OK
}
```

---

## 4. 型消去（Type Erasure）とその影響

Javaのジェネリクスは**実行時には型情報が消える**んだよね。これを型消去って呼ぶ。

### 型消去の仕組み

```java
// コンパイル前
List<String> stringList = new ArrayList<>();
List<Integer> integerList = new ArrayList<>();

// コンパイル後（内部的に）
List stringList = new ArrayList();
List integerList = new ArrayList();
```

実行時には両方とも`java.util.ArrayList`になる。

```java
System.out.println(stringList.getClass() == integerList.getClass());  // true
```

### 型消去の影響でできないこと

```java
// 1. instanceof で型パラメータをチェックできない
if (list instanceof List<String>) {}  // NG! コンパイルエラー
if (list instanceof List<?>) {}       // OK

// 2. new で型パラメータの配列を作れない
T[] array = new T[10];  // NG! コンパイルエラー

// 3. static フィールドに型パラメータを使えない
class Box<T> {
    private static T value;  // NG! コンパイルエラー
}

// 4. 型パラメータでキャストできない
T obj = (T) someObject;  // 警告が出る（unchecked cast）
```

**試験ポイント**: 型消去により、実行時にジェネリクスの型情報は取得できないよ！

---

## 5. 主要コレクションの特徴と使い分け

### 5.1 List（順序あり、重複OK）

| 実装 | 特徴 | 使い所 |
|-----|------|--------|
| **ArrayList** | インデックスアクセスが高速（O(1)）<br>挿入・削除は遅い（O(n)） | ランダムアクセスが多い場合 |
| **LinkedList** | 挿入・削除が高速（O(1)）<br>インデックスアクセスは遅い（O(n)） | 先頭/末尾の操作が多い場合 |

```java
List<String> arrayList = new ArrayList<>();
arrayList.add("A");
arrayList.get(0);  // 高速

List<String> linkedList = new LinkedList<>();
linkedList.addFirst("B");  // LinkedListの特別なメソッド
linkedList.addLast("C");
```

**試験ポイント**: LinkedListは`Deque`インターフェースも実装してるから、`addFirst/addLast`が使えるよ。

### 5.2 Set（重複なし）

| 実装 | 特徴 | 順序 | 制約 |
|-----|------|------|------|
| **HashSet** | 高速（O(1)） | 不定 | hashCode/equalsが必要 |
| **LinkedHashSet** | やや遅い | 挿入順 | hashCode/equalsが必要 |
| **TreeSet** | 遅い（O(log n)） | 自然順序 | **Comparableが必須** |

```java
Set<String> hashSet = new HashSet<>();
hashSet.add("C");
hashSet.add("A");
hashSet.add("B");
System.out.println(hashSet);  // [A, B, C] または [C, B, A] - 順序不定

Set<String> linkedHashSet = new LinkedHashSet<>();
linkedHashSet.add("C");
linkedHashSet.add("A");
linkedHashSet.add("B");
System.out.println(linkedHashSet);  // [C, A, B] - 挿入順

Set<String> treeSet = new TreeSet<>();
treeSet.add("C");
treeSet.add("A");
treeSet.add("B");
System.out.println(treeSet);  // [A, B, C] - ソート済み
```

**重要**: TreeSetに追加する要素は`Comparable`を実装してるか、`Comparator`を渡す必要がある！

```java
// これはNG!
class Person {
    String name;
}

Set<Person> set = new TreeSet<>();
set.add(new Person("Alice"));  // 実行時にClassCastException!

// これはOK
class Person implements Comparable<Person> {
    String name;

    @Override
    public int compareTo(Person other) {
        return this.name.compareTo(other.name);
    }
}
```

### 5.3 Queue/Deque（キュー、両端キュー）

| 実装 | 特徴 | 用途 |
|-----|------|------|
| **ArrayDeque** | 両端操作が高速<br>スタックとしても使える | 汎用キュー、スタック |
| **PriorityQueue** | 優先順位順に取り出せる | 優先度付きタスク |

```java
// キューとして使う
Queue<String> queue = new ArrayDeque<>();
queue.offer("First");
queue.offer("Second");
queue.poll();  // "First"

// スタックとして使う
Deque<String> stack = new ArrayDeque<>();
stack.push("A");
stack.push("B");
stack.pop();  // "B"

// 優先順位キュー（自然順序）
Queue<Integer> pq = new PriorityQueue<>();
pq.offer(30);
pq.offer(10);
pq.offer(20);
pq.poll();  // 10（最小値）
```

**試験ポイント**:
- `ArrayDeque`はnullを許可しない！
- `PriorityQueue`も要素は`Comparable`が必要！

### 5.4 Map（キーと値のペア）

| 実装 | 特徴 | 順序 | 制約 |
|-----|------|------|------|
| **HashMap** | 高速（O(1)） | 不定 | キーのhashCode/equalsが必要 |
| **LinkedHashMap** | やや遅い | 挿入順 | キーのhashCode/equalsが必要 |
| **TreeMap** | 遅い（O(log n)） | キーの自然順序 | **キーがComparable必須** |

```java
Map<String, Integer> hashMap = new HashMap<>();
hashMap.put("C", 3);
hashMap.put("A", 1);
hashMap.put("B", 2);
System.out.println(hashMap);  // 順序不定

Map<String, Integer> linkedHashMap = new LinkedHashMap<>();
linkedHashMap.put("C", 3);
linkedHashMap.put("A", 1);
linkedHashMap.put("B", 2);
System.out.println(linkedHashMap);  // {C=3, A=1, B=2} - 挿入順

Map<String, Integer> treeMap = new TreeMap<>();
treeMap.put("C", 3);
treeMap.put("A", 1);
treeMap.put("B", 2);
System.out.println(treeMap);  // {A=1, B=2, C=3} - キーでソート
```

---

## 6. TreeSet/TreeMapにはComparableが必要

これ、試験でよく出るから要注意だよ！

```java
// NG例
class Person {
    String name;
    int age;
}

Set<Person> set = new TreeSet<>();
set.add(new Person("Alice", 30));  // 実行時にClassCastException!

// OK例1: Comparableを実装
class Person implements Comparable<Person> {
    String name;
    int age;

    @Override
    public int compareTo(Person other) {
        return Integer.compare(this.age, other.age);
    }
}

Set<Person> set = new TreeSet<>();
set.add(new Person("Alice", 30));  // OK!

// OK例2: Comparatorを渡す
Set<Person> set = new TreeSet<>((p1, p2) -> p1.name.compareTo(p2.name));
set.add(new Person("Alice", 30));  // OK!
```

---

## 7. コレクション操作の基本

```java
List<String> list = new ArrayList<>();

// 追加
list.add("Apple");
list.add(0, "Banana");  // インデックス指定

// 削除
list.remove("Apple");     // 要素で削除
list.remove(0);           // インデックスで削除

// 検索
boolean exists = list.contains("Apple");
int index = list.indexOf("Apple");

// サイズ
int size = list.size();
boolean isEmpty = list.isEmpty();

// 反復処理
for (String item : list) {
    System.out.println(item);
}

// Iterator（削除しながら反復する場合に便利）
Iterator<String> iterator = list.iterator();
while (iterator.hasNext()) {
    String item = iterator.next();
    if (item.equals("Apple")) {
        iterator.remove();  // 安全に削除できる
    }
}
```

**試験ポイント**: 拡張forループ中に`list.remove()`を呼ぶと`ConcurrentModificationException`が発生するよ！Iteratorを使おう。

---

## 8. 試験ポイント・引っかけ問題

### 8.1 raw typeと型パラメータ

```java
List<String> list1 = new ArrayList<>();  // OK
List<String> list2 = new ArrayList();    // 警告が出るけどコンパイル通る
List list3 = new ArrayList<String>();    // 警告が出るけどコンパイル通る
```

### 8.2 ワイルドカードの制約

```java
// これらは全部コンパイルエラー！
List<? extends Number> list1 = new ArrayList<>();
list1.add(new Integer(5));  // NG!

List<? super Integer> list2 = new ArrayList<>();
Integer num = list2.get(0);  // NG! (Object obj = list2.get(0); ならOK)

List<?> list3 = new ArrayList<>();
list3.add("String");  // NG! (nullはOK)
```

### 8.3 TreeSet/TreeMapの落とし穴

```java
// Comparableを実装してないクラス
class MyClass {
    int value;
}

Set<MyClass> set = new TreeSet<>();
set.add(new MyClass());  // 実行時にClassCastException!
```

### 8.4 型消去による制限

```java
// これらは全部コンパイルエラー！
if (list instanceof List<String>) {}  // NG!
T[] array = new T[10];                // NG!
class Box<T> {
    static T value;  // NG!
}
```

### 8.5 Dequeの操作

```java
Deque<String> deque = new ArrayDeque<>();

// スタック操作（LIFO）
deque.push("A");
deque.push("B");
deque.pop();  // "B"

// キュー操作（FIFO）
deque.offer("A");
deque.offer("B");
deque.poll();  // "A"

// 両端操作
deque.addFirst("X");
deque.addLast("Y");
```

### 8.6 nullの扱い

```java
// nullを許可する
List<String> list = new ArrayList<>();
list.add(null);  // OK

Map<String, Integer> map = new HashMap<>();
map.put(null, 1);  // OK（キーがnullでもOK）

// nullを許可しない
Deque<String> deque = new ArrayDeque<>();
deque.add(null);  // NullPointerException!

Map<String, Integer> treeMap = new TreeMap<>();
treeMap.put(null, 1);  // NullPointerException!（キーはソート対象だから）
```

---

## 9. 覚えておくべきメソッド

### List特有のメソッド

```java
list.get(index)         // インデックスで取得
list.set(index, value)  // インデックスで置換
list.indexOf(value)     // 最初の出現位置
list.lastIndexOf(value) // 最後の出現位置
```

### Set特有のメソッド

```java
set.add(value)      // 追加（重複は無視される）
set.contains(value) // 存在チェック
```

### Queue/Deque特有のメソッド

```java
// Queueのメソッド
queue.offer(value)  // 追加（キャパシティ制限がある場合はfalse）
queue.poll()        // 取得して削除（空ならnull）
queue.peek()        // 取得のみ（空ならnull）

// Dequeのメソッド
deque.addFirst(value)  // 先頭に追加
deque.addLast(value)   // 末尾に追加
deque.removeFirst()    // 先頭を削除
deque.removeLast()     // 末尾を削除
deque.push(value)      // スタック: 先頭に追加
deque.pop()            // スタック: 先頭を削除
```

### Map特有のメソッド

```java
map.put(key, value)           // 追加（既存キーなら上書き）
map.get(key)                  // 取得（存在しなければnull）
map.getOrDefault(key, def)    // 取得（存在しなければデフォルト値）
map.containsKey(key)          // キーの存在チェック
map.containsValue(value)      // 値の存在チェック
map.keySet()                  // キーのSet
map.values()                  // 値のCollection
map.entrySet()                // エントリのSet
```

---

## 10. まとめ

### ジェネリクス

- **extends** = Producer = 読み取り専用
- **super** = Consumer = 書き込み可能
- **PECS原則** = Producer Extends, Consumer Super
- **型消去** = 実行時に型情報は消える

### コレクション

- **List**: ArrayList（高速アクセス）、LinkedList（高速挿入削除）
- **Set**: HashSet（高速）、LinkedHashSet（挿入順）、TreeSet（ソート、**Comparable必須**）
- **Queue/Deque**: ArrayDeque（汎用）、PriorityQueue（優先順位、**Comparable必須**）
- **Map**: HashMap（高速）、LinkedHashMap（挿入順）、TreeMap（キーでソート、**キーがComparable必須**）

### 試験の鉄則

1. TreeSet/TreeMapは**Comparable**が必要！
2. `? extends`は読み取り専用、`? super`は書き込み可能
3. 型消去により実行時に型情報は取れない
4. ArrayDequeとTreeMapは**nullを許可しない**
5. 拡張forループ中の削除は`ConcurrentModificationException`

これでジェネリクスとコレクションはバッチリだよね！
