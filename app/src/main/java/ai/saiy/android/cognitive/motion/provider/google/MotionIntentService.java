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

package ai.saiy.android.cognitive.motion.provider.google;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.Set;

import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

/**
 * Class to handle callbacks from the ActivityRecognition API.
 * <p>
 * If the confidence scores are high, the activity is stored, so we can understand the context of what the
 * user is doing elsewhere in the application.
 * <p>
 * Created by benrandall76@gmail.com on 05/07/2016.
 */

public class MotionIntentService extends IntentService {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = MotionIntentService.class.getSimpleName();

    protected static final long UPDATE_INTERVAL = 600000L;
    protected static final int REQUEST_CODE = 55;

    private long then;

    /**
     * Constructor
     */
    public MotionIntentService() {
        super(MotionIntentService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }

        then = System.nanoTime();
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onHandleIntent");
        }

        if (SPH.getMotionEnabled(getApplicationContext())) {

            if (intent != null) {
                if (DEBUG) {
                    examineIntent(intent);
                }

                if (ActivityRecognitionResult.hasResult(intent)) {
                    final Motion motion = extractMotion(intent);
                    if (motion != null) {
                        MotionHelper.setMotion(getApplicationContext(), motion);
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onHandleIntent: motion null: ignoring");
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onHandleIntent: no ActivityRecognition results");
                    }
                }
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "onHandleIntent: intent: null");
                }
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onHandleIntent: user has switched off. Don't store.");
            }
        }
    }

    /**
     * Store the most recent user activity for use elsewhere in the application.
     *
     * @param intent which should contain an {@link ActivityRecognitionResult}
     * @return a {@link Motion} object
     */
    private Motion extractMotion(final Intent intent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "extractMotion");
        }

        final ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

        if (result != null) {

            final DetectedActivity detectedActivity = result.getMostProbableActivity();

            if (detectedActivity != null) {
                if (DEBUG) {
                    logActivity(detectedActivity);
                }

                final int confidence = detectedActivity.getConfidence();

                if (confidence > Motion.CONFIDENCE_THRESHOLD) {

                    switch (detectedActivity.getType()) {

                        case DetectedActivity.WALKING:
                        case DetectedActivity.IN_VEHICLE:
                        case DetectedActivity.ON_BICYCLE:
                        case DetectedActivity.ON_FOOT:
                        case DetectedActivity.RUNNING:
                        case DetectedActivity.STILL:
                            return new Motion(detectedActivity.getType(), confidence, result.getTime());
                        case DetectedActivity.TILTING:
                        case DetectedActivity.UNKNOWN:
                        default:
                            break;
                    }

                } else {
                    if (DEBUG) {
                        MyLog.v(CLS_NAME, "detectedActivity: ignoring low confidence");
                    }
                }
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "detectedActivity: null");
                }
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "detectedActivity: ActivityRecognitionResult: null");
            }
        }

        return null;
    }

    /**
     * Logging only
     *
     * @param detectedActivity the {@link DetectedActivity}
     */
    private void logActivity(final DetectedActivity detectedActivity) {
        MyLog.v(CLS_NAME, "detectedActivity: confidence: " + detectedActivity.getConfidence());

        switch (detectedActivity.getType()) {

            case DetectedActivity.WALKING:
                MyLog.i(CLS_NAME, "detectedActivity: DetectedActivity.WALKING");
                break;
            case DetectedActivity.IN_VEHICLE:
                MyLog.i(CLS_NAME, "detectedActivity: DetectedActivity.IN_VEHICLE");
                break;
            case DetectedActivity.ON_BICYCLE:
                MyLog.i(CLS_NAME, "detectedActivity: DetectedActivity.ON_BICYCLE");
                break;
            case DetectedActivity.ON_FOOT:
                MyLog.i(CLS_NAME, "detectedActivity: DetectedActivity.ON_FOOT");
                break;
            case DetectedActivity.RUNNING:
                MyLog.i(CLS_NAME, "detectedActivity: DetectedActivity.RUNNING");
                break;
            case DetectedActivity.STILL:
                MyLog.i(CLS_NAME, "detectedActivity: DetectedActivity.STILL");
                break;
            case DetectedActivity.TILTING:
                MyLog.i(CLS_NAME, "detectedActivity: DetectedActivity.TILTING");
                break;
            case DetectedActivity.UNKNOWN:
                MyLog.i(CLS_NAME, "detectedActivity: DetectedActivity.UNKNOWN");
                break;
            default:
                MyLog.i(CLS_NAME, "detectedActivity: DetectedActivity.default");
                break;
        }
    }

    /**
     * For debugging the intent extras
     *
     * @param intent containing potential extras
     */
    private void examineIntent(@NonNull final Intent intent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "examineIntent");
        }

        final Bundle bundle = intent.getExtras();
        if (bundle != null) {
            final Set<String> keys = bundle.keySet();
            for (final String key : keys) {
                if (DEBUG) {
                    MyLog.v(CLS_NAME, "examineIntent: " + key + " ~ " + bundle.get(key));
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDestroy");
            MyLog.getElapsed(CLS_NAME, then);
        }
    }
}
