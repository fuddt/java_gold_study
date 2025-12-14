
⸻

1️⃣ インナークラスとは何か（定義）

外部クラスのインスタンスに“ひも付く”クラス。

class Outer {
    class Inner {
        void hello() {
            System.out.println("hello");
        }
    }
}

ここで重要なのは👇
👉 Inner は Outer のインスタンスなしでは存在できない

⸻

2️⃣ 生成方法（ここが超頻出）

❌ ダメな例（よく出る）

Outer.Inner inner = new Outer.Inner(); // コンパイルエラー

✅ 正しい生成手順

Outer outer = new Outer();             // ① 外部クラスのインスタンス
Outer.Inner inner = outer.new Inner(); // ② そこに紐づく Inner

inner.hello();

覚え方（試験用）
	•	outer.new Inner()
	•	new Inner() 単体は絶対ダメ

⸻

3️⃣ 外部クラスのメンバへのアクセス

インナークラスは 外部クラスの private メンバにもアクセス可能。

class Outer {
    private int x = 10;

    class Inner {
        void show() {
            System.out.println(x); // privateでもOK
        }
    }
}

これは
👉 「同一クラスファイル扱い」 だから。

⸻

4️⃣ this の意味（引っかけポイント）

class Outer {
    int x = 1;

    class Inner {
        int x = 2;

        void print() {
            System.out.println(x);          // ② Inner.x
            System.out.println(this.x);     // ② Inner.x
            System.out.println(Outer.this.x); // ① Outer.x
        }
    }
}

出力

2
2
1

試験で問われること
	•	this は 一番内側
	•	外側を指定したいときは Outer.this

⸻

5️⃣ インナークラスの修飾子制限

インナークラスは メンバクラスなので👇はOK。

public class Outer {
    protected class Inner {
    }
}

ただし👇はNG。

class Outer {
    static class Inner { } // ❌ インナークラスではない（staticネスト）
}


⸻

6️⃣ staticメンバを持てるか？

原則：❌ 持てない

class Outer {
    class Inner {
        static int x = 10; // ❌ コンパイルエラー
    }
}

例外：定数ならOK

class Outer {
    class Inner {
        static final int X = 10; // ✅
    }
}

理由：
static final の compile-time constant は実体を持たないから。

⸻

7️⃣ 試験での典型ひっかけ問題

Q. これは動く？

class Outer {
    class Inner {}
}

public class Test {
    public static void main(String[] args) {
        Outer.Inner i = new Outer().new Inner(); // ✅
    }
}

👉 動く

⸻

Q. これは？

class Outer {
    class Inner {}
}

public class Test {
    Inner i; // ❌
}

👉 動かない
Inner はトップレベルでは見えない
→ 正しくは Outer.Inner

⸻

ここまでの要点まとめ（暗記用）
	•	インナークラス = 外部クラスのインスタンスに依存
	•	生成方法：outer.new Inner()
	•	privateメンバにアクセス可能
	•	Outer.this が使える
	•	staticメンバは原則NG（定数のみOK）

⸻

次、どこを深掘りする？
	•	👉 staticインナークラス（ここで一気に比較が完成する）
	•	👉 ローカルクラス（事実上finalの罠）
	•	👉 無名クラス（interface / abstract との絡み）

流れ的には staticインナー が一番きれいに繋がる。