package app;

import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * Java Platform Module System (JPMS) の学習用デモプログラム
 * Java 9以降のモジュールシステムの主要概念を実行時に説明する
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("Java Platform Module System (JPMS) 学習デモ");
        System.out.println("=".repeat(80));
        System.out.println();

        // 1. module-info.java の基本ディレクティブの説明
        explainModuleInfoDirectives();

        // 2. 実行中のモジュール情報を取得
        demonstrateModuleAPI();

        // 3. モジュールのプロパティをプログラム的にチェック
        checkModuleProperties();

        // 4. 移行戦略の説明
        explainMigrationStrategies();

        // 5. ServiceLoaderの概念説明
        explainServiceLoader();

        // 6. jdepsツールの使い方
        explainJdepsTool();

        System.out.println("\n" + "=".repeat(80));
        System.out.println("デモ終了");
        System.out.println("=".repeat(80));
    }

    /**
     * module-info.java の各ディレクティブを説明
     */
    private static void explainModuleInfoDirectives() {
        System.out.println("【1. module-info.java のディレクティブ】");
        System.out.println("-".repeat(80));

        System.out.println("\n■ requires - 依存モジュールの宣言");
        System.out.println("  module app {");
        System.out.println("      requires java.sql;           // 通常の依存");
        System.out.println("      requires transitive java.xml; // 推移的依存（使用者も自動的にjava.xmlにアクセス可能）");
        System.out.println("      requires static java.compiler; // コンパイル時のみ必須、実行時は任意");
        System.out.println("  }");

        System.out.println("\n■ exports - パッケージの公開");
        System.out.println("  module app {");
        System.out.println("      exports com.example.api;        // すべてのモジュールに公開");
        System.out.println("      exports com.example.internal to module1, module2; // 特定モジュールのみに公開");
        System.out.println("  }");
        System.out.println("  ※ exportsはパッケージ単位。個別クラスは指定不可");

        System.out.println("\n■ opens - リフレクションアクセスの許可");
        System.out.println("  module app {");
        System.out.println("      opens com.example.entity;          // すべてのモジュールにリフレクション許可");
        System.out.println("      opens com.example.dto to hibernate; // 特定モジュールのみ許可");
        System.out.println("  }");
        System.out.println("  ※ opensはprivateメンバーへのリフレクションアクセスを許可");
        System.out.println("  ※ 通常のexportsはpublicメンバーのみアクセス可能");

        System.out.println("\n■ provides...with - サービスプロバイダの提供");
        System.out.println("  module app {");
        System.out.println("      provides com.example.spi.Service with com.example.impl.ServiceImpl;");
        System.out.println("  }");
        System.out.println("  ※ ServiceLoaderで動的にロードされる実装を提供");

        System.out.println("\n■ uses - サービスの使用宣言");
        System.out.println("  module app {");
        System.out.println("      uses com.example.spi.Service;");
        System.out.println("  }");
        System.out.println("  ※ ServiceLoaderでサービスを検索・利用することを宣言");

        System.out.println();
    }

    /**
     * Module APIを使って実行時のモジュール情報を取得
     */
    private static void demonstrateModuleAPI() {
        System.out.println("【2. Module API - 実行時のモジュール情報取得】");
        System.out.println("-".repeat(80));

        // 現在のクラスのモジュールを取得
        Module currentModule = Main.class.getModule();

        System.out.println("■ 現在のモジュール情報:");
        System.out.println("  モジュール名: " + currentModule.getName());
        System.out.println("  名前付きモジュール: " + currentModule.isNamed());

        // ModuleDescriptorから詳細情報を取得
        ModuleDescriptor descriptor = currentModule.getDescriptor();
        if (descriptor != null) {
            System.out.println("  モジュール記述子: " + descriptor.name());
            System.out.println("  requires数: " + descriptor.requires().size());
            System.out.println("  exports数: " + descriptor.exports().size());

            // requiresの詳細
            if (!descriptor.requires().isEmpty()) {
                System.out.println("\n  依存モジュール (requires):");
                descriptor.requires().forEach(req ->
                    System.out.println("    - " + req.name() +
                        (req.modifiers().contains(ModuleDescriptor.Requires.Modifier.TRANSITIVE) ? " (transitive)" : "") +
                        (req.modifiers().contains(ModuleDescriptor.Requires.Modifier.STATIC) ? " (static)" : ""))
                );
            }
        }

        // java.baseモジュールの情報（すべてのモジュールが暗黙的に依存）
        System.out.println("\n■ java.base モジュール:");
        Module javaBase = String.class.getModule();
        System.out.println("  すべてのモジュールは暗黙的に 'requires java.base' を持つ");
        System.out.println("  java.baseのモジュール名: " + javaBase.getName());

        // パッケージの確認
        System.out.println("\n■ モジュールのパッケージ確認:");
        Set<String> packages = currentModule.getPackages();
        System.out.println("  このモジュールのパッケージ数: " + packages.size());
        packages.forEach(pkg -> System.out.println("    - " + pkg));

        System.out.println();
    }

    /**
     * モジュールのプロパティをプログラム的にチェック
     */
    private static void checkModuleProperties() {
        System.out.println("【3. モジュールプロパティのプログラム的チェック】");
        System.out.println("-".repeat(80));

        Module currentModule = Main.class.getModule();

        // パッケージのエクスポート状況を確認
        System.out.println("■ パッケージの公開状況チェック:");
        String packageName = Main.class.getPackageName();
        System.out.println("  パッケージ 'app' がエクスポートされているか: " +
            currentModule.isExported(packageName));
        System.out.println("  java.langパッケージ(java.base)の公開状況: " +
            String.class.getModule().isExported("java.lang"));

        // リフレクションアクセスの確認
        System.out.println("\n■ リフレクションアクセスの確認:");
        System.out.println("  'app' パッケージが開かれているか: " +
            currentModule.isOpen(packageName));
        System.out.println("  ※ opensディレクティブで開かれたパッケージはリフレクション可能");

        // レイヤー情報
        System.out.println("\n■ ModuleLayer情報:");
        ModuleLayer layer = currentModule.getLayer();
        if (layer != null) {
            System.out.println("  レイヤー: " + layer);
            System.out.println("  親レイヤー数: " + layer.parents().size());
            System.out.println("  このレイヤーのモジュール数: " + layer.modules().size());
        } else {
            System.out.println("  このモジュールはレイヤーに属していない（unnamed module）");
        }

        System.out.println();
    }

    /**
     * モジュール移行戦略の説明
     */
    private static void explainMigrationStrategies() {
        System.out.println("【4. モジュール移行戦略】");
        System.out.println("-".repeat(80));

        System.out.println("■ トップダウン移行 (Top-Down Migration)");
        System.out.println("  アプリケーション → ライブラリの順で移行");
        System.out.println("  手順:");
        System.out.println("    1. アプリケーションモジュールにmodule-info.javaを作成");
        System.out.println("    2. 依存ライブラリは自動モジュール(Automatic Module)として扱う");
        System.out.println("    3. 徐々にライブラリもモジュール化");
        System.out.println("  メリット: 段階的移行が可能");
        System.out.println("  デメリット: 自動モジュールは全パッケージをexportsするため安全性が低い");

        System.out.println("\n■ ボトムアップ移行 (Bottom-Up Migration)");
        System.out.println("  ライブラリ → アプリケーションの順で移行");
        System.out.println("  手順:");
        System.out.println("    1. 依存関係のない低レベルライブラリから移行");
        System.out.println("    2. 依存ライブラリが揃ったら上位モジュールを移行");
        System.out.println("    3. 最後にアプリケーション本体を移行");
        System.out.println("  メリット: 明確なモジュール境界、高い安全性");
        System.out.println("  デメリット: すべての依存が揃うまで時間がかかる");

        System.out.println("\n■ 自動モジュール (Automatic Module)");
        System.out.println("  - module-info.javaなしのJARをモジュールパスに配置すると自動モジュールになる");
        System.out.println("  - モジュール名はMANIFESTのAutomatic-Module-Nameまたはファイル名から生成");
        System.out.println("  - すべてのパッケージが暗黙的にexportsされる");
        System.out.println("  - すべてのモジュールに暗黙的にrequiresする");
        System.out.println("  例: my-lib-1.0.jar → モジュール名 'my.lib'");

        System.out.println("\n■ 無名モジュール (Unnamed Module)");
        System.out.println("  - クラスパス上のすべてのクラス/JARは無名モジュールに属する");
        System.out.println("  - 無名モジュールはすべてのモジュールを読める");
        System.out.println("  - 名前付きモジュールは無名モジュールを読めない（requires不可）");
        System.out.println("  - レガシーコードとの互換性を保つための仕組み");

        System.out.println();
    }

    /**
     * ServiceLoaderの概念と使い方を説明
     */
    private static void explainServiceLoader() {
        System.out.println("【5. ServiceLoader - サービスプロバイダパターン】");
        System.out.println("-".repeat(80));

        System.out.println("■ ServiceLoaderとは");
        System.out.println("  プラグイン機構を実現するJavaの標準API");
        System.out.println("  インターフェースと実装を疎結合にし、実行時に動的にロード");

        System.out.println("\n■ 使い方（プロバイダ側）");
        System.out.println("  1. サービスインターフェースを定義");
        System.out.println("     public interface MyService {");
        System.out.println("         void execute();");
        System.out.println("     }");
        System.out.println();
        System.out.println("  2. 実装クラスを作成");
        System.out.println("     public class MyServiceImpl implements MyService {");
        System.out.println("         public void execute() { /* 実装 */ }");
        System.out.println("     }");
        System.out.println();
        System.out.println("  3. module-info.javaで提供を宣言");
        System.out.println("     module provider {");
        System.out.println("         exports com.example.api;  // インターフェースを公開");
        System.out.println("         provides com.example.api.MyService");
        System.out.println("             with com.example.impl.MyServiceImpl;");
        System.out.println("     }");

        System.out.println("\n■ 使い方（コンシューマ側）");
        System.out.println("  1. module-info.javaで使用を宣言");
        System.out.println("     module consumer {");
        System.out.println("         requires provider;  // または transitiveで取得");
        System.out.println("         uses com.example.api.MyService;");
        System.out.println("     }");
        System.out.println();
        System.out.println("  2. ServiceLoaderでロード");
        System.out.println("     ServiceLoader<MyService> loader = ServiceLoader.load(MyService.class);");
        System.out.println("     for (MyService service : loader) {");
        System.out.println("         service.execute();");
        System.out.println("     }");

        System.out.println("\n■ ServiceLoaderの実演（java.base内のサービス）");
        try {
            // java.nio.file.spi.FileSystemProviderの例
            ServiceLoader<java.nio.file.spi.FileSystemProvider> loader =
                ServiceLoader.load(java.nio.file.spi.FileSystemProvider.class);

            System.out.println("  システムにロードされているFileSystemProvider:");
            int count = 0;
            for (java.nio.file.spi.FileSystemProvider provider : loader) {
                count++;
                System.out.println("    - " + provider.getScheme() + ": " +
                    provider.getClass().getName());
            }
            System.out.println("  合計: " + count + " 個のプロバイダ");
        } catch (Exception e) {
            System.out.println("  エラー: " + e.getMessage());
        }

        System.out.println();
    }

    /**
     * jdepsツールの使い方を説明
     */
    private static void explainJdepsTool() {
        System.out.println("【6. jdeps - 依存関係分析ツール】");
        System.out.println("-".repeat(80));

        System.out.println("■ jdepsとは");
        System.out.println("  クラス/JARファイルの依存関係を分析するコマンドラインツール");
        System.out.println("  モジュール移行時の依存関係把握に必須");

        System.out.println("\n■ 基本的な使い方");
        System.out.println("  # 基本的な依存関係表示");
        System.out.println("  $ jdeps MyApp.jar");
        System.out.println();
        System.out.println("  # モジュール依存関係のサマリー表示");
        System.out.println("  $ jdeps -s MyApp.jar");
        System.out.println("  $ jdeps --summary MyApp.jar");
        System.out.println();
        System.out.println("  # 詳細な依存関係（パッケージレベル）");
        System.out.println("  $ jdeps -v MyApp.jar");
        System.out.println("  $ jdeps --verbose MyApp.jar");

        System.out.println("\n■ モジュール移行に便利なオプション");
        System.out.println("  # module-info.javaの生成提案");
        System.out.println("  $ jdeps --generate-module-info <出力ディレクトリ> MyApp.jar");
        System.out.println();
        System.out.println("  # JDK内部APIの使用をチェック");
        System.out.println("  $ jdeps --jdk-internals MyApp.jar");
        System.out.println("  ※ sun.*、com.sun.* などの使用を検出");
        System.out.println();
        System.out.println("  # クラスパス上の依存関係分析");
        System.out.println("  $ jdeps -cp lib/* MyApp.jar");
        System.out.println();
        System.out.println("  # 特定パッケージへの依存を検索");
        System.out.println("  $ jdeps -p java.sql MyApp.jar");
        System.out.println("  $ jdeps --package java.sql MyApp.jar");

        System.out.println("\n■ よく使うオプション組み合わせ");
        System.out.println("  # モジュール移行前の全体像把握");
        System.out.println("  $ jdeps -s -R -cp 'lib/*' MyApp.jar");
        System.out.println("    -s: サマリー表示");
        System.out.println("    -R: 再帰的に依存を追跡");
        System.out.println("    -cp: クラスパス指定");
        System.out.println();
        System.out.println("  # dotファイル形式で出力（可視化用）");
        System.out.println("  $ jdeps -dotoutput <dir> MyApp.jar");
        System.out.println("  $ dot -Tpng <dir>/summary.dot -o dependencies.png");

        System.out.println("\n■ 出力例");
        System.out.println("  MyApp.jar -> java.base");
        System.out.println("  MyApp.jar -> java.sql");
        System.out.println("     com.example.app -> java.lang      java.base");
        System.out.println("     com.example.app -> java.sql       java.sql");
        System.out.println("     com.example.app -> java.util      java.base");

        System.out.println();
    }
}
