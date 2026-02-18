# メソッド参照（Method Reference）完全ガイド

## メソッド参照とは

メソッド参照っていうのは、**ラムダ式をさらに省略した書き方**である。

ラムダ式が「引数を受け取って、メソッドを呼ぶだけ」の単純な処理の場合、メソッド参照を使ってもっとシンプルに書けるんだ。

```java
// 普通のラムダ式
Function<String, Integer> lambda = s -> Integer.parseInt(s);

// メソッド参照で省略
Function<String, Integer> methodRef = Integer::parseInt;
```

`::`（ダブルコロン）を使うのが特徴ではないだろうか

---

## 4種類のメソッド参照

メソッド参照には**4つのパターン**があるんである。これが試験で超重要！

### 1. 静的メソッド参照：`ClassName::staticMethod`

**静的メソッドを参照する**パターン。一番シンプルだね。

```java
// ラムダ式
Function<String, Integer> lambda = s -> Integer.parseInt(s);

// メソッド参照
Function<String, Integer> methodRef = Integer::parseInt;
```

**対応関係：**
- ラムダの引数 `s` → メソッドの引数になる
- `Integer.parseInt(s)` → `Integer::parseInt`

**よく使う例：**
```java
List<String> numbers = Arrays.asList("1", "2", "3");
numbers.stream()
       .map(Integer::parseInt)  // 各文字列をintに変換
       .forEach(System.out::println);

// Math.absも静的メソッド
IntUnaryOperator abs = Math::abs;
```

**ポイント：**
- クラス名の後に`::`を付ける
- 引数と戻り値の型が合っていればOK

---

### 2. 特定のオブジェクトのインスタンスメソッド参照：`instance::method`

**既に存在する特定のオブジェクト**のメソッドを参照するパターン。

```java
// ラムダ式
Consumer<String> lambda = s -> System.out.println(s);

// メソッド参照
Consumer<String> methodRef = System.out::println;
```

**対応関係：**
- `System.out` → 特定のオブジェクト（インスタンス）
- ラムダの引数 `s` → `println`の引数になる

**よく使う例：**
```java
List<String> words = Arrays.asList("Apple", "Banana", "Cherry");

// これが超頻出！
words.forEach(System.out::println);

// カスタムオブジェクトでも使える
Printer printer = new Printer();
words.forEach(printer::print);  // printerのprintメソッドを呼ぶ
```

**ポイント：**
- 既に作成済みのオブジェクト（インスタンス）を使う
- `this::method`も可能（自分自身のメソッド参照）

---

### 3. 任意のオブジェクトのインスタンスメソッド参照：`ClassName::instanceMethod`

**これが一番紛らわしい！** 試験で狙われるパターンである。

```java
// ラムダ式
Function<String, String> lambda = s -> s.toLowerCase();

// メソッド参照
Function<String, String> methodRef = String::toLowerCase;
```

**対応関係：**
- ラムダの**第一引数がレシーバー**（メソッドを呼ばれる対象）になる
- `s.toLowerCase()` → `String::toLowerCase`

**なぜ紛らわしいのか？**

- パターン1（静的メソッド参照）と書き方が似てる
- でも、**静的メソッドじゃなくてインスタンスメソッド**を参照してる

**見分け方：**
```java
// 静的メソッド参照（パターン1）
Function<String, Integer> f1 = Integer::parseInt;  // parseIntは静的メソッド

// 任意オブジェクトのインスタンスメソッド参照（パターン3）
Function<String, String> f2 = String::toLowerCase; // toLowerCaseはインスタンスメソッド
```

**引数が2つの場合：**
```java
// ラムダ式：第一引数がレシーバー、第二引数がメソッドの引数
BiFunction<String, String, Boolean> lambda = (str, prefix) -> str.startsWith(prefix);

// メソッド参照：同じ意味！
BiFunction<String, String, Boolean> methodRef = String::startsWith;
```

**よく使う例：**
```java
List<String> words = Arrays.asList("APPLE", "BANANA", "CHERRY");

// 各要素のtoLowerCase()を呼ぶ
words.stream()
     .map(String::toLowerCase)  // 各Stringのメソッドを呼ぶ
     .forEach(System.out::println);

// Comparatorでの使用（超頻出！）
List<String> names = Arrays.asList("John", "alice", "Bob");
names.sort(String::compareToIgnoreCase);  // 要素同士を比較
```

**試験ポイント：**
- `String::length` → 各文字列の`length()`を呼ぶ
- `String::isEmpty` → 各文字列の`isEmpty()`を呼ぶ
- `String::trim` → 各文字列の`trim()`を呼ぶ

これらは**パターン3**だから注意！

---

### 4. コンストラクタ参照：`ClassName::new`

**コンストラクタを参照する**パターン。`new`キーワードを使うよ。

```java
// ラムダ式
Supplier<List<String>> lambda = () -> new ArrayList<>();

// メソッド参照
Supplier<List<String>> methodRef = ArrayList::new;
```

**対応関係：**
- `new ArrayList<>()` → `ArrayList::new`
- 引数の数に応じて適切なコンストラクタが選ばれる

**引数ありコンストラクタ：**
```java
// 1引数コンストラクタ
Function<String, Person> lambda = name -> new Person(name);
Function<String, Person> methodRef = Person::new;

// 2引数コンストラクタ
BiFunction<String, Integer, Person> methodRef = Person::new;
```

**配列のコンストラクタ参照（超重要！）：**
```java
// 配列を生成
IntFunction<String[]> arrayCreator = String[]::new;
String[] array = arrayCreator.apply(5);  // 長さ5の配列

// Streamのtoarray()で超頻出！
List<String> list = Arrays.asList("a", "b", "c");
String[] array = list.stream()
                     .toArray(String[]::new);  // 配列に変換
```

**よく使う例：**
```java
List<String> names = Arrays.asList("Alice", "Bob", "Charlie");

// 各名前からPersonオブジェクトを生成
List<Person> people = names.stream()
                           .map(Person::new)  // コンストラクタ参照
                           .collect(Collectors.toList());

// Collectorsでも使える
Set<Person> peopleSet = names.stream()
                             .map(Person::new)
                             .collect(Collectors.toCollection(HashSet::new));
```

---

## メソッド参照とラムダ式の対応表

| メソッド参照 | ラムダ式 | 説明 |
|------------|---------|------|
| `Integer::parseInt` | `s -> Integer.parseInt(s)` | 静的メソッド |
| `System.out::println` | `s -> System.out.println(s)` | 特定オブジェクトのメソッド |
| `String::toLowerCase` | `s -> s.toLowerCase()` | 任意オブジェクトのメソッド |
| `String::startsWith` | `(s, prefix) -> s.startsWith(prefix)` | 任意オブジェクト（2引数） |
| `ArrayList::new` | `() -> new ArrayList<>()` | コンストラクタ（引数なし） |
| `Person::new` | `name -> new Person(name)` | コンストラクタ（引数あり） |
| `String[]::new` | `size -> new String[size]` | 配列コンストラクタ |

---

## メソッド参照が使える条件

メソッド参照が使えるのは、**ラムダ式が単純にメソッドを呼ぶだけ**の場合だけである。

### 使える例：

```java
// OK: 引数をそのまま渡すだけ
list.stream().map(String::toLowerCase);

// OK: メソッドを呼ぶだけ
list.forEach(System.out::println);

// OK: コンストラクタを呼ぶだけ
list.stream().map(Person::new);
```

### 使えない例：

```java
// NG: 演算が入る
list.stream().map(n -> n * 2);  // メソッド参照不可

// NG: 引数を加工する
list.stream().map(s -> s.toLowerCase() + "!");  // メソッド参照不可

// NG: 固定値の引数を渡す
list.stream().map(s -> s.substring(0, 3));  // メソッド参照不可

// NG: 複数のメソッドを呼ぶ
list.stream().map(s -> s.trim().toLowerCase());  // メソッド参照不可
```

**ポイント：**
- 引数をそのまま渡すだけ → OK
- 引数を加工する → NG（ラムダ式を使う）
- 固定値を渡す → NG（ラムダ式を使う）

---

## 試験でよく出る引っかけポイント

### 1. パターン1とパターン3の見分け

```java
// これはどっち？
Function<String, Integer> f = String::length;
```

**答え：パターン3（任意オブジェクトのインスタンスメソッド参照）**

- `length()`は**インスタンスメソッド**（静的じゃない）
- 各Stringオブジェクトの`length()`を呼ぶ

```java
// 比較：これはパターン1
Function<String, Integer> f = Integer::parseInt;
// parseInt()は静的メソッド
```

### 2. Comparatorでのメソッド参照

```java
// よく出る！
List<String> list = Arrays.asList("c", "a", "b");
list.sort(String::compareToIgnoreCase);  // これはパターン3
```

**なぜパターン3？**
- `compareToIgnoreCase()`はインスタンスメソッド
- 第一引数がレシーバー、第二引数がメソッドの引数になる
- `(s1, s2) -> s1.compareToIgnoreCase(s2)` と同じ

### 3. コンストラクタ参照の引数

```java
// これはコンパイルエラー？
Function<String, Person> f = Person::new;
```

**答え：コンパイル成功**（Personに1引数のString型コンストラクタがある場合）

**コンパイルエラーになる場合：**
```java
// Personに引数なしコンストラクタしかない場合
Function<String, Person> f = Person::new;  // エラー！
// 正しくは Supplier<Person> f = Person::new;
```

### 4. thisとsuperのメソッド参照

```java
class MyClass {
    public void doSomething() {
        // 自分のメソッドを参照
        Consumer<String> c1 = this::print;

        // 親クラスのメソッドを参照
        Consumer<String> c2 = super::print;
    }

    private void print(String s) {
        System.out.println(s);
    }
}
```

**ポイント：**
- `this::method` → 現在のインスタンスのメソッド（パターン2）
- `super::method` → 親クラスのメソッド（パターン2）

### 5. ジェネリクスとメソッド参照

```java
// これは動く？
List<String> list = Arrays.asList("1", "2", "3");
List<Integer> numbers = list.stream()
                            .map(Integer::parseInt)
                            .collect(Collectors.toList());
```

**答え：動く！**

- `Integer::parseInt`は`Function<String, Integer>`として扱われる
- 型推論が効くから問題なし

---

## 実戦での使い方

### Stream APIでの頻出パターン

```java
List<String> list = Arrays.asList("apple", "BANANA", "Cherry");

// map
list.stream()
    .map(String::toLowerCase)      // パターン3
    .map(String::trim)             // パターン3
    .forEach(System.out::println); // パターン2

// filter + map
list.stream()
    .filter(s -> s.length() > 5)   // ラムダ式（条件が複雑）
    .map(String::toUpperCase)      // パターン3
    .forEach(System.out::println); // パターン2

// sorted
list.stream()
    .sorted(String::compareToIgnoreCase)  // パターン3
    .forEach(System.out::println);

// collect
List<Person> people = list.stream()
                          .map(Person::new)  // パターン4
                          .collect(Collectors.toList());

// toArray
String[] array = list.stream()
                     .toArray(String[]::new);  // パターン4（配列）
```

### Collectors.toCollection()

```java
// HashSetに集める
Set<String> set = list.stream()
                      .map(String::toLowerCase)
                      .collect(Collectors.toCollection(HashSet::new));  // パターン4

// TreeSetに集める
Set<String> treeSet = list.stream()
                          .collect(Collectors.toCollection(TreeSet::new));
```

### Comparator.comparing()

```java
List<Person> people = Arrays.asList(
    new Person("Alice", 30),
    new Person("Bob", 25)
);

// 名前でソート
people.sort(Comparator.comparing(Person::getName));  // パターン3

// 年齢でソート
people.sort(Comparator.comparing(Person::getAge));   // パターン3

// 逆順
people.sort(Comparator.comparing(Person::getAge).reversed());
```

---

## まとめ

### 4つのパターンの暗記法

1. **`ClassName::staticMethod`** → 「クラス名::静的メソッド」
2. **`instance::method`** → 「インスタンス変数::メソッド」
3. **`ClassName::instanceMethod`** → 「クラス名::インスタンスメソッド」（紛らわしい！）
4. **`ClassName::new`** → 「クラス名::new」

### 判別のコツ

- **`::`の前がクラス名** → パターン1 or 3 or 4
  - `new`がある → パターン4
  - メソッドが静的 → パターン1
  - メソッドがインスタンス → パターン3
- **`::`の前が変数名/this/super** → パターン2

### 試験対策

- パターン3が一番狙われる！特に`String::length`や`String::compareToIgnoreCase`
- `toArray(String[]::new)`は超頻出
- メソッド参照が使えないケース（引数の加工が必要）も理解すること
- コンストラクタ参照で引数の数が合わないとコンパイルエラーになる

### 実戦では

- シンプルな処理はメソッド参照
- 複雑な処理はラムダ式
- 無理にメソッド参照を使おうとしないこと

これで完璧ではないだろうか頑張ろう！
