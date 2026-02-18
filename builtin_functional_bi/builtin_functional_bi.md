# Bi系関数型インターフェース完全ガイド

## 全体像

Java 11のGold試験で超頻出の「Bi系（2つの引数を取る）関数型インターフェース」とプリミティブ特殊化についてまとめるよ。

### 基本のBi系3兄弟

| インターフェース | シグネチャ | 用途 | 代表的なメソッド |
|--------------|----------|------|--------------|
| `BiFunction<T, U, R>` | `R apply(T t, U u)` | 2引数受け取って結果を返す | `Map.compute()`, `Map.replaceAll()` |
| `BiConsumer<T, U>` | `void accept(T t, U u)` | 2引数受け取って処理するだけ | `Map.forEach()` |
| `BiPredicate<T, U>` | `boolean test(T t, U u)` | 2引数受け取ってboolean返す | フィルタリング処理 |

### Operator系（引数と戻り値の型が同じ）

| インターフェース | 継承元 | シグネチャ | 特徴 |
|--------------|-------|----------|------|
| `UnaryOperator<T>` | `Function<T, T>` | `T apply(T t)` | 1引数、同じ型を返す |
| `BinaryOperator<T>` | `BiFunction<T, T, T>` | `T apply(T t1, T t2)` | 2引数、同じ型を返す |

### プリミティブ特殊化の命名規則

プリミティブ版は `Int`, `Long`, `Double` の3種類がある。命名規則を覚えればパターンで推測できる！

#### パターン1: 引数がプリミティブ
- `IntConsumer` → `void accept(int value)`
- `IntFunction<R>` → `R apply(int value)`
- `IntPredicate` → `boolean test(int value)`
- `IntSupplier` → `int getAsInt()`
- `IntUnaryOperator` → `int applyAsInt(int operand)`
- `IntBinaryOperator` → `int applyAsInt(int left, int right)`

#### パターン2: 戻り値がプリミティブ（ToXxx系）
- `ToIntFunction<T>` → `int applyAsInt(T value)`
- `ToDoubleFunction<T>` → `double applyAsDouble(T value)`
- `ToLongFunction<T>` → `long applyAsLong(T value)`
- `ToIntBiFunction<T, U>` → `int applyAsInt(T t, U u)`
- `ToDoubleBiFunction<T, U>` → `double applyAsDouble(T t, U u)`
- `ToLongBiFunction<T, U>` → `long applyAsLong(T t, U u)`

#### パターン3: オブジェクト + プリミティブ（ObjXxx系）
- `ObjIntConsumer<T>` → `void accept(T t, int value)`
- `ObjDoubleConsumer<T>` → `void accept(T t, double value)`
- `ObjLongConsumer<T>` → `void accept(T t, long value)`

## BiFunction<T, U, R>

### 基本的な使い方

```java
// 2つの数値を掛け算
BiFunction<Integer, Integer, Integer> multiply = (a, b) -> a * b;
System.out.println(multiply.apply(5, 3)); // 15

// 異なる型を受け取って別の型を返す
BiFunction<String, Integer, String> repeat = (str, times) -> str.repeat(times);
System.out.println(repeat.apply("Hi", 3)); // HiHiHi
```

### andThenメソッド

BiFunctionは `andThen()` で連鎖できる。ただし、**composeはない**から注意！

```java
BiFunction<String, String, String> concat = (s1, s2) -> s1 + s2;
BiFunction<String, String, String> concatAndUpper =
    concat.andThen(String::toUpperCase);
System.out.println(concatAndUpper.apply("hello", "world")); // HELLOWORLD
```

### 試験のポイント

- ❌ `BiFunction.compose()` は存在しない（Functionにはあるけど）
- ✅ `andThen()` は使える（結果に対して追加処理）
- ✅ 型パラメータは3つ（T, U, R）で、全部違っててもOK

## BiConsumer<T, U>

### Map.forEachでの典型的な使用例

これが一番よく出る使い方である！

```java
Map<String, Integer> scores = Map.of("Alice", 85, "Bob", 92);
scores.forEach((name, score) ->
    System.out.println(name + ": " + score)
);
```

### andThenでの連鎖

```java
BiConsumer<String, Integer> logger = (k, v) ->
    System.out.println("Processing: " + k);
BiConsumer<String, Integer> saver = (k, v) ->
    System.out.println("Saving: " + k + "=" + v);

BiConsumer<String, Integer> logAndSave = logger.andThen(saver);
logAndSave.accept("key1", 100);
// 出力:
// Processing: key1
// Saving: key1=100
```

### 試験のポイント

- ✅ `andThen()` で連鎖できる
- ✅ `Map.forEach()` の引数として使われる
- ❌ 戻り値はvoid（何も返さない）

## BiPredicate<T, U>

### 基本的な使い方

```java
// 2つの数値を比較
BiPredicate<Integer, Integer> isGreater = (a, b) -> a > b;
System.out.println(isGreater.test(10, 5)); // true

// 文字列とその長さをチェック
BiPredicate<String, Integer> isLongerThan = (str, len) -> str.length() > len;
System.out.println(isLongerThan.test("Hello", 3)); // true
```

### 論理演算メソッド

Predicateと同じく、`and()`, `or()`, `negate()` が使えるよ。

```java
BiPredicate<Integer, Integer> isEqual = (a, b) -> a.equals(b);
BiPredicate<Integer, Integer> isGreater = (a, b) -> a > b;
BiPredicate<Integer, Integer> isGreaterOrEqual = isGreater.or(isEqual);

System.out.println(isGreaterOrEqual.test(5, 5)); // true
System.out.println(isGreaterOrEqual.test(3, 5)); // false
```

### 試験のポイント

- ✅ `and()`, `or()`, `negate()` が使える
- ✅ `test()` メソッドでbooleanを返す
- ✅ StreamのfilterとかでMapのエントリをフィルタリングする時に便利

## UnaryOperator と BinaryOperator

### UnaryOperator<T> は Function<T, T> の特殊化

引数と戻り値の型が同じ場合に使うよ。

```java
// Function<String, String> の代わりに
UnaryOperator<String> toUpper = s -> s.toUpperCase();
System.out.println(toUpper.apply("hello")); // HELLO

// List.replaceAll() で使える
List<Integer> numbers = new ArrayList<>(List.of(1, 2, 3, 4, 5));
numbers.replaceAll(n -> n * 2);
System.out.println(numbers); // [2, 4, 6, 8, 10]
```

### BinaryOperator<T> は BiFunction<T, T, T> の特殊化

2つの引数と戻り値が全て同じ型の場合に使うよ。

```java
// BiFunction<Integer, Integer, Integer> の代わりに
BinaryOperator<Integer> add = (a, b) -> a + b;
System.out.println(add.apply(10, 20)); // 30

// maxBy, minBy静的メソッドが便利
BinaryOperator<Integer> max = BinaryOperator.maxBy(Integer::compareTo);
System.out.println(max.apply(5, 10)); // 10

BinaryOperator<String> min = BinaryOperator.minBy(String::compareTo);
System.out.println(min.apply("apple", "banana")); // apple
```

### Stream.reduce() での使用

```java
List<Integer> numbers = List.of(1, 2, 3, 4, 5);
int sum = numbers.stream()
    .reduce(0, (a, b) -> a + b); // BinaryOperator<Integer>
System.out.println(sum); // 15
```

### 試験のポイント

- ✅ `UnaryOperator<T>` extends `Function<T, T>`
- ✅ `BinaryOperator<T>` extends `BiFunction<T, T, T>`
- ✅ `BinaryOperator.maxBy()`, `minBy()` の静的メソッドを覚える
- ✅ `List.replaceAll()` は `UnaryOperator<E>` を引数に取る
- ✅ `Stream.reduce()` は `BinaryOperator<T>` を引数に取る

## プリミティブ特殊化の命名規則とコツ

### なぜプリミティブ特殊化が必要？

オートボクシング/アンボクシングのコストを避けるため。`Integer` じゃなくて `int` を直接扱える。

### 命名パターンから推測する方法

1. **接頭辞なし + プリミティブ型名** → 引数がプリミティブ
   - `IntConsumer` → intを受け取る
   - `IntFunction<R>` → intを受け取ってRを返す
   - `IntPredicate` → intを受け取ってbooleanを返す

2. **To + プリミティブ型名** → 戻り値がプリミティブ
   - `ToIntFunction<T>` → Tを受け取ってintを返す
   - `ToIntBiFunction<T, U>` → TとUを受け取ってintを返す

3. **Obj + プリミティブ型名** → オブジェクトとプリミティブのペア
   - `ObjIntConsumer<T>` → Tとintを受け取る

### Int系の完全リスト

```java
// 引数がint
IntConsumer ic = i -> System.out.println(i);
IntFunction<String> ifunc = i -> "Number: " + i;
IntPredicate ipred = i -> i % 2 == 0;
IntSupplier isupp = () -> 42;
IntUnaryOperator iuo = i -> i * 2;
IntBinaryOperator ibo = (a, b) -> a + b;

// 戻り値がint
ToIntFunction<String> tif = s -> s.length();
ToIntBiFunction<String, String> tibf = (s1, s2) -> s1.length() + s2.length();

// オブジェクト + int
ObjIntConsumer<String> oic = (str, i) -> System.out.println(str + ": " + i);
```

Long系とDouble系も同じパターンである！

### 試験で引っかかりやすいポイント

❌ 存在しないもの:
- `IntBiFunction` → 存在しない（`IntBinaryOperator`を使う）
- `BiIntFunction` → 存在しない
- `IntToFunction` → 存在しない（`IntFunction<R>`を使う）

✅ 存在するもの:
- `IntFunction<R>` → intを受け取ってRを返す
- `ToIntFunction<T>` → Tを受け取ってintを返す
- `IntUnaryOperator` → intを受け取ってintを返す
- `IntBinaryOperator` → 2つのintを受け取ってintを返す

### メソッド名の違い

| インターフェース | メソッド名 |
|--------------|----------|
| `Function<T, R>` | `R apply(T t)` |
| `IntFunction<R>` | `R apply(int value)` |
| `ToIntFunction<T>` | `int applyAsInt(T value)` |
| `IntUnaryOperator` | `int applyAsInt(int operand)` |
| `Supplier<T>` | `T get()` |
| `IntSupplier` | `int getAsInt()` |
| `Consumer<T>` | `void accept(T t)` |
| `IntConsumer` | `void accept(int value)` |

プリミティブを**返す**場合は `xxxAsInt()`, `xxxAsLong()`, `xxxAsDouble()` になるよ！

## Map操作との組み合わせ

Mapの便利メソッドは、ほとんどがBi系関数型インターフェースを使ってるんである。

### Map.forEach() - BiConsumer<K, V>

```java
Map<String, Integer> map = Map.of("a", 1, "b", 2);
map.forEach((key, value) -> System.out.println(key + "=" + value));
```

### Map.replaceAll() - BiFunction<K, V, V>

全ての値を変換する。

```java
Map<String, Integer> scores = new HashMap<>(Map.of("Alice", 80, "Bob", 90));
scores.replaceAll((name, score) -> score + 10); // 全員に10点加算
// {Alice=90, Bob=100}
```

### Map.compute() - BiFunction<K, V, V>

キーに対して計算を実行。既存の値がnullの可能性もある。

```java
Map<String, Integer> map = new HashMap<>();
map.put("count", 5);

// 既存の値に3を足す
map.compute("count", (key, value) -> value == null ? 3 : value + 3);
// {count=8}

// 存在しないキーには新しい値を設定
map.compute("newKey", (key, value) -> value == null ? 1 : value + 1);
// {count=8, newKey=1}
```

### Map.computeIfPresent() - BiFunction<K, V, V>

キーが存在する場合のみ計算。valueは絶対にnullじゃない。

```java
Map<String, Integer> map = new HashMap<>(Map.of("count", 5));
map.computeIfPresent("count", (key, value) -> value + 10);
// {count=15}

map.computeIfPresent("missing", (key, value) -> value + 10);
// キーが存在しないので何もしない
```

### Map.computeIfAbsent() - Function<K, V>

キーが存在しない場合のみ計算。**これはFunctionだから注意！**（Bi系じゃない）

```java
Map<String, Integer> map = new HashMap<>();
map.computeIfAbsent("count", key -> 100);
// {count=100}

map.computeIfAbsent("count", key -> 200);
// キーが存在するので何もしない。{count=100}のまま
```

### Map.merge() - BiFunction<V, V, V>

値をマージする。**引数は (oldValue, newValue) の順**である！

```java
Map<String, Integer> map = new HashMap<>(Map.of("count", 5));

// 既存の値に7を加算
map.merge("count", 7, (oldVal, newVal) -> oldVal + newVal);
// {count=12}

// 存在しないキーには第2引数の値がそのまま使われる
map.merge("newKey", 10, (oldVal, newVal) -> oldVal + newVal);
// {count=12, newKey=10}
```

### 試験のポイント

| メソッド | 引数の関数型 | キーが存在しない時 | 値がnullの可能性 |
|---------|-----------|---------------|--------------|
| `forEach()` | `BiConsumer<K, V>` | - | なし |
| `replaceAll()` | `BiFunction<K, V, V>` | - | なし |
| `compute()` | `BiFunction<K, V, V>` | 関数が実行される | **ある** |
| `computeIfPresent()` | `BiFunction<K, V, V>` | 関数が実行されない | **ない** |
| `computeIfAbsent()` | `Function<K, V>` | 関数が実行される | - |
| `merge()` | `BiFunction<V, V, V>` | 第2引数の値を使う | **ない** |

**超重要**: `compute()` だけは第2引数がnullの可能性があるから、nullチェックが必要！

```java
// ❌ これはNullPointerExceptionになる可能性がある
map.compute("key", (k, v) -> v + 1);

// ✅ 正しい書き方
map.compute("key", (k, v) -> v == null ? 1 : v + 1);
```

## 試験でよく出る引っかけ問題

### 引っかけ1: composeの有無

```java
// ❌ BiFunctionにはcomposeがない
BiFunction<String, String, String> bf = (s1, s2) -> s1 + s2;
bf.compose(String::toUpperCase); // コンパイルエラー！

// ✅ andThenは使える
bf.andThen(String::toUpperCase); // OK
```

### 引っかけ2: プリミティブ特殊化の名前

```java
// ❌ IntBiFunctionは存在しない
IntBiFunction<String> ibf = ...; // コンパイルエラー

// ✅ 正しくはIntBinaryOperator
IntBinaryOperator ibo = (a, b) -> a + b;

// ❌ BiIntFunctionも存在しない
BiIntFunction<String, String, Integer> bif = ...; // コンパイルエラー

// ✅ 正しくはToIntBiFunction
ToIntBiFunction<String, String> tibf = (s1, s2) -> s1.length() + s2.length();
```

### 引っかけ3: Mapメソッドの引数の型

```java
Map<String, Integer> map = new HashMap<>();

// ❌ computeIfAbsentはBiFunctionじゃない
map.computeIfAbsent("key", (k, v) -> v + 1); // コンパイルエラー

// ✅ FunctionだからKeyだけが引数
map.computeIfAbsent("key", k -> 1); // OK

// ❌ mergeの引数の順序を間違える
map.merge("key", 10, (newVal, oldVal) -> newVal + oldVal); // 論理エラー

// ✅ 正しくは (oldVal, newVal)
map.merge("key", 10, (oldVal, newVal) -> oldVal + newVal); // OK
```

### 引っかけ4: 型パラメータの数

```java
// ❌ BinaryOperatorに3つの型パラメータ
BinaryOperator<Integer, Integer, Integer> bo = ...; // コンパイルエラー

// ✅ BinaryOperatorは1つだけ
BinaryOperator<Integer> bo = (a, b) -> a + b; // OK

// ✅ BiFunctionは3つ必要
BiFunction<Integer, Integer, Integer> bf = (a, b) -> a + b; // OK
```

### 引っかけ5: computeでのnullチェック

```java
Map<String, Integer> map = new HashMap<>();

// ❌ valueがnullの可能性を考慮していない
map.compute("key", (k, v) -> v + 1); // NullPointerException

// ✅ nullチェックが必要
map.compute("key", (k, v) -> v == null ? 1 : v + 1); // OK

// ✅ computeIfPresentならnullチェック不要
map.computeIfPresent("key", (k, v) -> v + 1); // OK（キーが存在すればvはnullじゃない）
```

## まとめ: 覚え方のコツ

### 1. Bi系は「2つの引数」がキーワード
- `BiFunction<T, U, R>` → 2引数、結果を返す
- `BiConsumer<T, U>` → 2引数、返さない
- `BiPredicate<T, U>` → 2引数、booleanを返す

### 2. Operatorは「同じ型」がキーワード
- `UnaryOperator<T>` → 1引数、同じ型を返す
- `BinaryOperator<T>` → 2引数、同じ型を返す

### 3. プリミティブ特殊化は「位置」で判断
- **先頭にInt** → 引数がint (`IntConsumer`, `IntFunction`)
- **先頭にTo** → 戻り値がint (`ToIntFunction`)
- **先頭にObj** → オブジェクト+プリミティブ (`ObjIntConsumer`)

### 4. Mapメソッドのパターン
- `forEach` → `BiConsumer`
- `replaceAll` → `BiFunction<K, V, V>`
- `compute` → `BiFunction<K, V, V>` (nullチェック必要)
- `computeIfPresent` → `BiFunction<K, V, V>` (nullチェック不要)
- `computeIfAbsent` → `Function<K, V>` (Bi系じゃない！)
- `merge` → `BiFunction<V, V, V>` (引数は oldVal, newVal の順)

### 5. 存在しないものを覚える
- ❌ `BiFunction.compose()` → ない（andThenはある）
- ❌ `IntBiFunction`, `BiIntFunction` → ない
- ❌ `IntToFunction` → ない（`IntFunction<R>` か `ToIntFunction<T>` を使う）

この辺をしっかり押さえておけば、試験で迷わないはず！頑張って！
