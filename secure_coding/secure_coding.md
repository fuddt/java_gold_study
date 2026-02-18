
## セキュアコーディング（Java Gold）

Java Goldの試験では「セキュアコーディング」は独立した出題分野として出てくるよ。
コードを書くときに「どうすればセキュリティリスクを減らせるか」を問われる。

---

## 1. DoS（Denial of Service）防止

DoS攻撃ってのは、サーバーのリソースを枯渇させてサービスを止める攻撃のこと。

### 入力サイズの制限

```java
// ❌ 悪い例: サイズ制限なし
byte[] data = inputStream.readAllBytes(); // 巨大データでOutOfMemoryError

// ✅ 良い例: サイズを制限する
int MAX_SIZE = 1024 * 1024; // 1MB
byte[] buffer = new byte[MAX_SIZE];
int bytesRead = inputStream.read(buffer, 0, MAX_SIZE);
```

### コレクションのサイズ制限

```java
// ❌ 悪い例
List<String> list = new ArrayList<>();
while (hasMore()) {
    list.add(next()); // 無限に追加される可能性
}

// ✅ 良い例
int MAX_LIST_SIZE = 10000;
while (hasMore() && list.size() < MAX_LIST_SIZE) {
    list.add(next());
}
```

### リソースの確実な解放

```java
// ✅ try-with-resourcesで確実にcloseする
try (var conn = DriverManager.getConnection(url);
     var stmt = conn.prepareStatement(sql)) {
    // ...
} // 自動的にclose
```

**ポイント**: 入力は常に「悪意がある」と想定して制限を設けるべきである。

---

## 2. 入力検証

外部からの入力は**絶対に信頼しちゃダメ**。

### バリデーションのパターン

```java
// ✅ ホワイトリスト方式（許可するパターンを定義）
boolean isValid = input.matches("[a-zA-Z0-9]{3,20}");

// ✅ 範囲チェック
if (age < 0 || age > 150) {
    throw new IllegalArgumentException("不正な年齢");
}

// ✅ null チェック
Objects.requireNonNull(value, "値はnullにできません");
```

### バリデーションの原則

| 原則 | 説明 |
|------|------|
| ホワイトリスト方式 | 許可するパターンを定義（ブラックリストより安全） |
| 早期失敗 | 不正な入力は処理の最初で拒否 |
| 境界値チェック | 数値の範囲、文字列の長さを検証 |
| 型チェック | 期待する型かどうかを確認 |

---

## 3. 機密情報の保護

### パスワードにchar[]を使う理由

これは試験でも超頻出！

```java
// ❌ Stringでパスワード管理
String password = "secret";
// → String Poolに入る可能性がある
// → GCされるまでメモリに残り続ける
// → ヒープダンプで読み取られる危険性

// ✅ char[]でパスワード管理
char[] password = {'s', 'e', 'c', 'r', 'e', 't'};
// 使用後に明示的にゼロ埋め
Arrays.fill(password, '\0');
```

**なぜchar[]が安全なのか？**

| | String | char[] |
|---|--------|--------|
| 内容の変更 | 不可（不変） | 可能（ゼロ埋めできる） |
| String Pool | 残る可能性あり | 関係なし |
| メモリ滞在 | GCまで残る | ゼロ埋めで即消去 |
| ログ出力 | そのまま表示 | 参照が表示される |

### toString()で機密情報を隠す

```java
class User {
    private String name;
    private String password;

    @Override
    public String toString() {
        // パスワードは表示しない！
        return "User{name='" + name + "', password='****'}";
    }
}
```

---

## 4. 不変オブジェクト（Immutable Objects）

不変オブジェクトはスレッドセーフであり、外部から状態を改ざんされる心配がない。

### 不変クラスの作り方

1. クラスを`final`にする（継承不可）
2. フィールドを`private final`にする
3. setterを提供しない
4. ミュータブルなフィールドは**防御的コピー**を使う

```java
final class ImmutablePerson {
    private final String name;
    private final int age;
    private final Date birthDate; // Dateはミュータブル！

    ImmutablePerson(String name, int age, Date birthDate) {
        this.name = name;
        this.age = age;
        // ✅ 防御的コピー（入力時）
        this.birthDate = new Date(birthDate.getTime());
    }

    public String getName() { return name; }
    public int getAge() { return age; }

    public Date getBirthDate() {
        // ✅ 防御的コピー（出力時）
        return new Date(birthDate.getTime());
    }
}
```

**注意**: `String`や`Integer`はもともと不変だから防御的コピーは不要。
`Date`、`List`、`配列`などミュータブルなものだけコピーすればOK。

---

## 5. 防御的コピー（Defensive Copy）

### コピーオンインプット / コピーオンアウトプット

```java
class SafeContainer {
    private final List<String> items;

    SafeContainer(List<String> items) {
        // ✅ コピーオンインプット
        this.items = new ArrayList<>(items);
    }

    public List<String> getItems() {
        // ✅ コピーオンアウトプット
        return new ArrayList<>(items);
        // または Collections.unmodifiableList(items);
    }
}
```

### Collections.unmodifiableList()

```java
List<String> original = new ArrayList<>(Arrays.asList("A", "B"));
List<String> unmodifiable = Collections.unmodifiableList(original);

unmodifiable.add("C"); // UnsupportedOperationException!

// ⚠️ でも注意！originalを変更するとunmodifiableも変わる！
original.add("C");
System.out.println(unmodifiable); // [A, B, C] ← 変わっちゃう！

// ✅ Java 10以降は List.copyOf() を使うと完全に独立したコピーになる
List<String> safeCopy = List.copyOf(original);
```

---

## 6. シリアライゼーションの安全対策

### transient で機密フィールドを除外

```java
class User implements Serializable {
    String name;
    transient String password; // ✅ シリアライズされない
}
```

### readObject でバリデーション

```java
private void readObject(ObjectInputStream ois)
        throws IOException, ClassNotFoundException {
    ois.defaultReadObject();

    // ✅ デシリアライズ後にバリデーション
    if (name == null || name.isEmpty()) {
        throw new InvalidObjectException("不正なデータ");
    }
    if (age < 0 || age > 150) {
        throw new InvalidObjectException("不正な年齢");
    }
}
```

### シリアライゼーションの注意点

- `serialVersionUID`を明示的に宣言する
- 信頼できないソースからのデシリアライズは避ける
- `transient`で機密フィールドを保護する
- `readObject`でバリデーションを行う

---

## 7. 最小権限の原則

### アクセス修飾子

```
private → package-private(デフォルト) → protected → public
```

**原則**: 可能な限り狭いアクセス修飾子を使うべき。

```java
// ✅ フィールドはprivate
private int count;

// ✅ 内部ヘルパーメソッドはprivate
private void validate() { ... }

// ✅ 外部に公開が必要なものだけpublic
public String getName() { return name; }
```

### final の活用

```java
// ✅ 継承されたくないクラス
final class SecurityUtil { ... }

// ✅ オーバーライドされたくないメソッド
public final void validate() { ... }

// ✅ 再代入されたくない変数
final int MAX_SIZE = 100;
```

---

## 8. インジェクション攻撃の防止

### SQLインジェクション

```java
// ❌ 文字列連結（超危険！）
String sql = "SELECT * FROM users WHERE name = '" + userInput + "'";
// userInput が "'; DROP TABLE users; --" だったら…

// ✅ PreparedStatement（パラメータは自動エスケープ）
String sql = "SELECT * FROM users WHERE name = ?";
PreparedStatement ps = conn.prepareStatement(sql);
ps.setString(1, userInput);
```

### コマンドインジェクション

```java
// ❌ 文字列結合でコマンド実行
Runtime.getRuntime().exec("ls " + userInput);
// userInput が "; rm -rf /" だったら…

// ✅ 文字列配列でコマンド実行
Runtime.getRuntime().exec(new String[]{"ls", userInput});
// 引数が1つの文字列として扱われる
```

---

## 試験ポイント・引っかけ問題

### Q1: パスワード管理
```java
// どちらが安全？
String pw1 = "secret";
char[] pw2 = {'s', 'e', 'c', 'r', 'e', 't'};
```
**A**: `char[]`。使用後にゼロ埋めでメモリから消去できるから。

### Q2: 防御的コピー
```java
final class Config {
    private final Date created;

    Config(Date created) {
        this.created = created; // これは安全？
    }

    public Date getCreated() {
        return created; // これは安全？
    }
}
```
**A**: どちらも不安全。コンストラクタとゲッターの両方で`new Date(created.getTime())`と防御的コピーすべき。

### Q3: unmodifiableList の罠
```java
List<String> orig = new ArrayList<>(List.of("A"));
List<String> view = Collections.unmodifiableList(orig);
orig.add("B");
System.out.println(view.size()); // いくつ？
```
**A**: 2。`unmodifiableList`はビューなので、元のリストの変更が反映される。

### Q4: transient の効果
```java
class User implements Serializable {
    String name;
    transient String password = "default";
}
```
シリアライズ→デシリアライズ後、passwordの値は？

**A**: `null`。transientフィールドはデシリアライズ時にフィールド初期化子が実行されない（コンストラクタが呼ばれないため）。

### Q5: 不変クラスの条件
以下のクラスは不変か？
```java
class Person {
    private final String name;
    Person(String name) { this.name = name; }
    public String getName() { return name; }
}
```
**A**: 不完全。クラスが`final`でないため、サブクラスでミュータブルにされる可能性がある。

---

## まとめ

| 対策 | キーポイント |
|------|------------|
| DoS防止 | 入力サイズ制限、リソース解放、タイムアウト |
| 入力検証 | ホワイトリスト方式、早期失敗 |
| 機密情報保護 | char[]使用、toString()隠蔽 |
| 不変オブジェクト | final class, private final, 防御的コピー |
| 防御的コピー | コピーオンインプット & コピーオンアウトプット |
| シリアライズ安全 | transient, readObjectバリデーション |
| 最小権限 | 最も狭いアクセス修飾子を使う |
| インジェクション防止 | PreparedStatement、文字列配列 |
