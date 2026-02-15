# Stream Sort 完全攻略ガイド

Java Gold 試験で頻出の Stream のソート機能について、徹底的に解説するよ。

## 目次
1. [Comparable vs Comparator](#comparable-vs-comparator)
2. [Comparator の基本メソッド](#comparator-の基本メソッド)
3. [thenComparing でチェーン](#thencomparing-でチェーン)
4. [reversed() の使い方](#reversed-の使い方)
5. [null の扱い](#null-の扱い)
6. [試験の引っかけポイント](#試験の引っかけポイント)
7. [実践問題](#実践問題)

---

## Comparable vs Comparator

この違いをしっかり理解しないと試験で確実に落とすよね。

### Comparable インターフェース

- **自然順序（natural ordering）** を定義するインターフェース
- クラス自身に `compareTo()` メソッドを実装する
- 「このクラスはこう並べるのが標準だよ」って決める感じ

```java
public interface Comparable<T> {
    int compareTo(T o);
}
```

#### 実装例
```java
class Person implements Comparable<Person> {
    private String name;
    private int age;

    @Override
    public int compareTo(Person other) {
        // 年齢で比較（昇順）
        return this.age - other.age;
    }
}

// 使用例
List<Person> people = ...;
people.stream()
    .sorted()  // Comparable の compareTo() が使われる
    .collect(Collectors.toList());
```

#### compareTo() の戻り値

- **負の値**: this < other（this が前に来る）
- **0**: this == other（同じ順序）
- **正の値**: this > other（this が後に来る）

```java
// よくある実装パターン
public int compareTo(Person other) {
    return this.age - other.age;  // 昇順
}

// String の比較
public int compareTo(Person other) {
    return this.name.compareTo(other.name);  // 辞書順
}
```

### Comparator インターフェース

- **外部から比較ロジックを指定** するインターフェース
- クラスを変更せずに様々な順序でソートできる
- 「今回はこの順序で並べたいな」って自由に決められる感じ

```java
@FunctionalInterface
public interface Comparator<T> {
    int compare(T o1, T o2);
}
```

#### 実装例
```java
// ラムダ式で
Comparator<Person> byAge = (p1, p2) -> p1.getAge() - p2.getAge();

// メソッド参照で
Comparator<Person> byName = Comparator.comparing(Person::getName);

// 使用例
people.stream()
    .sorted(byAge)  // Comparator を指定
    .collect(Collectors.toList());
```

### どっちを使う？

| ケース | 使うもの |
|--------|---------|
| クラスに「標準的な順序」がある | Comparable |
| 複数の並び順が必要 | Comparator |
| 変更できないクラス（String、Integerなど） | Comparator |
| 状況に応じて順序を変えたい | Comparator |

---

## Comparator の基本メソッド

Stream でソートするときの基本メソッドを押さえておこう。

### sorted()

引数なしだと Comparable の自然順序を使う。

```java
// Comparable を実装している型
List<Integer> numbers = Arrays.asList(5, 2, 8, 1);
numbers.stream()
    .sorted()  // 昇順（1, 2, 5, 8）
    .collect(Collectors.toList());

List<String> words = Arrays.asList("banana", "apple", "cherry");
words.stream()
    .sorted()  // 辞書順
    .collect(Collectors.toList());
```

### sorted(Comparator)

Comparator を指定してソート。

```java
people.stream()
    .sorted(Comparator.comparing(Person::getName))
    .collect(Collectors.toList());
```

### Comparator.comparing()

**一番よく使うメソッド！** プロパティを指定してソート。

```java
// 基本形
Comparator.comparing(Person::getName)

// 複数指定も可能（後述）
Comparator.comparing(Person::getAge)
    .thenComparing(Person::getName)
```

### Comparator.comparingInt/Long/Double()

プリミティブ型専用の最適化版。ボクシングが起きないから速い。

```java
// comparingInt() - int 用
Comparator.comparingInt(Person::getAge)

// comparingLong() - long 用
Comparator.comparingLong(Person::getId)

// comparingDouble() - double 用
Comparator.comparingDouble(Person::getSalary)
```

**試験ポイント**: `comparing()` でも動くけど、プリミティブ型なら `comparingInt()` 等を使う方がベター。

### Comparator.naturalOrder() / reverseOrder()

Comparable を使った自然順序。

```java
// naturalOrder() - 昇順
List<String> words = Arrays.asList("banana", "apple", "cherry");
words.stream()
    .sorted(Comparator.naturalOrder())  // sorted() と同じ
    .collect(Collectors.toList());

// reverseOrder() - 降順
words.stream()
    .sorted(Comparator.reverseOrder())
    .collect(Collectors.toList());
```

---

## thenComparing でチェーン

複数の条件でソートしたいときは `thenComparing()` をチェーンするんだよね。

### 基本的なチェーン

```java
// 年齢でソート → 同じ年齢なら名前でソート
people.stream()
    .sorted(Comparator.comparingInt(Person::getAge)
        .thenComparing(Person::getName))
    .collect(Collectors.toList());
```

### thenComparingInt/Long/Double()

プリミティブ型用のチェーン。

```java
// 年齢でソート → 同じ年齢なら給与でソート
people.stream()
    .sorted(Comparator.comparingInt(Person::getAge)
        .thenComparingDouble(Person::getSalary))
    .collect(Collectors.toList());
```

### 複数チェーン

いくつでもチェーンできる。

```java
people.stream()
    .sorted(Comparator.comparing(Person::getDepartment)  // 部署
        .thenComparingInt(Person::getAge)                // → 年齢
        .thenComparing(Person::getName))                 // → 名前
    .collect(Collectors.toList());
```

### 試験の引っかけ

```java
// ❌ これはコンパイルエラー！
people.stream()
    .sorted(Comparator.comparing(Person::getName))
    .thenComparing(Person::getAge)  // ← Stream には thenComparing ない！
    .collect(Collectors.toList());

// ✅ 正しい書き方
people.stream()
    .sorted(Comparator.comparing(Person::getName)
        .thenComparing(Person::getAge))  // Comparator のメソッドとしてチェーン
    .collect(Collectors.toList());
```

**重要**: `thenComparing()` は **Comparator のメソッド** であって、Stream のメソッドじゃない！

---

## reversed() の使い方

逆順にするメソッド。でもちょっと癖があるから注意が必要だよね。

### 基本的な使い方

```java
// 年齢の降順
people.stream()
    .sorted(Comparator.comparingInt(Person::getAge).reversed())
    .collect(Collectors.toList());
```

### thenComparing と組み合わせるとき

```java
// パターン1: 年齢降順 → 名前昇順
people.stream()
    .sorted(Comparator.comparingInt(Person::getAge).reversed()
        .thenComparing(Person::getName))
    .collect(Collectors.toList());

// パターン2: 年齢昇順 → 名前降順
people.stream()
    .sorted(Comparator.comparingInt(Person::getAge)
        .thenComparing(Person::getName, Comparator.reverseOrder()))
    .collect(Collectors.toList());

// パターン3: 全体を逆順に（年齢降順 → 名前降順）
people.stream()
    .sorted(Comparator.comparingInt(Person::getAge)
        .thenComparing(Person::getName)
        .reversed())  // 最後に全体を反転
    .collect(Collectors.toList());
```

### 試験の引っかけ

```java
// ❌ これはコンパイルエラー！
people.stream()
    .sorted(Comparator.comparingInt(Person::getAge))
    .reversed()  // ← Stream には reversed() ない！
    .collect(Collectors.toList());

// ❌ これもダメ！
Comparator.reverseOrder(Person::getAge)  // ← reverseOrder() は引数取らない

// ✅ 正しい書き方
people.stream()
    .sorted(Comparator.comparingInt(Person::getAge).reversed())
    .collect(Collectors.toList());
```

---

## null の扱い

null が混じってるときの処理。これも試験に出るよね。

### nullsFirst() / nullsLast()

null を最初または最後に持ってくる。

```java
// nullsFirst - null を最初に
people.stream()
    .sorted(Comparator.comparing(Person::getName,
        Comparator.nullsFirst(Comparator.naturalOrder())))
    .collect(Collectors.toList());

// nullsLast - null を最後に
people.stream()
    .sorted(Comparator.comparing(Person::getName,
        Comparator.nullsLast(Comparator.naturalOrder())))
    .collect(Collectors.toList());
```

### オブジェクト自体が null のとき

```java
List<Person> people = Arrays.asList(
    new Person("田中", 25),
    null,  // ← オブジェクト自体が null
    new Person("佐藤", 30)
);

// nullsFirst でオブジェクト全体を処理
people.stream()
    .sorted(Comparator.nullsFirst(
        Comparator.comparing(Person::getName)))
    .collect(Collectors.toList());
```

### 試験の引っかけ

```java
// ❌ これは NullPointerException！
people.stream()
    .sorted(Comparator.comparing(Person::getName))  // name が null だと例外
    .collect(Collectors.toList());

// ✅ null を考慮
people.stream()
    .sorted(Comparator.comparing(Person::getName,
        Comparator.nullsLast(Comparator.naturalOrder())))
    .collect(Collectors.toList());

// ✅ または filter で除外
people.stream()
    .filter(p -> p.getName() != null)
    .sorted(Comparator.comparing(Person::getName))
    .collect(Collectors.toList());
```

---

## 試験の引っかけポイント

試験でよく出る引っかけパターンをまとめておくね。

### 1. メソッドの所属を間違える

```java
// ❌ Stream に thenComparing() はない
stream.sorted(...).thenComparing(...)

// ❌ Stream に reversed() はない
stream.sorted(...).reversed()

// ✅ Comparator のメソッドとして使う
stream.sorted(Comparator.comparing(...).thenComparing(...).reversed())
```

### 2. comparing() と comparingInt() の違い

```java
// どっちも動くけど...
Comparator.comparing(Person::getAge)       // Integer（ボクシング発生）
Comparator.comparingInt(Person::getAge)    // int（プリミティブ）

// 試験では comparingInt() の方が「適切」とされることが多い
```

### 3. reversed() の位置

```java
// パターン1: 第1条件だけ逆順
Comparator.comparing(Person::getAge).reversed()
    .thenComparing(Person::getName)

// パターン2: 全体を逆順
Comparator.comparing(Person::getAge)
    .thenComparing(Person::getName)
    .reversed()

// 結果が全然違う！
```

### 4. naturalOrder() vs reverseOrder()

```java
// naturalOrder() - 自然順序（Comparable を使う）
Comparator.naturalOrder()

// reverseOrder() - 自然順序の逆
Comparator.reverseOrder()

// ❌ 引数は取らない！
Comparator.reverseOrder(Person::getAge)  // コンパイルエラー
```

### 5. null 処理

```java
// ❌ null があると NullPointerException
stream.sorted(Comparator.comparing(Person::getName))

// ✅ nullsFirst/Last を使う
stream.sorted(Comparator.comparing(Person::getName,
    Comparator.nullsLast(Comparator.naturalOrder())))
```

### 6. Collections.sort() vs List.sort()

```java
// 両方とも元のリストを変更する（破壊的メソッド）
Collections.sort(list);  // 古い書き方
list.sort(null);         // Java 8 以降の推奨

// Stream の sorted() は非破壊的（新しいストリームを返す）
list.stream().sorted().collect(Collectors.toList());
```

### 7. compare() と compareTo()

```java
// Comparator.compare(T o1, T o2) - 2つのオブジェクトを比較
Comparator<Person> comp = (p1, p2) -> p1.getAge() - p2.getAge();

// Comparable.compareTo(T o) - 自分自身と他を比較
class Person implements Comparable<Person> {
    public int compareTo(Person other) {
        return this.age - other.age;
    }
}
```

---

## 実践問題

試験レベルの問題で理解度チェック！

### 問題1: 基本的なソート

次のコードの出力は？

```java
List<Integer> numbers = Arrays.asList(5, 2, 8, 1, 9);
numbers.stream()
    .sorted()
    .limit(3)
    .forEach(System.out::print);
```

<details>
<summary>答え</summary>

**出力**: `128`

- `sorted()` で昇順に並べる: [1, 2, 5, 8, 9]
- `limit(3)` で最初の3つ: [1, 2, 8]
- `forEach` で出力: 128

</details>

### 問題2: thenComparing

次のコードの Person リストの並び順は？

```java
class Person {
    String name;
    int age;
    // コンストラクタ、getter は省略
}

List<Person> people = Arrays.asList(
    new Person("田中", 25),
    new Person("佐藤", 30),
    new Person("鈴木", 25)
);

people.stream()
    .sorted(Comparator.comparingInt(Person::getAge)
        .thenComparing(Person::getName))
    .forEach(System.out::println);
```

<details>
<summary>答え</summary>

**出力順**:
1. Person{name='鈴木', age=25}
2. Person{name='田中', age=25}
3. Person{name='佐藤', age=30}

- 年齢で昇順ソート（25, 25, 30）
- 同じ年齢（25）の中では名前で昇順（鈴木、田中）

</details>

### 問題3: reversed()

次のコードの出力順は？

```java
people.stream()
    .sorted(Comparator.comparingInt(Person::getAge)
        .thenComparing(Person::getName)
        .reversed())
    .forEach(System.out::println);
```

<details>
<summary>答え</summary>

**出力順**:
1. Person{name='佐藤', age=30}
2. Person{name='田中', age=25}
3. Person{name='鈴木', age=25}

- 年齢昇順→名前昇順でソートした後、**全体を逆順に**
- つまり、年齢降順→名前降順と同じ結果

</details>

### 問題4: コンパイルエラー

次のコードでコンパイルエラーになるのはどれ？

```java
// A
people.stream()
    .sorted(Comparator.comparing(Person::getName))
    .thenComparing(Person::getAge)
    .collect(Collectors.toList());

// B
people.stream()
    .sorted(Comparator.comparing(Person::getName)
        .thenComparing(Person::getAge))
    .collect(Collectors.toList());

// C
people.stream()
    .sorted(Comparator.comparing(Person::getName))
    .reversed()
    .collect(Collectors.toList());

// D
people.stream()
    .sorted(Comparator.reverseOrder(Person::getName))
    .collect(Collectors.toList());
```

<details>
<summary>答え</summary>

**A, C, D がコンパイルエラー**

- **A**: Stream に `thenComparing()` メソッドはない
- **C**: Stream に `reversed()` メソッドはない
- **D**: `reverseOrder()` は引数を取らない

**B だけが正しい**

</details>

### 問題5: null 処理

次のコードで NullPointerException が発生するのはどれ？

```java
List<Person> people = Arrays.asList(
    new Person("田中", 25),
    new Person(null, 30),  // 名前が null
    new Person("鈴木", 25)
);

// A
people.stream()
    .sorted(Comparator.comparing(Person::getName))
    .collect(Collectors.toList());

// B
people.stream()
    .sorted(Comparator.comparing(Person::getName,
        Comparator.nullsLast(Comparator.naturalOrder())))
    .collect(Collectors.toList());

// C
people.stream()
    .filter(p -> p.getName() != null)
    .sorted(Comparator.comparing(Person::getName))
    .collect(Collectors.toList());
```

<details>
<summary>答え</summary>

**A だけが NullPointerException**

- **A**: `getName()` が null を返すと、比較時に例外
- **B**: `nullsLast()` で null を安全に処理
- **C**: `filter()` で null を除外しているので安全

</details>

---

## まとめ

### 絶対覚えるべきポイント

1. **Comparable vs Comparator**
   - Comparable: クラス自身が実装、自然順序
   - Comparator: 外部から指定、柔軟

2. **Comparator の主要メソッド**
   - `comparing()`: 汎用
   - `comparingInt/Long/Double()`: プリミティブ用（推奨）
   - `thenComparing()`: 複数条件
   - `reversed()`: 逆順

3. **null 処理**
   - `nullsFirst()`: null を最初
   - `nullsLast()`: null を最後

4. **よくある間違い**
   - Stream に `thenComparing()` はない → Comparator のメソッド
   - Stream に `reversed()` はない → Comparator のメソッド
   - `reverseOrder()` は引数を取らない

5. **メソッドの所属**
   - **Stream のメソッド**: `sorted()`
   - **Comparator のメソッド**: `comparing()`, `thenComparing()`, `reversed()`

### 試験対策チェックリスト

- [ ] Comparable と Comparator の違いを説明できる
- [ ] `comparing()` と `comparingInt()` の違いを理解している
- [ ] `thenComparing()` のチェーンを書ける
- [ ] `reversed()` の位置による違いを理解している
- [ ] `nullsFirst()` / `nullsLast()` を使える
- [ ] どのメソッドがどのクラスに属するか分かる
- [ ] `Collections.sort()` と `List.sort()` の違いを知っている
- [ ] null 処理をしないと例外が出ることを理解している

これで Stream のソートは完璧だね！試験頑張って！
