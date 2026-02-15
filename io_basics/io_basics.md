# Java Gold - I/O基礎 完全ガイド

## 目次
1. [バイトストリーム vs 文字ストリーム](#バイトストリーム-vs-文字ストリーム)
2. [InputStream/OutputStream系](#inputstreamoutputstream系)
3. [Reader/Writer系](#readerwriter系)
4. [Buffered系（バッファリング）](#buffered系バッファリング)
5. [InputStreamReader/OutputStreamWriter（ブリッジ）](#inputstreamreaderoutputstreamwriterブリッジ)
6. [Console I/O](#console-io)
7. [read()の戻り値](#readの戻り値)
8. [flush()の重要性](#flushの重要性)
9. [試験ポイント・引っかけ問題](#試験ポイント引っかけ問題)

---

## バイトストリーム vs 文字ストリーム

この違い、超重要だよね。まず基本から押さえていこう。

### バイトストリーム（InputStream/OutputStream）
- **何を扱う？** バイナリデータ（生のバイト列）
- **単位は？** 1バイト（8ビット、0〜255）
- **用途は？** 画像、音声、動画、ZIPファイルなど
- **読み書き** `read()` → int型で返す（0-255、終端は-1）

```java
// バイトストリームの例
try (FileInputStream fis = new FileInputStream("image.jpg")) {
    int b;
    while ((b = fis.read()) != -1) {
        // bは0〜255の値
    }
}
```

### 文字ストリーム（Reader/Writer）
- **何を扱う？** テキストデータ（文字）
- **単位は？** 1文字（Unicode）
- **用途は？** テキストファイル、ログ、設定ファイルなど
- **エンコーディング** 自動的に処理してくれる（便利！）

```java
// 文字ストリームの例
try (FileReader reader = new FileReader("text.txt")) {
    int c;
    while ((c = reader.read()) != -1) {
        // cは文字のUnicode値
        System.out.print((char)c);
    }
}
```

### どっちを使うべき？

| データ種類 | 使うべきストリーム |
|----------|----------------|
| テキスト | Reader/Writer |
| 画像・動画 | InputStream/OutputStream |
| PDF | InputStream/OutputStream |
| JSON・XML | Reader/Writer（テキストだから） |
| バイナリ全般 | InputStream/OutputStream |

**覚え方**: テキストなら文字ストリーム、それ以外はバイトストリームって感じだね。

---

## InputStream/OutputStream系

バイトストリームの主要クラスをまとめるよ。

### 主要クラス一覧

```
InputStream（抽象クラス）
  ├─ FileInputStream      // ファイルから読む
  ├─ BufferedInputStream  // バッファ付き
  ├─ ByteArrayInputStream // バイト配列から読む
  ├─ ObjectInputStream    // オブジェクトをデシリアライズ
  └─ PipedInputStream     // スレッド間通信

OutputStream（抽象クラス）
  ├─ FileOutputStream      // ファイルへ書く
  ├─ BufferedOutputStream  // バッファ付き
  ├─ ByteArrayOutputStream // バイト配列へ書く
  ├─ ObjectOutputStream    // オブジェクトをシリアライズ
  └─ PipedOutputStream     // スレッド間通信
```

### 主要メソッド

#### InputStream
```java
int read()                    // 1バイト読む（0-255）、終端で-1
int read(byte[] b)            // 配列に読む、読んだバイト数を返す
int read(byte[] b, int off, int len) // オフセット指定
void close()                  // クローズ（try-with-resourcesで自動）
int available()               // ブロックせずに読めるバイト数
```

#### OutputStream
```java
void write(int b)             // 1バイト書く
void write(byte[] b)          // 配列を書く
void write(byte[] b, int off, int len) // オフセット指定
void flush()                  // バッファを強制フラッシュ（重要！）
void close()                  // クローズ
```

### コード例

```java
// FileOutputStream - ファイルへの書き込み
try (FileOutputStream fos = new FileOutputStream("data.bin")) {
    byte[] data = {65, 66, 67}; // 'A', 'B', 'C'
    fos.write(data);
    fos.flush(); // 忘れずに！
}

// FileInputStream - ファイルからの読み込み
try (FileInputStream fis = new FileInputStream("data.bin")) {
    int b;
    while ((b = fis.read()) != -1) {
        System.out.print((char)b); // ABC
    }
}
```

---

## Reader/Writer系

文字ストリームの主要クラスだよ。

### 主要クラス一覧

```
Reader（抽象クラス）
  ├─ FileReader            // ファイルから読む
  ├─ BufferedReader        // バッファ付き（readLine()便利）
  ├─ CharArrayReader       // char配列から読む
  ├─ StringReader          // 文字列から読む
  └─ InputStreamReader     // バイトストリームから変換（ブリッジ）

Writer（抽象クラス）
  ├─ FileWriter            // ファイルへ書く
  ├─ BufferedWriter        // バッファ付き
  ├─ CharArrayWriter       // char配列へ書く
  ├─ StringWriter          // 文字列へ書く
  ├─ PrintWriter           // print/println/printf便利
  └─ OutputStreamWriter    // バイトストリームへ変換（ブリッジ）
```

### 主要メソッド

#### Reader
```java
int read()                    // 1文字読む、終端で-1
int read(char[] cbuf)         // 配列に読む
int read(char[] cbuf, int off, int len)
void close()
```

#### Writer
```java
void write(int c)             // 1文字書く
void write(char[] cbuf)       // 配列を書く
void write(String str)        // 文字列を書く
void flush()                  // 重要！
void close()
```

### コード例

```java
// FileWriter - テキストファイルへの書き込み
try (FileWriter writer = new FileWriter("text.txt")) {
    writer.write("こんにちは、Java Gold!");
    writer.flush();
}

// FileReader - テキストファイルからの読み込み
try (FileReader reader = new FileReader("text.txt")) {
    int c;
    while ((c = reader.read()) != -1) {
        System.out.print((char)c);
    }
}
```

---

## Buffered系（バッファリング）

パフォーマンスめちゃくちゃ上がるから、実務では絶対使うべきだよね。

### なぜバッファリング？

**バッファなし**: 1バイト/1文字ごとにディスクI/O → 遅い！
**バッファあり**: メモリに溜めて一気に読み書き → 速い！

```java
// 遅い例（1文字ずつI/O）
try (FileReader reader = new FileReader("large.txt")) {
    int c;
    while ((c = reader.read()) != -1) { // 毎回ディスクアクセス
        // 処理
    }
}

// 速い例（バッファリング）
try (BufferedReader reader = new BufferedReader(new FileReader("large.txt"))) {
    int c;
    while ((c = reader.read()) != -1) { // メモリから読む
        // 処理
    }
}
```

### BufferedReaderの便利メソッド

```java
String readLine()  // 1行読む（改行文字は含まない）、終端でnull
```

**超重要**: `readLine()`は終端で`null`を返す（-1じゃないよ！）

```java
try (BufferedReader reader = new BufferedReader(new FileReader("file.txt"))) {
    String line;
    while ((line = reader.readLine()) != null) { // nullチェック！
        System.out.println(line);
    }
}
```

### BufferedWriterの便利メソッド

```java
void newLine()  // プラットフォーム依存の改行を書く（\nとは限らない）
```

```java
try (BufferedWriter writer = new BufferedWriter(new FileWriter("file.txt"))) {
    writer.write("1行目");
    writer.newLine(); // Windows: \r\n, Unix: \n
    writer.write("2行目");
    writer.newLine();
}
```

### バッファサイズ

デフォルトは8192文字/バイトだけど、変更もできるよ。

```java
// カスタムバッファサイズ
BufferedReader reader = new BufferedReader(new FileReader("file.txt"), 16384);
```

---

## InputStreamReader/OutputStreamWriter（ブリッジ）

バイトストリームと文字ストリームの橋渡し役だよ。

### 何のため？

- **バイトストリーム** → **文字ストリーム** に変換
- **エンコーディング指定** ができる（UTF-8、Shift_JISなど）

```
バイトストリーム  →  InputStreamReader  →  文字ストリーム
(InputStream)                               (Reader)

文字ストリーム    →  OutputStreamWriter  →  バイトストリーム
(Writer)                                    (OutputStream)
```

### InputStreamReader

```java
// デフォルトエンコーディング
InputStreamReader isr = new InputStreamReader(new FileInputStream("file.txt"));

// エンコーディング指定
InputStreamReader isr = new InputStreamReader(
    new FileInputStream("file.txt"), "UTF-8");

// 標準入力から読む（よくあるパターン）
BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
String input = reader.readLine();
```

### OutputStreamWriter

```java
// エンコーディング指定
try (OutputStreamWriter osw = new OutputStreamWriter(
        new FileOutputStream("file.txt"), "UTF-8")) {
    osw.write("日本語テスト");
    osw.flush();
}
```

### 継承関係（重要！）

```
FileReader は InputStreamReader のサブクラス
FileWriter は OutputStreamWriter のサブクラス
```

実は`FileReader`の中身はこんな感じ:
```java
public class FileReader extends InputStreamReader {
    public FileReader(String fileName) throws FileNotFoundException {
        super(new FileInputStream(fileName));
    }
}
```

だから`FileReader`はエンコーディング指定できない（デフォルトのみ）。
エンコーディング指定したいなら`InputStreamReader`を直接使うべき！

---

## Console I/O

標準入出力とConsoleクラスについて。

### 標準入出力

```java
System.in   // 標準入力（InputStream型）
System.out  // 標準出力（PrintStream型）
System.err  // 標準エラー出力（PrintStream型）
```

### System.in（標準入力）

```java
// 直接使うのは面倒
int b = System.in.read(); // 1バイトしか読めない

// BufferedReaderで包むのが普通
BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
String line = reader.readLine();
```

### System.out / System.err（標準出力）

```java
System.out.println("通常メッセージ");   // 標準出力
System.err.println("エラーメッセージ"); // 標準エラー出力

// 型はどちらもPrintStream
System.out instanceof PrintStream // true
System.err instanceof PrintStream // true
```

**違い**: 出力先が別だから、リダイレクトで分けられるよ。
```bash
java Main > out.txt 2> err.txt
# out.txt: 標準出力のみ
# err.txt: 標準エラー出力のみ
```

### Consoleクラス

パスワード入力とかに便利（エコーバックなし）。

```java
Console console = System.console();
if (console != null) {
    String username = console.readLine("Username: ");
    char[] password = console.readPassword("Password: ");

    // パスワードは使用後すぐクリア（セキュリティ）
    Arrays.fill(password, ' ');
} else {
    // IDEや非対話環境ではnull
    System.out.println("Console not available");
}
```

**注意**: IDEから実行するとnullになることが多いよ。ターミナルから実行しよう。

---

## read()の戻り値

これ、試験で絶対出るから超重要！

### InputStream.read() / Reader.read()

```java
int read()  // 0-255（または文字コード）、終端で-1
```

**ポイント**:
- 戻り値は`int`型（byte型やchar型じゃない！）
- 終端は`-1`
- 0-255の範囲（バイトストリーム）または文字コード（文字ストリーム）

```java
try (FileInputStream fis = new FileInputStream("file.txt")) {
    int b;
    while ((b = fis.read()) != -1) { // -1チェック必須！
        // bは0-255
    }
}
```

### よくある間違い

```java
// ❌ 間違い1: byte型にキャスト
byte b;
while ((b = (byte)fis.read()) != -1) {
    // ダメ！byteは-128〜127だから、-1判定できない
}

// ❌ 間違い2: char型にキャスト（文字ストリーム）
char c;
while ((c = (char)reader.read()) != -1) {
    // ダメ！charは0〜65535（unsigned）だから、-1にならない
}

// ✅ 正しい: int型で受け取る
int b;
while ((b = fis.read()) != -1) {
    // OK！
    byte actualByte = (byte)b; // 必要なら後でキャスト
}
```

### BufferedReader.readLine()

```java
String readLine()  // 1行読む、終端でnull
```

**超重要**: `readLine()`は終端で`null`を返す（-1じゃない！）

```java
try (BufferedReader reader = new BufferedReader(new FileReader("file.txt"))) {
    String line;
    while ((line = reader.readLine()) != null) { // nullチェック！
        System.out.println(line);
    }
}
```

### まとめ表

| メソッド | 戻り値型 | 終端の値 |
|---------|---------|---------|
| `InputStream.read()` | int | -1 |
| `Reader.read()` | int | -1 |
| `BufferedReader.readLine()` | String | null |
| `InputStream.read(byte[])` | int | -1 |
| `Reader.read(char[])` | int | -1 |

---

## flush()の重要性

`flush()`、忘れたらデータが消えるかもよ！

### なぜflush()が必要？

ストリームは内部にバッファを持ってる。`write()`してもすぐにファイルに書かれるわけじゃない。

```
write() → バッファに溜める → （flush()）→ 実際にファイルへ書く
```

### いつflush()する？

1. **データを確実に書き出したいとき**
   ```java
   writer.write("重要なデータ");
   writer.flush(); // 今すぐ書き出す
   ```

2. **close()前に必ず書き出す**
   ```java
   writer.write("データ");
   writer.flush();
   writer.close(); // close()時にも自動でflush()される
   ```

3. **try-with-resources使えば安心**
   ```java
   try (FileWriter writer = new FileWriter("file.txt")) {
       writer.write("データ");
       // close()時に自動でflush()される
   }
   ```

### flush()しないとどうなる？

```java
FileWriter writer = new FileWriter("file.txt");
writer.write("重要なデータ");
// flush()も close()もしない
// → プログラムがクラッシュしたら、データは失われる！
```

### PrintWriter / PrintStreamの注意

`PrintWriter`と`PrintStream`は自動フラッシュ機能があるよ。

```java
// autoFlush=true（デフォルトは状況による）
PrintWriter pw = new PrintWriter(new FileWriter("file.txt"), true);
pw.println("これは自動でflush()される");
```

---

## 試験ポイント・引っかけ問題

Java Gold試験で狙われやすいポイントをまとめるよ！

### 1. read()の戻り値

```java
// ❌ これはコンパイルエラーになる？
byte b;
while ((b = fis.read()) != -1) {} // コンパイルOK（int→byteの暗黙変換）

// でも論理的に間違い！-1判定できない
```

**答え**: コンパイルは通るけど、論理エラー。

### 2. readLine()の終端

```java
// どっちが正しい？
while ((line = reader.readLine()) != -1)   // ❌ コンパイルエラー（Stringとintは比較できない）
while ((line = reader.readLine()) != null) // ✅ 正しい
```

### 3. close()の順序

```java
FileInputStream fis = new FileInputStream("file.txt");
BufferedInputStream bis = new BufferedInputStream(fis);

// どっちをclose()する？
bis.close();  // ✅ 外側をclose()すれば、内側も自動でclose()される
fis.close();  // 不要（でも呼んでもOK）
```

**ポイント**: ラッパーストリームをclose()すれば、内側のストリームも自動でclose()される。

### 4. FileReaderのエンコーディング

```java
// FileReaderでUTF-8指定できる？
FileReader reader = new FileReader("file.txt", "UTF-8"); // ❌ Java 10以前はコンパイルエラー

// 正しくは
InputStreamReader isr = new InputStreamReader(
    new FileInputStream("file.txt"), "UTF-8"); // ✅
```

**注意**: Java 11以降は`FileReader(File, Charset)`が追加されたよ。

### 5. BufferedWriterのnewLine()

```java
writer.write("1行目\n");       // プラットフォーム依存（Windowsで問題）
writer.write("1行目");
writer.newLine();              // ✅ プラットフォーム非依存
```

### 6. try-with-resources

```java
// これはコンパイルエラー？
try (FileReader reader = new FileReader("file.txt")) {
    // 処理
} // ✅ OK（close()自動呼び出し）

// これは？
FileReader reader = new FileReader("file.txt");
try (reader) { // Java 9以降
    // 処理
} // ✅ OK（Java 9以降）
```

### 7. PrintWriterの例外

```java
PrintWriter pw = new PrintWriter("file.txt");
pw.println("テスト");
// IOExceptionは投げられない！
// エラーチェックはcheckError()で
if (pw.checkError()) {
    System.out.println("エラー発生");
}
```

**ポイント**: `PrintWriter`と`PrintStream`は例外をスローしない（内部で握りつぶす）。

### 8. available()の誤解

```java
int available = fis.available(); // ブロックせずに読めるバイト数
byte[] buffer = new byte[available];
fis.read(buffer); // ❌ 必ずしも全部読めるとは限らない！
```

**注意**: `available()`は「ブロックせずに読める最小バイト数」であって、ファイル全体のサイズじゃないよ。

### 9. 継承関係

```java
// これはtrue？false？
new FileReader("file.txt") instanceof InputStreamReader   // ✅ true
new BufferedReader(reader) instanceof Reader              // ✅ true
new FileInputStream("file.txt") instanceof InputStream    // ✅ true
System.out instanceof PrintStream                         // ✅ true
System.out instanceof OutputStream                        // ✅ true
```

### 10. flush()のタイミング

```java
writer.write("データ");
writer.close(); // ✅ close()時に自動flush()される

// でもクラッシュに備えるなら
writer.write("データ");
writer.flush(); // ✅ 明示的にflush()した方が安全
writer.close();
```

---

## よくある試験問題パターン

### パターン1: 正しいコードを選べ

```java
// Q: ファイルを1行ずつ読むコードで正しいのは？

// A
try (BufferedReader reader = new BufferedReader(new FileReader("file.txt"))) {
    String line;
    while ((line = reader.readLine()) != null) {
        System.out.println(line);
    }
}

// B
try (BufferedReader reader = new BufferedReader(new FileReader("file.txt"))) {
    String line;
    while ((line = reader.readLine()) != -1) { // ❌ コンパイルエラー
        System.out.println(line);
    }
}

// C
try (BufferedReader reader = new BufferedReader(new FileReader("file.txt"))) {
    String line;
    while (reader.readLine() != null) { // ❌ 読んだ内容が使えない
        System.out.println(line); // lineは未代入
    }
}
```

**答え**: A

### パターン2: 出力結果を答えよ

```java
// file.txt の内容: "ABC"
try (FileInputStream fis = new FileInputStream("file.txt")) {
    System.out.println(fis.read()); // ?
    System.out.println(fis.read()); // ?
    System.out.println(fis.read()); // ?
    System.out.println(fis.read()); // ?
}

// 答え:
// 65  ('A')
// 66  ('B')
// 67  ('C')
// -1  (終端)
```

### パターン3: コンパイルエラーを見つけよ

```java
// コンパイルエラーはある？
try (FileReader reader = new FileReader("file.txt")) {
    int c;
    while ((c = reader.read()) != -1) {
        System.out.print((char)c);
    }
}

// 答え: コンパイルOK！実行もOK！
```

---

## まとめ

### 覚えるべき重要ポイント

1. **バイトストリーム vs 文字ストリーム**
   - バイト: バイナリデータ（画像、音声など）
   - 文字: テキストデータ

2. **read()の戻り値**
   - `int read()`: 0-255（または文字コード）、終端で-1
   - `String readLine()`: 1行、終端でnull

3. **flush()は忘れずに**
   - バッファの内容を強制的に書き出す
   - try-with-resourcesなら自動

4. **Buffered系は速い**
   - 必ず使うべき（性能向上）
   - `readLine()`便利

5. **ブリッジストリーム**
   - `InputStreamReader`: バイト→文字
   - `OutputStreamWriter`: 文字→バイト
   - エンコーディング指定可能

6. **try-with-resources**
   - 自動でclose()される
   - 実務では必須

### 試験対策チェックリスト

- [ ] `read()`の戻り値が`int`型であることを理解
- [ ] `readLine()`の終端が`null`であることを理解
- [ ] バイトストリームと文字ストリームの違いを説明できる
- [ ] `flush()`の必要性を理解
- [ ] `BufferedReader`の`readLine()`の使い方を理解
- [ ] `InputStreamReader`/`OutputStreamWriter`の役割を理解
- [ ] `System.in`、`System.out`、`System.err`の型を覚える
- [ ] `PrintWriter`/`PrintStream`が例外をスローしないことを理解
- [ ] try-with-resourcesの文法を理解
- [ ] 継承関係（`FileReader` extends `InputStreamReader`など）を理解

これでI/O基礎は完璧じゃね？試験頑張ってね！
