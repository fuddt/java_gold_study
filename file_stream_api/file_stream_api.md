# File Stream API - Java Gold 対策

ファイルとディレクトリを Stream で扱う API について学ぶよ。これ、試験でめっちゃ出るから要注意だね。

## 概要

Java 8 から、ファイル操作が Stream API と統合されて、めちゃくちゃ便利になったんだよね。でも、**リソースのクローズが必須**っていう罠があるから、そこが試験の引っかけポイントになってる。

### 主要メソッド一覧

| メソッド | 戻り値 | 説明 | 再帰 |
|---------|--------|------|------|
| `Files.lines(Path)` | `Stream<String>` | ファイルを行ごとに読み込む | - |
| `Files.list(Path)` | `Stream<Path>` | ディレクトリ直下を一覧表示 | ❌ |
| `Files.walk(Path)` | `Stream<Path>` | ディレクトリを再帰的に走査 | ✅ |
| `Files.walk(Path, int)` | `Stream<Path>` | 深さ制限付きで走査 | ✅ |
| `Files.find(Path, int, BiPredicate)` | `Stream<Path>` | 条件付き再帰検索 | ✅ |
| `BufferedReader.lines()` | `Stream<String>` | BufferedReader から行を読む | - |

**全部 try-with-resources 必須！** これ超重要！

---

## 1. Files.lines() - ファイルを Stream として読み込む

ファイルの内容を `Stream<String>` として読み込むメソッドだよ。1行が1つの要素になる。

### 基本的な使い方

```java
// ✅ 正しい使い方
try (Stream<String> lines = Files.lines(Paths.get("data.txt"))) {
    lines.forEach(System.out::println);
}

// ❌ 間違い: クローズしない
Stream<String> lines = Files.lines(Paths.get("data.txt"));
lines.forEach(System.out::println);  // リソースリーク！
```

### 特徴

- **遅延評価**: ファイル全体をメモリに読み込まない
- **クローズ必須**: try-with-resources を使わないとリソースリーク
- **文字エンコーディング**: デフォルトは UTF-8、オーバーロードで指定可能

```java
// UTF-8 以外を指定
try (Stream<String> lines = Files.lines(path, StandardCharsets.SHIFT_JIS)) {
    // 処理
}
```

### Stream API と組み合わせ

```java
// フィルタリング
try (Stream<String> lines = Files.lines(path)) {
    lines.filter(line -> line.startsWith("ERROR"))
         .forEach(System.out::println);
}

// 変換して収集
try (Stream<String> lines = Files.lines(path)) {
    List<String> upperList = lines
        .map(String::toUpperCase)
        .collect(Collectors.toList());
}

// 最初の10行だけ処理
try (Stream<String> lines = Files.lines(path)) {
    lines.limit(10)
         .forEach(System.out::println);
}
```

### 試験ポイント

1. **必ず try-with-resources で囲む**
2. **終端操作が実行されるまで、ファイルは読み込まれない（遅延評価）**
3. **IOException をスローする（検査例外）**

---

## 2. Files.list() - ディレクトリ直下を一覧表示

ディレクトリの直下にあるファイルとサブディレクトリを `Stream<Path>` として返すよ。**再帰しない**のがポイント。

### 基本的な使い方

```java
// ディレクトリ直下のみ表示
try (Stream<Path> paths = Files.list(Paths.get("/home/user"))) {
    paths.forEach(System.out::println);
}
```

### 特徴

- **非再帰**: サブディレクトリの中身は含まれない
- **クローズ必須**: try-with-resources 必須
- **`.` と `..` は含まれない**

### フィルタリング例

```java
// ファイルのみ
try (Stream<Path> paths = Files.list(dir)) {
    paths.filter(Files::isRegularFile)
         .forEach(System.out::println);
}

// ディレクトリのみ
try (Stream<Path> paths = Files.list(dir)) {
    paths.filter(Files::isDirectory)
         .forEach(System.out::println);
}

// .txt ファイルのみ
try (Stream<Path> paths = Files.list(dir)) {
    paths.filter(path -> path.toString().endsWith(".txt"))
         .forEach(System.out::println);
}
```

### 試験ポイント

1. **再帰しない** → サブディレクトリの中身は出ない
2. **必ず try-with-resources**
3. **ディレクトリが存在しない場合は IOException**

---

## 3. Files.walk() - 再帰的なディレクトリ走査

ディレクトリを**再帰的に**走査して、すべてのファイルとディレクトリを `Stream<Path>` として返すよ。

### 基本的な使い方

```java
// すべての階層を再帰的に走査
try (Stream<Path> paths = Files.walk(Paths.get("/home/user"))) {
    paths.forEach(System.out::println);
}
```

### 深さ制限

```java
// 深さ制限: maxDepth = 1 なら Files.list() と同じ
try (Stream<Path> paths = Files.walk(dir, 1)) {
    paths.forEach(System.out::println);
}

// maxDepth = 2 なら2階層まで
try (Stream<Path> paths = Files.walk(dir, 2)) {
    paths.forEach(System.out::println);
}

// すべての階層（デフォルト）
try (Stream<Path> paths = Files.walk(dir, Integer.MAX_VALUE)) {
    paths.forEach(System.out::println);
}
```

### 実用例

```java
// すべての .java ファイルを検索
try (Stream<Path> paths = Files.walk(Paths.get("src"))) {
    paths.filter(Files::isRegularFile)
         .filter(path -> path.toString().endsWith(".java"))
         .forEach(System.out::println);
}

// ファイルサイズの合計
try (Stream<Path> paths = Files.walk(dir)) {
    long totalSize = paths
        .filter(Files::isRegularFile)
        .mapToLong(path -> {
            try {
                return Files.size(path);
            } catch (IOException e) {
                return 0;
            }
        })
        .sum();
    System.out.println("Total: " + totalSize + " bytes");
}
```

### Files.list() との違い

| メソッド | 再帰 | 深さ制限 | 用途 |
|---------|------|---------|------|
| `Files.list()` | ❌ | 常に直下のみ | 1階層だけ見たい |
| `Files.walk()` | ✅ | 可能 | すべて見たい |
| `Files.walk(path, 1)` | ❌ | maxDepth=1 | list() と同等 |

### 試験ポイント

1. **デフォルトで全階層を再帰的に走査**
2. **maxDepth で深さ制限可能**
3. **Files.list() は再帰しないが、Files.walk(path, 1) は同等**
4. **必ず try-with-resources**

---

## 4. Files.find() - 条件付き再帰検索

`Files.walk()` + フィルタリングを一緒にやってくれるメソッドだね。`BiPredicate<Path, BasicFileAttributes>` で条件を指定できるのが特徴。

### シグネチャ

```java
static Stream<Path> find(
    Path start,
    int maxDepth,
    BiPredicate<Path, BasicFileAttributes> matcher
) throws IOException
```

### 基本的な使い方

```java
// .txt ファイルを検索
BiPredicate<Path, BasicFileAttributes> matcher =
    (path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(".txt");

try (Stream<Path> paths = Files.find(dir, Integer.MAX_VALUE, matcher)) {
    paths.forEach(System.out::println);
}
```

### BiPredicate の使い方

`BiPredicate<Path, BasicFileAttributes>` は2つの引数を取る:

1. **Path**: ファイル/ディレクトリのパス
2. **BasicFileAttributes**: ファイル属性（サイズ、作成日時など）

```java
// サイズが1MBより大きいファイル
BiPredicate<Path, BasicFileAttributes> largeMatcher =
    (path, attrs) -> attrs.isRegularFile() && attrs.size() > 1024 * 1024;

try (Stream<Path> paths = Files.find(dir, Integer.MAX_VALUE, largeMatcher)) {
    paths.forEach(System.out::println);
}

// ディレクトリのみ
BiPredicate<Path, BasicFileAttributes> dirMatcher =
    (path, attrs) -> attrs.isDirectory();

try (Stream<Path> paths = Files.find(dir, 10, dirMatcher)) {
    paths.forEach(System.out::println);
}

// 今日更新されたファイル
BiPredicate<Path, BasicFileAttributes> todayMatcher =
    (path, attrs) -> {
        try {
            FileTime today = FileTime.fromMillis(System.currentTimeMillis());
            FileTime modified = attrs.lastModifiedTime();
            return Duration.between(modified.toInstant(), today.toInstant())
                           .toHours() < 24;
        } catch (Exception e) {
            return false;
        }
    };
```

### Files.walk() との比較

```java
// Files.walk() + filter
try (Stream<Path> paths = Files.walk(dir)) {
    paths.filter(Files::isRegularFile)
         .filter(path -> path.toString().endsWith(".txt"))
         .forEach(System.out::println);
}

// Files.find() で同じことを実現
BiPredicate<Path, BasicFileAttributes> matcher =
    (path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(".txt");

try (Stream<Path> paths = Files.find(dir, Integer.MAX_VALUE, matcher)) {
    paths.forEach(System.out::println);
}
```

**違い**: `Files.find()` は `BasicFileAttributes` が使えるから、ファイルサイズや更新日時で効率的にフィルタリングできるんだよね。

### 試験ポイント

1. **BiPredicate<Path, BasicFileAttributes> を使う**
2. **maxDepth は必須パラメータ（Files.walk() と同じ）**
3. **BasicFileAttributes でファイル属性にアクセス可能**
4. **必ず try-with-resources**

---

## 5. BufferedReader.lines() - 代替手段

`Files.lines()` の代替として、`BufferedReader.lines()` も使えるよ。

### 基本的な使い方

```java
// Files.lines() と同等
try (BufferedReader reader = Files.newBufferedReader(path)) {
    reader.lines().forEach(System.out::println);
}
```

### 文字エンコーディングを指定

```java
try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.SHIFT_JIS)) {
    reader.lines().forEach(System.out::println);
}
```

### Files.lines() との比較

| メソッド | 簡潔さ | 柔軟性 | 用途 |
|---------|--------|--------|------|
| `Files.lines(path)` | ✅ | 普通 | シンプルに読みたい |
| `BufferedReader.lines()` | ❌ | ✅ | 細かい制御が必要 |

```java
// Files.lines() のほうが簡潔
try (Stream<String> lines = Files.lines(path)) {
    lines.forEach(System.out::println);
}

// BufferedReader は追加操作が可能
try (BufferedReader reader = Files.newBufferedReader(path)) {
    String firstLine = reader.readLine();  // 最初の行を別途読む
    reader.lines().forEach(System.out::println);  // 残りを Stream で処理
}
```

### 試験ポイント

1. **Files.lines() と同じく try-with-resources 必須**
2. **BufferedReader 自体もクローズ必要**
3. **より細かい制御が必要な場合に使う**

---

## 6. Stream API との組み合わせ

File Stream API は Stream API の全メソッドが使えるから、めっちゃ強力だよ。

### 中間操作

```java
// filter
try (Stream<String> lines = Files.lines(path)) {
    lines.filter(line -> line.contains("ERROR"))
         .forEach(System.out::println);
}

// map
try (Stream<String> lines = Files.lines(path)) {
    lines.map(String::trim)
         .map(String::toUpperCase)
         .forEach(System.out::println);
}

// limit
try (Stream<String> lines = Files.lines(path)) {
    lines.limit(10)  // 最初の10行だけ
         .forEach(System.out::println);
}

// skip
try (Stream<String> lines = Files.lines(path)) {
    lines.skip(1)  // ヘッダー行をスキップ
         .forEach(System.out::println);
}
```

### 終端操作

```java
// collect
try (Stream<String> lines = Files.lines(path)) {
    List<String> list = lines.collect(Collectors.toList());
}

// count
try (Stream<String> lines = Files.lines(path)) {
    long count = lines.filter(line -> line.contains("ERROR"))
                      .count();
}

// findFirst
try (Stream<String> lines = Files.lines(path)) {
    Optional<String> first = lines.filter(line -> line.startsWith("ERROR"))
                                  .findFirst();
}

// anyMatch
try (Stream<String> lines = Files.lines(path)) {
    boolean hasError = lines.anyMatch(line -> line.contains("ERROR"));
}
```

### 複数ファイルを統合

```java
// すべての .log ファイルから ERROR 行を抽出
try (Stream<Path> paths = Files.walk(dir)) {
    List<String> errors = paths
        .filter(Files::isRegularFile)
        .filter(path -> path.toString().endsWith(".log"))
        .flatMap(path -> {
            try {
                return Files.lines(path);
            } catch (IOException e) {
                return Stream.empty();
            }
        })
        .filter(line -> line.contains("ERROR"))
        .collect(Collectors.toList());
}
```

---

## 7. 遅延評価の利点

Stream は**遅延評価**だから、メモリ効率がめっちゃいいんだよね。

### 遅延評価とは

- **中間操作**: `filter()`, `map()` などは即座に実行されない
- **終端操作**: `forEach()`, `collect()` などで初めて実行される

```java
// この時点ではファイルは読まれない
Stream<String> lines = Files.lines(path);

// forEach() で初めてファイルが読まれる
lines.forEach(System.out::println);
```

### メリット

1. **メモリ効率**: ファイル全体をメモリに読み込まない
2. **必要な分だけ処理**: `limit(10)` なら10行しか読まない
3. **パイプライン処理**: フィルタリング→変換→収集を一気に実行

```java
// 巨大なファイルでも最初の10行だけメモリに読む
try (Stream<String> lines = Files.lines(hugeFile)) {
    lines.limit(10)
         .forEach(System.out::println);
}

// 条件に合う最初の1つだけ見つける（残りは読まない）
try (Stream<String> lines = Files.lines(path)) {
    Optional<String> first = lines
        .filter(line -> line.startsWith("ERROR"))
        .findFirst();
}
```

### 注意点

終端操作を実行しないと、何も起こらないよ。

```java
// これは何も実行されない！
try (Stream<String> lines = Files.lines(path)) {
    lines.filter(line -> line.contains("ERROR"));  // 終端操作がない
}

// ✅ 正しい
try (Stream<String> lines = Files.lines(path)) {
    lines.filter(line -> line.contains("ERROR"))
         .forEach(System.out::println);  // 終端操作
}
```

---

## 8. リソースクローズの重要性

これ、試験で一番引っかかるポイントじゃね？

### なぜクローズが必要？

以下のメソッドは内部で**ファイルハンドル**や**ディレクトリストリーム**を開くから、使い終わったら必ずクローズしないとリソースリークするんだよね。

- `Files.lines()`
- `Files.list()`
- `Files.walk()`
- `Files.find()`
- `BufferedReader.lines()`

### ❌ 間違った例

```java
// 間違い1: try-with-resources なし
Stream<String> lines = Files.lines(path);
lines.forEach(System.out::println);
// → リソースリーク！

// 間違い2: Stream を変数に代入してから try
Stream<Path> paths = Files.list(dir);
try (paths) {  // コンパイルエラー！
    paths.forEach(System.out::println);
}
// → try-with-resources は宣言と同時に行う必要がある

// 間違い3: 終端操作を忘れる
try (Stream<String> lines = Files.lines(path)) {
    lines.filter(line -> line.contains("ERROR"));
    // → forEach() がないので何も起こらない
}
```

### ✅ 正しい例

```java
// 正しい1: try-with-resources で宣言と同時
try (Stream<String> lines = Files.lines(path)) {
    lines.forEach(System.out::println);
}

// 正しい2: 複数のストリームをネスト
try (Stream<Path> paths = Files.walk(dir)) {
    paths.filter(Files::isRegularFile)
         .forEach(file -> {
             try (Stream<String> lines = Files.lines(file)) {
                 lines.forEach(System.out::println);
             } catch (IOException e) {
                 e.printStackTrace();
             }
         });
}

// 正しい3: flatMap 内では try-catch
try (Stream<Path> paths = Files.walk(dir)) {
    paths.flatMap(path -> {
        try {
            return Files.lines(path);  // 外側の try で管理
        } catch (IOException e) {
            return Stream.empty();
        }
    })
    .forEach(System.out::println);
}
```

### try-with-resources のルール

1. **変数宣言と同時に try に入れる**
2. **AutoCloseable インターフェースを実装している必要がある**（Stream は実装済み）
3. **複数リソースはセミコロンで区切る**

```java
// 複数リソース
try (Stream<String> lines1 = Files.lines(path1);
     Stream<String> lines2 = Files.lines(path2)) {
    // 処理
}
```

---

## 9. 試験での引っかけポイント

### ポイント1: クローズ忘れ

```java
// ❌ これはリソースリーク
Stream<Path> paths = Files.walk(dir);
paths.forEach(System.out::println);

// ✅ 正しい
try (Stream<Path> paths = Files.walk(dir)) {
    paths.forEach(System.out::println);
}
```

### ポイント2: Files.list() は再帰しない

```java
// Files.list() はサブディレクトリの中身を返さない
try (Stream<Path> paths = Files.list(dir)) {
    paths.forEach(System.out::println);  // 直下のみ
}

// Files.walk() は再帰する
try (Stream<Path> paths = Files.walk(dir)) {
    paths.forEach(System.out::println);  // 全階層
}
```

### ポイント3: maxDepth の意味

```java
// maxDepth = 1 なら Files.list() と同じ
Files.walk(dir, 1);  // 直下のみ

// maxDepth = 2 なら2階層まで
Files.walk(dir, 2);

// デフォルトは Integer.MAX_VALUE（全階層）
Files.walk(dir);
```

### ポイント4: BiPredicate の引数順

```java
// Files.find() の BiPredicate は (Path, BasicFileAttributes)
BiPredicate<Path, BasicFileAttributes> matcher =
    (path, attrs) -> attrs.isRegularFile();  // ✅

BiPredicate<BasicFileAttributes, Path> matcher =
    (attrs, path) -> attrs.isRegularFile();  // ❌ 順番が逆
```

### ポイント5: 終端操作を忘れる

```java
// ❌ 終端操作がないので何も実行されない
try (Stream<String> lines = Files.lines(path)) {
    lines.filter(line -> line.contains("ERROR"));
}

// ✅ 終端操作を追加
try (Stream<String> lines = Files.lines(path)) {
    lines.filter(line -> line.contains("ERROR"))
         .forEach(System.out::println);
}
```

### ポイント6: IOException の処理

```java
// Files.lines(), Files.list(), Files.walk(), Files.find() は
// 全て IOException をスローする（検査例外）

// ❌ try-catch がない
public void readFile(Path path) {
    try (Stream<String> lines = Files.lines(path)) {  // コンパイルエラー
        lines.forEach(System.out::println);
    }
}

// ✅ throws または try-catch
public void readFile(Path path) throws IOException {
    try (Stream<String> lines = Files.lines(path)) {
        lines.forEach(System.out::println);
    }
}
```

---

## 10. まとめ

### 必ず覚えるべきこと

1. **全部 try-with-resources 必須**
   - `Files.lines()`
   - `Files.list()`
   - `Files.walk()`
   - `Files.find()`
   - `BufferedReader.lines()`

2. **再帰の違い**
   - `Files.list()`: 再帰しない（直下のみ）
   - `Files.walk()`: 再帰する（全階層）
   - `Files.find()`: 再帰する（全階層、条件付き）

3. **遅延評価**
   - 終端操作が実行されるまでファイルは読まれない
   - メモリ効率が良い

4. **BiPredicate<Path, BasicFileAttributes>**
   - `Files.find()` で使用
   - ファイル属性で効率的にフィルタリング

### よくある試験問題

**問題1**: 以下のコードの問題は？
```java
Stream<String> lines = Files.lines(path);
lines.forEach(System.out::println);
```
**答え**: try-with-resources で囲んでいないため、リソースリークが発生する。

**問題2**: `Files.list()` と `Files.walk()` の違いは？
**答え**: `Files.list()` は直下のみ、`Files.walk()` は再帰的に全階層を走査する。

**問題3**: `Files.walk(dir, 1)` と `Files.list(dir)` の違いは？
**答え**: 機能的には同等（どちらも直下のみ）だが、`Files.walk()` は maxDepth を指定できる柔軟性がある。

**問題4**: `Files.find()` の第3引数の型は？
**答え**: `BiPredicate<Path, BasicFileAttributes>`

---

これで File Stream API の基礎は完璧だね！試験では特に**リソースクローズ**と**再帰の有無**が頻出だから、そこを重点的に押さえておこう。
