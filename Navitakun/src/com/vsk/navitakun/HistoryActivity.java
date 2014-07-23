package com.vsk.navitakun;

import java.util.ArrayList;
import java.util.Calendar;

import com.vsk.navitakun.db.DatabaseOpenHelper;
import com.vsk.navitakun.db.NaviHistory;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class HistoryActivity extends ListActivity {

    // UI部品
    private TextView monthData = null;
    private TextView totalData = null;

    private NaviHistory history;
    private ArrayList<NaviHistory> historyList;

    private DatabaseOpenHelper helper;
    private SQLiteDatabase db;

    int month = 0;
    int total = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 自動生成されたR.javaの定数を指定してXMLからレイアウトを生成
        setContentView(R.layout.list);

        // 合計表示TexViewのバインド
        monthData = (TextView) findViewById(R.id.month_data);
        totalData = (TextView) findViewById(R.id.total_data);

        helper = new DatabaseOpenHelper(this.getApplicationContext());
        db = helper.getReadableDatabase();
        updateView();

    }

    /**
     * List要素クリック時の処理
     *
     * 選択されたエンティティを詰めて参照画面へ遷移する
     */
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        // 選択された要素を取得する
        NaviHistory naviHistory = (NaviHistory) parent.getItemAtPosition(position);
        // 参照画面へ遷移する明示的インテントを生成
        Intent detailIntent = new Intent(this, ShowDetailActivity.class);
        // 選択されたオブジェクトをインテントに詰める
        detailIntent.putExtra(NaviHistory.TABLE_NAME, naviHistory);
        // アクティビティを開始する
        startActivity(detailIntent);
    }

    /**
     * アクティビティが前面に来るたびにデータを更新
     */
    @Override
    public void onResume(){
        super.onResume();
        updateView();
    }

    private void updateView(){
        historyList = new ArrayList<NaviHistory>();
        month = 0;
        total = 0;

        // ＮＡＶＩ履歴データの取得
        String sql = "select * from navi_history order by _id desc;";
        Cursor c = db.rawQuery(sql, null);
        int irecNum = c.getCount();
        c.moveToFirst();

        for  (int i=0; i < irecNum; i++){
            history = new NaviHistory();
            history.setRowid(Long.valueOf(c.getString(0)));
            history.setYear(c.getString(1));
            history.setMonth(c.getString(2));
            history.setDay(c.getString(3));
            history.setTime(c.getString(4));
            history.setStartLat(c.getString(5));
            history.setStartLng(c.getString(6));
            history.setDestMemo(c.getString(7));
            history.setDestAddress(c.getString(8));
            history.setDestLat(c.getString(9));
            history.setDestLng(c.getString(10));
            history.setDistance(c.getString(11));
            historyList.add(history);
            c.moveToNext();
        }
        c.close();

        ListAdapter adapter = new ListAdapter(this, historyList);
        setListAdapter(adapter);

        // 距離の取得と表示
        Calendar calendar = Calendar.getInstance();
        int y = calendar.get(Calendar.YEAR);
        int m = calendar.get(Calendar.MONTH) + 1;
        for (int i = 0; i < historyList.size(); i++) {
            NaviHistory nh = historyList.get(i);
            total = total + Integer.parseInt(nh.getDistance());
            if (y == Integer.parseInt(nh.getYear()) && m == Integer.parseInt(nh.getMonth())) {
                month = month + Integer.parseInt(nh.getDistance());
            }
        }
        String strMonth = Integer.valueOf(month).toString();
        String strTotal = Integer.valueOf(total).toString();
        String resultMonth = "";
        String resultTotal = "";
        if (strMonth.length() == 1) {
            resultMonth = "0.00" + strMonth + "km";
        } else if (strMonth.length() == 2) {
            resultMonth = "0.0" + strMonth + "km";
        } else if (strMonth.length() == 3) {
            resultMonth = "0." + strMonth + "km";
        } else {
            resultMonth = strMonth.substring(0, strMonth.length()-3) + "." + strMonth.substring(strMonth.length()-3) + "km";
        }
        if (strTotal.length() == 1) {
            resultTotal = "0.00" + strTotal + "km";
        } else if (strTotal.length() == 2) {
            resultTotal = "0.0" + strTotal + "km";
        } else if (strTotal.length() == 3) {
            resultTotal = "0." + strTotal + "km";
        } else {
            resultTotal = strTotal.substring(0, strTotal.length()-3) + "." + strTotal.substring(strTotal.length()-3) + "km";
        }

        monthData.setText(resultMonth);
        totalData.setText(resultTotal);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // 選択された要素を取得する
        NaviHistory naviHistory = (NaviHistory) historyList.get(position);
        // 参照画面へ遷移する明示的インテントを生成
        Intent detailIntent = new Intent(this, ShowDetailActivity.class);
        // 選択されたオブジェクトをインテントに詰める
        detailIntent.putExtra(NaviHistory.TABLE_NAME, naviHistory);
        // アクティビティを開始する
        startActivity(detailIntent);
    }

    /**
     * オプションメニューの生成
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menuhistory, menu);
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
                new AlertDialog.Builder(HistoryActivity.this)
                .setTitle("表示内容と操作方法")
                .setMessage(
                        "● 画面上部に歩いた距離（今月の合計と全体の合計）を表示します\n※履歴情報はナビゲーションを開始した際に自動登録されます\n\n● ＮＡＶＩ履歴のリストデータをタップすると詳細情報が確認できます")
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
