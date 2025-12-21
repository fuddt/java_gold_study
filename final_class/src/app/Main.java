// package app;
// // finalクラスは継承できないことを示す例

// class Main {
//     public static void main(String[] args) {
//         final ThisIsFinal finalInstance = new ThisIsFinal("Hello, Final Class!");
//         //　フィールドは変更できる
//         System.out.println(finalInstance.getMessage());
//         finalInstance.setMesage("Final Class Modified Message");
//         System.out.println(finalInstance.getMessage());
//     }
// }

// final abstract class ThisIsFinal {
//     private String message;

//     ThisIsFinal(String message) {
//         this.message = message;
//     }

//     public String getMessage() {
//         return message;
//     }

//     public void setMesage(String message) {
//         this.message = message;
//     }
// }


