Ring 非公式SDK サンプルアプリ
===
# 概要

このプロジェクトは、logbar Inc.が開発した指輪型ウェアラブルデバイス　Ring / Ring ZEROを、Androidアプリから使用するためのSDK(以下、非公式SDKと呼称)の開発を目指すプロジェクトの一部として、非公式SDKを使用したサンプルを作成するものです。

なお、__このプロジェクトはlogbar Inc.とは直接の関係はありません__。あくまで、ファンによる勝手なプロジェクトです。

# 詳細

Ring / Ring ZEROはlogbar Inc.が販売する指輪型ウェアラブルデバイスです。ジェスチャーにより、対応している機器のコントロールを行うことができます。詳細は、[logbar Inc.](http://logbar.jp/ring)をご覧ください。

しかし、2015年1月1日現在では、SDKの提供はされておらず、Ringを使用した独自のアプリ開発は行えない状態だったことから、Ringを利用したAndroidアプリの開発をスムーズに行うために、非公式SDKの開発を開始しました。

このプロジェクトでは、Androidライブラリ(\*.aar)の形式で開発した非公式SDKを組み込んだサンプルアプリケーションを公開しています。

現在の非公式SDKでは、以下の機能を利用することができます。

+ ジェスチャー認識
+ ジェスチャー登録
+ Ring本体情報の取得
+ バイブレーション、LEDの操作

なお、非公式SDK本体は、本プロジェクトの"RingInformalDriver"ディレクトリ内に格納されています。

# デモ

<http://www.youtube.com/watch?v=ngiRk_TddcQ>

# 免責

*このプロジェクトは、logbar Inc.とは直接の関係はありません。*

そのため、非公式SDKを含む本プロジェクトの成果物、ならびに本プロジェクトの成果物を使用して開発したアプリは、*logbar Inc.によってサポートされることはありません。*

また、非公式SDKは作者が独自に解析した内容を元に開発を行っており、使用されている技術はlogbar Inc.より公開された情報ではありません。そのため、非公式SDK、ならびに本プロジェクトの成果物自体にも一切の保証はありません。

このプロジェクトには一切の保証がなく、自己責任の元で利用することになることを了承いただいた上で、ご利用ください。

# 必要なもの

- logbar Ring または Ring ZERO

- Android 4.4(KitKat)以上を搭載し、かつBLEに対応したAndroid端末

- Androidアプリ開発環境一式(Android Studio推奨)

- やる気とアイデアと少しの無謀さ

# SDKの使用方法

## <a name="basis">基本

1. Androidライブラリ(ringinformaldriver-release.aar)を作成したアプリに組み込みます。

1. コールバック関数(RingDeviceCallback)を定義します。

        RingDevice.RingDeviceCallback mRingDeviceCallback = new RingDevice.RingDeviceCallback() {

          @Override
          public void onDeviceConnected(){
            /* 接続成功時の動作 */
          };

          @Override
          public void onDeviceConnectionFailed(int result) {
            /* 接続失敗時の動作。引数resultはエラーコード(下記)を示す。
             * 0 : Bluetooth/BLE利用不可
             * 1 : Ring発見失敗(ペアリングされていない等)
             * 2 : BLEサービスなし
             * 3 : Characteristicなし
             * 4 : 一時的な接続失敗。再接続したい場合は時間をおいて再度connect()を実行する。
             */
          };

          @Override
          public void onDeviceDisconnected() {
            /* デバイスから切断された場合の動作 */
          };

          @Override
          public void onCharacteristicRead(String name, String value) {
            /* Ring本体情報取得結果
             * name  : 情報名(RingDeviceInformationクラス参照)
             * value : 値
             */
          };

          @Override
          public void onCharacteristicWrote(int result) {
            /* 通知(バイブレーション、LED)を行った場合の動作
             * result : 成功した場合は0
             */
          };

          @Override
          public void onGestureDetected(GestureInformation perform, ArrayList<GestureInformation> recognize) {
            /* ジェスチャー認識結果
             * perform   : 実行したジェスチャーの情報
             * recognize : 一致すると判断されたジェスチャーの情報
             */
          };

          @Override
          public void onGestureRegistered(ArrayList<GestureInformation> gestures) {
            /* ジェスチャー登録結果
             * gestures : 登録したジェスチャーの情報
             * 現在引数のArrayListのサイズは1。将来的にファイル等からの
             * 登録を行う可能性があるので、ArrayListを採用。
             */
          };
        }

1. Ring非公式SDK(RingDevice)のインスタンスを作成します。

       RingDevice myRing = new RingDevice(getContext(), mRingDeviceCallback);

1. `RingDevice.connect()`メソッドを実行し、Ringに接続します。

※Ringへの接続時にRingのタッチセンサーの長押しが必要になる場合がありますので、connect()メソッドを実行したら、`RingDeviceCallback.onDeviceConnected()`または`RingDeviceCallback.onDeviceConnectionFailed()`が呼び出されるまでの間、ダイアログ等を使用してタッチセンサーの長押しを案内することを推奨します。

## ジェスチャー登録

非公式SDKでは、認識対象とするジェスチャーは、Ringを使って実際に行ったジェスチャーを行って登録します。

1. Ringに接続します。[基本](#basis)を参照。

1. `RingDevice.registerGesture(true)`メソッドを実行します。

        myRing.registerGesture(true);

1. Ringを使用してジェスチャーを実行します。

1. `RingDeviceCallback.onGestureRegistered(gestures)`に登録したジェスチャー情報が戻るので、第二引数のGestureInformationクラスからIDを取得し、そのIDに対して、実行したい内容を登録します。

1. `RingDevice.cancelRegisterGesture(false)`を実行して、ジェスチャー登録を終了します。

! **重要** !

認識したジェスチャーの登録をキャンセルする場合は、必ず`RingDevice.removeGetsure(id)`を実行して、非公式SDK内のジェスチャー登録を削除してください。

## ジェスチャー認識

Ringへの接続に成功すると、自動的にジェスチャー待ち受け状態となるので、以下の手順でジェスチャー認識を利用できます。

1. Ringに接続します。[基本](#basis)を参照。

1. Ringを使用してジェスチャーを実行します。

1. `RingDeviceCallback.onGestureDetected()`に認識されたジェスチャーの情報が返されるので、IDを取得して、そのIDに紐付けた動作を実行します。

備考

`RingDeviceCallback.onGestureDetected()`の第二引数には、実行したジェスチャーとの相違度が一定以下のジェスチャーからなるArrayListが戻されます。この時、ArrayListは相違度の降順でソートされています。

なお、閾値以下のジェスチャーがない場合、ArrayListのサイズが0になります。

また、判定の基準となる閾値は`RingDevice.setThreashold()`メソッドで設定することができます(デフォルト 10.0%)。

# ジェスチャー情報

認識、登録したジェスチャーに関する情報は、`GestureInformation`クラスとして表されます。

`GestureInformation`クラスには、次のメソッドがありますので、必要に応じてジェスチャーの情報を取得してください。

| メソッド                  | 動作                                         |
|:-------------------------|:--------------------------------------------|
| long getId()             | 登録されているジェスチャーのIDを取得します。      |
| Bitmap getBitmap()       | ジェスチャーを表す画像を取得します。             |
| float getDifferentRate() | 実行したジェスチャーと、登録されているジェスチャーの相違度を取得します。|

# その他

非公式SDKで使用している画像認識の精度は、正直高いものでありません。

また、SDKの使い勝手も決して良いものではないかもしれません。

そこで、画像認識について良いアイディアをお持ちであったり、このSDKに対してプラスとなるようなフィードバックをお持ちであれば、お気軽にご連絡ください。

# 謝辞

Ring / Ring ZEROという素晴らしいデバイスを開発してくれたlogbar Inc.に感謝します。
