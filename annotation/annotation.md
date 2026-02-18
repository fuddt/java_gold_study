
## アノテーション（Java Gold）

アノテーションはコードにメタデータ（追加情報）を付与する仕組みである。
試験ではメタアノテーションの仕様や、アノテーション要素の型制限がよく出る。

---

## 1. アノテーションとは

`@` 記号で始まる特殊なマーカー。クラス、メソッド、フィールドなどに付けられる。

```java
@Override
public String toString() {
    return "example";
}
```

---

## 2. 組み込みアノテーション

### @Override

スーパークラスやインターフェースのメソッドをオーバーライドしていることを明示。
該当メソッドが存在しないとコンパイルエラーになる → タイポ防止に役立つ。

```java
@Override
public String toString() { return "test"; }
```

### @Deprecated

非推奨のメソッドやクラスをマーク。使用すると警告が出る（エラーにはならない）。

```java
@Deprecated
public void oldMethod() { ... }
```

### @SuppressWarnings

コンパイラ警告を抑制する。

```java
@SuppressWarnings("unchecked")   // raw型の警告を抑制
@SuppressWarnings("deprecation") // 非推奨APIの警告を抑制
@SuppressWarnings({"unchecked", "rawtypes"}) // 複数指定
```

### @FunctionalInterface

関数型インターフェース（抽象メソッドが1つだけ）であることを明示。
- 抽象メソッドが0個or2個以上 → コンパイルエラー
- defaultメソッド、staticメソッドはカウントされない
- Objectのpublicメソッド（toString, equals等）もカウントされない

```java
@FunctionalInterface
interface MyFunction {
    void execute();           // 抽象メソッド1つ
    default void test() {}    // OK
    static void test2() {}    // OK
    String toString();        // OK（Objectのメソッド）
}
```

### @SafeVarargs

ジェネリクス可変長引数の型安全性警告を抑制。
**final、static、またはprivateメソッドにのみ**付けられる。

```java
@SafeVarargs
public final <T> void method(List<T>... lists) { ... }
```

通常のインスタンスメソッドには付けられない！

---

## 3. カスタムアノテーションの定義

`@interface` キーワードで定義する。

```java
@interface MyAnnotation {
    String value();               // 必須要素
    int version() default 1;      // デフォルト値あり
    String[] tags() default {};   // 配列型
}
```

### value の省略記法

要素が1つで名前が `value` の場合、使用時に名前を省略できる。

```java
@MyAnnotation("test")              // OK: value="test"
@MyAnnotation(value = "test")      // これも同じ

// ただし他の要素もある場合は省略不可
@Info(author = "Taro", date = "2026") // 複数要素 → 省略不可
```

---

## 4. メタアノテーション

アノテーションに付けるアノテーション。

### @Retention（保持期間）

アノテーションをいつまで保持するかを指定。**超重要！**

```java
@Retention(RetentionPolicy.SOURCE)   // ソースコードのみ
@Retention(RetentionPolicy.CLASS)    // クラスファイルまで（デフォルト!）
@Retention(RetentionPolicy.RUNTIME)  // 実行時まで
```

| ポリシー | ソース | .class | JVM実行時 | リフレクション |
|---------|--------|--------|----------|--------------|
| SOURCE | ○ | × | × | × |
| CLASS | ○ | ○ | × | × |
| RUNTIME | ○ | ○ | ○ | ○ |

**デフォルトはCLASS！**（RUNTIMEではない）
リフレクションで読み取りたいなら **RUNTIME** を指定すること。

### @Target（付けられる場所）

```java
@Target(ElementType.METHOD)                    // メソッドのみ
@Target({ElementType.TYPE, ElementType.FIELD}) // 複数指定
```

| ElementType | 説明 |
|-------------|------|
| TYPE | クラス、インターフェース、enum |
| FIELD | フィールド |
| METHOD | メソッド |
| PARAMETER | パラメータ |
| CONSTRUCTOR | コンストラクタ |
| LOCAL_VARIABLE | ローカル変数 |
| ANNOTATION_TYPE | アノテーション型 |
| PACKAGE | パッケージ |
| TYPE_PARAMETER | 型パラメータ（Java 8+） |
| TYPE_USE | 型の使用箇所（Java 8+） |
| MODULE | モジュール（Java 9+） |

**@Targetなしの場合**: すべての場所に付けられる（MODULE除く）。

### @Documented

JavaDocに含めるかどうかを指定。付けるとJavaDocに表示される。

### @Inherited

**クラスにのみ**継承される。メソッドやフィールドには継承されない！

```java
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface InheritedAnnotation {
    String value();
}

@InheritedAnnotation("親")
class Parent {}

class Child extends Parent {}
// → Child にも @InheritedAnnotation が継承される
```

### @Repeatable

同じアノテーションを複数回付けられるようにする。
**コンテナアノテーションが必須**。

```java
@Repeatable(Schedules.class)
@interface Schedule {
    String day();
}

// コンテナアノテーション（必須！）
@interface Schedules {
    Schedule[] value(); // 要素名は"value"でなければならない
}

// 使用
@Schedule(day = "月曜")
@Schedule(day = "水曜")
public void method() {}
```

コンテナの要素名は必ず `value` でなければならない。

---

## 5. アノテーション要素に使える型

**制限あり！** 試験頻出。

### 使える型

1. プリミティブ型（int, long, double, boolean等）
2. String
3. Class（Class<?>）
4. enum型
5. アノテーション型
6. 上記の配列

### 使えない型

- Object
- Integer等のラッパー型
- List<String>等のジェネリック型
- int[][] 等の多次元配列

```java
@interface Valid {
    int number();            // OK
    String text();           // OK
    Class<?> type();         // OK
    ElementType enumVal();   // OK
    int[] numbers();         // OK
}

@interface Invalid {
    Object obj();            // NG!
    Integer num();           // NG!
    List<String> list();     // NG!
    int[][] matrix();        // NG!
}
```

---

## 6. リフレクションでのアノテーション読み取り

`@Retention(RUNTIME)` のアノテーションのみ実行時に読み取れる。

```java
Class<?> clazz = MyClass.class;

// 存在確認
if (clazz.isAnnotationPresent(Info.class)) {
    Info info = clazz.getAnnotation(Info.class);
    String author = info.author();
}

// すべてのアノテーション
Annotation[] all = clazz.getAnnotations();          // 継承含む
Annotation[] declared = clazz.getDeclaredAnnotations(); // 直接付けたもののみ

// @Repeatable対応
Schedule[] schedules = method.getAnnotationsByType(Schedule.class);
```

| メソッド | 説明 |
|---------|------|
| `getAnnotation(Class)` | 指定アノテーション取得 |
| `getAnnotations()` | 全アノテーション（継承含む） |
| `getDeclaredAnnotations()` | 直接付けたもののみ |
| `isAnnotationPresent(Class)` | 存在確認 |
| `getAnnotationsByType(Class)` | @Repeatable対応で配列取得 |

---

## 試験ポイント・引っかけ問題

### Q1: @Retentionのデフォルト
```java
@interface MyAnnotation { String value(); }
```
リフレクションで読み取れる？

**A**: 読み取れない。デフォルトは `CLASS`。`RUNTIME` を指定する必要がある。

### Q2: @Inherited の範囲

```java
@Inherited @Target(ElementType.METHOD)
@interface MyAnnotation {}
```
メソッドに付けたら子クラスに継承される？

**A**: されない。`@Inherited` は**クラスレベルのアノテーションにのみ**適用される。

### Q3: @Repeatable のコンテナ

```java
@Repeatable(Tags.class)
@interface Tag { String value(); }

@interface Tags {
    Tag[] items(); // これでいい？
}
```

**A**: コンパイルエラー。コンテナの要素名は `value` でなければならない。`Tag[] value()` に修正が必要。

### Q4: アノテーション要素の型

```java
@interface Config {
    Integer timeout(); // これはOK？
}
```

**A**: コンパイルエラー。`Integer`（ラッパー型）は使えない。`int` なら OK。

### Q5: @SafeVarargs の付与先

```java
@SafeVarargs
public void method(List<String>... lists) {} // これはOK？
```

**A**: コンパイルエラー。`@SafeVarargs` は final、static、private メソッドにのみ付けられる。

### Q6: @Override とインターフェース

```java
interface Printable { void print(); }
class MyClass implements Printable {
    @Override
    public void print() {} // これはOK？
}
```

**A**: OK。インターフェースのメソッドの実装にも `@Override` は使える。

### Q7: @FunctionalInterface の条件

```java
@FunctionalInterface
interface MyFunc {
    void execute();
    boolean equals(Object o);
}
```

**A**: OK。`equals` は Object のメソッドなので抽象メソッドとしてカウントされない。

---

## まとめ

| トピック | 覚えるべきこと |
|---------|---------------|
| @Retention | デフォルトは**CLASS**（RUNTIMEではない！） |
| @Target | なしの場合は全場所に付けられる |
| @Inherited | **クラスのみ**に継承される |
| @Repeatable | コンテナの要素名は**value** |
| 要素の型 | プリミティブ、String、Class、enum、アノテーション、これらの配列 |
| @SafeVarargs | final/static/private メソッドのみ |
| リフレクション | RUNTIME のみ読み取り可能 |
