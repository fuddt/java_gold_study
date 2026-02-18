# Stream中間操作 完全ガイド

Java Gold試験で頻出のStream中間操作をマスターするための資料である。

## 目次
1. [中間操作とは](#中間操作とは)
2. [map() - 要素の変換](#map---要素の変換)
3. [flatMap() - ネスト構造の平坦化](#flatmap---ネスト構造の平坦化)
4. [map vs flatMap の違い](#map-vs-flatmap-の違い)
5. [filter() - 要素の絞り込み](#filter---要素の絞り込み)
6. [peek() - デバッグ用](#peek---デバッグ用)
7. [sorted() - ソート](#sorted---ソート)
8. [distinct() - 重複除去](#distinct---重複除去)
9. [limit() と skip()](#limit-と-skip)
10. [試験ポイント・引っかけ問題](#試験ポイント引っかけ問題)

---

## 中間操作とは

中間操作は**Stream**を返す操作のことである。終端操作が呼ばれるまで実際には実行されない（**遅延評価**）ってのが超重要。

### 主な中間操作一覧

| メソッド | 説明 | ステートレス/フル |
|---------|------|-----------------|
| `map()` | 要素を変換 | ステートレス |
| `flatMap()` | ネスト構造を平坦化 | ステートレス |
| `filter()` | 条件に合う要素だけ残す | ステートレス |
| `peek()` | 各要素に処理（主にデバッグ用） | ステートレス |
| `sorted()` | ソート | **ステートフル** |
| `distinct()` | 重複除去 | **ステートフル** |
| `limit()` | 最初のN個だけ取得 | ステートフル |
| `skip()` | 最初のN個をスキップ | ステートフル |

**ステートレス**: 前後の要素に依存しない（効率的）
**ステートフル**: 全要素を見る必要がある（メモリ使用）

---

## map() - 要素の変換

**要素を1対1で変換**する操作である。入力要素数 = 出力要素数。

### 基本構文

```java
Stream<R> map(Function<T, R> mapper)
```

### 使用例

```java
List<String> names = Arrays.asList("apple", "banana", "cherry");

// 文字列を大文字に変換
List<String> upper = names.stream()
    .map(String::toUpperCase)
    .collect(Collectors.toList());
// 結果: [APPLE, BANANA, CHERRY]

// 文字列の長さに変換
List<Integer> lengths = names.stream()
    .map(String::length)
    .collect(Collectors.toList());
// 結果: [5, 6, 6]

// 数値を二乗
List<Integer> numbers = Arrays.asList(1, 2, 3, 4);
List<Integer> squared = numbers.stream()
    .map(n -> n * n)
    .collect(Collectors.toList());
// 結果: [1, 4, 9, 16]
```

### ポイント

- **1つの要素 → 1つの結果**の変換
- `Function<T, R>`を受け取る（T型を受け取ってR型を返す）
- メソッド参照が使えるケースが多い

---

## flatMap() - ネスト構造の平坦化

**要素を1対多で変換してから平坦化**する操作。ネストしたStreamを1つのStreamにまとめるんである。

### 基本構文

```java
Stream<R> flatMap(Function<T, Stream<R>> mapper)
```

### 使用例

```java
// List<List<String>> → List<String>
List<List<String>> nested = Arrays.asList(
    Arrays.asList("a", "b"),
    Arrays.asList("c", "d"),
    Arrays.asList("e")
);

List<String> flat = nested.stream()
    .flatMap(list -> list.stream())  // 各ListをStreamに変換して平坦化
    .collect(Collectors.toList());
// 結果: [a, b, c, d, e]

// 文字列を文字に分解
List<String> words = Arrays.asList("hello", "world");
List<String> chars = words.stream()
    .flatMap(word -> Arrays.stream(word.split("")))
    .collect(Collectors.toList());
// 結果: [h, e, l, l, o, w, o, r, l, d]

// Optional<List<String>>の処理
Optional<List<String>> opt = Optional.of(Arrays.asList("x", "y"));
List<String> result = opt.stream()
    .flatMap(list -> list.stream())
    .collect(Collectors.toList());
// 結果: [x, y]
```

### ポイント

- **1つの要素 → 複数の結果**に変換してから平坦化
- `Function<T, Stream<R>>`を受け取る（StreamのStreamにならない）
- ネストした構造を扱うときに必須

---

## map vs flatMap の違い

これが**試験で超頻出**だから絶対理解しておこう！

### 違いを図で理解

```
map():
[a, b, c] --map(s -> s.toUpperCase())--> [A, B, C]
1個 → 1個の変換

flatMap():
[[a,b], [c,d]] --flatMap(list -> list.stream())--> [a, b, c, d]
1個 → 複数個の変換 + 平坦化
```

### コード比較

```java
List<String> words = Arrays.asList("hello", "world");

// mapを使った場合 - Stream<Stream<String>>になる
// これはコンパイルエラーか、意図しない結果になる
Stream<Stream<String>> streamOfStreams = words.stream()
    .map(word -> Arrays.stream(word.split("")));
// Stream<Stream<String>>型で使いにくい！

// flatMapを使った場合 - Stream<String>になる
Stream<String> flatStream = words.stream()
    .flatMap(word -> Arrays.stream(word.split("")));
// Stream<String>型で正しく平坦化される！

List<String> result = flatStream.collect(Collectors.toList());
// 結果: [h, e, l, l, o, w, o, r, l, d]
```

### 使い分けの目安

| 状況 | 使うメソッド |
|------|------------|
| String → Integer みたいな単純変換 | **map()** |
| List<List\<T>> → List\<T> の平坦化 | **flatMap()** |
| 文字列 → 文字の配列 | **flatMap()** |
| Optional\<T> → T の取り出し | **flatMap()** |
| Stream\<Stream\<T>> → Stream\<T> | **flatMap()** |

### 試験での引っかけ

```java
// 問題: これは何が出力される？
List<String> words = Arrays.asList("ab", "cd");
long count = words.stream()
    .map(s -> s.split(""))      // Stream<String[]>になる
    .count();

System.out.println(count);  // 答え: 2（配列が2個）

// flatMapを使うと
long count2 = words.stream()
    .flatMap(s -> Arrays.stream(s.split("")))  // Stream<String>になる
    .count();

System.out.println(count2);  // 答え: 4（文字が4個）
```

---

## filter() - 要素の絞り込み

**条件に合う要素だけを残す**操作である。

### 基本構文

```java
Stream<T> filter(Predicate<T> predicate)
```

### 使用例

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

// 偶数だけ
List<Integer> evens = numbers.stream()
    .filter(n -> n % 2 == 0)
    .collect(Collectors.toList());
// 結果: [2, 4, 6, 8, 10]

// 複数条件（AND）
List<Integer> result = numbers.stream()
    .filter(n -> n > 3)        // 3より大きい
    .filter(n -> n % 2 != 0)   // かつ奇数
    .collect(Collectors.toList());
// 結果: [5, 7, 9]

// 複数条件（OR）
List<Integer> result2 = numbers.stream()
    .filter(n -> n < 3 || n > 8)
    .collect(Collectors.toList());
// 結果: [1, 2, 9, 10]

// nullチェック
List<String> words = Arrays.asList("a", null, "b", null, "c");
List<String> nonNull = words.stream()
    .filter(Objects::nonNull)
    .collect(Collectors.toList());
// 結果: [a, b, c]
```

### ポイント

- `Predicate<T>`（boolean返す関数）を受け取る
- filterを複数繋げると**AND条件**になる
- OR条件は`||`で繋ぐか、`Predicate.or()`を使う

---

## peek() - デバッグ用

**要素を覗き見る**操作。主に**デバッグ目的**で使うんである。

### 基本構文

```java
Stream<T> peek(Consumer<T> action)
```

### 使用例

```java
List<String> result = Stream.of("a", "b", "c")
    .peek(s -> System.out.println("処理前: " + s))
    .map(String::toUpperCase)
    .peek(s -> System.out.println("処理後: " + s))
    .collect(Collectors.toList());

// 出力:
// 処理前: a
// 処理後: A
// 処理前: b
// 処理後: B
// 処理前: c
// 処理後: C
```

### 超重要な注意点

**peekは終端操作がないと実行されない！**

```java
// これは何も出力されない（終端操作がない）
Stream.of("a", "b", "c")
    .peek(s -> System.out.println(s));  // 実行されない！

// これは出力される（forEachが終端操作）
Stream.of("a", "b", "c")
    .peek(s -> System.out.println(s))
    .forEach(s -> {});  // 終端操作があるので実行される
```

### peekでやってはいけないこと

```java
// NG例: 外部変数を変更（副作用）
List<String> result = new ArrayList<>();
Stream.of("a", "b", "c")
    .peek(s -> result.add(s))  // これはダメ！予測不能な動作
    .collect(Collectors.toList());

// OK例: ログ出力やデバッグ
Stream.of("a", "b", "c")
    .peek(s -> System.out.println("DEBUG: " + s))  // これはOK
    .collect(Collectors.toList());
```

### ポイント

- **デバッグ専用**と考える（本番コードではあまり使わない）
- **終端操作がないと実行されない**（遅延評価の典型例）
- 要素の変更はできるが、**やるべきではない**

---

## sorted() - ソート

**要素をソート**する操作。自然順序またはComparatorでソートできる。

### 基本構文

```java
Stream<T> sorted()  // 自然順序（Comparableの実装に従う）
Stream<T> sorted(Comparator<T> comparator)  // Comparator指定
```

### 使用例

```java
List<Integer> numbers = Arrays.asList(5, 2, 8, 1, 9);

// 自然順序（昇順）
List<Integer> asc = numbers.stream()
    .sorted()
    .collect(Collectors.toList());
// 結果: [1, 2, 5, 8, 9]

// 降順
List<Integer> desc = numbers.stream()
    .sorted(Comparator.reverseOrder())
    .collect(Collectors.toList());
// 結果: [9, 8, 5, 2, 1]

// 文字列の長さでソート
List<String> words = Arrays.asList("apple", "pie", "banana", "cherry");
List<String> byLength = words.stream()
    .sorted(Comparator.comparing(String::length))
    .collect(Collectors.toList());
// 結果: [pie, apple, banana, cherry]

// 複数条件でソート（長さ→辞書順）
List<String> multi = words.stream()
    .sorted(Comparator.comparing(String::length)
           .thenComparing(Comparator.naturalOrder()))
    .collect(Collectors.toList());
// 結果: [pie, apple, banana, cherry]
```

### Comparatorの便利メソッド

```java
// 昇順
Comparator.naturalOrder()
Comparator.comparing(String::length)

// 降順
Comparator.reverseOrder()
Comparator.comparing(String::length).reversed()

// 複数条件
Comparator.comparing(String::length)
          .thenComparing(String::toLowerCase)

// nullを最初/最後に
Comparator.nullsFirst(Comparator.naturalOrder())
Comparator.nullsLast(Comparator.naturalOrder())
```

### ポイント

- `sorted()`は**ステートフル操作**（全要素を保持する必要がある）
- 自然順序を使う場合、要素は`Comparable`を実装している必要がある
- `sorted()`の位置で処理効率が変わる（後述）

---

## distinct() - 重複除去

**重複する要素を除去**する操作。`equals()`で判定する。

### 基本構文

```java
Stream<T> distinct()
```

### 使用例

```java
List<Integer> numbers = Arrays.asList(1, 2, 2, 3, 3, 3, 4);
List<Integer> unique = numbers.stream()
    .distinct()
    .collect(Collectors.toList());
// 結果: [1, 2, 3, 4]

// 文字列の重複除去（大文字小文字区別）
List<String> words = Arrays.asList("apple", "APPLE", "banana", "apple");
List<String> distinct1 = words.stream()
    .distinct()
    .collect(Collectors.toList());
// 結果: [apple, APPLE, banana]

// 大文字小文字を無視して重複除去
List<String> distinct2 = words.stream()
    .map(String::toLowerCase)
    .distinct()
    .collect(Collectors.toList());
// 結果: [apple, banana]
```

### カスタムオブジェクトの重複除去

```java
class Person {
    String name;
    int age;

    // equals()とhashCode()を正しく実装する必要がある
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return age == person.age && Objects.equals(name, person.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age);
    }
}

List<Person> people = Arrays.asList(
    new Person("Alice", 20),
    new Person("Bob", 25),
    new Person("Alice", 20)  // 重複
);

List<Person> unique = people.stream()
    .distinct()  // equals()で判定
    .collect(Collectors.toList());
// 結果: [Alice(20), Bob(25)]
```

### ポイント

- `equals()`と`hashCode()`を使って判定
- **ステートフル操作**（内部でSetを使う）
- 順序は最初に出現した要素の順序を保持

---

## limit() と skip()

**要素数を制限したり、スキップしたり**する操作である。

### 基本構文

```java
Stream<T> limit(long maxSize)  // 最初のmaxSize個だけ取得
Stream<T> skip(long n)         // 最初のn個をスキップ
```

### 使用例

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

// limit() - 最初の5個
List<Integer> first5 = numbers.stream()
    .limit(5)
    .collect(Collectors.toList());
// 結果: [1, 2, 3, 4, 5]

// skip() - 最初の5個をスキップ
List<Integer> skip5 = numbers.stream()
    .skip(5)
    .collect(Collectors.toList());
// 結果: [6, 7, 8, 9, 10]

// 組み合わせ（ページネーション）
List<Integer> page2 = numbers.stream()
    .skip(3)   // 最初の3個をスキップ
    .limit(3)  // その後3個取得
    .collect(Collectors.toList());
// 結果: [4, 5, 6]

// 無限ストリームとの組み合わせ
List<Integer> first10 = Stream.iterate(1, n -> n + 1)
    .limit(10)  // limitがないと無限ループ！
    .collect(Collectors.toList());
// 結果: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
```

### ポイント

- `limit()`は**ショートサーキット操作**（必要な分だけ処理）
- 無限ストリームには**必ずlimitが必要**
- `skip()`と`limit()`の順序が重要

---

## 試験ポイント・引っかけ問題

Java Gold試験で狙われやすいポイントをまとめたよ。

### 1. 遅延評価（Lazy Evaluation）

**中間操作は終端操作が呼ばれるまで実行されない！**

```java
// 問題: 何が出力される？
Stream<String> stream = Stream.of("a", "b", "c")
    .peek(s -> System.out.println(s));

// 答え: 何も出力されない（終端操作がない）

// 正しくは
stream.forEach(s -> {});  // これで初めてpeekが実行される
```

### 2. Streamの再利用不可

**一度使ったStreamは再利用できない！**

```java
Stream<String> stream = Stream.of("a", "b", "c");
stream.forEach(System.out::println);  // OK

stream.forEach(System.out::println);  // IllegalStateException!
```

### 3. map vs flatMap

**これが一番の引っかけポイント！**

```java
// 問題: 結果の型は？
List<String> words = Arrays.asList("hello", "world");

// ケース1: map
Stream<String[]> result1 = words.stream()
    .map(s -> s.split(""));  // Stream<String[]>

// ケース2: flatMap
Stream<String> result2 = words.stream()
    .flatMap(s -> Arrays.stream(s.split("")));  // Stream<String>

// 問題: countの結果は？
long count1 = words.stream()
    .map(s -> s.split(""))
    .count();  // 答え: 2（配列が2個）

long count2 = words.stream()
    .flatMap(s -> Arrays.stream(s.split("")))
    .count();  // 答え: 10（文字が10個）
```

### 4. sortedの位置

**sortedの位置でパフォーマンスと結果が変わる！**

```java
List<Integer> numbers = Arrays.asList(5, 2, 8, 1, 9, 3, 7, 4, 6);

// ケース1: sorted → limit
List<Integer> case1 = numbers.stream()
    .sorted()    // 全部ソート（9個）
    .limit(3)
    .collect(Collectors.toList());
// 結果: [1, 2, 3]（全体の最小3個）

// ケース2: limit → sorted
List<Integer> case2 = numbers.stream()
    .limit(3)    // 最初の3個取得
    .sorted()    // その3個だけソート
    .collect(Collectors.toList());
// 結果: [2, 5, 8]（最初の3個をソート）

// 試験ポイント: 結果が違う！
```

### 5. distinctとsortedの順序

```java
List<Integer> numbers = Arrays.asList(5, 2, 8, 2, 5, 1);

// ケース1: distinct → sorted
List<Integer> case1 = numbers.stream()
    .distinct()  // [5, 2, 8, 1]
    .sorted()    // [1, 2, 5, 8]
    .collect(Collectors.toList());
// 結果: [1, 2, 5, 8]

// ケース2: sorted → distinct
List<Integer> case2 = numbers.stream()
    .sorted()    // [1, 2, 2, 5, 5, 8]
    .distinct()  // [1, 2, 5, 8]
    .collect(Collectors.toList());
// 結果: [1, 2, 5, 8]

// 結果は同じだが、distinct→sortedの方が効率的！
```

### 6. filterの短絡評価

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

// 問題: 何回filterが実行される？
List<Integer> result = numbers.stream()
    .filter(n -> {
        System.out.println("filter: " + n);
        return n > 5;
    })
    .limit(2)
    .collect(Collectors.toList());

// 答え: 6回（5まで falseで、6と7でtrueなので2個揃った時点で終了）
```

### 7. peekの副作用

```java
// 問題: これは何が出力される？
List<String> list = new ArrayList<>();
Stream.of("a", "b", "c")
    .peek(s -> list.add(s))
    .forEach(s -> {});

System.out.println(list);
// 答え: [a, b, c]（動くが推奨されない）

// より悪い例（バグの温床）
List<String> list2 = new ArrayList<>();
Stream.of("a", "b", "c")
    .parallel()  // 並列ストリーム
    .peek(s -> list2.add(s))  // スレッドセーフじゃない！
    .forEach(s -> {});
// 答え: 予測不能（ConcurrentModificationExceptionの可能性）
```

### 8. sorted()のComparable要件

```java
// 問題: これはコンパイルできる？実行できる？
class Person {
    String name;
    // Comparableを実装していない
}

List<Person> people = Arrays.asList(new Person("Alice"), new Person("Bob"));

// コンパイルはOK、実行時にClassCastException!
List<Person> sorted = people.stream()
    .sorted()  // PersonはComparableじゃない
    .collect(Collectors.toList());

// 正しくはComparatorを指定
List<Person> sorted2 = people.stream()
    .sorted(Comparator.comparing(p -> p.name))  // OK
    .collect(Collectors.toList());
```

### 9. limitとinfinite stream

```java
// 問題: これは終了する？
Stream.iterate(1, n -> n + 1)
    .forEach(System.out::println);
// 答え: 終了しない（無限ループ）

// 正しくはlimitを使う
Stream.iterate(1, n -> n + 1)
    .limit(10)
    .forEach(System.out::println);
// 答え: 1〜10を出力して終了

// Java 9以降の条件付き
Stream.iterate(1, n -> n <= 10, n -> n + 1)  // Java 9+
    .forEach(System.out::println);
```

### 10. メソッドチェーンの順序最適化

```java
List<String> words = Arrays.asList("apple", "banana", "cherry", "date", "elderberry");

// 非効率な順序
List<String> bad = words.stream()
    .map(String::toUpperCase)      // 5個変換
    .filter(s -> s.length() > 5)   // 5個チェック
    .collect(Collectors.toList());

// 効率的な順序
List<String> good = words.stream()
    .filter(s -> s.length() > 5)   // 3個チェック
    .map(String::toUpperCase)      // 3個だけ変換
    .collect(Collectors.toList());

// 試験ポイント: filterを先にすると処理が減る
```

---

## まとめ

### 覚えておくべき重要ポイント

1. **遅延評価**: 中間操作は終端操作が来るまで実行されない
2. **Streamの再利用不可**: 一度使ったら終わり
3. **map vs flatMap**: 1対1なのか1対多なのか
4. **peekは終端操作必須**: ないと実行されない
5. **sortedの位置**: limitと組み合わせるときは要注意
6. **ステートフル操作**: sorted, distinct, limit, skipはメモリを使う
7. **filterは先に**: 無駄な処理を減らせる
8. **無限ストリームにはlimit**: ないと終わらない

### 試験対策チェックリスト

- [ ] mapとflatMapの違いを説明できる
- [ ] peekが実行されないケースを理解している
- [ ] sortedとlimitの順序で結果が変わることを知っている
- [ ] Streamが再利用できないことを知っている
- [ ] 遅延評価の仕組みを理解している
- [ ] ステートフル/ステートレス操作の違いを知っている
- [ ] Comparatorの各メソッドを使える
- [ ] filterの位置で効率が変わることを知っている

これで中間操作はバッチリである！実際にコードを動かして試してみることをお勧めする。
