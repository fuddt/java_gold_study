# javaのEnum

Enum（列挙型）は、関連する定数の集合を定義するための特別なデータ型。
Enumを使用すると、コードの可読性と保守性が向上するので、他の言語でも広く使用されている。

ただ、言語ごとにEnumの機能が結構異なる・・・
PythonとRustでも結構異なる・・・で、Javaも例外ではない。

複数言語を触ると結構混乱してくる。


# まずはめっちゃシンプルなEnumの例

```java
package app;

enum Color {
    RED, BLUE, GREEN;
}


public class Main {
    public static void main(String[] args) {
        Color myColor = Color.RED;
        printColor(myColor);
    }
    
    private static void printColor(Color color) {
        switch (color) {
            case RED:
                System.out.println("The color is Red.");
                break;
            case BLUE:
                System.out.println("The color is Blue.");
                break;
            case GREEN:
                System.out.println("The color is Green.");
                break;
            default:
                System.out.println("Unknown color.");
        }
    }
}
```

JavaのEnumはクラスの一種であり、定数に加えてフィールドやメソッドを持つことができる。

```java
package app;

enum Day {
    MON(1), TUE(2), WED(3);

    private int num;

    Day(int num) {
        this.num = num;
    }

    public int getNum() {
        return num;
    }
}


public class Main {
    public static void main(String[] args) {
        Day day = Day.MON;
        System.out.println(day.getNum());
    }

}

>> 1

```
上記の例では、`Day` Enumに整数フィールド`num`を追加し、コンストラクタで初期化している。


# enum 定数ごとに振る舞いを変えられる

```java
enum Calc {
    PLUS {
        int apply(int a, int b) { return a + b; }
    },
    MINUS {
        int apply(int a, int b) { return a - b; }
    };

    abstract int apply(int a, int b);
}
public class Main {
    public static void main(String[] args) {
        int result1 = Calc.PLUS.apply(5, 3);
        int result2 = Calc.MINUS.apply(5, 3);
        System.out.println("PLUS: " + result1);
        System.out.println("MINUS: " + result2);
    }
}
>> PLUS: 8
>> MINUS: 2
```

ここまできたときにふとした疑問・・・

上記って

```java
interface Calc {
    int apply(int a, int b);
}

class Plus implements Calc {
    public int apply(int a, int b) {
        return a + b;
    }
}

class Minus implements Calc {
    public int apply(int a, int b) {
        return a - b;
    }
}
```
でも同じじゃね？？

じゃあ、Enumを使うメリットって何？？と悩んだのでAIに聞いてみた。

## メリット1：取りうる種類が「固定」であることを保証したい
```
Calc c = Calc.PLUS;  // OK
Calc c = new Plus(); // そもそも存在しない
```

利用者が勝手に種類を増やせない

「PLUS / MINUS しか存在しない」ことが 型として保証される

クラスだと：

- サブクラスをいくらでも作れる
- システム外から増やされる可能性がある

なるほど！そういう視点はなかった

# 気をつけたいところ
JavaのEnumがいろいろできるからといって、なんでもかんでもEnumで表現しようとすると、かえってコードが複雑になりそう

アンチパターンを作ってもらった

```java
enum OrderStatus {
    NEW {
        void onEnter() { /* DB更新 */ }
        void onExit() { /* ログ */ }
    },
    PAID {
        void onEnter() { /* 決済処理 */ }
        void onExit() { /* 通知 */ }
    },
    SHIPPED {
        void onEnter() { /* 配送API */ }
        void onExit() { /* メール */ }
    };

    abstract void onEnter();
    abstract void onExit();
}
```

- 業務ロジックが全部集まる
- 変更理由が複数になる
- 単一責任原則が崩壊

Enumに限った話ではないけど、便利だとなんでもかんでも使いたくなっちゃうよね。

ChatGPT的に良いEnumの条件

## 良い enum の条件

- 種類が有限
- 外部から増えない
- 振る舞いが軽い
- if を消すために使っている
- 状態や責務を持ちすぎない

だとさ