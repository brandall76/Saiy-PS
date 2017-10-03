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

package ai.saiy.android.recognition;

import android.support.annotation.NonNull;

import java.util.regex.Pattern;

import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;

/**
 * Created by benrandall76@gmail.com on 04/09/2016.
 */

public class SaiyHotwordListener implements RecognitionListener {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = SaiyHotwordListener.class.getSimpleName();

    protected static final String OKAY_GOOGLE = "okaygoogle";
    protected static final String WAKEUP_SAIY = "wakeupsay";
    protected static final String STOP_LISTENING = "stoplistening";

    private static final Pattern pOKAY_GOOGLE = Pattern.compile(OKAY_GOOGLE);
    private static final Pattern pWAKEUP_SAIY = Pattern.compile(WAKEUP_SAIY);
    private static final Pattern pSTOP_LISTENING = Pattern.compile(STOP_LISTENING);

    public static final int ERROR_NULL = 1;
    public static final int ERROR_INITIALISE = 2;
    public static final int ERROR_PERMISSIONS = 3;

    private boolean hotwordDetected;

    public void onHotwordInitialised() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onHotwordInitialised");
        }
        hotwordDetected = false;
    }

    public void onHotwordStarted() {
    }

    public void onHotwordDetected(@NonNull final String hotword) {
    }

    public void onHotwordError(final int errorCode) {
    }

    public void onHotwordShutdown() {
    }

    @Override
    public void onBeginningOfSpeech() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onBeginningOfSpeech");
        }
    }

    @Override
    public void onEndOfSpeech() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onEndOfSpeech");
        }
        System.gc();
    }

    @Override
    public void onPartialResult(final Hypothesis hypothesis) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onPartialResult: hotwordDetected: " + hotwordDetected);
        }

        if (hypothesis != null) {

            if (!hotwordDetected) {

                final String detected = hypothesis.getHypstr();

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onPartialResult: detected: " + detected);
                }

                if (UtilsString.notNaked(detected)) {

                    hotwordDetected = true;

                    if (pOKAY_GOOGLE.matcher(detected.trim()).matches()) {
                        onHotwordDetected(OKAY_GOOGLE);
                    } else if (pWAKEUP_SAIY.matcher(detected.trim()).matches()) {
                        onHotwordDetected(WAKEUP_SAIY);
                    } else if (pSTOP_LISTENING.matcher(detected.trim()).matches()) {
                        onHotwordDetected(STOP_LISTENING);
                    } else {
                        hotwordDetected = false;
                    }
                }
            }
        }
    }

    @Override
    public void onResult(final Hypothesis hypothesis) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onResult");
        }
    }

    @Override
    public void onError(final Exception e) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onError");
        }
    }

    @Override
    public void onTimeout() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onTimeout");
        }
    }
}
