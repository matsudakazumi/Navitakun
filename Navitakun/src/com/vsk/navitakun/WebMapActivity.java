package com.vsk.navitakun;

import java.io.IOException;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import android.location.Address;
import android.location.Geocoder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.location.Location;
//import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

public class WebMapActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {

    private static final String URL = "file:///android_asset/webview.html";
    private WebView webView;
    private Button destButton = null;
    private Button historyButton = null;
    private JsObj jsobj;

//    private boolean locationFlg = false;
    private boolean isFailed = false;
    private boolean isOnCreate = false;


    // 位置情報管理クラス
//    protected MyLocationManager myLocationManager;
    private LocationClient locationClient;

    // 現在位置情報
    protected Location location;

    /** Called when the activity is first created. */
    @SuppressLint("SetJavaScriptEnabled") @Override
    public void onCreate(Bundle savedInstanceState) {
//        Log.d("### call onCreate() ### ", "Start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setTitleColor(Color.BLACK);

        // ★
        final int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            Toast.makeText(this, "Google Play service is not available (status=" + result + ")", Toast.LENGTH_LONG).show();
            finish();
        }
        locationClient = new LocationClient(this, this, this);
        locationClient.connect();
        this.isOnCreate = true;
        // ★

        // UI部品の取得
        webView = (WebView) findViewById(R.id.webview);
        destButton = (Button) findViewById(R.id.destButton);
        historyButton = (Button) findViewById(R.id.historyButton);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        jsobj = new JsObj(this.getApplicationContext());
        webView.addJavascriptInterface(jsobj, "android");
        webView.loadUrl(URL);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        // ボタンリスナー追加（マップに目的地を設定）
        destButton.setOnClickListener(destListener);
        historyButton.setOnClickListener(historyListener);
        

//        myLocationManager = new MyLocationManager((LocationManager) getSystemService(LOCATION_SERVICE));
//        Log.d("### call onCreate() ### ", "End");
    }

    @Override
    public void onConnected(Bundle arg0) {
        location = locationClient.getLastLocation();
        Log.d("Location: ", location.toString());
        Geocoder geocoder = new Geocoder(this);
        
        try {
            List<Address> result = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            Toast.makeText(this, "現在地 ： " + addressToText((Address)result.get(0)), Toast.LENGTH_LONG).show();
            return;
        } catch (IOException e) {
            Toast.makeText(this, "Connection Failed", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }

    private CharSequence addressToText(Address paramAddress) {
      StringBuilder sb = new StringBuilder();
      sb.append(paramAddress.getAddressLine(1));
      return sb;
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        Toast.makeText(this, "Connection Failed", Toast.LENGTH_LONG).show();
    }
    
    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected", Toast.LENGTH_LONG).show();
    }
    
    /**
     * 目的地を設定ボタンのクリック時処理
     */
    Button.OnClickListener destListener = new Button.OnClickListener() {
        public void onClick(View v) {
            Intent selectIntent = new Intent(WebMapActivity.this, SelectActivity.class);
            startActivity(selectIntent);
        }
    };

    /**
     * ＮＡＶＩ履歴表示ボタンのクリック時処理
     */
    Button.OnClickListener historyListener = new Button.OnClickListener() {
        public void onClick(View v) {
            Intent historyIntent = new Intent(WebMapActivity.this, HistoryActivity.class);
            startActivity(historyIntent);
        }
    };

    @Override
    protected void onResume() {
//        Log.d("### call onResume() ### ", "Start");
//        if (myLocationManager.getCurrentLocation() == null) {
//            // 位置情報の取得を開始します。
//            myLocationManager.start();
//        } else {
//            myLocationManager.stop();
//            myLocationManager.start();
//        }
        super.onResume();
        if (this.isOnCreate) {
          isOnCreate = false;
          return;
        }
        locationClient.connect();
//        Log.d("### call onResume() ### ", "End");
    }

    @Override
    protected void onPause() {
//        Log.d("### call onPause() ### ", "Start");
        // 他画面へ遷移する場合は位置情報取得を止めます。
    	
////////////////////////////////////////////////////////////////////////////////
//        myLocationManager.stop();
////////////////////////////////////////////////////////////////////////////////

    	super.onPause();
        this.locationClient.disconnect();

//        Log.d("### call onPause() ### ", "End");
    }

    /**
     * オプションメニューの生成
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menuwebmap, menu);
        return true;
    }

    /**
     * オプションメニューの選択
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {

            /* 現在位置を更新メニュー選択時の処理 */
            case R.id.menu_updateCurrentLocation:
////////////////////////////////////////////////////////////////////////////////
//                // 位置情報の更新時は位置情報の取得上限を１回に設定
//                if (1 != myLocationManager.getUpdateLimit()) {
//                    myLocationManager.setUpdateLimit(1);
//                }
//                myLocationManager.stop();
//                myLocationManager.start();
////////////////////////////////////////////////////////////////////////////////
            	
                this.locationClient.connect();
                webView.loadUrl(URL);
                break;

            /* この画面の操作方法メニュー選択時の処理 */
            case R.id.menu_help:
                new AlertDialog.Builder(WebMapActivity.this)
                .setTitle("表示内容と操作方法")
                .setMessage(
                        "● 新しい目的地を設定して経路の確認やナビゲーションを始める場合は「ＮＡＶＩを始める」をタップしてください\n\n● 過去のナビゲーション履歴を確認する場合は「履歴を見る」をタップしてください\n\n● 現在位置を更新する場合はメニューの「現在位置を更新」をタップしてください")
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

    protected void setLocation(Location loc) {
        location = loc;
    }

////////////////////////////////////////////////////////////////////////////////
//    /**
//     * ロケーションマネージャ
//     */
//    class MyLocationManager extends BetterLocationManager {
//
//        /**
//         * コンストラクタです。
//         *
//         * @param locationManager
//         *            位置情報サービス
//         */
//        public MyLocationManager(final LocationManager locationManager) {
//            super(locationManager);
//        }
//
//        @Override
//        protected void onLocationProviderNotAvailable() {
//            // Google Maps アプリと同様に[現在地機能を改善]ダイアログを起動します。
//            new AlertDialog.Builder(WebMapActivity.this)
//                    .setTitle("現在地機能を改善")
//                    .setMessage(
//                            "現在、位置情報は一部有効でないものがあります。次のように設定すると、もっともすばやく正確に現在地を検出できるようになります:\n\n● 位置情報の設定でGPSとワイヤレスネットワークをオンにする\n\n● Wi-Fiをオンにする")
//                    .setPositiveButton("設定", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(final DialogInterface dialog, final int which) {
//                            try {
//                                startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
//                            } catch (ActivityNotFoundException e) {
//                            } // 無視する
//                        }
//                    }).setNegativeButton("スキップ", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(final DialogInterface dialog, final int which) {
//                        }
//                    }).create().show();
//        }
//
//        @Override
//        protected void onLocationProgress(final long time) {
//            if (getCurrentLocation() == null && time == 0L) {
//                Toast.makeText(getApplicationContext(), "現在地を取得しています", Toast.LENGTH_LONG).show();
//            }
//        }
//
//        @Override
//        protected void onLocationTimeout() {
//            if (getCurrentLocation() == null) {
//                Toast.makeText(getApplicationContext(), "一時的に現在地を検出できません", Toast.LENGTH_LONG).show();
//            }
//        }
//
//        @Override
//        protected void onUpdateLocation(final Location location, final int updateCount) {
//            if (updateCount == 0) {
//                // 何もしない
//            }
//            setLocation(location);
//            webView.loadUrl(URL);
//        }
//    }
////////////////////////////////////////////////////////////////////////////////

    /**
     * Sets up the interface for getting access to Latitude and Longitude data
     * from device
     **/
    class JsObj {
        Context con;

        public JsObj(Context con) {
            this.con = con;
        }

        public double getLatitude() {
////////////////////////////////////////////////////////////////////////////////
//            if (null == location) {
//                if (!locationFlg) {
//                    Toast.makeText(getApplicationContext(), "現在地を取得できませんでした", Toast.LENGTH_LONG).show();
//                    locationFlg = true;
//                }
//                // 初期値（東京都庁）
//                return 35.689506;
//            }
//            return location.getLatitude();
////////////////////////////////////////////////////////////////////////////////
        	
            if (WebMapActivity.this.location == null) {
              WebMapActivity.this.isFailed = true;
              return 35.689506;
            }
            WebMapActivity.this.isFailed = false;
            return location.getLatitude();
        }

        public double getLongitude() {
////////////////////////////////////////////////////////////////////////////////
//            if (null == location) {
//                if (!locationFlg) {
//                    Toast.makeText(getApplicationContext(), "現在地を取得できませんでした", Toast.LENGTH_LONG).show();
//                    locationFlg = true;
//                }
//                // 初期値（東京都庁）
//                return 139.691701;
//            }
//            return location.getLongitude();
////////////////////////////////////////////////////////////////////////////////
        	
        	if (location == null) {
              isFailed = true;
              Toast.makeText(WebMapActivity.this.getApplicationContext(), "現在地を取得できませんでした", Toast.LENGTH_LONG).show();
              return 139.691701;
            }
            if (WebMapActivity.this.isFailed) {
              Toast.makeText(WebMapActivity.this.getApplicationContext(), "現在地を取得できませんでした", Toast.LENGTH_LONG).show();
              return 139.691701;
            }
            isFailed = false;
            return location.getLongitude();
        }

        public void setLatitude(double lat) {
            location.setLatitude(lat);
        }

        public void setLongitude(double lng) {
            location.setLongitude(lng);
        }

        public void setDebugPrint(String str) {
            Log.d("### call setDebugPrint() ### ", str);
        }
    }
}
