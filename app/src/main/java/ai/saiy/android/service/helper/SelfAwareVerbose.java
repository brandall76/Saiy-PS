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

package ai.saiy.android.service.helper;

import android.app.Service;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import ai.saiy.android.recognition.provider.android.RecognitionNative;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

/**
 * Helper class purely for logging verbose information. This will not be used in production, unless
 * logging is enabled when attempting to resolve a specific issue with a user.
 * <p/>
 * Created by benrandall76@gmail.com on 03/04/2016.
 */
public class SelfAwareVerbose {

    private static final String CLS_NAME = SelfAwareVerbose.class.getSimpleName();

    /**
     * Iterate through the recognition results and their associated confidence scores.
     *
     * @param bundle of recognition data
     */
    public static void logSpeechResults(final Bundle bundle) {
        MyLog.i(CLS_NAME, "logSpeechResults");

        examineBundle(bundle);

        final ArrayList<String> heardVoice = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        final ArrayList<String> unstable = bundle.getStringArrayList(RecognitionNative.UNSTABLE_RESULTS);
        final float[] confidence = bundle.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);

        if (heardVoice != null) {
            MyLog.d(CLS_NAME, "heardVoice: " + heardVoice.size());
        }
        if (unstable != null) {
            MyLog.d(CLS_NAME, "unstable: " + unstable.size());
        }
        if (confidence != null) {
            MyLog.d(CLS_NAME, "confidence: " + confidence.length);
        }

        /* handles empty string bug */
        if (UtilsList.notNaked(heardVoice)) {
            heardVoice.removeAll(Collections.singleton(""));
        }

        /* handles empty string bug */
        if (UtilsList.notNaked(unstable)) {
            unstable.removeAll(Collections.singleton(""));
        }

        if (UtilsList.notNaked(confidence) && UtilsList.notNaked(heardVoice) && confidence.length == heardVoice.size()) {
            for (int i = 0; i < heardVoice.size(); i++) {
                MyLog.i(CLS_NAME, "Results: " + heardVoice.get(i) + " ~ " + confidence[i]);
            }
        } else if (UtilsList.notNaked(heardVoice)) {
            for (int i = 0; i < heardVoice.size(); i++) {
                MyLog.i(CLS_NAME, "Results: " + heardVoice.get(i));
            }
        } else if (UtilsList.notNaked(unstable)) {
            for (int i = 0; i < unstable.size(); i++) {
                MyLog.i(CLS_NAME, "Unstable: " + unstable.get(i));
            }
        } else {
            MyLog.w(CLS_NAME, "Results: values error");
        }
    }

    /**
     * Check the parameter of any coming {@link Service#onTrimMemory(int)} call
     *
     * @param level the 'Trim' level
     */
    public static void memoryVerbose(final int level) {
        switch (level) {
            case Service.TRIM_MEMORY_BACKGROUND:
                MyLog.w(CLS_NAME, "memoryVerbose: TRIM_MEMORY_BACKGROUND");
                break;
            case Service.TRIM_MEMORY_COMPLETE:
                MyLog.w(CLS_NAME, "memoryVerbose: TRIM_MEMORY_COMPLETE");
                break;
            case Service.TRIM_MEMORY_MODERATE:
                MyLog.w(CLS_NAME, "memoryVerbose: TRIM_MEMORY_MODERATE");
                break;
            case Service.TRIM_MEMORY_RUNNING_CRITICAL:
                MyLog.w(CLS_NAME, "memoryVerbose: TRIM_MEMORY_RUNNING_CRITICAL");
                break;
            case Service.TRIM_MEMORY_RUNNING_LOW:
                MyLog.w(CLS_NAME, "memoryVerbose: TRIM_MEMORY_BACKGROUND");
                break;
            case Service.TRIM_MEMORY_RUNNING_MODERATE:
                MyLog.w(CLS_NAME, "memoryVerbose: TRIM_MEMORY_RUNNING_MODERATE");
                break;
            case Service.TRIM_MEMORY_UI_HIDDEN:
                MyLog.w(CLS_NAME, "memoryVerbose: TRIM_MEMORY_UI_HIDDEN");
                break;
            default:
                MyLog.w(CLS_NAME, "memoryVerbose: default");
                break;
        }
    }

    /**
     * For debugging the intent extras
     *
     * @param bundle containing potential extras
     */
    private static void examineBundle(@Nullable final Bundle bundle) {
        MyLog.i(CLS_NAME, "examineBundle");

        if (bundle != null) {
            final Set<String> keys = bundle.keySet();
            //noinspection Convert2streamapi
            for (final String key : keys) {
                MyLog.v(CLS_NAME, "examineBundle: " + key + " ~ " + bundle.get(key));
            }
        }
    }

}
