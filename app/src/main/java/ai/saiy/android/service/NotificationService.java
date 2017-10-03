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

package ai.saiy.android.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Set;

import ai.saiy.android.R;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.AnalysisResultHelper;
import ai.saiy.android.cognitive.identity.provider.microsoft.Speaker;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.intent.ExecuteIntent;
import ai.saiy.android.localisation.SaiyResourcesHelper;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.permissions.PermissionHelper;
import ai.saiy.android.personality.PersonalityHelper;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsString;

/**
 * Class to handle notification clicks, either from the foreground {@link SelfAware} service or other
 * notifications that are shown.
 * <p>
 * Created by benrandall76@gmail.com on 06/02/2016.
 */
public class NotificationService extends IntentService {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = NotificationService.class.getSimpleName();

    private long then;

    public static final String CLICK_ACTION = "click_action";
    public static final String INTENT_CLICK = "ai.saiy.android.INTENT_CLICK";

    public static final int NOTIFICATION_FOREGROUND = 77;
    public static final int NOTIFICATION_LISTENING = 21;
    public static final int NOTIFICATION_SPEAKING = 22;
    public static final int NOTIFICATION_FETCHING = 23;
    public static final int NOTIFICATION_COMPUTING = 24;
    public static final int NOTIFICATION_PERMISSIONS = 25;
    public static final int NOTIFICATION_EMOTION = 26;
    public static final int NOTIFICATION_HOTWORD = 27;
    public static final int NOTIFICATION_INITIALISING = 28;
    public static final int NOTIFICATION_IDENTIFICATION = 29;

    /**
     * Constructor
     */
    public NotificationService() {
        super(NotificationService.class.getSimpleName());
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

        if (intent != null) {

            final String action = intent.getAction();
            final Bundle bundle = intent.getExtras();

            if (bundle != null) {

                if (DEBUG) {
                    examineIntent(intent);
                }

                if (UtilsString.notNaked(action)) {

                    if (intent.getAction().equals(INTENT_CLICK)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onHandleIntent: INTENT_CLICK");
                        }

                        final SupportedLanguage sl = SupportedLanguage.getSupportedLanguage(
                                SPH.getVRLocale(getApplicationContext()));
                        bundle.putSerializable(LocalRequest.EXTRA_SUPPORTED_LANGUAGE, sl);

                        switch (bundle.getInt(CLICK_ACTION, 0)) {

                            case NOTIFICATION_FOREGROUND:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "onHandleIntent: NOTIFICATION_FOREGROUND");
                                }

                                bundle.putInt(LocalRequest.EXTRA_ACTION, LocalRequest.ACTION_SPEAK_LISTEN);
                                bundle.putString(LocalRequest.EXTRA_UTTERANCE, PersonalityHelper.getIntro(getApplicationContext(), sl));
                                bundle.putString(LocalRequest.EXTRA_RECOGNITION_LANGUAGE, SPH.getVRLocale(getApplicationContext()).toString());
                                bundle.putString(LocalRequest.EXTRA_TTS_LANGUAGE, SPH.getTTSLocale(getApplicationContext()).toString());
                                new LocalRequest(getApplicationContext(), bundle).execute();

                                break;
                            case NOTIFICATION_LISTENING:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "onHandleIntent: NOTIFICATION_LISTENING");
                                }
                                new LocalRequest(getApplicationContext(), bundle).execute();
                                break;
                            case NOTIFICATION_SPEAKING:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "onHandleIntent: NOTIFICATION_SPEAKING");
                                }
                                new LocalRequest(getApplicationContext(), bundle).execute();
                                break;
                            case NOTIFICATION_FETCHING:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "onHandleIntent: NOTIFICATION_FETCHING");
                                }
                                new LocalRequest(getApplicationContext(), bundle).execute();
                                break;
                            case NOTIFICATION_INITIALISING:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "onHandleIntent: NOTIFICATION_INITIALISING");
                                }
                                new LocalRequest(getApplicationContext(), bundle).execute();
                                break;
                            case NOTIFICATION_COMPUTING:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "onHandleIntent: NOTIFICATION_COMPUTING");
                                }
                                new LocalRequest(getApplicationContext(), bundle).execute();
                                break;
                            case NOTIFICATION_PERMISSIONS:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "onHandleIntent: NOTIFICATION_PERMISSIONS");
                                }

                                ExecuteIntent.openApplicationSpecificSettings(getApplicationContext(), getPackageName());

                                bundle.putInt(LocalRequest.EXTRA_ACTION, LocalRequest.ACTION_SPEAK_ONLY);
                                bundle.putString(LocalRequest.EXTRA_RECOGNITION_LANGUAGE,
                                        SPH.getVRLocale(getApplicationContext()).toString());
                                bundle.putString(LocalRequest.EXTRA_TTS_LANGUAGE,
                                        SPH.getTTSLocale(getApplicationContext()).toString());

                                String permissionContent;

                                switch (bundle.getInt(PermissionHelper.REQUESTED_PERMISSION, 0)) {

                                    case PermissionHelper.REQUEST_AUDIO:
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "onHandleIntent: REQUEST_AUDIO");
                                        }
                                        permissionContent = getString(ai.saiy.android.R.string.permission_audio);
                                        break;
                                    case PermissionHelper.REQUEST_GROUP_CONTACTS:
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "onHandleIntent: REQUEST_GROUP_CONTACTS");
                                        }
                                        permissionContent = getString(ai.saiy.android.R.string.permission_group_contacts);
                                        break;
                                    case PermissionHelper.REQUEST_FILE:
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "onHandleIntent: REQUEST_FILE");
                                        }
                                        // TODO
                                        permissionContent = getString(ai.saiy.android.R.string.permission_unknown);
                                        break;
                                    default:
                                        if (DEBUG) {
                                            MyLog.w(CLS_NAME, "onHandleIntent: PermissionHelper.UNKNOWN");
                                        }
                                        permissionContent = getString(ai.saiy.android.R.string.permission_unknown);
                                        break;
                                }

                                bundle.putString(LocalRequest.EXTRA_UTTERANCE, permissionContent);

                                new LocalRequest(getApplicationContext(), bundle).execute();
                                break;
                            case NOTIFICATION_EMOTION:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "onHandleIntent: NOTIFICATION_EMOTION");
                                }

                                bundle.putInt(LocalRequest.EXTRA_ACTION, LocalRequest.ACTION_SPEAK_ONLY);

                                final String emotionAnalysis = AnalysisResultHelper.getEmotionDescription(
                                        getApplicationContext(), sl);

                                bundle.putString(LocalRequest.EXTRA_UTTERANCE, emotionAnalysis);
                                bundle.putString(LocalRequest.EXTRA_RECOGNITION_LANGUAGE,
                                        SPH.getVRLocale(getApplicationContext()).toString());
                                bundle.putString(LocalRequest.EXTRA_TTS_LANGUAGE,
                                        SPH.getTTSLocale(getApplicationContext()).toString());
                                bundle.putSerializable(LocalRequest.EXTRA_COMMAND, CC.COMMAND_EMOTION);

                                new LocalRequest(getApplicationContext(), bundle).execute();
                                break;
                            case NOTIFICATION_HOTWORD:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "onHandleIntent: NOTIFICATION_HOTWORD");
                                }

                                bundle.putInt(LocalRequest.EXTRA_ACTION, LocalRequest.ACTION_STOP_HOTWORD);
                                final LocalRequest request = new LocalRequest(getApplicationContext(), bundle);
                                request.setShutdownHotword();
                                request.execute();

                                final Intent closeShadeIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                                sendBroadcast(closeShadeIntent);

                                break;
                            case NOTIFICATION_IDENTIFICATION:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "onHandleIntent: NOTIFICATION_IDENTIFICATION");
                                }

                                bundle.putInt(LocalRequest.EXTRA_ACTION, LocalRequest.ACTION_SPEAK_ONLY);
                                bundle.putString(LocalRequest.EXTRA_RECOGNITION_LANGUAGE,
                                        SPH.getVRLocale(getApplicationContext()).toString());
                                bundle.putString(LocalRequest.EXTRA_TTS_LANGUAGE,
                                        SPH.getTTSLocale(getApplicationContext()).toString());

                                switch (bundle.getInt(LocalRequest.EXTRA_CONDITION)) {

                                    case Condition.CONDITION_IDENTIFY:
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "onHandleIntent: CONDITION_IDENTIFY");
                                        }

                                        final Speaker.Confidence confidence = (Speaker.Confidence) bundle.getSerializable(
                                                Speaker.EXTRA_IDENTIFY_OUTCOME);

                                        if (confidence != null) {

                                            switch (confidence) {

                                                case HIGH:
                                                    if (DEBUG) {
                                                        MyLog.i(CLS_NAME, "checkResult: confidence: "
                                                                + Speaker.Confidence.HIGH.name());
                                                    }

                                                    bundle.putString(LocalRequest.EXTRA_UTTERANCE,
                                                            PersonalityResponse.getVocalIDHigh(getApplicationContext(),
                                                                    sl));
                                                    break;
                                                case NORMAL:
                                                    if (DEBUG) {
                                                        MyLog.i(CLS_NAME, "checkResult: confidence: "
                                                                + Speaker.Confidence.NORMAL.name());
                                                    }
                                                    bundle.putString(LocalRequest.EXTRA_UTTERANCE,
                                                            PersonalityResponse.getVocalIDMedium(getApplicationContext(),
                                                                    sl));
                                                    break;
                                                case LOW:
                                                    if (DEBUG) {
                                                        MyLog.i(CLS_NAME, "checkResult: confidence: "
                                                                + Speaker.Confidence.LOW.name());
                                                    }
                                                    bundle.putString(LocalRequest.EXTRA_UTTERANCE,
                                                            PersonalityResponse.getVocalIDLow(getApplicationContext(),
                                                                    sl));
                                                    break;
                                                case UNDEFINED:
                                                    if (DEBUG) {
                                                        MyLog.i(CLS_NAME, "checkResult: confidence: "
                                                                + Speaker.Confidence.UNDEFINED.name());
                                                    }
                                                    bundle.putString(LocalRequest.EXTRA_UTTERANCE,
                                                            PersonalityResponse.getVocalIDLow(getApplicationContext(),
                                                                    sl));
                                                    break;
                                                case ERROR:
                                                    if (DEBUG) {
                                                        MyLog.i(CLS_NAME, "checkResult: confidence: "
                                                                + Speaker.Confidence.ERROR.name());
                                                    }
                                                    bundle.putString(LocalRequest.EXTRA_UTTERANCE,
                                                            PersonalityResponse.getVocalIDError(getApplicationContext(),
                                                                    sl));
                                                    break;
                                            }
                                        }

                                        break;
                                    case Condition.CONDITION_IDENTITY:
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "onHandleIntent: CONDITION_IDENTITY");
                                        }

                                        if (bundle.getBoolean(Speaker.EXTRA_IDENTITY_OUTCOME, false)) {
                                            bundle.putString(LocalRequest.EXTRA_UTTERANCE,
                                                    SaiyResourcesHelper.getStringResource(getApplicationContext(),
                                                            sl, R.string.speech_enroll_success));
                                        } else {
                                            bundle.putString(LocalRequest.EXTRA_UTTERANCE,
                                                    PersonalityResponse.getEnrollmentError(getApplicationContext(),
                                                            sl));
                                        }

                                        break;
                                }

                                new LocalRequest(getApplicationContext(), bundle).execute();

                                break;
                            default:
                                if (DEBUG) {
                                    MyLog.w(CLS_NAME, "onHandleIntent: default");
                                }
                                new LocalRequest(getApplicationContext(), bundle).execute();
                                break;
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "onHandleIntent: ACTION_UNKNOWN");
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onHandleIntent: Bundle null");
                    }
                }
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "onHandleIntent: Action null");
                }
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, " onHandleIntent: Intent null");
            }
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
            //noinspection Convert2streamapi
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

