package app;

import java.util.function.Predicate;
import java.util.function.Function;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Main {

    public static void main(String[] args) {

        // ① Predicate：条件判定（true / false）
        Predicate<Integer> isAdult = age -> age >= 20;

        System.out.println(isAdult.test(18)); // false
        System.out.println(isAdult.test(25)); // true


        // ② Function：変換（T -> R）
        Function<String, Integer> lengthFunc = s -> s.length();

        System.out.println(lengthFunc.apply("Java")); // 4


        // ③ Consumer：消費（戻り値なし）
        Consumer<String> printer = s -> System.out.println("Hello " + s);

        printer.accept("World"); // Hello World


        // ④ Supplier：供給（引数なし）
        Supplier<String> messageSupplier = () -> "Hello from Supplier";

        System.out.println(messageSupplier.get());
    }
}