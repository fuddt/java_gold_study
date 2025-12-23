package app;

interface Vehicle {
    // 抽象メソッド
    void drive();
    // デフォルトメソッド
    default void honk() {
        System.out.println("Beep beep!");
    }
    // 静的メソッド
    static void service() {
        System.out.println("Vehicle is being serviced.");
    }
}
class Car implements Vehicle {
    @Override
    public void drive() {
        System.out.println("The car is driving.");
    }
}
public class Main {
    public static void main(String[] args) {
        Car myCar = new Car();
        myCar.drive(); // The car is driving.
        myCar.honk();  // Beep beep!
        Vehicle.service(); // Vehicle is being serviced.
    }
}