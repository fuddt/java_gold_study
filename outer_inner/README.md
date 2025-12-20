javaのコンパイルコマンド
```bash
javac --module-source-path src -d out $(find src -name "*.java")
```
javaの実行コマンド

```bash
java --module-path out -m app/app.Main
```