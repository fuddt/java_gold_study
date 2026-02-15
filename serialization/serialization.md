# シリアライゼーション（Serialization）完全攻略

## シリアライズって何？

シリアライズっていうのは、**Javaオブジェクトをバイト列に変換して保存したり、ネットワーク経由で送信できるようにする仕組み**だよね。逆にバイト列からオブジェクトに戻すことを**デシリアライズ**って言うんだ。

```java
オブジェクト → [シリアライズ] → バイト列（ファイルやネットワークで送信）
バイト列 → [デシリアライズ] → オブジェクト（復元）
```

## Serializableインターフェース（超基本）

### マーカーインターフェース

`Serializable`は**マーカーインターフェース**って呼ばれてて、メソッドが一つも定義されてないんだよね。単に「このクラスはシリアライズできますよ」っていう目印なんだ。

```java
class Person implements Serializable {
    private static final long serialVersionUID = 1L;  // これも重要！

    private String name;
    private int age;

    // 普通のクラスとして実装
}
```

### 使い方の基本

```java
// シリアライズ（書き込み）
try (ObjectOutputStream out = new ObjectOutputStream(
        new FileOutputStream("person.ser"))) {
    Person p = new Person("太郎", 30);
    out.writeObject(p);  // オブジェクトを書き込む
}

// デシリアライズ（読み込み）
try (ObjectInputStream in = new ObjectInputStream(
        new FileInputStream("person.ser"))) {
    Person p = (Person) in.readObject();  // オブジェクトを読み込む
}
```

## transientキーワード（めっちゃ重要）

### transientの意味

`transient`をつけたフィールドは**シリアライズされない**んだ。パスワードとか、保存したくない情報に使うよね。

```java
class User implements Serializable {
    private String username;           // シリアライズされる
    private transient String password; // シリアライズされない！

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
```

### デシリアライズ後の値

transientフィールドは、デシリアライズ後に**デフォルト値**になるんだよ。

```java
User user = new User("taro", "secret123");
// シリアライズ → デシリアライズ
// 復元後：username = "taro", password = null （デフォルト値）
```

| 型 | デフォルト値 |
|---|---|
| 参照型（String等） | `null` |
| int | `0` |
| boolean | `false` |
| double | `0.0` |

## serialVersionUID（互換性管理）

### なぜ必要？

`serialVersionUID`は**クラスのバージョン管理**に使われるんだ。これがないと、クラス構造が少し変わっただけでデシリアライズが失敗する可能性があるんだよね。

```java
class Person implements Serializable {
    // これを明示的に定義すべき！
    private static final long serialVersionUID = 1L;

    private String name;
    private int age;
}
```

### serialVersionUIDが一致しない場合

```java
// 古いバージョン（serialVersionUID = 1L）でシリアライズ
// 新しいバージョン（serialVersionUID = 2L）でデシリアライズ
// → InvalidClassException が発生！
```

### 試験ポイント

- `serialVersionUID`を**明示的に定義しないと、コンパイラが自動生成する**
- 自動生成されたIDは**クラス構造が変わると変わる**から、互換性が壊れやすい
- だから**明示的に定義するのがベストプラクティス**だよね

## デシリアライズ時のコンストラクタ呼び出し（超重要！試験頻出）

ここが**一番の引っかけポイント**じゃね？めっちゃ重要だから、しっかり覚えてね。

### ルール1: Serializableクラス自身のコンストラクタは呼ばれない

```java
class Person implements Serializable {
    private String name;

    public Person(String name) {
        this.name = name;
        System.out.println("コンストラクタ呼ばれた！");
    }
}

// デシリアライズ時：
Person p = (Person) in.readObject();
// → 「コンストラクタ呼ばれた！」は出力されない！
// → コンストラクタを経由せずにオブジェクトが復元される
```

**なぜ？** → Javaがリフレクションを使って直接フィールドに値を設定するから

### ルール2: 親クラスがSerializableでない場合、親の引数なしコンストラクタが呼ばれる！

これが**超頻出の引っかけ問題**だよ！

```java
// 親クラス：Serializableを実装していない
class Animal {
    protected String species;

    // 引数なしコンストラクタ（必須！）
    public Animal() {
        this.species = "Unknown";
        System.out.println("Animal()呼ばれた");
    }

    public Animal(String species) {
        this.species = species;
    }
}

// 子クラス：Serializableを実装
class Dog extends Animal implements Serializable {
    private String name;

    public Dog(String species, String name) {
        super(species);
        this.name = name;
    }
}

// 使用例
Dog dog = new Dog("柴犬", "ポチ");
System.out.println(dog.species);  // "柴犬"

// シリアライズ → デシリアライズ
Dog restored = (Dog) in.readObject();
System.out.println(restored.species);  // "Unknown" （引数なしコンストラクタが呼ばれたため）
System.out.println(restored.name);     // "ポチ" （Dogクラスのフィールドは復元される）
```

**超重要ポイント：**
1. 親クラス`Animal`は`Serializable`じゃない
2. デシリアライズ時に**親の引数なしコンストラクタ`Animal()`が呼ばれる**
3. だから`species`は`"Unknown"`になる（元の`"柴犬"`は失われる）
4. 子クラス`Dog`のフィールド`name`は正しく復元される
5. **親クラスに引数なしコンストラクタがないと、デシリアライズ時に例外が発生する**

### 試験の引っかけポイント

```java
// Q: デシリアライズ後、dogのspeciesの値は？
Dog dog = new Dog("柴犬", "ポチ");
// シリアライズ → デシリアライズ
Dog restored = (Dog) in.readObject();
System.out.println(restored.species);

// A: "Unknown"
// 理由：親クラスがSerializableじゃないから、引数なしコンストラクタが呼ばれる
```

## staticフィールドはシリアライズされない

`static`フィールドは**クラスに属する**から、シリアライズの対象外なんだよね。

```java
class Counter implements Serializable {
    private int instanceCount;          // シリアライズされる
    private static int totalCount = 0;  // シリアライズされない！

    public Counter(int instanceCount) {
        this.instanceCount = instanceCount;
        totalCount++;
    }
}

// 使用例
Counter c1 = new Counter(5);
Counter.totalCount = 100;  // staticフィールドを変更

// シリアライズ → デシリアライズ
Counter restored = (Counter) in.readObject();
System.out.println(restored.instanceCount);  // 5 （復元される）
System.out.println(Counter.totalCount);      // 100 （現在のJVMの値）
```

**ポイント：** staticフィールドは**現在のJVMの状態**を反映するよ。

## オブジェクトグラフのシリアライズ

オブジェクトが他のオブジェクトを参照してる場合、**参照先のオブジェクトもSerializableじゃないとダメ**なんだ。

```java
class Address implements Serializable {  // これもSerializable必須！
    private String city;

    public Address(String city) {
        this.city = city;
    }
}

class Person implements Serializable {
    private String name;
    private Address address;  // 参照先もSerializable

    public Person(String name, Address address) {
        this.name = name;
        this.address = address;
    }
}

// OK：PersonもAddressもSerializable
Person p = new Person("太郎", new Address("東京"));
out.writeObject(p);  // 成功！addressも一緒にシリアライズされる
```

### NotSerializableExceptionが発生するケース

```java
class NonSerializable {  // Serializableを実装していない
    private String data;
}

class BadPerson implements Serializable {
    private String name;
    private NonSerializable data;  // これが問題！
}

// NG：
BadPerson bad = new BadPerson("太郎", new NonSerializable());
out.writeObject(bad);  // NotSerializableException が発生！
```

**試験ポイント：** オブジェクトグラフのすべてのオブジェクトが`Serializable`を実装してないとダメだよ。

## カスタムシリアライゼーション

### writeObject/readObjectメソッド

特殊な処理が必要な場合、**privateメソッドとして`writeObject`と`readObject`を定義**できるんだ。

```java
class CustomPerson implements Serializable {
    private String name;
    private transient String password;  // transientだけど保存したい

    // カスタムシリアライゼーション（privateメソッド）
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();  // デフォルトのシリアライゼーション

        // パスワードを暗号化して保存
        String encrypted = encrypt(password);
        out.writeObject(encrypted);
    }

    // カスタムデシリアライゼーション（privateメソッド）
    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();  // デフォルトのデシリアライゼーション

        // 暗号化されたパスワードを復号化
        String encrypted = (String) in.readObject();
        this.password = decrypt(encrypted);
    }

    private String encrypt(String s) { /* 暗号化処理 */ }
    private String decrypt(String s) { /* 復号化処理 */ }
}
```

**重要ポイント：**
- `writeObject`/`readObject`は**privateメソッド**として定義
- シグネチャは**完全に一致**させる必要がある
- Javaが**リフレクションで自動的に呼び出す**
- `defaultWriteObject()`/`defaultReadObject()`を呼ぶのが一般的

## 試験で狙われる引っかけポイント

### 1. コンストラクタの呼び出し

```java
// Q: デシリアライズ時、何が出力される？
class Test implements Serializable {
    public Test() {
        System.out.println("コンストラクタ");
    }
}
Test t = (Test) in.readObject();

// A: 何も出力されない
// 理由：Serializableクラス自身のコンストラクタは呼ばれない
```

### 2. 親クラスの引っかけ

```java
// Q: デシリアライズ後、xの値は？
class Parent {
    int x = 10;
    public Parent() { x = 20; }
}
class Child extends Parent implements Serializable {
    int y = 30;
}

Child c = new Child();  // x=20, y=30
// シリアライズ → デシリアライズ
Child restored = (Child) in.readObject();

// A: x=20, y=30
// 理由：親の引数なしコンストラクタが呼ばれるから、x=20になる
```

### 3. transientの引っかけ

```java
// Q: デシリアライズ後、passwordの値は？
class User implements Serializable {
    String name = "太郎";
    transient String password = "secret";
}
User u = new User();
// シリアライズ → デシリアライズ
User restored = (User) in.readObject();

// A: password = null
// 理由：transientフィールドはシリアライズされないから、デフォルト値（null）になる
```

### 4. staticの引っかけ

```java
// Q: デシリアライズ後、countの値は？
class Counter implements Serializable {
    static int count = 0;
    int id;

    public Counter(int id) {
        this.id = id;
        count++;
    }
}

Counter c1 = new Counter(1);  // count=1
Counter c2 = new Counter(2);  // count=2
// c1をシリアライズ

Counter.count = 100;  // staticフィールドを変更

// c1をデシリアライズ
Counter restored = (Counter) in.readObject();

// A: count = 100
// 理由：staticフィールドはシリアライズされない。現在のJVMの値（100）が参照される
```

### 5. NotSerializableExceptionの引っかけ

```java
// Q: 例外が発生するのはどれ？

// ケース1
class A implements Serializable {
    String name;
}
A a = new A();
out.writeObject(a);  // OK

// ケース2
class B implements Serializable {
    transient Thread thread = new Thread();  // Threadは非Serializable
}
B b = new B();
out.writeObject(b);  // OK（transientだから無視される）

// ケース3
class C implements Serializable {
    Thread thread = new Thread();  // transientがない！
}
C c = new C();
out.writeObject(c);  // NotSerializableException！

// A: ケース3
// 理由：ThreadはSerializableじゃないし、transientもついてないから
```

## serialVersionUIDの引っかけ

```java
// Q: 以下の場合、デシリアライズは成功する？

// 古いバージョン
class Person implements Serializable {
    private static final long serialVersionUID = 1L;
    String name;
}
// シリアライズ

// 新しいバージョン（フィールド追加）
class Person implements Serializable {
    private static final long serialVersionUID = 1L;  // 同じUID
    String name;
    int age = 0;  // 新しいフィールド
}
// デシリアライズ

// A: 成功する
// 理由：serialVersionUIDが同じで、互換性のある変更（フィールド追加）だから
//       新しいフィールドageはデフォルト値（0）になる
```

## まとめ：覚えるべき超重要ポイント

1. **Serializableはマーカーインターフェース**（メソッドなし）
2. **transientフィールドはシリアライズされない** → デシリアライズ後はデフォルト値
3. **staticフィールドはシリアライズされない** → 現在のJVM状態を反映
4. **Serializableクラス自身のコンストラクタは呼ばれない**
5. **親クラスが非Serializableなら、親の引数なしコンストラクタが呼ばれる**（超重要！）
6. **オブジェクトグラフ全体がSerializableじゃないとNotSerializableException**
7. **serialVersionUIDは明示的に定義すべき**（互換性管理）
8. **カスタムシリアライゼーションはprivateメソッド**で実装

これだけ押さえとけば、試験のシリアライゼーション問題は完璧だよね！
