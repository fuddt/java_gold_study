# Stream API 基本編

## Stream APIとは

Java 8で導入された、コレクションやデータ列を関数型スタイルで処理するためのAPIだよね。ラムダ式と組み合わせて使うことで、簡潔で読みやすいコードが書けるんだ。

従来のforループと比べて：
- 宣言的で読みやすい
- 並列処理が簡単
- 遅延評価で効率的
- 不変性を保ちやすい

## Streamパイプラインの仕組み

Streamの処理は必ず3つの段階で構成されるよ：

```
ソース → 中間操作 → 終端操作
```

### 1. ソース（Source）
Streamの生成元。コレクション、配列、ファイル、生成関数などから作られる。

### 2. 中間操作（Intermediate Operations）
Streamを返す操作。複数連鎖できる。**遅延評価**されるのがポイント！

主な中間操作：
- `filter()` - 条件でフィルタリング
- `map()` - 要素を変換
- `flatMap()` - 要素を平坦化
- `distinct()` - 重複除去
- `sorted()` - ソート
- `peek()` - 副作用実行（デバッグ用）
- `limit()` - 要素数制限
- `skip()` - 先頭スキップ

### 3. 終端操作（Terminal Operations）
Streamを消費して結果を返す操作。これが呼ばれて初めて実際の処理が実行される。

主な終端操作：
- `forEach()` - 各要素に処理実行
- `count()` - 要素数カウント
- `collect()` - コレクションに収集
- `reduce()` - 要素を集約
- `anyMatch()/allMatch()/noneMatch()` - 条件チェック
- `findFirst()/findAny()` - 要素検索
- `min()/max()` - 最小値/最大値

### パイプラインの例

```java
List<String> result = list.stream()        // ソース
    .filter(s -> s.length() > 3)           // 中間操作
    .map(String::toUpperCase)              // 中間操作
    .sorted()                              // 中間操作
    .collect(Collectors.toList());         // 終端操作
```

## Stream生成方法

### 1. Stream.of() - 可変長引数から

```java
Stream<String> stream = Stream.of("a", "b", "c");
Stream<Integer> nums = Stream.of(1, 2, 3, 4, 5);
```

ポイント：
- 要素を直接指定してStreamを作る最もシンプルな方法
- 可変長引数なので任意の個数を指定できる

### 2. Collection.stream() - コレクションから

```java
List<String> list = Arrays.asList("apple", "banana", "cherry");
Stream<String> stream = list.stream();

Set<Integer> set = new HashSet<>(Arrays.asList(1, 2, 3));
Stream<Integer> stream2 = set.stream();
```

ポイント：
- 実務で最もよく使うパターン
- Collection系（List、Set等）は全て`.stream()`メソッドを持つ

### 3. Arrays.stream() - 配列から

```java
String[] array = {"Java", "Python", "Go"};
Stream<String> stream = Arrays.stream(array);

// プリミティブ配列も可能
int[] nums = {1, 2, 3, 4, 5};
IntStream intStream = Arrays.stream(nums);

// 範囲指定も可能（開始インデックス, 終了インデックス）
IntStream rangeStream = Arrays.stream(nums, 1, 4); // インデックス1〜3
```

ポイント：
- 配列からStream生成する標準的な方法
- 範囲指定で部分配列からStreamを作れる

### 4. Stream.generate() - 無限ストリーム（Supplier使用）

```java
// 同じ値を無限に生成
Stream<String> infiniteStream = Stream.generate(() -> "Hello")
    .limit(5); // limitで制限しないと無限に生成される！

// ランダム値を生成
Stream<Double> randomStream = Stream.generate(Math::random)
    .limit(10);
```

ポイント：
- `Supplier`を使って値を生成
- **無限ストリーム**なので必ず`limit()`で制限すること
- 状態を持たない生成に向いている

### 5. Stream.iterate() - 初期値と関数で生成

```java
// 1から始まって1ずつ増える無限ストリーム
Stream<Integer> stream = Stream.iterate(1, n -> n + 1)
    .limit(10); // 1, 2, 3, ..., 10

// Java 9以降：条件付きiterate
Stream<Integer> stream2 = Stream.iterate(1, n -> n <= 10, n -> n + 1);
// 条件(n <= 10)を満たす間だけ生成
```

ポイント：
- `iterate(初期値, 次の値を生成する関数)`
- 前の値を使って次の値を生成できる
- **無限ストリーム**なのでlimitが必要（Java 9以降は条件で制御可）
- フィボナッチ数列など連続的なデータ生成に便利

### 6. IntStream.range() / rangeClosed()

```java
// 終端を含まない
IntStream range = IntStream.range(1, 5);  // 1, 2, 3, 4

// 終端を含む
IntStream rangeClosed = IntStream.rangeClosed(1, 5);  // 1, 2, 3, 4, 5
```

ポイント：
- `range(start, end)` - endは含まない（半開区間）
- `rangeClosed(start, end)` - endも含む（閉区間）
- forループの代わりによく使われる
- プリミティブ型なのでボクシングのオーバーヘッドがない

### 7. プリミティブ型特化Stream

```java
IntStream intStream = IntStream.of(1, 2, 3);
LongStream longStream = LongStream.of(100L, 200L, 300L);
DoubleStream doubleStream = DoubleStream.of(1.1, 2.2, 3.3);

// 統計情報が簡単に取れる
IntSummaryStatistics stats = intStream.summaryStatistics();
System.out.println(stats.getAverage());
System.out.println(stats.getMax());
```

ポイント：
- `IntStream`, `LongStream`, `DoubleStream`の3種類
- ボクシング/アンボクシングのコストがない（高速）
- `sum()`, `average()`, `max()`, `min()`などの統計メソッドが豊富

## Streamは遅延評価（Lazy Evaluation）

これは超重要！**中間操作は終端操作が呼ばれるまで実行されない**んだよね。

### 遅延評価の例

```java
Stream<String> stream = Stream.of("a", "b", "c")
    .filter(s -> {
        System.out.println("filter: " + s);
        return true;
    })
    .map(s -> {
        System.out.println("map: " + s);
        return s.toUpperCase();
    });

System.out.println("まだ何も出力されない");

// 終端操作を呼んで初めて実行される
stream.forEach(System.out::println);
```

出力：
```
まだ何も出力されない
filter: a
map: a
A
filter: b
map: b
B
filter: c
map: c
C
```

### 遅延評価のメリット

1. **効率的な処理**
   ```java
   // 最初の3つが見つかった時点で処理が止まる
   list.stream()
       .filter(expensiveOperation())  // 全要素に対して実行されない
       .limit(3)
       .collect(Collectors.toList());
   ```

2. **無限ストリームの扱い**
   ```java
   // 無限ストリームでも問題なし（limitで打ち切られる）
   Stream.iterate(0, n -> n + 1)
       .filter(n -> n % 2 == 0)
       .limit(10)
       .forEach(System.out::println);
   ```

3. **短絡評価（Short-circuit）**
   ```java
   // anyMatchは最初に条件を満たす要素が見つかった時点で終了
   boolean hasEven = list.stream()
       .peek(n -> System.out.println("checking: " + n))
       .anyMatch(n -> n % 2 == 0);
   ```

### 試験ポイント：遅延評価

```java
// ❌ 終端操作がないので何も実行されない
Stream.of(1, 2, 3)
    .filter(n -> {
        System.out.println(n);  // 出力されない！
        return n > 1;
    });

// ✅ 終端操作があるので実行される
Stream.of(1, 2, 3)
    .filter(n -> {
        System.out.println(n);  // 出力される
        return n > 1;
    })
    .count();
```

## Streamは使い捨て（Single-Use）

**Streamは一度終端操作を呼んだら再利用できない**。これも超重要ポイントだよね。

### 再利用できない例

```java
Stream<String> stream = Stream.of("a", "b", "c");

// 1回目の使用
stream.forEach(System.out::println);  // OK

// 2回目の使用
stream.forEach(System.out::println);  // ❌ IllegalStateException!
```

エラーメッセージ：
```
java.lang.IllegalStateException: stream has already been operated upon or closed
```

### 正しい再利用方法

```java
// ソースのコレクションを保持して、毎回新しいStreamを生成する
List<String> list = Arrays.asList("a", "b", "c");

list.stream().forEach(System.out::println);  // 1回目
list.stream().forEach(System.out::println);  // 2回目（OK）
```

### Streamを変数に保存する場合の注意

```java
// ❌ 悪い例：Streamを変数に保存
Stream<String> stream = list.stream().filter(s -> s.length() > 3);
long count1 = stream.count();
long count2 = stream.count();  // エラー！

// ✅ 良い例1：その都度生成
long count1 = list.stream().filter(s -> s.length() > 3).count();
long count2 = list.stream().filter(s -> s.length() > 3).count();

// ✅ 良い例2：Supplier<Stream>として保存
Supplier<Stream<String>> streamSupplier =
    () -> list.stream().filter(s -> s.length() > 3);
long count1 = streamSupplier.get().count();
long count2 = streamSupplier.get().count();  // OK
```

## Java Gold試験のポイント・引っかけ問題

### 1. 終端操作がないコード

```java
// ❌ 何も出力されない（終端操作がない）
list.stream()
    .filter(s -> {
        System.out.println(s);
        return true;
    })
    .map(String::toUpperCase);

// ✅ forEachで終端操作を追加
list.stream()
    .filter(s -> {
        System.out.println(s);
        return true;
    })
    .map(String::toUpperCase)
    .forEach(x -> {});  // 終端操作
```

### 2. Streamの再利用

```java
Stream<Integer> stream = Stream.of(1, 2, 3);
System.out.println(stream.count());      // 3
System.out.println(stream.count());      // ❌ IllegalStateException
```

### 3. range vs rangeClosed

```java
IntStream.range(1, 5).forEach(System.out::print);       // 1234
IntStream.rangeClosed(1, 5).forEach(System.out::print); // 12345
```

### 4. generate と iterate の違い

```java
// generate：状態を持たない（毎回同じSupplierを呼ぶ）
Stream.generate(() -> Math.random()).limit(3);

// iterate：前の値を使う（状態を持つ）
Stream.iterate(1, n -> n + 1).limit(3);  // 1, 2, 3
```

### 5. プリミティブStreamへの変換

```java
List<Integer> list = Arrays.asList(1, 2, 3);

// ❌ コンパイルエラー
IntStream s1 = list.stream();

// ✅ mapToIntで変換が必要
IntStream s2 = list.stream().mapToInt(Integer::intValue);
```

### 6. 中間操作の順序による効率

```java
// ❌ 非効率（全要素をmapしてからfilter）
list.stream()
    .map(expensiveOperation())
    .filter(s -> s.length() > 3)
    .collect(Collectors.toList());

// ✅ 効率的（filterしてから必要な要素だけmap）
list.stream()
    .filter(s -> s.length() > 3)
    .map(expensiveOperation())
    .collect(Collectors.toList());
```

### 7. peekは中間操作（終端操作ではない）

```java
// ❌ 何も出力されない（peekは中間操作なので終端操作が必要）
Stream.of(1, 2, 3).peek(System.out::println);

// ✅ forEachまたは他の終端操作を追加
Stream.of(1, 2, 3).peek(System.out::println).count();
```

### 8. 無限ストリームの取り扱い

```java
// ❌ 終了しない（limitがない）
Stream.generate(() -> "a").forEach(System.out::println);

// ✅ limitで制限
Stream.generate(() -> "a").limit(10).forEach(System.out::println);

// ✅ takeWhileで条件指定（Java 9+）
Stream.iterate(1, n -> n + 1).takeWhile(n -> n <= 10).forEach(System.out::println);
```

### 9. Arrays.streamの範囲指定

```java
int[] arr = {1, 2, 3, 4, 5};
Arrays.stream(arr, 1, 4);  // インデックス1, 2, 3（4は含まない）
// 出力: 2, 3, 4
```

### 10. 空のStream

```java
Stream<String> empty1 = Stream.empty();          // 空のStream
Stream<String> empty2 = Stream.of();             // これも空
Stream<String> empty3 = Collections.emptyList().stream();  // これも空

empty1.count();  // 0
```

## まとめ

### Stream APIの重要ポイント

1. **パイプライン構造**：ソース → 中間操作 → 終端操作
2. **遅延評価**：終端操作が呼ばれるまで実行されない
3. **使い捨て**：一度使ったStreamは再利用できない
4. **中間操作は複数連鎖可能**、終端操作は1つだけ
5. **プリミティブStream**（IntStream等）は効率的

### 試験で絶対出るポイント

- 終端操作がないコードは何も実行されない
- Streamの再利用は`IllegalStateException`
- `range`は終端を含まない、`rangeClosed`は含む
- `generate`と`iterate`の違い
- `peek`は中間操作（終端操作じゃない）
- 無限ストリームには`limit`が必要

これだけ押さえておけば、Stream APIの基本は完璧じゃね？
