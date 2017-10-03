/*
 * Copyright (c) 2016. Saiy Ltd. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ai.saiy.android.utils.Conditions;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.telephony.TelephonyManager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

/**
 * Class to check the device's network connectivity and speed. This is made of up of code samples
 * collected from many places, but particularly from the author below.
 * <p>
 * Created by benrandall76@gmail.com on 20/03/2016.
 *
 * @author emil http://stackoverflow.com/users/220710/emil
 */
public final class Network {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Network.class.getSimpleName();

    private static final String PING_URL = "http://connectivitycheck.gstatic.com/generate_204";
    private static final String USER_AGENT = "User-Agent";
    private static final String USER_AGENT_ANDROID = "Android";
    private static final String CONNECTION = "Connection";
    private static final String CLOSE = "close";
    public static final int PING_TIMEOUT = 2000;

    private static final int CONNECTION_TYPE_UNKNOWN = 0;
    private static final int CONNECTION_TYPE_2G = 1;
    public static final int CONNECTION_TYPE_3G = 2;
    private static final int CONNECTION_TYPE_4G = 98;
    private static final int CONNECTION_TYPE_WIFI = 99;

    private static final Object lock = new Object();

    /**
     * Prevent instantiation
     */
    public Network() {
        throw new IllegalArgumentException(Resources.getSystem().getString(android.R.string.no));
    }

    public static boolean isNetworkAvailable(final Context ctx) {
        final NetworkInfo activeNetworkInfo = getActiveNetworkInfo(ctx);
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Check if there is any connectivity to a WiFi network
     *
     * @param ctx the application Context
     * @return true if the device is connected to WiFi
     */
    public static boolean isNetworkWiFi(final Context ctx) {
        final NetworkInfo info = getActiveNetworkInfo(ctx);

        return info != null && (info.getType() == ConnectivityManager.TYPE_WIFI
                || info.getType() == ConnectivityManager.TYPE_ETHERNET);

    }

    /**
     * Check if the current connection type and speed is above those required by the current action,
     * as well as any user defaults.
     *
     * @param ctx the application Context
     * @return true if the connection is sufficient
     */
    public static boolean connectivityPass(final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "connectivityPass");
        }

        if (isNetworkAvailable(ctx)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "connectivityPass: isConnected: true");
            }

            final int connectionMinimum = SPH.getConnectionMinimum(ctx);
            if (DEBUG) {
                MyLog.i(CLS_NAME, "connectionMinimum: " + connectionMinimum);
            }

            final int connectionType = getConnectionType(ctx);

            if (DEBUG) {
                switch (connectionType) {

                    case CONNECTION_TYPE_WIFI:
                        MyLog.i(CLS_NAME, "CONNECTION_TYPE_WIFI");
                        break;
                    case CONNECTION_TYPE_4G:
                        MyLog.i(CLS_NAME, "CONNECTION_TYPE_4G");
                        break;
                    case CONNECTION_TYPE_3G:
                        MyLog.i(CLS_NAME, "CONNECTION_TYPE_3G");
                        break;
                    case CONNECTION_TYPE_2G:
                        MyLog.i(CLS_NAME, "CONNECTION_TYPE_2G");
                        break;
                    case CONNECTION_TYPE_UNKNOWN:
                    default:
                        MyLog.i(CLS_NAME, "CONNECTION_TYPE_UNKNOWN");
                        break;
                }
            }

            return connectionType >= connectionMinimum;

        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "connectivityPass: isConnected: false");
            }

            return false;
        }
    }

    /**
     * Get the current connection type from the {@link ConnectivityManager}
     *
     * @param ctx the application Context
     * @return the integer constant of the connection type
     */
    public static int getConnectionType(final Context ctx) {
        final NetworkInfo info = getActiveNetworkInfo(ctx);
        final int infoType = info.getType();

        switch (infoType) {

            case ConnectivityManager.TYPE_WIFI:
            case ConnectivityManager.TYPE_ETHERNET:
            case ConnectivityManager.TYPE_BLUETOOTH:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getConnectionType: CONNECTION_TYPE_WIFI");
                }
                return CONNECTION_TYPE_WIFI;

            case ConnectivityManager.TYPE_WIMAX:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getConnectionType: CONNECTION_TYPE_4G");
                }
                return CONNECTION_TYPE_4G;

            case ConnectivityManager.TYPE_MOBILE:

                final TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
                final int networkType = tm.getNetworkType();

                switch (networkType) {

                    case TelephonyManager.NETWORK_TYPE_GPRS: // ~ 100 kbps
                    case TelephonyManager.NETWORK_TYPE_EDGE: // ~ 50-100 kbps
                    case TelephonyManager.NETWORK_TYPE_CDMA: // ~ 14-64 kbps
                    case TelephonyManager.NETWORK_TYPE_1xRTT: // ~ 50-100 kbps
                    case TelephonyManager.NETWORK_TYPE_IDEN: // ~25 kbps
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "getConnectionType: CONNECTION_TYPE_2G");
                        }
                        return CONNECTION_TYPE_2G;

                    case TelephonyManager.NETWORK_TYPE_UMTS: // ~ 400-7000 kbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_0: // ~ 400-1000 kbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_A: // ~ 600-1400 kbps
                    case TelephonyManager.NETWORK_TYPE_HSDPA: // ~ 2-14 Mbps
                    case TelephonyManager.NETWORK_TYPE_HSUPA: // ~ 1-23 Mbps
                    case TelephonyManager.NETWORK_TYPE_HSPA: // ~ 700-1700 kbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_B: // ~ 5 Mbps
                    case TelephonyManager.NETWORK_TYPE_EHRPD: // ~ 1-2 Mbps
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "getConnectionType: CONNECTION_TYPE_3G");
                        }
                        return CONNECTION_TYPE_3G;

                    case TelephonyManager.NETWORK_TYPE_LTE: // ~ 10+ Mbps
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "getConnectionType: CONNECTION_TYPE_4G");
                        }
                        return CONNECTION_TYPE_4G;
                    case TelephonyManager.NETWORK_TYPE_HSPAP: // ~ 10-20 Mbps
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "getConnectionType: CONNECTION_TYPE_4G");
                        }
                        return CONNECTION_TYPE_4G;

                    default:
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "getConnectionType: CONNECTION_TYPE_UNKNOWN");
                        }
                        return CONNECTION_TYPE_UNKNOWN;

                }

            default:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getConnectionType: CONNECTION_TYPE_UNKNOWN");
                }
                return CONNECTION_TYPE_UNKNOWN;
        }
    }

    /**
     * Is the device 4g capable
     *
     * @param ctx the application Context
     * @return true if the device has the capability
     */
    @SuppressWarnings("deprecation")
    public static boolean isFourGCapable(final Context ctx) {

        final ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

        final NetworkInfo[] info = cm.getAllNetworkInfo();

        for (int i = 0; i < info.length - 1; i++) {

            if (info[i].getType() == ConnectivityManager.TYPE_WIMAX) {
                return true;
            }

        }

        return false;
    }

    /**
     * Get the network info
     *
     * @param ctx the application Context
     * @return the {@link NetworkInfo}
     */
    public static NetworkInfo getActiveNetworkInfo(final Context ctx) {
        final ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    /**
     * Check if there is any connectivity
     *
     * @param ctx the application Context
     * @return true if the device currently has a connection
     */
    public static boolean isConnected(final Context ctx) {
        final NetworkInfo info = getActiveNetworkInfo(ctx);
        return (info != null && info.isConnected());
    }

    /**
     * Check if there is any connectivity to a mobile network
     *
     * @param ctx the application Context
     * @return true if the device is connected to a mobile network
     */
    public static boolean isConnectedMobile(final Context ctx) {
        final NetworkInfo info = getActiveNetworkInfo(ctx);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    /**
     * Check if there is fast connectivity
     *
     * @param ctx the application Context
     * @return true if the connection is 3g or above
     */
    public static boolean isConnectionFast(final Context ctx) {
        final NetworkInfo info = getActiveNetworkInfo(ctx);
        final TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        return (info != null && info.isConnected() && isConnectionFast(info.getType(), tm.getNetworkType()));
    }

    /**
     * Check if the connection is fast
     *
     * @param type    of connection
     * @param subType of connection
     * @return true if the connection is 3g or above
     */
    public static boolean isConnectionFast(final int type, final int subType) {

        switch (type) {

            case ConnectivityManager.TYPE_WIFI:
                return true;

            case ConnectivityManager.TYPE_WIMAX:
                return true;

            case ConnectivityManager.TYPE_MOBILE:

                switch (subType) {

                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                        return false; // ~ 50-100 kbps
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                        return false; // ~ 14-64 kbps
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                        return false; // ~ 50-100 kbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        return true; // ~ 400-1000 kbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        return true; // ~ 600-1400 kbps
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                        return false; // ~ 100 kbps
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                        return true; // ~ 2-14 Mbps
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                        return true; // ~ 700-1700 kbps
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                        return true; // ~ 1-23 Mbps
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                        return true; // ~ 400-7000 kbps
                    case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                        return true; // ~ 1-2 Mbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                        return true; // ~ 5 Mbps
                    case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                        return true; // ~ 10-20 Mbps
                    case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                        return false; // ~25 kbps
                    case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                        return true; // ~ 10+ Mbps
                    // Unknown
                    case TelephonyManager.NETWORK_TYPE_UNKNOWN:

                    default:
                        return false;
                }

            default:
                return false;

        }
    }

    /**
     * Check the conditions are suitable to request a Text to Speech network synthesised voice
     *
     * @param ctx the application context
     * @return true if the network conditions are suitable, false otherwise
     */
    public static boolean shouldTTSNetwork(@NonNull final Context ctx) {
        return SPH.getNetworkSynthesis(ctx) && Network.connectivityPass(ctx)
                && (!SPH.getPingCheck(ctx) || pingSuccessSynchronous(ctx));
    }

    /**
     * Perform a synchronous ping to check the network connection is actually connected. Although
     * this will lock the main thread, it's not called from a UI 'environment' and is short-lived
     * enough not to cause an issue.
     *
     * @param ctx the application context
     * @return true if the connection responded with the established timeout, false otherwise
     */
    private static boolean pingSuccessSynchronous(@NonNull final Context ctx) {

        synchronized (lock) {

            final AtomicBoolean success = new AtomicBoolean(false);

            new Thread() {
                public void run() {

                    HttpURLConnection urlConnection = null;

                    final int timeout = SPH.getPingTimeout(ctx);

                    try {
                        urlConnection = (HttpURLConnection) (new URL(PING_URL).openConnection());
                        urlConnection.setRequestProperty(USER_AGENT, USER_AGENT_ANDROID);
                        urlConnection.addRequestProperty(CONNECTION, CLOSE);
                        urlConnection.setConnectTimeout(timeout);
                        urlConnection.setReadTimeout(timeout);
                        urlConnection.connect();
                        success.set(urlConnection.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT
                                && urlConnection.getContentLength() == 0);
                    } catch (final SocketTimeoutException e) {
                        if (DEBUG) {
                            MyLog.e(CLS_NAME, "pingSuccess SocketTimeoutException");
                        }
                    } catch (final IOException e) {
                        if (DEBUG) {
                            MyLog.e(CLS_NAME, "pingSuccess IOException");
                        }
                    } catch (final Exception e) {
                        if (DEBUG) {
                            MyLog.e(CLS_NAME, "pingSuccess Exception");
                        }
                    } finally {
                        if (urlConnection != null) {
                            try {
                                urlConnection.disconnect();
                            } catch (final Exception e) {
                                if (DEBUG) {
                                    MyLog.e(CLS_NAME, "pingSuccess disconnect Exception");
                                }
                            }
                        }
                        synchronized (lock) {
                            lock.notifyAll();
                        }
                    }
                }
            }.start();

            try {
                lock.wait();
            } catch (final InterruptedException e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "InterruptedException");
                    e.printStackTrace();
                }
            }

            return success.get();
        }
    }


    /**
     * Perform a ping to check the network connection is actually connected.
     *
     * @param ctx the application context
     * @return true if the connection responded with the established timeout, false otherwise
     */
    @WorkerThread
    private static boolean pingSuccess(@NonNull final Context ctx) {

        HttpURLConnection urlConnection = null;

        final int timeout = SPH.getPingTimeout(ctx);

        try {
            urlConnection = (HttpURLConnection) (new URL(PING_URL).openConnection());
            urlConnection.setRequestProperty(USER_AGENT, USER_AGENT_ANDROID);
            urlConnection.addRequestProperty(CONNECTION, CLOSE);
            urlConnection.setConnectTimeout(timeout);
            urlConnection.setReadTimeout(timeout);
            urlConnection.connect();
            return (urlConnection.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT
                    && urlConnection.getContentLength() == 0);
        } catch (final SocketTimeoutException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "pingSuccess SocketTimeoutException");
            }
        } catch (final IOException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "pingSuccess IOException");
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "pingSuccess Exception");
            }
        } finally {

            if (urlConnection != null) {
                try {
                    urlConnection.disconnect();
                } catch (final Exception e) {
                    if (DEBUG) {
                        MyLog.e(CLS_NAME, "pingSuccess disconnect Exception");
                    }
                }
            }
        }

        return false;
    }

    /**
     * Check if the {@link CC} we are about to perform requires a network connection. If it does, either check a network
     * connection is provisionally available or if the user preferences confirm, actually perform a 'ping' to confirm
     * the network is reachable.
     * <p>
     * // TODO - I have a bad feeling about this. Preventing any network commands processing on the basis of this
     * // TODO - network response is a major concern. But then again, if the Google Network goes down, won't the
     * // TODO - world be post-apocalyptic anyway?
     *
     * @param ctx the application context
     * @param cc  the {@link CC} that is about to be performed
     * @return true if the network parameters pass, false otherwise.
     */
    public static boolean networkProceed(@NonNull final Context ctx, @NonNull final CC cc) {

        if (CC.requiresNetwork(cc)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "command requires network: true");
            }

            if (SPH.getPingCheck(ctx)) {
                if (isNetworkAvailable(ctx)) {
                    if (Network.pingSuccess(ctx)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "pingSuccess: true");
                        }
                        return true;
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "pingSuccess: false");
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "pingSuccess: isConnected: false");
                    }
                }
            } else {

                if (Network.isNetworkAvailable(ctx)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "isNetworkAvailable: true");
                    }
                    return true;
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "isNetworkAvailable: false");
                    }
                }
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "command requires network: false");
            }
            return true;
        }

        return false;
    }
}
