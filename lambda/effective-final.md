---

* ラムダ式から参照できるローカル変数は
  **final または effectively final**
* effectively final =
  **一度も再代入されていない変数**
* 再代入した瞬間に **コンパイルエラー**

---

## サンプル構成

```text
src/
 └─ app/
    └─ Main.java
```

---

## Main.java（OKな例）

```java
package app;

import java.util.function.IntPredicate;

public class Main {

    public static void main(String[] args) {

        int threshold = 10; 
        // threshold は一度も変更されていない
        // → effectively final

        IntPredicate isGreaterThan = x -> x > threshold;

        System.out.println(isGreaterThan.test(5));   // false
        System.out.println(isGreaterThan.test(15));  // true
    }
}
```

### ここで起きていること

* `threshold` は final と書いていない
* でも **再代入していない**
* だから **effectively final**
* ラムダ式から参照できる

---

## 次：NG な例（試験でよく出る）

```java
package app;

import java.util.function.IntPredicate;

public class Main {

    public static void main(String[] args) {

        int threshold = 10;

        IntPredicate isGreaterThan = x -> x > threshold;

        threshold = 20; // ← 再代入

        System.out.println(isGreaterThan.test(15));
    }
}
```

### 結果

```
コンパイルエラー
Local variable threshold defined in an enclosing scope must be final or effectively final

./src/app/Main.java:11: エラー: ラムダ式から参照されるローカル変数は、finalまたは事実上のfinalである必要があります
        IntPredicate isGreaterThan = x -> x > threshold;
                                              ^
エラー1個

```

---

## なぜダメなのか（ここが理解ポイント）

ラムダ式は **その場で実行されるとは限らない**。

```java
IntPredicate p = x -> x > threshold;
```

これは：

* いつ実行されるか分からない
* 別スレッドで実行される可能性もある
* 実行時に threshold が
  10 なのか 20 なのか分からなくなる


ラムダから見えるローカル変数は変更不可である必要がある。

---

## ここでよく出る勘違い

### フィールドは変更できる

```java
public class Main {

    static int threshold = 10;

    public static void main(String[] args) {

        IntPredicate p = x -> x > threshold;

        threshold = 20; // OK（フィールド）

        System.out.println(p.test(15)); // false
    }
}
```
```
app.Main 
false
```

### なぜOK？

* フィールドはヒープ上
* ラムダは **値ではなく参照**を見る
* Java が管理できる

---

## 試験で問われる比較ポイント
- ローカル変数
    - ラムダから参照できる
    - 再代入は不可（NG）
- effectively final な変数
    - ラムダから参照できる
    - 再代入は不可（NG）
- フィールド
    - ラムダから参照できる
    - 再代入も可能（OK）

---

ラムダはローカル変数を
**キャプチャはできるが変更はできない**

---

# いろんなラムダの書き方
## 式ラムダとステートメントラムダ

```java
package app;

import java.util.function.IntFunction;

public class Main {

    static int threshold = 10;

    public static void main(String[] args) {

        // 式ラムダ
        IntFunction<Integer> doublevalue = x -> x * 2;
        System.out.println(doublevalue.apply(5));

        // ステートメントラムダ
        IntFunction<Integer> tripleValue = x -> {
            int result = x*x;
            return result;
        };
    }
}
```

# 標準関数型インターフェース
## java.util.function パッケージ
Java には「よく使う形の関数型インタフェース」が 最初から用意されている。


- 判定したい → Predicate
- 変換したい → Function
- 使うだけ   → Consumer
- 生成したい → Supplier


```java
package app;

import java.util.function.Predicate;
import java.util.function.Function;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Main {

    public static void main(String[] args) {

        // ① Predicate：条件判定（true / false）
        Predicate<Integer> isAdult = age -> age >= 20;

        System.out.println(isAdult.test(18)); // false
        System.out.println(isAdult.test(25)); // true


        // ② Function：変換（T -> R）
        Function<String, Integer> lengthFunc = s -> s.length();

        System.out.println(lengthFunc.apply("Java")); // 4


        // ③ Consumer：消費（戻り値なし）
        Consumer<String> printer = s -> System.out.println("Hello " + s);

        printer.accept("World"); // Hello World


        // ④ Supplier：供給（引数なし）
        Supplier<String> messageSupplier = () -> "Hello from Supplier";

        System.out.println(messageSupplier.get());
    }
}

```

```
java --module-path out -m app/app.Main

false
true
4
Hello World
Hello from Supplier

```
