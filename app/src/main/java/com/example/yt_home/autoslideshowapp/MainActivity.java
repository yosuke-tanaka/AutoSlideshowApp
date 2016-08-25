package com.example.yt_home.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    // コンポーネント
    TextView textView;
    Button button1;
    Button button2;
    Button button3;

    // 画像URL一覧
    ArrayList<Uri> imageUriList = new ArrayList<>();

    // 現在表示中の画像ID (0起算)
    int curImgId = 0;

    // 画像数
    int numImg = 0;

    // 再生中フラグ
    boolean isPlaying = false;

    // タイマ関連
    MyTimerTask timerTask = null;
    Timer mTimer   = null;
    Handler mHandler = new Handler();
    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            // mHandlerを通じてUI Threadへ処理をキューイング
            mHandler.post( new Runnable() {
                public void run() {

                    //次の画像を表示
                    StepAndShowImg(+1);
                }
            });
        }
    }

    // メインアクティビティ
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);
        button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(this);
        button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(this);
        button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(this);

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                }
                break;
            default:
                break;
        }
    }

    // 画像URL一覧取得
    private void getContentsInfo() {

        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        numImg = 0;
        if (cursor.moveToFirst()) {
            do {
                int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                Long id = cursor.getLong(fieldIndex);
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                // 画像一覧に追加
                imageUriList.add(imageUri);
                numImg++;
            } while (cursor.moveToNext());
        }
        cursor.close();

        if(numImg == 0)
        {
            showAlertDialog("画像がありません");
            //ボタンタップ不可
            button1.setEnabled(false);
            button2.setEnabled(false);
            button3.setEnabled(false);
            return;
        }

        // 最初の画像を表示する
        ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
        imageVIew.setImageURI(imageUriList.get(curImgId));
        textView.setText("画像：" + (curImgId + 1) + "/" + numImg);
    }

    // ボタンクリック時の処理
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button1)
        {
            // 次へ
            StepAndShowImg(+1);
        }
        else if (v.getId() == R.id.button2)
        {
            // 戻る
            StepAndShowImg(-1);
        }
        else if (v.getId() == R.id.button3)
        {
            // 再生/停止
            if(isPlaying == false)
            {
                // 再生
                if(mTimer == null){
                    //タイマーの初期化処理
                    timerTask = new MyTimerTask();
                    mTimer = new Timer(true);
                    mTimer.schedule(timerTask, 2000, 2000);
                }

                isPlaying = true;
                button3.setText("停止");

                //ボタンタップ不可
                button1.setEnabled(false);
                button2.setEnabled(false);
            }
            else
            {
                // 停止
                if(mTimer != null){
                    mTimer.cancel();
                    mTimer = null;
                }

                isPlaying = false;
                button3.setText("再生");

                //ボタンタップ可
                button1.setEnabled(true);
                button2.setEnabled(true);
            }
        }
    }

    // step分画像を進め、その画像を表示する
    private void StepAndShowImg(int step)
    {
        curImgId += step;
        if(curImgId >= numImg)
        {
            curImgId = 0;
        }
        else if(curImgId < 0)
        {
            curImgId = numImg - 1;
        }

        ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
        imageVIew.setImageURI(imageUriList.get(curImgId));
        textView.setText("画像：" + (curImgId + 1) + "/" + numImg);
    }

    // エラーダイアログ
    private void showAlertDialog(String msg) {
        // AlertDialog.Builderクラスを使ってAlertDialogの準備をする
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("エラー");
        alertDialogBuilder.setMessage(msg);

        // OKボタンに表示される文字列、押したときのリスナーを設定する
        alertDialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("UI_PARTS", "OKボタン");
                    }
                });

        // AlertDialogを作成して表示する
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}