package com.vsk.navitakun;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

/**
 * 可能であれば、GPS プロバイダとネットワークプロバイダを同時に使用して最良の位置情報を取得する位置情報取得機能を提供します。<p>
 *
 */
public abstract class BetterLocationManager {

    /**
     * 位置情報サービスを保持します。
     */
    private LocationManager locationManager;

    /**
     * コンストラクタです。
     *
     * @param locationManager 位置情報サービス
     */
    public BetterLocationManager(final LocationManager locationManager) {
        this.locationManager = locationManager;
    }

    //////////////////////////////////////////////////////////////////////////
    // Last Known Location

    /**
     * GPS プロバイダとネットワークプロバイダを使用して最良の LastKnownLocation を取得して返します。<p>
     * このメソッドは利便性の為に提供しています。
     *
     * @return 最良の LastKnownLocation または <code>null</code>
     * @see {@link LocationManager#getLastKnownLocation(String)}
     */
    public Location getLastKnownLocation() {
        return getLastKnownLocation(new String[]{ LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER });
    }

    /**
     * 指定された位置情報プロバイダ識別子の列挙を使用して最良の LastKnownLocation を取得して返します。
     *
     * @param providers 位置情報プロバイダ識別子の列挙
     * @return 最良の LastKnownLocation または <code>null</code>
     * @throws IllegalArgumentException 位置情報プロバイダ識別子が不正な場合
     * @see {@link LocationManager#getLastKnownLocation(String)}
     */
    public Location getLastKnownLocation(final String[] providers) throws IllegalArgumentException {
//        Log.d("### call BetterLocationManager.getLastKnownLocation() ### ", "Start");
        if (locationManager == null || providers == null) {
            return null;
        }

        Location result = null;
        for (final String provider : providers) {
            if (provider != null && locationManager.isProviderEnabled(provider)) {
                final Location lastKnownLocation = locationManager.getLastKnownLocation(provider);
                if (isBetterLocation(result, lastKnownLocation)) {
                    result = lastKnownLocation;
                }
            }
        }
//        Log.d("### call BetterLocationManager.getLastKnownLocation() ### ", "End");
        return result;
    }

    //////////////////////////////////////////////////////////////////////////
    // Listening for location updates

    /**
     * 位置情報プロバイダが保持している最後に受信した位置情報を使用するかどうかのデフォルト値です。
     */
    public static final boolean DEFAULT_USE_LAST_KNOWN_LOCATION = true;

    /**
     * 位置情報プロバイダが保持している最後に受信した位置情報を使用するかどうかを保持します。
     */
    private boolean useLastKnownLocation = DEFAULT_USE_LAST_KNOWN_LOCATION;

    /**
     * 位置情報プロバイダが保持している最後に受信した位置情報を使用するかどうかを返します。
     *
     * @return 位置情報プロバイダが保持している最後に受信した位置情報を使用するかどうか
     */
    public boolean isUseLastKnownLocation() { return useLastKnownLocation; }

    /**
     * 位置情報プロバイダが保持している最後に受信した位置情報を使用するかどうかを設定します。
     *
     * @param useLastKnownLocation 位置情報プロバイダが保持している最後に受信した位置情報を使用するかどうか
     */
    public void setUseLastKnownLocation(final boolean useLastKnownLocation) { this.useLastKnownLocation = useLastKnownLocation; }

    /**
     * 位置情報の情報としての鮮度を有効とする時間 (ミリ秒) を保持します。
     */
    private long significantlyNewer = 5 * 1000L;

    public long getSignificantlyNewer() { return significantlyNewer; }

    public void setSignificantlyNewer(final long significantlyNewer) { this.significantlyNewer = significantlyNewer; }

    /**
     * 位置情報タイマを保持します。
     */
    private Timer locationTimer;

    /**
     * 位置情報取得のデフォルトの開始遅延時間 (ミリ秒) です。
     */
    public static final long DEFAULT_DELAY = 0L;

    /**
     * 位置情報取得の開始遅延時間 (ミリ秒) を保持します。
     */
    private long delay = DEFAULT_DELAY;

    /**
     * 位置情報取得の開始遅延時間 (ミリ秒) を返します。
     *
     * @return 位置情報取得の開始遅延時間 (ミリ秒)
     */
    public long getDelay() { return delay; }

    /**
     * 位置情報取得の開始遅延時間 (ミリ秒) を設定します。<p>
     * <code>0</code> より小さい値が指定された場合は、<code>0</code> が設定されます。
     *
     * @param delay 位置情報取得の開始遅延時間 (ミリ秒)
     */
    public void setDelay(final long delay) { this.delay = delay < 0L ? 0L : delay; }

    /**
     * 位置情報取得の実行間隔時間 (ミリ秒) です。
     */
    public static final long DEFAULT_INTERVAL = 5 * 1000L;

    /**
     * 位置情報取得の実行間隔時間 (ミリ秒) を保持します。
     */
    long interval = DEFAULT_INTERVAL;

    /**
     * 位置情報タイマの実行間隔時間 (ミリ秒) を返します。
     *
     * @return 位置情報タイマの実行間隔時間 (ミリ秒)
     */
    public long getInterval() { return interval; }

    /**
     * 位置情報タイマの実行間隔時間 (ミリ秒) を設定します。
     *
     * @param interval 位置情報タイマの実行間隔時間 (ミリ秒)
     * @throws IllegalStateException 位置情報タイマが起動している場合
     */
    public void setInterval(final long interval) {
        if (locationTimer != null) {
            throw new IllegalStateException();
        }
        this.interval = interval;
    }

    /**
     * 位置情報取得のデフォルトの最大待ち時間 (ミリ秒) です。
     */
    public static final long DEFAULT_TIMEOUT = 15 * 1000L;

    /**
     * 位置情報取得の最大待ち時間 (ミリ秒) を保持します。
     */
    long timeout = DEFAULT_TIMEOUT;

    /**
     * 位置情報取得の最大待ち時間 (ミリ秒) を返します。
     *
     * @return 位置情報取得の最大待ち時間 (ミリ秒)
     */
    public long getTimeout() { return timeout; }

    /**
     * 位置情報取得の最大待ち時間 (ミリ秒) を設定します。<p>
     * <code>0</code> 以下の値が指定された場合はタイムアウトしません。
     *
     * @param timeout 位置情報取得の最大待ち時間 (ミリ秒)
     */
    public void setTimeout(final long timeout) { this.timeout = timeout; }

    /**
     * 位置情報取得の経過時間 (ミリ秒) を保持します。
     */
    long time;

    /**
     * 現在の位置情報を保持します。
     */
    Location currentLocation;

    /**
     * 現在の位置情報を返します。<p>
     * 現在の位置情報が不明な場合や位置情報の取得を行っていない場合は <code>null</code> を返します。<br>
     * このメソッドは利便性のために提供しています。
     *
     * @return 現在の位置情報。または <code>null</code>
     */
    public Location getCurrentLocation() { return currentLocation; }

    private LocationListener gpsLocationListener;
    private LocationListener networkLocationListener;

    private long minTime = 0L;
    private float minDistance = 5;

    public static final int DEFAULT_UPDATE_LIMIT = 2;

    /** 有効な位置情報の更新可能限界数です。 */
    private int updateLimit = DEFAULT_UPDATE_LIMIT;

    public int getUpdateLimit() {
        return this.updateLimit;
    }
    public void setUpdateLimit(int updateLimit) {
        this.updateLimit = updateLimit;
    }

    /** 有効な位置情報を更新した回数を保持します。 */
    private int updateCount;

    /**
     * 位置情報の取得を開始します。
     */
    public void start() {
//        Log.d("### call BetterLocationManager.start() ### ", "Start");
        stop();
        currentLocation = null;
        updateCount = 0;

        if (locationManager == null) {
            // 位置情報サービスが搭載されていない端末の場合は以下の処理を行ないません。
            return;
        }

        final boolean gps     = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        final boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        // 使用可能な位置情報プロバイダが見つからない場合
        if (!gps && !network) {
            onLocationProviderNotAvailable();
            Log.d("### call BetterLocationManager.start() ### ", "End-1");
            return;
        }

        if (useLastKnownLocation) {
            final Location lastKnownLocation = getLastKnownLocation();
            if (lastKnownLocation != null) {
                // 最後に取得できた位置情報があれば、とりあえず設定します。
                updateLocation(lastKnownLocation, true);

                // 最後に取得できた位置情報が5秒以内のものであれば、これ以上処理を実行しません。
                if ((new Date().getTime() - lastKnownLocation.getTime()) < significantlyNewer) {
                    Log.d("### call BetterLocationManager.start() ### ", "End-2");
                    return;
                }
            }
        }

        if (timeout > 0) {
            time = 0L;
            final Handler handler = new Handler();
            locationTimer = new Timer(true);
            locationTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (time > timeout) {
                                stop();
                                onLocationTimeout();
                                Log.d("### call BetterLocationManager.start() ### ", "End-3");
                                return;
                            } else {
                                onLocationProgress(time);
                            }
                            time = time + interval;
                        }
                    });
                }
            }, delay, interval);
        }

        // 位置情報の取得を開始します。
        if (gps) {
            gpsLocationListener = new LocationListener() {
                @Override
                public void onLocationChanged(final Location location) {
                    if (isBetterLocation(currentLocation, location)) {
                        updateLocation(location, false);
                    }
                }
                @Override public void onProviderDisabled(final String provider) {}
                @Override public void onProviderEnabled(final String provider) {}
                @Override public void onStatusChanged(final String provider, final int status, final Bundle extras) {}
            };
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, gpsLocationListener);
//            Log.d("### call BetterLocationManager.start() ### ", "gps");
        }
        if (network) {
            networkLocationListener = new LocationListener() {
                @Override
                public void onLocationChanged(final Location location) {
                    if (isBetterLocation(currentLocation, location)) {
                        updateLocation(location, false);
                    }
                }
                @Override public void onProviderDisabled(final String provider) {}
                @Override public void onProviderEnabled(final String provider) {}
                @Override public void onStatusChanged(final String provider, final int status, final Bundle extras) {}
            };
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, networkLocationListener);
//            Log.d("### call BetterLocationManager.start() ### ", "network");
        }
        Log.d("### call BetterLocationManager.start() ### ", "End-4");
    }

    /**
     * 位置情報の取得を停止します。<p>
     * 位置情報の取得が開始されていない場合は何も行いません。
     */
    public void stop() {
//        Log.d("### call BetterLocationManager.stop() ### ", "Start");
        if (locationManager != null) {
            if (locationTimer != null) {
                locationTimer.cancel();
                locationTimer.purge();
                locationTimer = null;
            }
            if (networkLocationListener != null) {
                locationManager.removeUpdates(networkLocationListener);
                networkLocationListener = null;
            }
            if (gpsLocationListener != null) {
                locationManager.removeUpdates(gpsLocationListener);
                gpsLocationListener = null;
            }
        }
//        Log.d("### call BetterLocationManager.stop() ### ", "End");
    }

    /**
     * 指定された位置情報を現在地として設定します。<p>
     * 位置情報が有効な位置情報として指定された場合に、有効な位置情報の更新可能限界数に達した場合、位置情報の取得が停止されます。
     *
     * @param location 位置情報
     * @param lastKnownLocation 有効な位置情報かどうか
     */
    void updateLocation(final Location location, final boolean lastKnownLocation) {
//        Log.d("### call BetterLocationManager.updateLocation() ### ", "Start");
        if (!lastKnownLocation && updateLimit > 0) {
            updateCount++;
            if (updateCount >= updateLimit) {
                stop();
            }
        }
        currentLocation = location;
        onUpdateLocation(location, updateCount);
//        Log.d("### call BetterLocationManager.updateLocation() ### ", "End");
    }

    //////////////////////////////////////////////////////////////////////////
    // abstract method

    /**
     * 位置情報取得の開始時に、位置情報プロバイダがいずれも有効でない場合に呼出されます。
     *
     * @see {@link #start()}
     */
    protected abstract void onLocationProviderNotAvailable();

    /**
     * 位置情報の取得中に、実行間隔単位で呼出されます。
     *
     * @param time
     * @see {@link #start()}
     */
    protected abstract void onLocationProgress(long time);

    /**
     * 位置情報の取得でタイムアウトした場合に呼出されます。
     */
    protected abstract void onLocationTimeout();

    protected abstract void onUpdateLocation(Location location, int updateCount);

    //////////////////////////////////////////////////////////////////////////

    /**
     * 指定された新しい位置情報が現在の位置情報より有効かどうかを返します。
     *
     * @param currentLocation 現在の位置情報
     * @param newLocation 新しい位置情報
     * @return 新しい位置情報が有効かどうか
     */
    public boolean isBetterLocation(final Location currentLocation, final Location newLocation) {
//        Log.d("### call BetterLocationManager.isBetterLocation() ### ", "Start");
        if (newLocation == null) {
            // 新しい位置情報が null の場合は常に無効と判断します。
            Log.d("### call BetterLocationManager.isBetterLocation() ### ", "End-1");
            return false;
        }
        if (currentLocation == null) {
            Log.d("### call BetterLocationManager.isBetterLocation() ### ", "End-2");
            return true;
        }

        // Check whether the new location fix is newer or older
        final long timeDelta = newLocation.getTime() - currentLocation.getTime();
        if (timeDelta > significantlyNewer) {
            Log.d("### call BetterLocationManager.isBetterLocation() ### ", "End-3");
            return true;
        } else if (timeDelta < significantlyNewer) {
            Log.d("### call BetterLocationManager.isBetterLocation() ### ", "End-4");
            return false;
        }

        final int accuracyDelta = (int) (newLocation.getAccuracy() - currentLocation.getAccuracy());
        if (accuracyDelta < 0) {
            Log.d("### call BetterLocationManager.isBetterLocation() ### ", "End-5");
            return true;
        } else if (timeDelta > 0 && accuracyDelta <= 0) {
            Log.d("### call BetterLocationManager.isBetterLocation() ### ", "End-6");
            return true;
        } else if (timeDelta > 0 && accuracyDelta <= 200 && isSameProvider(newLocation.getProvider(), currentLocation.getProvider())) {
            Log.d("### call BetterLocationManager.isBetterLocation() ### ", "End-7");
            return true;
        }
        Log.d("### call BetterLocationManager.isBetterLocation() ### ", "End-8");
        return false;
    }

    /** Checks whether two providers are the same */
    public static boolean isSameProvider(final String provider1, final String provider2) {
//        Log.d("### call BetterLocationManager.isSameProvider() ### ", "Start");
        if (provider1 == null) {
            Log.d("### call BetterLocationManager.isSameProvider() ### ", "End-1");
            return provider2 == null;
        }
        Log.d("### call BetterLocationManager.isSameProvider() ### ", "End-2");
        return provider1.equals(provider2);
    }

}