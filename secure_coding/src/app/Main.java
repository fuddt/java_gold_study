package app;

import java.io.*;
import java.util.*;

/**
 * Java Gold セキュアコーディング学習
 * - DoS防止
 * - 入力検証
 * - 機密情報保護
 * - 不変オブジェクト
 * - 防御的コピー
 * - シリアライゼーションの安全対策
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Java Gold セキュアコーディング ===\n");

        // 1. DoS防止
        demonstrateDosPrevention();

        // 2. 入力検証
        demonstrateInputValidation();

        // 3. 機密情報保護（char[] vs String）
        demonstrateSensitiveDataProtection();

        // 4. 不変オブジェクトによるセキュリティ
        demonstrateImmutableObjects();

        // 5. 防御的コピー（Defensive Copy）
        demonstrateDefensiveCopy();

        // 6. シリアライゼーションの安全対策
        demonstrateSerializationSecurity();

        // 7. 最小権限の原則
        demonstrateLeastPrivilege();

        // 8. インジェクション攻撃の防止
        demonstrateInjectionPrevention();
    }

    // === 1. DoS防止 ===
    static void demonstrateDosPrevention() {
        System.out.println("--- 1. DoS防止 ---");

        // ❌ 悪い例: 入力サイズを制限しない
        // String input = readAll(request); // 巨大データでメモリ枯渇の恐れ

        // ✅ 良い例: 入力サイズに制限を設ける
        int MAX_INPUT_SIZE = 1024 * 1024; // 1MB
        System.out.println("入力サイズ制限: " + MAX_INPUT_SIZE + " bytes");

        // ✅ 良い例: コレクションのサイズを制限する
        int MAX_LIST_SIZE = 10000;
        List<String> limitedList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            if (limitedList.size() >= MAX_LIST_SIZE) {
                System.out.println("リスト上限に達しました");
                break;
            }
            limitedList.add("item" + i);
        }
        System.out.println("リストサイズ: " + limitedList.size());

        // ✅ 良い例: リソースを確実に解放する（try-with-resources）
        System.out.println("リソース解放にはtry-with-resourcesを使用すべき");

        // ✅ 良い例: タイムアウトを設定する
        System.out.println("ネットワーク操作にはタイムアウトを設定すべき\n");
    }

    // === 2. 入力検証 ===
    static void demonstrateInputValidation() {
        System.out.println("--- 2. 入力検証 ---");

        // ✅ 良い例: 入力値を検証する
        String userInput = "John123";
        if (isValidUsername(userInput)) {
            System.out.println("有効なユーザー名: " + userInput);
        }

        String badInput = "<script>alert('xss')</script>";
        if (!isValidUsername(badInput)) {
            System.out.println("無効なユーザー名を拒否: " + badInput);
        }

        // ✅ 良い例: 数値範囲を検証する
        int age = 25;
        if (age >= 0 && age <= 150) {
            System.out.println("有効な年齢: " + age);
        }

        // ✅ 良い例: null チェック
        String value = Objects.requireNonNull("有効な値", "値はnullにできません");
        System.out.println("null チェック済み: " + value + "\n");
    }

    static boolean isValidUsername(String username) {
        // 英数字のみ、3〜20文字
        return username != null && username.matches("[a-zA-Z0-9]{3,20}");
    }

    // === 3. 機密情報保護 ===
    static void demonstrateSensitiveDataProtection() {
        System.out.println("--- 3. 機密情報保護 ---");

        // ❌ 悪い例: パスワードをStringで扱う
        // String password = "secret123";
        // → StringはString Poolに残り、GCされるまでメモリに残り続ける
        // → ヒープダンプから読み取られる危険性がある
        System.out.println("❌ StringでパスワードNG → String Poolに残り続ける");

        // ✅ 良い例: パスワードをchar[]で扱う
        char[] password = {'s', 'e', 'c', 'r', 'e', 't'};
        System.out.println("✅ char[]でパスワード管理 → 使用後にゼロ埋めできる");

        // 使用後にゼロ埋め
        Arrays.fill(password, '\0');
        System.out.println("ゼロ埋め後: " + Arrays.toString(password));

        // ❌ 悪い例: ログに機密情報を出力する
        // logger.info("Password: " + password);
        System.out.println("❌ ログに機密情報を出力してはいけない");

        // ✅ 良い例: toString()で機密情報を隠す
        SecureUser user = new SecureUser("太郎", "secret");
        System.out.println("✅ toString()で隠蔽: " + user + "\n");
    }

    // === 4. 不変オブジェクトによるセキュリティ ===
    static void demonstrateImmutableObjects() {
        System.out.println("--- 4. 不変オブジェクト ---");

        // 不変オブジェクトの条件:
        // 1. クラスをfinalにする（継承不可）
        // 2. フィールドをprivate finalにする
        // 3. setterを提供しない
        // 4. ミュータブルなフィールドは防御的コピーを返す

        ImmutablePerson person = new ImmutablePerson("太郎", 30,
                new Date());
        System.out.println("不変オブジェクト: " + person.getName() + ", " + person.getAge());

        // 外部からDateを変更しても影響しない（防御的コピーされている）
        Date birthDate = person.getBirthDate();
        long originalTime = birthDate.getTime();
        birthDate.setTime(0); // 外部で変更を試みる
        System.out.println("外部変更後も元のまま: " +
                (person.getBirthDate().getTime() == originalTime));
        System.out.println();
    }

    // === 5. 防御的コピー ===
    static void demonstrateDefensiveCopy() {
        System.out.println("--- 5. 防御的コピー ---");

        // ❌ 悪い例: ミュータブルなオブジェクトをそのまま返す
        Date[] dates = { new Date() };
        UnsafeContainer unsafe = new UnsafeContainer(dates);
        dates[0].setTime(0); // 元の配列を変更 → コンテナの中身も変わってしまう！
        System.out.println("❌ 防御的コピーなし: 外部変更の影響を受ける");

        // ✅ 良い例: コピーを作成して保持する
        Date[] dates2 = { new Date() };
        SafeContainer safe = new SafeContainer(dates2);
        long originalTime = safe.getDates()[0].getTime();
        dates2[0].setTime(0); // 元の配列を変更しても…
        System.out.println("✅ 防御的コピーあり: 外部変更の影響なし = " +
                (safe.getDates()[0].getTime() == originalTime));

        // ✅ 良い例: Collections.unmodifiableList()
        List<String> original = new ArrayList<>(Arrays.asList("A", "B", "C"));
        List<String> unmodifiable = Collections.unmodifiableList(original);
        try {
            unmodifiable.add("D");
        } catch (UnsupportedOperationException e) {
            System.out.println("✅ 変更不可リスト: UnsupportedOperationException\n");
        }
    }

    // === 6. シリアライゼーションの安全対策 ===
    static void demonstrateSerializationSecurity() {
        System.out.println("--- 6. シリアライゼーションの安全対策 ---");

        // ✅ transient で機密フィールドを除外
        SerializableUser sUser = new SerializableUser("太郎", "secret123");
        System.out.println("シリアライズ前: name=" + sUser.name +
                ", password=" + sUser.password);

        try {
            // シリアライズ
            File tmpFile = File.createTempFile("serial_test", ".dat");
            tmpFile.deleteOnExit();

            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(tmpFile))) {
                oos.writeObject(sUser);
            }

            // デシリアライズ
            try (ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(tmpFile))) {
                SerializableUser restored = (SerializableUser) ois.readObject();
                System.out.println("デシリアライズ後: name=" + restored.name +
                        ", password=" + restored.password);
                System.out.println("→ transientフィールドはnullに（保護されている）");
            }
        } catch (Exception e) {
            System.out.println("エラー: " + e.getMessage());
        }

        // ✅ readObject でバリデーションを行う
        System.out.println("✅ readObject()内でデータの妥当性を検証すべき");

        // ✅ serialVersionUID を明示的に指定する
        System.out.println("✅ serialVersionUIDを明示的に宣言すべき\n");
    }

    // === 7. 最小権限の原則 ===
    static void demonstrateLeastPrivilege() {
        System.out.println("--- 7. 最小権限の原則 ---");

        System.out.println("✅ フィールドは可能な限りprivateにする");
        System.out.println("✅ メソッドは必要最小限のアクセス修飾子を使う");
        System.out.println("✅ クラスはpackage-privateをデフォルトにする");
        System.out.println("✅ publicは本当に外部に公開が必要なものだけ");

        // アクセス修飾子の範囲（狭い順）
        System.out.println("\nアクセス修飾子（狭い → 広い）:");
        System.out.println("  private → package-private(デフォルト) → protected → public");

        // ✅ finalを活用する
        System.out.println("\n✅ クラスをfinalにする → 継承による改ざんを防止");
        System.out.println("✅ メソッドをfinalにする → オーバーライドを防止\n");
    }

    // === 8. インジェクション攻撃の防止 ===
    static void demonstrateInjectionPrevention() {
        System.out.println("--- 8. インジェクション攻撃の防止 ---");

        // ❌ SQLインジェクション（悪い例）
        String userInput = "'; DROP TABLE users; --";
        String badQuery = "SELECT * FROM users WHERE name = '" + userInput + "'";
        System.out.println("❌ 文字列連結によるSQL:");
        System.out.println("  " + badQuery);

        // ✅ PreparedStatementを使う（良い例）
        System.out.println("\n✅ PreparedStatementを使用:");
        System.out.println("  String sql = \"SELECT * FROM users WHERE name = ?\";");
        System.out.println("  PreparedStatement ps = conn.prepareStatement(sql);");
        System.out.println("  ps.setString(1, userInput);");
        System.out.println("  → パラメータは自動的にエスケープされる");

        // ✅ コマンドインジェクション防止
        System.out.println("\n✅ Runtime.exec()には文字列配列を使う:");
        System.out.println("  ❌ Runtime.exec(\"cmd \" + userInput)");
        System.out.println("  ✅ Runtime.exec(new String[]{\"cmd\", userInput})");

        System.out.println("\n=== セキュアコーディング学習完了 ===");
    }
}

// === セキュアなユーザークラス（toString()で機密情報を隠す） ===
class SecureUser {
    private String name;
    private String password;

    SecureUser(String name, String password) {
        this.name = name;
        this.password = password;
    }

    @Override
    public String toString() {
        // パスワードは表示しない！
        return "SecureUser{name='" + name + "', password='****'}";
    }
}

// === 不変クラスの例 ===
final class ImmutablePerson {
    private final String name;
    private final int age;
    private final Date birthDate; // ミュータブルなフィールド

    ImmutablePerson(String name, int age, Date birthDate) {
        this.name = name;
        this.age = age;
        // 防御的コピー（コンストラクタ）
        this.birthDate = new Date(birthDate.getTime());
    }

    public String getName() { return name; }
    public int getAge() { return age; }

    // 防御的コピーを返す（ゲッター）
    public Date getBirthDate() {
        return new Date(birthDate.getTime());
    }
}

// === 防御的コピーなし（危険） ===
class UnsafeContainer {
    private Date[] dates;

    UnsafeContainer(Date[] dates) {
        this.dates = dates; // ❌ 参照をそのまま保持
    }

    Date[] getDates() {
        return dates; // ❌ 参照をそのまま返す
    }
}

// === 防御的コピーあり（安全） ===
class SafeContainer {
    private Date[] dates;

    SafeContainer(Date[] dates) {
        // ✅ コピーを作成して保持
        this.dates = new Date[dates.length];
        for (int i = 0; i < dates.length; i++) {
            this.dates[i] = new Date(dates[i].getTime());
        }
    }

    Date[] getDates() {
        // ✅ コピーを返す
        Date[] copy = new Date[dates.length];
        for (int i = 0; i < dates.length; i++) {
            copy[i] = new Date(dates[i].getTime());
        }
        return copy;
    }
}

// === シリアライズ可能なユーザー（transientで機密保護） ===
class SerializableUser implements Serializable {
    private static final long serialVersionUID = 1L;

    String name;
    transient String password; // ✅ transientでシリアライズから除外

    SerializableUser(String name, String password) {
        this.name = name;
        this.password = password;
    }

    // ✅ デシリアライズ時にバリデーションを行う
    private void readObject(ObjectInputStream ois)
            throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        // バリデーション
        if (name == null || name.isEmpty()) {
            throw new InvalidObjectException("名前は必須です");
        }
    }
}
