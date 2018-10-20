# 地図アプリ
サンプル地図アプリです。
もともと2017年頭に書いたQiitaの記事のサンプルコードでしたが、1年経った今推奨されない方法で位置情報を取得するコードを書いていたことに気づいたので修正することにしました。
ただ、元のコードはそのままにしておきたいので少し修正し、新しいコードはProductFlavorで分けることにしました。

## Google Maps Keyについて
`src/release/res/values/google_maps_api.xml`と同様のファイルをdebugの下にも置いて、自身で`Google Maps API key`を取得し設定してください。

## ProductFlavorについて
 - oldMap: java＋android.locationを使用したサンプル。パーミッションも自分で処理。
 - recentMap: kotlin+Google Location Services APIを使用したサンプル。PermissionDispatcherを使わせていただいております。
