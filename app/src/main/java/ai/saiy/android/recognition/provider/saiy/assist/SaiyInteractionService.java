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

package ai.saiy.android.recognition.provider.saiy.assist;

/**
 * Created by benrandall76@gmail.com on 22/08/2016.
 */

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.service.voice.AlwaysOnHotwordDetector;
import android.service.voice.AlwaysOnHotwordDetector.Callback;
import android.service.voice.AlwaysOnHotwordDetector.EventPayload;
import android.service.voice.VoiceInteractionService;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import java.util.List;
import java.util.Locale;

import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsLocale;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SaiyInteractionService extends VoiceInteractionService {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = SaiyInteractionService.class.getSimpleName();

    private static final String ACTION_MANAGE_VOICE_KEYPHRASES = "com.android.intent.action.MANAGE_VOICE_KEYPHRASES";
    public static final String EXTRA_VOICE_KEYPHRASE_HINT_TEXT = "com.android.intent.extra.VOICE_KEYPHRASE_HINT_TEXT";
    public static final String EXTRA_VOICE_KEYPHRASE_LOCALE = "com.android.intent.extra.VOICE_KEYPHRASE_LOCALE";
    private static final long MINI_SLEEP = 1500L;

    private AlwaysOnHotwordDetector mHotwordDetector;
    private String hotword;
    private Locale locale;

    @Override
    public void onCreate() {
        super.onCreate();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }

        final PackageManager pm = getPackageManager();
        final List<ResolveInfo> resolveInfoList = pm.queryIntentActivities(
                new Intent(ACTION_MANAGE_VOICE_KEYPHRASES), PackageManager.MATCH_DEFAULT_ONLY);

        if (!UtilsList.notNaked(resolveInfoList)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onCreate: no enrollment services available on the device");
            }

            this.stopSelf();
        }
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onStartCommand: " + startId);
        }

        final Bundle bundle = intent.getExtras();
        if (bundle != null && !bundle.isEmpty()) {

            hotword = bundle.getString(EXTRA_VOICE_KEYPHRASE_HINT_TEXT);
            locale = UtilsLocale.stringToLocale(bundle.getString(EXTRA_VOICE_KEYPHRASE_LOCALE,
                    SupportedLanguage.ENGLISH.getLanguageCountry()));

            if (DEBUG) {
                MyLog.i(CLS_NAME, "hotword: " + hotword);
                MyLog.i(CLS_NAME, "locale: " + locale.toString());
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onReady() {
        super.onReady();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onReady");
        }

        if (hotword != null && locale != null) {
            mHotwordDetector = createAlwaysOnHotwordDetector(hotword, locale, hotwordCallback);
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onReady: trying again");
                    }
                    if (hotword != null && locale != null) {
                        mHotwordDetector = SaiyInteractionService.this.createAlwaysOnHotwordDetector(hotword, locale, hotwordCallback);
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "onReady: hotword info permanently missing");
                        }
                    }
                }
            }, MINI_SLEEP);
        }
    }

    /**
     * Callbacks for hotword registration and availability
     */
    private final Callback hotwordCallback = new Callback() {

        @Override
        public void onAvailabilityChanged(int status) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onAvailabilityChanged");
            }

            switch (status) {
                case AlwaysOnHotwordDetector.STATE_HARDWARE_UNAVAILABLE:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "STATE_HARDWARE_UNAVAILABLE");
                    }
                    break;
                case AlwaysOnHotwordDetector.STATE_KEYPHRASE_UNSUPPORTED:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "STATE_KEYPHRASE_UNSUPPORTED");
                    }
                    break;
                case AlwaysOnHotwordDetector.STATE_KEYPHRASE_UNENROLLED:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "STATE_KEYPHRASE_UNENROLLED");
                    }

                    Intent enroll = mHotwordDetector.createEnrollIntent();
                    MyLog.i(CLS_NAME, "Need to enroll with " + enroll);

                    break;
                case AlwaysOnHotwordDetector.STATE_KEYPHRASE_ENROLLED:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "STATE_KEYPHRASE_ENROLLED - starting recognition");
                    }

                    try {

                        if (mHotwordDetector.startRecognition(
                                AlwaysOnHotwordDetector.RECOGNITION_FLAG_CAPTURE_TRIGGER_AUDIO)) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "startRecognition succeeded");
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "startRecognition failed");
                            }
                        }

                    } catch (final IllegalStateException e) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "startRecognition IllegalStateException");
                            e.printStackTrace();
                        }
                    } catch (final UnsupportedOperationException e) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "startRecognition UnsupportedOperationException");
                            e.printStackTrace();
                        }
                    }

                    break;
            }
        }

        @Override
        public void onDetected(@NonNull final EventPayload eventPayload) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onDetected");
            }
        }

        @Override
        public void onError() {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onError");
            }
        }

        @Override
        public void onRecognitionPaused() {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onRecognitionPaused");
            }
        }

        @Override
        public void onRecognitionResumed() {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onRecognitionResumed");
            }
        }
    };
}
