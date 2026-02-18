
## JDBC（Java Gold）

JDBCはJavaからデータベースにアクセスするためのAPIである。
試験ではAPIの使い方とその挙動が問われる。実際のDB操作より、APIの仕様理解が重要。

---

## 1. JDBCの全体像

```
1. DriverManager.getConnection() で接続
    ↓
2. Statement / PreparedStatement でSQL作成
    ↓
3. executeQuery()/executeUpdate() でSQL実行
    ↓
4. ResultSet で結果取得（SELECTの場合）
    ↓
5. close() でリソース解放（ResultSet → Statement → Connection）
```

```java
try (Connection conn = DriverManager.getConnection(url, user, password);
     PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE id = ?")) {
    ps.setInt(1, 100);
    try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            System.out.println(rs.getString("name"));
        }
    }
}
```

---

## 2. DriverManager.getConnection()

### URL形式

```
jdbc:subprotocol:subname
```

| DB | URL例 |
|----|-------|
| MySQL | `jdbc:mysql://localhost:3306/testdb` |
| PostgreSQL | `jdbc:postgresql://localhost/mydb` |
| Oracle | `jdbc:oracle:thin:@localhost:1521:orcl` |
| Derby | `jdbc:derby:memory:testdb;create=true` |

### オーバーロード

```java
// 1. URLのみ
Connection conn = DriverManager.getConnection(url);

// 2. URL + ユーザー名 + パスワード
Connection conn = DriverManager.getConnection(url, user, password);

// 3. URL + Properties
Properties props = new Properties();
props.setProperty("user", "dbuser");
props.setProperty("password", "dbpass");
Connection conn = DriverManager.getConnection(url, props);
```

**試験ポイント**: JDBC 4.0以降は `Class.forName()` によるドライバロード不要（自動ロード）。

---

## 3. Statement vs PreparedStatement vs CallableStatement

### 比較表

| | Statement | PreparedStatement | CallableStatement |
|---|-----------|-------------------|-------------------|
| 用途 | 単純なSQL | パラメータ付きSQL | ストアドプロシージャ |
| SQLインジェクション | 危険 | 安全 | 安全 |
| パラメータ | なし | `?` でバインド | `?` + OUT |
| プリコンパイル | されない | される | される |
| 推奨度 | △ | ◎ | ○ |

### 継承関係

```
Statement ← PreparedStatement ← CallableStatement
```

PreparedStatementはStatementを継承し、CallableStatementはPreparedStatementを継承している。

### CallableStatement の構文

```java
// プロシージャ呼び出し
CallableStatement cs = conn.prepareCall("{call getUserById(?)}");
cs.setInt(1, 100);

// OUTパラメータ
cs.registerOutParameter(1, Types.INTEGER);
cs.execute();
int result = cs.getInt(1);

// ファンクション呼び出し（戻り値あり）
CallableStatement cs = conn.prepareCall("{? = call calculateTotal(?, ?)}");
cs.registerOutParameter(1, Types.DOUBLE);
```

---

## 4. PreparedStatement のパラメータバインド

**超重要: インデックスは1から始まる！**（0じゃない）

```java
String sql = "INSERT INTO users (id, name, age) VALUES (?, ?, ?)";
PreparedStatement ps = conn.prepareStatement(sql);
ps.setInt(1, 1001);       // 1番目
ps.setString(2, "田中");   // 2番目
ps.setInt(3, 30);          // 3番目
int rows = ps.executeUpdate();
```

### 実行メソッドの違い

| メソッド | 用途 | 戻り値 |
|---------|------|--------|
| `executeQuery()` | SELECT | `ResultSet` |
| `executeUpdate()` | INSERT/UPDATE/DELETE | `int`（影響行数） |
| `execute()` | 汎用 | `boolean`（ResultSetがあればtrue） |

**引っかけ**: `execute()` が true を返すのはResultSetがある場合だけ。
UPDATE文で `execute()` を使うと `false` が返る。

---

## 5. ResultSet の操作

### 基本パターン

```java
while (rs.next()) {  // 次の行に移動
    int id = rs.getInt("id");      // カラム名で取得
    String name = rs.getString(2); // インデックスで取得（1から！）
}
```

### カーソル位置（超重要！）

初期位置は**先頭行の前**。`next()` を呼んで初めて1行目に移動する。

```
[beforeFirst] ← 初期位置
   ↓ next() → true
[1行目]
   ↓ next() → true
[2行目]
   ↓ next() → false
[afterLast]
```

`rs.getInt("id")` を `next()` なしで呼ぶと **SQLException** が発生する。

### NULL値の扱い

```java
int age = rs.getInt("age");
if (rs.wasNull()) {
    System.out.println("NULL!");
}
```

`getInt()` でNULL列を取得すると `0` が返る。本当にNULLか確認するには `wasNull()` を使う。

---

## 6. ResultSet のタイプとコンカレンシー

### デフォルト: TYPE_FORWARD_ONLY + CONCUR_READ_ONLY

```java
// タイプとコンカレンシーの指定
Statement stmt = conn.createStatement(
    ResultSet.TYPE_SCROLL_INSENSITIVE,
    ResultSet.CONCUR_READ_ONLY
);
```

### タイプ

| タイプ | スクロール | DB変更の反映 |
|-------|-----------|-------------|
| TYPE_FORWARD_ONLY | 前方向のみ | — |
| TYPE_SCROLL_INSENSITIVE | 双方向 | 反映しない |
| TYPE_SCROLL_SENSITIVE | 双方向 | 反映する |

TYPE_FORWARD_ONLY で `previous()`, `first()`, `last()` を呼ぶと **SQLException**！

### コンカレンシー

| コンカレンシー | 更新 |
|-------------|------|
| CONCUR_READ_ONLY | 読み取りのみ |
| CONCUR_UPDATABLE | ResultSetから直接DB更新可能 |

### スクロールメソッド

```java
next()                // 次の行（FORWARD_ONLYでも使える）
previous()            // 前の行
first() / last()      // 最初/最後の行
absolute(int row)     // 指定行（1から、負数は後ろから）
relative(int rows)    // 相対移動
beforeFirst()         // 先頭の前
afterLast()           // 最後の後
```

---

## 7. トランザクション管理

### auto-commit（デフォルト: true）

```java
conn.getAutoCommit();        // true（デフォルト）
conn.setAutoCommit(false);   // 明示的トランザクション開始
```

auto-commitがtrueだと、各SQL文の実行後に自動コミットされる。

### 明示的トランザクション

```java
conn.setAutoCommit(false);
try {
    stmt.executeUpdate("INSERT ...");
    stmt.executeUpdate("UPDATE ...");
    conn.commit();       // 成功→コミット
} catch (SQLException e) {
    conn.rollback();     // 失敗→ロールバック
}
```

### Savepoint

```java
Savepoint sp = conn.setSavepoint("sp1");
// ... 処理 ...
conn.rollback(sp);  // sp1までロールバック
conn.commit();       // sp1より前の変更はコミットされる
```

**重要**: `close()` 時に未コミットの変更はロールバックされる。

---

## 8. リソースのクローズ順序

### 順序: ResultSet → Statement → Connection（内側→外側）

```java
// ✅ try-with-resources（推奨）
try (Connection conn = DriverManager.getConnection(url);
     Statement stmt = conn.createStatement();
     ResultSet rs = stmt.executeQuery(sql)) {
    while (rs.next()) { ... }
}
// 自動: rs → stmt → conn の順でclose
```

### 注意点

- 同じStatementで新しいクエリを実行すると、**前のResultSetは自動クローズ**される
- PreparedStatementは再利用可能だが、ResultSetは毎回新しく取得する
- `close()` は `SQLException` をスローする可能性がある

---

## 試験ポイント・引っかけ問題

### Q1: インデックス

```java
ps.setInt(0, 100);  // 結果は？
```
**A**: SQLException。インデックスは**1から**。

### Q2: next() 忘れ

```java
ResultSet rs = stmt.executeQuery("SELECT * FROM users");
rs.getInt("id");  // 結果は？
```
**A**: SQLException。`next()` を呼ばないとカーソルは先頭行の前にいる。

### Q3: executeQuery() と executeUpdate()

```java
int rows = stmt.executeQuery("UPDATE users SET name = 'Alice'");
```
**A**: コンパイルエラー。`executeQuery()` は `ResultSet` を返す。

### Q4: FORWARD_ONLY で previous()

```java
Statement stmt = conn.createStatement();  // デフォルト
ResultSet rs = stmt.executeQuery("SELECT * FROM users");
rs.next();
rs.previous();  // 結果は？
```
**A**: SQLException。デフォルトはTYPE_FORWARD_ONLYなので`previous()`は使えない。

### Q5: 未コミットでclose()

```java
conn.setAutoCommit(false);
stmt.executeUpdate("INSERT INTO users VALUES (1, 'Alice')");
conn.close();  // INSERT はどうなる？
```
**A**: ロールバックされる。`commit()` を呼んでいないため。

### Q6: execute() の戻り値

```java
boolean result = stmt.execute("UPDATE users SET name = 'Alice'");
```
**A**: `false`。UPDATE文はResultSetを返さないため。

### Q7: 同じStatementの複数ResultSet

```java
ResultSet rs1 = stmt.executeQuery("SELECT * FROM users");
ResultSet rs2 = stmt.executeQuery("SELECT * FROM orders");
rs1.next();  // 結果は？
```
**A**: SQLException。rs2取得時にrs1は自動クローズされる。

---

## まとめ

| トピック | 覚えるべきこと |
|---------|---------------|
| URL形式 | `jdbc:subprotocol:subname` |
| Statement種類 | Statement < PreparedStatement < CallableStatement |
| パラメータ | インデックスは**1から** |
| 実行メソッド | executeQuery→ResultSet, executeUpdate→int, execute→boolean |
| ResultSet初期位置 | 先頭行の**前** |
| ResultSetデフォルト | TYPE_FORWARD_ONLY + CONCUR_READ_ONLY |
| auto-commit | デフォルト **true** |
| クローズ順序 | ResultSet → Statement → Connection |
| 未コミット+close | ロールバックされる |
