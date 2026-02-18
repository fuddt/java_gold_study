# Java Gold スレッドセーフティ完全ガイド

## 目次
1. [synchronized の基本](#synchronized-の基本)
2. [volatile キーワード](#volatile-キーワード)
3. [Atomic クラス](#atomic-クラス)
4. [並行コレクション](#並行コレクション)
5. [CyclicBarrier](#cyclicbarrier)
6. [BlockingQueue](#blockingqueue)
7. [ReentrantLock](#reentrantlock)
8. [デッドロックとライブロック](#デッドロックとライブロック)
9. [試験ポイント・引っかけ問題](#試験ポイント引っかけ問題)

---

## synchronized の基本

### 概要
`synchronized`はJavaでスレッドセーフな処理を実装する最も基本的な方法である。複数のスレッドが同時に同じリソースにアクセスするのを防ぐために使うんだ。

### メソッドレベルの synchronized

```java
public class Counter {
    private int count = 0;

    // メソッド全体をロック
    public synchronized void increment() {
        count++;
    }

    public synchronized int getCount() {
        return count;
    }
}
```

**特徴:**
- メソッド全体が同期される
- インスタンスメソッドの場合、`this`オブジェクトをロックとして使用
- staticメソッドの場合、クラスオブジェクト（`ClassName.class`）がロックになる

### ブロックレベルの synchronized

```java
public class BetterCounter {
    private int count = 0;
    private final Object lock = new Object();

    public void increment() {
        // 必要な部分だけロック
        synchronized (lock) {
            count++;
        }
    }
}
```

**利点:**
- 細かい粒度でロックを制御できる
- 複数の独立したロックオブジェクトを使い分けられる
- メソッドレベルより効率的な場合が多い

### メソッドレベル vs ブロックレベル

| 特徴 | メソッドレベル | ブロックレベル |
|------|--------------|--------------|
| 記述の簡潔さ | シンプル | やや複雑 |
| ロックの粒度 | メソッド全体 | 任意の範囲 |
| ロックオブジェクト | this（または Class） | 任意のオブジェクト |
| パフォーマンス | やや遅い可能性 | 最適化しやすい |

**例:**

```java
public class MultiLockExample {
    private int balance1 = 0;
    private int balance2 = 0;
    private final Object lock1 = new Object();
    private final Object lock2 = new Object();

    public void updateBalance1(int amount) {
        synchronized (lock1) {  // lock1だけロック
            balance1 += amount;
        }
    }

    public void updateBalance2(int amount) {
        synchronized (lock2) {  // lock2だけロック（独立して動く）
            balance2 += amount;
        }
    }
}
```

---

## volatile キーワード

### 概要
`volatile`はメモリの可視性を保証するキーワードである。synchronizedみたいにロックはしないけど、他のスレッドからの変更が即座に見えるようにしてくれるんだ。

### 使い方

```java
public class VolatileFlag {
    private volatile boolean running = true;

    public void run() {
        while (running) {
            // 処理
        }
    }

    public void stop() {
        running = false;  // この変更が他スレッドからすぐ見える
    }
}
```

### volatileが必要な理由

CPUキャッシュのせいで、スレッドAが変更した値をスレッドBが見れないことがあるんである。volatileを付けると：

1. 変数の読み書きが必ずメインメモリから行われる
2. CPUキャッシュによる不整合を防ぐ
3. 命令の並び替え（reordering）を制限する

### volatileの制限

```java
private volatile int count = 0;

public void increment() {
    count++;  // これはスレッドセーフじゃない！
}
```

**なぜか？**
- `count++`は実際には3つの操作（読み取り → 加算 → 書き込み）
- volatileは可視性だけを保証する
- アトミック性（不可分性）は保証しない

**正しい方法:**
```java
// synchronizedを使う
public synchronized void increment() {
    count++;
}

// またはAtomicIntegerを使う
private AtomicInteger count = new AtomicInteger(0);
public void increment() {
    count.incrementAndGet();
}
```

### いつ使うか？

**volatileを使う場面:**
- フラグ変数（boolean）
- 状態変数（単純な読み書きのみ）
- ダブルチェックロッキングパターン

**synchronizedを使う場面:**
- 複数の操作をアトミックにしたい
- count++のような複合操作
- 複数の変数を一貫性を保って更新したい

---

## Atomic クラス

### 概要
Atomicクラスはロックを使わずにスレッドセーフな操作を提供するクラス群である。CAS（Compare-And-Swap）という仕組みを使ってるんだ。

### 主要なAtomicクラス

#### AtomicInteger

```java
AtomicInteger counter = new AtomicInteger(0);

// 基本操作
counter.incrementAndGet();  // ++counter
counter.getAndIncrement();  // counter++
counter.decrementAndGet();  // --counter
counter.getAndDecrement();  // counter--
counter.addAndGet(5);       // counter += 5
counter.getAndAdd(5);       // 加算前の値を返す

// CAS操作
counter.compareAndSet(10, 20);  // 10なら20に更新、成功ならtrue
```

#### AtomicLong

```java
AtomicLong longCounter = new AtomicLong(0L);
longCounter.incrementAndGet();
longCounter.addAndGet(100L);
```

#### AtomicBoolean

```java
AtomicBoolean flag = new AtomicBoolean(false);
flag.set(true);
boolean oldValue = flag.getAndSet(false);  // trueを返してfalseに設定
```

#### AtomicReference

```java
AtomicReference<String> ref = new AtomicReference<>("初期値");
ref.set("新しい値");
ref.compareAndSet("新しい値", "更新後の値");

// 複雑なオブジェクトも扱える
AtomicReference<User> userRef = new AtomicReference<>(new User("太郎"));
userRef.updateAndGet(user -> new User("次郎"));
```

### CAS（Compare-And-Swap）とは？

```java
// CASの疑似コード
boolean compareAndSet(int expectedValue, int newValue) {
    if (currentValue == expectedValue) {
        currentValue = newValue;
        return true;  // 成功
    }
    return false;  // 失敗（他のスレッドが変更済み）
}
```

**特徴:**
- ロックフリー（ロックを使わない）
- 他のスレッドが変更していたら失敗する
- 失敗したら再試行する（楽観的ロック）

### Atomic vs synchronized

| 特徴 | Atomic | synchronized |
|------|--------|-------------|
| ロック | 不要 | 必要 |
| パフォーマンス | 高速 | やや遅い |
| 複数変数の同期 | 難しい | 簡単 |
| 用途 | 単一変数の操作 | 複雑な処理 |

**使い分け:**

```java
// Atomicが適している
private AtomicInteger counter = new AtomicInteger(0);
public void increment() {
    counter.incrementAndGet();
}

// synchronizedが適している
public synchronized void transferMoney(Account from, Account to, int amount) {
    from.balance -= amount;  // 複数の操作を
    to.balance += amount;    // 一貫性を保って実行
}
```

---

## 並行コレクション

### 概要
通常のコレクション（ArrayList、HashMapなど）はスレッドセーフじゃないんである。並行コレクションは複数スレッドから安全にアクセスできるように設計されてるんだ。

### ConcurrentHashMap

```java
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

// 複数スレッドから同時アクセス可能
map.put("key1", 100);
map.get("key1");
map.remove("key1");

// アトミックな操作
map.putIfAbsent("key2", 200);  // なければ追加
map.replace("key2", 200, 300);  // 200なら300に置換

// 便利なメソッド
map.computeIfAbsent("key3", k -> 100);  // なければ計算して追加
map.merge("key4", 50, Integer::sum);    // 値をマージ
```

**特徴:**
- セグメント単位でロック（細かい粒度）
- nullキー・null値は不可
- HashMapより高速な並行アクセス

**試験ポイント:**
```java
// これはNG！
map.put(null, 100);           // NullPointerException
map.put("key", null);         // NullPointerException

// サイズは正確じゃない可能性がある
int size = map.size();  // 概算値の場合がある
```

### CopyOnWriteArrayList

```java
CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
list.add("A");
list.add("B");
list.add("C");

// イテレート中に追加してもOK
for (String s : list) {
    if (s.equals("B")) {
        list.add("D");  // ConcurrentModificationExceptionが出ない
    }
}
```

**仕組み:**
- 書き込み時に内部配列をコピー
- 読み取りはロック不要
- イテレータは作成時のスナップショット

**使い所:**
- 読み取りが多く、書き込みが少ない場合
- イテレート中に変更が必要な場合

**注意点:**
- 書き込みコストが高い
- メモリを多く使う

### CopyOnWriteArraySet

```java
CopyOnWriteArraySet<Integer> set = new CopyOnWriteArraySet<>();
set.add(1);
set.add(2);
set.add(2);  // 重複は追加されない

System.out.println(set);  // [1, 2]
```

**特徴:**
- CopyOnWriteArrayListベース
- 重複を許さない
- 読み取り重視の設計

### Collections.synchronizedXxx vs 並行コレクション

```java
// 古い方法（推奨しない）
List<String> syncList = Collections.synchronizedList(new ArrayList<>());
synchronized (syncList) {
    for (String s : syncList) {  // 外部でロックが必要
        System.out.println(s);
    }
}

// 推奨される方法
CopyOnWriteArrayList<String> concurrentList = new CopyOnWriteArrayList<>();
for (String s : concurrentList) {  // ロック不要
    System.out.println(s);
}
```

### 並行コレクションの選び方

| 要件 | 推奨コレクション |
|------|---------------|
| Map、並行性高い | ConcurrentHashMap |
| List、読み取り多い | CopyOnWriteArrayList |
| Set、読み取り多い | CopyOnWriteArraySet |
| Queue、生産者-消費者 | BlockingQueue |
| Deque、両端操作 | ConcurrentLinkedDeque |

---

## CyclicBarrier

### 概要
CyclicBarrierは複数のスレッドを同期ポイントで待ち合わせるための仕組みである。全スレッドがバリアに到達するまで待つんだ。

### 基本的な使い方

```java
int numberOfThreads = 3;

// 全スレッドが到達したときに実行されるアクション
CyclicBarrier barrier = new CyclicBarrier(numberOfThreads, () -> {
    System.out.println("全員集合！処理開始");
});

for (int i = 0; i < numberOfThreads; i++) {
    new Thread(() -> {
        try {
            System.out.println("準備中...");
            Thread.sleep(1000);
            System.out.println("準備完了、待機");
            barrier.await();  // ここで待つ
            System.out.println("処理開始");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }).start();
}
```

**実行結果:**
```
準備中...
準備中...
準備中...
準備完了、待機
準備完了、待機
準備完了、待機
全員集合！処理開始
処理開始
処理開始
処理開始
```

### タイムアウト付き

```java
try {
    barrier.await(5, TimeUnit.SECONDS);  // 5秒待つ
} catch (TimeoutException e) {
    System.out.println("タイムアウト");
}
```

### リセットと再利用

```java
CyclicBarrier barrier = new CyclicBarrier(3);

// 1回目の使用
barrier.await();
barrier.await();
barrier.await();  // バリア解放

// 2回目の使用（自動的にリセットされる）
barrier.await();
barrier.await();
barrier.await();  // また使える

// 手動リセット
barrier.reset();
```

### CountDownLatch との違い

| 特徴 | CyclicBarrier | CountDownLatch |
|------|--------------|---------------|
| 再利用 | 可能 | 不可（1回のみ） |
| 待機方法 | 全スレッドがawait() | countdown()を待つ |
| バリアアクション | あり | なし |
| 用途 | スレッド間の同期 | 初期化完了を待つ |

**CountDownLatchの例:**
```java
CountDownLatch latch = new CountDownLatch(3);

// 3つのタスクを起動
for (int i = 0; i < 3; i++) {
    new Thread(() -> {
        // 処理
        latch.countDown();  // カウント減らす
    }).start();
}

latch.await();  // カウントが0になるまで待つ
System.out.println("全タスク完了");
```

---

## BlockingQueue

### 概要
BlockingQueueは生産者-消費者パターンを実装するための並行キューである。キューが満杯/空のときに自動的にブロックしてくれるんだ。

### 主要な実装クラス

#### ArrayBlockingQueue

```java
// 容量5の有界キュー
BlockingQueue<String> queue = new ArrayBlockingQueue<>(5);

// 生産者
new Thread(() -> {
    try {
        queue.put("Item1");  // 追加（満杯なら待機）
        queue.put("Item2");
        queue.put("Item3");
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}).start();

// 消費者
new Thread(() -> {
    try {
        String item = queue.take();  // 取得（空なら待機）
        System.out.println(item);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}).start();
```

#### LinkedBlockingQueue

```java
// 容量無制限（実質的にはInteger.MAX_VALUE）
BlockingQueue<Integer> queue = new LinkedBlockingQueue<>();

// または容量指定
BlockingQueue<Integer> boundedQueue = new LinkedBlockingQueue<>(100);
```

### 主要メソッド

| 操作 | 例外を投げる | 特殊値を返す | ブロック | タイムアウト |
|------|------------|-----------|---------|------------|
| 追加 | add(e) | offer(e) | put(e) | offer(e, time, unit) |
| 削除 | remove() | poll() | take() | poll(time, unit) |
| 確認 | element() | peek() | - | - |

**例:**
```java
BlockingQueue<String> queue = new ArrayBlockingQueue<>(3);

// add: 満杯なら例外
queue.add("A");
queue.add("B");
queue.add("C");
// queue.add("D");  // IllegalStateException

// offer: 満杯ならfalse
boolean success = queue.offer("D");  // false

// put: 満杯なら待機
queue.put("E");  // 空きができるまでブロック

// offerタイムアウト付き
boolean added = queue.offer("F", 1, TimeUnit.SECONDS);
```

### 生産者-消費者パターンの完全な例

```java
public class ProducerConsumerExample {
    public static void main(String[] args) {
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(10);

        // 生産者
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 20; i++) {
                    queue.put(i);
                    System.out.println("生産: " + i);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // 消費者（複数）
        Thread consumer1 = new Thread(createConsumer(queue, "消費者1"));
        Thread consumer2 = new Thread(createConsumer(queue, "消費者2"));

        producer.start();
        consumer1.start();
        consumer2.start();
    }

    static Runnable createConsumer(BlockingQueue<Integer> queue, String name) {
        return () -> {
            try {
                while (true) {
                    Integer item = queue.take();
                    System.out.println(name + " 消費: " + item);
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
    }
}
```

---

## ReentrantLock

### 概要
ReentrantLockはsynchronizedより柔軟なロック機構である。tryLock、タイムアウト、割り込み可能なロックなど、高度な機能があるんだ。

### 基本的な使い方

```java
public class LockExample {
    private final ReentrantLock lock = new ReentrantLock();
    private int count = 0;

    public void increment() {
        lock.lock();  // ロック取得
        try {
            count++;
        } finally {
            lock.unlock();  // 必ずfinallyでアンロック
        }
    }
}
```

**重要:** finallyブロックでunlock()を呼ぶのは必須である。例外が発生してもロックを解放しないとデッドロックになっちゃう。

### tryLock - ノンブロッキング取得

```java
public boolean tryIncrement() {
    if (lock.tryLock()) {  // すぐ取得を試みる
        try {
            count++;
            return true;
        } finally {
            lock.unlock();
        }
    }
    return false;  // 取得できなかった
}
```

### tryLock - タイムアウト付き

```java
public boolean tryIncrementWithTimeout() {
    try {
        // 3秒以内にロックを取得
        if (lock.tryLock(3, TimeUnit.SECONDS)) {
            try {
                count++;
                return true;
            } finally {
                lock.unlock();
            }
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
    return false;
}
```

### 割り込み可能なロック

```java
public void incrementInterruptibly() throws InterruptedException {
    lock.lockInterruptibly();  // 割り込み可能
    try {
        count++;
    } finally {
        lock.unlock();
    }
}
```

### 公平性（Fairness）

```java
// 公平なロック（待ち時間が長いスレッドを優先）
ReentrantLock fairLock = new ReentrantLock(true);

// 不公平なロック（デフォルト、パフォーマンス重視）
ReentrantLock unfairLock = new ReentrantLock(false);
```

**公平性の違い:**
- **公平（fair）**: 待ち時間順にロックを取得、スレッドの飢餓を防ぐ
- **不公平（unfair）**: スループット重視、性能が良い

### Condition - 待機と通知

```java
public class ConditionExample {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private boolean ready = false;

    public void waitForReady() throws InterruptedException {
        lock.lock();
        try {
            while (!ready) {
                condition.await();  // wait()に相当
            }
        } finally {
            lock.unlock();
        }
    }

    public void setReady() {
        lock.lock();
        try {
            ready = true;
            condition.signalAll();  // notifyAll()に相当
        } finally {
            lock.unlock();
        }
    }
}
```

### ReentrantLock vs synchronized

| 特徴 | ReentrantLock | synchronized |
|------|--------------|-------------|
| 構文 | 明示的なlock/unlock | 自動的 |
| tryLock | あり | なし |
| タイムアウト | あり | なし |
| 割り込み可能 | あり | なし |
| 公平性 | 選択可能 | 不公平 |
| Condition | 複数可能 | 1つのみ |
| パフォーマンス | 同等 | 同等 |
| コードの安全性 | unlock忘れのリスク | 安全 |

**使い分け:**

```java
// synchronizedで十分な場合
public synchronized void simpleMethod() {
    count++;
}

// ReentrantLockが必要な場合
public void complexMethod() {
    if (lock.tryLock(1, TimeUnit.SECONDS)) {  // タイムアウトが必要
        try {
            // 処理
        } finally {
            lock.unlock();
        }
    }
}
```

### ReadWriteLock

読み取りと書き込みで異なるロックを使えるんだ。

```java
public class ReadWriteLockExample {
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();
    private int value = 0;

    public int read() {
        readLock.lock();  // 読み取りロック（複数同時可）
        try {
            return value;
        } finally {
            readLock.unlock();
        }
    }

    public void write(int newValue) {
        writeLock.lock();  // 書き込みロック（排他的）
        try {
            value = newValue;
        } finally {
            writeLock.unlock();
        }
    }
}
```

**特徴:**
- 複数スレッドが同時に読み取り可能
- 書き込み中は他のすべての操作がブロック
- 読み取りが多い場合に効率的

---

## デッドロックとライブロック

### デッドロック（Deadlock）

#### 概要
デッドロックは複数のスレッドがお互いにロックを待ち続ける状態である。誰も進めなくなっちゃうんだ。

#### 典型的なデッドロックの例

```java
public class DeadlockExample {
    private final Object lock1 = new Object();
    private final Object lock2 = new Object();

    public void method1() {
        synchronized (lock1) {
            System.out.println("Thread1: lock1取得");
            Thread.sleep(100);
            synchronized (lock2) {  // lock2待ち
                System.out.println("Thread1: lock2取得");
            }
        }
    }

    public void method2() {
        synchronized (lock2) {
            System.out.println("Thread2: lock2取得");
            Thread.sleep(100);
            synchronized (lock1) {  // lock1待ち
                System.out.println("Thread2: lock1取得");
            }
        }
    }
}
```

**何が起きるか:**
1. スレッド1がlock1を取得
2. スレッド2がlock2を取得
3. スレッド1がlock2を待つ（スレッド2が保持中）
4. スレッド2がlock1を待つ（スレッド1が保持中）
5. 両方とも永遠に待ち続ける

#### デッドロックの条件（4つすべて揃うと発生）

1. **相互排除（Mutual Exclusion）**: リソースを排他的に使用
2. **保持待機（Hold and Wait）**: ロックを保持したまま他のロックを待つ
3. **非プリエンプション（No Preemption）**: ロックを強制的に奪えない
4. **循環待機（Circular Wait）**: スレッドが循環的に待機

#### デッドロック回避策

**1. ロック順序の統一**

```java
public class NoDeadlock {
    private final Object lock1 = new Object();
    private final Object lock2 = new Object();

    public void method1() {
        synchronized (lock1) {  // 常にlock1 → lock2の順
            synchronized (lock2) {
                // 処理
            }
        }
    }

    public void method2() {
        synchronized (lock1) {  // 同じ順序
            synchronized (lock2) {
                // 処理
            }
        }
    }
}
```

**2. tryLockでタイムアウト**

```java
public boolean safeMethod() {
    Lock lock1 = new ReentrantLock();
    Lock lock2 = new ReentrantLock();

    try {
        if (lock1.tryLock(1, TimeUnit.SECONDS)) {
            try {
                if (lock2.tryLock(1, TimeUnit.SECONDS)) {
                    try {
                        // 処理
                        return true;
                    } finally {
                        lock2.unlock();
                    }
                }
            } finally {
                lock1.unlock();
            }
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
    return false;
}
```

**3. すべてのロックを一度に取得**

```java
// ロック取得専用メソッド
public boolean acquireLocks(Lock... locks) {
    for (Lock lock : locks) {
        if (!lock.tryLock()) {
            // 失敗したら取得済みのロックをすべて解放
            for (Lock acquiredLock : locks) {
                if (acquiredLock.isLocked()) {
                    acquiredLock.unlock();
                }
            }
            return false;
        }
    }
    return true;
}
```

### ライブロック（Livelock）

#### 概要
ライブロックはスレッドが互いに譲り合って結果的に進まない状態である。デッドロックと違って、スレッドはアクティブに動いてるんだが。

#### 例え話
廊下で向かい合った2人が「どうぞどうぞ」とお互いに譲り合って、結局誰も進めない状態ではないだろうか

#### コード例

```java
public class LivelockExample {
    static class Spoon {
        private Diner owner;

        public synchronized void use() {
            System.out.println(owner.name + "がスプーンで食事中");
        }

        public synchronized void setOwner(Diner d) {
            owner = d;
        }

        public synchronized Diner getOwner() {
            return owner;
        }
    }

    static class Diner {
        private String name;
        private boolean isHungry;

        public void eatWith(Spoon spoon, Diner spouse) {
            while (isHungry) {
                // 相手がお腹空いてたらスプーンを譲る
                if (spoon.getOwner() != this) {
                    try { Thread.sleep(1); } catch (InterruptedException e) {}
                    continue;
                }

                if (spouse.isHungry) {
                    System.out.println(name + ": 相手がお腹空いてるから譲るよ");
                    spoon.setOwner(spouse);
                    continue;  // 譲っても相手も譲るのでライブロック
                }

                spoon.use();
                isHungry = false;
            }
        }
    }
}
```

#### ライブロック回避策

1. **ランダムな待機時間を入れる**
```java
Thread.sleep(random.nextInt(100));  // ランダムな時間待つ
```

2. **優先度を設定する**
```java
if (this.priority > other.priority) {
    // 優先度が高い方が先に実行
}
```

### スレッドの飢餓（Starvation）

優先度が低いスレッドがずっと実行されない状態である。

```java
// 公平なロックで回避
ReentrantLock fairLock = new ReentrantLock(true);
```

---

## 試験ポイント・引っかけ問題

### 1. synchronized関連

#### Q1: このコードはスレッドセーフ？

```java
public class Counter {
    private int count = 0;

    public void increment() {
        synchronized (this) {
            count++;
        }
    }

    public int getCount() {
        return count;  // synchronizedなし
    }
}
```

**答え:** スレッドセーフじゃない！getCount()もsynchronizedが必要である。

**正解:**
```java
public synchronized int getCount() {
    return count;
}
```

#### Q2: static synchronizedのロックオブジェクトは？

```java
public class MyClass {
    public static synchronized void method() {
        // 何がロックされる？
    }
}
```

**答え:** `MyClass.class`（クラスオブジェクト）がロックされるよ。

#### Q3: 異なるオブジェクトでロックできる？

```java
public void method1() {
    synchronized (new Object()) {  // これはNG
        count++;
    }
}
```

**答え:** 毎回新しいオブジェクトを作ってるからロックにならない！同じオブジェクトを使わないとダメである。

### 2. volatile関連

#### Q4: volatileで十分？

```java
private volatile int count = 0;

public void increment() {
    count++;  // スレッドセーフ？
}
```

**答え:** スレッドセーフじゃない！`count++`は3つの操作（読み取り、加算、書き込み）だから、synchronizedかAtomicIntegerが必要である。

#### Q5: volatileが必要な場面は？

```java
// パターン1: フラグ
private volatile boolean running = true;

// パターン2: ダブルチェックロッキング
private volatile MyClass instance;

// パターン3: 単純な状態変数
private volatile int status = 0;
```

**答え:** 単純な読み書きのみの場合にvolatileを使うよ。

### 3. Atomic関連

#### Q6: AtomicIntegerの正しい使い方は？

```java
// A
atomicInt.incrementAndGet();

// B
atomicInt.set(atomicInt.get() + 1);
```

**答え:** Aが正解。Bは2つの操作に分かれてるからアトミックじゃないよ。

#### Q7: compareAndSetの動作は？

```java
AtomicInteger ai = new AtomicInteger(10);
boolean result = ai.compareAndSet(10, 20);
```

**答え:**
- 現在の値が10なら20に更新してtrueを返す
- 現在の値が10じゃなければ何もせずfalseを返す

### 4. 並行コレクション関連

#### Q8: ConcurrentHashMapでnullは使える？

```java
ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
map.put(null, "value");  // どうなる？
map.put("key", null);    // どうなる？
```

**答え:** どっちも`NullPointerException`が発生する！ConcurrentHashMapはnullキー・null値を許可しないんだ。

#### Q9: CopyOnWriteArrayListの特徴は？

```java
CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
list.add("A");

for (String s : list) {
    list.add("B");  // これは安全？
}
```

**答え:** 安全である！イテレータ作成時のスナップショットを使うから`ConcurrentModificationException`は発生しないんだ。

### 5. ReentrantLock関連

#### Q10: unlock()を忘れたら？

```java
lock.lock();
doSomething();
lock.unlock();  // 例外が発生したら？
```

**答え:** 例外が発生したらunlock()が呼ばれない。デッドロックの原因になる。必ずfinallyブロックで呼ぶべきである。

**正解:**
```java
lock.lock();
try {
    doSomething();
} finally {
    lock.unlock();
}
```

#### Q11: tryLockの戻り値は？

```java
if (lock.tryLock()) {
    try {
        // 処理
    } finally {
        lock.unlock();
    }
} else {
    // ロック取得失敗
}
```

**答え:**
- ロック取得成功 → `true`
- ロック取得失敗 → `false`

### 6. BlockingQueue関連

#### Q12: put()とoffer()の違いは？

```java
BlockingQueue<String> queue = new ArrayBlockingQueue<>(1);
queue.put("A");
queue.put("B");   // どうなる？

queue.offer("C"); // どうなる？
```

**答え:**
- `put("B")` → キューが満杯なので、空きができるまでブロック
- `offer("C")` → キューが満杯なので、すぐにfalseを返す

#### Q13: take()とpoll()の違いは？

```java
BlockingQueue<String> queue = new ArrayBlockingQueue<>(1);
String s1 = queue.take();  // どうなる？
String s2 = queue.poll();  // どうなる？
```

**答え:**
- `take()` → キューが空なので、要素が追加されるまでブロック
- `poll()` → キューが空なので、すぐにnullを返す

### 7. デッドロック関連

#### Q14: デッドロックが発生する？

```java
// スレッド1
synchronized (lockA) {
    synchronized (lockB) {
        // 処理
    }
}

// スレッド2
synchronized (lockB) {
    synchronized (lockA) {
        // 処理
    }
}
```

**答え:** デッドロックが発生する可能性が高いよ！ロックの取得順序が逆だ。

**回避策:**
```java
// 両方とも同じ順序にする
synchronized (lockA) {
    synchronized (lockB) {
        // 処理
    }
}
```

### 8. よくある間違い

#### パターン1: Double-Checked Locking（古い書き方）

```java
// 間違い
if (instance == null) {
    synchronized (MyClass.class) {
        if (instance == null) {
            instance = new MyClass();  // volatileなしだと危険
        }
    }
}

// 正しい
private volatile MyClass instance;
if (instance == null) {
    synchronized (MyClass.class) {
        if (instance == null) {
            instance = new MyClass();
        }
    }
}
```

#### パターン2: 間違ったロックオブジェクト

```java
// 間違い
synchronized (count) {  // intはロックできない
    count++;
}

// 間違い
synchronized ("lock") {  // 文字列リテラルは危険（インターン）
    // 処理
}

// 正しい
private final Object lock = new Object();
synchronized (lock) {
    count++;
}
```

#### パターン3: synchronizedスコープの勘違い

```java
public void method() {
    synchronized (this) {
        int local = count;
    }
    local++;  // これはsynchronizedの外！
    count = local;  // 危険
}
```

---

## まとめ：スレッドセーフティの選び方フローチャート

```
スレッドセーフな処理が必要？
  ↓ YES
単純なカウンター？
  ↓ YES → AtomicInteger/AtomicLong
  ↓ NO
フラグや状態変数（単純な読み書き）？
  ↓ YES → volatile
  ↓ NO
複数の操作をアトミックに実行したい？
  ↓ YES
  |
  タイムアウトや条件待機が必要？
    ↓ YES → ReentrantLock + Condition
    ↓ NO → synchronized
  ↓
コレクションを使う？
  ↓ YES
  |
  Mapが必要？
    ↓ YES → ConcurrentHashMap
  ↓
  Listが必要？
    ↓ YES
    |
    読み取り多い？
      ↓ YES → CopyOnWriteArrayList
      ↓ NO → Collections.synchronizedList
  ↓
  生産者-消費者パターン？
    ↓ YES → BlockingQueue (ArrayBlockingQueue)
  ↓
スレッド間の同期ポイントが必要？
  ↓ YES → CyclicBarrier / CountDownLatch
```

---

## 試験前チェックリスト

- [ ] synchronizedのロックオブジェクトを理解してる？（this vs クラスオブジェクト）
- [ ] volatileはアトミック性を保証しないことを理解してる？
- [ ] Atomicクラスのメソッド名を覚えてる？（incrementAndGet vs getAndIncrement）
- [ ] ConcurrentHashMapはnull不可を覚えてる？
- [ ] CopyOnWriteArrayListは書き込みコストが高いことを理解してる？
- [ ] BlockingQueueのメソッドの違いを理解してる？（put vs offer, take vs poll）
- [ ] ReentrantLockは必ずfinallyでunlockすることを理解してる？
- [ ] デッドロックの4条件を覚えてる？
- [ ] デッドロックの回避方法を説明できる？
- [ ] CyclicBarrierとCountDownLatchの違いを理解してる？

頑張ろう！
