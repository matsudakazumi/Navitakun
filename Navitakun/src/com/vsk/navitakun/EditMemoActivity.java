package com.vsk.navitakun;

import com.vsk.navitakun.db.NaviHistory;
import com.vsk.navitakun.db.NaviHistoryDao;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class EditMemoActivity extends Activity {
    // 現在表示中のBizCardオブジェクト
    private NaviHistory naviHistory = null;

    // UI部品
    private TextView dateTv = null;
    private TextView destTv = null;
    private TextView distanceTv = null;
    private EditText edMemo = null;

    private Button saveButton = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 自動生成されたR.javaの定数を指定してXMLからレイアウトを生成
        setContentView(R.layout.editmemo);
        setTitleColor(Color.BLACK);

        // Intentから対象のBizCardオブジェクトを取得
        naviHistory = (NaviHistory) getIntent().getSerializableExtra( NaviHistory.TABLE_NAME);

        // UI部品の取得
        dateTv = (TextView) findViewById( R.id.date_data);
        destTv = (TextView) findViewById( R.id.dest_data);
        distanceTv = (TextView) findViewById( R.id.distance_data);
        edMemo = (EditText) findViewById( R.id.memo_txt);

        saveButton = (Button) findViewById(R.id.saveButton);

        // 表示内容の更新
        updateView();

        // ボタンリスナー追加
        saveButton.setOnClickListener(saveListener);
    }

    /**
     * 画面の表示内容を更新する
     */
    private void updateView(){
        String y = naviHistory.getYear();
        String m = naviHistory.getMonth();
        String d = naviHistory.getDay();
        String t = naviHistory.getTime();
        dateTv.setText(y + "/" + m + "/" +  d + " " + t);
        destTv.setText(naviHistory.getDestAddress());
        distanceTv.setText(naviHistory.getDistance() + "m");
        edMemo.setText(naviHistory.getDestMemo());
    }


    /**
     * 保存ボタンのクリック時処理
     */
    Button.OnClickListener saveListener = new Button.OnClickListener() {
        public void onClick(View v) {
            // 保存処理
Log.d("edMemo", edMemo.getText().toString());
            naviHistory.setDestMemo(edMemo.getText().toString());
            NaviHistoryDao dao = new NaviHistoryDao(EditMemoActivity.this);
            dao.save(naviHistory);
            // メッセージ表示
            Toast toast = Toast.makeText(EditMemoActivity.this, "保存しました", Toast.LENGTH_SHORT);
            toast.show();
            // 保存時に終了し、前のアクティビティへ戻る
            setResult(RESULT_OK);
            finish();
        }
    };

    /**
     * オプションメニューの生成
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menueditmemo, menu);
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
                new AlertDialog.Builder(EditMemoActivity.this)
                .setTitle("表示内容と操作方法")
                .setMessage(
                        "● メモ情報を入力して「保存」をタップするとＮＡＶＩ履歴情報とひも付けてメモ内容を記録します")
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
