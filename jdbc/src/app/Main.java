package app;

import java.sql.*;

/**
 * Java Gold JDBC 学習
 * - DriverManager, Connection, Statement, PreparedStatement, CallableStatement
 * - ResultSet, トランザクション管理
 * ※ 実際のDBなしで動作するデモ（API使用パターンの説明）
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Java Gold JDBC 学習 ===\n");

        // 1. JDBC接続の基本
        demonstrateConnection();

        // 2. Statement の種類
        demonstrateStatementTypes();

        // 3. PreparedStatement のパラメータバインド
        demonstratePreparedStatement();

        // 4. ResultSet の操作
        demonstrateResultSet();

        // 5. ResultSet のタイプとコンカレンシー
        demonstrateResultSetTypes();

        // 6. トランザクション管理
        demonstrateTransaction();

        // 7. リソースのクローズ順序
        demonstrateResourceClose();

        // 8. SQLException
        demonstrateSQLException();
    }

    // === 1. JDBC接続の基本 ===
    static void demonstrateConnection() {
        System.out.println("--- 1. JDBC接続の基本 ---");

        // JDBC URL形式: jdbc:subprotocol:subname
        System.out.println("■ JDBC URL形式");
        System.out.println("  jdbc:mysql://localhost:3306/testdb");
        System.out.println("  jdbc:postgresql://localhost/mydb");
        System.out.println("  jdbc:oracle:thin:@localhost:1521:orcl");
        System.out.println("  jdbc:derby:memory:testdb;create=true");

        // getConnection のオーバーロード
        System.out.println("\n■ DriverManager.getConnection() のオーバーロード");
        System.out.println("  1. getConnection(String url)");
        System.out.println("  2. getConnection(String url, String user, String password)");
        System.out.println("  3. getConnection(String url, Properties info)");

        // 実際にSQLExceptionを発生させる
        System.out.println("\n■ 存在しないドライバでの接続試行");
        try {
            Connection conn = DriverManager.getConnection("jdbc:nonexistent://localhost/test");
        } catch (SQLException e) {
            System.out.println("  SQLException発生: " + e.getMessage());
        }

        // JDBC 4.0以降はドライバの自動ロード
        System.out.println("\n■ JDBC 4.0以降: Class.forName()は不要（自動ロード）");
        System.out.println();
    }

    // === 2. Statement の種類 ===
    static void demonstrateStatementTypes() {
        System.out.println("--- 2. Statement の種類 ---");

        System.out.println("■ Statement（基本）");
        System.out.println("  Statement stmt = conn.createStatement();");
        System.out.println("  ResultSet rs = stmt.executeQuery(\"SELECT * FROM users\");");
        System.out.println("  → SQLインジェクションの危険性あり！");

        System.out.println("\n■ PreparedStatement（推奨！）");
        System.out.println("  PreparedStatement ps = conn.prepareStatement(");
        System.out.println("      \"SELECT * FROM users WHERE id = ? AND name = ?\");");
        System.out.println("  ps.setInt(1, 100);");
        System.out.println("  ps.setString(2, \"太郎\");");
        System.out.println("  ResultSet rs = ps.executeQuery();");
        System.out.println("  → パラメータバインドで安全！プリコンパイル済み");

        System.out.println("\n■ CallableStatement（ストアドプロシージャ用）");
        System.out.println("  CallableStatement cs = conn.prepareCall(\"{call getUserById(?)}\");");
        System.out.println("  cs.setInt(1, 100);");
        System.out.println("  cs.execute();");
        System.out.println("  → OUTパラメータ: registerOutParameter() が必要");

        System.out.println("\n■ 継承関係");
        System.out.println("  Statement ← PreparedStatement ← CallableStatement");
        System.out.println();
    }

    // === 3. PreparedStatement のパラメータバインド ===
    static void demonstratePreparedStatement() {
        System.out.println("--- 3. PreparedStatement のパラメータバインド ---");

        System.out.println("■ パラメータの設定（インデックスは1から！）");
        System.out.println("  String sql = \"INSERT INTO users (id, name, age) VALUES (?, ?, ?)\";");
        System.out.println("  PreparedStatement ps = conn.prepareStatement(sql);");
        System.out.println("  ps.setInt(1, 1001);        // 1番目のパラメータ");
        System.out.println("  ps.setString(2, \"田中\");   // 2番目のパラメータ");
        System.out.println("  ps.setInt(3, 30);          // 3番目のパラメータ");

        System.out.println("\n■ 主な setXxx() メソッド");
        System.out.println("  setInt(int index, int value)");
        System.out.println("  setString(int index, String value)");
        System.out.println("  setDouble(int index, double value)");
        System.out.println("  setBoolean(int index, boolean value)");
        System.out.println("  setLong(int index, long value)");
        System.out.println("  setDate(int index, java.sql.Date value)");
        System.out.println("  setTimestamp(int index, Timestamp value)");
        System.out.println("  setNull(int index, int sqlType)");
        System.out.println("  setObject(int index, Object value)");

        System.out.println("\n■ 実行メソッドの違い");
        System.out.println("  executeQuery()  → SELECT用、ResultSetを返す");
        System.out.println("  executeUpdate() → INSERT/UPDATE/DELETE用、影響行数(int)を返す");
        System.out.println("  execute()       → 汎用、ResultSetがあればtrue");

        System.out.println("\n■ ⚠️ インデックスは1から！（0ではない）");
        System.out.println("  ps.setInt(0, 100); // → SQLException!");
        System.out.println();
    }

    // === 4. ResultSet の操作 ===
    static void demonstrateResultSet() {
        System.out.println("--- 4. ResultSet の操作 ---");

        System.out.println("■ 基本パターン");
        System.out.println("  ResultSet rs = stmt.executeQuery(\"SELECT id, name FROM users\");");
        System.out.println("  while (rs.next()) {");
        System.out.println("      int id = rs.getInt(\"id\");          // カラム名で取得");
        System.out.println("      String name = rs.getString(2);     // インデックスで取得（1から）");
        System.out.println("  }");

        System.out.println("\n■ カーソル位置");
        System.out.println("  [初期位置] (beforeFirst) ← ここからスタート");
        System.out.println("      ↓ rs.next() → true");
        System.out.println("  [1行目]");
        System.out.println("      ↓ rs.next() → true");
        System.out.println("  [2行目]");
        System.out.println("      ↓ rs.next() → false");
        System.out.println("  [最終位置] (afterLast)");

        System.out.println("\n■ 重要ポイント");
        System.out.println("  - 初期位置は先頭行の「前」");
        System.out.println("  - next() で初めて1行目に移動");
        System.out.println("  - カラムインデックスは1から（0ではない！）");
        System.out.println("  - wasNull() で直前のgetXxx()がNULLか確認");

        System.out.println("\n■ 主な getXxx() メソッド");
        System.out.println("  getInt(), getString(), getDouble(), getBoolean()");
        System.out.println("  getLong(), getDate(), getTimestamp(), getObject()");
        System.out.println("  ※ カラム名(String)またはインデックス(int)で指定可能");
        System.out.println();
    }

    // === 5. ResultSet のタイプとコンカレンシー ===
    static void demonstrateResultSetTypes() {
        System.out.println("--- 5. ResultSet のタイプとコンカレンシー ---");

        System.out.println("■ タイプ（スクロール方向）");
        System.out.println("  TYPE_FORWARD_ONLY      : 前方向のみ（デフォルト）、最速");
        System.out.println("  TYPE_SCROLL_INSENSITIVE: 双方向、DB変更を反映しない");
        System.out.println("  TYPE_SCROLL_SENSITIVE  : 双方向、DB変更を反映する");

        System.out.println("\n■ コンカレンシー（更新可否）");
        System.out.println("  CONCUR_READ_ONLY  : 読み取り専用（デフォルト）");
        System.out.println("  CONCUR_UPDATABLE  : 更新可能（updateRow()等が使える）");

        // 定数値の確認
        System.out.println("\n■ 定数値の確認");
        System.out.println("  TYPE_FORWARD_ONLY       = " + ResultSet.TYPE_FORWARD_ONLY);
        System.out.println("  TYPE_SCROLL_INSENSITIVE = " + ResultSet.TYPE_SCROLL_INSENSITIVE);
        System.out.println("  TYPE_SCROLL_SENSITIVE   = " + ResultSet.TYPE_SCROLL_SENSITIVE);
        System.out.println("  CONCUR_READ_ONLY        = " + ResultSet.CONCUR_READ_ONLY);
        System.out.println("  CONCUR_UPDATABLE        = " + ResultSet.CONCUR_UPDATABLE);

        System.out.println("\n■ 作成方法");
        System.out.println("  Statement stmt = conn.createStatement(");
        System.out.println("      ResultSet.TYPE_SCROLL_INSENSITIVE,");
        System.out.println("      ResultSet.CONCUR_READ_ONLY);");

        System.out.println("\n■ スクロールメソッド（SCROLL系のみ）");
        System.out.println("  previous(), first(), last()");
        System.out.println("  absolute(int row), relative(int rows)");
        System.out.println("  beforeFirst(), afterLast()");
        System.out.println("  → TYPE_FORWARD_ONLYでは使えない！SQLException");

        System.out.println("\n■ 更新メソッド（CONCUR_UPDATABLEのみ）");
        System.out.println("  updateXxx() + updateRow() でDBに反映");
        System.out.println("  deleteRow() で行削除");
        System.out.println("  moveToInsertRow() + insertRow() で行挿入");
        System.out.println();
    }

    // === 6. トランザクション管理 ===
    static void demonstrateTransaction() {
        System.out.println("--- 6. トランザクション管理 ---");

        System.out.println("■ auto-commit（デフォルト: true）");
        System.out.println("  conn.getAutoCommit()       // true（各SQL後に自動コミット）");
        System.out.println("  conn.setAutoCommit(false)  // 明示的トランザクション制御開始");

        System.out.println("\n■ 明示的トランザクション");
        System.out.println("  conn.setAutoCommit(false);");
        System.out.println("  try {");
        System.out.println("      stmt.executeUpdate(\"INSERT ...\");");
        System.out.println("      stmt.executeUpdate(\"UPDATE ...\");");
        System.out.println("      conn.commit();           // 全て成功→コミット");
        System.out.println("  } catch (SQLException e) {");
        System.out.println("      conn.rollback();         // 失敗→ロールバック");
        System.out.println("  }");

        System.out.println("\n■ Savepoint（部分的ロールバック）");
        System.out.println("  Savepoint sp = conn.setSavepoint(\"sp1\");");
        System.out.println("  // ... 処理 ...");
        System.out.println("  conn.rollback(sp);  // sp1地点までロールバック");
        System.out.println("  conn.commit();       // sp1以前の変更はコミットされる");

        System.out.println("\n■ 重要ポイント");
        System.out.println("  - close()時に未コミット → ロールバックされる");
        System.out.println("  - DDL文(CREATE等)は多くのDBで自動コミット");
        System.out.println("  - auto-commitがtrueの時にcommit()を呼んでもエラーにはならない");
        System.out.println();
    }

    // === 7. リソースのクローズ順序 ===
    static void demonstrateResourceClose() {
        System.out.println("--- 7. リソースのクローズ順序 ---");

        System.out.println("■ クローズ順序（内側から外側へ）");
        System.out.println("  ResultSet → Statement → Connection");

        System.out.println("\n■ try-with-resources（推奨！）");
        System.out.println("  try (Connection conn = DriverManager.getConnection(url);");
        System.out.println("       Statement stmt = conn.createStatement();");
        System.out.println("       ResultSet rs = stmt.executeQuery(sql)) {");
        System.out.println("      while (rs.next()) { ... }");
        System.out.println("  }");
        System.out.println("  // 自動: rs.close() → stmt.close() → conn.close()");

        System.out.println("\n■ 同じStatementで別クエリを実行すると…");
        System.out.println("  ResultSet rs1 = stmt.executeQuery(\"SELECT * FROM users\");");
        System.out.println("  ResultSet rs2 = stmt.executeQuery(\"SELECT * FROM orders\");");
        System.out.println("  rs1.next(); // SQLException! rs1は自動クローズ済み");
        System.out.println("  → 同じStatementから新しいResultSetを取得すると前のは閉じられる");

        System.out.println("\n■ PreparedStatement の再利用");
        System.out.println("  PreparedStatement は再利用可能");
        System.out.println("  ただし ResultSet は毎回新しく取得する");
        System.out.println();
    }

    // === 8. SQLException ===
    static void demonstrateSQLException() {
        System.out.println("--- 8. SQLException ---");

        System.out.println("■ SQLExceptionは検査例外（try-catchまたはthrows必須）");

        // 実際にSQLExceptionを発生させて詳細を確認
        try {
            DriverManager.getConnection("jdbc:invalid://localhost/test");
        } catch (SQLException e) {
            System.out.println("\n■ SQLExceptionの情報");
            System.out.println("  getMessage(): " + e.getMessage());
            System.out.println("  getSQLState(): " + e.getSQLState());
            System.out.println("  getErrorCode(): " + e.getErrorCode());

            // 例外チェーン
            SQLException next = e.getNextException();
            System.out.println("  getNextException(): " + next);
        }

        System.out.println("\n■ 主なSQLStateコード");
        System.out.println("  08xxx: 接続エラー");
        System.out.println("  22xxx: データ例外");
        System.out.println("  23xxx: 整合性制約違反");
        System.out.println("  42xxx: 構文エラー");

        System.out.println("\n=== JDBC学習完了 ===");
    }
}
