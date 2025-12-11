# AIチャットログ - 2025-01-01 - ラムダ式の基本

## 対話の目的
ラムダ式の基本的な構文と実用的な使用例を理解する

## 質問と回答

### Q1: Javaのラムダ式とは何ですか？簡潔に教えてください。

**回答:**
ラムダ式は、匿名関数を簡潔に記述するための構文です。Java 8から導入されました。
基本的な構文は `(引数) -> { 処理 }` です。

主な特徴:
- コードを簡潔に書ける
- 関数型インターフェース（抽象メソッドが1つだけのインターフェース）の実装に使用
- 外部変数を参照できる（実質的にfinal）

**理解したこと:**
- ラムダ式は匿名関数の短縮記法
- 関数型インターフェースと組み合わせて使う
- 変数キャプチャには制約がある（final or 実質final）

---

### Q2: ラムダ式の実用的な使用例を教えてください

**回答:**
よくある使用例：

1. リストのソート
```java
List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
names.sort((s1, s2) -> s1.compareTo(s2));
// または
names.sort(String::compareTo);  // メソッド参照
```

2. コレクションのフィルタリング
```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6);
List<Integer> evenNumbers = numbers.stream()
    .filter(n -> n % 2 == 0)
    .collect(Collectors.toList());
```

3. スレッドの実装
```java
new Thread(() -> System.out.println("Hello from thread")).start();
```

**理解したこと:**
- ソート、フィルタリング、スレッド処理などでよく使われる
- Stream APIと組み合わせると強力
- メソッド参照（`::`）も覚える必要がある

---

### Q3: ラムダ式で使える変数に制約があると聞きましたが、詳しく教えてください

**回答:**
ラムダ式内で外部変数を参照する場合、その変数は実質的にfinalである必要があります。

実質的にfinalとは:
- 明示的にfinalキーワードがついていなくても
- 値が変更されない変数のこと

エラー例:
```java
int count = 0;
list.forEach(item -> {
    count++;  // コンパイルエラー！countが変更されている
});
```

正しい例:
```java
final int threshold = 10;
list.stream()
    .filter(n -> n > threshold)  // OK: thresholdは変更されない
    .forEach(System.out::println);
```

**理解したこと:**
- ラムダ式内で外部変数を変更できない
- これは並行処理時の安全性のため
- カウンタが必要な場合はAtomicIntegerなどを使う必要がある

---

## まとめ
- ラムダ式は `(引数) -> { 処理 }` という構文で匿名関数を表現
- 関数型インターフェースの実装に使用される
- Stream API、Collections、スレッドなど様々な場面で活用できる
- 外部変数の参照には制約がある（実質final）
- メソッド参照（`::`）も合わせて学ぶべき

## 関連リンク
- Oracle Java Tutorial - Lambda Expressions
- Java 8 Stream API Documentation

## 次に学ぶこと
- メソッド参照の詳細
- 標準の関数型インターフェース（Predicate, Function, Consumer, Supplier）
- Stream APIとの組み合わせ
