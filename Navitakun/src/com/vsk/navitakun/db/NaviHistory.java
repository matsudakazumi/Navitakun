package com.vsk.navitakun.db;

import java.io.Serializable;

@SuppressWarnings("serial")
public class NaviHistory implements Serializable {

    // テーブル名
    public static final String TABLE_NAME = "navi_history";

    // カラム名
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_YEAR = "year";
    public static final String COLUMN_MONTH = "month";
    public static final String COLUMN_DAY = "day";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_START_LAT = "start_lat";
    public static final String COLUMN_START_LNG = "start_lng";
    public static final String COLUMN_DEST_MEMO = "dest_memo";
    public static final String COLUMN_DEST_ADDRESS = "dest_address";
    public static final String COLUMN_DEST_LAT = "dest_lat";
    public static final String COLUMN_DEST_LNG = "dest_lng";
    public static final String COLUMN_DISTANCE = "distance";

    // プロパティ
    private Long rowid = null;
    private String year = null;
    private String month = null;
    private String day = null;
    private String time = null;
    private String startLat = null;
    private String startLng = null;
    private String destAddress = null;
    private String destMemo = null;
    private String destLat = null;
    private String destLng = null;
    private String distance = null;

    public Long getRowid() {
        return rowid;
    }

    public void setRowid(Long rowid) {
        this.rowid = rowid;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStartLat() {
        return startLat;
    }

    public void setStartLat(String startLat) {
        this.startLat = startLat;
    }

    public String getStartLng() {
        return startLng;
    }

    public void setStartLng(String startLng) {
        this.startLng = startLng;
    }

    public String getDestAddress() {
        return destAddress;
    }

    public void setDestAddress(String destAddress) {
        this.destAddress = destAddress;
    }

    public String getDestMemo() {
        return destMemo;
    }

    public void setDestMemo(String destMemo) {
        this.destMemo = destMemo;
    }

    public String getDestLat() {
        return destLat;
    }

    public void setDestLat(String destLat) {
        this.destLat = destLat;
    }

    public String getDestLng() {
        return destLng;
    }

    public void setDestLng(String destLng) {
        this.destLng = destLng;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    /**
     * ListView表示の際に利用するので日時 + 目的地の住所を返す
     */
    public String getDateTime() {
        StringBuilder builder = new StringBuilder();
        builder.append(getYear().toString()).append("/")
            .append(getMonth().toString()).append("/")
            .append(getDay().toString()).append(" ")
            .append(getTime());

        return builder.toString();
    }

    /**
     * ListView表示の際に利用するので日時 + 目的地の住所を返す
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getDestMemo()).append("\n")
            .append(getYear().toString()).append("/")
            .append(getMonth().toString()).append("/")
            .append(getDay().toString()).append(" ")
            .append(getTime()).append("\n")
            .append(getDestAddress());

        return builder.toString();
    }
}
