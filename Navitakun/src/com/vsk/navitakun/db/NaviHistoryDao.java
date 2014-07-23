package com.vsk.navitakun.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class NaviHistoryDao {
    private DatabaseOpenHelper helper = null;

    public NaviHistoryDao(Context context) {
        helper = new DatabaseOpenHelper(context);
    }

    /**
     * Navi履歴の保存
     * rowidがnullの場合はinsert、rowidが!nullの場合はupdate
     * @param naviHistory 保存対象のオブジェクト
     * @return 保存結果
     */
    public NaviHistory save(NaviHistory naviHistory){
        SQLiteDatabase db = helper.getWritableDatabase();
        NaviHistory result = null;
        try {
            ContentValues values = new ContentValues();
            values.put( NaviHistory.COLUMN_YEAR, naviHistory.getYear());
            values.put( NaviHistory.COLUMN_MONTH, naviHistory.getMonth());
            values.put( NaviHistory.COLUMN_DAY, naviHistory.getDay());
            values.put( NaviHistory.COLUMN_TIME, naviHistory.getTime());
            values.put( NaviHistory.COLUMN_START_LAT, naviHistory.getStartLat());
            values.put( NaviHistory.COLUMN_START_LNG, naviHistory.getStartLng());
            values.put( NaviHistory.COLUMN_DEST_MEMO, naviHistory.getDestMemo());
            values.put( NaviHistory.COLUMN_DEST_ADDRESS, naviHistory.getDestAddress());
            values.put( NaviHistory.COLUMN_DEST_LAT, naviHistory.getDestLat());
            values.put( NaviHistory.COLUMN_DEST_LNG, naviHistory.getDestLng());
            values.put( NaviHistory.COLUMN_DISTANCE, naviHistory.getDistance());

            Long rowId = naviHistory.getRowid();
            // IDがnullの場合はinsert
            if( rowId == null){
                rowId = db.insert( NaviHistory.TABLE_NAME, null, values);
            }
            else{
                db.update(NaviHistory.TABLE_NAME, values, NaviHistory.COLUMN_ID + "=?", new String[]{String.valueOf(rowId)});
            }
            result = load(rowId);
        } finally {
            db.close();
        }
        return result;
    }

    /**
     * レコードの削除
     * @param naviHistory 削除対象のオブジェクト
     */
    public void delete(NaviHistory naviHistory) {
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            db.delete(NaviHistory.TABLE_NAME, NaviHistory.COLUMN_ID + "=?", new String[]{String.valueOf(naviHistory.getRowid())});
        } finally {
            db.close();
        }
    }

    /**
     * idでNaviHistoryをロードする
     * @param rowId PK
     * @return ロード結果
     */
    public NaviHistory load(Long rowId) {
        SQLiteDatabase db = helper.getReadableDatabase();

        NaviHistory naviHistory = null;
        try {
            Cursor cursor = db.query(NaviHistory.TABLE_NAME, null, NaviHistory.COLUMN_ID + "=?", new String[]{String.valueOf(rowId)}, null, null, null);
            cursor.moveToFirst();
            naviHistory = getNaviHistory(cursor);
        } finally {
            db.close();
        }
        return naviHistory;
    }

    /**
     * 一覧を取得する
     * @return 検索結果
     */
    public List<NaviHistory> list() {
        SQLiteDatabase db = helper.getReadableDatabase();

        List<NaviHistory> naviHistoryList;
        try {
            Cursor cursor = db.query(NaviHistory.TABLE_NAME, null, null, null, null, null, NaviHistory.COLUMN_ID + " DESC");
            naviHistoryList = new ArrayList<NaviHistory>();
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                naviHistoryList.add(getNaviHistory(cursor));
                cursor.moveToNext();
            }
        } finally {
            db.close();
        }
        return naviHistoryList;
    }

    /**
     * カーソルからオブジェクトへの変換
     * @param cursor カーソル
     * @return 変換結果
     */
    private NaviHistory getNaviHistory(Cursor cursor){
        NaviHistory naviHistory = new NaviHistory();

        naviHistory.setRowid(cursor.getLong(0));
        naviHistory.setYear(cursor.getString(1));
        naviHistory.setMonth(cursor.getString(2));
        naviHistory.setDay(cursor.getString(3));
        naviHistory.setTime(cursor.getString(4));
        naviHistory.setStartLat(cursor.getString(5));
        naviHistory.setStartLng(cursor.getString(6));
        naviHistory.setDestMemo(cursor.getString(7));
        naviHistory.setDestAddress(cursor.getString(8));
        naviHistory.setDestLat(cursor.getString(9));
        naviHistory.setDestLng(cursor.getString(10));
        naviHistory.setDistance(cursor.getString(11));
        return naviHistory;
    }
}
