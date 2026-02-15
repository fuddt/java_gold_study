# Optional - Java Gold試験対策

## Optionalとは？

`Optional<T>`は、**nullかもしれない値を安全に扱うためのコンテナクラス**だよね。Java 8で導入された。

### なぜOptionalを使うのか？

- **NullPointerExceptionを防ぐため**
- nullチェックを忘れることによるバグを減らす
- APIの戻り値が「値がないかもしれない」ことを明示的に示せる

昔はこうだった：
```java
String name = getName(); // nullかもしれない
if (name != null) {
    System.out.println(name.toUpperCase());
}
```

Optionalを使うと：
```java
Optional<String> name = getName(); // 明示的に「nullかもしれない」
name.ifPresent(n -> System.out.println(n.toUpperCase()));
```

---

## Optionalの生成方法

### 1. Optional.of(T value)
```java
Optional<String> opt = Optional.of("Hello");
```

**重要：** 引数がnullだと`NullPointerException`が発生する！
```java
Optional<String> opt = Optional.of(null); // ← NullPointerException！
```

### 2. Optional.ofNullable(T value)
```java
String str = null;
Optional<String> opt = Optional.ofNullable(str); // OK、空のOptionalになる
```

nullかもしれない値を扱うときは`ofNullable()`を使うべし。

### 3. Optional.empty()
```java
Optional<String> opt = Optional.empty(); // 空のOptional
```

明示的に空のOptionalを作りたいときに使う。

---

## 値の取得メソッド

### get() - 危険！使うな！

```java
Optional<String> opt = Optional.empty();
String value = opt.get(); // ← NoSuchElementException発生！
```

**試験ポイント：** `get()`は値が存在しない場合に`NoSuchElementException`を投げる。基本的に使わない方がいい。

### orElse(T other)
```java
Optional<String> opt = Optional.empty();
String value = opt.orElse("デフォルト値"); // "デフォルト値"が返される
```

値がない場合にデフォルト値を返す。

### orElseGet(Supplier<T> supplier)
```java
Optional<String> opt = Optional.empty();
String value = opt.orElseGet(() -> "デフォルト値");
```

値がない場合にSupplierから値を取得する。

### orElseThrow()
```java
Optional<String> opt = Optional.empty();
String value = opt.orElseThrow(); // NoSuchElementException
```

値がない場合に例外を投げる。カスタム例外も指定できる：
```java
String value = opt.orElseThrow(() -> new IllegalStateException("値がない"));
```

---

## 超重要！orElse vs orElseGet の違い（試験頻出！）

これは**試験で絶対に出る**から覚えておけ！

### orElse(T other)
```java
Optional<String> opt = Optional.of("存在する");
String result = opt.orElse(expensiveMethod()); // ← メソッドは必ず実行される！
```

**重要：** `orElse()`の引数は**常に評価される**。値が存在していても`expensiveMethod()`が実行される！

### orElseGet(Supplier<T> supplier)
```java
Optional<String> opt = Optional.of("存在する");
String result = opt.orElseGet(() -> expensiveMethod()); // ← 値があるので実行されない
```

**重要：** `orElseGet()`の引数は**遅延評価される**。値が存在する場合はSupplierが実行されない！

### 違いのまとめ

| メソッド | 値が存在する場合 | 値が存在しない場合 |
|---------|---------------|----------------|
| `orElse(other)` | `other`は**常に評価される**（無駄！） | `other`が返される |
| `orElseGet(supplier)` | `supplier`は**評価されない**（効率的！） | `supplier.get()`が実行されて返される |

**試験での引っかけ：**
```java
Optional<String> opt = Optional.of("値あり");
String result = opt.orElse(createDefault()); // createDefault()は実行される？ → YES！
```

---

## 値の存在チェック

### isPresent()
```java
Optional<String> opt = Optional.of("Hello");
if (opt.isPresent()) { // true
    System.out.println(opt.get());
}
```

値が存在するかチェックする。

### isEmpty() (Java 11+)
```java
Optional<String> opt = Optional.empty();
if (opt.isEmpty()) { // true
    System.out.println("値なし");
}
```

値が存在しないかチェックする。`!isPresent()`と同じ意味。

---

## 値があるときの処理

### ifPresent(Consumer<T> action)
```java
Optional<String> opt = Optional.of("Hello");
opt.ifPresent(value -> System.out.println(value)); // 値があれば出力
```

値が存在する場合のみアクションを実行する。

### ifPresentOrElse(Consumer<T> action, Runnable emptyAction) (Java 9+)
```java
Optional<String> opt = Optional.empty();
opt.ifPresentOrElse(
    value -> System.out.println("値: " + value),
    () -> System.out.println("値なし")
);
```

値がある場合とない場合の両方を処理できる。

---

## 値の変換

### map(Function<T, U> mapper)
```java
Optional<String> name = Optional.of("yamada");
Optional<String> upper = name.map(String::toUpperCase); // Optional["YAMADA"]
Optional<Integer> length = name.map(String::length);    // Optional[6]
```

Optionalの値を別の値に変換する。値がない場合は空のOptionalのまま。

### flatMap(Function<T, Optional<U>> mapper)
```java
Optional<String> name = Optional.of("yamada");
Optional<String> email = name.flatMap(n -> findEmail(n)); // Optional["yamada@example.com"]
```

Optionalを返すメソッドの結果を平坦化する。

**mapとflatMapの違い：**
```java
// mapだとOptional<Optional<String>>になってしまう
Optional<Optional<String>> nested = name.map(n -> findEmail(n));

// flatMapならOptional<String>に平坦化される
Optional<String> flat = name.flatMap(n -> findEmail(n));
```

---

## Optionalのアンチパターン

### 1. isPresentでチェックしてからgetする
```java
// ❌ 悪い例
if (opt.isPresent()) {
    String value = opt.get();
    System.out.println(value);
}

// ✅ 良い例
opt.ifPresent(value -> System.out.println(value));
```

### 2. Optionalをフィールドやメソッド引数に使う
```java
// ❌ 悪い例
class User {
    private Optional<String> name; // フィールドにOptionalは使わない
}

void setName(Optional<String> name) {} // 引数にOptionalは使わない

// ✅ 良い例
class User {
    private String name; // nullableなフィールド
}

Optional<String> getName() { // 戻り値でOptionalを使う
    return Optional.ofNullable(name);
}
```

### 3. Optionalでnullを返す
```java
// ❌ 最悪
Optional<String> getName() {
    return null; // Optionalの意味がない！
}

// ✅ 正しい
Optional<String> getName() {
    return Optional.empty();
}
```

---

## 試験ポイント・引っかけ問題

### 1. Optional.of(null)
```java
Optional<String> opt = Optional.of(null); // NullPointerException！
```
**答え：** 例外が発生する。`ofNullable()`を使うべき。

### 2. get()で値を取得
```java
Optional<String> opt = Optional.empty();
String value = opt.get(); // NoSuchElementException！
```
**答え：** 例外が発生する。`orElse()`などを使うべき。

### 3. orElseの評価タイミング
```java
Optional<String> opt = Optional.of("値あり");
String result = opt.orElse(expensiveMethod());
```
**質問：** `expensiveMethod()`は実行される？
**答え：** YES！`orElse()`の引数は常に評価される。

### 4. orElseGetの評価タイミング
```java
Optional<String> opt = Optional.of("値あり");
String result = opt.orElseGet(() -> expensiveMethod());
```
**質問：** `expensiveMethod()`は実行される？
**答え：** NO！値が存在するので実行されない。

### 5. mapの連鎖
```java
Optional<String> name = Optional.of("yamada");
Optional<Integer> length = name.map(String::toUpperCase).map(String::length);
System.out.println(length.orElse(0)); // 6
```
**答え：** mapは連鎖できる。途中で空になったらそこで終了。

### 6. 空のOptionalに対する操作
```java
Optional<String> empty = Optional.empty();
Optional<String> result = empty.map(String::toUpperCase);
System.out.println(result); // Optional.empty
```
**答え：** 空のOptionalに`map()`を適用しても空のまま。

### 7. orElseThrowの動作
```java
Optional<String> opt = Optional.empty();
String value = opt.orElseThrow(); // 例外の種類は？
```
**答え：** `NoSuchElementException`が投げられる。

---

## コード例でまとめ

```java
// 生成
Optional<String> opt1 = Optional.of("Hello");           // OK
Optional<String> opt2 = Optional.of(null);              // NullPointerException
Optional<String> opt3 = Optional.ofNullable(null);      // OK (空のOptional)
Optional<String> opt4 = Optional.empty();               // 空のOptional

// 値の取得
String v1 = opt1.get();                                 // "Hello"（危険！）
String v2 = opt1.orElse("default");                     // "Hello"
String v3 = opt4.orElse("default");                     // "default"
String v4 = opt1.orElseGet(() -> "default");            // "Hello"
String v5 = opt1.orElseThrow();                         // "Hello"

// 値の存在チェック
boolean b1 = opt1.isPresent();                          // true
boolean b2 = opt4.isEmpty();                            // true

// 値があるときの処理
opt1.ifPresent(v -> System.out.println(v));             // "Hello"出力
opt4.ifPresent(v -> System.out.println(v));             // 何もしない

// 値の変換
Optional<String> upper = opt1.map(String::toUpperCase); // Optional["HELLO"]
Optional<Integer> len = opt1.map(String::length);       // Optional[5]
```

---

## まとめ

- **Optional.of(null)は例外** → `ofNullable()`を使え
- **get()は危険** → `orElse()`系を使え
- **orElse vs orElseGet** → パフォーマンスが違う！試験頻出！
- **isPresentでチェックしてget()はアンチパターン** → `ifPresent()`を使え
- **Optionalはメソッドの戻り値で使う** → フィールドや引数には使わない

これで試験はバッチリだね！
