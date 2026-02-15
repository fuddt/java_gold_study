# Java Gold 例外処理 完全攻略

## 1. try-with-resources の仕組み

### 基本ルール
```java
try (MyResource r = new MyResource()) {
    r.use();
} // 自動的にr.close()が呼ばれる
```

- `AutoCloseable`または`Closeable`を実装したクラスが使えるよ
- `try`ブロックを抜ける時に自動で`close()`が呼ばれる
- 例外が起きてもちゃんと`close()`される

### AutoCloseableの実装
```java
class MyResource implements AutoCloseable {
    @Override
    public void close() throws Exception {
        System.out.println("リソースをクローズ");
    }
}
```

**ポイント**:
- `Closeable`は`IOException`のみスロー可能
- `AutoCloseable`は`Exception`をスローできる（より汎用的）
- Java 7以降で使える機能だよね

---

## 2. 複数リソースのclose順序

### 重要な仕様
```java
try (
    Resource1 r1 = new Resource1();  // 1番目に開く
    Resource2 r2 = new Resource2();  // 2番目に開く
    Resource3 r3 = new Resource3()   // 3番目に開く
) {
    // 使用
} // close順序: r3 → r2 → r1（逆順！）
```

**試験頻出**: 宣言と逆順でcloseされる！

### なぜ逆順？
後に開いたリソースが先に開いたリソースに依存してる可能性があるから。
例: ファイル → バッファリーダーの順なら、リーダーを先にcloseしないとダメじゃね？

---

## 3. 抑制された例外（Suppressed Exceptions）

### 何が起きるか
```java
try (ProblematicResource r = new ProblematicResource()) {
    throw new Exception("メイン例外");  // ①
} // close()でも例外発生 ②
```

- ①のメイン例外がスローされる
- ②のclose例外は「抑制された例外」として記録される
- `getSuppressed()`で取得可能

### 実装例
```java
try (Resource r = new Resource()) {
    r.doSomething(); // 例外1
} catch (Exception e) {
    System.out.println("メイン: " + e.getMessage());

    for (Throwable t : e.getSuppressed()) {
        System.out.println("抑制: " + t.getMessage());
    }
}
```

**試験ポイント**:
- try-with-resources特有の機能
- 通常のtry-finallyでは抑制されず、finally例外が優先される

---

## 4. カスタム例外の作り方

### Checked例外（Exceptionを継承）
```java
class MyCheckedException extends Exception {
    public MyCheckedException(String message) {
        super(message);
    }
}

// 使い方
void method() throws MyCheckedException {  // throwsが必須
    throw new MyCheckedException("エラー");
}
```

### Unchecked例外（RuntimeExceptionを継承）
```java
class MyUncheckedException extends RuntimeException {
    public MyUncheckedException(String message) {
        super(message);
    }
}

// 使い方
void method() {  // throwsは不要（書いてもOK）
    throw new MyUncheckedException("エラー");
}
```

**どっちを使う？**:
- **Checked**: 呼び出し側で対処が必須の例外（ファイル未存在等）
- **Unchecked**: プログラマのミスによる例外（null参照、不正引数等）

---

## 5. マルチキャッチの制約

### 基本構文
```java
try {
    // 何か処理
} catch (IOException | SQLException e) {  // |で複数指定
    e.printStackTrace();
}
```

### 重要な制約

#### ❌ 制約1: 親子関係のある例外は一緒にできない
```java
// コンパイルエラー！
catch (Exception | IOException e) { }
// IOException は Exception の子クラスなのでNG
```

#### ❌ 制約2: 例外変数は暗黙的にfinal
```java
catch (IOException | SQLException e) {
    e = new IOException();  // コンパイルエラー！
    // eは暗黙的にfinalなので再代入不可
}
```

#### ✅ 正しい書き方
```java
catch (IOException | SQLException e) {
    // eの型は両方の共通親クラス（この場合Exception）
    System.out.println(e.getMessage());  // OK
}
```

**試験頻出**: 親子関係チェックと、finalであることを問う問題！

---

## 6. Effectively final in catch

### catchした例外変数の性質
```java
try {
    throw new IOException();
} catch (IOException e) {
    // eはeffectively final（実質的にfinal）

    Runnable r = () -> {
        System.out.println(e);  // ラムダから参照可能
    };

    // e = new IOException();  // 再代入不可
}
```

**ポイント**:
- catch変数は再代入できない（effectively final）
- だからラムダ式や無名クラスから参照できるんだよね

---

## 7. assert文の使い方

### 基本構文
```java
assert 条件式;
assert 条件式 : エラーメッセージ;
```

### 使用例
```java
assert age >= 0 : "年齢は0以上";
assert list != null : "リストがnull";
```

### 有効化方法
```bash
# デフォルトでは無効
java Main

# -eaフラグで有効化
java -ea Main
java -enableassertions Main

# 無効化（デフォルト）
java -da Main
java -disableassertions Main
```

### assertが失敗すると
```java
assert false : "失敗メッセージ";
// → AssertionError がスローされる
```

**重要**:
- `AssertionError`は`Error`の子クラス（`Exception`じゃない）
- 本番環境では無効化すべき（デバッグ用）
- `if`文の代わりに使っちゃダメ（無効化される可能性があるから）

**試験ポイント**:
- `-ea`なしでは無効化されている
- `AssertionError`は`Exception`ではなく`Error`

---

## 8. Checked vs Unchecked 例外

### 例外の階層
```
Throwable
├─ Error (システムレベルの深刻なエラー)
│   └─ OutOfMemoryError, StackOverflowError等
└─ Exception
    ├─ Checked例外 (RuntimeException以外)
    │   └─ IOException, SQLException等
    └─ RuntimeException (Unchecked例外)
        └─ NullPointerException, IllegalArgumentException等
```

### Checked例外
- コンパイル時にcatchまたはthrowsが必須
- 回復可能なエラーを想定
- 例: `IOException`, `SQLException`, `ClassNotFoundException`

```java
// throwsが必須
void method() throws IOException {
    throw new IOException();
}

// または catch が必須
void method() {
    try {
        throw new IOException();
    } catch (IOException e) {
        // 処理
    }
}
```

### Unchecked例外
- catchやthrowsは任意
- プログラミングミスを想定
- 例: `NullPointerException`, `ArrayIndexOutOfBoundsException`, `IllegalArgumentException`

```java
// throwsなしでもOK
void method() {
    throw new IllegalArgumentException();
}
```

**試験頻出**: どの例外がCheckedでどれがUncheckedか！

---

## 9. finally と return の組み合わせの罠

### 罠1: finallyのreturnはtryのreturnを上書き
```java
int method() {
    try {
        return 1;  // これは無視される！
    } finally {
        return 2;  // これが返される
    }
}
// 結果: 2
```

**超重要**: finallyでreturnすると、tryのreturnは完全に無視される！

### 罠2: finallyで変数変更しても影響なし（プリミティブ）
```java
int method() {
    int x = 1;
    try {
        return x;  // ここでxの値（1）が評価される
    } finally {
        x = 100;  // return値には影響しない
    }
}
// 結果: 1
```

### 罠3: finallyで例外が起きると元の例外は失われる
```java
void method() throws Exception {
    try {
        throw new Exception("try例外");
    } finally {
        throw new Exception("finally例外");  // こっちが優先
    }
}
// 結果: "finally例外" がスローされ、"try例外" は失われる
```

**違い**: try-with-resourcesなら元の例外が優先され、closeの例外は抑制される！

---

## 10. 試験頻出ポイント・引っかけ問題

### ❌ よくある間違い 1: 親子関係のマルチキャッチ
```java
// コンパイルエラー！
catch (Exception | IOException e) { }
```

### ❌ よくある間違い 2: マルチキャッチで再代入
```java
catch (IOException | SQLException e) {
    e = new IOException();  // コンパイルエラー！
}
```

### ❌ よくある間違い 3: try-with-resourcesの順序
```java
try (R1 r1 = new R1(); R2 r2 = new R2()) {
}
// close順序: r2 → r1 （逆順！）
```

### ❌ よくある間違い 4: finallyで例外
```java
try {
    throw new Exception("A");
} finally {
    throw new Exception("B");  // Bが優先、Aは失われる
}
```

### ❌ よくある間違い 5: assertはデフォルト無効
```java
assert false;
// -eaなしでは何も起きない！
```

---

## 11. コード例で理解を深める

### 例1: 複雑なtry-with-resources
```java
try (
    FileInputStream fis = new FileInputStream("file.txt");
    BufferedInputStream bis = new BufferedInputStream(fis)
) {
    // 処理
}
// close順序: bis → fis
```

### 例2: 抑制された例外の確認
```java
class BadResource implements AutoCloseable {
    public void doWork() throws Exception {
        throw new Exception("作業中エラー");
    }

    @Override
    public void close() throws Exception {
        throw new Exception("クローズエラー");
    }
}

try (BadResource r = new BadResource()) {
    r.doWork();
} catch (Exception e) {
    System.out.println(e.getMessage());  // "作業中エラー"
    System.out.println(e.getSuppressed()[0].getMessage());  // "クローズエラー"
}
```

### 例3: catchの順序
```java
try {
    // 処理
} catch (FileNotFoundException e) {  // より具体的な例外を先に
    // 処理
} catch (IOException e) {  // 親クラスは後
    // 処理
} catch (Exception e) {  // 最も汎用的な例外は最後
    // 処理
}
```

**ルール**: 子クラスを先に、親クラスを後に書かないとコンパイルエラー！

---

## 12. まとめ：試験直前チェックリスト

- [ ] try-with-resourcesはAutoCloseableが必要
- [ ] 複数リソースは逆順でclose
- [ ] 抑制された例外はgetSuppressed()で取得
- [ ] マルチキャッチは親子関係NG、変数はfinal
- [ ] assertは-eaで有効化、失敗するとAssertionError
- [ ] Checked例外はthrows必須、Uncheckedは任意
- [ ] finallyのreturnはtryのreturnを上書き
- [ ] finallyで例外が起きると元の例外は失われる
- [ ] RuntimeExceptionとErrorはUnchecked
- [ ] catchは子クラスから先に書く

---

## 13. 練習問題

### 問題1
```java
try (R1 r1 = new R1(); R2 r2 = new R2(); R3 r3 = new R3()) {
    System.out.println("処理");
}
```
close()の呼び出し順序は？

<details>
<summary>答え</summary>
r3.close() → r2.close() → r1.close()（逆順）
</details>

### 問題2
```java
catch (IOException | FileNotFoundException e) { }
```
このコードはコンパイルできる？

<details>
<summary>答え</summary>
NO。FileNotFoundExceptionはIOExceptionの子クラスなので親子関係のある例外は一緒にできない。
</details>

### 問題3
```java
int test() {
    try {
        return 1;
    } finally {
        return 2;
    }
}
```
このメソッドの戻り値は？

<details>
<summary>答え</summary>
2。finallyのreturnがtryのreturnを上書きする。
</details>

### 問題4
```java
assert x > 0;
```
このコードで`x = -1`の時、何が起きる？（-eaなし）

<details>
<summary>答え</summary>
何も起きない。assertはデフォルトで無効化されている。
</details>

### 問題5
```java
try (Resource r = new Resource()) {
    throw new Exception("A");
}
// Resource.close()でException("B")がスローされる
```
最終的にスローされる例外は？

<details>
<summary>答え</summary>
Exception("A")がメイン例外としてスローされ、Exception("B")はgetSuppressed()で取得できる抑制された例外になる。
</details>

---

これでJava Gold例外処理は完璧じゃね？頑張って！
