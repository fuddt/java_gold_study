# Stream 検索・マッチング操作

## 概要

Streamの検索・マッチング操作は、**終端操作**である。これらの操作は短絡評価（short-circuit）を行うから、効率的に処理できるんだ。

## 主要メソッド一覧

### find系メソッド（Optional返却）

| メソッド | 戻り値 | 説明 |
|---------|--------|------|
| `findFirst()` | `Optional<T>` | ストリームの最初の要素を返す |
| `findAny()` | `Optional<T>` | ストリームの任意の要素を返す |

### match系メソッド（boolean返却）

| メソッド | 戻り値 | 説明 |
|---------|--------|------|
| `anyMatch(Predicate<T>)` | `boolean` | 1つでも条件を満たせば true |
| `allMatch(Predicate<T>)` | `boolean` | 全て条件を満たせば true |
| `noneMatch(Predicate<T>)` | `boolean` | 1つも条件を満たさなければ true |

---

## 1. findFirst() vs findAny()

### findFirst()

```java
List<String> names = Arrays.asList("太郎", "花子", "次郎");
Optional<String> first = names.stream()
    .filter(name -> name.endsWith("郎"))
    .findFirst();
// 結果: Optional[太郎] (必ず最初の要素)
```

- **常に**ストリームの最初の要素を返す
- 順序が重要な場合に使う
- 順次・並列ストリームどちらでも結果は同じ

### findAny()

```java
// 順次ストリーム
Optional<String> any1 = names.stream()
    .filter(name -> name.endsWith("郎"))
    .findAny();
// 結果: 通常は最初の要素（保証はない）

// 並列ストリーム
Optional<String> any2 = names.parallelStream()
    .filter(name -> name.endsWith("郎"))
    .findAny();
// 結果: 不定（最初に見つかった要素）
```

- **任意の**要素を返す（どれかは不定）
- 並列ストリームで効率的
- 順序が不要な場合に使う

### 試験ポイント

**Q: findAny()は必ず最初の要素を返す？**
→ **NO!** 順次ストリームでは最初になりがちだが、保証はない。特に並列ストリームでは完全に不定である。

**Q: findFirst()とfindAny()、どちらが速い？**
→ 順次ストリームなら同じくらい。並列ストリームでは**findAny()の方が速い**（最初に見つけたスレッドの結果を使える）。

---

## 2. match系メソッド

### anyMatch() - 1つでもマッチすれば true

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);

boolean hasEven = numbers.stream()
    .anyMatch(n -> n % 2 == 0);
// 結果: true (2, 4がマッチ)

boolean hasLarge = numbers.stream()
    .anyMatch(n -> n > 100);
// 結果: false (1つもマッチしない)
```

- 最初にマッチした要素が見つかったら**即終了**（短絡評価）
- 空ストリームでは **false**

### allMatch() - 全てマッチすれば true

```java
List<Integer> numbers = Arrays.asList(2, 4, 6, 8);

boolean allEven = numbers.stream()
    .allMatch(n -> n % 2 == 0);
// 結果: true (全て偶数)

boolean allLarge = numbers.stream()
    .allMatch(n -> n > 5);
// 結果: false (2, 4がマッチしない)
```

- 最初にマッチしない要素が見つかったら**即終了**（短絡評価）
- 空ストリームでは **true**（超重要！）

### noneMatch() - 1つもマッチしなければ true

```java
List<Integer> numbers = Arrays.asList(1, 3, 5, 7);

boolean noEven = numbers.stream()
    .noneMatch(n -> n % 2 == 0);
// 結果: true (偶数が1つもない)

boolean noLarge = numbers.stream()
    .noneMatch(n -> n > 5);
// 結果: false (7がマッチ)
```

- 最初にマッチした要素が見つかったら**即終了**（短絡評価）
- 空ストリームでは **true**

### 試験ポイント

**空ストリームでのmatch系メソッドの挙動（超重要！）**

```java
Stream<Integer> empty = Stream.empty();

empty.allMatch(n -> n > 0);   // true  ← 引っかけ注意！
empty.anyMatch(n -> n > 0);   // false
empty.noneMatch(n -> n > 0);  // true
```

**なぜ allMatch が true なのか？**
- 論理学的に「全ての要素が条件を満たす」= 「反例が存在しない」
- 空ストリームには反例がない → true
- これは**vacuous truth（空虚な真）**と呼ばれる概念である

---

## 3. 短絡評価（Short-Circuit）

### 短絡評価とは？

結果が確定した時点で、残りの要素の処理をスキップする仕組みである。

### anyMatch の短絡評価

```java
Stream.of(1, 2, 3, 4, 5)
    .peek(n -> System.out.println("処理中: " + n))
    .anyMatch(n -> n > 2);

// 出力:
// 処理中: 1
// 処理中: 2
// 処理中: 3  ← ここで true 確定、4と5は処理されない
```

### allMatch の短絡評価

```java
Stream.of(2, 4, 6, 7, 8)
    .peek(n -> System.out.println("処理中: " + n))
    .allMatch(n -> n % 2 == 0);

// 出力:
// 処理中: 2
// 処理中: 4
// 処理中: 6
// 処理中: 7  ← ここで false 確定、8は処理されない
```

### noneMatch の短絡評価

```java
Stream.of(1, 3, 5, 6, 7)
    .peek(n -> System.out.println("処理中: " + n))
    .noneMatch(n -> n % 2 == 0);

// 出力:
// 処理中: 1
// 処理中: 3
// 処理中: 5
// 処理中: 6  ← ここで false 確定、7は処理されない
```

### findFirst の短絡評価

```java
Stream.of(10, 20, 30, 40)
    .peek(n -> System.out.println("処理中: " + n))
    .filter(n -> n > 15)
    .findFirst();

// 出力:
// 処理中: 10
// 処理中: 20  ← ここで要素が見つかり終了、30と40は処理されない
```

### 試験ポイント

**Q: 以下のコードで、peek は何回実行される？**

```java
Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    .peek(n -> System.out.println(n))
    .anyMatch(n -> n > 5);
```

**A: 6回**（1, 2, 3, 4, 5, 6 が処理され、6で条件を満たして終了）

---

## 4. 並列ストリームでの動作

### findAny() の並列処理

```java
List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

// 並列ストリーム
list.parallelStream()
    .filter(n -> n > 5)
    .findAny();
// 結果: 6, 7, 8, 9, 10 のいずれか（実行ごとに異なる可能性）
```

- 複数のスレッドが並列で処理
- **最初に見つけたスレッドの結果**を返す
- どのスレッドが最初かは実行環境に依存

### findFirst() の並列処理

```java
list.parallelStream()
    .filter(n -> n > 5)
    .findFirst();
// 結果: 必ず 6（最初の要素）
```

- 並列処理でも**順序を保証**するため、オーバーヘッドがある
- 順序が不要なら findAny() の方が効率的

### 試験ポイント

**Q: 並列ストリームで findAny() を使うメリットは？**
→ **パフォーマンス向上**。順序を気にせず、最初に見つかった要素を返せるから、findFirst()より速いんである。

**Q: 並列ストリームで match系メソッドも短絡評価される？**
→ **YES!** どのスレッドかが結果を確定させた時点で、他のスレッドも処理を中断する。

---

## 5. よくある引っかけ問題

### 引っかけ1: 空ストリームでの allMatch

```java
Stream.empty().allMatch(n -> false);  // true!
```

条件が `false` でも、空ストリームなら `allMatch` は `true` である。

### 引っかけ2: findAny の結果

```java
// これは必ず "A" を返す？
Stream.of("A", "B", "C")
    .findAny()
    .get();
```

**NO!** 順次ストリームでは "A" になりがちだが、**保証はない**。並列ストリームなら確実に不定である。

### 引っかけ3: match系とfind系の違い

```java
// これは同じ？
stream.anyMatch(n -> n > 5);
stream.filter(n -> n > 5).findAny().isPresent();
```

**ほぼ同じ**だが、戻り値の型が違う（`boolean` vs `Optional`）。試験では戻り値の型を問われることがあるから注意である。

### 引っかけ4: 短絡評価の回数

```java
Stream.of(1, 2, 3, 4, 5)
    .peek(System.out::println)
    .allMatch(n -> n < 3);
```

何回出力される？ → **3回**（1, 2, 3 が処理され、3で false 確定）

### 引っかけ5: noneMatch と allMatch の関係

```java
// これは同じ？
stream.noneMatch(p);
!stream.anyMatch(p);
```

**論理的には同じ**だが、ストリームは1度しか使えないから、実際には同じコードにはできないよ。

---

## 6. 試験対策まとめ

### 絶対覚えるポイント

1. **空ストリームで allMatch は true**
   - `Stream.empty().allMatch(任意の条件)` → `true`

2. **findFirst と findAny の違い**
   - `findFirst`: 常に最初の要素
   - `findAny`: 並列ストリームで不定

3. **短絡評価の動作**
   - `anyMatch`: 1つマッチしたら終了
   - `allMatch`: 1つマッチしなかったら終了
   - `noneMatch`: 1つマッチしたら終了
   - `findFirst/findAny`: 要素が見つかったら終了

4. **match系メソッドの戻り値は boolean**
   - `anyMatch/allMatch/noneMatch` → `boolean`
   - `findFirst/findAny` → `Optional<T>`

5. **これらは全て終端操作**
   - 実行後、そのストリームは使用不可

### よく出る問題パターン

1. 空ストリームでの動作（特に allMatch）
2. 並列ストリームでの findAny の挙動
3. 短絡評価で何回処理されるか
4. findFirst と findAny の使い分け
5. match系メソッドの論理関係

---

## コード例

```java
// 基本的な使い方
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);

// find系
Optional<Integer> first = numbers.stream().findFirst();  // Optional[1]
Optional<Integer> any = numbers.stream().findAny();      // Optional[1]（通常）

// match系
boolean hasEven = numbers.stream().anyMatch(n -> n % 2 == 0);   // true
boolean allPositive = numbers.stream().allMatch(n -> n > 0);    // true
boolean noNegative = numbers.stream().noneMatch(n -> n < 0);    // true

// 空ストリーム
boolean empty1 = Stream.<Integer>empty().allMatch(n -> n > 0);   // true!
boolean empty2 = Stream.<Integer>empty().anyMatch(n -> n > 0);   // false
boolean empty3 = Stream.<Integer>empty().noneMatch(n -> n > 0);  // true

// 短絡評価
long count = Stream.of(1, 2, 3, 4, 5)
    .peek(System.out::println)  // 1, 2, 3 だけ出力される
    .anyMatch(n -> n > 2);      // 3でtrue確定

// 並列ストリームでのfindAny
IntStream.range(0, 100)
    .parallel()
    .filter(n -> n > 50)
    .findAny();  // 51〜99のいずれか（不定）
```

---

これで Stream の検索・マッチング操作はバッチリだね！特に**空ストリームでの allMatch が true**っていうのは、試験で絶対狙われるから要注意である。
