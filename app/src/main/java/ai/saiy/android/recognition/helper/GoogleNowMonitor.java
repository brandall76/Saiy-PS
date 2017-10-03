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

package ai.saiy.android.recognition.helper;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Process;
import android.support.annotation.NonNull;

import java.util.regex.Pattern;

import ai.saiy.android.applications.UtilsApplication;
import ai.saiy.android.intent.IntentConstants;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

/**
 * Created by benrandall76@gmail.com on 05/09/2016.
 */

public class GoogleNowMonitor {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = GoogleNowMonitor.class.getSimpleName();

    private static final Pattern pPACKAGE_NAME_GOOGLE_NOW = Pattern.compile(IntentConstants.PACKAGE_NAME_GOOGLE_NOW);

    private static final long MAX_DURATION = 180000L;
    private static final long HISTORY = 10000L;
    private final long then = System.currentTimeMillis();

    /**
     * Start monitoring the foreground application to see if/when the user leaves Google Now. Once they do,
     * the hotword detection can restart. A time limit to keep this process running is defined by
     * {@link #MAX_DURATION}
     *
     * @param ctx the application context
     */
    public void start(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "start");
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_LESS_FAVORABLE);

                boolean timeout = true;

                while ((System.currentTimeMillis() - MAX_DURATION) < then) {

                    try {
                        Thread.sleep(5000);
                    } catch (final InterruptedException e) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "InterruptedException");
                            e.printStackTrace();
                        }
                    }

                    final String foregroundPackage = UtilsApplication.getForegroundPackage(ctx, HISTORY);

                    if (UtilsString.notNaked(foregroundPackage)) {

                        if (pPACKAGE_NAME_GOOGLE_NOW.matcher(foregroundPackage).matches()) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "foreground remains google");
                            }
                        } else {
                            GoogleNowMonitor.this.restartHotword(ctx);
                            timeout = false;
                            break;
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "foreground package null");
                        }
                    }
                }

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "shutting down: timeout: " + timeout);
                }

                if (timeout) {
                    GoogleNowMonitor.this.shutdownHotword(ctx);
                }
            }
        }).start();
    }

    /**
     * Send a request to restart the hotword detection
     *
     * @param ctx the application context
     */
    private void restartHotword(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "restartHotword");
        }

        final LocalRequest request = new LocalRequest(ctx);
        request.prepareDefault(LocalRequest.ACTION_START_HOTWORD, null);
        request.execute();
    }

    /**
     * Send a request to prevent the hotword detection from restarting
     *
     * @param ctx the application context
     */
    private void shutdownHotword(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "shutdownHotword");
        }

        final LocalRequest request = new LocalRequest(ctx);
        request.prepareDefault(LocalRequest.ACTION_STOP_HOTWORD, null);
        request.setShutdownHotword();
        request.execute();
    }
}
