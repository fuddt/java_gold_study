package app;

/**
 * 数値を1つ受け取って、数値を返す
 * 抽象メソッドが1つだけなので関数型インタフェース
 */
@FunctionalInterface
interface IntProcessor {

    // 抽象メソッドは1つだけ
    int process(int x);

    // default メソッドはあってもOK（抽象メソッドに数えない）
    default void log(int x) {   
        System.out.println("input = " + x);
    }
}


public class Main {

    public static void main(String[] args) {

        // ① ラムダ式で実装（2倍にする処理）
        IntProcessor doubleProcessor = x -> x * 2;

        // ② ラムダ式で実装（2乗する処理）
        IntProcessor squareProcessor = x -> x * x;

        // ③ 処理を「引数として」渡す
        execute(5, doubleProcessor);
        execute(5, squareProcessor);

        // ④ その場限りの処理も書ける（使い捨て）
        execute(5, x -> x + 10);
    }

    /**
     * 数値と「処理」を受け取って実行するメソッド
     */
    private static void execute(int value, IntProcessor processor) {

        // default メソッドも呼べる
        processor.log(value);

        int result = processor.process(value);
        System.out.println("result = " + result);
        System.out.println("-----");
    }
}