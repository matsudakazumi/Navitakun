package com.vsk.navitakun;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;

import com.vsk.navitakun.db.NaviHistory;
import com.vsk.navitakun.db.NaviHistoryDao;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
//import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
//import android.location.LocationManager;
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
import android.widget.Toast;

public class NaviMapActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {

    private static final String URL = "file:///android_asset/naviwebview.html";
    private WebView naviView;
    private Button updButton = null;
    private JsObj jsobj;

    // 位置情報管理クラス
//    protected MyLocationManager myLocationManager;
    private LocationClient locationClient;

    // 現在位置情報
    protected Location currentLocation;

    // 出発地位置情報
    protected double startLat = 0.0d;
    protected double startLng = 0.0d;

    // 目的地位置情報
    protected double destLat = 0.0d;
    protected double destLng = 0.0d;
    boolean destExistFlg = false;

    // 履歴からの遷移を識別
    protected String rireki = "0";

    // 画面データ保存フラグ
    protected boolean saveFlg = false;
    
    private boolean isFailed = false;
    private boolean isOnCreate = false;

    /** Called when the activity is first created. */
    @SuppressLint("SetJavaScriptEnabled") @Override
    public void onCreate(Bundle savedInstanceState) {
//        Log.d("### call onCreate() ### ", "Start");
        super.onCreate(savedInstanceState);
        
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

        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        requestWindowFeature(Window.FEATURE_RIGHT_ICON);
        setContentView(R.layout.naviwebview);
        setTitleColor(Color.BLACK);
        getWindow().setFeatureDrawableResource(Window.FEATURE_RIGHT_ICON, R.drawable.ic_launcher);

        // UI部品の取得
        naviView = (WebView) findViewById(R.id.naviwebview);
        updButton = (Button) findViewById(R.id.naviUpdButton);

        // 位置情報を取得
        String strSLat = (String)getIntent().getSerializableExtra("start_lat");
        String strSLng = (String)getIntent().getSerializableExtra("start_lng");
        String strDLat = (String)getIntent().getSerializableExtra("dest_lat");
        String strDLng = (String)getIntent().getSerializableExtra("dest_lng");
        if (null != strSLat && null != strSLng && null != strDLat && null != strDLng) {
            destExistFlg = true;
            // Intentから位置情報を取得
            startLat = Double.valueOf(strSLat);
            startLng = Double.valueOf(strSLng);
            destLat = Double.valueOf(strDLat);
            destLng = Double.valueOf(strDLng);
        } else {
            // エラー処理
        }
        String strRireki = (String)getIntent().getSerializableExtra("rireki");
        if ("1".equals(strRireki)) {
            rireki = strRireki;
        }

        WebSettings webSettings = naviView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        jsobj = new JsObj(this.getApplicationContext());
        naviView.addJavascriptInterface(jsobj, "android");
        naviView.loadUrl(URL);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // ボタンリスナー追加（マップに目的地を設定）
        updButton.setOnClickListener(updListener);

////////////////////////////////////////////////////////////////////////////////
//        // ロケーションマネージャの生成
//        myLocationManager = new MyLocationManager((LocationManager) getSystemService(LOCATION_SERVICE));
//        // 現在位置取得処理の実行間隔の設定
//        myLocationManager.setInterval(60 * 1000L);
//        // タイマーの設定
//        myLocationManager.setTimeout(5 * 60 * 1000L);
//        // 位置情報更新回数の上限設定（ナビ開始時は自動で５分間で５回位置情報を更新する設定とする）
//        myLocationManager.setUpdateLimit(5);
////////////////////////////////////////////////////////////////////////////////

        // 現在地マーカーの説明
        Toast toast = Toast.makeText(this, "現在地を人型の赤いアイコンで表示します", Toast.LENGTH_LONG);
        toast.show();

//        Log.d("### call onCreate() ### ", "End");
    }

    @Override
    public void onConnected(Bundle arg0) {
        currentLocation = locationClient.getLastLocation();
        Log.d("Location: ", currentLocation.toString());
        Geocoder geocoder = new Geocoder(this);
        
        try {
            List<Address> result = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
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
     * 位置情報を更新ボタンのクリック時処理
     */
    Button.OnClickListener updListener = new Button.OnClickListener() {
        public void onClick(View v) {
////////////////////////////////////////////////////////////////////////////////
//            // 位置情報の更新時は位置情報の取得上限を３回に設定（自動で３分間で３回位置情報を更新する設定とする）
//            if (3 != myLocationManager.getUpdateLimit()) {
//                myLocationManager.setUpdateLimit(3);
//            }
//            myLocationManager.stop();
//            myLocationManager.start();
////////////////////////////////////////////////////////////////////////////////
            
            locationClient.connect();
            naviView.loadUrl(URL);
        }
    };

    @Override
    protected void onResume() {
////////////////////////////////////////////////////////////////////////////////
//        if (myLocationManager.getCurrentLocation() == null) {
//            // 位置情報の取得を開始します。
//            myLocationManager.start();
//        }
////////////////////////////////////////////////////////////////////////////////
    	
        super.onResume();
        if (this.isOnCreate) {
            isOnCreate = false;
            return;
        }
        locationClient.connect();
    }

    @Override
    protected void onPause() {
////////////////////////////////////////////////////////////////////////////////
//        // 他画面へ遷移する場合は位置情報取得を止めます。
//        myLocationManager.stop();
////////////////////////////////////////////////////////////////////////////////
    	
        super.onPause();
        locationClient.disconnect();
    }

    protected void setLocation(Location loc) {
        currentLocation = loc;
    }

    /**
     * オプションメニューの生成
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menunavi, menu);
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
                new AlertDialog.Builder(NaviMapActivity.this)
                .setTitle("表示内容と操作方法")
                .setMessage(
                        "● 出発地をスタートアイコン、目的地を旗アイコン、現在地を赤い人型アイコンで表示します\n\n● 画面上部には現在地から目的地までの距離と時速４．２ｋｍで歩いた場合の到着予想時間を表示します\n※ 現在位置を更新する度に表示内容が最新情報で更新されます\n\n● 画面下部にはナビゲーションの文字情報が表示されます\n※ 現在位置を更新する度に表示内容が最新情報で更新されます\n\n\n● 現在位置を更新する場合は「現在位置を更新する」をタップしてください")
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
//            new AlertDialog.Builder(NaviMapActivity.this)
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
//            naviView.loadUrl(URL);
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
            if (null == currentLocation) {
            	isFailed = true;
                return startLat;
            }
        	isFailed = false;
            return currentLocation.getLatitude();
        }

        public double getLongitude() {
            if (null == currentLocation) {
            	isFailed = true;
                if (isOnCreate) {
                	Toast.makeText(NaviMapActivity.this.getApplicationContext(), "現在地を取得できませんでした", Toast.LENGTH_LONG).show();
                }
                return startLng;
            }
            if (isFailed) {
                if (isOnCreate) {
                	Toast.makeText(NaviMapActivity.this.getApplicationContext(), "現在地を取得できませんでした", Toast.LENGTH_LONG).show();
                }
              return startLng;
            }
        	isFailed = false;
            return currentLocation.getLongitude();
        }

        public double getStartLatitude() {
            return startLat;
        }

        public double getStartLongitude() {
            return startLng;
        }

        public double getDestLatitude() {
            return destLat;
        }

        public double getDestLongitude() {
            return destLng;
        }

        public boolean getDestExistFlg() {
            return destExistFlg;
        }

        public boolean getSaveFlg() {
            return saveFlg;
        }

        public void setSaveFlg() {
            saveFlg = true;
        }

        public void reload() {
            naviView.loadUrl(URL);
        }

        public void saveNaviHistory(String y, String m, String d, String time, String startLat, String startLng, String destLat, String destLng, String total) throws IOException {
            // 目的地の位置情報を住所に変換
            String strAddress = new String("");
            Geocoder geocoder = new Geocoder(NaviMapActivity.this, Locale.JAPAN);
            List<Address> list_address = geocoder.getFromLocation(Double.parseDouble(destLat), Double.parseDouble(destLng), 1);
            if (!list_address.isEmpty()){
                Address address = list_address.get(0);
                strAddress = address.getAddressLine(1);
            } else {
                Toast toast = Toast.makeText(NaviMapActivity.this, "位置情報から住所に変換に\nできませんでした", Toast.LENGTH_LONG);
                toast.show();
            }

            NaviHistory naviHistory = new NaviHistory();
            naviHistory.setYear(y);
            naviHistory.setMonth(m);
            naviHistory.setDay(d);
            naviHistory.setTime(time);
            naviHistory.setStartLat(startLat);
            naviHistory.setStartLng(startLng);
            naviHistory.setDestMemo("（メモ未設定）");
            naviHistory.setDestAddress(strAddress);
            naviHistory.setDestLat(destLat);
            naviHistory.setDestLng(destLng);
            naviHistory.setDistance(total);

            // 更新処理
            NaviHistoryDao dao = new NaviHistoryDao(NaviMapActivity.this);
            naviHistory = dao.save(naviHistory);
        }

        public boolean getRirekiFlg() {
            if ("1".equals(rireki)) {
                return true;
            } else {
                return false;
            }
        }

        public void setDebugPrint(String str) {
            Log.d("### call setDebugPrint() ### ", str);
        }
    }
}
