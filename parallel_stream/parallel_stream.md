# Parallel Stream 完全攻略ガイド

## 1. Parallel Streamの基本

### 1.1 作成方法

Parallel Streamを作る方法は2つあるよね。

```java
List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);

// 方法1: parallelStream()
Stream<Integer> parallel1 = list.parallelStream();

// 方法2: stream().parallel()
Stream<Integer> parallel2 = list.stream().parallel();

// 確認
System.out.println(parallel1.isParallel());  // true
```

どっちを使っても同じ結果になる。コレクションから直接作る場合は`parallelStream()`が楽だね。

### 1.2 並列処理の仕組み

Parallel Streamは内部的に**Fork/Join Framework**を使ってる。

- データを複数のチャンクに分割
- 各チャンクを異なるスレッドで処理
- 結果をマージして最終結果を生成

デフォルトでは**共通のForkJoinPool**を使用し、スレッド数は`Runtime.getRuntime().availableProcessors()`で決まる（通常はCPUコア数）。

### 1.3 シーケンシャルに戻す

並列ストリームをシーケンシャルに戻すこともできる。

```java
Stream<Integer> seq = list.parallelStream().sequential();
System.out.println(seq.isParallel());  // false
```

## 2. forEach vs forEachOrdered（超重要！）

これは試験でよく出るポイントだね。

### 2.1 forEach()

並列ストリームで`forEach()`を使うと、**順序が保証されない**。

```java
List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);

// 順序がバラバラになる可能性がある
list.parallelStream().forEach(System.out::println);
// 出力例: 5, 2, 7, 1, 4, 3, 8, 6
```

### 2.2 forEachOrdered()

順序を保証したい場合は`forEachOrdered()`を使う。

```java
// 順序が保証される
list.parallelStream().forEachOrdered(System.out::println);
// 出力: 1, 2, 3, 4, 5, 6, 7, 8
```

**ただし注意！** `forEachOrdered()`を使うと並列処理のメリットが減る。順序を保証するためにスレッド間の同期が必要になる。

### 2.3 試験での引っかけポイント

```java
// これは順序が保証される？されない？
list.parallelStream()
    .map(n -> n * 2)
    .collect(Collectors.toList());
```

**答え: 順序は保証される！**

`collect(Collectors.toList())`の結果リストは元の順序を保持する。並列処理中の処理順序は不定だが、結果のコレクションは順序が保たれるんである。

ただし、`forEach()`での出力順序は不定：

```java
list.parallelStream()
    .map(n -> n * 2)
    .forEach(System.out::println);  // バラバラに出力される
```

## 3. reduce()の並列処理（超超重要！！）

これが一番重要なポイントではないだろうか特に**combinerが実際に使われる**ってとこ。

### 3.1 reduce()の3つの引数

```java
T reduce(T identity,
         BiFunction<T, T, T> accumulator,
         BinaryOperator<T> combiner)
```

- **identity**: 初期値
- **accumulator**: 要素を累積する関数
- **combiner**: 部分結果を結合する関数

### 3.2 シーケンシャルストリームでは？

シーケンシャルストリームでは**combinerは使われない**！

```java
int sum = list.stream().reduce(
    0,
    (a, b) -> a + b,  // accumulatorだけ使われる
    (a, b) -> a + b   // combinerは呼ばれない！
);
```

### 3.3 パラレルストリームでは？

パラレルストリームでは**combinerが実際に使われる**！

```java
int sum = list.parallelStream().reduce(
    0,
    (a, b) -> {
        System.out.println("accumulator: " + a + " + " + b);
        return a + b;
    },
    (a, b) -> {
        System.out.println("combiner: " + a + " + " + b);  // これが実際に呼ばれる！
        return a + b;
    }
);
```

**処理の流れ:**

1. データを複数のチャンクに分割
2. 各チャンクでaccumulatorを使って部分結果を計算
3. **combinerで部分結果を結合**

```
データ: [1, 2, 3, 4, 5, 6, 7, 8]

チャンク1: [1, 2, 3, 4]
  accumulator: 0 + 1 = 1
  accumulator: 1 + 2 = 3
  accumulator: 3 + 3 = 6
  accumulator: 6 + 4 = 10

チャンク2: [5, 6, 7, 8]
  accumulator: 0 + 5 = 5
  accumulator: 5 + 6 = 11
  accumulator: 11 + 7 = 18
  accumulator: 18 + 8 = 26

combiner: 10 + 26 = 36  ← ここで結合！
```

### 3.4 combinerが間違っているとどうなる？

これが試験の引っかけポイント！

```java
int wrong = list.parallelStream().reduce(
    0,
    (a, b) -> a + b,     // accumulator: 足し算
    (a, b) -> a - b      // combiner: 引き算（間違い！）
);
// 結果が間違う！
```

combinerはaccumulatorと**同じロジック**でないといけない。これ超重要。

### 3.5 試験での引っかけ問題例

```java
// Q: この結果は？
List<Integer> list = Arrays.asList(1, 2, 3, 4);
int result = list.parallelStream().reduce(
    10,              // identity
    (a, b) -> a + b,
    (a, b) -> a + b
);
```

**A: 期待と違う結果になる！**

シーケンシャルなら: 10 + 1 + 2 + 3 + 4 = 20

パラレルだと:
```
チャンク1: 10 + 1 + 2 = 13
チャンク2: 10 + 3 + 4 = 17  ← identityが各チャンクで使われる！
combiner: 13 + 17 = 30
```

identityは**真の恒等元**でないといけない（足し算なら0、掛け算なら1）。

## 4. 分解と縮小（Spliterator）

### 4.1 Spliteratorとは？

Parallel Streamは内部的に**Spliterator**を使ってデータを分割する。

- **Split**: データを分割
- **Iterator**: 要素を反復

### 4.2 データ構造による違い

データ構造によって分割のしやすさが違うんである。

**分割しやすい（並列処理向き）:**
- `ArrayList`: インデックスアクセスで簡単に分割できる
- `Array`: 同上
- `IntStream.range()`: 範囲を分割できる
- `HashSet`: ある程度分割可能
- `TreeSet`: ある程度分割可能

**分割しにくい（並列処理不向き）:**
- `LinkedList`: 順次アクセスが必要
- `Stream.iterate()`: 前の要素に依存
- ファイルI/O系のStream: 順次読み込みが基本

```java
// 並列処理に向いている
ArrayList<Integer> arrayList = new ArrayList<>(Arrays.asList(1, 2, 3, ...));
arrayList.parallelStream()...  // 効率的

// 並列処理に向いていない
LinkedList<Integer> linkedList = new LinkedList<>(Arrays.asList(1, 2, 3, ...));
linkedList.parallelStream()...  // 非効率的（分割が遅い）
```

## 5. 並列ストリームで注意すべきこと

### 5.1 副作用を避ける

並列ストリームでは**副作用（side effect）のある操作は危険**。

#### NG例1: 外部変数の変更

```java
List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
List<Integer> result = new ArrayList<>();  // スレッドセーフじゃない

// これはダメ！データ競合が発生する
list.parallelStream()
    .forEach(n -> result.add(n * 2));  // 危険！

System.out.println(result.size());  // 5にならない可能性がある
```

**なぜダメ？**
- 複数のスレッドが同時に`result.add()`を呼ぶ
- `ArrayList`はスレッドセーフじゃない
- データが失われたり、例外が発生したりする

#### OK例: collect()を使う

```java
List<Integer> result = list.parallelStream()
    .map(n -> n * 2)
    .collect(Collectors.toList());  // スレッドセーフ
```

`collect()`は内部的にスレッドセーフに実装されてるから安全。

#### NG例2: カウンタの更新

```java
int count = 0;  // これもダメ

list.parallelStream()
    .forEach(n -> count++);  // コンパイルエラー（finalじゃない変数にアクセス）
```

これはコンパイルエラーになるけど、AtomicIntegerを使っても避けるべき：

```java
AtomicInteger count = new AtomicInteger(0);

list.parallelStream()
    .forEach(n -> count.incrementAndGet());  // 動くけど遅い

// 代わりにcount()を使う
long count = list.parallelStream().count();  // こっちの方がいい
```

### 5.2 状態を持つ操作は避ける

**ステートフル（stateful）な操作**は並列処理で問題になることがある。

#### ステートレス vs ステートフル

**ステートレス（OK）:**
- `map()`: 各要素を独立に変換
- `filter()`: 各要素を独立に判定
- `flatMap()`: 各要素を独立に展開

**ステートフル（注意）:**
- `distinct()`: 全要素を把握する必要がある
- `sorted()`: 全要素をソートする必要がある
- `limit()`: 何個取ったか数える必要がある
- `skip()`: 何個スキップしたか数える必要がある

ステートフルな操作は並列処理で**パフォーマンスが悪化**する可能性がある。

```java
// これは遅い
list.parallelStream()
    .sorted()      // 全要素をソート（並列処理のメリットが減る）
    .limit(10)     // 上位10個を取得（並列処理のメリットが減る）
    .collect(Collectors.toList());
```

### 5.3 スレッドセーフじゃないコレクション

並列ストリームで使うコレクションは注意が必要。

**スレッドセーフ:**
- 標準のCollectors（`toList()`, `toSet()`, `groupingBy()`等）
- `ConcurrentHashMap`
- `CopyOnWriteArrayList`

**スレッドセーフじゃない:**
- `ArrayList`
- `HashMap`
- `HashSet`

```java
// NG: 外部のHashMapに書き込む
Map<Integer, String> map = new HashMap<>();  // スレッドセーフじゃない
list.parallelStream()
    .forEach(n -> map.put(n, String.valueOf(n)));  // 危険！

// OK: Collectorsを使う
Map<Integer, String> map = list.parallelStream()
    .collect(Collectors.toMap(
        n -> n,
        n -> String.valueOf(n)
    ));  // 安全
```

## 6. 並列ストリームが有効な場面

### 6.1 有効な場面

以下の条件を**全て**満たす場合に並列ストリームが有効：

1. **データ量が多い**
   - 最低でも数千要素以上
   - 理想は数万〜数百万要素

2. **処理が重い**
   - CPU負荷の高い計算
   - 暗号化/復号化
   - 画像処理
   - 複雑なアルゴリズム

3. **ステートレスな操作**
   - `map()`, `filter()`, `reduce()`等
   - 副作用なし

4. **分割しやすいデータ構造**
   - `ArrayList`, 配列, `HashSet`等

#### 良い例

```java
// 大量のデータに重い処理
List<String> passwords = ...;  // 100万個のパスワード

List<String> hashed = passwords.parallelStream()
    .map(pwd -> expensiveHashFunction(pwd))  // 重い処理
    .collect(Collectors.toList());

// これは並列化で大幅に高速化される
```

### 6.2 逆効果な場面

以下の場合は並列ストリームを使うべきじゃない：

1. **データ量が少ない**
   - 数百要素以下
   - 並列化のオーバーヘッドが処理時間より大きい

2. **処理が軽い**
   - 単純な計算（足し算、掛け算等）
   - getter/setterの呼び出しのみ

3. **順序が重要**
   - `limit()`, `skip()`, `findFirst()`等
   - 並列化で順序保証のコストがかかる

4. **ステートフルな操作が多い**
   - `sorted()`, `distinct()`等

5. **I/O処理**
   - ファイル読み書き
   - ネットワーク通信
   - データベースアクセス
   - ただし、I/O待ちが多い場合は逆に効果的なこともある

#### 悪い例

```java
// データ量が少ない
List<Integer> small = Arrays.asList(1, 2, 3, 4, 5);
small.parallelStream()
    .map(n -> n * 2)  // 軽い処理
    .collect(Collectors.toList());
// シーケンシャルの方が速い

// 順序が重要
list.parallelStream()
    .sorted()
    .limit(10)
    .collect(Collectors.toList());
// 並列化のメリットがほぼない
```

## 7. findAny() vs findFirst()

### 7.1 シーケンシャルストリーム

シーケンシャルでは`findAny()`も`findFirst()`も同じ動作（最初の要素を返す）。

```java
Optional<Integer> any = list.stream()
    .filter(n -> n > 3)
    .findAny();  // 通常は4を返す

Optional<Integer> first = list.stream()
    .filter(n -> n > 3)
    .findFirst();  // 4を返す
```

### 7.2 パラレルストリーム

パラレルでは動作が違う！

```java
// findAny(): 本当に「どれか」を返す（実行ごとに変わる可能性）
Optional<Integer> any = list.parallelStream()
    .filter(n -> n > 3)
    .findAny();  // 4, 5, 6, 7... どれが返ってくるか不定

// findFirst(): 最初の要素を返す（順序保証）
Optional<Integer> first = list.parallelStream()
    .filter(n -> n > 3)
    .findFirst();  // 必ず4を返す
```

**パフォーマンスの違い:**
- `findAny()`: 速い（最初に見つかった要素を返せばいい）
- `findFirst()`: 遅い（順序を保証するためのコストがかかる）

並列ストリームで順序が重要じゃない場合は`findAny()`を使うべし。

## 8. パフォーマンスの測定

### 8.1 オーバーヘッド

並列処理には以下のオーバーヘッドがある：

1. **スレッドの起動コスト**
2. **データの分割コスト**
3. **結果のマージコスト**
4. **スレッド間の同期コスト**

これらのコストが処理時間より大きい場合、並列化は逆効果。

### 8.2 目安

一般的な目安：

- **データ量**: 10,000要素以上で効果が出始める
- **処理時間**: 1要素あたり1マイクロ秒以上の処理
- **高速化率**: 理想的にはCPUコア数に近い倍率（実際は1.5〜3倍程度が多い）

### 8.3 測定例

```java
List<Integer> list = IntStream.range(0, 1_000_000)
    .boxed()
    .collect(Collectors.toList());

// シーケンシャル
long start = System.currentTimeMillis();
long sum1 = list.stream()
    .mapToLong(n -> heavyComputation(n))
    .sum();
long time1 = System.currentTimeMillis() - start;

// パラレル
start = System.currentTimeMillis();
long sum2 = list.parallelStream()
    .mapToLong(n -> heavyComputation(n))
    .sum();
long time2 = System.currentTimeMillis() - start;

System.out.println("Sequential: " + time1 + "ms");
System.out.println("Parallel: " + time2 + "ms");
System.out.println("Speedup: " + (double)time1/time2 + "x");
```

## 9. 試験ポイント・引っかけ問題

### 9.1 combinerの引っかけ

```java
// Q: この結果は？
List<String> list = Arrays.asList("a", "b", "c");
String result = list.parallelStream().reduce(
    "START",
    (s1, s2) -> s1 + s2,
    (s1, s2) -> s1 + s2
);
```

**A: "STARTabSTARTcSTARTd"みたいな変な結果**

各チャンクで"START"が使われるから、結果に複数回"START"が現れる。正しいidentityは`""`（空文字列）。

### 9.2 forEach vs forEachOrderedの引っかけ

```java
// Q: この出力は順序が保証される？
list.parallelStream()
    .filter(n -> n > 5)
    .forEach(System.out::println);
```

**A: 保証されない**

`forEach()`は順序を保証しない。順序が必要なら`forEachOrdered()`を使う。

### 9.3 collect()の順序

```java
// Q: この結果リストは順序が保証される？
List<Integer> result = list.parallelStream()
    .map(n -> n * 2)
    .collect(Collectors.toList());
```

**A: 保証される**

`collect(Collectors.toList())`は元の順序を保持する。

### 9.4 statefulな操作

```java
// Q: これは正しく動く？
List<Integer> result = new ArrayList<>();
list.parallelStream()
    .forEach(n -> result.add(n));
```

**A: 正しく動かない**

`ArrayList`はスレッドセーフじゃないので、データ競合が発生する。`collect()`を使うべき。

### 9.5 identityの条件

```java
// Q: このreduceは正しく動く？（パラレル）
int result = list.parallelStream().reduce(
    1,  // identity
    (a, b) -> a + b,
    (a, b) -> a + b
);
```

**A: 正しく動かない**

identityが1だと、各チャンクで1が足されてしまう。足し算のidentityは0でないといけない。

### 9.6 データ構造の選択

```java
// Q: どちらが並列処理に向いている？
LinkedList<Integer> linked = ...;
ArrayList<Integer> array = ...;
```

**A: ArrayList**

`ArrayList`はランダムアクセスが可能なので分割しやすい。`LinkedList`は順次アクセスが必要なので分割しにくい。

### 9.7 限定操作

```java
// Q: これは並列処理の効果がある？
list.parallelStream()
    .limit(10)
    .forEach(System.out::println);
```

**A: ほとんどない**

`limit()`は順序依存の操作なので、並列処理のメリットが大幅に減る。

## 10. まとめ

### 並列ストリームを使うべき時

- データ量が多い（数千〜数百万要素）
- 処理が重い（CPU負荷が高い）
- ステートレスな操作（`map`, `filter`, `reduce`）
- 分割しやすいデータ構造（`ArrayList`, 配列）
- 順序が重要じゃない

### 並列ストリームを使うべきじゃない時

- データ量が少ない（数百要素以下）
- 処理が軽い（単純な計算）
- ステートフルな操作が多い（`sorted`, `limit`）
- 副作用がある操作
- スレッドセーフじゃないコレクションを使う

### 重要ポイント

1. **combinerは並列処理でのみ使われる**
2. **forEach()は順序を保証しない、forEachOrdered()は保証する**
3. **identityは真の恒等元でないといけない**
4. **副作用のある操作は避ける**
5. **collect()を使えばスレッドセーフ**
6. **小さいデータでは逆効果**
7. **findAny()はfindFirst()より速い（並列時）**

これで試験対策はバッチリだね！
