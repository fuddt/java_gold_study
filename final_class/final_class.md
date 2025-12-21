# final class
finalクラスについては継承できないっていうのと、
finalでもフィールド変数に関しては変更可能っていうのをわかっていればいいんじゃないかなと思った。

他になにか問題として出そうなことあるかな・・・

まず、ディレクトリ構成です。
```plaintext
final_class
└── src
    └── app
        ├── Main.java
        └── module-info.java

2 directories, 2 files
```

## ./final_class/src/app/Main.java
```java
package app;
// finalクラスは継承できないことを示す例

class Main {
    public static void main(String[] args) {
        
    }
}

final class ThisIsFinal {
    private String message;

    ThisIsFinal(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMesage(String message) {
        this.message = message;
    }
}

class SubClass extends ThisIsFinal {
    SubClass(String message) {
        super(message);
    }
}

```

コンパイルしようとすると

```
javac --module-source-path src -d out $(find src -name "*.java")

src/app/Main.java:26: エラー: final ThisIsFinalからは継承できません
class SubClass extends ThisIsFinal {
                       ^
エラー1個
```

って感じです。


フィールドは変更可能

```
package app;
// finalクラスは継承できないことを示す例

class Main {
    public static void main(String[] args) {
        final ThisIsFinal finalInstance = new ThisIsFinal("Hello, Final Class!");
        //　フィールドは変更できる
        System.out.println(finalInstance.getMessage());
        finalInstance.setMesage("Final Class Modified Message");
        System.out.println(finalInstance.getMessage());
    }
}

final class ThisIsFinal {
    private String message;

    ThisIsFinal(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMesage(String message) {
        this.message = message;
    }
}


```

```
java --module-path out -m app/app.Main

Hello, Final Class!
Final Class Modified Message
```

# 以下は可能かどうか

```
final abstract class A {
}
```
→abstractが継承を前提としているので、finalとは矛盾するため不可

```
src/app/Main.java:14: エラー: 修飾子abstractとfinalの組合せは不正です
```

というエラーになる。


