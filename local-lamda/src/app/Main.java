package app;


public class Main {
    public static void main(String[] args) {

        Runnable r = () -> {
            int count = 0;   // 毎回初期化される
            count++;
            System.out.println(count);
        };
        r.run(); // 1
        r.run(); // 1
        r.run(); // 1
    }
}
