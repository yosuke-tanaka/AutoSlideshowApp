package com.example.yt_home.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    TextView textView;
    Button button1;
    Button button2;
    Button button3;

    // 画像一覧
    ArrayList<Uri> imageUriList = new ArrayList<>();

    // 現在表示中の画像ID (0起算)
    int curImgId = 0;

    // 画像数
    int numImg = 0;

    // 再生中フラグ
    boolean isPlaying = false;

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

        // 最初の画像を表示する
        ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
        imageVIew.setImageURI(imageUriList.get(curImgId));
        textView.setText((curImgId + 1) + "/" + numImg);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button1)
        {
            // 次へ
            curImgId++;
            if(curImgId == numImg)
            {
                curImgId = 0;
            }

            ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
            imageVIew.setImageURI(imageUriList.get(curImgId));
            textView.setText((curImgId + 1) + "/" + numImg);
        }
        else if (v.getId() == R.id.button2)
        {
            // 戻る
            curImgId--;
            if(curImgId < 0)
            {
                curImgId = numImg - 1;
            }

            ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
            imageVIew.setImageURI(imageUriList.get(curImgId));
            textView.setText((curImgId + 1) + "/" + numImg);
        }
        else if (v.getId() == R.id.button3)
        {
            // 再生/停止
            if(isPlaying == false)
            {
                isPlaying = true;
                button3.setText("再生");
            }
            else
            {
                isPlaying = false;
                button3.setText("停止");
            }
        }
    }
}