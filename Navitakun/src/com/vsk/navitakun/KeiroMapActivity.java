package com.vsk.navitakun;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

public class KeiroMapActivity extends Activity {

    // UI部品
    private Button naviButton = null;

    private static final String URL = "file:///android_asset/keirowebview.html";
    private WebView keiroWebView;
    private JsObj jsobj;

    protected boolean locationFlg = false;
    protected boolean changeFlg = false;
    protected boolean destExistFlg = false;

    // 出発地位置情報
    protected double startLat = 0.0d;
    protected double startLng = 0.0d;

    // 目的地位置情報
    protected double destLat = 0.0d;
    protected double destLng = 0.0d;

    // 履歴からの遷移を識別
    protected String rireki = "0";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
//        Log.d("### call onCreate() ### ", "Start");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        requestWindowFeature(Window.FEATURE_RIGHT_ICON);
        setContentView(R.layout.keirowebview);
        setTitleColor(Color.BLACK);
        getWindow().setFeatureDrawableResource(Window.FEATURE_RIGHT_ICON,R.drawable.ic_launcher);
        keiroWebView = (WebView) findViewById(R.id.keirowebview);

        // UI部品の取得
        naviButton = (Button) findViewById(R.id.k_naviButton);

        String strSLat = (String)getIntent().getSerializableExtra("start_lat");
        String strSLng = (String)getIntent().getSerializableExtra("start_lng");
        String strDLat = (String)getIntent().getSerializableExtra("dest_lat");
        String strDLng = (String)getIntent().getSerializableExtra("dest_lng");
        String strRireki = (String)getIntent().getSerializableExtra("rireki");
        if ("1".equals(strRireki)) {
            rireki = strRireki;
        }
        if (null != strSLat && null != strSLng && null != strDLat && null != strDLng) {
            destExistFlg = true;
            // Intentから位置情報を取得
            startLat = Double.valueOf(strSLat);
            startLng = Double.valueOf(strSLng);
            destLat = Double.valueOf(strDLat);
            destLng = Double.valueOf(strDLng);
        } else {
            // TODO エラー処理
        }

        WebSettings webSettings = keiroWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        jsobj = new JsObj(this.getApplicationContext());
        keiroWebView.addJavascriptInterface(jsobj, "android");
        keiroWebView.loadUrl(URL);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // ボタンリスナー追加
        naviButton.setOnClickListener(k_naviListener);
//        Log.d("### call onCreate() ### ", "End");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    /**
     * ＮＡＶＩボタンのクリック時処理
     */
    Button.OnClickListener k_naviListener = new Button.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(KeiroMapActivity.this, NaviMapActivity.class);
            // 出発地と目的地の位置情報をインテントに設定
            intent.putExtra("start_lat", Double.toString(startLat));
            intent.putExtra("start_lng", Double.toString(startLng));
            intent.putExtra("dest_lat", Double.toString(destLat));
            intent.putExtra("dest_lng", Double.toString(destLng));
            intent.putExtra("rireki", rireki);

            // NaviMapActivity起動
            startActivity(intent);
        }
    };


    /**
     * オプションメニューの生成
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menukeiro, menu);
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
                new AlertDialog.Builder(KeiroMapActivity.this)
                .setTitle("表示内容と操作方法")
                .setMessage(
                        "● 経路は徒歩で移動することを前提とし、出発地から目的地まで青いラインで表示します\n\n● 画面上部に出発地から目的地までのトータル移動距離と時速４．２ｋｍで歩いた場合の所要時間を表示します\n\n● 画面左上の黄色い人型アイコンを地図上にドラッグ＆ドロップするとストリートビューが表示されます\n\n\n● ナビゲーションを開始する場合は「ＮＡＶＩ」を選択してください\n※ 「ＮＡＶＩ」を選択すると出発地や目的地などのＮＡＶＩ情報がアプリに記録されます\n（ 記録されたＮＡＶＩ情報の履歴は後から確認／編集ができます）")
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


    /**
     * Sets up the interface for getting access to Latitude and Longitude data
     * from device
     **/
    class JsObj {
        Context con;

        public JsObj(Context con) {
            this.con = con;
        }

        public double getStartLatitude() {
            if (0.0d == startLat) {
                // 初期値（東京都庁）
                return 35.689506;
            }
            return startLat;
        }

        public double getStartLongitude() {
            if (0.0d == startLng) {
                // 初期値（東京都庁）
                return 139.691701;
            }
            return startLng;
        }

        public boolean getDestExistFlg() {
            return destExistFlg;
        }

        public void setDestExistFlg(boolean flg) {
            destExistFlg = flg;
        }

        public double getDestLatitude() {
            return destLat;
        }

        public double getDestLongitude() {
            return destLng;
        }

        public void setDebugPrint(String str) {
            Log.d("### call setDebugPrint() ### ", str);
        }
    }
}
