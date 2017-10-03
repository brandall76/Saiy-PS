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

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Pair;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ai.saiy.android.accessibility.SaiyAccessibilityService;
import ai.saiy.android.api.Defaults;
import ai.saiy.android.api.RequestParcel;
import ai.saiy.android.api.SaiyDefaults;
import ai.saiy.android.api.helper.CallbackType;
import ai.saiy.android.api.helper.Validation;
import ai.saiy.android.configuration.NuanceConfiguration;
import ai.saiy.android.error.Issue;
import ai.saiy.android.error.IssueContent;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.service.ISaiyListener;
import ai.saiy.android.service.SelfAware;
import ai.saiy.android.tts.SaiyTextToSpeech;
import ai.saiy.android.ui.activity.ActivityIssue;
import ai.saiy.android.ui.notification.NotificationHelper;
import ai.saiy.android.utils.Global;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsBundle;
import ai.saiy.android.utils.UtilsString;

/**
 * A utility Class that provides methods to {@link SelfAwareConditions} mainly to avoid
 * cluttering {@link SelfAware} that is already a very busy Class.
 * <p>
 * Created by benrandall76@gmail.com on 06/02/2016.
 */
public class SelfAwareHelper {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = SelfAwareHelper.class.getSimpleName();

    private static final String FULL_STOP_SPACE = ". ";
    private static final String QUESTION_MARK_SPACE = "? ";
    private static final String EXCLAMATION_MARK_SPACE = "! ";
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String COMMA_SPACE = ", ";
    private static final String JUST_A_SPACE = " ";

    private static final Pattern pSERVICE_SELF_AWARE = Pattern.compile(SelfAware.class.getName());

    private final Context mContext;
    private final TelephonyManager telephonyManager;

    /**
     * Constructor
     *
     * @param mContext         the application context
     * @param telephonyManager the {@link TelephonyManager}
     */
    public SelfAwareHelper(@NonNull final Context mContext, @NonNull final TelephonyManager telephonyManager) {
        this.mContext = mContext;
        this.telephonyManager = telephonyManager;
    }

    /**
     * The Text to Speech provider has returned an error. It may be sufficient to just let the user know
     * something went wrong by displaying an error Toast. However, we need to check the {@link Condition}
     * to see if the user was in the middle of a command and in such a case, we'll need to attempt
     * to resolve the issue and continue from where we left off.
     *
     * @param defaultTTS one of {@link SaiyDefaults.TTS}
     * @param bundle     containing any possible {@link Condition}
     */
    public void handleTTSError(@NonNull final SaiyDefaults.TTS defaultTTS, @NonNull final Bundle bundle) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "handleTTSError");
        }

        boolean showToast = true;

        switch (bundle.getInt(LocalRequest.EXTRA_CONDITION, Condition.CONDITION_NONE)) {

            case Condition.CONDITION_CONVERSATION:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Condition.CONDITION_CONVERSATION");
                }
                // TODO
                break;
            case Condition.CONDITION_ROOT:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Condition.CONDITION_ROOT");
                }
                // TODO
                break;
            case Condition.CONDITION_EMOTION:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Condition.CONDITION_EMOTION");
                }
                // TODO
                break;
            case Condition.CONDITION_GOOGLE_NOW:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Condition.CONDITION_GOOGLE_NOW");
                }
                // TODO
                break;
            case Condition.CONDITION_IDENTITY:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Condition.CONDITION_IDENTITY");
                }
                // TODO
                break;
            case Condition.CONDITION_IGNORE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Condition.CONDITION_IGNORE");
                }
                // TODO
                break;
            case Condition.CONDITION_SECURE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Condition.CONDITION_SECURE");
                }
                // TODO
                break;
            case Condition.CONDITION_TRANSLATION:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Condition.CONDITION_TRANSLATION");
                }
                break;
            case Condition.CONDITION_USER_CUSTOM:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Condition.CONDITION_USER_CUSTOM");
                }
                // TODO
                break;
            case Condition.CONDITION_NONE:
            default:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Condition.CONDITION_NONE");
                }

                break;
        }

        if (showToast) {
            showTTSErrorToast(defaultTTS);
        }
    }

    /**
     * @param defaultVR one of {@link SaiyDefaults.VR}
     * @param bundle    containing any possible {@link Condition}
     */
    public void handleVRError(@NonNull final SaiyDefaults.VR defaultVR, @NonNull final SupportedLanguage sl,
                              @NonNull final Locale vrLocale, @NonNull final Locale ttsLocale,
                              @NonNull final Bundle bundle) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "handleVRError");
        }

        switch (bundle.getInt(LocalRequest.EXTRA_CONDITION, Condition.CONDITION_NONE)) {

            case Condition.CONDITION_CONVERSATION:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Condition.CONDITION_CONVERSATION");
                }
                // TODO
                break;
            case Condition.CONDITION_ROOT:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Condition.CONDITION_ROOT");
                }
                // TODO
                break;
            case Condition.CONDITION_EMOTION:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Condition.CONDITION_EMOTION");
                }
                // TODO
                break;
            case Condition.CONDITION_GOOGLE_NOW:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Condition.CONDITION_GOOGLE_NOW");
                }
                // TODO
                break;
            case Condition.CONDITION_IDENTITY:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Condition.CONDITION_IDENTITY");
                }
                announceVRError(sl, vrLocale, ttsLocale);
                break;
            case Condition.CONDITION_IDENTIFY:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Condition.CONDITION_IDENTIFY");
                }
                announceVRError(sl, vrLocale, ttsLocale);
                break;
            case Condition.CONDITION_IGNORE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Condition.CONDITION_IGNORE");
                }
                // TODO
                break;
            case Condition.CONDITION_SECURE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Condition.CONDITION_SECURE");
                }
                // TODO
                break;
            case Condition.CONDITION_TRANSLATION:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Condition.CONDITION_TRANSLATION");
                }
                // TODO
                break;
            case Condition.CONDITION_USER_CUSTOM:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Condition.CONDITION_USER_CUSTOM");
                }
                // TODO
                break;
            case Condition.CONDITION_NONE:
            default:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Condition.CONDITION_NONE");
                }
                // TODO
                break;
        }
    }

    private void announceVRError(@NonNull final SupportedLanguage sl, @NonNull final Locale vrLocale,
                                 @NonNull final Locale ttsLocale) {

        final LocalRequest request = new LocalRequest(mContext);
        request.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, sl, vrLocale, ttsLocale,
                PersonalityResponse.getEnrollmentAPIError(mContext, sl));
        request.execute();

    }

    private void showTTSErrorToast(@NonNull final SaiyDefaults.TTS defaultTTS) {

        switch (defaultTTS) {

            case LOCAL:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "handleTTSError: SpeechDefault.LOCAL");
                }
                showToast(mContext.getString(ai.saiy.android.R.string.error_tts_progress), Toast.LENGTH_SHORT);
                break;
            case NETWORK_NUANCE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "handleTTSError: SpeechDefault.NETWORK_NUANCE");
                }
                showToast(mContext.getString(ai.saiy.android.R.string.error_tts_progress), Toast.LENGTH_SHORT);
                break;
            default:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "handleTTSError: SpeechDefault.default");
                }
                showToast(mContext.getString(ai.saiy.android.R.string.error_tts_progress), Toast.LENGTH_SHORT);
                break;
        }
    }


    /**
     * The recognition provider has returned an error. It may be sufficient to just let the user know
     * something went wrong by displaying an error Toast. However, we need to check the {@link Condition}
     * to see if the user was in the middle of a command and in such a case, , we'll need to attempt
     * to resolve the issue and continue from where we left off.
     *
     * @param error  the {@link SpeechRecognizer} error constant
     * @param bundle containing any possible {@link Condition}
     */
    public void handleRecognitionError(final int error, @NonNull final Bundle bundle,
                                       @NonNull final SaiyDefaults.VR defaultRecognizer,
                                       @NonNull final SupportedLanguage sl, @NonNull final Locale vrLocale, @NonNull final Locale ttsLocale) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "handleRecognitionError");
        }

        switch (bundle.getInt(LocalRequest.EXTRA_CONDITION, Condition.CONDITION_NONE)) {

            case Condition.CONDITION_CONVERSATION:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Condition.CONDITION_CONVERSATION");
                }
                // TODO
                break;
            case Condition.CONDITION_ROOT:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Condition.CONDITION_ROOT");
                }
                // TODO
                break;
            case Condition.CONDITION_EMOTION:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Condition.CONDITION_EMOTION");
                }
                // TODO
                break;
            case Condition.CONDITION_GOOGLE_NOW:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Condition.CONDITION_GOOGLE_NOW");
                }
                // TODO
                break;
            case Condition.CONDITION_IDENTITY:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Condition.CONDITION_IDENTITY");
                }
                // TODO
                break;
            case Condition.CONDITION_IGNORE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Condition.CONDITION_IGNORE");
                }
                // TODO
                break;
            case Condition.CONDITION_SECURE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Condition.CONDITION_SECURE");
                }
                // TODO
                break;
            case Condition.CONDITION_TRANSLATION:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Condition.CONDITION_TRANSLATION");
                }
                // TODO
                break;
            case Condition.CONDITION_USER_CUSTOM:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Condition.CONDITION_USER_CUSTOM");
                }
                // TODO
                break;
            case Condition.CONDITION_NONE:
            default:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Condition.CONDITION_NONE");
                }

                switch (error) {

                    case SpeechRecognizer.ERROR_AUDIO:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "handleRecognitionError: ERROR_AUDIO");
                        }
                        showToast("SpeechRecognizer.ERROR_AUDIO", Toast.LENGTH_SHORT);
                        break;
                    case SpeechRecognizer.ERROR_CLIENT:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "handleRecognitionError: ERROR_CLIENT");
                        }
                        showToast("SpeechRecognizer.ERROR_CLIENT", Toast.LENGTH_SHORT);
                        break;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "handleRecognitionError: ERROR_INSUFFICIENT_PERMISSIONS");
                        }
                        showToast("SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS", Toast.LENGTH_SHORT);
                        break;
                    case SpeechRecognizer.ERROR_NETWORK:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "handleRecognitionError: ERROR_NETWORK");
                        }
                        showToast("SpeechRecognizer.ERROR_NETWORK", Toast.LENGTH_SHORT);
                        break;
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "handleRecognitionError: ERROR_NETWORK_TIMEOUT");
                        }
                        showToast("SpeechRecognizer.ERROR_NETWORK_TIMEOUT", Toast.LENGTH_SHORT);
                        break;
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "handleRecognitionError: ERROR_NO_MATCH");
                        }
                        showToast("SpeechRecognizer.ERROR_NO_MATCH", Toast.LENGTH_SHORT);
                        break;
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "handleRecognitionError: ERROR_RECOGNIZER_BUSY");
                        }
                        showToast("SpeechRecognizer.ERROR_RECOGNIZER_BUSY", Toast.LENGTH_SHORT);
                        break;
                    case SpeechRecognizer.ERROR_SERVER:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "handleRecognitionError: ERROR_SERVER");
                        }
                        showToast("SpeechRecognizer.ERROR_SERVER", Toast.LENGTH_SHORT);
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "handleRecognitionError: ERROR_SPEECH_TIMEOUT");
                        }
                        showToast("SpeechRecognizer.ERROR_SPEECH_TIMEOUT", Toast.LENGTH_SHORT);
                        break;
                    case SelfAware.JB_TIMEOUT_ERROR:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "handleRecognitionError: JB_TIMEOUT_ERROR");
                        }
                        showToast("SpeechRecognizer.JB_TIMEOUT_ERROR", Toast.LENGTH_SHORT);
                        break;
                    default:
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "handleRecognitionError: ERROR_UNKNOWN");
                        }
                        showToast("SpeechRecognizer.ERROR_UNKNOWN", Toast.LENGTH_SHORT);
                        break;
                }
                break;
        }
    }

    /**
     * The user's device is missing a voice recognition provider. This method will start the
     * {@link ActivityIssue} with the relevant details populated in
     * {@link IssueContent}
     */
    public void issueNoVRProvider() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "issueNoVRProvider");
        }

        new Issue(mContext, Issue.ISSUE_NO_VR).execute();
    }

    /**
     * The user's device is missing a text to speech. This method will start the
     * {@link ActivityIssue} with the relevant details populated in
     * {@link IssueContent}
     */
    public void issueNoTTSProvider() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "issueNoTTSProvider");
        }

        new Issue(mContext, Issue.ISSUE_NO_TTS_ENGINE).execute();
    }

    /**
     * Check if the device radio is currently in use. If this is called with a pending callback, we
     * can assume it caused an {@link CallbackType#CB_INTERRUPTED} error.
     *
     * @return true if the radio is in use
     */
    public boolean isInterrupted() {
        return telephonyManager.getCallState() != TelephonyManager.CALL_STATE_IDLE;
    }

    /**
     * Check the device conditions to see if it is an appropriate time to use TTS playback.
     *
     * @return true if the conditions are satisfied.
     */
    public boolean shouldSpeak() {
        return telephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE;
    }

    /**
     * Helper method to extract the package name from an incoming intent. Such intents should always
     * have included their package name in {@link Intent#setAction(String)}, which will be updated
     * each time {@link android.app.Service#onBind(Intent)} or {@link android.app.Service#onRebind(Intent)} is called.
     *
     * @param intent to extract the package from
     * @return the package name or null if the package cannot be resolved
     */
    public String getPackage(final Intent intent) {

        if (intent != null) {
            final String action = intent.getAction();

            if (action != null) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getPackage: " + action);
                }
                return action;
            } else {
                MyLog.w(CLS_NAME, mContext.getString(ai.saiy.android.R.string.error_package_name_null));
            }
        } else {
            MyLog.w(CLS_NAME, mContext.getString(ai.saiy.android.R.string.error_intent_null));
        }

        return null;
    }


    /**
     * Check the {@link Intent} received from the {@link android.app.Service#onBind(Intent)} to see
     * if the request should be declined, or if it's a local or remote request.
     *
     * @param intent         received
     * @param blacklistArray holding the currently blacklisted packages.
     * @return a {@link Pair} with {@link Pair#first} true for permission to bind, with
     * {@link Pair#second} true if the request is local
     */
    public Pair<Boolean, Boolean> shouldBind(final Intent intent, final ArrayList<String> blacklistArray) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "shouldBind");
        }

        final String packageName = getPackage(intent);

        if (packageName != null) {
            if (packageName.matches(mContext.getPackageName())) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "shouldBind: returning local");
                }
                return new Pair<>(true, true);
            } else {
                if (blacklistArray.contains(packageName)) {
                    MyLog.e(CLS_NAME, mContext.getString(ai.saiy.android.R.string.error_package_blacklisted, packageName));
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "shouldBind: returning remote");
                    }
                    return new Pair<>(true, false);
                }
            }
        }

        return new Pair<>(false, false);
    }

    /**
     * Check the {@link Intent} received from the {@link android.app.Service#onRebind(Intent)} to see
     * if the request should be declined.
     *
     * @param intent         received
     * @param blacklistArray holding the currently blacklisted packages.
     * @return true if the rebind should be permitted.
     */
    public boolean shouldRebind(final Intent intent, @NonNull final ArrayList<String> blacklistArray) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "shouldRebind");
        }

        final String packageName = getPackage(intent);

        if (UtilsString.notNaked(packageName)) {
            if (blacklistArray.contains(packageName)) {
                MyLog.e(CLS_NAME, mContext.getString(ai.saiy.android.R.string.error_package_blacklisted, packageName));
                return false;
            } else {
                return true;
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "shouldRebind: packageName naked");
            }
        }

        return false;
    }

    /**
     * Check the {@link Intent} received from the {@link android.app.Service#onUnbind(Intent)} to see
     * if we should allow future binds to call {@link android.app.Service#onRebind(Intent)}.
     *
     * @param intent received
     * @return true if we will permit future calls to {@link android.app.Service#onRebind(Intent)}
     */
    public boolean shouldUnbind(final Intent intent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "shouldUnbind");
        }

        if (getPackage(intent) != null) {
            return true;
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "shouldUnbind: package name null. Returning false");
            }
            return false;
        }
    }

    /**
     * Over-cautious check to see if the Text to Speech engine is ready to use. May seem like overkill,
     * but the cause of many, many crash reports.
     *
     * @return true if the TTS Engine is ready to use
     */
    public boolean checkTTSConditions(final SaiyTextToSpeech tts, final SaiyDefaults.TTS defaultTTS) {

        if (defaultTTS != SaiyDefaults.TTS.NETWORK_NUANCE) {

            if (DEBUG) {
                if (tts == null) {
                    MyLog.w(CLS_NAME, "checkTTSConditions: tts null");
                } else {
                    MyLog.i(CLS_NAME, "checkTTSConditions: tts assigned");
                }
            }

            return tts != null && !tts.shouldReinitialise(null);

        } else {
            return true;
        }
    }

    /**
     * Validate the remote request, by checking that a {@link ISaiyListener} has been attached.
     * If it hasn't, an error is logged and the request ignored. There is no way to send an error
     * back to the caller, as the interface is null.
     * <p>
     *
     * @param rl the remote {@link ISaiyListener}
     * @return true if the listener is present
     */
    public boolean validateRemote(final ISaiyListener rl) throws RemoteException {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "validateRemote");
        }

        if (rl != null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "validateRemote: rl valid");
            }
            return true;
        } else {
            MyLog.e("Remote Saiy Request", mContext.getString(ai.saiy.android.R.string.error_saiylistener_null));
        }

        return false;
    }

    /**
     * Check that the @param bundle is valid, this includes scrubbing it for parameters that could
     * cause the app to crash. If it is not valid, an error is logged and the request ignored.
     * <p>
     *
     * @param rl     the remote {@link ISaiyListener}
     * @param bundle the bundle to check
     * @return true if the bundle is acceptable
     */
    public boolean validateRemoteBundle(final ISaiyListener rl, final Bundle bundle) throws RemoteException {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "validateRemoteBundle");
        }

        if (bundle != null) {
            bundle.setClassLoader(RequestParcel.class.getClassLoader());

            if (UtilsBundle.isSuspicious(bundle)) {
                MyLog.e("Remote Saiy Request", mContext.getString(ai.saiy.android.R.string.error_bundle_corrupt));
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "validateRemoteBundle: bundle valid");
                }
                return true;
            }
        } else {
            MyLog.e("Remote Saiy Request", mContext.getString(ai.saiy.android.R.string.error_bundle_null));
        }

        rl.onError(Defaults.ERROR.ERROR_DEVELOPER.name(), Validation.ID_UNKNOWN);
        return false;
    }

    /**
     * Another remote request is being processed and the interface will return
     * {@link Defaults.ERROR#ERROR_BUSY}. Beforehand, check that the
     * {@link RequestParcel} is valid, this includes the basic requirements of a request id.
     * If it is not valid, an error is logged and the request ignored.
     * <p>
     * Otherwise, the busy response is sent.
     *
     * @param rl     the remote {@link ISaiyListener}
     * @param bundle the bundle to check
     */
    public void manageRemoteBusy(final ISaiyListener rl, final Bundle bundle) throws RemoteException {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "manageRemoteBusy");
        }

        if (rl != null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "manageRemoteBusy: rl valid");
            }

            if (bundle != null) {
                bundle.setClassLoader(RequestParcel.class.getClassLoader());

                if (UtilsBundle.isSuspicious(bundle)) {
                    MyLog.e("Remote Saiy Request", mContext.getString(ai.saiy.android.R.string.error_bundle_corrupt));
                } else {

                    final RequestParcel parcel = bundle.getParcelable(RequestParcel.PARCEL_KEY);

                    if (Validation.validateParcel(mContext, parcel)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "manageRemoteBusy: validateParcel successful");
                        }
                        if (Validation.validateParams(mContext, parcel)) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "manageRemoteBusy: validateParams successful: returning busy");
                            }
                            rl.onError(Defaults.ERROR.ERROR_BUSY.name(), parcel.getRequestId());
                        } else {
                            MyLog.e("Remote Saiy Request", mContext.getString(ai.saiy.android.R.string.error_request_parcel_params_invalid));
                            rl.onError(Defaults.ERROR.ERROR_DEVELOPER.name(), Validation.ID_UNKNOWN);
                        }
                    } else {
                        MyLog.e("Remote Saiy Request", mContext.getString(ai.saiy.android.R.string.error_request_parcel_invalid));
                        rl.onError(Defaults.ERROR.ERROR_DEVELOPER.name(), Validation.ID_UNKNOWN);
                    }
                }
            } else {
                MyLog.e("Remote Saiy Request", mContext.getString(ai.saiy.android.R.string.error_bundle_null));
                rl.onError(Defaults.ERROR.ERROR_DEVELOPER.name(), Validation.ID_UNKNOWN);
            }
        } else {
            MyLog.e("Remote Saiy Request", mContext.getString(ai.saiy.android.R.string.error_saiylistener_null));
        }
    }

    /**
     * When using Nuance Mix.nlu we need to make sure we use the right server host.
     *
     * @return the corresponding Uri
     * @see <a href="https://developer.nuance.com/phpbb/viewtopic.php?f=15&t=1159&sid=b03f0098fdc1cc5078929b4f39237a0f">Which host/a>
     */
    public Uri getNuanceUri(final boolean isTTS) {

        if (!isTTS) {
            switch (SPH.getDefaultLanguageModel(mContext)) {
                case NUANCE:
                    return NuanceConfiguration.SERVER_URI_NLU;
            }
        }

        return NuanceConfiguration.SERVER_URI;
    }

    /**
     * Check the length of the speech.
     *
     * @param utterance the utterance to be spoken
     * @return the length of the speech or zero if it is null
     */
    public int getSpeechLength(final String utterance) {

        if (UtilsString.notNaked(utterance)) {
            return utterance.length();
        }

        return 0;
    }

    /**
     * Start the SelfAware service if it is not running using the {@link Global} application context.
     * This context is used to prevent, where ever possible, Android from associating memory objects to
     * service.
     * <p>
     * A double check is performed on the {@link SelfAware instance} due to this
     * API being 'unsuitable' for production.
     * <p>
     * The SelfAware.instance has provided erroneous results in low memory conditions.
     *
     * @param ctx the application context to use to start the service
     * @see <a href="http://stackoverflow.com/q/16153674/1256219">Service MemoryPrepare Objects/a>
     */
    public static void startSelfAwareIfRequired(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "startSelfAwareIfRequired");
        }

        if (!selfAwareRunning(ctx)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "startSelfAwareIfRequired: not running - starting");
            }
            startService(ctx);
        }
    }

    /**
     * Start the {@link SelfAware} service
     *
     * @param ctx the application context
     */
    public static void startService(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "startService");
        }

        NotificationHelper.createNotificationChannels(ctx);

        final Intent intent = new Intent(ctx, SelfAware.class);
        intent.setAction(ctx.getApplicationContext().getPackageName());
        ctx.getApplicationContext().startService(intent);
    }

    /**
     * restart the {@link SelfAware} service
     *
     * @param ctx the application context
     */
    public static void restartService(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "restartService");
        }

        if (selfAwareRunning(ctx)) {
            stopService(ctx);
        }

        startService(ctx);
    }

    /**
     * Toggle the {@link SelfAware} service
     *
     * @param ctx the application context
     */
    public static void toggleService(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "toggleService");
        }

        if (selfAwareRunning(ctx)) {
            stopService(ctx);
        } else {
            startService(ctx);
        }
    }

    /**
     * Stop the {@link SelfAware} service
     *
     * @param ctx the application context
     */
    public static void stopService(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "stopService");
        }

        if (selfAwareRunning(ctx)) {
            final Intent intent = new Intent(ctx, SelfAware.class);
            intent.setAction(ctx.getApplicationContext().getPackageName());
            ctx.getApplicationContext().stopService(intent);
        }
    }

    /**
     * Start the {@link SaiyAccessibilityService} service
     *
     * @param ctx the application context
     */
    public static void startAccessibilityService(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "startAccessibilityService");
        }

        final Intent intent = new Intent(ctx, SaiyAccessibilityService.class);
        intent.setAction(ctx.getApplicationContext().getPackageName());
        ctx.getApplicationContext().startService(intent);
    }

    /**
     * Check the running condition of {@link SaiyAccessibilityService}
     *
     * @param ctx the application context
     */
    public static boolean saiyAccessibilityRunning(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "saiyAccessibilityRunning");
        }

        final AccessibilityManager aManager = (AccessibilityManager) ctx.getSystemService(Context.ACCESSIBILITY_SERVICE);

        String className;
        for (final AccessibilityServiceInfo service : aManager.getEnabledAccessibilityServiceList(
                AccessibilityServiceInfo.FEEDBACK_ALL_MASK)) {

            try {

                className = service.getId();

                if (className != null) {
                    if (className.trim().endsWith(SaiyAccessibilityService.class.getSimpleName())) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "SaiyAccessibilityService running");
                        }
                        return true;
                    }
                }

            } catch (final NullPointerException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "NullPointerException");
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "Exception");
                }
            }
        }

        if (DEBUG) {
            MyLog.i(CLS_NAME, "SaiyAccessibilityService not running");
        }

        return false;
    }

    /**
     * Check the running condition of {@link SelfAware}
     *
     * @param ctx the application context
     */
    public static boolean selfAwareRunning(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "selfAwareRunning");
        }

        final ActivityManager aManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);

        ComponentName cName;
        String className;

        for (final ActivityManager.RunningServiceInfo service : aManager.getRunningServices(Integer.MAX_VALUE)) {

            try {

                cName = service.service;

                if (cName != null) {
                    className = cName.getClassName();

                    if (className != null) {
                        if (pSERVICE_SELF_AWARE.matcher(className).matches()) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "SelfAware in runningInfo");
                            }

                            final boolean doubleCheck = SelfAware.checkInstance();
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "SelfAware double check is running: " + doubleCheck);
                            }

                            return doubleCheck;
                        }
                    }
                }

            } catch (final NullPointerException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "NullPointerException");
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "Exception");
                }
            }
        }

        if (DEBUG) {
            MyLog.i(CLS_NAME, "SelfAware not running");
        }

        return false;
    }

    /**
     * Show an error toast on the main thread.
     *
     * @param toastWords the String to toast
     * @param length     one of {@link Toast#LENGTH_LONG} or {@link Toast#LENGTH_SHORT}
     */
    public void showToast(@Nullable final String toastWords, final int length) {

        if (UtilsString.notNaked(toastWords)) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, toastWords, length).show();
                }
            });
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "showToast: naked String: ignoring");
            }
        }
    }

    /**
     * Method to time the completion of code sections.
     *
     * @param then the start time
     * @return the elapsed time in milliseconds.
     */
    public long getElapsed(final Long then) {
        return TimeUnit.MILLISECONDS.convert((System.nanoTime() - then), TimeUnit.NANOSECONDS);
    }

    /**
     * Split the utterance into separate strings that are of a size that doesn't exceed the maximum
     * amount that the initialised voice engine accepts. This amount varies between engines.
     *
     * @param utterance the utterance to split
     * @param maxLength the maximum length of utterance the initialised engine allows
     * @return an ArrayList<String> containing the multiple split utterances.
     */
    public static ArrayList<String> splitUtteranceRegex(@NonNull final String utterance, final int maxLength) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "splitUtteranceRegex: ");
        }

        final long then = System.nanoTime();

        final ArrayList<String> speakableUtterances = new ArrayList<>();

        final Pattern p = Pattern.compile(".{" + String.valueOf(1) + "," + maxLength + "}(?:[.!?,]\\s+|\\n|$)",
                Pattern.DOTALL);

        final Matcher matcher = p.matcher(utterance);
        String groupUtterance;
        int length;
        boolean next = matcher.find();

        while (next) {
            groupUtterance = matcher.group().trim();
            length = groupUtterance.length();
            speakableUtterances.add(matcher.group().trim());

            if (DEBUG) {
                MyLog.i(CLS_NAME, "Group: " + length + ":- " + groupUtterance);
            }

            next = matcher.find();

            if (next && length < SaiyTextToSpeech.MIN_UTTERANCE_LENGTH) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "matcher failed to separate sufficiently: falling back ");
                }

                return splitUtteranceDeliberate(utterance);
            }
        }

        if (speakableUtterances.isEmpty()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "matcher failed empty: falling back ");
            }
            return splitUtteranceDeliberate(utterance);
        }

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME + " splitUtteranceRegex:", then);
        }

        return speakableUtterances;

    }

    /**
     * Called if the more readable {@link #splitUtteranceRegex(String, int)} has failed.
     *
     * @param utterance the utterance to split
     * @return an {@code ArrayList<String>} containing the multiple split utterances.
     */
    public static ArrayList<String> splitUtteranceDeliberate(@NonNull String utterance) {

        final long then = System.nanoTime();
        final ArrayList<String> speakableUtterances = new ArrayList<>();

        int splitLocation;
        String success;

        while (utterance.length() > SaiyTextToSpeech.MAX_UTTERANCE_LENGTH) {
            splitLocation = utterance.lastIndexOf(FULL_STOP_SPACE, SaiyTextToSpeech.MAX_UTTERANCE_LENGTH);

            if (DEBUG) {
                MyLog.i(CLS_NAME, "(0 FULL STOP) - last index at: " + splitLocation);
            }

            if (splitLocation < SaiyTextToSpeech.MIN_UTTERANCE_LENGTH) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "(1 FULL STOP) - NOT_OK");
                }
                splitLocation = utterance.lastIndexOf(QUESTION_MARK_SPACE, SaiyTextToSpeech.MAX_UTTERANCE_LENGTH);

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "(1 QUESTION MARK) - last index at: " + splitLocation);
                }

                if (splitLocation < SaiyTextToSpeech.MIN_UTTERANCE_LENGTH) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "(2 QUESTION MARK) - NOT_OK");
                    }
                    splitLocation = utterance.lastIndexOf(EXCLAMATION_MARK_SPACE, SaiyTextToSpeech.MAX_UTTERANCE_LENGTH);

                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "(2 EXCLAMATION MARK) - last index at: " + splitLocation);
                    }

                    if (splitLocation < SaiyTextToSpeech.MIN_UTTERANCE_LENGTH) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "(3 EXCLAMATION MARK) - NOT_OK");
                        }
                        splitLocation = utterance.lastIndexOf(LINE_SEPARATOR, SaiyTextToSpeech.MAX_UTTERANCE_LENGTH);

                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "(3 SEPARATOR) - last index at: " + splitLocation);
                        }

                        if (splitLocation < SaiyTextToSpeech.MIN_UTTERANCE_LENGTH) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "(4 SEPARATOR) - NOT_OK");
                            }
                            splitLocation = utterance.lastIndexOf(COMMA_SPACE, SaiyTextToSpeech.MAX_UTTERANCE_LENGTH);

                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "(4 COMMA) - last index at: " + splitLocation);
                            }

                            if (splitLocation < SaiyTextToSpeech.MIN_UTTERANCE_LENGTH) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "(5 COMMA) - NOT_OK");
                                }
                                splitLocation = utterance.lastIndexOf(JUST_A_SPACE, SaiyTextToSpeech.MAX_UTTERANCE_LENGTH);

                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "(5 SPACE) - last index at: " + splitLocation);
                                }

                                if (splitLocation < SaiyTextToSpeech.MIN_UTTERANCE_LENGTH) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "(6 SPACE) - NOT_OK");
                                    }
                                    splitLocation = SaiyTextToSpeech.MAX_UTTERANCE_LENGTH;

                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "(6 MAX_UTTERANCE_LENGTH) - last index at: " + splitLocation);
                                    }
                                } else {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "Accepted");
                                    }
                                    splitLocation -= 1;
                                }
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "Accepted");
                            }
                            splitLocation -= 1;
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "Accepted");
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Accepted");
                    }
                }
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Accepted");
                }
            }

            success = utterance.substring(0, (splitLocation + 2));
            speakableUtterances.add(success.trim());

            if (DEBUG) {
                MyLog.i(CLS_NAME, "Split - Length: " + success.length() + " -:- " + success);
                MyLog.i(CLS_NAME, "------------------------------");
            }

            utterance = utterance.substring((splitLocation + 2)).trim();
        }

        speakableUtterances.add(utterance);

        if (DEBUG) {
            MyLog.i(CLS_NAME, "Split - Length: " + utterance.length() + " -:- " + utterance);
            MyLog.getElapsed(CLS_NAME, then);
        }

        return speakableUtterances;
    }
}
