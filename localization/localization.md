
## ローカライゼーション（Java Gold）

ローカライゼーションは「アプリケーションを異なる言語・地域に対応させる」こと。
Java Gold では Locale、ResourceBundle、DateTimeFormatter、NumberFormat が出題される。

---

## 1. Locale クラス

### Locale の生成方法

```java
// ① 定数（よく使うもの）
Locale japan = Locale.JAPAN;       // ja_JP（言語 + 国）
Locale japanese = Locale.JAPANESE; // ja（言語のみ）
Locale us = Locale.US;             // en_US

// ② コンストラクタ
Locale fr = new Locale("fr", "FR");  // フランス語_フランス
Locale de = new Locale("de");        // ドイツ語（国なし）

// ③ Locale.Builder（推奨）
Locale it = new Locale.Builder()
    .setLanguage("it")
    .setRegion("IT")
    .build();

// ④ forLanguageTag（BCP 47形式）
Locale ko = Locale.forLanguageTag("ko-KR"); // ハイフン区切り
```

**試験ポイント**: `Locale.JAPAN`（ja_JP）と `Locale.JAPANESE`（ja）は異なるため注意。
- `JAPAN` = 言語（ja）+ 国（JP）
- `JAPANESE` = 言語（ja）のみ

### Locale の主要メソッド

| メソッド | 例（Locale.JAPAN） | 説明 |
|---------|-------------------|------|
| `getLanguage()` | "ja" | 言語コード |
| `getCountry()` | "JP" | 国コード |
| `getDisplayLanguage()` | "日本語" | 表示用言語名 |
| `getDisplayCountry()` | "日本" | 表示用国名 |
| `toString()` | "ja_JP" | 文字列表現 |

---

## 2. ResourceBundle の検索順序（超重要！）

これは試験で毎回のように出るため、確実に覚えておくこと。

### 検索順序

`ResourceBundle.getBundle("Messages", new Locale("fr", "FR"))` で、
デフォルトロケールが `ja_JP` の場合:

```
1. Messages_fr_FR  ← 要求ロケール（言語_国）
2. Messages_fr     ← 要求ロケール（言語のみ）
3. Messages_ja_JP  ← デフォルトロケール（言語_国）
4. Messages_ja     ← デフォルトロケール（言語のみ）
5. Messages        ← ベースバンドル（デフォルト）
6. MissingResourceException!  ← 何も見つからない
```

**重要ポイント**:
- **.class ファイルが .properties より優先**される
- 見つかったバンドルの**親バンドル**も読み込まれる（キーのフォールバック用）

### キーのフォールバック

```
Messages_fr_FR.properties: greeting=Bonjour
Messages_fr.properties:    farewell=Au revoir
Messages.properties:        greeting=Hello, farewell=Goodbye, app=MyApp
```

`Locale("fr", "FR")` で `getBundle` した場合:
- `greeting` → "Bonjour"（fr_FR で見つかる）
- `farewell` → "Au revoir"（fr にフォールバック）
- `app` → "MyApp"（ベースバンドルにフォールバック）
- `unknown` → **MissingResourceException**

### ResourceBundle の使い方

```java
ResourceBundle bundle = ResourceBundle.getBundle("Messages", Locale.JAPAN);
String greeting = bundle.getString("greeting"); // キーで値を取得

// 全キーの取得
Set<String> keys = bundle.keySet();
```

---

## 3. DateTimeFormatter

### 定義済みフォーマッタ

```java
LocalDate date = LocalDate.of(2025, 12, 25);
date.format(DateTimeFormatter.ISO_LOCAL_DATE);  // 2025-12-25
```

### ロケール依存フォーマッタ

```java
// FormatStyle: FULL, LONG, MEDIUM, SHORT
DateTimeFormatter f = DateTimeFormatter
    .ofLocalizedDate(FormatStyle.FULL)
    .withLocale(Locale.JAPAN);
date.format(f);  // 2025年12月25日木曜日
```

| FormatStyle | 日本 | 米国 |
|-------------|------|------|
| FULL | 2025年12月25日木曜日 | Thursday, December 25, 2025 |
| LONG | 2025年12月25日 | December 25, 2025 |
| MEDIUM | 2025/12/25 | Dec 25, 2025 |
| SHORT | 2025/12/25 | 12/25/25 |

### カスタムパターン（ofPattern）

```java
DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy/MM/dd");
DateTimeFormatter f2 = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.US);
```

主なパターン文字:
| 文字 | 意味 | 例 |
|------|------|-----|
| y | 年 | 2025 |
| M | 月 | 12, Dec |
| d | 日 | 25 |
| H | 時（24時間） | 14 |
| h | 時（12時間） | 2 |
| m | 分 | 30 |
| s | 秒 | 45 |
| E | 曜日 | 木 |
| a | AM/PM | 午後 |

### 型の不一致に注意！

```java
LocalDate date = LocalDate.now();
DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
date.format(timeFormatter); // DateTimeException!
// → LocalDate に時刻情報はない！
```

これは試験でよく出る引っかけである。

---

## 4. NumberFormat

### 数値フォーマット

```java
NumberFormat nf = NumberFormat.getInstance(Locale.JAPAN);
nf.format(1234567.89);  // 1,234,567.89

// ドイツはカンマとピリオドが逆！
NumberFormat de = NumberFormat.getInstance(Locale.GERMANY);
de.format(1234567.89);  // 1.234.567,89
```

### 通貨フォーマット

```java
NumberFormat jpCur = NumberFormat.getCurrencyInstance(Locale.JAPAN);
jpCur.format(1234.56);  // ￥1,235（小数点以下は四捨五入）

NumberFormat usCur = NumberFormat.getCurrencyInstance(Locale.US);
usCur.format(1234.56);  // $1,234.56
```

### パーセントフォーマット

```java
NumberFormat pct = NumberFormat.getPercentInstance();
pct.format(0.75);  // 75%
// ⚠️ 0.75を渡すと100倍されて75%になる！
// 75を渡すと7,500%になるから注意！
```

### パース（文字列→数値）

```java
NumberFormat nf = NumberFormat.getInstance(Locale.US);
Number num = nf.parse("1,234,567.89"); // ParseExceptionをスロー
// → 1234567.89 (Double)
```

**注意**: `parse()` は **ParseException**（checked例外）をスローする。

---

## 5. DecimalFormat

### パターン記号

| 記号 | 意味 | 例 |
|------|------|-----|
| `#` | 数字（ゼロなら表示しない） | `#.##` → `.5` |
| `0` | 数字（ゼロでも表示する） | `0.00` → `0.50` |
| `.` | 小数点 | |
| `,` | グループ区切り | |
| `%` | パーセント（100倍して%付加） | |

```java
new DecimalFormat("#,###.##").format(1234.5);   // 1,234.5
new DecimalFormat("000000.000").format(1234.5); // 001234.500
```

**`#` と `0` の違い**が試験で出るよ！
- `#` → ゼロは省略（`.5`）
- `0` → ゼロも表示（`0.50`）

---

## 6. Locale.Category

Java では DISPLAY と FORMAT の2つのカテゴリでロケールを別々に設定できる。

```java
// 表示用ロケール（UIのラベル等）
Locale.setDefault(Locale.Category.DISPLAY, Locale.US);

// フォーマット用ロケール（数値・日付のフォーマット）
Locale.setDefault(Locale.Category.FORMAT, Locale.JAPAN);
```

| カテゴリ | 用途 | 影響を受けるクラス |
|---------|------|-------------------|
| DISPLAY | UI表示 | `getDisplayLanguage()` 等 |
| FORMAT | フォーマット | `NumberFormat`, `DateTimeFormatter` 等 |

```java
// FORMAT カテゴリのロケールが使われる
NumberFormat nf = NumberFormat.getCurrencyInstance();
// → FORMAT が JAPAN なら ￥ で表示
```

---

## 試験ポイント・引っかけ問題

### Q1: ResourceBundle の検索順序

デフォルトロケールが `en_US` の場合、
`ResourceBundle.getBundle("Msg", new Locale("ja", "JP"))` の検索順序は？

**A**:
1. `Msg_ja_JP`
2. `Msg_ja`
3. `Msg_en_US`
4. `Msg_en`
5. `Msg`
6. `MissingResourceException`

### Q2: NumberFormat のパーセント

```java
NumberFormat pct = NumberFormat.getPercentInstance();
System.out.println(pct.format(75));
```
出力は？

**A**: `7,500%`。`getPercentInstance()` は値を100倍するから。0.75を渡すべき。

### Q3: DateTimeFormatter の型不一致

```java
LocalTime time = LocalTime.now();
DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
time.format(f);
```
結果は？

**A**: **DateTimeException**がスローされる。`LocalTime`に年月日の情報はない。

### Q4: DecimalFormat の # と 0

```java
System.out.println(new DecimalFormat("#.##").format(0.5));
System.out.println(new DecimalFormat("0.00").format(0.5));
```
出力は？

**A**:
- `#.##` → `0.5`（整数部の#は少なくとも1桁は表示）
- `0.00` → `0.50`（0なので必ず表示）

### Q5: Locale.JAPAN vs Locale.JAPANESE

```java
System.out.println(Locale.JAPAN);     // ?
System.out.println(Locale.JAPANESE);  // ?
```

**A**:
- `Locale.JAPAN` → `ja_JP`（言語 + 国）
- `Locale.JAPANESE` → `ja`（言語のみ）

ResourceBundle の検索に影響する！`JAPANESE` だと国コードがないので `Messages_ja` から検索が始まる。

### Q6: Locale.Builder の不正な値

```java
Locale locale = new Locale.Builder()
    .setLanguage("INVALID")
    .build();
```
結果は？

**A**: `IllformedLocaleException`がスローされる。Builder は入力を検証する。
一方 `new Locale("INVALID")` はエラーにならない（検証しない）。

---

## まとめ

| トピック | 覚えるべきこと |
|---------|---------------|
| Locale | 生成4方法、JAPAN vs JAPANESE の違い |
| ResourceBundle | 検索順序（言語_国→言語→デフォルト言語_国→デフォルト言語→ベース） |
| DateTimeFormatter | FormatStyle、ofPattern、型の不一致例外 |
| NumberFormat | getInstance / getCurrencyInstance / getPercentInstance |
| DecimalFormat | # vs 0 の違い |
| Locale.Category | DISPLAY vs FORMAT |
