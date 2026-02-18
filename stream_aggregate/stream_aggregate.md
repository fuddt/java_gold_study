# Stream 集約操作 完全ガイド

## 概要

Streamの集約操作は、ストリームの要素を処理して単一の結果を生成する**終端操作**である。集約操作には色々あるけど、Java Goldで重要なのは以下のやつら。

- `count()` - 要素数
- `max()` / `min()` - 最大/最小
- `sum()` / `average()` - 合計/平均（プリミティブストリームのみ）
- `reduce()` - 汎用的な集約
- `summaryStatistics()` - 統計情報を一度に取得

## 基本的な集約操作

### 1. count() - 要素数をカウント

一番シンプルな集約操作。`long`型を返すよ。

```java
List<String> list = Arrays.asList("a", "b", "c");
long count = list.stream().count(); // 3
```

**ポイント:**
- 戻り値は`long`型（`int`じゃないぞ！）
- 中間操作で絞り込んだ後の要素数が取れる

```java
long count = list.stream()
    .filter(s -> s.length() > 5)
    .count(); // フィルタ後の要素数
```

### 2. max() / min() - 最大/最小値

`Stream<T>`の`max()`と`min()`は**Comparatorが必須**である。戻り値は`Optional<T>`。

```java
List<Integer> numbers = Arrays.asList(5, 2, 8, 1, 9);

Optional<Integer> max = numbers.stream()
    .max(Comparator.naturalOrder());
System.out.println(max.get()); // 9

Optional<Integer> min = numbers.stream()
    .min(Comparator.naturalOrder());
System.out.println(min.get()); // 1
```

**試験ポイント:**
- `max()`と`min()`は`Comparator`が**必須**
- 戻り値は`Optional<T>`（要素がない場合があるため）
- プリミティブストリーム（IntStream等）の場合は`Comparator`不要

```java
// これはコンパイルエラー
Optional<Integer> max = numbers.stream().max(); // NG

// Comparatorが必要
Optional<Integer> max = numbers.stream()
    .max(Comparator.naturalOrder()); // OK
```

## プリミティブストリーム（IntStream, LongStream, DoubleStream）

### プリミティブストリームとは？

`Stream<Integer>`みたいなボクシング型のストリームとは別に、プリミティブ型専用のストリームがあるんである。

- `IntStream` - int専用
- `LongStream` - long専用
- `DoubleStream` - double専用

**なぜプリミティブストリームが必要なの？**
- ボクシング/アンボクシングのオーバーヘッドを避けるため
- `sum()`, `average()`, `summaryStatistics()`などの便利メソッドが使える

### プリミティブストリームの生成

```java
// IntStream
IntStream.of(1, 2, 3, 4, 5)
IntStream.range(1, 5)      // 1,2,3,4 (endは含まない)
IntStream.rangeClosed(1, 5) // 1,2,3,4,5 (endを含む)

// LongStream
LongStream.of(100L, 200L, 300L)
LongStream.range(1L, 5L)

// DoubleStream
DoubleStream.of(1.5, 2.5, 3.5)
```

**試験ポイント:**
- `range(start, end)` - endは**含まない**
- `rangeClosed(start, end)` - endを**含む**

### sum() と average()

プリミティブストリームには`sum()`と`average()`メソッドがある。

```java
IntStream scores = IntStream.of(80, 90, 75, 85, 95);

// sum() -> int/long/double型を返す
int total = scores.sum(); // 425

// average() -> OptionalDoubleを返す（要素がない場合があるため）
IntStream scores2 = IntStream.of(80, 90, 75, 85, 95);
OptionalDouble avg = scores2.average();
System.out.println(avg.getAsDouble()); // 85.0
```

**超重要な試験ポイント:**

| メソッド | IntStream | LongStream | DoubleStream |
|---------|-----------|------------|--------------|
| `sum()` | `int` | `long` | `double` |
| `average()` | `OptionalDouble` | `OptionalDouble` | `OptionalDouble` |
| `max()` | `OptionalInt` | `OptionalLong` | `OptionalDouble` |
| `min()` | `OptionalInt` | `OptionalLong` | `OptionalDouble` |

```java
// average()の戻り値に注意！
IntStream stream = IntStream.of(1, 2, 3);

// これはコンパイルエラー
double avg = stream.average(); // NG: OptionalDoubleだから

// 正しい使い方
OptionalDouble avg = stream.average(); // OK
double value = avg.getAsDouble(); // または avg.orElse(0.0)
```

### max() / min() on プリミティブストリーム

プリミティブストリームの`max()`/`min()`は**Comparatorが不要**である。

```java
IntStream stream = IntStream.of(5, 2, 8, 1, 9);

OptionalInt max = stream.max(); // Comparator不要！
System.out.println(max.getAsInt()); // 9

OptionalInt min = IntStream.of(5, 2, 8, 1, 9).min();
System.out.println(min.getAsInt()); // 1
```

**比較: Stream<Integer> vs IntStream**

```java
// Stream<Integer> - Comparator必須
Stream<Integer> s1 = Stream.of(1, 2, 3);
Optional<Integer> max1 = s1.max(Comparator.naturalOrder()); // Comparator必須

// IntStream - Comparator不要
IntStream s2 = IntStream.of(1, 2, 3);
OptionalInt max2 = s2.max(); // Comparator不要
```

## mapToInt() / mapToLong() / mapToDouble()

`Stream<T>`をプリミティブストリームに変換するメソッド。これ超重要である。

```java
List<String> words = Arrays.asList("Java", "Stream", "API");

// Stream<String> -> IntStream
int totalLength = words.stream()
    .mapToInt(String::length)  // IntStreamに変換
    .sum(); // 13

// Stream<Integer> -> IntStream
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
int sum = numbers.stream()
    .mapToInt(Integer::intValue) // または i -> i
    .sum();
```

**試験ポイント:**
- `Stream<T>`には`sum()`メソッドがない
- `sum()`を使いたければ`mapToInt()`等でプリミティブストリームに変換する必要がある

```java
// これはコンパイルエラー
Stream<Integer> stream = Stream.of(1, 2, 3);
int sum = stream.sum(); // NG: Stream<T>にsum()はない

// 正しい方法
int sum = Stream.of(1, 2, 3)
    .mapToInt(Integer::intValue)
    .sum(); // OK
```

### boxed() - プリミティブストリームからStream<T>へ

逆方向の変換も可能である。

```java
IntStream intStream = IntStream.of(1, 2, 3, 4, 5);

// IntStream -> Stream<Integer>
Stream<Integer> boxed = intStream.boxed();
List<Integer> list = boxed.collect(Collectors.toList());
```

## summaryStatistics() - 統計情報を一度に取得

プリミティブストリームには`summaryStatistics()`という便利メソッドがあるんである。これで`count`, `sum`, `min`, `average`, `max`を一度に取得できる。

```java
IntStream scores = IntStream.of(80, 90, 75, 85, 95, 70);
IntSummaryStatistics stats = scores.summaryStatistics();

System.out.println(stats.getCount());   // 6
System.out.println(stats.getSum());     // 495
System.out.println(stats.getAverage()); // 82.5
System.out.println(stats.getMin());     // 70
System.out.println(stats.getMax());     // 95
```

**戻り値の型:**
- `IntStream` -> `IntSummaryStatistics`
- `LongStream` -> `LongSummaryStatistics`
- `DoubleStream` -> `DoubleSummaryStatistics`

**試験ポイント:**
- `summaryStatistics()`は**プリミティブストリームのみ**
- `Stream<Integer>`には存在しない

## reduce() - 汎用的な集約操作

`reduce()`は最も汎用的な集約操作である。自分で集約ロジックを定義できる。

### 3つのオーバーロード

#### 1. reduce(identity, accumulator)

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);

// identity: 初期値
// accumulator: (累積値, 要素) -> 新しい累積値
int sum = numbers.stream()
    .reduce(0, (acc, n) -> acc + n); // 15

int product = numbers.stream()
    .reduce(1, (acc, n) -> acc * n); // 120
```

**パラメータ:**
- `identity` - 初期値（恒等値）
- `accumulator` - `(T累積値, T要素) -> T新しい累積値`

**戻り値:** `T`型（Optionalではない）

#### 2. reduce(accumulator)

初期値を指定しないバージョン。戻り値は`Optional<T>`になる。

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);

Optional<Integer> sum = numbers.stream()
    .reduce((a, b) -> a + b); // Optional<Integer>

System.out.println(sum.get()); // 15

// 空のストリームの場合
Optional<Integer> emptySum = Stream.<Integer>empty()
    .reduce((a, b) -> a + b);
System.out.println(emptySum.orElse(0)); // 0
```

**試験ポイント:**
- 初期値がないので、戻り値は`Optional<T>`
- 空のストリームの場合は`Optional.empty()`が返る

#### 3. reduce(identity, accumulator, combiner)

並列ストリーム用のバージョン。`combiner`は複数スレッドの結果を結合する際に使われる。

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);

int sum = numbers.parallelStream()
    .reduce(
        0,                        // identity
        (acc, n) -> acc + n,     // accumulator
        (acc1, acc2) -> acc1 + acc2  // combiner
    );
```

**パラメータ:**
- `identity` - 初期値
- `accumulator` - `(U累積値, T要素) -> U新しい累積値`
- `combiner` - `(U結果1, U結果2) -> U結合結果`

**combinerの役割:**
- 並列ストリームでは、各スレッドが独立に`accumulator`で処理を行う
- その後、`combiner`で各スレッドの結果を結合する

```
スレッド1: 0 + 1 + 2 = 3
スレッド2: 0 + 3 + 4 = 7
スレッド3: 0 + 5 = 5

combiner: 3 + 7 = 10
combiner: 10 + 5 = 15
```

### reduce()の使用例

```java
// 文字列の連結
List<String> words = Arrays.asList("Java", "Gold", "Stream");
String result = words.stream()
    .reduce("", (acc, s) -> acc + s); // "JavaGoldStream"

// 最大値を求める（max()の代替）
Optional<Integer> max = numbers.stream()
    .reduce((a, b) -> a > b ? a : b);

// 最小値を求める（min()の代替）
Optional<Integer> min = numbers.stream()
    .reduce((a, b) -> a < b ? a : b);
```

## Stream<Integer> vs IntStream の違い

これマジで重要な試験ポイントである。

### Stream<Integer> - ボクシング型のストリーム

```java
Stream<Integer> stream = Stream.of(1, 2, 3, 4, 5);

// 使えないメソッド
// stream.sum();              // NG: sum()は存在しない
// stream.average();          // NG: average()は存在しない
// stream.summaryStatistics(); // NG: summaryStatistics()は存在しない

// 使えるメソッド
long count = stream.count();  // OK
Optional<Integer> max = stream.max(Comparator.naturalOrder()); // OK
```

### IntStream - プリミティブint型のストリーム

```java
IntStream stream = IntStream.of(1, 2, 3, 4, 5);

// 使えるメソッド（Stream<Integer>にはない）
int sum = stream.sum();                    // OK
OptionalDouble avg = stream.average();     // OK
IntSummaryStatistics stats = stream.summaryStatistics(); // OK
OptionalInt max = stream.max();            // OK (Comparator不要)
```

### 比較表

| メソッド | Stream<Integer> | IntStream |
|---------|----------------|-----------|
| `count()` | `long` | `long` |
| `max()` | `Optional<Integer>` (Comparator必須) | `OptionalInt` (Comparator不要) |
| `min()` | `Optional<Integer>` (Comparator必須) | `OptionalInt` (Comparator不要) |
| `sum()` | **存在しない** | `int` |
| `average()` | **存在しない** | `OptionalDouble` |
| `summaryStatistics()` | **存在しない** | `IntSummaryStatistics` |

### 変換方法

```java
// Stream<Integer> -> IntStream
Stream<Integer> boxedStream = Stream.of(1, 2, 3);
IntStream intStream = boxedStream.mapToInt(Integer::intValue);

// IntStream -> Stream<Integer>
IntStream primitiveStream = IntStream.of(1, 2, 3);
Stream<Integer> boxed = primitiveStream.boxed();
```

## 試験ポイント・引っかけ問題

### 1. average()の戻り値

```java
// これはコンパイルエラー
IntStream stream = IntStream.of(1, 2, 3);
double avg = stream.average(); // NG: OptionalDoubleだから

// 正しい
OptionalDouble avg = stream.average(); // OK
```

**覚え方:** average()は**必ず**`OptionalDouble`を返す（空のストリームの可能性があるため）

### 2. max()/min()とComparator

```java
// Stream<T>の場合 - Comparator必須
Stream<Integer> s1 = Stream.of(1, 2, 3);
Optional<Integer> max = s1.max(Comparator.naturalOrder()); // OK
Optional<Integer> max = s1.max(); // NG: Comparator必須

// IntStreamの場合 - Comparator不要
IntStream s2 = IntStream.of(1, 2, 3);
OptionalInt max = s2.max(); // OK
```

### 3. sum()はプリミティブストリームのみ

```java
// これはコンパイルエラー
Stream<Integer> stream = Stream.of(1, 2, 3);
int sum = stream.sum(); // NG: sum()は存在しない

// mapToInt()で変換が必要
int sum = stream.mapToInt(Integer::intValue).sum(); // OK
```

### 4. reduce()の戻り値

```java
// identity付き -> T型
int sum1 = stream.reduce(0, (a, b) -> a + b); // int型

// identityなし -> Optional<T>
Optional<Integer> sum2 = stream.reduce((a, b) -> a + b); // Optional<Integer>
```

### 5. range() vs rangeClosed()

```java
IntStream.range(1, 5)       // 1,2,3,4 (5は含まない)
IntStream.rangeClosed(1, 5) // 1,2,3,4,5 (5を含む)
```

**覚え方:** `rangeClosed`は"Closed"（閉じてる）から終端を含む

### 6. summaryStatistics()はプリミティブストリームのみ

```java
// OK
IntSummaryStatistics stats = IntStream.of(1,2,3).summaryStatistics();

// NG: Stream<Integer>には存在しない
// IntSummaryStatistics stats = Stream.of(1,2,3).summaryStatistics();
```

### 7. 戻り値の型に注意

**よく出る引っかけ:**

```java
// count()の戻り値はlong（intじゃない）
long count = stream.count(); // OK
int count = stream.count(); // NG: longからintへの暗黙的な変換はできない

// sum()の戻り値
IntStream -> int
LongStream -> long
DoubleStream -> double

// average()の戻り値
全て OptionalDouble

// max()/min()の戻り値
Stream<T> -> Optional<T>
IntStream -> OptionalInt
LongStream -> OptionalLong
DoubleStream -> OptionalDouble
```

## まとめ

### 集約操作の種類

| 操作 | 戻り値 | 備考 |
|-----|-------|------|
| `count()` | `long` | 要素数 |
| `max()` | `Optional<T>` / `OptionalInt` 等 | Stream<T>はComparator必須 |
| `min()` | `Optional<T>` / `OptionalInt` 等 | Stream<T>はComparator必須 |
| `sum()` | `int`/`long`/`double` | プリミティブストリームのみ |
| `average()` | `OptionalDouble` | プリミティブストリームのみ |
| `summaryStatistics()` | `XxxSummaryStatistics` | プリミティブストリームのみ |
| `reduce()` | `T` または `Optional<T>` | identityの有無による |

### 絶対覚えるべきポイント

1. **average()は必ずOptionalDoubleを返す**
2. **sum()はプリミティブストリームのみ（Stream<Integer>にはない）**
3. **Stream<T>のmax()/min()はComparator必須、IntStream等は不要**
4. **count()の戻り値はlong（intじゃない）**
5. **reduce()でidentityがない場合はOptionalを返す**
6. **range(a,b)はbを含まない、rangeClosed(a,b)はbを含む**
7. **summaryStatistics()はプリミティブストリームのみ**

### よくある間違い

```java
// NG例
Stream.of(1,2,3).sum()  // Stream<Integer>にsum()はない
IntStream.of(1,2,3).average()  // 戻り値はOptionalDouble（doubleじゃない）
Stream.of(1,2,3).max()  // Comparatorが必要
int count = list.stream().count()  // longをintに代入できない

// OK例
Stream.of(1,2,3).mapToInt(i->i).sum()  // mapToIntで変換
OptionalDouble avg = IntStream.of(1,2,3).average()  // OptionalDouble
Stream.of(1,2,3).max(Comparator.naturalOrder())  // Comparator指定
long count = list.stream().count()  // long型で受け取る
```

これで Stream の集約操作はバッチリだね！試験頑張ろう！
