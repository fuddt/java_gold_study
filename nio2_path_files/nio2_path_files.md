# NIO.2 Path と Files - Java Gold 試験対策

## 概要

NIO.2（New I/O 2）はJava 7で導入されたファイルI/O APIである。従来の`java.io.File`クラスより柔軟で強力である。試験では特に`Path`と`Files`クラスの使い方が非常に頻出するため要注意である。

## Path クラスの基本

### Path の作成方法

Path オブジェクトを作る方法は主に2つある。

```java
// 方法1: Path.of() - Java 11以降の推奨方法
Path path1 = Path.of("file.txt");
Path path2 = Path.of("dir", "subdir", "file.txt");  // dir/subdir/file.txt

// 方法2: Paths.get() - 古い方法だが試験に出る
Path path3 = Paths.get("file.txt");
Path path4 = Paths.get("dir", "subdir", "file.txt");
```

**試験ポイント**: `Path.of()`と`Paths.get()`は機能的に同じである。新しいコードでは`Path.of()`を使うべきであるが、試験では両方出る。

### Path の主要メソッド

#### getFileName() - ファイル名取得

パスの最後の要素を返す。

```java
Path path = Path.of("/home/user/file.txt");
System.out.println(path.getFileName());  // file.txt

Path dir = Path.of("/home/user");
System.out.println(dir.getFileName());  // user
```

#### getParent() - 親ディレクトリ取得

親ディレクトリのPathを返す。親がない場合は`null`である。

```java
Path path = Path.of("/home/user/file.txt");
System.out.println(path.getParent());  // /home/user

Path root = Path.of("/");
System.out.println(root.getParent());  // null
```

#### getRoot() - ルート要素取得

ルート要素を返す。相対パスの場合は`null`になるのがポイント！

```java
Path absolute = Path.of("/home/user/file.txt");
System.out.println(absolute.getRoot());  // /（Unixの場合）

Path relative = Path.of("dir/file.txt");
System.out.println(relative.getRoot());  // null
```

**試験の引っかけ**: Windowsでは`C:\`のようなドライブレターがrootになる。

#### getNameCount() と getName(int index)

パスの要素数を取得したり、特定インデックスの要素を取得したりする。

```java
Path path = Path.of("/home/user/docs/file.txt");

System.out.println(path.getNameCount());  // 4
// 注意：ルート「/」はカウントしない！

System.out.println(path.getName(0));  // home
System.out.println(path.getName(1));  // user
System.out.println(path.getName(2));  // docs
System.out.println(path.getName(3));  // file.txt

// IndexOutOfBoundsException注意！
path.getName(4);  // 例外！
```

**超重要な試験ポイント**:
- インデックスは0始まり
- ルート要素はカウントに含まれない
- 範囲外アクセスで`IndexOutOfBoundsException`

#### subpath(int beginIndex, int endIndex)

パスの一部を切り出す。`String.substring()`のような動作である。

```java
Path path = Path.of("/home/user/docs/file.txt");

System.out.println(path.subpath(0, 2));  // home/user
System.out.println(path.subpath(1, 3));  // user/docs
System.out.println(path.subpath(2, 4));  // docs/file.txt
```

**試験の引っかけ**:
- endIndexは含まれない（exclusive）
- ルート要素は含まれない
- 不正な範囲で`IllegalArgumentException`

## resolve() - 超重要！

`resolve()`はパスを結合するメソッドだが、試験で絶対出るから完璧に理解しておくこと！

### 基本的な使い方

```java
Path base = Path.of("/home/user");
Path relative = Path.of("docs/file.txt");

Path result = base.resolve(relative);
System.out.println(result);  // /home/user/docs/file.txt
```

### 超重要な挙動：引数が絶対パスの場合

**これが試験に絶対出る！** 引数が絶対パスの場合、引数がそのまま返るんである。

```java
Path base = Path.of("/home/user");
Path absolute = Path.of("/tmp/file.txt");

Path result = base.resolve(absolute);
System.out.println(result);  // /tmp/file.txt
// baseは無視される！
```

**試験の引っかけパターン**:

```java
Path p1 = Path.of("/data");
Path p2 = Path.of("/backup/file.txt");
Path result = p1.resolve(p2);
// 答え: /backup/file.txt（/data/backup/file.txtではない）
```

### 空文字列の場合

```java
Path base = Path.of("/home/user");
Path empty = Path.of("");

Path result = base.resolve(empty);
System.out.println(result);  // /home/user（baseがそのまま返る）
```

## relativize() - これも超重要！

2つのパス間の相対パスを計算するメソッドである。

### 基本的な使い方

```java
Path p1 = Path.of("/home/user/docs");
Path p2 = Path.of("/home/user/images/photo.jpg");

// p1からp2への相対パス
Path relative = p1.relativize(p2);
System.out.println(relative);  // ../images/photo.jpg

// p2からp1への相対パス
Path relative2 = p2.relativize(p1);
System.out.println(relative2);  // ../../docs
```

### 超重要な制約：両方とも同じ種類のパスが必要

**試験に絶対出る！** 絶対パスと相対パスを混在させると`IllegalArgumentException`が発生する。

```java
Path absolute = Path.of("/home/user/file.txt");
Path relative = Path.of("docs/other.txt");

// これはエラー！
Path result = absolute.relativize(relative);  // IllegalArgumentException

// 逆もエラー！
Path result2 = relative.relativize(absolute);  // IllegalArgumentException
```

**試験対策**: relativize()を使う前に、両方とも絶対パスか両方とも相対パスかチェックすること！

### 同じパスの場合

```java
Path p1 = Path.of("/home/user");
Path p2 = Path.of("/home/user");

Path relative = p1.relativize(p2);
System.out.println(relative);  // ""（空のパス）
```

## normalize() - . と .. の解決

`normalize()`は論理的にパスを正規化するメソッドである。`.`（カレントディレクトリ）と`..`（親ディレクトリ）を解決する。

```java
Path path1 = Path.of("/home/user/./docs");
System.out.println(path1.normalize());  // /home/user/docs

Path path2 = Path.of("/home/user/docs/../images");
System.out.println(path2.normalize());  // /home/user/images

Path path3 = Path.of("/home/./user/../admin/./file.txt");
System.out.println(path3.normalize());  // /home/admin/file.txt
```

### 重要な注意点

1. **ファイルシステムをチェックしない** - 論理的な操作のみ
2. **シンボリックリンクを解決しない** - 実際のファイルシステムを見ない
3. **ファイルの存在を確認しない**

```java
// これは動く（ファイルが存在しなくてもOK）
Path normalized = Path.of("/fake/../real/./file.txt").normalize();
System.out.println(normalized);  // /real/file.txt
```

**試験の引っかけ**: `normalize()`はファイルシステムを見ないため、存在しないパスでも処理できる。`toRealPath()`と混同しないこと。

## toAbsolutePath() vs toRealPath()

この2つは混同しやすいから要注意である！

### toAbsolutePath() - 絶対パスに変換

相対パスを絶対パスに変換する。ファイルの存在チェックはしない。

```java
Path relative = Path.of("file.txt");
Path absolute = relative.toAbsolutePath();
// 例: /Users/username/current-dir/file.txt
```

**特徴**:
- ファイルが存在しなくてもOK
- カレントディレクトリを基準に変換
- `normalize()`は自動的に呼ばれない

### toRealPath() - 実際のパスを取得

ファイルシステム上の実際のパスを返す。**ファイルが存在する必要がある**よ。

```java
Path path = Path.of("file.txt");
Path realPath = path.toRealPath();
// ファイルが存在する必要がある！
```

**特徴**:
- ファイルが存在しないと`NoSuchFileException`
- シンボリックリンクを解決する
- `normalize()`も自動的に適用される
- セキュリティチェックも行われる

**試験の引っかけ**:

```java
// toAbsolutePath() - OK
Path abs = Path.of("nonexistent.txt").toAbsolutePath();

// toRealPath() - NoSuchFileException！
Path real = Path.of("nonexistent.txt").toRealPath();  // 例外！
```

## Files クラスのチェックメソッド

### exists() - 存在確認

```java
Path path = Path.of("file.txt");
boolean exists = Files.exists(path);
```

**注意**: シンボリックリンクの場合、リンク先の存在を確認する。

### isDirectory() - ディレクトリ判定

```java
boolean isDir = Files.isDirectory(path);
```

### isRegularFile() - 通常ファイル判定

```java
boolean isFile = Files.isRegularFile(path);
```

**試験ポイント**: ディレクトリの場合は`false`である！

### その他の便利メソッド

```java
Files.isReadable(path);    // 読み取り可能か
Files.isWritable(path);    // 書き込み可能か
Files.isExecutable(path);  // 実行可能か
Files.isHidden(path);      // 隠しファイルか
```

## ファイル・ディレクトリの作成と削除

### createFile() - ファイル作成

```java
Path file = Path.of("newfile.txt");
Files.createFile(file);
```

**例外**:
- `FileAlreadyExistsException` - すでに存在する場合
- `NoSuchFileException` - 親ディレクトリが存在しない場合

### createDirectory() - ディレクトリ作成

```java
Path dir = Path.of("newdir");
Files.createDirectory(dir);
```

**重要**: 親ディレクトリが存在する必要がある！

### createDirectories() - 複数階層作成

```java
Path nested = Path.of("parent/child/grandchild");
Files.createDirectories(nested);
```

親ディレクトリも含めて全部作ってくれるよ。すでに存在していても例外は発生しない。

**試験の引っかけ**:

```java
// createDirectory() - 親が存在しないとエラー
Files.createDirectory(Path.of("a/b/c"));  // NoSuchFileException

// createDirectories() - OK
Files.createDirectories(Path.of("a/b/c"));  // 成功
```

### delete() - 削除

```java
Path file = Path.of("file.txt");
Files.delete(file);
```

**例外**:
- `NoSuchFileException` - ファイルが存在しない
- `DirectoryNotEmptyException` - ディレクトリが空でない

**超重要**: ディレクトリは空でないと削除できない！

### deleteIfExists() - 存在する場合のみ削除

```java
boolean deleted = Files.deleteIfExists(path);
// 存在しなくても例外なし、戻り値がfalseになるだけ
```

**試験ポイント**: `delete()`と`deleteIfExists()`の違いをしっかり覚えよう！

## copy() と move() - ファイルのコピーと移動

### copy() - 基本的なコピー

```java
Path source = Path.of("source.txt");
Path target = Path.of("target.txt");
Files.copy(source, target);
```

**例外**:
- `FileAlreadyExistsException` - ターゲットがすでに存在する
- `NoSuchFileException` - ソースが存在しない

### move() - 基本的な移動

```java
Path source = Path.of("source.txt");
Path target = Path.of("target.txt");
Files.move(source, target);
```

移動後、元のファイルは存在しなくなる。

## StandardCopyOption - 超重要！

コピー・移動のオプションを指定できる列挙型である。試験によく出る！

### REPLACE_EXISTING - 既存ファイルを上書き

```java
Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
```

これがないと`FileAlreadyExistsException`が発生するけど、これがあれば上書きされるよ。

### COPY_ATTRIBUTES - 属性もコピー

```java
Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
```

ファイルの最終更新時刻などの属性もコピーされる。

### ATOMIC_MOVE - アトミックな移動

```java
Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
```

**重要な制約**:
- `move()`でのみ使える（`copy()`では使えない）
- 同じファイルシステム内でのみ使える
- 移動が途中で失敗しない（オール・オア・ナッシング）

**試験の引っかけ**:

```java
// これはエラー！ATOMIC_MOVEはcopyで使えない
Files.copy(source, target, StandardCopyOption.ATOMIC_MOVE);  // UnsupportedOperationException
```

### 複数オプションの指定

```java
Files.copy(source, target,
    StandardCopyOption.REPLACE_EXISTING,
    StandardCopyOption.COPY_ATTRIBUTES);
```

可変長引数だから複数指定できる。

### ディレクトリのコピー - 超重要な注意点！

```java
Path dirSource = Path.of("sourceDir");
Path dirTarget = Path.of("targetDir");
Files.copy(dirSource, dirTarget);
```

**超重要**: ディレクトリをコピーしても、**中身はコピーされない**！空のディレクトリだけが作られる。

再帰的にコピーしたい場合は、`Files.walk()`などを使って自分で実装する必要がある。

## ファイルの読み書き

### write() - ファイルへの書き込み

```java
List<String> lines = List.of("Line 1", "Line 2", "Line 3");
Path file = Path.of("file.txt");
Files.write(file, lines);
```

**デフォルト動作**:
- ファイルが存在しない場合は作成される
- 存在する場合は上書きされる（内容が消える）

### 追記モード

```java
Files.write(file, lines, StandardOpenOption.APPEND);
```

既存の内容の後ろに追加されるよ。

### readAllLines() - 全行読み込み

```java
List<String> lines = Files.readAllLines(file);
for (String line : lines) {
    System.out.println(line);
}
```

ファイル全体をメモリに読み込むから、大きいファイルには向かないね。

### readString() / writeString() - Java 11+

```java
// 書き込み
Files.writeString(file, "Hello, World!");

// 読み込み
String content = Files.readString(file);
```

文字列として一気に読み書きできる。便利だが大きいファイルには注意！

## ファイル属性の取得

### size() - ファイルサイズ

```java
long size = Files.size(file);  // バイト単位
```

### getLastModifiedTime() - 最終更新時刻

```java
FileTime lastModified = Files.getLastModifiedTime(file);
System.out.println(lastModified);
```

### setLastModifiedTime() - 最終更新時刻の設定

```java
FileTime newTime = FileTime.from(Instant.now());
Files.setLastModifiedTime(file, newTime);
```

### readAttributes() - 複数属性を一度に取得

```java
BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);

System.out.println(attrs.creationTime());
System.out.println(attrs.lastModifiedTime());
System.out.println(attrs.lastAccessTime());
System.out.println(attrs.size());
System.out.println(attrs.isDirectory());
System.out.println(attrs.isRegularFile());
System.out.println(attrs.isSymbolicLink());
```

個別に取得するより効率的である。

## よくある例外まとめ

### NoSuchFileException

ファイルまたはディレクトリが存在しない場合に発生。

```java
Files.delete(Path.of("nonexistent.txt"));  // NoSuchFileException
Files.copy(Path.of("nonexistent.txt"), target);  // NoSuchFileException
Path.of("nonexistent.txt").toRealPath();  // NoSuchFileException
```

### FileAlreadyExistsException

ファイルやディレクトリがすでに存在する場合に発生。

```java
Files.createFile(existingFile);  // FileAlreadyExistsException
Files.copy(source, existingFile);  // FileAlreadyExistsException
// ※ REPLACE_EXISTINGがあれば発生しない
```

### DirectoryNotEmptyException

空でないディレクトリを削除しようとした場合に発生。

```java
Files.delete(nonEmptyDirectory);  // DirectoryNotEmptyException
```

### IllegalArgumentException

不正な引数の場合に発生。

```java
// 絶対パスと相対パスの混在
absolutePath.relativize(relativePath);  // IllegalArgumentException

// 不正なインデックス
path.subpath(-1, 2);  // IllegalArgumentException
path.subpath(2, 1);   // IllegalArgumentException（begin > end）
```

### UnsupportedOperationException

サポートされていない操作を行った場合に発生。

```java
// ATOMIC_MOVEはcopyで使えない
Files.copy(source, target, StandardCopyOption.ATOMIC_MOVE);
```

## 試験でよく出る引っかけポイント

### 1. resolve()で絶対パスが渡された場合

```java
Path p1 = Path.of("/home/user");
Path p2 = Path.of("/tmp/file.txt");
Path result = p1.resolve(p2);
// 答え: /tmp/file.txt（p1は無視される！）
```

### 2. relativize()で異なる種類のパス

```java
Path absolute = Path.of("/home/user");
Path relative = Path.of("file.txt");
Path result = absolute.relativize(relative);  // IllegalArgumentException
```

### 3. getNameCount()でルートは含まれない

```java
Path path = Path.of("/home/user");
System.out.println(path.getNameCount());  // 2（/はカウントしない）
```

### 4. normalize()はファイルシステムを見ない

```java
// ファイルが存在しなくてもOK
Path normalized = Path.of("/fake/dir/../file.txt").normalize();
// 結果: /fake/file.txt
```

### 5. toRealPath()はファイルが必要

```java
Path real = Path.of("nonexistent.txt").toRealPath();  // NoSuchFileException
```

### 6. ディレクトリのコピーは中身をコピーしない

```java
Files.copy(sourceDir, targetDir);
// targetDirは空のディレクトリになる！
```

### 7. delete()とdeleteIfExists()の違い

```java
Files.delete(Path.of("nonexistent.txt"));  // NoSuchFileException
Files.deleteIfExists(Path.of("nonexistent.txt"));  // false（例外なし）
```

### 8. ATOMIC_MOVEはcopyで使えない

```java
Files.copy(source, target, StandardCopyOption.ATOMIC_MOVE);  // UnsupportedOperationException
```

### 9. createDirectory()とcreateDirectories()の違い

```java
Files.createDirectory(Path.of("a/b/c"));  // NoSuchFileException（親が存在しない）
Files.createDirectories(Path.of("a/b/c"));  // OK（親も作成される）
```

### 10. Pathは不変オブジェクト

```java
Path path = Path.of("file.txt");
path.normalize();  // これだけだと意味がない
Path normalized = path.normalize();  // 新しいPathが返る
```

## まとめ：絶対覚えるべきポイント

1. **resolve()**: 引数が絶対パスならその引数がそのまま返る
2. **relativize()**: 両方とも同じ種類（絶対 or 相対）のパスが必要
3. **normalize()**: ファイルシステムを見ない、論理的な操作のみ
4. **toRealPath()**: ファイルが存在する必要がある
5. **getNameCount()**: ルートはカウントしない
6. **ディレクトリのコピー**: 中身はコピーされない
7. **ATOMIC_MOVE**: move()のみで使える、同じファイルシステム内のみ
8. **delete()**: 存在しないとNoSuchFileException
9. **createDirectory()**: 親が必要、createDirectories()は親も作る
10. **StandardCopyOption**: REPLACE_EXISTING, COPY_ATTRIBUTES, ATOMIC_MOVE

これらのポイントを完璧にマスターすれば、NIO.2の問題は余裕である！頑張ろう！
