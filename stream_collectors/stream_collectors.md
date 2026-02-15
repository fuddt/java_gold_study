# Stream Collectors 完全ガイド

## Collectorsの全体像

Collectorsクラスは、Streamの終端操作`collect()`で使うコレクターを提供する工場クラスだよね。ストリームの要素を集めて、リスト、マップ、文字列、統計情報なんかに変換できるわけ。

### 主要なCollectorsメソッド

```
1. コレクション系
   - toList()              : ArrayList (変更可能)
   - toSet()               : HashSet (重複排除)
   - toUnmodifiableList()  : 変更不可リスト (Java 10+)
   - toCollection(Supplier): 任意のコレクション

2. マップ系
   - toMap()               : キーと値のマップ作成
   - groupingBy()          : グループ化してMap<K, List<V>>
   - partitioningBy()      : 二分割してMap<Boolean, List<V>>

3. 文字列系
   - joining()             : 文字列結合

4. 統計系
   - counting()            : 要素数カウント
   - summingInt()          : 合計
   - averagingInt()        : 平均
   - summarizingInt()      : 統計情報まとめて取得

5. downstream系
   - mapping()             : 他のコレクターと組み合わせる
```

---

## groupingBy vs partitioningBy の違い

これ試験でめっちゃ出るからしっかり押さえとこうぜ！

### groupingBy()

- **分類関数**: 任意の型のキーを返す関数
- **戻り値**: `Map<K, List<V>>` (キーは分類関数の戻り値の型)
- **キーの数**: 0個以上 (データ次第)

```java
// 都市別にグループ化 → Map<String, List<Person>>
Map<String, List<Person>> byCity = people.stream()
    .collect(Collectors.groupingBy(Person::getCity));
// 結果: {"東京"=[...], "大阪"=[...], "福岡"=[...]}
```

### partitioningBy()

- **分類関数**: Predicate (boolean を返す)
- **戻り値**: `Map<Boolean, List<V>>` (キーは必ずBooleanのtrueとfalse)
- **キーの数**: 常に2個 (true と false)

```java
// 30歳以上/未満で分割 → Map<Boolean, List<Person>>
Map<Boolean, List<Person>> partitioned = people.stream()
    .collect(Collectors.partitioningBy(p -> p.getAge() >= 30));
// 結果: {false=[...], true=[...]}
```

### 重要な違い

**1. partitioningByは必ず2つのキー**

```java
// 誰もマッチしなくてもfalseとtrueの両方のキーが存在
Map<Boolean, List<Person>> result = people.stream()
    .filter(p -> p.getAge() > 100)  // 空のストリーム
    .collect(Collectors.partitioningBy(p -> p.getAge() >= 30));

// 結果: {false=[], true=[]}  ← 両方空リストだけど存在する
```

**2. groupingByは実際に存在するキーのみ**

```java
Map<String, List<Person>> byCity = people.stream()
    .filter(p -> p.getAge() > 100)  // 空のストリーム
    .collect(Collectors.groupingBy(Person::getCity));

// 結果: {}  ← 空のマップ
```

### どっちを使うべき？

- **2つに分けたい** → `partitioningBy()` (性別、成人/未成年、合格/不合格など)
- **複数グループに分けたい** → `groupingBy()` (都道府県別、年齢別、部署別など)

---

## toMap のキー重複時の挙動

これは絶対覚えておかないとヤバい！試験で頻出だし、実務でもハマるポイント。

### 基本形: キーが一意の場合

```java
Map<String, Integer> nameToAge = people.stream()
    .collect(Collectors.toMap(
        Person::getName,  // キー
        Person::getAge    // 値
    ));
```

これは問題なし。ただし...

### キー重複時 → 例外が投げられる！

```java
// 年齢をキーにすると重複が発生
Map<Integer, String> ageToName = people.stream()
    .collect(Collectors.toMap(
        Person::getAge,   // 25歳が3人いる！
        Person::getName
    ));

// → IllegalStateException: Duplicate key
```

**超重要**: merge関数を指定しないと、キー重複時に`IllegalStateException`が投げられるんだよね。これ知らないとバグる。

### merge関数で重複を解決

```java
// パターン1: 値を結合する
Map<Integer, String> merged = people.stream()
    .collect(Collectors.toMap(
        Person::getAge,
        Person::getName,
        (existing, replacement) -> existing + ", " + replacement  // merge関数
    ));
// 結果: {25="太郎, 次郎, さくら", 30="花子, 健太", 35="美咲"}

// パターン2: 古い値を保持
(oldVal, newVal) -> oldVal

// パターン3: 新しい値で上書き
(oldVal, newVal) -> newVal
```

### toMapの引数パターン

```java
// 2引数版: キー重複時は例外
toMap(keyMapper, valueMapper)

// 3引数版: merge関数で重複解決
toMap(keyMapper, valueMapper, mergeFunction)

// 4引数版: Mapの実装を指定
toMap(keyMapper, valueMapper, mergeFunction, mapSupplier)
```

---

## downstreamコレクターの概念

downstream（下流）コレクターは、`groupingBy()`や`partitioningBy()`の**第2引数**として使われるコレクターのこと。グループ化した後の各グループに対して、さらに処理を加えるイメージだね。

### 基本的な使い方

```java
// downstreamなし: Map<String, List<Person>>
Map<String, List<Person>> byCity = people.stream()
    .collect(Collectors.groupingBy(Person::getCity));

// downstreamあり: Map<String, Long>
Map<String, Long> cityCount = people.stream()
    .collect(Collectors.groupingBy(
        Person::getCity,      // 分類関数
        Collectors.counting() // downstream: 各グループの要素数
    ));
```

### よく使うdownstreamコレクター

**1. counting() - 要素数カウント**

```java
Map<String, Long> count = people.stream()
    .collect(Collectors.groupingBy(
        Person::getCity,
        Collectors.counting()
    ));
// 結果: {"東京"=3, "大阪"=2, "福岡"=1}
```

**2. summingInt() - 合計**

```java
Map<String, Integer> totalSalary = people.stream()
    .collect(Collectors.groupingBy(
        Person::getCity,
        Collectors.summingInt(Person::getSalary)
    ));
// 結果: {"東京"=19000, "大阪"=12500, "福岡"=4500}
```

**3. mapping() - さらに変換**

```java
Map<String, List<String>> cityToNames = people.stream()
    .collect(Collectors.groupingBy(
        Person::getCity,
        Collectors.mapping(
            Person::getName,
            Collectors.toList()
        )
    ));
// 結果: {"東京"=["太郎", "次郎", "美咲"], ...}
```

**4. collectingAndThen() - 後処理**

```java
Map<String, Long> cityCount = people.stream()
    .collect(Collectors.groupingBy(
        Person::getCity,
        Collectors.collectingAndThen(
            Collectors.counting(),
            count -> count * 2  // カウント結果を2倍にする
        )
    ));
```

### 多階層のdownstream

downstreamの中にさらにdownstreamを入れることもできるぜ。

```java
// 都市別 → 年齢別 → 名前リスト
Map<String, Map<Integer, List<String>>> multiLevel = people.stream()
    .collect(Collectors.groupingBy(
        Person::getCity,                        // 第1階層
        Collectors.groupingBy(                  // 第2階層 downstream
            Person::getAge,
            Collectors.mapping(                 // 第3階層 downstream
                Person::getName,
                Collectors.toList()
            )
        )
    ));
```

---

## 試験頻出パターン

### パターン1: 戻り値の型を見極める

```java
// 問: 以下の戻り値の型は？
people.stream().collect(Collectors.groupingBy(Person::getCity))
// → Map<String, List<Person>>

people.stream().collect(Collectors.partitioningBy(p -> p.getAge() >= 30))
// → Map<Boolean, List<Person>>

people.stream().collect(Collectors.groupingBy(
    Person::getCity,
    Collectors.counting()
))
// → Map<String, Long>  (downstreamでLongに変換)
```

### パターン2: toMapのキー重複

```java
// 問: 以下のコードは例外を投げるか？
people.stream().collect(Collectors.toMap(
    Person::getAge,  // 重複あり
    Person::getName
))
// → Yes! IllegalStateException (merge関数がないため)

// 修正版
people.stream().collect(Collectors.toMap(
    Person::getAge,
    Person::getName,
    (old, new) -> old  // merge関数追加
))
// → OK
```

### パターン3: joiningの引数

```java
// 問: 空のストリームに対してjoining()を実行すると？
Stream.empty().collect(Collectors.joining(", ", "[", "]"))
// → "[]"  (空でも接頭辞・接尾辞は付く)

Stream.empty().collect(Collectors.joining(", "))
// → ""  (空文字列)
```

### パターン4: partitioningByのキー

```java
// 問: 誰もマッチしない条件でpartitioningByしたら？
people.stream()
    .filter(p -> p.getAge() > 100)
    .collect(Collectors.partitioningBy(p -> p.getAge() >= 30))
// → {false=[], true=[]}  (両方のキーが存在)

// groupingByの場合は？
people.stream()
    .filter(p -> p.getAge() > 100)
    .collect(Collectors.groupingBy(Person::getCity))
// → {}  (空のマップ)
```

---

## 引っかけ問題

### 引っかけ1: toListの戻り値

```java
// 問: 以下の戻り値は変更可能か？
List<String> list = people.stream()
    .map(Person::getName)
    .collect(Collectors.toList());

list.add("追加");  // OK？
// → OK! toList()は変更可能なリストを返す

// 変更不可にしたい場合
List<String> immutable = people.stream()
    .map(Person::getName)
    .collect(Collectors.toUnmodifiableList());  // Java 10+

immutable.add("追加");  // UnsupportedOperationException
```

### 引っかけ2: groupingByのデフォルトMap

```java
// 問: groupingByの戻り値のMapの実装クラスは？
Map<String, List<Person>> map = people.stream()
    .collect(Collectors.groupingBy(Person::getCity));

// → HashMap (順序保証なし)

// LinkedHashMapにしたい場合 (挿入順を保持)
Map<String, List<Person>> ordered = people.stream()
    .collect(Collectors.groupingBy(
        Person::getCity,
        LinkedHashMap::new,  // Map Supplier
        Collectors.toList()
    ));
```

### 引っかけ3: summingIntの戻り値

```java
// 問: 以下の戻り値の型は？
int sum = people.stream()
    .collect(Collectors.summingInt(Person::getSalary));

// → コンパイルエラー！
// summingInt()の戻り値は Integer (ボクシング型)

Integer sum = people.stream()
    .collect(Collectors.summingInt(Person::getSalary));
// → OK

// またはmapToInt().sum()を使う
int sum = people.stream()
    .mapToInt(Person::getSalary)
    .sum();
// → これならint (プリミティブ)
```

### 引っかけ4: mapping()は単独で使えない

```java
// 問: 以下のコードは正しいか？
List<String> names = people.stream()
    .collect(Collectors.mapping(
        Person::getName,
        Collectors.toList()
    ));

// → OK! でもわざわざmapping()を使う意味はない
// map()を使った方が自然

List<String> names = people.stream()
    .map(Person::getName)
    .collect(Collectors.toList());

// mapping()はdownstreamとして使うのが正しい使い方
Map<String, List<String>> cityToNames = people.stream()
    .collect(Collectors.groupingBy(
        Person::getCity,
        Collectors.mapping(Person::getName, Collectors.toList())
    ));
```

---

## よくある間違い

### 間違い1: collectとcountを混同

```java
// ❌ 間違い
long count = people.stream()
    .filter(p -> p.getAge() >= 30)
    .collect(Collectors.count());  // count()メソッドは存在しない

// ✅ 正しい (パターン1: Streamのcount())
long count = people.stream()
    .filter(p -> p.getAge() >= 30)
    .count();

// ✅ 正しい (パターン2: Collectors.counting())
long count = people.stream()
    .filter(p -> p.getAge() >= 30)
    .collect(Collectors.counting());
```

### 間違い2: groupingByの引数順序

```java
// ❌ 間違い
Map<String, Long> map = people.stream()
    .collect(Collectors.groupingBy(
        Collectors.counting(),  // downstreamが先はNG
        Person::getCity
    ));

// ✅ 正しい (分類関数 → downstream)
Map<String, Long> map = people.stream()
    .collect(Collectors.groupingBy(
        Person::getCity,
        Collectors.counting()
    ));
```

### 間違い3: toMapでのキー重複無視

```java
// ❌ 間違い (キー重複を考慮していない)
Map<Integer, String> map = people.stream()
    .collect(Collectors.toMap(
        Person::getAge,
        Person::getName
    ));
// → IllegalStateException

// ✅ 正しい
Map<Integer, String> map = people.stream()
    .collect(Collectors.toMap(
        Person::getAge,
        Person::getName,
        (old, newVal) -> old  // merge関数を追加
    ));
```

---

## まとめ

Stream APIのCollectorsは、データの集約・変換の基本だよね。特に以下のポイントは試験でも実務でも超重要じゃね？

1. **toMapはキー重複時に例外** → merge関数必須
2. **groupingByは0個以上のキー、partitioningByは必ず2個** → 使い分けが大事
3. **downstreamコレクターで柔軟な集約** → 多階層も可能
4. **戻り値の型を正確に把握** → Map<K, List<V>> vs Map<K, Long> など

コード例を何度も動かして、手に馴染ませるのが一番だぜ。頑張ってこうぜ！
