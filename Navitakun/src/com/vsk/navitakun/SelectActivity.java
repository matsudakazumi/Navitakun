package com.vsk.navitakun;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class SelectActivity extends Activity {

    // UI部品
    private Button inputButton = null;
    private Button mapButton = null;
    private Button voiceButton = null;

    // リクエストコード
    private static final int REQUEST = 0;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        requestWindowFeature(Window.FEATURE_RIGHT_ICON);
        setContentView(R.layout.select);
        setTitleColor(Color.BLACK);
        getWindow().setFeatureDrawableResource(Window.FEATURE_RIGHT_ICON, R.drawable.ic_launcher);

        // UI部品の取得
        inputButton = (Button) findViewById(R.id.inputButton);
        mapButton = (Button) findViewById(R.id.mapButton);
        voiceButton = (Button) findViewById(R.id.voiceButton);

        TextView textView = (TextView) findViewById(R.id.explanation);
        // テキストビューのテキストを設定します
        textView.setText("目的地の設定方法を選択してください");

        // ボタンリスナー追加
        inputButton.setOnClickListener(inputListener);
        mapButton.setOnClickListener(mapListener);
        voiceButton.setOnClickListener(voiceListener);

        // AdMob
//        AdView adView = (AdView)this.findViewById(R.id.ad);
//        AdRequest adRequest = new AdRequest();
//        adView.loadAd(adRequest);
//        adView = new AdView(this);
//        adView.setAdUnitId(MY_AD_UNIT_ID);
//        adView.setAdSize(AdSize.BANNER);

        AdView adView = (AdView)this.findViewById(R.id.ad);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    /**
     * 住所を入力ボタンのクリック時処理
     */
    Button.OnClickListener inputListener = new Button.OnClickListener() {
        public void onClick(View v) {
            // SearchActivity起動
            Intent intent = new Intent(SelectActivity.this, SearchActivity.class);
            startActivity(intent);
        }
    };

    /**
     * 地図で指定ボタンのクリック時処理
     */
    Button.OnClickListener mapListener = new Button.OnClickListener() {
        public void onClick(View v) {
            // DestMapActivity起動
            Intent intent = new Intent(SelectActivity.this, DestMapActivity.class);
            startActivity(intent);
        }
    };

    /**
     * 音声入力ボタンのクリック時処理
     */
    Button.OnClickListener voiceListener = new Button.OnClickListener() {
        public void onClick(View v) {
            try {
                // インテント作成
                // 入力した音声を解析する。
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                // free-form speech recognition.
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                // 表示させる文字列
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"駅や公共施設の名称などの音声データを\n解析して位置情報に変換します");
                // インテント開始
                startActivityForResult(intent, REQUEST);
            } catch (ActivityNotFoundException e) {
                // アクティビティが見つからなかった
                Toast.makeText(SelectActivity.this,"音声入力のためのアクティビティが見つかりませんでした", Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 自分が投げたインテントであれば応答する
        if (requestCode == REQUEST && resultCode == RESULT_OK) {
            StringBuilder sb = new StringBuilder();

            // 結果文字列リスト
            // 複数の文字を認識した場合，結合して出力
            ArrayList<String> speechToChar = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (speechToChar.size() > 0) {
                sb.append(speechToChar.get(0));
            }
            for (int i = 0; i< speechToChar.size(); i++) {
                // 音声データの解析（やらない）
            }

            // 文字が短かった場合空白文字でパディング
            for (int i = (20-sb.toString().length()); i > 0; i--) {
                sb.append(" ");
            }

            // 住所を詰めてDestMapActivityを起動
            Intent mapIntent = new Intent(SelectActivity.this, DestMapActivity.class);
            mapIntent.putExtra("input_address", sb.toString());
            startActivity(mapIntent);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * オプションメニューの生成
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menuselect, menu);
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
                new AlertDialog.Builder(SelectActivity.this)
                .setTitle("表示内容と操作方法")
                .setMessage(
                        "● 住所／郵便番号／場所の名称等を入力して目的地を設定する場合は「住所を入力」をタップしてください\n\n● 地図上で目的地を設定する場合は「地図で指定」をタップしてください\n\n● 駅や場所の名称等を音声で設定する場合は「音声入力」をタップしてください")
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
