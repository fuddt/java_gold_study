package app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    // デモ用の一時ディレクトリ
    private static Path tempDir;

    public static void main(String[] args) {
        try {
            // テスト用のディレクトリ構造を作成
            setupTestEnvironment();

            System.out.println("=== File Stream API のデモ ===\n");

            // 1. Files.lines() - ファイルを Stream<String> として読み込む
            demonstrateFilesLines();

            // 2. Files.list() - ディレクトリの内容を一覧表示（非再帰）
            demonstrateFilesList();

            // 3. Files.walk() - ディレクトリを再帰的に走査
            demonstrateFilesWalk();

            // 4. Files.find() - 条件に基づいて再帰的に検索
            demonstrateFilesFind();

            // 5. BufferedReader.lines() - 代替手段
            demonstrateBufferedReaderLines();

            // 6. Stream API との組み合わせ
            demonstrateStreamCombination();

            // 7. リソースクローズを忘れた場合の危険性
            demonstrateResourceLeakDanger();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // クリーンアップ
            cleanup();
        }
    }

    /**
     * テスト用のディレクトリとファイルを作成
     */
    private static void setupTestEnvironment() throws IOException {
        tempDir = Files.createTempDirectory("file_stream_test");
        System.out.println("テストディレクトリ作成: " + tempDir + "\n");

        // ファイル構造:
        // tempDir/
        //   ├─ file1.txt
        //   ├─ file2.txt
        //   ├─ data.csv
        //   ├─ subdir1/
        //   │   ├─ file3.txt
        //   │   └─ file4.log
        //   └─ subdir2/
        //       ├─ file5.txt
        //       └─ nested/
        //           └─ file6.txt

        // ルートディレクトリのファイル
        writeFile(tempDir.resolve("file1.txt"),
            "Line 1: Hello World",
            "Line 2: Java Gold",
            "Line 3: Stream API");

        writeFile(tempDir.resolve("file2.txt"),
            "Line 1: File Stream",
            "Line 2: Must close",
            "Line 3: Try-with-resources");

        writeFile(tempDir.resolve("data.csv"),
            "name,age,city",
            "Alice,25,Tokyo",
            "Bob,30,Osaka");

        // サブディレクトリ1
        Path subdir1 = Files.createDirectory(tempDir.resolve("subdir1"));
        writeFile(subdir1.resolve("file3.txt"),
            "This is file3",
            "In subdirectory 1");

        writeFile(subdir1.resolve("file4.log"),
            "2024-01-01 INFO: Application started",
            "2024-01-01 ERROR: Something went wrong");

        // サブディレクトリ2（ネストあり）
        Path subdir2 = Files.createDirectory(tempDir.resolve("subdir2"));
        writeFile(subdir2.resolve("file5.txt"),
            "File in subdir2");

        Path nested = Files.createDirectory(subdir2.resolve("nested"));
        writeFile(nested.resolve("file6.txt"),
            "Deeply nested file");
    }

    /**
     * ファイルに複数行を書き込む
     */
    private static void writeFile(Path path, String... lines) throws IOException {
        Files.write(path, List.of(lines));
    }

    /**
     * 1. Files.lines() - ファイルを Stream<String> として読み込む
     * 【重要】遅延評価、クローズ必須！
     */
    private static void demonstrateFilesLines() throws IOException {
        System.out.println("--- 1. Files.lines() ---");

        Path file = tempDir.resolve("file1.txt");

        // ✅ 正しい使い方: try-with-resources
        System.out.println("✅ try-with-resources で正しく使用:");
        try (Stream<String> lines = Files.lines(file)) {
            lines.forEach(System.out::println);
        }

        System.out.println();

        // フィルタリングの例
        System.out.println("✅ Stream API と組み合わせ（フィルタリング）:");
        try (Stream<String> lines = Files.lines(file)) {
            lines.filter(line -> line.contains("Java"))
                 .forEach(System.out::println);
        }

        System.out.println();

        // ❌ 間違った使い方: クローズしない（デモのため実際には実行しない）
        System.out.println("❌ 危険: try-with-resources を使わない場合");
        System.out.println("   Stream<String> lines = Files.lines(file);");
        System.out.println("   lines.forEach(System.out::println);");
        System.out.println("   → リソースリークの危険性あり！\n");

        System.out.println();
    }

    /**
     * 2. Files.list() - ディレクトリの内容を一覧表示（非再帰）
     * 【重要】サブディレクトリの中身は表示されない
     */
    private static void demonstrateFilesList() throws IOException {
        System.out.println("--- 2. Files.list() ---");
        System.out.println("✅ ディレクトリ直下のファイル/ディレクトリのみ表示（非再帰）:");

        try (Stream<Path> paths = Files.list(tempDir)) {
            paths.forEach(path -> {
                if (Files.isDirectory(path)) {
                    System.out.println("  📁 " + path.getFileName());
                } else {
                    System.out.println("  📄 " + path.getFileName());
                }
            });
        }

        System.out.println("\n✅ ファイルのみフィルタリング:");
        try (Stream<Path> paths = Files.list(tempDir)) {
            paths.filter(Files::isRegularFile)
                 .forEach(path -> System.out.println("  " + path.getFileName()));
        }

        System.out.println();
    }

    /**
     * 3. Files.walk() - ディレクトリを再帰的に走査
     * 【重要】デフォルトで全階層、maxDepth で深さ制限可能
     */
    private static void demonstrateFilesWalk() throws IOException {
        System.out.println("--- 3. Files.walk() ---");

        // 全階層走査（デフォルト）
        System.out.println("✅ 全階層を再帰的に走査:");
        try (Stream<Path> paths = Files.walk(tempDir)) {
            paths.forEach(path -> {
                int depth = path.getNameCount() - tempDir.getNameCount();
                String indent = "  ".repeat(depth);
                System.out.println(indent + path.getFileName());
            });
        }

        System.out.println();

        // 深さ制限（maxDepth = 1 → 直下のみ、Files.list() と同等）
        System.out.println("✅ maxDepth = 1 で深さ制限（Files.list() と同等）:");
        try (Stream<Path> paths = Files.walk(tempDir, 1)) {
            paths.filter(path -> !path.equals(tempDir))
                 .forEach(path -> System.out.println("  " + path.getFileName()));
        }

        System.out.println();

        // maxDepth = 2（2階層まで）
        System.out.println("✅ maxDepth = 2 で2階層まで:");
        try (Stream<Path> paths = Files.walk(tempDir, 2)) {
            paths.filter(path -> !path.equals(tempDir))
                 .forEach(path -> System.out.println("  " + path));
        }

        System.out.println();

        // すべての .txt ファイルを検索
        System.out.println("✅ すべての .txt ファイルを検索:");
        try (Stream<Path> paths = Files.walk(tempDir)) {
            paths.filter(Files::isRegularFile)
                 .filter(path -> path.toString().endsWith(".txt"))
                 .forEach(path -> System.out.println("  " + path));
        }

        System.out.println();
    }

    /**
     * 4. Files.find() - 条件に基づいて再帰的に検索
     * 【重要】BiPredicate<Path, BasicFileAttributes> を使用
     */
    private static void demonstrateFilesFind() throws IOException {
        System.out.println("--- 4. Files.find() ---");

        // .txt ファイルを検索
        System.out.println("✅ .txt ファイルを検索:");
        BiPredicate<Path, BasicFileAttributes> txtMatcher =
            (path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(".txt");

        try (Stream<Path> paths = Files.find(tempDir, Integer.MAX_VALUE, txtMatcher)) {
            paths.forEach(path -> System.out.println("  " + path));
        }

        System.out.println();

        // サイズが100バイトより大きいファイルを検索
        System.out.println("✅ サイズが50バイトより大きいファイルを検索:");
        BiPredicate<Path, BasicFileAttributes> sizeMatcher =
            (path, attrs) -> attrs.isRegularFile() && attrs.size() > 50;

        try (Stream<Path> paths = Files.find(tempDir, Integer.MAX_VALUE, sizeMatcher)) {
            paths.forEach(path -> {
                try {
                    long size = Files.size(path);
                    System.out.println("  " + path.getFileName() + " (" + size + " bytes)");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        System.out.println();

        // ディレクトリのみ検索
        System.out.println("✅ ディレクトリのみ検索:");
        BiPredicate<Path, BasicFileAttributes> dirMatcher =
            (path, attrs) -> attrs.isDirectory();

        try (Stream<Path> paths = Files.find(tempDir, Integer.MAX_VALUE, dirMatcher)) {
            paths.filter(path -> !path.equals(tempDir))
                 .forEach(path -> System.out.println("  " + path));
        }

        System.out.println();
    }

    /**
     * 5. BufferedReader.lines() - 代替手段
     * Files.lines() と同等だが、BufferedReader 経由
     */
    private static void demonstrateBufferedReaderLines() throws IOException {
        System.out.println("--- 5. BufferedReader.lines() ---");

        Path file = tempDir.resolve("file1.txt");

        System.out.println("✅ BufferedReader.lines() を使用:");
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            reader.lines()
                  .forEach(System.out::println);
        }

        System.out.println();

        // Files.lines() との比較
        System.out.println("【比較】");
        System.out.println("  Files.lines(path):");
        System.out.println("    - 簡潔な記述");
        System.out.println("    - 内部で BufferedReader を使用");
        System.out.println("  BufferedReader.lines():");
        System.out.println("    - より細かい制御が可能");
        System.out.println("    - 文字エンコーディングを明示的に指定したい場合に便利");

        System.out.println();
    }

    /**
     * 6. Stream API との組み合わせ
     * filter, map, collect などの実践例
     */
    private static void demonstrateStreamCombination() throws IOException {
        System.out.println("--- 6. Stream API との組み合わせ ---");

        Path csvFile = tempDir.resolve("data.csv");

        // CSVファイルをパースして処理
        System.out.println("✅ CSVファイルの処理:");
        try (Stream<String> lines = Files.lines(csvFile)) {
            List<String> names = lines
                .skip(1)  // ヘッダー行をスキップ
                .map(line -> line.split(",")[0])  // 名前だけ抽出
                .collect(Collectors.toList());

            System.out.println("  名前一覧: " + names);
        }

        System.out.println();

        // 複数ファイルの内容を統合
        System.out.println("✅ 複数ファイルから特定の行を抽出:");
        try (Stream<Path> paths = Files.walk(tempDir)) {
            long count = paths
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".txt"))
                .flatMap(path -> {
                    try {
                        return Files.lines(path);
                    } catch (IOException e) {
                        return Stream.empty();
                    }
                })
                .filter(line -> line.contains("Line"))
                .peek(System.out::println)
                .count();

            System.out.println("  合計: " + count + " 行");
        }

        System.out.println();

        // ファイルサイズの合計
        System.out.println("✅ .txt ファイルのサイズ合計:");
        try (Stream<Path> paths = Files.walk(tempDir)) {
            long totalSize = paths
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".txt"))
                .mapToLong(path -> {
                    try {
                        return Files.size(path);
                    } catch (IOException e) {
                        return 0L;
                    }
                })
                .sum();

            System.out.println("  合計サイズ: " + totalSize + " bytes");
        }

        System.out.println();
    }

    /**
     * 7. リソースクローズを忘れた場合の危険性
     * 【試験重要ポイント】
     */
    private static void demonstrateResourceLeakDanger() {
        System.out.println("--- 7. リソースクローズの重要性 ---");
        System.out.println();
        System.out.println("【試験での引っかけポイント】");
        System.out.println();
        System.out.println("❌ 間違い例1: try-with-resources なし");
        System.out.println("  Stream<String> lines = Files.lines(path);");
        System.out.println("  lines.forEach(System.out::println);");
        System.out.println("  → リソースリーク！");
        System.out.println();

        System.out.println("❌ 間違い例2: Stream を変数に代入してから try");
        System.out.println("  Stream<Path> paths = Files.list(dir);");
        System.out.println("  try (paths) {  // これはコンパイルエラー！");
        System.out.println("    paths.forEach(System.out::println);");
        System.out.println("  }");
        System.out.println("  → try-with-resources は変数宣言と同時に行う必要がある");
        System.out.println();

        System.out.println("✅ 正しい例:");
        System.out.println("  try (Stream<String> lines = Files.lines(path)) {");
        System.out.println("    lines.forEach(System.out::println);");
        System.out.println("  }");
        System.out.println();

        System.out.println("【重要】以下のメソッドは全て Stream を返すため、クローズが必須:");
        System.out.println("  - Files.lines(Path)");
        System.out.println("  - Files.list(Path)");
        System.out.println("  - Files.walk(Path)");
        System.out.println("  - Files.walk(Path, int maxDepth)");
        System.out.println("  - Files.find(Path, int maxDepth, BiPredicate)");
        System.out.println("  - BufferedReader.lines()");
        System.out.println();

        System.out.println("【遅延評価の利点】");
        System.out.println("  - 巨大なファイルでもメモリに全て読み込まない");
        System.out.println("  - 必要な行だけ処理できる（limit() などと組み合わせ）");
        System.out.println("  - ただし、終端操作が実行されるまで処理は開始されない");
        System.out.println();
    }

    /**
     * クリーンアップ: 一時ファイルとディレクトリを削除
     */
    private static void cleanup() {
        if (tempDir != null) {
            try {
                // ディレクトリを再帰的に削除
                try (Stream<Path> paths = Files.walk(tempDir)) {
                    paths.sorted((a, b) -> b.compareTo(a))  // 深い方から削除
                         .forEach(path -> {
                             try {
                                 Files.delete(path);
                             } catch (IOException e) {
                                 System.err.println("削除失敗: " + path);
                             }
                         });
                }
                System.out.println("クリーンアップ完了: テストディレクトリを削除しました");
            } catch (IOException e) {
                System.err.println("クリーンアップエラー: " + e.getMessage());
            }
        }
    }
}
