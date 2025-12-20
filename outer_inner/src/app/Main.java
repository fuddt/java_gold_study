package app;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Outer outer = new Outer("05");
        Outer.JyoCodeConverter converter = outer.new JyoCodeConverter();
        converter.printJyoCode();
    }
}

class Outer {
    private final String jyoCD;

    Outer(String jyoCD) {
        this.jyoCD = jyoCD;
    }

    class JyoCodeConverter {

        private final Map<String, String> dictionary = Map.of(
            "01", "札幌",
            "02", "函館",
            "03", "新潟",
            "04", "京都",
            "05", "東京",
            "06", "中山",
            "07", "福島",
            "08", "阪神",
            "09", "中京",
            "10", "小倉"
        );

        void printJyoCode() {
            System.out.println("JyoCD: " + dictionary.get(jyoCD));
        }
    }
}
