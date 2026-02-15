package app;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            // 一時ディレクトリを作成（実行環境に依存しない）
            Path tempDir = Files.createTempDirectory("nio2-study-");
            System.out.println("=== 作業ディレクトリ: " + tempDir + " ===\n");

            try {
                // === 1. Path の作成 ===
                System.out.println("【1. Path の作成】");
                demonstratePathCreation(tempDir);

                // === 2. Path のメソッド ===
                System.out.println("\n【2. Path のメソッド】");
                demonstratePathMethods(tempDir);

                // === 3. resolve() と relativize() ===
                System.out.println("\n【3. resolve() と relativize()】");
                demonstrateResolveAndRelativize(tempDir);

                // === 4. normalize() ===
                System.out.println("\n【4. normalize()】");
                demonstrateNormalize();

                // === 5. toAbsolutePath() と toRealPath() ===
                System.out.println("\n【5. toAbsolutePath() と toRealPath()】");
                demonstrateAbsoluteAndRealPath(tempDir);

                // === 6. Files のチェックメソッド ===
                System.out.println("\n【6. Files のチェックメソッド】");
                demonstrateFilesCheck(tempDir);

                // === 7. ファイル・ディレクトリ操作 ===
                System.out.println("\n【7. ファイル・ディレクトリ操作】");
                demonstrateFileOperations(tempDir);

                // === 8. copy(), move() と StandardCopyOption ===
                System.out.println("\n【8. copy(), move() と StandardCopyOption】");
                demonstrateCopyAndMove(tempDir);

                // === 9. ファイル読み書き ===
                System.out.println("\n【9. ファイル読み書き】");
                demonstrateReadWrite(tempDir);

                // === 10. ファイル属性 ===
                System.out.println("\n【10. ファイル属性】");
                demonstrateAttributes(tempDir);

            } finally {
                // クリーンアップ（一時ファイル削除）
                cleanupDirectory(tempDir);
                System.out.println("\n=== クリーンアップ完了 ===");
            }

        } catch (IOException e) {
            System.err.println("エラー: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 1. Path の作成
    private static void demonstratePathCreation(Path tempDir) {
        // Path.of() - Java 11+
        Path path1 = Path.of("test.txt");
        System.out.println("Path.of(\"test.txt\"): " + path1);

        // 複数の文字列を連結
        Path path2 = Path.of("dir", "subdir", "file.txt");
        System.out.println("Path.of(\"dir\", \"subdir\", \"file.txt\"): " + path2);

        // Paths.get() - 古い方法だが試験に出る
        Path path3 = Paths.get("old-style.txt");
        System.out.println("Paths.get(\"old-style.txt\"): " + path3);

        // 絶対パス
        Path path4 = Path.of(tempDir.toString(), "absolute-example.txt");
        System.out.println("絶対パス例: " + path4);
    }

    // 2. Path のメソッド
    private static void demonstratePathMethods(Path tempDir) {
        Path path = Path.of(tempDir.toString(), "dir1", "dir2", "file.txt");
        System.out.println("対象パス: " + path);

        // getFileName() - パスの最後の要素
        System.out.println("  getFileName(): " + path.getFileName());

        // getParent() - 親ディレクトリ
        System.out.println("  getParent(): " + path.getParent());

        // getRoot() - ルート要素（相対パスならnull）
        System.out.println("  getRoot(): " + path.getRoot());

        // getNameCount() - パス要素数（ルートは含まない）
        System.out.println("  getNameCount(): " + path.getNameCount());

        // getName(index) - 指定インデックスの要素（0始まり）
        for (int i = 0; i < path.getNameCount(); i++) {
            System.out.println("  getName(" + i + "): " + path.getName(i));
        }

        // subpath(beginIndex, endIndex) - 部分パス
        System.out.println("  subpath(0, 2): " + path.subpath(0, 2));
        System.out.println("  subpath(1, 3): " + path.subpath(1, 3));
    }

    // 3. resolve() と relativize()
    private static void demonstrateResolveAndRelativize(Path tempDir) {
        // resolve() - パスの結合
        Path base = Path.of("home", "user");
        Path relative = Path.of("docs", "file.txt");

        // 相対パスを結合
        Path resolved = base.resolve(relative);
        System.out.println("base.resolve(relative): " + resolved);

        // 重要！引数が絶対パスの場合、引数がそのまま返る
        Path absolutePath = Path.of("/tmp/absolute.txt");
        Path resolvedAbsolute = base.resolve(absolutePath);
        System.out.println("base.resolve(absolutePath): " + resolvedAbsolute);
        System.out.println("  → 絶対パスが渡されたので、引数がそのまま返る！");

        // relativize() - 相対パスの計算
        Path path1 = Path.of("home", "user", "docs");
        Path path2 = Path.of("home", "user", "images", "photo.jpg");

        // path1 から path2 への相対パス
        Path relative1 = path1.relativize(path2);
        System.out.println("\npath1.relativize(path2): " + relative1);

        // path2 から path1 への相対パス
        Path relative2 = path2.relativize(path1);
        System.out.println("path2.relativize(path1): " + relative2);

        // 重要！両方とも同じ種類（絶対パスか相対パス）でないとIllegalArgumentException
        try {
            Path abs = Path.of(tempDir.toString(), "file.txt");
            Path rel = Path.of("other.txt");
            abs.relativize(rel); // 例外発生！
        } catch (IllegalArgumentException e) {
            System.out.println("\n絶対パスと相対パスの混在でエラー: " + e.getClass().getSimpleName());
        }
    }

    // 4. normalize()
    private static void demonstrateNormalize() {
        // normalize() - . と .. を解決
        Path path1 = Path.of("home", "user", ".", "docs");
        System.out.println("元のパス: " + path1);
        System.out.println("normalize(): " + path1.normalize());

        Path path2 = Path.of("home", "user", "docs", "..", "images");
        System.out.println("\n元のパス: " + path2);
        System.out.println("normalize(): " + path2.normalize());

        Path path3 = Path.of("home", ".", "user", "..", "admin", "file.txt");
        System.out.println("\n元のパス: " + path3);
        System.out.println("normalize(): " + path3.normalize());

        // 注意：normalize() はシンボリックリンクを解決しない
        System.out.println("\n注意：normalize() はファイルシステムを確認しない（論理的な操作）");
    }

    // 5. toAbsolutePath() と toRealPath()
    private static void demonstrateAbsoluteAndRealPath(Path tempDir) throws IOException {
        // 相対パスを作成
        Path relative = Path.of("test.txt");
        System.out.println("相対パス: " + relative);

        // toAbsolutePath() - 絶対パスに変換（ファイルの存在チェックなし）
        Path absolute = relative.toAbsolutePath();
        System.out.println("toAbsolutePath(): " + absolute);

        // toRealPath() - 実際のパスに変換（ファイルが存在する必要がある）
        Path testFile = tempDir.resolve("realpath-test.txt");
        Files.createFile(testFile);

        Path realPath = testFile.toRealPath();
        System.out.println("\ntoRealPath(): " + realPath);
        System.out.println("  → ファイルが存在する必要がある");
        System.out.println("  → シンボリックリンクも解決する");
        System.out.println("  → normalize() も自動的に適用される");

        // ファイルが存在しない場合
        try {
            Path.of("non-existent.txt").toRealPath();
        } catch (NoSuchFileException e) {
            System.out.println("\n存在しないファイルで toRealPath() → NoSuchFileException");
        }
    }

    // 6. Files のチェックメソッド
    private static void demonstrateFilesCheck(Path tempDir) throws IOException {
        Path file = tempDir.resolve("check-test.txt");
        Path dir = tempDir.resolve("check-dir");

        Files.createFile(file);
        Files.createDirectory(dir);

        // exists() - ファイル・ディレクトリの存在確認
        System.out.println("Files.exists(file): " + Files.exists(file));
        System.out.println("Files.exists(存在しないパス): " + Files.exists(Path.of("xxx.txt")));

        // isDirectory() - ディレクトリかどうか
        System.out.println("\nFiles.isDirectory(dir): " + Files.isDirectory(dir));
        System.out.println("Files.isDirectory(file): " + Files.isDirectory(file));

        // isRegularFile() - 通常ファイルかどうか
        System.out.println("\nFiles.isRegularFile(file): " + Files.isRegularFile(file));
        System.out.println("Files.isRegularFile(dir): " + Files.isRegularFile(dir));

        // その他の便利なメソッド
        System.out.println("\nFiles.isReadable(file): " + Files.isReadable(file));
        System.out.println("Files.isWritable(file): " + Files.isWritable(file));
        System.out.println("Files.isExecutable(file): " + Files.isExecutable(file));
        System.out.println("Files.isHidden(file): " + Files.isHidden(file));
    }

    // 7. ファイル・ディレクトリ操作
    private static void demonstrateFileOperations(Path tempDir) throws IOException {
        // createFile() - ファイル作成
        Path newFile = tempDir.resolve("new-file.txt");
        Files.createFile(newFile);
        System.out.println("Files.createFile() 成功: " + newFile.getFileName());

        // すでに存在する場合は FileAlreadyExistsException
        try {
            Files.createFile(newFile);
        } catch (FileAlreadyExistsException e) {
            System.out.println("すでに存在するファイルを作成 → FileAlreadyExistsException");
        }

        // createDirectory() - ディレクトリ作成（親ディレクトリは存在する必要がある）
        Path newDir = tempDir.resolve("new-dir");
        Files.createDirectory(newDir);
        System.out.println("\nFiles.createDirectory() 成功: " + newDir.getFileName());

        // createDirectories() - 親ディレクトリも含めて作成
        Path nestedDir = tempDir.resolve("parent").resolve("child").resolve("grandchild");
        Files.createDirectories(nestedDir);
        System.out.println("Files.createDirectories() 成功: " + nestedDir);

        // delete() - ファイル・ディレクトリ削除
        Path deleteFile = tempDir.resolve("delete-me.txt");
        Files.createFile(deleteFile);
        Files.delete(deleteFile);
        System.out.println("\nFiles.delete() 成功");

        // 存在しないファイルを削除 → NoSuchFileException
        try {
            Files.delete(Path.of("non-existent.txt"));
        } catch (NoSuchFileException e) {
            System.out.println("存在しないファイルを削除 → NoSuchFileException");
        }

        // deleteIfExists() - 存在する場合のみ削除（例外なし）
        boolean deleted = Files.deleteIfExists(Path.of("non-existent.txt"));
        System.out.println("Files.deleteIfExists(存在しないファイル): " + deleted);

        // 注意：ディレクトリは空でないと削除できない
        Path dirWithFile = tempDir.resolve("dir-with-file");
        Files.createDirectory(dirWithFile);
        Files.createFile(dirWithFile.resolve("file.txt"));
        try {
            Files.delete(dirWithFile);
        } catch (DirectoryNotEmptyException e) {
            System.out.println("\n空でないディレクトリを削除 → DirectoryNotEmptyException");
        }
    }

    // 8. copy(), move() と StandardCopyOption
    private static void demonstrateCopyAndMove(Path tempDir) throws IOException {
        // === copy() ===
        Path source = tempDir.resolve("source.txt");
        Files.write(source, List.of("元のファイル内容"));

        // 基本的なコピー
        Path copy1 = tempDir.resolve("copy1.txt");
        Files.copy(source, copy1);
        System.out.println("Files.copy() 成功: " + copy1.getFileName());

        // すでに存在する場合は FileAlreadyExistsException
        try {
            Files.copy(source, copy1);
        } catch (FileAlreadyExistsException e) {
            System.out.println("既存ファイルへのコピー → FileAlreadyExistsException");
        }

        // REPLACE_EXISTING - 既存ファイルを上書き
        Path copy2 = tempDir.resolve("copy2.txt");
        Files.write(copy2, List.of("古い内容"));
        Files.copy(source, copy2, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("\nREPLACE_EXISTING でコピー成功: " + copy2.getFileName());

        // COPY_ATTRIBUTES - 属性もコピー
        Path copy3 = tempDir.resolve("copy3.txt");
        Files.copy(source, copy3, StandardCopyOption.COPY_ATTRIBUTES);
        System.out.println("COPY_ATTRIBUTES でコピー成功: " + copy3.getFileName());

        // === move() ===
        Path moveSource = tempDir.resolve("move-source.txt");
        Files.write(moveSource, List.of("移動するファイル"));

        // 基本的な移動
        Path moveDest = tempDir.resolve("move-dest.txt");
        Files.move(moveSource, moveDest);
        System.out.println("\nFiles.move() 成功: " + moveDest.getFileName());
        System.out.println("元のファイルは存在しない: " + !Files.exists(moveSource));

        // ATOMIC_MOVE - アトミックな移動（同じファイルシステム内のみ）
        Path atomicSource = tempDir.resolve("atomic-source.txt");
        Files.write(atomicSource, List.of("アトミック移動"));
        Path atomicDest = tempDir.resolve("atomic-dest.txt");
        Files.move(atomicSource, atomicDest, StandardCopyOption.ATOMIC_MOVE);
        System.out.println("\nATOMIC_MOVE で移動成功: " + atomicDest.getFileName());

        // ディレクトリのコピー（中身はコピーされない！）
        Path dirSource = tempDir.resolve("dir-source");
        Files.createDirectory(dirSource);
        Files.createFile(dirSource.resolve("file-inside.txt"));

        Path dirCopy = tempDir.resolve("dir-copy");
        Files.copy(dirSource, dirCopy);
        System.out.println("\nディレクトリのコピー成功（中身はコピーされない）");
        System.out.println("コピーしたディレクトリは空: " +
            (Files.list(dirCopy).count() == 0));
    }

    // 9. ファイル読み書き
    private static void demonstrateReadWrite(Path tempDir) throws IOException {
        Path file = tempDir.resolve("readwrite-test.txt");

        // write() - ファイルへの書き込み
        List<String> lines = List.of(
            "1行目",
            "2行目",
            "3行目：日本語テスト"
        );
        Files.write(file, lines);
        System.out.println("Files.write() 成功");

        // readAllLines() - 全行を読み込み
        List<String> readLines = Files.readAllLines(file);
        System.out.println("\nFiles.readAllLines() 結果:");
        for (String line : readLines) {
            System.out.println("  " + line);
        }

        // 追記モード
        Files.write(file, List.of("追加行"), StandardOpenOption.APPEND);
        System.out.println("\nAPPEND オプションで追記成功");

        // readString() - ファイル全体を文字列として読み込み（Java 11+）
        String content = Files.readString(file);
        System.out.println("\nFiles.readString() 結果:");
        System.out.println(content);
    }

    // 10. ファイル属性
    private static void demonstrateAttributes(Path tempDir) throws IOException {
        Path file = tempDir.resolve("attributes-test.txt");
        Files.write(file, List.of("属性テスト"));

        // size() - ファイルサイズ
        long size = Files.size(file);
        System.out.println("Files.size(): " + size + " bytes");

        // getLastModifiedTime() - 最終更新時刻
        FileTime lastModified = Files.getLastModifiedTime(file);
        System.out.println("Files.getLastModifiedTime(): " + lastModified);

        // setLastModifiedTime() - 最終更新時刻の設定
        FileTime newTime = FileTime.from(Instant.now().minusSeconds(3600));
        Files.setLastModifiedTime(file, newTime);
        System.out.println("変更後の時刻: " + Files.getLastModifiedTime(file));

        // readAttributes() - 複数の属性を一度に取得
        BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
        System.out.println("\nBasicFileAttributes:");
        System.out.println("  creationTime(): " + attrs.creationTime());
        System.out.println("  lastModifiedTime(): " + attrs.lastModifiedTime());
        System.out.println("  lastAccessTime(): " + attrs.lastAccessTime());
        System.out.println("  size(): " + attrs.size());
        System.out.println("  isDirectory(): " + attrs.isDirectory());
        System.out.println("  isRegularFile(): " + attrs.isRegularFile());
        System.out.println("  isSymbolicLink(): " + attrs.isSymbolicLink());
    }

    // クリーンアップ用のヘルパーメソッド
    private static void cleanupDirectory(Path dir) throws IOException {
        if (Files.exists(dir)) {
            Files.walk(dir)
                .sorted((a, b) -> b.compareTo(a)) // 深い順にソート（ファイル→親ディレクトリ）
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        System.err.println("削除失敗: " + path);
                    }
                });
        }
    }
}
