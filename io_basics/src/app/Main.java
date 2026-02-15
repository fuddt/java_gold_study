package app;

import java.io.*;
import java.nio.file.*;

/**
 * Java Gold I/O基礎の学習用サンプル
 * バイトストリーム、文字ストリーム、コンソールI/Oなどを網羅
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Java I/O 基礎学習 ===\n");

        try {
            // 一時ファイルの作成（実行環境に依存しない）
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
            Path tempFile1 = Files.createTempFile(tempDir, "io_test_", ".txt");
            Path tempFile2 = Files.createTempFile(tempDir, "io_test_", ".dat");
            Path tempFile3 = Files.createTempFile(tempDir, "io_test_", ".txt");

            System.out.println("一時ファイル作成: " + tempFile1);
            System.out.println("一時ファイル作成: " + tempFile2);
            System.out.println("一時ファイル作成: " + tempFile3 + "\n");

            // 1. バイトストリームの基本（InputStream/OutputStream）
            demonstrateByteStreams(tempFile2);

            // 2. 文字ストリームの基本（Reader/Writer）
            demonstrateCharacterStreams(tempFile1);

            // 3. BufferedStream（バッファリングによる性能向上）
            demonstrateBufferedStreams(tempFile1, tempFile3);

            // 4. InputStreamReader/OutputStreamWriter（ブリッジ）
            demonstrateBridgeStreams(tempFile2);

            // 5. PrintWriter / PrintStream
            demonstratePrintStreams(tempFile1);

            // 6. Console I/O
            demonstrateConsoleIO();

            // 7. read()の戻り値（-1の重要性）
            demonstrateReadReturnValue(tempFile1);

            // 8. バイトストリームと文字ストリームの違い
            demonstrateDifference();

            // クリーンアップ
            Files.deleteIfExists(tempFile1);
            Files.deleteIfExists(tempFile2);
            Files.deleteIfExists(tempFile3);
            System.out.println("\n一時ファイルを削除しました");

        } catch (IOException e) {
            System.err.println("エラー発生: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 1. バイトストリームの基本（InputStream/OutputStream）
     * - バイナリデータを扱う
     * - 1バイト単位で読み書き
     */
    private static void demonstrateByteStreams(Path file) throws IOException {
        System.out.println("【1. バイトストリーム - InputStream/OutputStream】");

        // FileOutputStream: ファイルへのバイト出力
        // try-with-resources: 自動でclose()が呼ばれる
        try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
            byte[] data = {65, 66, 67, 68, 69}; // 'A', 'B', 'C', 'D', 'E'
            fos.write(data);
            fos.flush(); // バッファの内容を強制的に書き出す（重要！）
            System.out.println("バイトデータ書き込み完了: " + data.length + "バイト");
        }

        // FileInputStream: ファイルからのバイト入力
        try (FileInputStream fis = new FileInputStream(file.toFile())) {
            int b;
            System.out.print("読み込んだバイトデータ: ");
            // read()は1バイト読み込み、int型（0-255）で返す。終端は-1
            while ((b = fis.read()) != -1) {
                System.out.print((char)b + " "); // 文字として表示
            }
            System.out.println();
        }
        System.out.println();
    }

    /**
     * 2. 文字ストリームの基本（Reader/Writer）
     * - テキストデータを扱う
     * - 文字エンコーディングを自動処理
     */
    private static void demonstrateCharacterStreams(Path file) throws IOException {
        System.out.println("【2. 文字ストリーム - Reader/Writer】");

        // FileWriter: ファイルへの文字出力
        try (FileWriter writer = new FileWriter(file.toFile())) {
            String text = "こんにちは、Java Gold!\nI/Oは重要だよ。";
            writer.write(text);
            writer.flush(); // 重要！バッファをフラッシュ
            System.out.println("文字データ書き込み完了");
        }

        // FileReader: ファイルからの文字入力
        try (FileReader reader = new FileReader(file.toFile())) {
            int c;
            System.out.print("読み込んだ文字データ: ");
            // read()は1文字読み込み、int型で返す。終端は-1
            while ((c = reader.read()) != -1) {
                System.out.print((char)c);
            }
            System.out.println();
        }
        System.out.println();
    }

    /**
     * 3. BufferedStream（バッファリング）
     * - 内部バッファを持ち、I/O回数を削減
     * - 性能が大幅に向上
     * - BufferedReader.readLine()は便利
     */
    private static void demonstrateBufferedStreams(Path inputFile, Path outputFile) throws IOException {
        System.out.println("【3. Buffered系 - パフォーマンス向上】");

        // BufferedWriter: バッファ付き文字出力
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile.toFile()))) {
            writer.write("1行目のテキスト");
            writer.newLine(); // 改行を書き込む（プラットフォーム依存しない）
            writer.write("2行目のテキスト");
            writer.newLine();
            writer.write("3行目のテキスト");
            // close()時に自動的にflush()される
            System.out.println("BufferedWriterで書き込み完了");
        }

        // BufferedReader: バッファ付き文字入力
        try (BufferedReader reader = new BufferedReader(new FileReader(outputFile.toFile()))) {
            String line;
            int lineNum = 1;
            System.out.println("BufferedReaderで読み込み:");
            // readLine()は1行読み込む。終端はnull（-1ではない！）
            while ((line = reader.readLine()) != null) {
                System.out.println("  " + lineNum + ": " + line);
                lineNum++;
            }
        }

        // BufferedInputStream / BufferedOutputStream（バイトストリーム版）
        try (BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(outputFile.toFile()))) {
            bos.write("Buffered byte stream".getBytes());
            System.out.println("BufferedOutputStreamで書き込み完了");
        }

        try (BufferedInputStream bis = new BufferedInputStream(
                new FileInputStream(outputFile.toFile()))) {
            byte[] buffer = new byte[100];
            int bytesRead = bis.read(buffer);
            System.out.println("BufferedInputStreamで読み込み: " +
                new String(buffer, 0, bytesRead));
        }
        System.out.println();
    }

    /**
     * 4. InputStreamReader / OutputStreamWriter（ブリッジ）
     * - バイトストリームと文字ストリームの橋渡し
     * - エンコーディングを明示的に指定可能
     */
    private static void demonstrateBridgeStreams(Path file) throws IOException {
        System.out.println("【4. ブリッジストリーム - InputStreamReader/OutputStreamWriter】");

        // OutputStreamWriter: バイトストリームに文字を書き込む
        try (OutputStreamWriter osw = new OutputStreamWriter(
                new FileOutputStream(file.toFile()), "UTF-8")) {
            osw.write("UTF-8エンコーディングで書き込み: 日本語テスト");
            osw.flush();
            System.out.println("OutputStreamWriterで書き込み完了（UTF-8）");
        }

        // InputStreamReader: バイトストリームから文字を読み込む
        try (InputStreamReader isr = new InputStreamReader(
                new FileInputStream(file.toFile()), "UTF-8")) {
            char[] buffer = new char[100];
            int charsRead = isr.read(buffer);
            System.out.println("InputStreamReaderで読み込み: " +
                new String(buffer, 0, charsRead));
        }

        // System.inとの組み合わせ例（実際には実行しない）
        System.out.println("例: new BufferedReader(new InputStreamReader(System.in))");
        System.out.println("    →標準入力（バイト）を文字ストリームに変換");
        System.out.println();
    }

    /**
     * 5. PrintWriter / PrintStream
     * - print(), println(), printf()などの便利メソッド
     * - 例外をスローしない（checkError()で確認）
     */
    private static void demonstratePrintStreams(Path file) throws IOException {
        System.out.println("【5. PrintWriter / PrintStream】");

        // PrintWriter: 文字出力
        try (PrintWriter pw = new PrintWriter(new FileWriter(file.toFile()))) {
            pw.println("PrintWriterのテスト");
            pw.print("数値: ");
            pw.println(12345);
            pw.printf("フォーマット: %s, %d%n", "文字列", 999);

            // checkError()で書き込みエラーをチェック（例外は投げない）
            if (pw.checkError()) {
                System.err.println("PrintWriterでエラー発生");
            } else {
                System.out.println("PrintWriterで書き込み完了");
            }
        }

        // PrintStream: バイト出力（System.outもPrintStream）
        System.out.println("System.outはPrintStreamのインスタンス: " +
            (System.out instanceof PrintStream));
        System.out.println("System.errもPrintStreamのインスタンス: " +
            (System.err instanceof PrintStream));
        System.out.println();
    }

    /**
     * 6. Console I/O
     * - System.in: 標準入力（InputStream）
     * - System.out: 標準出力（PrintStream）
     * - System.err: 標準エラー出力（PrintStream）
     * - Console: パスワード入力などに便利
     */
    private static void demonstrateConsoleIO() throws IOException {
        System.out.println("【6. Console I/O】");

        System.out.println("System.in: " + System.in.getClass().getName());
        System.out.println("System.out: " + System.out.getClass().getName());
        System.out.println("System.err: " + System.err.getClass().getName());

        // 標準エラー出力の例
        System.err.println("これは標準エラー出力です");

        // Console（IDEや一部環境では使えない場合がある）
        Console console = System.console();
        if (console != null) {
            System.out.println("Consoleが利用可能");
            // console.readLine("入力してください: ");
            // console.readPassword("パスワード: "); // エコーバックなし
        } else {
            System.out.println("Consoleは利用不可（IDEや非対話環境では使えない）");
        }
        System.out.println();
    }

    /**
     * 7. read()の戻り値（-1の重要性）
     * - InputStream.read(): 0-255のint、終端で-1
     * - Reader.read(): 文字のint値、終端で-1
     * - BufferedReader.readLine(): String、終端でnull
     */
    private static void demonstrateReadReturnValue(Path file) throws IOException {
        System.out.println("【7. read()の戻り値 - -1の重要性】");

        // サンプルデータ書き込み
        try (FileWriter writer = new FileWriter(file.toFile())) {
            writer.write("ABC");
        }

        // read()の戻り値を確認
        try (FileReader reader = new FileReader(file.toFile())) {
            System.out.println("read()の戻り値:");
            int c;
            while ((c = reader.read()) != -1) {
                System.out.println("  " + c + " -> '" + (char)c + "'");
            }
            System.out.println("  最後は-1（ストリームの終端）");
        }

        // よくある間違い: byte型にキャストすると-1判定ができない
        System.out.println("\n【注意】read()の戻り値をbyteにキャストしてはいけない:");
        System.out.println("  int c = is.read(); // 正しい");
        System.out.println("  byte c = (byte)is.read(); // 間違い！-1判定できない");
        System.out.println();
    }

    /**
     * 8. バイトストリームと文字ストリームの違い
     */
    private static void demonstrateDifference() {
        System.out.println("【8. バイトストリームと文字ストリームの違い】");

        System.out.println("■ バイトストリーム（InputStream/OutputStream）");
        System.out.println("  - バイナリデータを扱う");
        System.out.println("  - 1バイト単位（0-255）");
        System.out.println("  - 画像、音声、動画など");
        System.out.println("  - エンコーディングを意識する必要がある");

        System.out.println("\n■ 文字ストリーム（Reader/Writer）");
        System.out.println("  - テキストデータを扱う");
        System.out.println("  - 1文字単位（Unicode）");
        System.out.println("  - テキストファイル、ログファイルなど");
        System.out.println("  - エンコーディングを自動処理");

        System.out.println("\n■ ブリッジ（InputStreamReader/OutputStreamWriter）");
        System.out.println("  - バイトストリーム ←→ 文字ストリーム");
        System.out.println("  - エンコーディングを明示的に指定可能");
        System.out.println();
    }
}
