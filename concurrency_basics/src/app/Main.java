package app;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== 並行処理の基礎 ===\n");

        // 1. Runnableの基本
        demonstrateRunnable();

        // 2. Callableの基本
        demonstrateCallable();

        // 3. ExecutorServiceの種類
        demonstrateExecutorTypes();

        // 4. Futureの使い方
        demonstrateFuture();

        // 5. submit() vs execute()
        demonstrateSubmitVsExecute();

        // 6. invokeAll() と invokeAny()
        demonstrateInvokeAllAndAny();

        // 7. ScheduledExecutorService
        demonstrateScheduledExecutor();

        // 8. ExecutorServiceのライフサイクル
        demonstrateLifecycle();

        System.out.println("\n=== 全てのデモ完了 ===");
    }

    // 1. Runnableインターフェースのデモ
    // Runnable: 戻り値なし、チェック例外を投げられない
    private static void demonstrateRunnable() {
        System.out.println("--- 1. Runnable の基本 ---");

        // 従来の方法: Threadクラスを使用
        Runnable task = () -> {
            System.out.println("Runnable実行中: " + Thread.currentThread().getName());
        };

        Thread thread = new Thread(task);
        thread.start();

        try {
            thread.join(); // スレッドの終了を待つ
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println();
    }

    // 2. Callableインターフェースのデモ
    // Callable<V>: 戻り値あり、チェック例外を投げられる
    private static void demonstrateCallable() {
        System.out.println("--- 2. Callable の基本 ---");

        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Callable<Integer>: Integer型の値を返す
        Callable<Integer> task = () -> {
            System.out.println("Callable実行中...");
            Thread.sleep(1000);
            return 42; // 戻り値を返せる！
        };

        try {
            Future<Integer> future = executor.submit(task);
            Integer result = future.get(); // ブロッキング！結果が出るまで待つ
            System.out.println("Callableの結果: " + result);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown(); // 重要: リソースを解放
        }

        System.out.println();
    }

    // 3. ExecutorServiceの種類
    private static void demonstrateExecutorTypes() {
        System.out.println("--- 3. ExecutorService の種類 ---");

        // (a) newFixedThreadPool: 固定数のスレッドプール
        ExecutorService fixed = Executors.newFixedThreadPool(3);
        System.out.println("FixedThreadPool(3): 最大3つのスレッドが同時実行");
        for (int i = 0; i < 5; i++) {
            final int taskId = i;
            fixed.submit(() -> {
                System.out.println("  Fixed Task " + taskId + " - " + Thread.currentThread().getName());
            });
        }
        fixed.shutdown();
        awaitTermination(fixed);

        // (b) newSingleThreadExecutor: 単一スレッドのエグゼキュータ
        ExecutorService single = Executors.newSingleThreadExecutor();
        System.out.println("\nSingleThreadExecutor: タスクは順番に実行される");
        for (int i = 0; i < 3; i++) {
            final int taskId = i;
            single.submit(() -> {
                System.out.println("  Single Task " + taskId + " - " + Thread.currentThread().getName());
            });
        }
        single.shutdown();
        awaitTermination(single);

        // (c) newCachedThreadPool: 必要に応じてスレッドを作成・再利用
        ExecutorService cached = Executors.newCachedThreadPool();
        System.out.println("\nCachedThreadPool: 必要に応じてスレッド数が変動");
        for (int i = 0; i < 3; i++) {
            final int taskId = i;
            cached.submit(() -> {
                System.out.println("  Cached Task " + taskId + " - " + Thread.currentThread().getName());
            });
        }
        cached.shutdown();
        awaitTermination(cached);

        System.out.println();
    }

    // 4. Futureの使い方
    private static void demonstrateFuture() {
        System.out.println("--- 4. Future の使い方 ---");

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Callable<String> task = () -> {
            Thread.sleep(2000);
            return "非同期処理の結果";
        };

        Future<String> future = executor.submit(task);

        try {
            // isDone(): タスクが完了したかチェック（ノンブロッキング）
            System.out.println("isDone: " + future.isDone());

            // get(): 結果を取得（ブロッキング！完了まで待つ）
            System.out.println("結果を待機中...");
            String result = future.get(); // 最大2秒待つ
            System.out.println("結果: " + result);
            System.out.println("isDone: " + future.isDone());

            // タイムアウト付きget()
            Future<String> future2 = executor.submit(() -> {
                Thread.sleep(5000);
                return "遅い処理";
            });

            try {
                // 1秒でタイムアウト
                future2.get(1, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                System.out.println("タイムアウト発生！");
                future2.cancel(true); // タスクをキャンセル
                System.out.println("isCancelled: " + future2.isCancelled());
            }

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
            awaitTermination(executor);
        }

        System.out.println();
    }

    // 5. submit() vs execute()
    private static void demonstrateSubmitVsExecute() {
        System.out.println("--- 5. submit() vs execute() ---");

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // execute(): 戻り値なし、Runnableのみ
        System.out.println("execute(): Futureを返さない");
        executor.execute(() -> {
            System.out.println("  execute()で実行");
        });

        // submit(): Futureを返す、RunnableとCallableの両方OK
        System.out.println("submit(): Futureを返す");
        Future<?> future1 = executor.submit(() -> {
            System.out.println("  submit(Runnable)で実行");
        });

        Future<Integer> future2 = executor.submit(() -> {
            System.out.println("  submit(Callable)で実行");
            return 100;
        });

        try {
            future1.get(); // 完了を待つ
            Integer result = future2.get();
            System.out.println("  Callableの結果: " + result);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
            awaitTermination(executor);
        }

        System.out.println();
    }

    // 6. invokeAll() と invokeAny()
    private static void demonstrateInvokeAllAndAny() {
        System.out.println("--- 6. invokeAll() と invokeAny() ---");

        // invokeAll(): 全てのタスクを実行し、全ての結果を待つ
        System.out.println("invokeAll(): 全タスクの完了を待つ");
        ExecutorService executor1 = Executors.newFixedThreadPool(3);

        List<Callable<String>> tasks1 = new ArrayList<>();
        tasks1.add(() -> {
            Thread.sleep(1000);
            return "タスク1完了";
        });
        tasks1.add(() -> {
            Thread.sleep(500);
            return "タスク2完了";
        });
        tasks1.add(() -> {
            Thread.sleep(1500);
            return "タスク3完了";
        });

        try {
            List<Future<String>> futures = executor1.invokeAll(tasks1);
            for (Future<String> future : futures) {
                System.out.println("  結果: " + future.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor1.shutdown();
            awaitTermination(executor1);
        }

        // invokeAny(): 最初に完了したタスクの結果を返す
        System.out.println("\ninvokeAny(): 最速のタスクの結果のみ取得");
        ExecutorService executor2 = Executors.newFixedThreadPool(3);

        List<Callable<String>> tasks2 = new ArrayList<>();
        tasks2.add(() -> {
            Thread.sleep(2000);
            return "遅いタスク";
        });
        tasks2.add(() -> {
            Thread.sleep(500);
            return "速いタスク"; // これが返される
        });
        tasks2.add(() -> {
            Thread.sleep(1000);
            return "普通のタスク";
        });

        try {
            String result = executor2.invokeAny(tasks2);
            System.out.println("  最速の結果: " + result);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor2.shutdown();
            awaitTermination(executor2);
        }

        System.out.println();
    }

    // 7. ScheduledExecutorService
    private static void demonstrateScheduledExecutor() {
        System.out.println("--- 7. ScheduledExecutorService ---");

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

        // (a) schedule(): 遅延実行（1回だけ）
        System.out.println("schedule(): 2秒後に1回実行");
        scheduler.schedule(() -> {
            System.out.println("  2秒後に実行されました");
        }, 2, TimeUnit.SECONDS);

        // (b) scheduleAtFixedRate(): 固定レートで繰り返し実行
        // 前回の開始時刻から固定間隔
        System.out.println("\nscheduleAtFixedRate(): 初期遅延1秒、その後1秒間隔で実行");
        ScheduledFuture<?> fixedRate = scheduler.scheduleAtFixedRate(() -> {
            System.out.println("  FixedRate実行: " + System.currentTimeMillis());
        }, 1, 1, TimeUnit.SECONDS);

        // (c) scheduleWithFixedDelay(): 固定遅延で繰り返し実行
        // 前回の完了時刻から固定間隔
        System.out.println("scheduleWithFixedDelay(): 初期遅延1秒、完了後1秒待って実行");
        ScheduledFuture<?> fixedDelay = scheduler.scheduleWithFixedDelay(() -> {
            System.out.println("  FixedDelay実行: " + System.currentTimeMillis());
        }, 1, 1, TimeUnit.SECONDS);

        // 5秒後にキャンセル
        try {
            Thread.sleep(5000);
            fixedRate.cancel(false);
            fixedDelay.cancel(false);
            System.out.println("\n定期実行をキャンセルしました");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            scheduler.shutdown();
            awaitTermination(scheduler);
        }

        System.out.println();
    }

    // 8. ExecutorServiceのライフサイクル
    private static void demonstrateLifecycle() {
        System.out.println("--- 8. ExecutorService ライフサイクル ---");

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // タスクを投入
        executor.submit(() -> {
            try {
                Thread.sleep(2000);
                System.out.println("  長いタスク完了");
            } catch (InterruptedException e) {
                System.out.println("  タスクが中断されました");
            }
        });

        // shutdown(): 新しいタスクを受け付けない、実行中のタスクは完了を待つ
        System.out.println("shutdown(): 新規タスク受付停止、実行中タスクは完了を待つ");
        executor.shutdown();

        // shutdownしたら新しいタスクは受け付けない
        try {
            executor.submit(() -> System.out.println("実行されない"));
        } catch (RejectedExecutionException e) {
            System.out.println("  エラー: shutdown後は新規タスクを受け付けない");
        }

        // isShutdown(): shutdownが呼ばれたか
        System.out.println("isShutdown: " + executor.isShutdown());

        // isTerminated(): 全タスクが完了したか
        System.out.println("isTerminated: " + executor.isTerminated());

        // awaitTermination(): 指定時間まで終了を待つ
        try {
            boolean terminated = executor.awaitTermination(3, TimeUnit.SECONDS);
            System.out.println("awaitTermination(3秒): " + terminated);
            System.out.println("isTerminated: " + executor.isTerminated());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // shutdownNow()のデモ
        System.out.println("\nshutdownNow()のデモ:");
        ExecutorService executor2 = Executors.newFixedThreadPool(2);

        executor2.submit(() -> {
            try {
                Thread.sleep(10000);
                System.out.println("  完了（実行されないはず）");
            } catch (InterruptedException e) {
                System.out.println("  タスクが強制中断されました");
            }
        });

        try {
            Thread.sleep(500); // タスクの開始を待つ
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // shutdownNow(): 実行中のタスクを中断し、待機中のタスクを返す
        System.out.println("shutdownNow(): 実行中タスクを中断、待機中タスクをリストで返す");
        List<Runnable> notExecuted = executor2.shutdownNow();
        System.out.println("実行されなかったタスク数: " + notExecuted.size());

        awaitTermination(executor2);

        System.out.println();
    }

    // ヘルパーメソッド: ExecutorServiceの終了を待つ
    private static void awaitTermination(ExecutorService executor) {
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
