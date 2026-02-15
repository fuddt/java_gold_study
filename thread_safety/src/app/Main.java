package app;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;
import java.util.*;

/**
 * Java Gold スレッドセーフティの総合サンプル
 * このプログラムはスレッドセーフティに関する様々な概念を実演する
 */
public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Java Gold スレッドセーフティ学習プログラム ===\n");

        // 1. synchronizedキーワード（メソッドレベル）
        demonstrateSynchronizedMethod();

        // 2. synchronizedブロック
        demonstrateSynchronizedBlock();

        // 3. volatileキーワード
        demonstrateVolatile();

        // 4. Atomicクラス
        demonstrateAtomic();

        // 5. 並行コレクション
        demonstrateConcurrentCollections();

        // 6. CyclicBarrier
        demonstrateCyclicBarrier();

        // 7. BlockingQueue
        demonstrateBlockingQueue();

        // 8. ReentrantLock
        demonstrateReentrantLock();

        // 9. レースコンディション（同期なし vs あり）
        demonstrateRaceCondition();

        // 10. デッドロック（理論的説明）
        demonstrateDeadlockConcept();

        System.out.println("\n=== すべてのデモが完了しました ===");
    }

    // ==================== 1. synchronized メソッドレベル ====================
    static class SynchronizedCounter {
        private int count = 0;

        // メソッド全体をsynchronizedにする
        public synchronized void increment() {
            count++;
        }

        public synchronized int getCount() {
            return count;
        }
    }

    static void demonstrateSynchronizedMethod() throws InterruptedException {
        System.out.println("【1. synchronized メソッドレベル】");
        SynchronizedCounter counter = new SynchronizedCounter();

        // 複数スレッドで同時にインクリメント
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    counter.increment();
                }
            });
            threads[i].start();
        }

        // すべてのスレッドが終わるまで待つ
        for (Thread t : threads) {
            t.join();
        }

        System.out.println("期待値: 5000, 実際の値: " + counter.getCount());
        System.out.println("→ synchronizedにより、正しくカウントされる\n");
    }

    // ==================== 2. synchronized ブロック ====================
    static class BlockLevelSync {
        private int count1 = 0;
        private int count2 = 0;
        private final Object lock1 = new Object();
        private final Object lock2 = new Object();

        public void increment1() {
            // lock1でのみ同期
            synchronized (lock1) {
                count1++;
            }
        }

        public void increment2() {
            // lock2でのみ同期（異なるロックオブジェクト）
            synchronized (lock2) {
                count2++;
            }
        }

        public int getCount1() {
            synchronized (lock1) {
                return count1;
            }
        }

        public int getCount2() {
            synchronized (lock2) {
                return count2;
            }
        }
    }

    static void demonstrateSynchronizedBlock() throws InterruptedException {
        System.out.println("【2. synchronized ブロックレベル】");
        BlockLevelSync sync = new BlockLevelSync();

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                sync.increment1();
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                sync.increment2();
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("Count1: " + sync.getCount1() + ", Count2: " + sync.getCount2());
        System.out.println("→ 異なるロックオブジェクトで独立した同期が可能\n");
    }

    // ==================== 3. volatile ====================
    static class VolatileExample {
        // volatileを付けないと他のスレッドから変更が見えない可能性がある
        private volatile boolean running = true;

        public void stop() {
            running = false;
        }

        public void doWork() {
            System.out.println("作業開始...");
            long count = 0;
            while (running) {
                count++;
                if (count % 100_000_000 == 0) {
                    System.out.println("作業中... (" + (count / 100_000_000) + ")");
                }
                if (count > 500_000_000) {
                    break; // 安全のための上限
                }
            }
            System.out.println("作業終了 (count: " + count + ")");
        }
    }

    static void demonstrateVolatile() throws InterruptedException {
        System.out.println("【3. volatile キーワード】");
        VolatileExample example = new VolatileExample();

        Thread worker = new Thread(() -> example.doWork());
        worker.start();

        // 少し待ってから停止
        Thread.sleep(100);
        example.stop();
        worker.join(2000); // 最大2秒待つ

        System.out.println("→ volatileにより、フラグの変更が他スレッドから見える\n");
    }

    // ==================== 4. Atomic クラス ====================
    static void demonstrateAtomic() throws InterruptedException {
        System.out.println("【4. Atomic クラス】");

        // AtomicInteger
        AtomicInteger atomicInt = new AtomicInteger(0);
        Thread[] threads = new Thread[5];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    atomicInt.incrementAndGet(); // アトミックな操作
                }
            });
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        System.out.println("AtomicInteger: " + atomicInt.get());

        // AtomicLong
        AtomicLong atomicLong = new AtomicLong(0);
        atomicLong.addAndGet(100);
        System.out.println("AtomicLong: " + atomicLong.get());

        // AtomicReference
        AtomicReference<String> atomicRef = new AtomicReference<>("初期値");
        atomicRef.compareAndSet("初期値", "新しい値"); // CAS操作
        System.out.println("AtomicReference: " + atomicRef.get());

        System.out.println("→ Atomicクラスはロックなしでスレッドセーフな操作を提供\n");
    }

    // ==================== 5. 並行コレクション ====================
    static void demonstrateConcurrentCollections() throws InterruptedException {
        System.out.println("【5. 並行コレクション】");

        // ConcurrentHashMap
        ConcurrentHashMap<String, Integer> concurrentMap = new ConcurrentHashMap<>();
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                concurrentMap.put("key" + i, i);
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 50; i < 150; i++) {
                concurrentMap.put("key" + i, i * 2);
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("ConcurrentHashMap サイズ: " + concurrentMap.size());

        // CopyOnWriteArrayList
        CopyOnWriteArrayList<String> cowList = new CopyOnWriteArrayList<>();
        cowList.add("A");
        cowList.add("B");
        cowList.add("C");

        // イテレート中に追加しても ConcurrentModificationException が発生しない
        for (String s : cowList) {
            if (s.equals("B")) {
                cowList.add("D"); // 安全に追加できる
            }
        }
        System.out.println("CopyOnWriteArrayList: " + cowList);

        // CopyOnWriteArraySet
        CopyOnWriteArraySet<Integer> cowSet = new CopyOnWriteArraySet<>();
        cowSet.add(1);
        cowSet.add(2);
        cowSet.add(2); // 重複は追加されない
        System.out.println("CopyOnWriteArraySet: " + cowSet);

        System.out.println("→ 並行コレクションは複数スレッドからの同時アクセスに対応\n");
    }

    // ==================== 6. CyclicBarrier ====================
    static void demonstrateCyclicBarrier() throws InterruptedException {
        System.out.println("【6. CyclicBarrier】");

        int numberOfThreads = 3;
        CyclicBarrier barrier = new CyclicBarrier(numberOfThreads, () -> {
            System.out.println("→ 全スレッドがバリアに到達！処理を続行します");
        });

        Thread[] threads = new Thread[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadNum = i + 1;
            threads[i] = new Thread(() -> {
                try {
                    System.out.println("スレッド" + threadNum + ": 準備中...");
                    Thread.sleep(threadNum * 100); // 異なる時間待つ
                    System.out.println("スレッド" + threadNum + ": 準備完了、バリアで待機");
                    barrier.await(); // 全スレッドがここで待つ
                    System.out.println("スレッド" + threadNum + ": バリア通過、処理継続");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        System.out.println("→ CyclicBarrierで複数スレッドを同期ポイントで待機させる\n");
    }

    // ==================== 7. BlockingQueue ====================
    static void demonstrateBlockingQueue() throws InterruptedException {
        System.out.println("【7. BlockingQueue (ArrayBlockingQueue)】");

        BlockingQueue<String> queue = new ArrayBlockingQueue<>(5);

        // プロデューサー
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    String item = "Item-" + i;
                    queue.put(item); // キューに追加（満杯なら待機）
                    System.out.println("生産: " + item);
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // コンシューマー
        Thread consumer = new Thread(() -> {
            try {
                Thread.sleep(100); // 少し遅れて開始
                for (int i = 1; i <= 5; i++) {
                    String item = queue.take(); // キューから取得（空なら待機）
                    System.out.println("  消費: " + item);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        consumer.start();
        producer.join();
        consumer.join();

        System.out.println("→ BlockingQueueで生産者-消費者パターンを実装\n");
    }

    // ==================== 8. ReentrantLock ====================
    static class ReentrantLockExample {
        private final ReentrantLock lock = new ReentrantLock();
        private int count = 0;

        public void incrementWithLock() {
            lock.lock(); // ロック取得
            try {
                count++;
            } finally {
                lock.unlock(); // 必ずアンロック
            }
        }

        public boolean tryIncrementWithTimeout() {
            try {
                // 100ms以内にロックを取得できるか試す
                if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
                    try {
                        count++;
                        return true;
                    } finally {
                        lock.unlock();
                    }
                }
                return false;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        public int getCount() {
            lock.lock();
            try {
                return count;
            } finally {
                lock.unlock();
            }
        }
    }

    static void demonstrateReentrantLock() throws InterruptedException {
        System.out.println("【8. ReentrantLock】");
        ReentrantLockExample example = new ReentrantLockExample();

        Thread[] threads = new Thread[3];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    example.incrementWithLock();
                }
            });
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        System.out.println("Count: " + example.getCount());

        // tryLockのデモ
        boolean acquired = example.tryIncrementWithTimeout();
        System.out.println("tryLock成功: " + acquired);

        System.out.println("→ ReentrantLockはsynchronizedより柔軟な制御が可能\n");
    }

    // ==================== 9. レースコンディション ====================
    static class UnsafeCounter {
        private int count = 0;

        public void increment() {
            // スレッドセーフでない！
            count++; // これは実際には3つの操作（読み取り、加算、書き込み）
        }

        public int getCount() {
            return count;
        }
    }

    static void demonstrateRaceCondition() throws InterruptedException {
        System.out.println("【9. レースコンディション】");

        // 同期なし
        UnsafeCounter unsafeCounter = new UnsafeCounter();
        Thread[] threads1 = new Thread[5];

        for (int i = 0; i < threads1.length; i++) {
            threads1[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    unsafeCounter.increment();
                }
            });
            threads1[i].start();
        }

        for (Thread t : threads1) {
            t.join();
        }

        System.out.println("同期なし - 期待値: 5000, 実際: " + unsafeCounter.getCount());
        System.out.println("→ レースコンディションにより値が不正確になる可能性あり");

        // 同期あり
        SynchronizedCounter safeCounter = new SynchronizedCounter();
        Thread[] threads2 = new Thread[5];

        for (int i = 0; i < threads2.length; i++) {
            threads2[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    safeCounter.increment();
                }
            });
            threads2[i].start();
        }

        for (Thread t : threads2) {
            t.join();
        }

        System.out.println("同期あり - 期待値: 5000, 実際: " + safeCounter.getCount());
        System.out.println("→ synchronizedにより正確な値が保証される\n");
    }

    // ==================== 10. デッドロック ====================
    static void demonstrateDeadlockConcept() {
        System.out.println("【10. デッドロックの概念】");
        System.out.println("デッドロックとは：");
        System.out.println("  - スレッドAがロック1を保持してロック2を待つ");
        System.out.println("  - スレッドBがロック2を保持してロック1を待つ");
        System.out.println("  → 両方のスレッドが永遠に待ち続ける状態");
        System.out.println();
        System.out.println("回避方法：");
        System.out.println("  1. ロックの取得順序を統一する");
        System.out.println("  2. tryLock()でタイムアウトを設定する");
        System.out.println("  3. デッドロック検出ツールを使用する");
        System.out.println();

        // 実際のデッドロックは避け、tryLockで安全に実演
        final Object lock1 = new Object();
        final Object lock2 = new Object();

        Thread t1 = new Thread(() -> {
            synchronized (lock1) {
                System.out.println("スレッド1: lock1取得");
                try { Thread.sleep(50); } catch (InterruptedException e) {}
                System.out.println("スレッド1: lock2を取得しようとする...");
                synchronized (lock2) {
                    System.out.println("スレッド1: lock2取得（デッドロック回避）");
                }
            }
        });

        Thread t2 = new Thread(() -> {
            try { Thread.sleep(10); } catch (InterruptedException e) {}
            synchronized (lock1) { // 同じ順序でロック取得（デッドロック回避）
                System.out.println("スレッド2: lock1取得");
                synchronized (lock2) {
                    System.out.println("スレッド2: lock2取得（デッドロック回避）");
                }
            }
        });

        t1.start();
        t2.start();

        try {
            t1.join(1000);
            t2.join(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("→ ロック順序を統一することでデッドロックを回避\n");
    }
}
