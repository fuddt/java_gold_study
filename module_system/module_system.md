# Java Platform Module System (JPMS) - Gold試験対策

Java 9で導入されたモジュールシステムの完全ガイドだよ。Gold試験では頻出分野だから、しっかり押さえておこうね！

## 目次
1. [JPMSの全体像](#jpmsの全体像)
2. [module-info.javaの各ディレクティブ](#module-infojavaの各ディレクティブ)
3. [移行戦略](#移行戦略)
4. [自動モジュールと無名モジュール](#自動モジュールと無名モジュール)
5. [ServiceLoaderの仕組み](#serviceloaderの仕組み)
6. [jdepsツールの使い方](#jdepsツールの使い方)
7. [試験ポイント・引っかけ問題](#試験ポイント引っかけ問題)

---

## JPMSの全体像

### モジュールシステムって何？
Java 9から導入された、パッケージの上位概念だよね。パッケージをグループ化して、**強力なカプセル化**を実現する仕組みだ。

### なぜモジュールシステムが必要なの？
従来のJavaの問題点：
- **publicは世界中に公開**：同じクラスパス上なら誰でもアクセス可能
- **JDK自体が巨大**：rt.jarが60MB超え、使わないAPIも全部ロード
- **依存関係が不明確**：どのJARが何に依存してるか分かりにくい
- **内部APIの濫用**：sun.*パッケージとか使われまくり

モジュールシステムで解決：
- **明示的な依存関係**：requires で宣言必須
- **強力なカプセル化**：exports しないパッケージは完全に隠蔽
- **JDKの分割**：java.base, java.sql, java.xml など小さいモジュールに分割
- **信頼性向上**：コンパイル時に依存関係をチェック

```java
// module-info.java の例
module com.example.myapp {
    requires java.sql;           // java.sqlモジュールに依存
    exports com.example.api;     // apiパッケージを公開
    // com.example.internalは非公開（他モジュールからアクセス不可）
}
```

---

## module-info.javaの各ディレクティブ

モジュールの設定ファイル `module-info.java` はソースルート直下に配置するんだよね。

### 基本構文

```java
module モジュール名 {
    // ディレクティブ
}
```

**重要**：モジュール名は慣習的に逆ドメイン形式（com.example.app）を使うけど、必須じゃないよ。

---

### 1. requires - 依存モジュールの宣言

他のモジュールに依存することを宣言するディレクティブだよ。

#### 通常のrequires
```java
module com.example.app {
    requires java.sql;  // java.sqlモジュールに依存
    requires java.xml;
}
```

#### requires transitive - 推移的依存

```java
// libモジュール
module com.example.lib {
    requires transitive java.xml;  // 推移的依存
    exports com.example.lib.api;
}

// appモジュール
module com.example.app {
    requires com.example.lib;  // libをrequiresするだけで...
    // 自動的にjava.xmlも使える！（transitive効果）
}
```

**ポイント**：
- `requires transitive` は「このモジュールを使う人も、このモジュールが必要だよ」という宣言
- API内でjava.xmlのクラスを使ってるなら、transitive必須じゃね？
- 試験でよく出る！

#### requires static - コンパイル時依存

```java
module com.example.app {
    requires static lombok;  // コンパイル時のみ必須
}
```

**ポイント**：
- コンパイル時には必要だけど、実行時は任意（Optional dependency）
- Lombokみたいなアノテーションプロセッサで使うよね
- 実行時に存在しなくてもエラーにならない

---

### 2. exports - パッケージの公開

モジュール内のパッケージを他モジュールから使えるようにするディレクティブ。

#### 全モジュールに公開

```java
module com.example.app {
    exports com.example.api;      // すべてのモジュールに公開
    exports com.example.util;
    // com.example.internalは非公開
}
```

#### 特定モジュールのみに公開（限定公開）

```java
module com.example.app {
    exports com.example.internal to com.example.test, com.example.admin;
    // testとadminモジュールのみアクセス可能
}
```

**重要な制約**：
- ✅ **パッケージ単位**でのみ公開可能
- ❌ 個別のクラスやインターフェースは指定不可
- ❌ サブパッケージは自動的に含まれない

```java
exports com.example.api;
// ✅ com.example.api配下のクラスは公開
// ❌ com.example.api.impl は公開されない（別途exportsが必要）
```

**試験の引っかけ**：
```java
// これはコンパイルエラー！
exports com.example.api.MyClass;  // ❌ クラス単位は不可

// 正しくはパッケージ指定
exports com.example.api;  // ✅
```

---

### 3. opens - リフレクションアクセスの許可

`exports` は public メンバーのアクセスのみだけど、`opens` は **private メンバーへのリフレクションアクセス** も許可するよ。

#### 全モジュールにリフレクション許可

```java
module com.example.app {
    opens com.example.entity;  // リフレクション全開
}
```

#### 特定モジュールのみ許可

```java
module com.example.app {
    opens com.example.entity to hibernate, jackson;
    // HibernateとJacksonのみリフレクションでprivateフィールドにアクセス可能
}
```

**exports vs opens の違い**：

| ディレクティブ | 通常アクセス | リフレクション |
|--------------|------------|--------------|
| **exports なし** | ❌ | ❌ |
| **exports** | ✅ public のみ | ❌ |
| **opens** | ❌ | ✅ private も可 |
| **exports + opens** | ✅ public のみ | ✅ private も可 |

**ユースケース**：
- **opens**: JPA Entity、Jackson DTO など、フレームワークがリフレクションする場合
- **exports**: 通常のAPI公開

```java
// 両方使う例
module com.example.app {
    exports com.example.api;     // API公開（通常アクセス）
    opens com.example.entity;    // Entity（リフレクション用）
}
```

#### open module - モジュール全体を開く

```java
open module com.example.app {
    // すべてのパッケージが暗黙的にopens扱い
    requires java.sql;
}
```

**注意**：`open module` はリフレクションを許可するだけで、exports はしないよ！

---

### 4. provides...with / uses - ServiceLoader

プラグイン機構を実現するディレクティブだね。

#### プロバイダ側（実装を提供）

```java
// サービスインターフェース
package com.example.spi;
public interface PaymentService {
    void processPayment(double amount);
}

// 実装クラス
package com.example.impl;
public class CreditCardPayment implements PaymentService {
    @Override
    public void processPayment(double amount) {
        System.out.println("Credit card: " + amount);
    }
}

// module-info.java
module com.example.payment {
    exports com.example.spi;  // インターフェースは公開
    // 実装クラスは非公開でOK
    provides com.example.spi.PaymentService
        with com.example.impl.CreditCardPayment;
}
```

#### コンシューマ側（実装を使用）

```java
// module-info.java
module com.example.app {
    requires com.example.payment;
    uses com.example.spi.PaymentService;
}

// 実装をロード
ServiceLoader<PaymentService> loader = ServiceLoader.load(PaymentService.class);
for (PaymentService service : loader) {
    service.processPayment(100.0);
}
```

**ポイント**：
- `provides` の with 以降の実装クラスは exports 不要
- 複数実装を provides できる：
  ```java
  provides Service with Impl1, Impl2, Impl3;
  ```
- ServiceLoader は実行時に動的ロード

---

## 移行戦略

既存プロジェクトをモジュール化する2つの戦略があるよ。

### トップダウン移行（Top-Down Migration）

**アプリケーション → ライブラリ** の順で移行。

```
手順：
1. アプリケーションにmodule-info.javaを作成
2. 依存ライブラリは自動モジュールとして扱う（module-info.javaなし）
3. 徐々にライブラリもモジュール化
```

```java
// アプリケーションのmodule-info.java
module com.example.app {
    requires mylib;  // mylib.jarは自動モジュール（まだmodule-info.javaなし）
    requires java.sql;
}
```

**メリット**：
- 段階的移行が可能
- すぐに始められる

**デメリット**：
- 自動モジュールは全パッケージが exports される（安全性低い）
- 移行中は依存関係が曖昧

---

### ボトムアップ移行（Bottom-Up Migration）

**ライブラリ → アプリケーション** の順で移行。

```
手順：
1. 依存のない低レベルライブラリから移行
2. 依存ライブラリが揃ったら上位モジュールを移行
3. 最後にアプリケーション本体を移行
```

```java
// 低レベルライブラリ
module com.example.util {
    exports com.example.util;
}

// 中レベルライブラリ
module com.example.db {
    requires com.example.util;  // すでにモジュール化済み
    requires java.sql;
    exports com.example.db;
}

// アプリケーション
module com.example.app {
    requires com.example.db;
    requires com.example.util;
}
```

**メリット**：
- 明確なモジュール境界
- 高い安全性

**デメリット**：
- すべての依存が揃うまで時間がかかる
- サードパーティライブラリの対応待ちになることも

---

## 自動モジュールと無名モジュール

### 自動モジュール（Automatic Module）

**module-info.java なし** の JAR を **モジュールパス** に配置すると自動モジュールになるよ。

```bash
# mylib.jar（module-info.javaなし）をモジュールパスに配置
java --module-path mods --module com.example.app
```

**自動モジュールの特徴**：
1. **モジュール名の決定**（優先順位）：
   - MANIFEST.MF の `Automatic-Module-Name` エントリ
   - なければ JAR ファイル名から生成（my-lib-1.0.jar → my.lib）

2. **すべてのパッケージが暗黙的に exports される**

3. **すべてのモジュールに暗黙的に requires する**（java.base 以外も）

4. **クラスパスの内容も読める**

```java
// 自動モジュールを使う
module com.example.app {
    requires mylib;  // mylibは自動モジュール
}
```

**試験の引っかけ**：
- 自動モジュールは `exports` を書かなくても全部公開されるよ
- 自動モジュールは `requires` を書かなくても全部読めるよ

---

### 無名モジュール（Unnamed Module）

**クラスパス** 上のすべてのクラス/JAR は **無名モジュール** に属するよ。

```bash
# クラスパスに配置
java -cp lib/mylib.jar:app.jar com.example.Main
```

**無名モジュールの特徴**：
1. **名前がない**（requires で指定不可）

2. **すべてのモジュールを読める**（exports されてるパッケージにアクセス可）

3. **すべてのパッケージを exports する**（無名モジュール内で相互アクセス可）

4. **名前付きモジュールから読めない**

```java
// これはコンパイルエラー！
module com.example.app {
    requires unnamed.module;  // ❌ 無名モジュールは requires 不可
}
```

**モジュールパス vs クラスパス**：

| | モジュールパス | クラスパス |
|---|---|---|
| **module-info.java あり** | 名前付きモジュール | ❌ 使えない |
| **module-info.java なし** | 自動モジュール | 無名モジュール |

---

### モジュール間の読み取り関係まとめ

| 読む側 ↓ / 読まれる側 → | 名前付きモジュール | 自動モジュール | 無名モジュール |
|---|---|---|---|
| **名前付きモジュール** | ✅ requires で明示 | ✅ requires で明示 | ❌ 読めない |
| **自動モジュール** | ✅ 自動で読める | ✅ 自動で読める | ✅ 読める |
| **無名モジュール** | ✅ exports のみ | ✅ すべて読める | ✅ すべて読める |

**覚え方**：
- 名前付きモジュールは厳格（requires 必須、無名モジュール読めない）
- 無名モジュールは寛容（何でも読める）
- 自動モジュールは中間（何でも読めるけど、読まれるには requires 必要）

---

## ServiceLoaderの仕組み

### ServiceLoaderとは

プラグイン機構を実現する Java 標準の API だよ。インターフェースと実装を疎結合にして、実行時に動的にロードできるんだよね。

### 使用例：データベースドライバ

#### 1. サービスインターフェース定義

```java
// db-api モジュール
package com.example.db.spi;

public interface DatabaseDriver {
    Connection connect(String url);
    String getName();
}
```

```java
// db-api/module-info.java
module db.api {
    exports com.example.db.spi;  // インターフェース公開
}
```

---

#### 2. プロバイダ実装（MySQL用）

```java
// mysql-driver モジュール
package com.example.db.mysql;

import com.example.db.spi.DatabaseDriver;

public class MySQLDriver implements DatabaseDriver {
    @Override
    public Connection connect(String url) {
        return new MySQLConnection(url);
    }

    @Override
    public String getName() {
        return "MySQL Driver";
    }
}
```

```java
// mysql-driver/module-info.java
module mysql.driver {
    requires db.api;
    provides com.example.db.spi.DatabaseDriver
        with com.example.db.mysql.MySQLDriver;
    // 実装クラスは exports 不要！
}
```

---

#### 3. プロバイダ実装（PostgreSQL用）

```java
// postgresql-driver モジュール
package com.example.db.postgresql;

import com.example.db.spi.DatabaseDriver;

public class PostgreSQLDriver implements DatabaseDriver {
    @Override
    public Connection connect(String url) {
        return new PostgreSQLConnection(url);
    }

    @Override
    public String getName() {
        return "PostgreSQL Driver";
    }
}
```

```java
// postgresql-driver/module-info.java
module postgresql.driver {
    requires db.api;
    provides com.example.db.spi.DatabaseDriver
        with com.example.db.postgresql.PostgreSQLDriver;
}
```

---

#### 4. コンシューマ側（アプリケーション）

```java
// app モジュール
package com.example.app;

import com.example.db.spi.DatabaseDriver;
import java.util.ServiceLoader;

public class Application {
    public static void main(String[] args) {
        ServiceLoader<DatabaseDriver> loader =
            ServiceLoader.load(DatabaseDriver.class);

        System.out.println("利用可能なドライバ:");
        for (DatabaseDriver driver : loader) {
            System.out.println("  - " + driver.getName());
        }

        // 特定のドライバを選択
        DatabaseDriver driver = loader.stream()
            .map(ServiceLoader.Provider::get)
            .filter(d -> d.getName().contains("MySQL"))
            .findFirst()
            .orElseThrow();

        Connection conn = driver.connect("jdbc:mysql://localhost/mydb");
    }
}
```

```java
// app/module-info.java
module com.example.app {
    requires db.api;
    uses com.example.db.spi.DatabaseDriver;
    // 実装モジュールは requires 不要！実行時に動的ロード
}
```

---

### ServiceLoader の動作

```
実行時のモジュール構成：
--module-path mods:drivers

mods/
  ├── db.api.jar
  └── com.example.app.jar

drivers/
  ├── mysql.driver.jar
  └── postgresql.driver.jar

→ ServiceLoader が drivers/ 配下の provides を自動検出してロード
```

**重要ポイント**：
- コンシューマは **実装モジュールを requires しない**
- プロバイダの **実装クラスは exports 不要**（provides のみ）
- 実行時にモジュールパスに配置されてれば自動的にロードされる
- `uses` は必須（宣言しないと ServiceLoader がロードできない）

---

### ServiceLoader Java 9 以降の新機能

#### stream() メソッド

```java
ServiceLoader<DatabaseDriver> loader = ServiceLoader.load(DatabaseDriver.class);

// 遅延ロード（実際に get() が呼ばれるまでインスタンス化されない）
loader.stream()
    .map(ServiceLoader.Provider::get)
    .forEach(driver -> System.out.println(driver.getName()));

// インスタンス化前に型情報だけ取得
loader.stream()
    .map(ServiceLoader.Provider::type)
    .forEach(clazz -> System.out.println(clazz.getName()));
```

---

## jdepsツールの使い方

`jdeps` は **依存関係分析ツール** で、モジュール移行時に超重要だよ！

### 基本的な使い方

#### 1. 基本的な依存関係表示

```bash
$ jdeps MyApp.jar

MyApp.jar -> java.base
MyApp.jar -> java.sql
   com.example.app                 -> java.lang              java.base
   com.example.app                 -> java.sql               java.sql
   com.example.app                 -> java.util              java.base
   com.example.app                 -> sun.misc               JDK internal API (java.base)
```

**sun.misc が出たら要注意！** 内部API使ってるよ。

---

#### 2. サマリー表示（-s / --summary）

```bash
$ jdeps -s MyApp.jar

MyApp.jar -> java.base
MyApp.jar -> java.sql
MyApp.jar -> java.xml
```

**モジュール間の依存だけ** 見たい時に便利だよね。

---

#### 3. 詳細表示（-v / --verbose）

```bash
$ jdeps -v MyApp.jar

MyApp.jar -> java.base
   com.example.app.Main              -> java.io.PrintStream       java.base
   com.example.app.Main              -> java.lang.Object          java.base
   com.example.app.Main              -> java.lang.String          java.base
   com.example.app.Main              -> java.lang.System          java.base
```

**クラスレベル** の依存まで表示。

---

### モジュール移行に便利なオプション

#### 1. module-info.java の生成提案

```bash
$ jdeps --generate-module-info ./output MyApp.jar

# output/MyApp/module-info.java が生成される
```

生成されるファイル例：
```java
module MyApp {
    requires java.base;  // 暗黙的なので実際は不要
    requires java.sql;
    requires java.xml;

    exports com.example.app;
    exports com.example.util;
}
```

**そのまま使わないこと！** 必要ない exports も含まれることがあるよ。

---

#### 2. JDK内部APIの使用チェック（--jdk-internals）

```bash
$ jdeps --jdk-internals MyApp.jar

MyApp.jar -> JDK removed internal API
   com.example.app.Util            -> sun.misc.BASE64Encoder    JDK internal API (java.base)

Warning: JDK internal APIs are unsupported and private to JDK implementation that are
subject to be removed or changed incompatibly and could break your application.
Please modify your code to eliminate dependence on any JDK internal APIs.

Suggested Replacement:
   sun.misc.BASE64Encoder -> Use java.util.Base64
```

**移行前に必ずチェック！** 内部API使ってたら公開APIに置き換える必要があるよ。

---

#### 3. 再帰的依存分析（-R / --recursive）

```bash
$ jdeps -s -R -cp 'lib/*' MyApp.jar

MyApp.jar -> java.base
MyApp.jar -> java.sql
MyApp.jar -> lib/mylib.jar

lib/mylib.jar -> java.base
lib/mylib.jar -> java.xml
lib/mylib.jar -> lib/commons-lang.jar

lib/commons-lang.jar -> java.base
```

**全依存ライブラリ** の依存関係を追跡。

---

#### 4. 特定パッケージへの依存を検索（-p / --package）

```bash
$ jdeps -p java.sql MyApp.jar

MyApp.jar -> java.sql
   com.example.app.DatabaseManager -> java.sql
```

「どのクラスが java.sql 使ってるの？」を調べる時に便利。

---

#### 5. dotファイル出力（可視化用）

```bash
$ jdeps -dotoutput ./dot-output MyApp.jar

# Graphviz で画像生成
$ dot -Tpng ./dot-output/summary.dot -o dependencies.png
```

**ビジュアルで依存関係** を確認できるよ。

---

### よく使うオプション組み合わせ

#### モジュール移行前の全体像把握

```bash
$ jdeps -s -R -cp 'lib/*' MyApp.jar
```
- `-s`: サマリー（モジュール間依存のみ）
- `-R`: 再帰的（全ライブラリを追跡）
- `-cp`: クラスパス指定

---

#### 内部API使用 + 詳細情報

```bash
$ jdeps --jdk-internals -v MyApp.jar
```
- `--jdk-internals`: 内部API検出
- `-v`: 詳細表示

---

#### module-info.java 生成 + 検証

```bash
# 1. module-info.java 生成
$ jdeps --generate-module-info ./output MyApp.jar

# 2. 生成されたファイルをsrcにコピー
$ cp ./output/MyApp/module-info.java ./src/

# 3. コンパイルして検証
$ javac -d mods/MyApp $(find src -name "*.java")

# 4. モジュール情報を確認
$ jar --describe-module --file=mods/MyApp.jar
```

---

### jdeps の出力を読む

```bash
$ jdeps MyApp.jar

MyApp.jar -> java.base
MyApp.jar -> java.sql
   com.example.app                 -> java.lang              java.base
   com.example.app                 -> java.sql               java.sql
   com.example.app                 -> java.util.concurrent   java.base
```

読み方：
```
[JARファイル] -> [依存先モジュール]
   [パッケージ] -> [使用パッケージ] [含まれるモジュール]
```

---

## 試験ポイント・引っかけ問題

Gold試験でよく出る引っかけポイントをまとめるよ！

### 1. requires の引っかけ

#### ❌ 間違い：java.base は明示的に書く必要がある
```java
module com.example.app {
    requires java.base;  // ❌ 不要（暗黙的に requires される）
}
```

**正解**：すべてのモジュールは **暗黙的に java.base を requires** するから書かなくてOK。

---

#### ❌ 間違い：requires transitive の推移性

```java
// モジュールA
module A {
    requires transitive B;
}

// モジュールB
module B {
    requires transitive C;
}

// モジュールD
module D {
    requires A;
    // Cも自動的に使える？
}
```

**正解**：D は B も C も **直接使える**！
- D requires A → A が transitive で B を requires → D は B を読める
- B が transitive で C を requires → D は C も読める

**推移的依存は連鎖する** よ。

---

### 2. exports の引っかけ

#### ❌ 間違い：サブパッケージも自動的に exports される

```java
module com.example.app {
    exports com.example.api;
}

// これは使える？
import com.example.api.v2.NewAPI;  // ❌ 使えない！
```

**正解**：`com.example.api` を exports しても、`com.example.api.v2` は **自動的に含まれない**。

```java
// 正しくは別途 exports
module com.example.app {
    exports com.example.api;
    exports com.example.api.v2;  // 明示的に exports
}
```

---

#### ❌ 間違い：クラス単位で exports できる

```java
module com.example.app {
    exports com.example.api.MyClass;  // ❌ コンパイルエラー
}
```

**正解**：exports は **パッケージ単位のみ**。クラス単位は不可。

---

### 3. opens の引っかけ

#### ❌ 間違い：opens すると通常アクセスも可能になる

```java
module com.example.app {
    opens com.example.entity;  // リフレクション許可
}

// 他モジュールから
import com.example.entity.User;  // ❌ コンパイルエラー
```

**正解**：`opens` は **リフレクションのみ** 許可。通常アクセスには `exports` が必要。

```java
module com.example.app {
    exports com.example.entity;  // 通常アクセス
    opens com.example.entity;    // リフレクションも
}
```

---

#### ❌ 間違い：open module は exports も含む

```java
open module com.example.app {
    // すべてのパッケージが exports される？
}

// 他モジュールから
import com.example.internal.Utils;  // ❌ コンパイルエラー
```

**正解**：`open module` は **opens のみ**。exports は別途必要。

---

### 4. provides / uses の引っかけ

#### ❌ 間違い：provides の実装クラスは exports 必要

```java
module provider {
    exports com.example.spi;
    exports com.example.impl;  // ❌ 不要
    provides com.example.spi.Service with com.example.impl.ServiceImpl;
}
```

**正解**：実装クラスは **exports 不要**。インターフェースのみ exports。

---

#### ❌ 間違い：uses を書かなくても ServiceLoader は動く

```java
module consumer {
    requires provider;
    // uses 書いてない
}

// コード内
ServiceLoader<Service> loader = ServiceLoader.load(Service.class);
// ❌ 何もロードされない
```

**正解**：`uses` を **明示的に宣言** しないと ServiceLoader はロードできないよ。

---

### 5. 自動モジュール vs 無名モジュール

#### ❌ 間違い：無名モジュールは requires できる

```java
module com.example.app {
    requires unnamed.module;  // ❌ コンパイルエラー
}
```

**正解**：無名モジュールは **名前がない** ので requires 不可。

---

#### ❌ 間違い：名前付きモジュールは無名モジュールを読める

```java
module com.example.app {
    // クラスパス上のライブラリを使いたい
}

// コード内
import org.apache.commons.lang.StringUtils;  // ❌ コンパイルエラー
```

**正解**：名前付きモジュールは **無名モジュールを読めない**。

対処法：
1. ライブラリをモジュールパスに移動（自動モジュール化）
2. アプリケーションをクラスパスで実行（無名モジュール化）

---

#### ❌ 間違い：自動モジュールのモジュール名

```bash
# JAR ファイル名: my-awesome-lib-1.2.3.jar
```

自動生成されるモジュール名は？
- `my-awesome-lib-1.2.3` ❌
- `my-awesome-lib` ❌
- `my.awesome.lib` ✅

**正解**：ハイフンは `.` に、バージョン番号は削除される。

---

### 6. jdeps の引っかけ

#### ❌ 間違い：--generate-module-info の出力はそのまま使える

```bash
$ jdeps --generate-module-info ./output MyApp.jar
```

生成された module-info.java:
```java
module MyApp {
    requires java.base;  // ❌ 不要（暗黙的）
    exports com.example.internal;  // ❌ 公開すべきでない
}
```

**正解**：生成されたファイルは **参考程度**。必ず手動で修正する。

---

### 7. モジュールパス vs クラスパス

#### ❌ 間違い：モジュールパスとクラスパス両方に同じJARを配置

```bash
$ java --module-path mods --class-path mods -m com.example.app
# ❌ 予期しない動作
```

**正解**：同じJARは **どちらか一方** に配置。

---

### 8. コンパイル/実行時の引っかけ

#### ❌ 間違い：module-info.java の配置場所

```
src/
  ├── com/example/app/
  │   ├── Main.java
  │   └── module-info.java  // ❌ パッケージ内は不可
```

**正解**：module-info.java は **ソースルート直下**。

```
src/
  ├── module-info.java  // ✅
  └── com/example/app/
      └── Main.java
```

---

#### ❌ 間違い：モジュール内のパッケージ名とモジュール名の関係

```java
module com.example.app {}  // モジュール名

package org.other.utils;   // ❌ パッケージ名は任意（関係ない）
```

**正解**：モジュール名とパッケージ名は **無関係**。ただし慣習的に合わせることが多い。

---

### 9. 試験でよく出る組み合わせ問題

#### 問題：次のうち正しいものは？

A. `requires transitive static java.xml;`
B. `exports com.example to java.base;`
C. `opens module com.example.app {}`
D. `provides Service with Impl1, Impl2;`

**正解**：**D**

- A: ❌ `transitive` と `static` は併用不可（実際は可能だが、意味がない）
- B: ❌ `java.base` は他モジュールを requires できない（依存の基盤）
- C: ❌ `opens module` は構文エラー（`open module` が正しい）
- D: ✅ 複数実装を provides 可能

---

### 10. 実行時エラーの引っかけ

#### コンパイルは通るが実行時エラー

```java
// module-info.java
module com.example.app {
    requires java.sql;
    // java.naming を requires してない
}

// Main.java
import javax.sql.DataSource;  // java.sql パッケージ
import javax.naming.Context;  // java.naming パッケージ

public class Main {
    public static void main(String[] args) {
        Context ctx = ...;  // ❌ 実行時エラー
    }
}
```

**正解**：`javax.naming` は **java.naming モジュール** に含まれる。requires 必要。

---

## まとめ：試験対策チェックリスト

### 絶対覚えるべきこと
- ✅ すべてのモジュールは暗黙的に `requires java.base`
- ✅ `requires transitive` は推移的依存（連鎖する）
- ✅ `exports` はパッケージ単位のみ（サブパッケージは含まない）
- ✅ `opens` はリフレクション用（通常アクセスには exports 必要）
- ✅ `provides` の実装クラスは exports 不要
- ✅ `uses` を書かないと ServiceLoader は動かない
- ✅ 名前付きモジュールは無名モジュールを読めない
- ✅ 自動モジュールは全パッケージを exports、全モジュールを requires
- ✅ module-info.java はソースルート直下

### jdeps でよく使うオプション
- ✅ `-s` / `--summary`: サマリー表示
- ✅ `--generate-module-info`: module-info.java 生成
- ✅ `--jdk-internals`: 内部API使用チェック
- ✅ `-R` / `--recursive`: 再帰的依存分析

### 引っかけポイント
- ❌ `open module` は exports しない（opens のみ）
- ❌ `requires transitive` は連鎖する
- ❌ サブパッケージは自動的に exports されない
- ❌ 無名モジュールは requires 不可
- ❌ 自動モジュール名はハイフン→ドット、バージョン削除

---

## 参考コード：完全なマルチモジュール例

### プロジェクト構成

```
myproject/
├── db-api/
│   ├── src/
│   │   ├── module-info.java
│   │   └── com/example/db/spi/
│   │       └── DatabaseDriver.java
│   └── ...
├── mysql-driver/
│   ├── src/
│   │   ├── module-info.java
│   │   └── com/example/db/mysql/
│   │       └── MySQLDriver.java
│   └── ...
├── postgresql-driver/
│   ├── src/
│   │   ├── module-info.java
│   │   └── com/example/db/postgresql/
│   │       └── PostgreSQLDriver.java
│   └── ...
└── app/
    ├── src/
    │   ├── module-info.java
    │   └── com/example/app/
    │       └── Main.java
    └── ...
```

### db-api モジュール

```java
// db-api/src/module-info.java
module db.api {
    exports com.example.db.spi;
}
```

```java
// db-api/src/com/example/db/spi/DatabaseDriver.java
package com.example.db.spi;

public interface DatabaseDriver {
    void connect(String url);
    String getName();
}
```

### mysql-driver モジュール

```java
// mysql-driver/src/module-info.java
module mysql.driver {
    requires db.api;
    provides com.example.db.spi.DatabaseDriver
        with com.example.db.mysql.MySQLDriver;
}
```

```java
// mysql-driver/src/com/example/db/mysql/MySQLDriver.java
package com.example.db.mysql;

import com.example.db.spi.DatabaseDriver;

public class MySQLDriver implements DatabaseDriver {
    @Override
    public void connect(String url) {
        System.out.println("MySQL接続: " + url);
    }

    @Override
    public String getName() {
        return "MySQL Driver";
    }
}
```

### app モジュール

```java
// app/src/module-info.java
module com.example.app {
    requires db.api;
    uses com.example.db.spi.DatabaseDriver;
}
```

```java
// app/src/com/example/app/Main.java
package com.example.app;

import com.example.db.spi.DatabaseDriver;
import java.util.ServiceLoader;

public class Main {
    public static void main(String[] args) {
        ServiceLoader<DatabaseDriver> loader =
            ServiceLoader.load(DatabaseDriver.class);

        for (DatabaseDriver driver : loader) {
            System.out.println("発見: " + driver.getName());
            driver.connect("jdbc:example://localhost/mydb");
        }
    }
}
```

### コンパイル・実行

```bash
# 1. 各モジュールをコンパイル
$ javac -d mods/db.api db-api/src/module-info.java db-api/src/com/example/db/spi/*.java
$ javac --module-path mods -d mods/mysql.driver mysql-driver/src/module-info.java mysql-driver/src/com/example/db/mysql/*.java
$ javac --module-path mods -d mods/postgresql.driver postgresql-driver/src/module-info.java postgresql-driver/src/com/example/db/postgresql/*.java
$ javac --module-path mods -d mods/com.example.app app/src/module-info.java app/src/com/example/app/*.java

# 2. 実行
$ java --module-path mods -m com.example.app/com.example.app.Main

# 出力:
# 発見: MySQL Driver
# MySQL接続: jdbc:example://localhost/mydb
# 発見: PostgreSQL Driver
# PostgreSQL接続: jdbc:example://localhost/mydb
```

---

これでJava Goldのモジュールシステムは完璧だよね！
試験頑張って！
