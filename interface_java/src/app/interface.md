# Java 抽象クラスとインタフェース

## 抽象クラスとインタフェースの比較
抽象クラス

- 抽象クラスは、他のクラスが継承できるように設計されたクラス
- 複数の抽象クラスを継承することはできない（単一継承）
- 抽象クラスは、状態（フィールド）と振る舞い（メソッド）を持つことができる
- 抽象クラスは、コンストラクタを持つことができる
- 抽象クラスは、アクセス修飾子を使用してメンバーの可視性を制御できる


インタフェース
- インタフェースは、クラスが実装できる契約を定義する
- クラスは複数のインタフェースを実装できる（多重実装）
- インタフェースは、状態を持つことはできない（Java 8以降では、デフォルトメソッドと静的メソッドを持つことができる）
- インタフェースは、コンストラクタを持つことはできない
- インタフェースのメンバーは、デフォルトでpublicであり、他のアクセス修飾子を使用できない


インターフェースに関してはPythonにはない概念。
似たようなことをするのにABCモジュールを使うことができるけど、Javaのインターフェースほど厳密ではない。
Protocols（PEP 544）も似たような概念を提供するが、これもJavaのインターフェースとは異なる。

ってことでPythonしかやったことがない人には？？？な内容。
Rustだとtraitが似たような概念を提供する。


# 使い分け
最初は
メソッドの他に状態（フィールド）を持たせたいってなったら抽象クラスを使い、
そうでなければインタフェースを使うって感じでいいと思う。


抽象クラスとインターフェースってなんのためにあるんだ！メリットは！？って最初思う。
独学で一番苦労したところ。ここで理解に詰まってしまったのは自分の中で「共同開発」を想定していなかったからだと思う。
抽象クラスやインターフェースは、コードの再利用性と保守性を高め、異なるクラス間での一貫性を確保するために使用される。
特に大規模なプロジェクトやチームでの開発において、抽象クラスとインターフェースは重要な役割を果たす。
抽象クラスは、共通の機能や状態を持つクラス群に対して、基本的な実装を提供するために使用される。
インターフェースは、異なるクラスが共通の契約を実装するための手段を提供し、異なるクラス間での相互運用性を促進する。

なんで、これらが共同開発で活躍するんだっていうと、
抽象クラスやインターフェースを使用することで、開発者は
- 共通の基盤を持つクラスを簡単に作成できる
- 異なるクラスが同じインターフェースを実装することで、一貫した方法で相互作用できる
- コードの変更が他の部分に影響を与えにくくなる
- 新しい機能を追加する際に、既存のコードを変更せずに済むことが多い
といったメリットがある。

なかなか個人で開発しているとピンとこないけど、チームで開発しているときには非常に役立つ概念。
お互いにルールを守ろうねっていうのをコードで会話するための手段と考えるとわかりやすいかも。

このメリットに気づいたのは、過去に自分が書いたコードをしばらくたってから見返したときだった。
最初は抽象クラスやインターフェースを使わずに書いていたから、記憶も無くなって、「あれ？このクラスってなんでこんな風に書いたんだっけ？」ってなった。

ここから、見様見真似で抽象クラスとインターフェースを使うようにしたら、数ヶ月経ったあとでも、「ああ、こういう意図で書いたんだな」って思い出せるようになった。

前置きはさておき、内容に入っていく！


# 抽象クラス

```java

abstract class Animal {
    // 抽象メソッド（サブクラスで実装する必要がある）
    public abstract void makeSound();

    // 具象メソッド（サブクラスで継承される）
    public void eat() {
        System.out.println("This animal is eating.");
    }
}
```
上記の例では、`Animal`クラスは抽象クラスであり、`makeSound`メソッドは抽象メソッドとして定義されている。 サブクラスはこのメソッドを実装する必要がある。一方、`eat`メソッドは具象メソッドであり、サブクラスで継承される。

```java
class Dog extends Animal {
    @Override
    public void makeSound() {
        System.out.println("Woof!");
    }
}
```
上記の`Dog`クラスは`Animal`クラスを継承し、`makeSound`メソッドを実装している。
eatメソッドは継承されているので、そのまま使用できる。

# インタフェース

```java
interface Flyable {
    // 抽象メソッド（サブクラスで実装する必要がある）
    void fly();
}
```
上記の例では、`Flyable`インタフェースが定義されており、`fly`メソッドは抽象メソッドとして宣言されている。 クラスはこのインタフェースを実装する必要がある。

```java
class Bird implements Flyable {
    @Override
    public void fly() {
        System.out.println("The bird is flying.");
    }
}
```
上記の`Bird`クラスは`Flyable`インタフェースを実装し、`fly`メソッドを提供している。

ここまできたときにちょっと思うのが、抽象クラスだけで良くないか？インターフェースって抽象クラスの下位互換じゃないの？って思う・・・

しかし、インターフェースには抽象クラスにはない利点がある。
- 多重実装が可能: クラスは複数のインターフェースを実装できるため、異なる機能を組み合わせることができる。
- 柔軟な設計: インターフェースはクラスの実装から独立しているため、異なるクラスが同じインターフェースを実装できる。
- 一貫性の確保: インターフェースを使用することで、異なるクラス間で一貫した方法で相互作用できる。

抽象クラスよりもインターフェースの方が機能が少ないじゃん！と思うかもしれないが、インターフェースのシンプルなので、
柔軟性が高く、特に多重実装が必要な場合には非常に有用。


# ポリモーフィズムを利用した設計
抽象クラスとインターフェースは、ポリモーフィズムを利用した設計において重要な役割を果たす。
ポリモーフィズムとは、異なるクラスが同じインターフェースや抽象クラスを共有し、共通の方法で操作できることを指す。


複数のクラスが共通のインターフェースを実装していれば、同じメソッドを呼び出すことができる。
```java
interface Shape {
    double area();
}
class Circle implements Shape {
    private double radius;

    public Circle(double radius) {
        this.radius = radius;
    }

    @Override
    public double area() {
        return Math.PI * radius * radius;
    }
}

class Polygon implements Shape {
    private double width;
    private double height;

    public Polygon(double width, double height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public double area() {
        return width * height;
    }
}

class Traiangle implements Shape {
    private double base;
    private double height;

    public Triangle(double base, double height) {
        this.base = base;
        this.height = height;
    }

    @Override
    public double area() {
        return 0.5 * base * height;
    }
}


public class Main {
    private static void printArea(Shape shape) {
        System.out.println("Area: " + shape.area());
    }

    public static void main(String[] args) {
        Shape circle = new Circle(5);
        Shape polygon = new Polygon(4, 6);
        Shape triangle = new Traiangle(4, 6);

        Shape[] shapes = {circle, polygon, triangle};
        for (Shape shape : shapes) {
            printArea(shape);
        }
    }
}

```

# Java 8以降のインタフェースの拡張
Java 8以降、インターフェースは以下のような新しい機能をサポートしている。
- デフォルトメソッド: インターフェース内で具象メソッドを定義できる。これにより、既存のインターフェースに新しいメソッドを追加しても、既存の実装に影響を与えない。
- 静的メソッド: インターフェース内で静的メソッドを定義できる。これにより、インターフェースに関連するユーティリティメソッドを提供できる。

```java
interface Vehicle {
    // 抽象メソッド
    void drive();
    // デフォルトメソッド
    default void honk() {
        System.out.println("Beep beep!");
    }
    // 静的メソッド
    static void service() {
        System.out.println("Vehicle is being serviced.");
    }
}
```
上記の`Vehicle`インターフェースには、抽象メソッド`drive`、デフォルトメソッド`honk`、および静的メソッド`service`が含まれている。

```java
class Car implements Vehicle {
    @Override
    public void drive() {
        System.out.println("The car is driving.");
    }
}
```
上記の`Car`クラスは`Vehicle`インターフェースを実装し、`drive`メソッドを提供している。 `honk`メソッドはデフォルト実装が提供されているため、オーバーライドする必要はない。

```java
public class Main {
    public static void main(String[] args) {
        Car myCar = new Car();
        myCar.drive(); // The car is driving.
        myCar.honk();  // Beep beep!
        Vehicle.service(); // Vehicle is being serviced.
    }
}
```
上記の`Main`クラスでは、`Car`オブジェクトを作成し、`drive`メソッドと`honk`メソッドを呼び出している。また、`Vehicle`インターフェースの静的メソッド`service`も呼び出している。
