# 使い方ガイド / Quick Start Guide

## このリポジトリの使い方

### 📝 毎日の学習の流れ

1. **朝・学習開始時**
   ```bash
   # 今日の日付で学習記録ファイルを作成
   cp templates/daily_note_template.md daily_notes/$(date +%Y-%m-%d).md
   ```

2. **学習中**
   - わからないことがあったらAIに質問
   - 重要な対話は `chat_logs/` に記録
   - コードを書いて試す
   - 理解したことをメモ

3. **夜・学習終了時**
   - 今日の学習記録を完成させる
   - 次回の学習予定を書く
   - 進捗トラッカー (`progress/learning_progress.md`) を更新

### 🤖 AIとの対話を記録する

重要な質問やブレークスルーがあった対話は記録しておきましょう：

```bash
# トピック名を入れてファイル作成
cp templates/chat_log_template.md chat_logs/$(date +%Y-%m-%d)_トピック名.md
```

例：
- `chat_logs/2025-01-15_stream_api.md`
- `chat_logs/2025-01-20_concurrency.md`

### 📚 学習資料の作成

各トピックについて学んだことをまとめます：

```bash
cp templates/study_material_template.md study_materials/トピック名.md
```

例：
- `study_materials/lambda_expressions.md` (既に例があります)
- `study_materials/stream_api.md`
- `study_materials/generics.md`

### 📊 進捗管理

`progress/learning_progress.md` を定期的に更新：
- トピックのチェックボックスを更新
- 週次振り返りを記入
- 模擬試験の結果を記録

## ファイル命名規則

### 日々の学習記録
フォーマット: `YYYY-MM-DD.md` または `YYYY-MM-DD_description.md`
- `2025-01-15.md`
- `2025-01-15_lambda_study.md`

### AIチャットログ
フォーマット: `YYYY-MM-DD_topic.md`
- `2025-01-15_generics.md`
- `2025-01-20_stream_operations.md`

### 学習資料
フォーマット: `topic_name.md`
- `lambda_expressions.md`
- `stream_api.md`
- `nio2_file_io.md`

## ヒントとコツ

### 📝 記録のコツ
- **完璧を求めない**: 気づいたことをすぐメモ
- **コードは必ず動かす**: 理解したつもりにならない
- **疑問はすぐ解決**: わからないことはAIに質問
- **定期的に見返す**: 過去の記録を読み返して定着させる

### 🤖 AIの活用法
1. **概念の理解**: 「〜とは何ですか？」
2. **具体例の要求**: 「実用的な例を教えてください」
3. **コード説明**: 「このコードを解説してください」
4. **練習問題**: 「〜について練習問題を作ってください」
5. **比較**: 「AとBの違いを教えてください」

### ⏰ 学習のペース
- **毎日少しずつ**: 1日30分でもOK
- **週に1度は振り返り**: 進捗を確認
- **月に1度は総復習**: 忘れていることを再確認

### 📈 進捗の測り方
1. チェックリストの完了率
2. 模擬試験のスコア
3. コードを書く速度と正確性
4. 説明できるトピック数

## 便利なコマンド

### 今日の学習を始める
```bash
# 学習記録ファイルを作成して開く
TODAY=$(date +%Y-%m-%d)
cp templates/daily_note_template.md daily_notes/$TODAY.md
# お好みのエディタで開く
code daily_notes/$TODAY.md  # VS Code
vim daily_notes/$TODAY.md   # Vim
```

### 今週の学習を確認
```bash
# 今週作成したファイルを表示
find daily_notes -name "*.md" -mtime -7
```

### 特定トピックの学習記録を検索
```bash
# 「ラムダ式」について書いた記録を検索
grep -r "ラムダ" daily_notes/ chat_logs/
```

## 次のステップ

1. ✅ このガイドを読んだら、今日の学習記録を作成してみましょう
2. ✅ `progress/learning_progress.md` で学習開始日を記入
3. ✅ 最初のトピックを選んで学習開始
4. ✅ わからないことがあったらAIに質問して、対話を記録

## サンプルファイル

参考になるサンプルがすでに用意されています：
- 📝 `daily_notes/2025-01-01_example.md` - 学習記録の例
- 💬 `chat_logs/2025-01-01_lambda_basics.md` - AI対話ログの例
- 📚 `study_materials/lambda_expressions.md` - 学習資料の例

これらを見て、自分のスタイルを見つけてください！

---

**頑張ってJava Gold合格を目指しましょう！ 🎯**
