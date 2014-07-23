package com.vsk.navitakun;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SearchActivity extends Activity {

    // UI部品
    private EditText addressText = null;
    private Button mapButton = null;

    String strDistLat;
    String strDistLng;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        requestWindowFeature(Window.FEATURE_RIGHT_ICON);
        setContentView(R.layout.search);
        setTitleColor(Color.BLACK);
        getWindow().setFeatureDrawableResource(Window.FEATURE_RIGHT_ICON, R.drawable.ic_launcher);

        // UI部品の取得
        addressText = (EditText) findViewById(R.id.addressText);
        mapButton = (Button) findViewById(R.id.mapButton);

        TextView textView = (TextView) findViewById(R.id.address);
        // テキストビューのテキストを設定します
        textView.setText("住所や駅などの場所を入力してください");

        // ボタンリスナー追加
        mapButton.setOnClickListener(mapListener);
    }

    /**
     * 入力した住所を目的地として設定するボタンのクリック時処理
     */
    Button.OnClickListener mapListener = new Button.OnClickListener() {
        public void onClick(View v) {
            String address = addressText.getText().toString();
            // 住所を詰めてDestMapActivityを起動
            Intent mapIntent = new Intent(SearchActivity.this, DestMapActivity.class);
            mapIntent.putExtra("input_address", address);
            startActivity(mapIntent);
        }
    };


    /**
     * オプションメニューの生成
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menusearch, menu);
        return true;
    }

    /**
     * オプションメニューの選択
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {

            /* この画面の操作方法メニュー選択時の処理 */
            case R.id.menu_help:
                new AlertDialog.Builder(SearchActivity.this)
                .setTitle("表示内容と操作方法")
                .setMessage(
                        "● 郵便番号で指定する\n（例） 110-0004\n\n● 住所で指定する\n（例） 東京都台東区下谷1-13-6\n\n● 建物や駅の名称で指定する\n（例） 六本木ヒルズ\n\n● 大まかな場所で指定する\n（例） 品川")
                .setPositiveButton("ＯＫ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        try {
                            // 何もしない
                        } catch (ActivityNotFoundException e) {
                            // 何もしない
                        }
                    }
                }).create().show();

        }
        return true;
    }
}
