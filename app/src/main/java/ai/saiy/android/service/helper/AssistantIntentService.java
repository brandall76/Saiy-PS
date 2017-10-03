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

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;

import java.util.Set;

import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityHelper;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsLocale;
import ai.saiy.android.utils.UtilsString;

/**
 * Class to handle the various available voice interaction intents:
 * <p>
 * {@link RecognizerIntent#ACTION_VOICE_SEARCH_HANDS_FREE}
 * {@link Intent#ACTION_VOICE_COMMAND}
 * {@link Intent#ACTION_SEARCH_LONG_PRESS}
 * {@link Intent#ACTION_VOICE_COMMAND}
 * {@link Intent#ACTION_ASSIST}
 * <p>
 * Created by benrandall76@gmail.com on 15/08/2016.
 */

public class AssistantIntentService extends IntentService {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = AssistantIntentService.class.getSimpleName();

    private final String EXTRA_ASSIST_CONTEXT = "android.intent.extra.ASSIST_CONTEXT";

    private long then;

    public AssistantIntentService() {
        super(AssistantIntentService.class.getSimpleName());
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
            examineIntent(intent);
        }

        final Bundle actionBundle = new Bundle();
        actionBundle.putInt(LocalRequest.EXTRA_ACTION, LocalRequest.ACTION_SPEAK_LISTEN);

        if (intent != null) {
            final Bundle bundle = intent.getExtras();
            if (bundle != null) {

                final String action = intent.getAction();

                if (UtilsString.notNaked(action)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onHandleIntent: action: " + action);
                    }

                    if (intent.getAction().equals(Intent.ACTION_ASSIST)) {
                        if (DEBUG) {
                            if (bundle.containsKey(EXTRA_ASSIST_CONTEXT)) {
                                final Bundle assistBundle = bundle.getBundle(EXTRA_ASSIST_CONTEXT);

                                MyLog.i(CLS_NAME, "onHandleIntent checking assistBundle");
                                examineBundle(assistBundle);
                            }
                        }
                    }
                }

                if (bundle.containsKey(LocalRequest.EXTRA_RECOGNITION_LANGUAGE)) {

                    final String vrLocale = bundle.getString(LocalRequest.EXTRA_RECOGNITION_LANGUAGE);

                    if (UtilsString.notNaked(vrLocale)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onHandleIntent: EXTRA_RECOGNITION_LANGUAGE: " + vrLocale);
                        }
                        actionBundle.putString(LocalRequest.EXTRA_RECOGNITION_LANGUAGE, vrLocale);
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onHandleIntent: EXTRA_RECOGNITION_LANGUAGE naked");
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onHandleIntent: no EXTRA_RECOGNITION_LANGUAGE");
                    }
                }

                if (bundle.containsKey(LocalRequest.EXTRA_TTS_LANGUAGE)) {

                    final String ttsLocale = bundle.getString(LocalRequest.EXTRA_TTS_LANGUAGE);

                    if (UtilsString.notNaked(ttsLocale)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onHandleIntent: EXTRA_TTS_LANGUAGE: " + ttsLocale);
                        }
                        actionBundle.putString(LocalRequest.EXTRA_TTS_LANGUAGE, ttsLocale);
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onHandleIntent: EXTRA_TTS_LANGUAGE naked");
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onHandleIntent: no EXTRA_TTS_LANGUAGE");
                    }
                }

                if (bundle.containsKey(LocalRequest.EXTRA_SUPPORTED_LANGUAGE)) {

                    final SupportedLanguage supportedLanguage = (SupportedLanguage) bundle.getSerializable(
                            LocalRequest.EXTRA_SUPPORTED_LANGUAGE);

                    if (supportedLanguage != null) {
                        actionBundle.putSerializable(LocalRequest.EXTRA_SUPPORTED_LANGUAGE, supportedLanguage);
                        actionBundle.putString(LocalRequest.EXTRA_UTTERANCE,
                                PersonalityHelper.getIntro(getApplicationContext(), supportedLanguage));
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onHandleIntent: EXTRA_SUPPORTED_LANGUAGE null");
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onHandleIntent: no EXTRA_SUPPORTED_LANGUAGE");
                    }
                }

                if (bundle.containsKey(RecognizerIntent.EXTRA_SECURE)) {

                    final Object secureObject = bundle.get(RecognizerIntent.EXTRA_SECURE);

                    if (secureObject != null) {

                        boolean secure = false;

                        if (secureObject instanceof Boolean) {
                            secure = (boolean) secureObject;
                        } else if (secureObject instanceof String) {
                            secure = Boolean.parseBoolean((String) secureObject);
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "onHandleIntent: EXTRA_SECURE of unknown type ignoring");
                            }
                        }

                        if (secure) {
                            actionBundle.putInt(LocalRequest.EXTRA_CONDITION, Condition.CONDITION_SECURE);
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "onHandleIntent: EXTRA_SECURE false ignoring");
                            }
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onHandleIntent: secureObject null");
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onHandleIntent: no EXTRA_SECURE");
                    }
                }
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onHandleIntent: bundle null ignoring");
                }
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onHandleIntent: intent null ignoring");
            }
        }

        if (!actionBundle.containsKey(LocalRequest.EXTRA_RECOGNITION_LANGUAGE)) {
            actionBundle.putString(LocalRequest.EXTRA_RECOGNITION_LANGUAGE, SPH.getVRLocale(getApplicationContext()).toString());
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onHandleIntent: actionBundle: auto adding EXTRA_RECOGNITION_LANGUAGE");
            }
        }

        if (!actionBundle.containsKey(LocalRequest.EXTRA_TTS_LANGUAGE)) {
            actionBundle.putString(LocalRequest.EXTRA_TTS_LANGUAGE, SPH.getTTSLocale(getApplicationContext()).toString());
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onHandleIntent: actionBundle: auto adding EXTRA_TTS_LANGUAGE");
            }
        }

        if (!actionBundle.containsKey(LocalRequest.EXTRA_SUPPORTED_LANGUAGE)) {
            @SuppressWarnings("ConstantConditions") final SupportedLanguage sl = SupportedLanguage.getSupportedLanguage(
                    UtilsLocale.stringToLocale(actionBundle.getString(LocalRequest.EXTRA_RECOGNITION_LANGUAGE)));
            actionBundle.putSerializable(LocalRequest.EXTRA_SUPPORTED_LANGUAGE, sl);
            actionBundle.putString(LocalRequest.EXTRA_UTTERANCE, PersonalityHelper.getIntro(getApplicationContext(), sl));
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onHandleIntent: actionBundle: auto adding EXTRA_SUPPORTED_LANGUAGE");
            }
        }

        new LocalRequest(getApplicationContext(), actionBundle).execute();
    }

    /**
     * For debugging the intent extras
     *
     * @param intent containing potential extras
     */
    private void examineIntent(@Nullable final Intent intent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "examineIntent");
        }

        if (intent != null) {
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
    }

    /**
     * For debugging the intent extras
     *
     * @param bundle containing potential extras
     */
    private void examineBundle(@Nullable final Bundle bundle) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "examineBundle");
        }

        if (bundle != null) {
            final Set<String> keys = bundle.keySet();
            for (final String key : keys) {
                if (DEBUG) {
                    MyLog.v(CLS_NAME, "examineBundle: " + key + " ~ " + bundle.get(key));
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
