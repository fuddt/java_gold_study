package app;

import java.io.*;
import java.nio.file.*;

// ========================================
// 1. 基本的なSerializableクラス
// ========================================

/**
 * Serializableインターフェースはマーカーインターフェース（メソッドを持たない）
 * これを実装することで、オブジェクトをバイト列に変換できるようになる
 */
class Person implements Serializable {
    // serialVersionUIDは互換性管理のために使用される
    // これがないと、クラス構造が変わった時にデシリアライズが失敗する可能性がある
    private static final long serialVersionUID = 1L;

    private String name;
    private int age;

    // transientキーワード：このフィールドはシリアライズされない
    // パスワードなど、保存したくないデータに使う
    private transient String password;

    // staticフィールドはシリアライズされない（クラスに属するため）
    private static int count = 0;

    public Person(String name, int age, String password) {
        this.name = name;
        this.age = age;
        this.password = password;
        count++;
        System.out.println("Person()コンストラクタが呼ばれた: " + name);
    }

    @Override
    public String toString() {
        return String.format("Person[name=%s, age=%d, password=%s, count=%d]",
                           name, age, password, count);
    }

    public static int getCount() {
        return count;
    }

    public static void resetCount() {
        count = 0;
    }
}

// ========================================
// 2. 参照先もSerializableでなければならない
// ========================================

class Address implements Serializable {
    private static final long serialVersionUID = 1L;
    private String city;
    private String street;

    public Address(String city, String street) {
        this.city = city;
        this.street = street;
    }

    @Override
    public String toString() {
        return String.format("Address[city=%s, street=%s]", city, street);
    }
}

class Employee implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    // このフィールドの型もSerializableでなければならない
    private Address address;

    public Employee(String name, Address address) {
        this.name = name;
        this.address = address;
        System.out.println("Employee()コンストラクタが呼ばれた: " + name);
    }

    @Override
    public String toString() {
        return String.format("Employee[name=%s, address=%s]", name, address);
    }
}

// ========================================
// 3. 親クラスがSerializableでない場合
// ========================================

/**
 * 親クラスがSerializableを実装していない場合
 * デシリアライズ時に親クラスの引数なしコンストラクタが呼ばれる！
 */
class Animal {
    protected String species;

    // 引数なしコンストラクタが必須
    public Animal() {
        this.species = "Unknown";
        System.out.println("Animal()引数なしコンストラクタが呼ばれた");
    }

    public Animal(String species) {
        this.species = species;
        System.out.println("Animal(String)コンストラクタが呼ばれた: " + species);
    }
}

class Dog extends Animal implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;

    public Dog(String species, String name) {
        super(species);
        this.name = name;
        System.out.println("Dog()コンストラクタが呼ばれた: " + name);
    }

    @Override
    public String toString() {
        return String.format("Dog[species=%s, name=%s]", species, name);
    }
}

// ========================================
// 4. NotSerializableExceptionのケース
// ========================================

// Serializableを実装していないクラス
class NonSerializableClass {
    private String data;

    public NonSerializableClass(String data) {
        this.data = data;
    }
}

class BadEmployee implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    // これが原因でNotSerializableExceptionが発生する
    private NonSerializableClass data;

    public BadEmployee(String name, NonSerializableClass data) {
        this.name = name;
        this.data = data;
    }
}

// ========================================
// 5. カスタムシリアライゼーション
// ========================================

/**
 * 独自のwriteObject/readObjectメソッドで
 * シリアライゼーションの挙動をカスタマイズできる
 */
class CustomPerson implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private transient String password; // transientだが、カスタムで保存する

    public CustomPerson(String name, String password) {
        this.name = name;
        this.password = password;
    }

    // カスタムシリアライゼーション：privateメソッドとして定義
    private void writeObject(ObjectOutputStream out) throws IOException {
        System.out.println("カスタムwriteObjectが呼ばれた");
        out.defaultWriteObject(); // デフォルトのシリアライゼーションを実行
        // パスワードを暗号化して保存（ここでは単純に逆順にする）
        String encrypted = new StringBuilder(password).reverse().toString();
        out.writeObject(encrypted);
    }

    // カスタムデシリアライゼーション：privateメソッドとして定義
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        System.out.println("カスタムreadObjectが呼ばれた");
        in.defaultReadObject(); // デフォルトのデシリアライゼーションを実行
        // 暗号化されたパスワードを復号化（逆順を戻す）
        String encrypted = (String) in.readObject();
        this.password = new StringBuilder(encrypted).reverse().toString();
    }

    @Override
    public String toString() {
        return String.format("CustomPerson[name=%s, password=%s]", name, password);
    }
}

// ========================================
// メインクラス
// ========================================

public class Main {

    public static void main(String[] args) {
        System.out.println("=== Java Gold シリアライゼーション学習 ===\n");

        demo1_BasicSerialization();
        demo2_TransientAndStatic();
        demo3_ObjectGraph();
        demo4_NonSerializableParent();
        demo5_NotSerializableException();
        demo6_CustomSerialization();
    }

    // ========================================
    // デモ1: 基本的なシリアライゼーション
    // ========================================
    private static void demo1_BasicSerialization() {
        System.out.println("【デモ1】基本的なシリアライゼーション");
        System.out.println("-------------------------------------");

        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("person", ".ser");

            // シリアライゼーション（書き込み）
            Person person = new Person("太郎", 30, "secret123");
            System.out.println("シリアライズ前: " + person);

            try (ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream(tempFile.toFile()))) {
                out.writeObject(person);
                System.out.println("シリアライズ完了");
            }

            // デシリアライゼーション（読み込み）
            System.out.println("\nデシリアライズ開始...");
            try (ObjectInputStream in = new ObjectInputStream(
                    new FileInputStream(tempFile.toFile()))) {
                Person restored = (Person) in.readObject();
                System.out.println("デシリアライズ後: " + restored);
                // 注目：Personのコンストラクタは呼ばれていない！
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cleanup(tempFile);
        }

        System.out.println();
    }

    // ========================================
    // デモ2: transientとstaticの挙動
    // ========================================
    private static void demo2_TransientAndStatic() {
        System.out.println("【デモ2】transientとstaticの挙動");
        System.out.println("-------------------------------------");

        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("transient", ".ser");

            Person.resetCount();

            // シリアライゼーション
            Person person = new Person("花子", 25, "password456");
            System.out.println("シリアライズ前: " + person);
            System.out.println("シリアライズ前のcount: " + Person.getCount());

            try (ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream(tempFile.toFile()))) {
                out.writeObject(person);
            }

            // staticフィールドを変更
            Person dummy = new Person("次郎", 40, "dummy");
            System.out.println("現在のcount: " + Person.getCount());

            // デシリアライゼーション
            try (ObjectInputStream in = new ObjectInputStream(
                    new FileInputStream(tempFile.toFile()))) {
                Person restored = (Person) in.readObject();
                System.out.println("\nデシリアライズ後: " + restored);
                System.out.println("重要ポイント:");
                System.out.println("- passwordはnull（transientなので保存されなかった）");
                System.out.println("- countは2（staticフィールドは現在のJVM状態を反映）");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cleanup(tempFile);
        }

        System.out.println();
    }

    // ========================================
    // デモ3: オブジェクトグラフのシリアライゼーション
    // ========================================
    private static void demo3_ObjectGraph() {
        System.out.println("【デモ3】オブジェクトグラフのシリアライゼーション");
        System.out.println("-------------------------------------");

        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("employee", ".ser");

            Address address = new Address("東京", "渋谷1-2-3");
            Employee employee = new Employee("山田", address);

            System.out.println("シリアライズ前: " + employee);

            // シリアライゼーション
            try (ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream(tempFile.toFile()))) {
                out.writeObject(employee);
                System.out.println("シリアライズ完了");
            }

            // デシリアライゼーション
            System.out.println("\nデシリアライズ開始...");
            try (ObjectInputStream in = new ObjectInputStream(
                    new FileInputStream(tempFile.toFile()))) {
                Employee restored = (Employee) in.readObject();
                System.out.println("デシリアライズ後: " + restored);
                System.out.println("重要：参照先のAddressオブジェクトも復元された");
                System.out.println("重要：EmployeeもAddressもコンストラクタは呼ばれていない");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cleanup(tempFile);
        }

        System.out.println();
    }

    // ========================================
    // デモ4: 親クラスがSerializableでない場合
    // ========================================
    private static void demo4_NonSerializableParent() {
        System.out.println("【デモ4】親クラスがSerializableでない場合");
        System.out.println("-------------------------------------");

        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("dog", ".ser");

            // シリアライゼーション
            Dog dog = new Dog("柴犬", "ポチ");
            System.out.println("\nシリアライズ前: " + dog);

            try (ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream(tempFile.toFile()))) {
                out.writeObject(dog);
                System.out.println("シリアライズ完了");
            }

            // デシリアライゼーション
            System.out.println("\nデシリアライズ開始...");
            try (ObjectInputStream in = new ObjectInputStream(
                    new FileInputStream(tempFile.toFile()))) {
                Dog restored = (Dog) in.readObject();
                System.out.println("デシリアライズ後: " + restored);
                System.out.println("\n★超重要ポイント★");
                System.out.println("- 親クラスAnimalの引数なしコンストラクタが呼ばれた！");
                System.out.println("- そのため、speciesが\"Unknown\"になっている");
                System.out.println("- 親クラスのフィールドはシリアライズされない");
                System.out.println("- Dogクラスのコンストラクタは呼ばれていない");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cleanup(tempFile);
        }

        System.out.println();
    }

    // ========================================
    // デモ5: NotSerializableExceptionが発生するケース
    // ========================================
    private static void demo5_NotSerializableException() {
        System.out.println("【デモ5】NotSerializableExceptionが発生するケース");
        System.out.println("-------------------------------------");

        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("bad", ".ser");

            NonSerializableClass data = new NonSerializableClass("test");
            BadEmployee bad = new BadEmployee("田中", data);

            try (ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream(tempFile.toFile()))) {
                out.writeObject(bad);
                System.out.println("シリアライズ完了（ここには到達しない）");
            }

        } catch (NotSerializableException e) {
            System.out.println("★NotSerializableExceptionが発生！");
            System.out.println("原因: " + e.getMessage());
            System.out.println("説明: BadEmployeeのフィールドdataの型NonSerializableClassが");
            System.out.println("      Serializableを実装していないため");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cleanup(tempFile);
        }

        System.out.println();
    }

    // ========================================
    // デモ6: カスタムシリアライゼーション
    // ========================================
    private static void demo6_CustomSerialization() {
        System.out.println("【デモ6】カスタムシリアライゼーション");
        System.out.println("-------------------------------------");

        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("custom", ".ser");

            CustomPerson person = new CustomPerson("佐藤", "mypassword");
            System.out.println("シリアライズ前: " + person);

            // シリアライゼーション
            try (ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream(tempFile.toFile()))) {
                out.writeObject(person);
            }

            // デシリアライゼーション
            System.out.println("\nデシリアライズ開始...");
            try (ObjectInputStream in = new ObjectInputStream(
                    new FileInputStream(tempFile.toFile()))) {
                CustomPerson restored = (CustomPerson) in.readObject();
                System.out.println("デシリアライズ後: " + restored);
                System.out.println("\n重要：");
                System.out.println("- transientフィールドだが、カスタムメソッドで保存・復元された");
                System.out.println("- writeObject/readObjectはprivateメソッドとして定義");
                System.out.println("- リフレクションで自動的に呼び出される");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cleanup(tempFile);
        }

        System.out.println();
    }

    // ユーティリティ：一時ファイルの削除
    private static void cleanup(Path file) {
        if (file != null) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
