package com.vsk.navitakun;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class DestMapActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {

    // UI部品
    private Button keiroButton = null;
    private Button naviButton = null;

    private static final String URL = "file:///android_asset/destwebview.html";
    private WebView destwebView;
    private JsObj jsobj;

    protected boolean destExistFlg = false;

    // 位置情報管理クラス
    private LocationClient locationClient;
//    protected MyLocationManager myLocationManager;

    // 現在位置情報
    protected Location location;

    // 出発地位置情報
    protected double startLat;
    protected double startLng;

    // 目的地位置情報
    protected double destLat;
    protected double destLng;

    // ダミー目的地位置情報（確定前）
    protected double destDummyLat;
    protected double destDummyLng;

    // 目的地設定の有無フラグ
    protected boolean dialogFlg;
    
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
        setContentView(R.layout.destwebview);
        setTitleColor(Color.BLACK);
        getWindow().setFeatureDrawableResource(Window.FEATURE_RIGHT_ICON, R.drawable.ic_launcher);
        destwebView = (WebView) findViewById(R.id.destwebview);

        // UI部品の取得
        keiroButton = (Button) findViewById(R.id.keiroButton);
        naviButton = (Button) findViewById(R.id.naviButton);

        // Intentから入力された住所を取得
        String strAddress = (String) getIntent().getSerializableExtra("input_address");
        if (null != strAddress && strAddress.length() > 0) {
            // 座標取得用のGeocoder取得
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                // 住所から位置情報へ変換
                List<Address> addressList = geocoder.getFromLocationName(strAddress, 10);
                Address address = null;
                if (addressList.isEmpty()) {
                    String message = getResources().getString(R.string.cannot_get_address);
                    Toast toast = Toast.makeText(this, message + "[" + strAddress + "]", Toast.LENGTH_LONG);
                    toast.show();
                } else if (addressList.size() == 1) {
                    address = addressList.get(0);
                } else {
                    // 複数のアドレスが見つかった場合はダイアログで対象を選択する
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setTitle(R.string.select_target);
                    // ダイアログ表示用の文字列の生成
                    List<String> strAddressList = new ArrayList<String>();
                    for (Address element : addressList) {
                        int maxAddressLineIdx = element.getMaxAddressLineIndex();
                        strAddressList.add(element.getAddressLine(maxAddressLineIdx));
                    }
                    // ダイアログに表示するリストの生成
                    AddressSelectionListener listener = new AddressSelectionListener(addressList);
                    ListView listView = new ListView(this);
                    ArrayAdapter<String> listAdaptor = new ArrayAdapter<String>(this,
                            android.R.layout.simple_list_item_1, strAddressList);
                    listView.setAdapter(listAdaptor);
                    listView.setOnItemClickListener(listener);
                    // ダイアログにリストを生成
                    alertDialogBuilder.setView(listView);
                    // ダイアログの表示
                    AlertDialog dialog = alertDialogBuilder.create();
                    listener.setDialog(dialog);
                    dialog.show();
                }

                if (address != null) {
                    destLat = address.getLatitude();
                    destLng = address.getLongitude();
                    destExistFlg = true;
                }
            } catch (IOException e) {
                Toast toast = Toast.makeText(this, R.string.cannot_get_address, Toast.LENGTH_LONG);
                toast.show();
            }
        }

        String strDestLat = (String) getIntent().getSerializableExtra("lat");
        String strDestLng = (String) getIntent().getSerializableExtra("lng");
        if (null != strDestLat && Double.parseDouble(strDestLat) != 0 && null != strDestLng
                && Double.parseDouble(strDestLng) != 0) {
            destLat = Double.parseDouble(strDestLat);
            destLng = Double.parseDouble(strDestLng);
            destExistFlg = true;
        }

        WebSettings webSettings = destwebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        jsobj = new JsObj(this.getApplicationContext());
        destwebView.addJavascriptInterface(jsobj, "android");
        destwebView.loadUrl(URL);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

////////////////////////////////////////////////////////////////////////////////
//        myLocationManager = new MyLocationManager((LocationManager) getSystemService(LOCATION_SERVICE));
////////////////////////////////////////////////////////////////////////////////

        // ボタンリスナー追加
        keiroButton.setOnClickListener(keiroListener);
        naviButton.setOnClickListener(naviListener);

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
            destwebView.loadUrl(URL);
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
     * 経路ボタンのクリック時処理
     */
    Button.OnClickListener keiroListener = new Button.OnClickListener() {
        public void onClick(View v) {
            if (destExistFlg) {
                // 出発地と目的地の位置情報をインテントに設定
                Intent intent = new Intent(DestMapActivity.this, KeiroMapActivity.class);
                intent.putExtra("start_lat", Double.toString(startLat));
                intent.putExtra("start_lng", Double.toString(startLng));
                intent.putExtra("dest_lat", Double.toString(destLat));
                intent.putExtra("dest_lng", Double.toString(destLng));
                // KeiroMapActivity起動
                startActivity(intent);
            } else {
                Toast toast = Toast.makeText(DestMapActivity.this, "目的地が設定されていません", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    };

    /**
     * ＮＡＶＩボタンのクリック時処理
     */
    Button.OnClickListener naviListener = new Button.OnClickListener() {
        public void onClick(View v) {
            if (destExistFlg) {
                Intent intent = new Intent(DestMapActivity.this, NaviMapActivity.class);
                // 出発地と目的地の位置情報をインテントに設定
                intent.putExtra("start_lat", Double.toString(startLat));
                intent.putExtra("start_lng", Double.toString(startLng));
                intent.putExtra("dest_lat", Double.toString(destLat));
                intent.putExtra("dest_lng", Double.toString(destLng));

                // NaviMapActivity起動
                startActivity(intent);
            } else {
                Toast toast = Toast.makeText(DestMapActivity.this, "目的地が設定されていません", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    };

    @Override
    protected void onResume() {
//        Log.d("### call onResume() ### ", "Start");
//        if (myLocationManager.getCurrentLocation() == null) {
//            // 位置情報の取得を開始します。
//            myLocationManager.start();
//        }
        super.onResume();
        if (this.isOnCreate) {
            isOnCreate = false;
        } else {
            locationClient.connect();
        }

        // 目的地が設定されていない場合
        if (!destExistFlg) {
            Toast toast = Toast.makeText(this, "地図上で目的地を\nタップしてください", Toast.LENGTH_LONG);
            toast.show();
        } else {
            Toast toast = Toast.makeText(this, "現在地と目的地のアイコンは\nドラッグして位置情報を\n微調整することができます", Toast.LENGTH_LONG);
            toast.show();
        }
//        Log.d("### call onResume() ### ", "End");
    }

    @Override
    protected void onPause() {
        // 他画面へ遷移する場合は位置情報取得を止めます。
//        myLocationManager.stop();
        super.onPause();
        locationClient.disconnect();
    }

    /**
     * オプションメニューの生成
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menudestwebmap, menu);
        return true;
    }

    /**
     * オプションメニューの選択
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {

            /* 目的地をメールで共有する */
            case R.id.menu_mail:
                // 目的地が設定されている場合
                if (destExistFlg) {
                    // 「ここです！」固定で設定する
                    String infoStr = "%e3%81%93%e3%81%93%e3%81%a7%e3%81%99%ef%bc%81";
                    // GoogleMapsURLの生成
                    StringBuilder strUrl = new StringBuilder();
                    strUrl.append("http://maps.google.co.jp/maps?f=q&hl=ja&geocode=&q=loc:");
                    strUrl.append(destLat);
                    strUrl.append(",");
                    strUrl.append(destLng);
                    strUrl.append("+(");
                    strUrl.append(infoStr);
                    strUrl.append(")&ie=UTF8&ll=");
                    strUrl.append(destLat);
                    strUrl.append(",");
                    strUrl.append(destLng);
                    strUrl.append("&z=18");

                    // メールで目的地を共有
                    Intent mailIntent = new Intent();
                    mailIntent.setAction(Intent.ACTION_SEND);
                    mailIntent.setType("message/rfc822");
                    mailIntent.putExtra(Intent.EXTRA_SUBJECT, "場所のお知らせ（なび太くん）");
                    mailIntent.putExtra(Intent.EXTRA_TEXT, strUrl.toString());
                    startActivity(mailIntent);
                    break;

                // 目的地が設定されていない場合
                } else {
                    String message = getResources().getString(R.string.no_destination);
                    Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
                    toast.show();
                    break;
                }

            /* 現在位置を更新メニュー選択時の処理 */
            case R.id.menu_updateCurrentLocation:
                this.locationClient.connect();
                destwebView.loadUrl(URL);
                break;
                    
            /* 目的地を消す */
            case R.id.menu_del:
                // 目的地が設定されている場合
                if (destExistFlg) {
                    destLat = 0;
                    destLng = 0;
                    destExistFlg = false;
                    destwebView.loadUrl(URL);
                // 目的地が設定されていない場合
                } else {
                    String message = getResources().getString(R.string.no_destination);
                    Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
                    toast.show();
                }
                break;

            /* この画面の操作方法メニュー選択時の処理 */
            case R.id.menu_help:
                new AlertDialog.Builder(DestMapActivity.this)
                .setTitle("表示内容と操作方法")
                .setMessage(
                        "● 設定した出発地のアイコン（人型）と目的地のアイコン（下向き矢印）はドラッグ＆ドロップで位置を調整することができます\n\n\n● 地図上で目的地を設定する場合は該当する位置をタップしてください\n\n● 設定した出発地から目的地までの経路を確認する場合は「経路表示」をタップしてください\n\n● 設定した出発地から目的地までのナビゲーションを開始する場合は「ＮＡＶＩ」をタップしてください\n\n● 設定した目的地の位置情報をメールで共有する場合はメニューの「目的地をメールで共有」をタップしてください\n\n● 設定した目的地のアイコンを消す場合はメニューの「目的地を消す」をタップしてください")
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

    /**
     * 住所の候補が複数あった場合にリストで選択された住所を表示するリスナ
     */
    class AddressSelectionListener implements OnItemClickListener {
        private List<Address> listAddress = null;
        private AlertDialog dialog = null;

        private AddressSelectionListener(List<Address> listAddress) {
            this.listAddress = listAddress;
        }

        public AlertDialog getDialog() {
            return dialog;
        }

        public void setDialog(AlertDialog dialog) {
            this.dialog = dialog;
        }

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // 選択された住所を目的地として設定
            Address address = listAddress.get(position);
            destLat = address.getLatitude();
            destLng = address.getLongitude();
            destExistFlg = true;
            // ダイアログを閉じる
            dialog.dismiss();
            destwebView.loadUrl(URL);
        }
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
//            new AlertDialog.Builder(DestMapActivity.this)
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
//                Toast.makeText(getApplicationContext(), "現在地を取得しています。", Toast.LENGTH_LONG).show();
//            }
//        }
//
//        @Override
//        protected void onLocationTimeout() {
//            if (getCurrentLocation() == null) {
//                Toast.makeText(getApplicationContext(), "一時的に現在地を検出できません。", Toast.LENGTH_LONG).show();
//            }
//        }
//
//        @Override
//        protected void onUpdateLocation(final Location location, final int updateCount) {
//            setLocation(location);
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
            if (null == location) {
                // 初期値（東京都庁）
                return 35.689506;
            }
            return location.getLatitude();
        }

        public double getLongitude() {
            if (null == location) {
                // 初期値（東京都庁）
                return 139.691701;
            }
            return location.getLongitude();
        }

        public void setLatitude(double lat) {
            location.setLatitude(lat);
        }

        public void setLongitude(double lng) {
            location.setLongitude(lng);
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

        public void setDestLatitude(double lat) {
            destLat = lat;
        }

        public void setDestLongitude(double lng) {
            destLng = lng;
        }

        public void setDestDummyLatitude(double lat) {
            destDummyLat = lat;
        }

        public void setDestDummyLongitude(double lng) {
            destDummyLng = lng;
        }

        public double getStartLatitude() {
            return startLat;
        }

        public double getStartLongitude() {
            return startLng;
        }

        public void setStartLatitude(double lat) {
            startLat = lat;
        }

        public void setStartLongitude(double lng) {
            startLng = lng;
        }

        public void setDebugPrint(String str) {
            Log.d("### call setDebugPrint() ### ", str);
        }

        public void dispDialog() {
            // 地図上でタップした場所を目的地として設定するかどうかを確認するダイアログを起動します。
            new AlertDialog.Builder(DestMapActivity.this)
                    .setTitle("選択")
                    .setMessage("タップした場所を目的地として設定しますか？")
                    .setPositiveButton("する", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            destLat = destDummyLat;
                            destLng = destDummyLng;
                            destExistFlg = true;
                            destDummyLat = 0.0d;
                            destDummyLng = 0.0d;
                            Toast.makeText(DestMapActivity.this.getApplicationContext(), "現在地と目的地のアイコンは\nドラッグして位置情報を\n微調整することができます", Toast.LENGTH_LONG).show();
                            destwebView.loadUrl(URL);
                        }
                    }).setNegativeButton("しない", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            destDummyLat = 0.0d;
                            destDummyLng = 0.0d;
                        }
                    }).create().show();
        }
    }
}
