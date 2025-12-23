package app;

enum Calc {
    PLUS {
        int apply(int a, int b) { return a + b; }
    },
    MINUS {
        int apply(int a, int b) { return a - b; }
    };

    abstract int apply(int a, int b);
}
public class Main {
    public static void main(String[] args) {
        int result1 = Calc.PLUS.apply(5, 3);
        int result2 = Calc.MINUS.apply(5, 3);
        System.out.println("PLUS: " + result1);
        System.out.println("MINUS: " + result2);
    }
}