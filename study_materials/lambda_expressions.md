# ラムダ式の基本

## 概要
ラムダ式は、Java 8で導入された機能で、匿名関数を簡潔に記述するための構文です。
関数型プログラミングのスタイルをJavaに取り入れ、コードをより読みやすく、保守しやすくします。

## 重要なポイント

### 基本構文
```
(parameters) -> expression
(parameters) -> { statements; }
```

### 関数型インターフェース
- ラムダ式は関数型インターフェースの実装に使用される
- 関数型インターフェース = 抽象メソッドが1つだけのインターフェース
- `@FunctionalInterface` アノテーションで明示できる

### 変数のキャプチャ
- 外部変数は実質的にfinalである必要がある
- ローカル変数、インスタンス変数の扱いが異なる

## コード例

### 例1: Comparatorの実装
```java
// 従来の書き方
Collections.sort(list, new Comparator<String>() {
    @Override
    public int compare(String s1, String s2) {
        return s1.compareTo(s2);
    }
});

// ラムダ式
Collections.sort(list, (s1, s2) -> s1.compareTo(s2));

// メソッド参照
Collections.sort(list, String::compareTo);
```

### 例2: Runnableの実装
```java
// 従来の書き方
new Thread(new Runnable() {
    @Override
    public void run() {
        System.out.println("Hello from thread");
    }
}).start();

// ラムダ式
new Thread(() -> System.out.println("Hello from thread")).start();
```

### 例3: カスタム関数型インターフェース
```java
@FunctionalInterface
interface Calculator {
    int calculate(int a, int b);
}

public class Main {
    public static void main(String[] args) {
        // 使用例
        Calculator add = (a, b) -> a + b;
        Calculator multiply = (a, b) -> a * b;

        System.out.println(add.calculate(5, 3));      // 8
        System.out.println(multiply.calculate(5, 3)); // 15
    }
}
```

### 例4: 変数のキャプチャ
```java
// OK: 実質final
int threshold = 10;
list.stream()
    .filter(n -> n > threshold)
    .forEach(System.out::println);

// エラー: 変数が変更される
int count = 0;
list.forEach(item -> {
    count++;  // コンパイルエラー
});
```

## よくある間違い

### 1. 型推論の誤解
```java
// エラー: 型を指定する場合は全パラメータに必要
(String s1, s2) -> s1.compareTo(s2);  // NG

// 正しい
(String s1, String s2) -> s1.compareTo(s2);  // OK
(s1, s2) -> s1.compareTo(s2);               // OK (型推論)
```

### 2. returnの省略ルール
```java
// 単一式の場合はreturn不要
(a, b) -> a + b  // OK

// 複数文の場合はreturn必須
(a, b) -> {
    int sum = a + b;
    return sum;  // returnが必要
}
```

### 3. 外部変数の変更
```java
// エラー: 実質finalでない変数の参照
int count = 0;
list.forEach(item -> count++);  // NG

// 解決策: AtomicIntegerを使用
AtomicInteger count = new AtomicInteger(0);
list.forEach(item -> count.incrementAndGet());  // OK
```

## 試験対策のポイント

1. **ラムダ式の構文**
   - パラメータの括弧の省略ルール（パラメータが1つの場合のみ省略可）
   - 本体が単一式の場合の中括弧とreturnの省略

2. **関数型インターフェース**
   - `@FunctionalInterface` アノテーションの意味
   - 標準の関数型インターフェース（Predicate, Function, Consumer, Supplier）

3. **変数のスコープ**
   - 実質finalの概念
   - `this` の参照がラムダ式ではどこを指すか

4. **メソッド参照**
   - 静的メソッド参照: `ClassName::staticMethod`
   - インスタンスメソッド参照: `object::instanceMethod`
   - 任意オブジェクトのインスタンスメソッド参照: `ClassName::instanceMethod`
   - コンストラクタ参照: `ClassName::new`

## 練習問題

### 問題1
次のコードの出力は？
```java
List<String> list = Arrays.asList("a", "bb", "ccc");
list.sort((s1, s2) -> s2.length() - s1.length());
System.out.println(list);
```

<details>
<summary>答え</summary>
[ccc, bb, a] - 長さの降順にソートされる
</details>

### 問題2
次のコードはコンパイルエラーになるか？
```java
int x = 10;
Runnable r = () -> System.out.println(x);
x = 20;
r.run();
```

<details>
<summary>答え</summary>
コンパイルエラー - xが実質finalでないため
</details>

## 参考資料
- Oracle Java Tutorial - Lambda Expressions
- Effective Java 第3版 - 項目42, 43, 44

## 学習メモ
- ラムダ式とStream APIは密接に関連している
- メソッド参照は4種類すべて覚える必要がある
- 実質finalの概念は並行処理でも重要
