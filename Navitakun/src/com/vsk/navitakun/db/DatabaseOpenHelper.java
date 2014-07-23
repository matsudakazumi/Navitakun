package com.vsk.navitakun.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseOpenHelper extends SQLiteOpenHelper {
    // データベース名の定数
    private static final String DB_NAME = "NAVITAKUN";

    /**
     * コンストラクタ
     */
    public DatabaseOpenHelper(Context context) {
        // 指定したデータベース名が存在しない場合は、新たに作成されonCreate()が呼ばれる
        // バージョンを変更するとonUpgrade()が呼ばれる
        super(context, DB_NAME, null, 1);
    }

    /**
     * データベースの生成に呼び出されるので、 スキーマの生成を行う
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();

        try{
            // テーブルの生成
            StringBuilder createSql = new StringBuilder();
            createSql.append("create table " + NaviHistory.TABLE_NAME + " (");
            createSql.append(NaviHistory.COLUMN_ID + " integer primary key autoincrement not null,");
            createSql.append(NaviHistory.COLUMN_YEAR + " text not null,");
            createSql.append(NaviHistory.COLUMN_MONTH + " text not null,");
            createSql.append(NaviHistory.COLUMN_DAY + " text not null,");
            createSql.append(NaviHistory.COLUMN_TIME + " text not null,");
            createSql.append(NaviHistory.COLUMN_START_LAT + " text not null,");
            createSql.append(NaviHistory.COLUMN_START_LNG + " text not null,");
            createSql.append(NaviHistory.COLUMN_DEST_MEMO + " text,");
            createSql.append(NaviHistory.COLUMN_DEST_ADDRESS + " text,");
            createSql.append(NaviHistory.COLUMN_DEST_LAT + " text not null,");
            createSql.append(NaviHistory.COLUMN_DEST_LNG + " text not null,");
            createSql.append(NaviHistory.COLUMN_DISTANCE + " text not null");
            createSql.append(")");

            db.execSQL(createSql.toString());
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * データベースの更新
     *
     * 親クラスのコンストラクタに渡すversionを変更したときに呼び出される
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // データベースの更新
    }

}
