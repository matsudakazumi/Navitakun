package com.vsk.navitakun;

import java.net.URLEncoder;

import com.vsk.navitakun.db.NaviHistory;
import com.vsk.navitakun.db.NaviHistoryDao;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class ShowDetailActivity extends Activity {
    // 現在表示中のBizCardオブジェクト
    private NaviHistory naviHistory = null;

    // UI部品
    private TextView memoTv = null;
    private TextView dateTv = null;
    private TextView destTv = null;
    private TextView keiroTv = null;
    private TextView distanceTv = null;

    private Button editButton = null;
    private Button deleteButton = null;
    private Button mailButton = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 自動生成されたR.javaの定数を指定してXMLからレイアウトを生成
        setContentView(R.layout.show);
        setTitleColor(Color.BLACK);

        // Intentから対象のBizCardオブジェクトを取得
        naviHistory = (NaviHistory) getIntent().getSerializableExtra( NaviHistory.TABLE_NAME);

        // UI部品の取得
        memoTv = (TextView) findViewById( R.id.hist_memo);
        dateTv = (TextView) findViewById( R.id.date_data);
        destTv = (TextView) findViewById( R.id.dest_data);
        keiroTv = (TextView) findViewById( R.id.keiro_data);
        distanceTv = (TextView) findViewById( R.id.distance_data);

        editButton = (Button) findViewById(R.id.editButton);
        deleteButton = (Button) findViewById(R.id.deleteButton);
        mailButton = (Button) findViewById(R.id.mailButton);

        // 表示内容の更新
        updateView();

        // ボタンリスナー追加
        editButton.setOnClickListener(editListener);
        deleteButton.setOnClickListener(deleteListener);
        mailButton.setOnClickListener(mailListener);

        // AdMob
        AdView adView = (AdView)this.findViewById(R.id.ad2);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    /**
     * 画面の表示内容を更新する
     */
    private void updateView(){
        memoTv.setText(naviHistory.getDestMemo());
        String y = naviHistory.getYear();
        String m = naviHistory.getMonth();
        String d = naviHistory.getDay();
        String t = naviHistory.getTime();
        dateTv.setText(y + "/" + m + "/" +  d + " " + t);
        destTv.setText(naviHistory.getDestAddress());
        String link = "ＮＡＶＩの経路を地図で見る";
        SpannableString keiroSpan = new SpannableString(link);
        keiroSpan.setSpan(new KeiroSpan(), 0, link.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        keiroTv.setText(keiroSpan);
        keiroTv.setMovementMethod(LinkMovementMethod.getInstance());
        distanceTv.setText(naviHistory.getDistance() + "m");
    }

    /**
     * 経路リンクがクリックされた際の処理
     */
    class KeiroSpan extends ClickableSpan {
        @Override
        public void onClick(View widget) {
            // 住所を詰めてMapActivityを起動
            Intent mapIntent = new Intent(ShowDetailActivity.this, KeiroMapActivity.class);
            mapIntent.putExtra("start_lat", naviHistory.getStartLat());
            mapIntent.putExtra("start_lng", naviHistory.getStartLng());
            mapIntent.putExtra("dest_lat", naviHistory.getDestLat());
            mapIntent.putExtra("dest_lng", naviHistory.getDestLng());
            mapIntent.putExtra("rireki", "1");
            startActivity( mapIntent);
        }
    }

    /**
     * 編集画面の結果に応じて画面をリフレッシュ
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( resultCode == RESULT_OK){
            // データベースから再読み込み
            NaviHistoryDao dao = new NaviHistoryDao(this);
            naviHistory = dao.load(naviHistory.getRowid());
            // 表示を更新
            updateView();
        }
    }

    /**
     * アクティビティが前面に来るたびにデータを更新
     */
    @Override
    protected void onResume() {
        super.onResume();

        // データベースから再読み込み
        NaviHistoryDao dao = new NaviHistoryDao(this);
        naviHistory = dao.load(naviHistory.getRowid());
        // 表示を更新
        updateView();
    }

    /**
     * メモ編集ボタンのクリック時処理
     */
    Button.OnClickListener editListener = new Button.OnClickListener() {
        public void onClick(View v) {
            // 現在表示中のエンティティを詰めて編集画面へ
            Intent editIntent = new Intent(ShowDetailActivity.this, EditMemoActivity.class);
            editIntent.putExtra(NaviHistory.TABLE_NAME, naviHistory);
            startActivityForResult(editIntent, -1);
        }
    };

    /**
     * 削除ボタンのクリック時処理
     */
    Button.OnClickListener deleteListener = new Button.OnClickListener() {
        public void onClick(View v) {
            // 確認ダイアログの表示
            AlertDialog.Builder builder = new AlertDialog.Builder(ShowDetailActivity.this);
            // アイコン設定
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            // タイトル設定
            builder.setTitle("削除してよろしいですか？");
            // OKボタン設定
            builder.setPositiveButton( android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // 削除処理
                    NaviHistoryDao dao = new NaviHistoryDao(ShowDetailActivity.this);
                    dao.delete(naviHistory);
                    // メッセージ表示
                    Toast toast = Toast.makeText(ShowDetailActivity.this, "削除しました", Toast.LENGTH_SHORT);
                    toast.show();
                    finish();
                }
            });
            // キャンセルボタン設定
            builder.setNegativeButton( android.R.string.cancel, null);
            // ダイアログの表示
            builder.show();
        }
    };

    /**
     * 目的地をメールで共有ボタンのクリック時処理
     */
    Button.OnClickListener mailListener = new Button.OnClickListener() {
        public void onClick(View v) {
            // 情報ウィンドウに表示する文字情報の生成
            String infoStr = "";
            try {
                infoStr = URLEncoder.encode(naviHistory.getDestMemo(), "UTF-8");
            } catch (Throwable t) {
                // メッセージ表示
                Toast toast = Toast.makeText(ShowDetailActivity.this, "ＵＲＬエンコーディングに失敗しました", Toast.LENGTH_SHORT);
                toast.show();
                // 変換に失敗した場合は「ここです！」を設定する
                infoStr = "%e3%81%93%e3%81%93%e3%81%a7%e3%81%99%ef%bc%81";
            }

            // GoogleMapsURLの生成
            StringBuilder strUrl = new StringBuilder();
            strUrl.append("http://maps.google.co.jp/maps?f=q&hl=ja&geocode=&q=loc:");
            strUrl.append(naviHistory.getDestLat());
            strUrl.append(",");
            strUrl.append(naviHistory.getDestLng());
            strUrl.append("+(");
            strUrl.append(infoStr);
            strUrl.append(")&ie=UTF8&ll=");
            strUrl.append(naviHistory.getDestLat());
            strUrl.append(",");
            strUrl.append(naviHistory.getDestLng());
            strUrl.append("&z=18");

            // メールで目的地を共有
            Intent mailIntent = new Intent();
            mailIntent.setAction(Intent.ACTION_SEND);
            mailIntent.setType("message/rfc822");
            mailIntent.putExtra(Intent.EXTRA_SUBJECT, "場所のお知らせ（なび太くん）");
            String honbun = naviHistory.getDestMemo() + "\n\n" + strUrl.toString();
            mailIntent.putExtra(Intent.EXTRA_TEXT, honbun);
            startActivity(mailIntent);
        }
    };

    /**
     * オプションメニューの生成
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menushow, menu);
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
                new AlertDialog.Builder(ShowDetailActivity.this)
                .setTitle("表示内容と操作方法")
                .setMessage(
                        "● 「ＮＡＶＩの経路を地図で見る」リンクをタップすると目的地や経路を地図上で確認することができます\n\n● 「メモ編集」をタップするとＮＡＶＩ履歴に任意のタイトルやメモ情報などを書き込むことができます\n\n● 「履歴削除」をタップすると現在表示しているＮＡＶＩ履歴の記録を削除します\n\n● 「目的地をメールで共有」をタップするとメール本文に目的地の位置情報リンクを自動設定した状態でメーラーを起動します\n※ メールに設定されたリンクからGoogleMapやブラウザを利用して位置情報を確認することができます")
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
