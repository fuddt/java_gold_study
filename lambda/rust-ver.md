---

# Main.java の Rust版

Java の `Predicate` / `Function` / `Consumer` / `Supplier` は、Rust では「クロージャ（closure）」で表現できる。

- `Predicate<T>` → `|T| -> bool`
- `Function<T, R>` → `|T| -> R`
- `Consumer<T>` → `|T| -> ()`
- `Supplier<T>` → `|| -> T`

---

## main.rs 例

```rust
fn main() {
    // ① Predicate：条件判定（true / false）
    let is_adult = |age: i32| age >= 20;

    println!("{}", is_adult(18)); // false
    println!("{}", is_adult(25)); // true

    // ② Function：変換（T -> R）
    let length_func = |s: &str| s.len();

    println!("{}", length_func("Java")); // 4

    // ③ Consumer：消費（戻り値なし）
    let printer = |s: &str| println!("Hello {}", s);

    printer("World"); // Hello World

    // ④ Supplier：供給（引数なし）
    let message_supplier = || "Hello from Supplier";

    println!("{}", message_supplier());
}
```

※ `&str::len()` は「文字数」ではなく「UTF-8 のバイト数」。(`"Java"` はどちらも 4)
