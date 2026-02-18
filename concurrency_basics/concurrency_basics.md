# Java Gold 並行処理の基礎

## 1. Runnable vs Callable

### Runnableインターフェース
```java
@FunctionalInterface
public interface Runnable {
    void run();
}
```

- **戻り値なし** (void)
- **チェック例外を投げられない**
- 従来からあるインターフェース
- `Thread`クラスや`ExecutorService`で使える

```java
Runnable task = () -> {
    System.out.println("実行中");
    // return できない！
    // throws できない！
};
```

### Callableインターフェース
```java
@FunctionalInterface
public interface Callable<V> {
    V call() throws Exception;
}
```

- **戻り値あり** (ジェネリック型V)
- **チェック例外を投げられる**
- Java 5から追加
- `ExecutorService`で使える（`Thread`では使えない！）

```java
Callable<Integer> task = () -> {
    Thread.sleep(1000); // チェック例外OK
    return 42; // 戻り値を返せる！
};
```

### 試験ポイント
- **Threadクラスのコンストラクタ**は`Runnable`のみ受け付ける（`Callable`は不可！）
- `ExecutorService.execute()`は`Runnable`のみ（`Callable`は不可！）
- `ExecutorService.submit()`は両方OK

---

## 2. ExecutorServiceの種類と使い分け

### (a) newFixedThreadPool(int nThreads)
```java
ExecutorService executor = Executors.newFixedThreadPool(3);
```

- **固定数のスレッドプール**
- 指定した数のスレッドが再利用される
- タスクが多い場合、キューで待機
- **使いどころ**: タスク数が多く、リソースを制限したい場合

### (b) newSingleThreadExecutor()
```java
ExecutorService executor = Executors.newSingleThreadExecutor();
```

- **単一スレッド**のエグゼキュータ
- タスクは**順番に実行される**（並行実行しない）
- **使いどころ**: タスクの順序を保証したい場合

### (c) newCachedThreadPool()
```java
ExecutorService executor = Executors.newCachedThreadPool();
```

- **必要に応じてスレッドを作成・再利用**
- アイドル状態のスレッドは60秒後に削除
- スレッド数に上限なし（理論上）
- **使いどころ**: 短時間で完了する多数のタスク

### 試験ポイント
- `newFixedThreadPool(1)` と `newSingleThreadExecutor()` の違い:
  - `newSingleThreadExecutor()`は**再構成不可**（スレッド数を変更できない）
  - `newFixedThreadPool(1)`はキャストして変更可能

---

## 3. Futureの使い方

### Future<V>インターフェース
非同期タスクの結果を表すインターフェースである。

```java
Future<Integer> future = executor.submit(callable);
```

### 主要メソッド

#### get() - 結果を取得（ブロッキング！）
```java
Integer result = future.get(); // タスク完了まで待つ
```

- **ブロッキング**: 結果が出るまで待つ
- `ExecutionException`: タスク内で例外が発生
- `InterruptedException`: 待機中に中断された

#### get(long timeout, TimeUnit unit) - タイムアウト付き
```java
try {
    Integer result = future.get(1, TimeUnit.SECONDS);
} catch (TimeoutException e) {
    // 時間内に完了しなかった
}
```

#### isDone() - 完了チェック（ノンブロッキング）
```java
if (future.isDone()) {
    Integer result = future.get(); // すぐ返る
}
```

#### cancel(boolean mayInterruptIfRunning) - キャンセル
```java
future.cancel(true);  // 実行中でも中断を試みる
future.cancel(false); // 未開始のタスクのみキャンセル
```

- キャンセル成功したら`true`を返す
- すでに完了/キャンセル済みなら`false`

#### isCancelled() - キャンセルされたか
```java
if (future.isCancelled()) {
    System.out.println("キャンセルされました");
}
```

### 試験ポイント
- **get()はブロッキング！** これ超重要
- `cancel(true)`しても、タスクが`InterruptedException`をキャッチして無視したら止まらない
- キャンセル後に`get()`を呼ぶと`CancellationException`（チェック例外じゃない！）

---

## 4. submit() vs execute() の違い

### execute(Runnable command)
```java
executor.execute(() -> {
    System.out.println("実行");
});
```

- `Runnable`のみ受け付ける
- **戻り値なし**（void）
- 例外は呼び出し元に伝播しない

### submit()のオーバーロード
```java
// (1) Runnable版
Future<?> future1 = executor.submit(runnable);

// (2) Runnable + 結果値
Future<String> future2 = executor.submit(runnable, "完了");

// (3) Callable版
Future<Integer> future3 = executor.submit(callable);
```

- `Runnable`と`Callable`の両方OK
- **Futureを返す**
- 例外は`Future.get()`を呼んだ時に`ExecutionException`でラップされて投げられる

### 試験ポイント
- `execute()`は`Future`を返さない（結果を取得できない）
- `submit(Runnable)`で返される`Future<?>`の`get()`は`null`を返す
- `submit(Runnable, T result)`の`get()`は指定した`result`を返す

---

## 5. shutdown() vs shutdownNow() の違い

### shutdown()
```java
executor.shutdown();
```

- **新規タスクの受付を停止**
- **実行中のタスクは完了を待つ**
- **キューにあるタスクも実行される**
- 即座には終了しない

### shutdownNow()
```java
List<Runnable> notExecuted = executor.shutdownNow();
```

- **新規タスクの受付を停止**
- **実行中のタスクを中断しようとする**（`Thread.interrupt()`）
- **キューにある未実行タスクをListで返す**
- ベストエフォート（確実な中断は保証されない）

### 関連メソッド

#### isShutdown()
```java
boolean shutdown = executor.isShutdown();
```
- `shutdown()`または`shutdownNow()`が呼ばれたら`true`

#### isTerminated()
```java
boolean terminated = executor.isTerminated();
```
- **全てのタスクが完了したら**`true`
- `shutdown()`の直後は`false`（まだタスク実行中）

#### awaitTermination(long timeout, TimeUnit unit)
```java
boolean terminated = executor.awaitTermination(1, TimeUnit.MINUTES);
```
- 指定時間まで終了を待つ（ブロッキング）
- 時間内に終了したら`true`、タイムアウトしたら`false`
- `shutdown()`と組み合わせて使うのが一般的

### 試験ポイント
- `shutdown()`後に`submit()`すると`RejectedExecutionException`
- `shutdownNow()`しても、タスクが`InterruptedException`をキャッチして無視したら止まらない
- **リソースリーク防止のため、必ずshutdown()を呼ぶ！**（finallyブロックで）

---

## 6. invokeAll() vs invokeAny() の挙動

### invokeAll(Collection<? extends Callable<T>> tasks)
```java
List<Callable<String>> tasks = Arrays.asList(
    () -> "タスク1",
    () -> "タスク2",
    () -> "タスク3"
);

List<Future<String>> futures = executor.invokeAll(tasks);
```

- **全てのタスクを実行**
- **全てが完了するまで待つ**（ブロッキング）
- 結果を`List<Future<T>>`で返す
- タスクの順序は保持される

#### タイムアウト版
```java
List<Future<String>> futures = executor.invokeAll(tasks, 1, TimeUnit.SECONDS);
```
- 時間内に完了しないタスクは自動的にキャンセルされる

### invokeAny(Collection<? extends Callable<T>> tasks)
```java
String result = executor.invokeAny(tasks);
```

- **最初に完了したタスクの結果を返す**
- 他のタスクは**キャンセルされる**
- 結果を直接返す（`Future`じゃない！）
- 全てのタスクが失敗したら`ExecutionException`

#### タイムアウト版
```java
String result = executor.invokeAny(tasks, 1, TimeUnit.SECONDS);
```
- 時間内に1つも完了しなかったら`TimeoutException`

### 試験ポイント
- `invokeAll()`は**全て**完了を待つ
- `invokeAny()`は**1つ**完了したら即座に返す
- `invokeAll()`の返り値: `List<Future<T>>`
- `invokeAny()`の返り値: `T`（直接！）
- どちらも`ExecutorService`をブロックする

---

## 7. ScheduledExecutorServiceの使い方

### 作成
```java
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
```

### (a) schedule() - 遅延実行（1回だけ）
```java
// Runnable版
ScheduledFuture<?> future1 = scheduler.schedule(
    () -> System.out.println("実行"),
    5,
    TimeUnit.SECONDS
);

// Callable版
ScheduledFuture<String> future2 = scheduler.schedule(
    () -> "結果",
    5,
    TimeUnit.SECONDS
);
```
- 指定時間後に**1回だけ**実行

### (b) scheduleAtFixedRate() - 固定レート実行
```java
ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
    () -> System.out.println("定期実行"),
    1,  // 初期遅延
    3,  // 間隔
    TimeUnit.SECONDS
);
```
- **前回の開始時刻から**固定間隔で実行
- タスクが長引いても、次の実行はスキップされない（キューに入る）
- 0秒: 開始
- 1秒: 1回目開始
- 4秒: 2回目開始（1+3秒）
- 7秒: 3回目開始（4+3秒）

### (c) scheduleWithFixedDelay() - 固定遅延実行
```java
ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(
    () -> System.out.println("定期実行"),
    1,  // 初期遅延
    3,  // 遅延
    TimeUnit.SECONDS
);
```
- **前回の完了時刻から**固定間隔で実行
- タスクの実行時間を考慮する
- 0秒: 開始
- 1秒: 1回目開始
- 2秒: 1回目完了（1秒かかった）
- 5秒: 2回目開始（2+3秒）
- 6秒: 2回目完了
- 9秒: 3回目開始（6+3秒）

### ScheduledFuture
```java
future.cancel(false); // キャンセル
long delay = future.getDelay(TimeUnit.SECONDS); // 次回実行までの時間
```

### 試験ポイント
- `scheduleAtFixedRate()`: **開始時刻**基準（タスクが長引くと詰まる可能性）
- `scheduleWithFixedDelay()`: **完了時刻**基準（タスクの実行時間を考慮）
- どちらも`Runnable`のみ（`Callable`は不可！）
- `cancel(false)`で定期実行を停止

---

## 8. よくある引っかけ問題

### Q1: これはコンパイルエラーになるか？
```java
Callable<Integer> callable = () -> 42;
Thread thread = new Thread(callable); // コンパイルエラー！
```
**答え**: エラー！ `Thread`は`Runnable`のみ受け付ける

---

### Q2: この出力は何？
```java
ExecutorService executor = Executors.newSingleThreadExecutor();
Future<?> future = executor.submit(() -> System.out.println("実行"));
System.out.println(future.get()); // 何が出力される？
executor.shutdown();
```
**答え**: `null`（`submit(Runnable)`の`Future.get()`は`null`を返す）

---

### Q3: このコードは正常終了するか？
```java
ExecutorService executor = Executors.newSingleThreadExecutor();
executor.submit(() -> System.out.println("実行"));
// shutdown()を呼ばない
```
**答え**: プログラムが終了しない！（スレッドが残り続ける）

---

### Q4: 何が出力されるか？
```java
ExecutorService executor = Executors.newSingleThreadExecutor();
executor.shutdown();
System.out.println(executor.isTerminated()); // (1)
executor.awaitTermination(1, TimeUnit.SECONDS);
System.out.println(executor.isTerminated()); // (2)
```
**答え**:
- (1): `false`（`shutdown()`直後はまだタスクがあるかも）
- (2): `true`（タスクがなければすぐ終了）

---

### Q5: `invokeAny()`の動作
```java
List<Callable<String>> tasks = Arrays.asList(
    () -> { Thread.sleep(3000); return "遅い"; },
    () -> { Thread.sleep(1000); return "速い"; }
);
String result = executor.invokeAny(tasks);
System.out.println(result);
```
**答え**: `"速い"`（最初に完了したタスクの結果を返す）

---

### Q6: これはどうなる？
```java
Future<Integer> future = executor.submit(() -> {
    Thread.sleep(5000);
    return 42;
});
future.cancel(true);
Integer result = future.get(); // 例外が発生する？
```
**答え**: `CancellationException`が発生（**チェック例外じゃない！**）

---

### Q7: `scheduleAtFixedRate` vs `scheduleWithFixedDelay`
タスクが2秒かかる場合、3秒間隔で実行すると？

**scheduleAtFixedRate**:
- 0秒: 開始
- 2秒: 完了
- 3秒: 開始（前回の**開始**から3秒）
- 5秒: 完了
- 6秒: 開始

**scheduleWithFixedDelay**:
- 0秒: 開始
- 2秒: 完了
- 5秒: 開始（前回の**完了**から3秒）
- 7秒: 完了
- 10秒: 開始

---

## まとめ: 試験で押さえるべきポイント

### 1. インターフェースの違い
- **Runnable**: 戻り値なし、例外投げられない
- **Callable**: 戻り値あり、例外投げられる

### 2. ExecutorServiceの種類
- **Fixed**: スレッド数固定
- **Single**: 1スレッドのみ、順序保証
- **Cached**: 必要に応じて増減

### 3. Futureの罠
- **get()はブロッキング！**
- キャンセル後の`get()`は`CancellationException`

### 4. submit vs execute
- **execute**: Runnableのみ、Futureなし
- **submit**: 両方OK、Futureあり

### 5. shutdownの違い
- **shutdown**: 実行中タスクを待つ
- **shutdownNow**: 中断を試みる、未実行タスクをListで返す

### 6. invokeの違い
- **invokeAll**: 全て完了を待つ、`List<Future<T>>`を返す
- **invokeAny**: 1つ完了で返す、`T`を直接返す

### 7. Scheduledの違い
- **scheduleAtFixedRate**: 開始時刻基準
- **scheduleWithFixedDelay**: 完了時刻基準

### 8. 必ず覚えること
- **ExecutorServiceは必ずshutdown()を呼ぶ！**
- **get()はブロッキング！**
- **ThreadはCallableを受け付けない！**

試験頑張ろう！
