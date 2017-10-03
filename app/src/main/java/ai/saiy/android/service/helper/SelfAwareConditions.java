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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognitionService;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Pair;
import android.widget.Toast;

import com.google.auth.oauth2.AccessToken;
import com.google.common.util.concurrent.RateLimiter;
import com.nuance.speechkit.DetectionType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.api.Defaults;
import ai.saiy.android.api.RequestParcel;
import ai.saiy.android.api.SaiyDefaults;
import ai.saiy.android.api.helper.BlackList;
import ai.saiy.android.api.helper.BlackListHelper;
import ai.saiy.android.api.helper.Callback;
import ai.saiy.android.api.helper.CallbackType;
import ai.saiy.android.api.helper.Validation;
import ai.saiy.android.api.language.nlu.NLULanguageMicrosoft;
import ai.saiy.android.api.language.tts.TTSLanguageNuance;
import ai.saiy.android.api.language.vr.VRLanguageGoogle;
import ai.saiy.android.api.language.vr.VRLanguageIBM;
import ai.saiy.android.api.language.vr.VRLanguageMicrosoft;
import ai.saiy.android.api.language.vr.VRLanguageNuance;
import ai.saiy.android.api.language.vr.VRLanguageWit;
import ai.saiy.android.api.request.SaiyRequestParams;
import ai.saiy.android.audio.AudioParameters;
import ai.saiy.android.audio.RecognitionMic;
import ai.saiy.android.audio.SaiySoundPool;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.configuration.BluemixConfiguration;
import ai.saiy.android.configuration.GoogleConfiguration;
import ai.saiy.android.configuration.MicrosoftConfiguration;
import ai.saiy.android.configuration.NuanceConfiguration;
import ai.saiy.android.configuration.WitConfiguration;
import ai.saiy.android.device.UtilsDevice;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.memory.MemoryPrepare;
import ai.saiy.android.nlu.apiai.RemoteAPIAI;
import ai.saiy.android.permissions.PermissionHelper;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.recognition.Recognition;
import ai.saiy.android.recognition.SaiyHotwordListener;
import ai.saiy.android.recognition.SaiyRecognitionListener;
import ai.saiy.android.recognition.helper.RecognitionDefaults;
import ai.saiy.android.recognition.provider.android.RecognitionNative;
import ai.saiy.android.recognition.provider.bluemix.RecognitionBluemix;
import ai.saiy.android.recognition.provider.google.chromium.RecognitionGoogleChromium;
import ai.saiy.android.recognition.provider.google.cloud.RecognitionGoogleCloud;
import ai.saiy.android.recognition.provider.microsoft.RecognitionMicrosoft;
import ai.saiy.android.recognition.provider.nuance.RecognitionNuance;
import ai.saiy.android.recognition.provider.remote.RecognitionRemote;
import ai.saiy.android.recognition.provider.sphinx.RecognitionSphinx;
import ai.saiy.android.recognition.provider.wit.RecognitionWit;
import ai.saiy.android.service.ISaiyListener;
import ai.saiy.android.service.SelfAware;
import ai.saiy.android.sound.VolumeHelper;
import ai.saiy.android.tts.SaiyProgressListener;
import ai.saiy.android.tts.SaiyTextToSpeech;
import ai.saiy.android.tts.TTS;
import ai.saiy.android.tts.engine.EngineNuance;
import ai.saiy.android.tts.helper.SaiyVoice;
import ai.saiy.android.tts.helper.SpeechPriority;
import ai.saiy.android.tts.helper.TTSDefaults;
import ai.saiy.android.ui.notification.NotificationHelper;
import ai.saiy.android.utils.Conditions.Network;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsLocale;
import ai.saiy.android.utils.UtilsString;

import static ai.saiy.android.applications.Installed.PACKAGE_NAME_GOOGLE;

/**
 * A utility Class that provides methods to {@link SelfAware} mainly to avoid
 * cluttering what is already a very busy Class. It extends a further utility Class {@link SelfAwareHelper}
 * created for the same reason.
 * <p/>
 * There are parameters within this class that are repeatedly checked, and it may make immediate
 * sense to assign these values to local variables - However, persistent variables can cause issues
 * when they are not correctly overwritten for each new request and invalidated upon an error or
 * failure. So to not have to deal with such eventualities, they are not persisted. Performance
 * therefore, may take a minor hit.
 * <p/>
 * Created by benrandall76@gmail.com on 20/03/2016.
 */
public class SelfAwareConditions extends SelfAwareHelper implements IConditionListener {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = SelfAwareConditions.class.getSimpleName();

    private static final String WAKELOCK_TAG = SelfAware.class.getSimpleName() + "Lock";
    private static final String WAKELOCK_DISPLAY_TAG = WAKELOCK_TAG + "Display";

    private static final double THROTTLE_RATE = 4;
    private static final int THROTTLE_IGNORE = 4;

    public static final long VIBRATE_MIN = 40L;
    private static final long FETCHING_DELAY = 1000L;
    public static final long DEFAULT_INACTIVITY_TIMEOUT = 900000L;
    private static final long SCREEN_WAKE_TIME = 10000L;

    private final PowerManager.WakeLock wakeLock;
    private final PowerManager.WakeLock wakeLockDisplay;

    private final Handler handler = new Handler();

    /**
     * Helper class to manage blacklisted applications
     */
    private final BlackListHelper blackListHelper = new BlackListHelper();

    /**
     * An ArrayList of {@link BlackList} data containing information for remote requests that have
     * previously been declined due to throttling constraints. This is generated from scratch each
     * time the Service restarts, to allow the offending application to right its wrongs.
     */
    private final ArrayList<BlackList> throttledArray = new ArrayList<>();

    /**
     * If an external application has misconfigured its request and for example's sake, is making
     * them in an infinite loop, we need to throttle their ability to make Saiy perform their requests
     * or become bogged down attempting to handle them.
     * <p/>
     * Eventually the remote package will be added to a blacklist array which the user
     * can choose to manually release in the Saiy Application Settings.
     * <p/>
     * The rate limiting is currently applied in all conditions of a remote request, such as checking
     * the listening or speaking state - In isolation, it would be less expensive to simply
     * return the state, however, being overcautious in this way will eventually prevent the misbehaving
     * remote application from binding to the service at all; which is a better outcome.
     */
    private final RateLimiter throttle = RateLimiter.create(THROTTLE_RATE);

    /**
     * This is a list of callbacks that have been registered with the service by remote clients.
     * We use this list to respond accordingly.
     */
    private final RemoteCallbackList<ISaiyListener> remoteCallbacks = new RemoteCallbackList<>();

    private volatile Callback callback;
    private volatile Bundle bundle;
    private final Context mContext;
    private volatile int count;
    private volatile boolean isCancelled;
    private volatile boolean restartHotword;
    private final SaiySoundPool saiySoundPool;

    /**
     * Constructor
     * <p/>
     * When a remote application first binds to {@link SelfAware} it may make a number of initial
     * requests to check the state of the application (such as speaking or listening). We set the
     * {@link #count} to zero here and allow it to make {@link #THROTTLE_IGNORE} requests before we
     * start applying any potential throttling limit on requests.
     *
     * @param mContext         the application context
     * @param telephonyManager the {@link TelephonyManager}
     */
    public SelfAwareConditions(@NonNull final Context mContext, @NonNull final TelephonyManager telephonyManager) {
        super(mContext, telephonyManager);
        this.mContext = mContext;
        count = 0;

        final PowerManager manager = (PowerManager) this.mContext.getSystemService(Context.POWER_SERVICE);
        wakeLock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG);

        //noinspection deprecation
        wakeLockDisplay = manager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                WAKELOCK_DISPLAY_TAG);

        saiySoundPool = new SaiySoundPool().setUp(this.mContext, SaiySoundPool.VOICE_RECOGNITION);
    }

    /**
     * Check if the calling remote request can be processed, due to throttling limits defined
     * by {@link #THROTTLE_RATE} per second.
     * <p/>
     * The method also checks if the remote package has already been blacklisted for making repeated
     * requests that appear to be misconfigured. Additionally, it checks if the current request is
     * one of a number of requests that will be declined and therefore should be added to the
     * blacklist.
     *
     * @param callingUid the Uid of the remote request
     * @return true if the remote app is not blacklisted and throttling is within limits.
     */
    private boolean grantAcquire(final int callingUid) throws SecurityException {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "grantAcquire: " + callingUid);
        }

        final String packageName = mContext.getPackageManager().getNameForUid(callingUid);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "grantAcquire: " + packageName);
        }

        count++;

        final ArrayList<String> blacklistArray = blackListHelper.fetch(mContext);

        if (UtilsString.notNaked(packageName) && callingUid > 0) {
            for (final String name : blacklistArray) {
                if (name.matches(packageName)) {
                    MyLog.e(CLS_NAME, mContext.getString(R.string.error_package_blacklisted, packageName));
                    return false;
                }
            }

            if (count < THROTTLE_IGNORE || throttle.tryAcquire()) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "grantAcquire: granted");
                }
                return true;
            } else {
                MyLog.e(CLS_NAME, mContext.getString(R.string.error_throttle_limit));

                final BlackList blackList = new BlackList(packageName, callingUid, System.currentTimeMillis());
                throttledArray.add(blackList);

                if (BlackList.shouldBlackList(throttledArray)) {
                    MyLog.e(CLS_NAME, mContext.getString(R.string.error_package_blacklisted, packageName));
                    blacklistArray.add(blackList.getPackageName());
                    blackListHelper.save(mContext, blacklistArray);
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "grantAcquire: not blacklisting");
                    }
                }

                return false;
            }
        } else {
            MyLog.e(CLS_NAME, mContext.getString(R.string.error_package_name_null));
            return false;
        }

    }

    /**
     * Check if the calling application has the correct permission to control Saiy. This is a
     * secondary check which guards private methods.
     *
     * @param callingUid of the remote request
     * @return true if the permission has been granted.
     */
    public boolean checkSaiyPermission(final int callingUid) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkSaiyPermission");
        }
        return PermissionHelper.checkSaiyPermission(mContext, callingUid);
    }

    /**
     * Check if the calling application has the correct permission to control Saiy.
     *
     * @return true if the permission has been granted.
     */
    public boolean checkSaiyRemotePermission() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkSaiyRemotePermission");
        }
        return PermissionHelper.checkSaiyRemotePermission(mContext);
    }

    /**
     * Check to see if we have the audio permission
     *
     * @return true if the permission has been granted
     */
    public boolean checkAudioPermission() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkAudioPermission");
        }
        return PermissionHelper.checkAudioPermissions(mContext);
    }

    /**
     * Check to see if we have the write files permission
     *
     * @return true if the permission has been granted
     */
    public boolean checkFilePermission() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkFilePermission");
        }
        return PermissionHelper.checkFilePermissions(mContext);
    }

    /**
     * Check if the user deliberately requested to stop the speech or recognition. This handles errors
     * of Text to Speech engines incorrectly throwing {@link android.speech.tts.UtteranceProgressListener#onError(String)}
     * instead of {@link android.speech.tts.UtteranceProgressListener#onStop(String, boolean)}
     * <p/>
     * Additionally, it handles recognition providers throwing {@link RecognitionListener#onError(int)}
     * instead of {@link RecognitionListener#onResults(Bundle)} regardless of whether a segment of
     * speech has already been detected and reported.
     *
     * @return true if it was a deliberate request
     */
    public boolean isUserInterrupted() {
        return getBundle().containsKey(TTSDefaults.EXTRA_INTERRUPTED);
    }

    /**
     * At present, only one remote callback at a time will be allowed to register. This might
     * seem sensible given the nature of the interaction, but when other features are exposed, this
     * will need to be rethought see <a href="http://stackoverflow.com/q/35734112/1256219">Managing remote callbacks/a>
     * <p/>
     * This method can be used to check if we should register a further callback, or to direct any
     * results or interaction to a callback, rather than allowing Saiy to handle it internally.
     *
     * @return true if a callback is currently registered in the {@link RemoteCallbackList}
     */
    public boolean servingRemote() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "servingRemote");
        }

        synchronized (remoteCallbacks) {

            int count = remoteCallbacks.beginBroadcast();
            remoteCallbacks.finishBroadcast();
            if (DEBUG) {
                MyLog.i(CLS_NAME, "servingRemote: size: " + count);
            }

            return count > 0;
        }
    }

    /**
     * Checks all possible parameters from the remote request in overly verbose way. We need to make
     * sure that if anything is misconfigured, it does not cause Saiy to crash.
     *
     * @param rl     the remote request
     * @param bundle of the remote request
     * @return true if the remote request should be actioned
     * @throws RemoteException
     */
    @SuppressWarnings("ConstantConditions")
    public boolean shouldAction(final ISaiyListener rl, final Bundle bundle) throws RemoteException {
        if (DEBUG) {
            MyLog.d(CLS_NAME, "shouldAction");
        }

        if (grantAcquire(Binder.getCallingUid())) {
            if (!servingRemote()) {
                if (validateRemote(rl) && validateRemoteBundle(rl, bundle)) {

                    final RequestParcel parcel = bundle.getParcelable(RequestParcel.PARCEL_KEY);

                    if (validateRemoteParcel(rl, parcel)) {

                        callback = new Callback(parcel, mContext.getPackageManager().getNameForUid(Binder.getCallingUid()),
                                Binder.getCallingUid(), System.currentTimeMillis());

                        if (DEBUG) {
                            MyLog.d(CLS_NAME, "remoteBinder: speakListen: words: " + callback.getParcel().getUtterance());
                            MyLog.d(CLS_NAME, "remoteBinder: speakListen: getRequestId: " + callback.getParcel().getRequestId());
                        }

                        return true;

                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "remoteBinder: speakListen: parcel validation failed");
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "remoteBinder: speakListen: validation failed");
                    }
                }
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "remoteBinder: speakListen: remote busy");
                }

                manageRemoteBusy(rl, bundle);
            }
        }

        return false;
    }

    /**
     * Check if the hotword detection is currently active
     *
     * @param recogSphinx the {@link RecognitionSphinx} instance
     * @return true if the hotword is active, false otherwise
     */
    public boolean isHotwordActive(@Nullable final RecognitionSphinx recogSphinx) {
        return isListening() && recogSphinx != null && recogSphinx.isListening();
    }

    /**
     * Check if the recognition is currently in use
     *
     * @return true if the state is SPEAKING or PROCESSING
     */
    public boolean isListening() {
        final Recognition.State state = Recognition.getState();
        return (state == Recognition.State.LISTENING
                || state == Recognition.State.PROCESSING);
    }

    /**
     * Check if the Text to Speech engine is currently in use. Try/catch due to misbehaving engines
     *
     * @return true if the engine is speaking
     */
    public Pair<Boolean, Integer> isSpeaking(final SaiyTextToSpeech tts) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "isSpeaking");
        }

        boolean speaking = false;

        switch (getDefaultTTS()) {

            case LOCAL:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "isSpeaking: LOCAL");
                }

                try {

                    if (tts == null) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "isSpeaking: LOCAL null");
                        }
                        speaking = false;
                    } else if (tts.isSpeaking()) {
                        speaking = true;
                    }

                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "speaking: " + speaking);
                    }

                    return new Pair<>(speaking,
                            getBundle().getInt(LocalRequest.EXTRA_SPEECH_PRIORITY, SpeechPriority.PRIORITY_NORMAL));
                } catch (final NullPointerException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "isSpeaking: NullPointerException");
                        e.printStackTrace();
                    }
                } catch (final Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "isSpeaking: Exception");
                        e.printStackTrace();
                    }
                }
                break;
            case NETWORK_NUANCE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "isSpeaking: NETWORK_NUANCE");
                }
                speaking = (TTS.getState() == TTS.State.SPEAKING);
                break;
            default:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "isSpeaking: Default?");
                }
                break;
        }

        return new Pair<>(speaking,
                getBundle().getInt(LocalRequest.EXTRA_SPEECH_PRIORITY, SpeechPriority.PRIORITY_NORMAL));
    }

    /**
     * Check the speech request priority to see if it overrides the current process
     *
     * @param tts           the current bound {@link SaiyTextToSpeech} object
     * @param requestBundle the {@link Bundle} of the request
     * @return a pair containing two {@link Pair} with the first parameter of the first pair denoting if the
     * request should proceed and the second if there is an overriding action. The first parameter of the
     * second pair denotes if the Text to Speech engine is currently speaking and the second if the
     * voice recognition is currently in use.
     */
    public Pair<Pair<Boolean, Boolean>, Pair<Boolean, Boolean>> proceedPriority(@NonNull final SaiyTextToSpeech tts,
                                                                                @NonNull final Bundle requestBundle) {

        final Pair<Boolean, Integer> isSpeakingPair = isSpeaking(tts);
        final boolean isListening = isListening();

        if (isSpeakingPair.first || isListening) {

            final int requestPriority = requestBundle.getInt(LocalRequest.EXTRA_SPEECH_PRIORITY, SpeechPriority.PRIORITY_NORMAL);
            final int currentPriority = isSpeakingPair.second;

            if (DEBUG) {
                switch (requestPriority) {

                    case SpeechPriority.PRIORITY_LOW:
                        MyLog.i(CLS_NAME, "requestPriority: PRIORITY_LOW");
                        break;
                    case SpeechPriority.PRIORITY_REMOTE:
                        MyLog.i(CLS_NAME, "requestPriority: PRIORITY_REMOTE");
                        break;
                    case SpeechPriority.PRIORITY_NORMAL:
                        MyLog.i(CLS_NAME, "requestPriority: PRIORITY_NORMAL");
                        break;
                    case SpeechPriority.PRIORITY_MAX:
                        MyLog.i(CLS_NAME, "requestPriority: PRIORITY_MAX");
                        break;
                }

                switch (currentPriority) {

                    case SpeechPriority.PRIORITY_LOW:
                        MyLog.i(CLS_NAME, "proceedPriority: PRIORITY_LOW");
                        break;
                    case SpeechPriority.PRIORITY_REMOTE:
                        MyLog.i(CLS_NAME, "proceedPriority: PRIORITY_REMOTE");
                        break;
                    case SpeechPriority.PRIORITY_NORMAL:
                        MyLog.i(CLS_NAME, "proceedPriority: PRIORITY_NORMAL");
                        break;
                    case SpeechPriority.PRIORITY_MAX:
                        MyLog.i(CLS_NAME, "proceedPriority: PRIORITY_MAX");
                        break;
                }
            }

            if (requestPriority < currentPriority) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "requestPriority < currentPriority ignoring");
                }
                return new Pair<>(new Pair<>(false, false), new Pair<>(isSpeakingPair.first, isListening));
            } else if (requestPriority == currentPriority) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "requestPriority == currentPriority proceeding");
                }
                return new Pair<>(new Pair<>(true, false), new Pair<>(isSpeakingPair.first, isListening));
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "requestPriority > currentPriority overriding");
                }
                return new Pair<>(new Pair<>(true, true), new Pair<>(isSpeakingPair.first, isListening));
            }

        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "proceedPriority: proceed as dormant");
            }
        }

        return new Pair<>(new Pair<>(true, false), new Pair<>(false, false));
    }

    /**
     * Remove any lingering interrupted parameters
     *
     * @param params the {@link SelfAwareParameters}
     */
    public void removeInterrupted(@NonNull final SelfAwareParameters params) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "removeInterrupted");
        }

        getBundle().remove(TTSDefaults.EXTRA_INTERRUPTED);
        params.remove(TTSDefaults.EXTRA_INTERRUPTED);
        params.remove(TTSDefaults.EXTRA_INTERRUPTED_FORCED);
    }

    /**
     * Called to stop speech
     */
    public void stopSpeech(@Nullable final SaiyTextToSpeech tts, @NonNull final SelfAwareParameters params,
                           @Nullable final EngineNuance en, final boolean preventRecognition) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "stopSpeech: preventRecognition: " + preventRecognition);
        }

        getBundle().putBoolean(TTSDefaults.EXTRA_INTERRUPTED, true);
        params.putObject(TTSDefaults.EXTRA_INTERRUPTED, true);

        if (preventRecognition) {
            params.putObject(TTSDefaults.EXTRA_INTERRUPTED_FORCED, true);
        }

        final String utteranceId = params.getUtteranceId();

        if (utteranceId.startsWith(SaiyTextToSpeech.ARRAY_FIRST)
                || utteranceId.startsWith(SaiyTextToSpeech.ARRAY_INTERIM)) {
            if (preventRecognition) {
                params.setUtteranceId(String.valueOf(LocalRequest.ACTION_SPEAK_ONLY));
            } else {
                params.setUtteranceId(TextUtils.split(utteranceId, SaiyTextToSpeech.ARRAY_DELIMITER)[1]);
            }
        }

        switch (getDefaultTTS()) {

            case LOCAL:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "stopSpeech: SpeechDefault.LOCAL");
                }

                if (tts != null) {
                    tts.stop();
                }
                break;
            case NETWORK_NUANCE:

                if (en != null) {

                    switch (TTS.getState()) {

                        case SPEAKING:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "stopSpeech: NUANCE: SPEAKING");
                            }
                            en.stopSpeech();
                            break;
                        case IDLE:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "stopSpeech: NUANCE: IDLE");
                            }
                            break;
                        default:
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "stopSpeech: Default STATE?");
                            }
                            break;
                    }
                    break;
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "stopSpeech: NUANCE null");
                    }
                }
                break;
            default:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "stopSpeech: Default PROVIDER?");
                }
                break;
        }
    }

    /**
     * Stop the recognition currently in use
     */
    public void stopListening(final RecognitionNuance recogNuance, final RecognitionGoogleCloud recogGoogle,
                              final RecognitionGoogleChromium recogGoogleChromium,
                              final RecognitionMicrosoft recogOxford, final RecognitionWit recogWit,
                              final RecognitionBluemix recogIBM, final RecognitionRemote recogRemote,
                              final RecognitionMic recogMic, final SpeechRecognizer recogNative,
                              final RecognitionSphinx recogSphinx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "stopListening");
        }

        getBundle().putBoolean(TTSDefaults.EXTRA_INTERRUPTED, true);

        if (recogSphinx != null && recogSphinx.isListening()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "stopListening: hotword");
            }

            recogSphinx.stopListening();
        } else {

            switch (getDefaultRecognition()) {

                case NUANCE:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "stopListening: " + SaiyDefaults.VR.NUANCE.name());
                    }

                    if (recogNuance != null) {

                        switch (Recognition.getState()) {

                            case PROCESSING:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "stopListening: PROCESSING");
                                }
                                recogNuance.cancelListening();
                                break;
                            case LISTENING:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "stopListening: SPEAKING");
                                }
                                recogNuance.stopListening();
                                break;
                        }
                        break;
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "stopListening null");
                        }
                    }
                    break;
                case GOOGLE_CLOUD:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "stopListening: " + SaiyDefaults.VR.GOOGLE_CLOUD.name());
                    }

                    if (recogGoogle != null) {

                        switch (Recognition.getState()) {

                            case PROCESSING:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "stopListening: PROCESSING");
                                }
                                recogGoogle.stopListening();
                                break;
                            case LISTENING:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "stopListening: SPEAKING");
                                }
                                recogGoogle.stopListening();
                                break;
                        }
                        break;
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "stopListening null");
                        }
                    }
                    break;
                case GOOGLE_CHROMIUM:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "stopListening: " + SaiyDefaults.VR.GOOGLE_CHROMIUM.name());
                    }

                    if (recogGoogleChromium != null) {

                        switch (Recognition.getState()) {

                            case PROCESSING:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "stopListening: PROCESSING");
                                }
                                recogGoogleChromium.stopListening();
                                break;
                            case LISTENING:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "stopListening: SPEAKING");
                                }
                                recogGoogleChromium.stopListening();
                                break;
                        }
                        break;
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "stopListening null");
                        }
                    }
                    break;
                case MICROSOFT:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "stopListening: " + SaiyDefaults.VR.MICROSOFT.name());
                    }

                    if (recogOxford != null) {

                        switch (Recognition.getState()) {

                            case PROCESSING:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "stopListening: PROCESSING");
                                }
                                recogOxford.stopListening();
                                break;
                            case LISTENING:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "stopListening: SPEAKING");
                                }
                                recogOxford.stopListening();
                                break;
                        }
                        break;
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "stopListening null");
                        }
                    }
                    break;
                case WIT:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "stopListening: " + SaiyDefaults.VR.WIT.name());
                    }

                    if (recogWit != null) {

                        switch (Recognition.getState()) {

                            case PROCESSING:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "stopListening: PROCESSING");
                                }
                                recogWit.stopListening();
                                break;
                            case LISTENING:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "stopListening: SPEAKING");
                                }
                                recogWit.stopListening();
                                break;
                        }
                        break;
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "stopListening null");
                        }
                    }
                    break;
                case IBM:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "stopListening: " + SaiyDefaults.VR.IBM.name());
                    }

                    if (recogIBM != null) {

                        switch (Recognition.getState()) {

                            case PROCESSING:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "stopListening: PROCESSING");
                                }
                                recogIBM.stopListening();
                                break;
                            case LISTENING:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "stopListening: SPEAKING");
                                }
                                recogIBM.stopListening();
                                break;
                        }
                        break;
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "stopListening null");
                        }
                    }
                    break;
                case NATIVE:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "stopListening: " + SaiyDefaults.VR.NATIVE.name());
                    }

                    if (recogNative != null) {

                        switch (Recognition.getState()) {

                            case PROCESSING:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "stopListening: PROCESSING");
                                }
                                recogNative.cancel();
                                Recognition.setState(Recognition.State.IDLE);
                                break;
                            case LISTENING:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "stopListening: LISTENING");
                                }
                                recogNative.stopListening();
                                Recognition.setState(Recognition.State.IDLE);
                                break;
                        }
                        break;
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "stopListening null");
                        }
                    }

                    break;
                case REMOTE:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "stopListening: " + SaiyDefaults.VR.REMOTE.name());
                    }

                    if (recogRemote != null) {

                        switch (Recognition.getState()) {

                            case PROCESSING:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "stopListening PROCESSING");
                                }
                                recogRemote.cancel();
                                Recognition.setState(Recognition.State.IDLE);
                                break;
                            case LISTENING:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "stopListening LISTENING");
                                }
                                recogRemote.stopListening();
                                Recognition.setState(Recognition.State.IDLE);
                                break;
                        }
                        break;
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "stopListening null");
                        }
                    }

                    break;
                case MIC:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "stopListening: " + SaiyDefaults.VR.MIC.name());
                    }

                    if (recogMic != null) {

                        switch (Recognition.getState()) {

                            case PROCESSING:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "stopListening PROCESSING");
                                }
                                recogMic.stopRecording();
                                Recognition.setState(Recognition.State.IDLE);
                                break;
                            case LISTENING:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "stopListening LISTENING");
                                }
                                recogMic.stopRecording();
                                Recognition.setState(Recognition.State.IDLE);
                                break;
                        }
                        break;
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "stopListening null");
                        }
                    }

                    break;
                default:
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "stopListening.default?");
                    }
                    break;
            }
        }
    }


    /**
     * Get the current {@link Callback}
     *
     * @return the current {@link Callback}
     */
    private Callback getCallback() {
        return callback;
    }

    /**
     * Disable this callback list.  All registered callbacks are unregistered,
     * and the list is disabled so that future calls to {@link RemoteCallbackList#register} will
     * fail.  This should only be used when the Service is stopping, to prevent clients
     * from registering callbacks after it is stopped.
     *
     * @see {@link RemoteCallbackList#register}
     */
    public void killCallbacks() {
        remoteCallbacks.kill();
    }

    /**
     * Check that the {@link RequestParcel} is valid, this includes the basic requirements of
     * a speech string and a request id. If it is not valid, an error is logged and the request ignored.
     * <p/>
     * Otherwise, the listener is added to the remoteCallbacks list.
     *
     * @param rl     the remote {@link ISaiyListener}
     * @param parcel the remote {@link RequestParcel}
     * @return true if the parcel is configured correctly
     */
    private boolean validateRemoteParcel(final ISaiyListener rl, final RequestParcel parcel) throws RemoteException {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "validateRemoteParcel");
        }

        if (Validation.validateParcel(mContext, parcel)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "validateRemoteBundle: validateParcel successful");
            }
            if (Validation.validateParams(mContext, parcel)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "validateRemoteBundle: validateParams successful");
                }

                addCallback(rl);

                if (DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    MyLog.v(CLS_NAME, "validateRemoteBundle: registered clients: "
                            + remoteCallbacks.getRegisteredCallbackCount());
                }

                return true;
            } else {
                MyLog.e("Remote Saiy Request", mContext.getString(R.string.error_request_parcel_params_invalid));
            }
        } else {
            MyLog.e("Remote Saiy Request", mContext.getString(R.string.error_request_parcel_invalid));
        }

        rl.onError(Defaults.ERROR.ERROR_DEVELOPER.name(), Validation.ID_UNKNOWN);
        return false;
    }

    /**
     * Add a callback to the {@link RemoteCallbackList}
     * <p/>
     * With the current setup, only one registered callback should be present at a time. Having
     * previously passed many configuration checks, if there are other callbacks present in the list,
     * they will be removed, leaving just this request, which will prevent a bottleneck under odd
     * circumstances.
     *
     * @param rl the remote {@link ISaiyListener}
     */
    private void addCallback(final ISaiyListener rl) throws RemoteException {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "addCallback");
        }

        synchronized (remoteCallbacks) {

            int count = remoteCallbacks.beginBroadcast();
            if (DEBUG) {
                MyLog.i(CLS_NAME, "addCallback: current size: " + count);
            }

            if (count > 0) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "addCallback: removing " + count + " objects");
                }
                while (count > 0) {
                    count--;
                    remoteCallbacks.unregister(remoteCallbacks.getBroadcastItem(count));
                }
            }

            remoteCallbacks.register(rl);
            remoteCallbacks.finishBroadcast();
        }
    }

    /**
     * An event has occurred that may require a callback to a remote listener. Check the existence
     * of such and remove the callback as done if required.
     *
     * @param callbackType the {@link CallbackType} to handle
     * @param results      {@link Bundle} containing the recognition results
     */
    public void manageCallback(int callbackType, final Bundle results) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "manageCallback");
        }

        synchronized (remoteCallbacks) {

            int count = remoteCallbacks.beginBroadcast();
            if (DEBUG) {
                MyLog.i(CLS_NAME, "manageCallback: size: " + count);
            }

            if (count > 0) {

                try {

                    if (isInterrupted()) {
                        callbackType = CallbackType.CB_INTERRUPTED;
                    }

                    switch (callbackType) {

                        case CallbackType.CB_UTTERANCE_COMPLETED:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "manageCallback: CallbackType.CB_UTTERANCE_COMPLETED");
                            }

                            remoteCallbacks.getBroadcastItem(0).onUtteranceCompleted(
                                    callback.getParcel().getRequestId());

                            switch (callback.getParcel().getAction()) {

                                case SPEAK_ONLY:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "manageCallback: ACTION.SPEAK_ONLY - removing");
                                    }
                                    remoteCallbacks.unregister(remoteCallbacks.getBroadcastItem(0));
                                    break;
                                case SPEAK_LISTEN:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "manageCallback: ACTION.SPEAK_LISTEN - persisting");
                                    }
                                    break;
                                default:
                                    if (DEBUG) {
                                        MyLog.w(CLS_NAME, "manageCallback: ACTION.UNKNOWN - removing");
                                    }
                                    remoteCallbacks.unregister(remoteCallbacks.getBroadcastItem(0));
                                    break;
                            }

                            break;

                        case CallbackType.CB_RESULTS_RECOGNITION:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "manageCallback: CallbackType.CB_RESULTS_RECOGNITION");
                            }

                            remoteCallbacks.getBroadcastItem(0).onSpeechResults(results,
                                    callback.getParcel().getRequestId());
                            remoteCallbacks.unregister(remoteCallbacks.getBroadcastItem(0));
                            break;

                        case CallbackType.CB_ERROR_NO_MATCH:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "manageCallback: CallbackType.CB_ERROR_NO_MATCH");
                            }

                            remoteCallbacks.getBroadcastItem(0).onError(Defaults.ERROR.ERROR_NO_MATCH.name(),
                                    callback.getParcel().getRequestId());
                            remoteCallbacks.unregister(remoteCallbacks.getBroadcastItem(0));
                            break;

                        case CallbackType.CB_ERROR_BUSY:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "manageCallback: CallbackType.CB_ERROR_BUSY");
                            }

                            remoteCallbacks.getBroadcastItem(0).onError(Defaults.ERROR.ERROR_BUSY.name(),
                                    callback.getParcel().getRequestId());
                            remoteCallbacks.unregister(remoteCallbacks.getBroadcastItem(0));
                            break;

                        case CallbackType.CB_ERROR_NETWORK:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "manageCallback: CallbackType.CB_ERROR_NETWORK");
                            }

                            remoteCallbacks.getBroadcastItem(0).onError(Defaults.ERROR.ERROR_NETWORK.name(),
                                    callback.getParcel().getRequestId());
                            remoteCallbacks.unregister(remoteCallbacks.getBroadcastItem(0));
                            break;

                        case CallbackType.CB_ERROR_DENIED:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "manageCallback: CallbackType.CB_ERROR_DENIED");
                            }

                            remoteCallbacks.getBroadcastItem(0).onError(Defaults.ERROR.ERROR_DENIED.name(),
                                    callback.getParcel().getRequestId());
                            remoteCallbacks.unregister(remoteCallbacks.getBroadcastItem(0));
                            break;

                        case CallbackType.CB_ERROR_SAIY:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "manageCallback: CallbackType.CB_ERROR_GENERIC");
                            }

                            remoteCallbacks.getBroadcastItem(0).onError(Defaults.ERROR.ERROR_SAIY.name(),
                                    callback.getParcel().getRequestId());
                            remoteCallbacks.unregister(remoteCallbacks.getBroadcastItem(0));
                            break;

                        case CallbackType.CB_INTERRUPTED:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "manageCallback: CallbackType.CB_INTERRUPTED");
                            }

                            remoteCallbacks.getBroadcastItem(0).onError(Defaults.ERROR.ERROR_INTERRUPTED.name(),
                                    callback.getParcel().getRequestId());
                            remoteCallbacks.unregister(remoteCallbacks.getBroadcastItem(0));
                            break;

                        case CallbackType.CB_ERROR_DEVELOPER:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "manageCallback: CallbackType.CB_ERROR_DEVELOPER");
                            }

                            remoteCallbacks.getBroadcastItem(0).onError(Defaults.ERROR.ERROR_DEVELOPER.name(),
                                    callback.getParcel().getRequestId());
                            remoteCallbacks.unregister(remoteCallbacks.getBroadcastItem(0));
                            break;

                        case CallbackType.CB_UNKNOWN:
                        default:
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "manageCallback: CallbackType.CB_UNKNOWN");
                            }

                            remoteCallbacks.getBroadcastItem(0).onError(Defaults.ERROR.ERROR_UNKNOWN.name(),
                                    callback.getParcel().getRequestId());
                            remoteCallbacks.unregister(remoteCallbacks.getBroadcastItem(0));
                            break;
                    }
                } catch (final RemoteException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "manageCallback: RemoteException");
                        e.printStackTrace();
                    }
                } catch (final NullPointerException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "manageCallback: NullPointerException");
                        e.printStackTrace();
                    }
                } catch (final IndexOutOfBoundsException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "manageCallback: IndexOutOfBoundsException");
                        e.printStackTrace();
                    }
                } catch (final Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "manageCallback: Exception");
                        e.printStackTrace();
                    }
                } finally {
                    System.gc();
                }
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "manageCallback: none registered");
                }
            }

            remoteCallbacks.finishBroadcast();
        }
    }

    /**
     * Check if the current recognition session detected the {@link CC#COMMAND_CANCEL}
     *
     * @return true if the user requested to cancel
     */
    public boolean isCancelled() {
        return isCancelled;
    }

    /**
     * Reset the persistent {@link #isCancelled} at the start of each speech session.
     */
    private void resetCancelled() {
        isCancelled = false;
    }

    /**
     * If we detect a {@link CC#COMMAND_CANCEL} in the voice data,
     * it may have been in the unstable results and therefore not always appear in the actual results.
     * Making the (possibly incorrect) assumption that the unstable results were correct, we need
     * to avoid Saiy not responding correctly and so we directly initiate a cancel response, without
     * using the resource of going via {@link ai.saiy.android.processing.Quantum}
     */
    public void setCancelled() {
        isCancelled = true;
    }

    /**
     * Get the default recognition provider, either from the remote request or the user preferences.
     *
     * @return the default provider or ordinal equivalent.
     */
    public SaiyDefaults.VR getDefaultRecognition() {
        if (servingRemote()) {
            return SaiyDefaults.VR.remoteToLocal(callback.getParcel().getProviderVR());
        } else {

            switch (getBundle().getInt(LocalRequest.EXTRA_CONDITION, Condition.CONDITION_NONE)) {

                case Condition.CONDITION_EMOTION:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "getDefaultRecognition: CONDITION_EMOTION");
                    }
                    return SaiyDefaults.VR.MIC;
                case Condition.CONDITION_IDENTITY:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "getDefaultRecognition: CONDITION_IDENTITY");
                    }
                    return SaiyDefaults.VR.MIC;
                case Condition.CONDITION_IDENTIFY:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "getDefaultRecognition: CONDITION_IDENTIFY");
                    }
                    return SaiyDefaults.VR.MIC;
                case Condition.CONDITION_NONE:
                default:
                    break;
            }

            return SPH.getDefaultRecognition(mContext);
        }
    }

    /**
     * Get the default language model, either from the remote request or the user preferences.
     *
     * @return the default language model or ordinal equivalent.
     */
    private SaiyDefaults.LanguageModel getDefaultLanguageModel() {
        if (servingRemote()) {
            return SaiyDefaults.LanguageModel.remoteToLocal(callback.getParcel().getLanguageModel());
        } else {
            return SPH.getDefaultLanguageModel(mContext);
        }
    }

    /**
     * Get the default language model, either from the remote request or the user preferences.
     *
     * @param servingRemote if Saiy is currently serving a remote request
     * @return the default language model or ordinal equivalent.
     */
    public SaiyDefaults.LanguageModel getDefaultLanguageModel(final boolean servingRemote) {
        if (servingRemote) {
            return SaiyDefaults.LanguageModel.remoteToLocal(callback.getParcel().getLanguageModel());
        } else {
            return SPH.getDefaultLanguageModel(mContext);
        }
    }

    /**
     * Get the default TTS provider, either from the remote request or the user preferences.
     *
     * @return the default provider or ordinal equivalent.
     */
    public SaiyDefaults.TTS getDefaultTTS() {
        if (servingRemote()) {
            return SaiyDefaults.TTS.remoteToLocal(callback.getParcel().getProviderTTS());
        } else {
            return SPH.getDefaultTTS(mContext);
        }
    }

    /**
     * Check if a network connection of any speed is available to use
     *
     * @return true if a connection is available
     */
    public boolean isNetworkAvailable() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "isNetworkAvailable: " + Network.isNetworkAvailable(mContext));
        }

        return Network.isNetworkAvailable(mContext);
    }

    /**
     * Called from {@link SelfAware#onStartCommand(Intent, int, int)} we need to decide if the Text
     * to Speech Engine should be initialised, so to reduce any lag when a request to speak comes in.
     * <p/>
     * If this is a local request we'll check the user setting in the shared preferences to see if
     * they have decided against this.
     *
     * @param tts    the {@link SaiyTextToSpeech}
     * @param intent received in {@link SelfAware#onStartCommand(Intent, int, int)}
     * @return true if the engine should be initialised.
     */
    public boolean shouldWarmUp(final SaiyTextToSpeech tts, final Intent intent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "shouldWarmUp");
        }

        if (tts == null) {
            if (intent != null) {

                final String callingPackage = getPackage(intent);

                if (UtilsString.notNaked(callingPackage) && callingPackage.matches(mContext.getPackageName())) {

                    switch (getDefaultTTS()) {

                        case LOCAL:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "onStartCommand: LOCAL");
                            }
                            return true;
                        case NETWORK_NUANCE:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "onStartCommand: NETWORK_NUANCE");
                            }
                            break;
                    }
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onStartCommand: Not a local request. No warm up.");
                    }
                }
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onStartCommand: Intent is null. No warm up.");
                }
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onStartCommand: TTS bound. No warm up.");
            }
        }

        return false;
    }

    /**
     * Set the language of the Voice Engine if required
     *
     * @param tts the {@link SaiyTextToSpeech} instance
     */
    public void setVoice(@NonNull final SaiyTextToSpeech tts, @NonNull final SelfAwareParameters params) {

        final Locale loc;

        if (servingRemote()) {

            switch (callback.getParcel().getProviderTTS()) {

                case LOCAL:
                    loc = getCallback().getParcel().getTTSLanguageLocal().getLocale();
                    break;
                default:
                    loc = getTTSLocale();
                    break;
            }
        } else {
            loc = getTTSLocale();
        }

        tts.setVoice(loc.getLanguage(), loc.getCountry(), this, params);
    }

    /**
     * Get the {@link Locale} of the Text to Speech request
     *
     * @param servingRemote if Saiy is currently serving a remote request
     * @return the Text to Speech request language
     */
    public Locale getTTSLocale(final boolean servingRemote) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getTTSLocale: serving remote: " + servingRemote);
        }

        if (servingRemote) {

            switch (callback.getParcel().getProviderTTS()) {

                case LOCAL:
                    return getCallback().getParcel().getTTSLanguageLocal().getLocale();
                case NETWORK_NUANCE:
                    return UtilsLocale.stringToLocale(getCallback().getParcel().getTTSLanguageNuance().getLocaleString());
                default:
                    return getTTSLocale();
            }
        }

        return getTTSLocale();

    }

    /**
     * Get the {@link Locale} of the Text to Speech request
     *
     * @return the Text to Speech request language
     */
    private Locale getTTSLocale() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getTTSLocale");
        }

        if (getBundle().containsKey(LocalRequest.EXTRA_TTS_LANGUAGE)) {

            final String localeString = getBundle().getString(LocalRequest.EXTRA_TTS_LANGUAGE);

            if (UtilsString.notNaked(localeString)) {
                return UtilsLocale.stringToLocale(localeString);
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getTTSLocale: no value for EXTRA_TTS_LANGUAGE");
                }
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getTTSLocale: no extra for EXTRA_TTS_LANGUAGE");
            }
        }

        return SPH.getTTSLocale(mContext);
    }

    /**
     * Add the recognition results to the existing {@link Bundle}
     *
     * @param results recognition results {@link Bundle}
     */
    public void putResults(@NonNull final Bundle results) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "putResults");
        }

        final ArrayList<String> heardVoice = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        final float[] confidence = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);

        if (heardVoice != null) {
            getBundle().putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, heardVoice);
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "putResults: speech empty");
            }
            getBundle().putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, new ArrayList<String>());
        }

        if (confidence != null) {
            getBundle().putFloatArray(SpeechRecognizer.CONFIDENCE_SCORES, confidence);
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "putResults: confidence empty");
            }
            getBundle().putFloatArray(SpeechRecognizer.CONFIDENCE_SCORES, new float[0]);
        }
    }

    /**
     * Get the Text to Speech queue type
     *
     * @return one of {@link TextToSpeech#QUEUE_ADD} or {@link TextToSpeech#QUEUE_FLUSH}
     */
    public int getQueueType() {
        return getBundle().getInt(LocalRequest.EXTRA_QUEUE_TYPE, TextToSpeech.QUEUE_FLUSH);
    }

    /**
     * Check if the queue type is {@link TextToSpeech#QUEUE_ADD}
     *
     * @param bundle the parameters
     * @return true if the queue type is {@link TextToSpeech#QUEUE_ADD}. False otherwise
     */
    public boolean isQueueAdd(final Bundle bundle) {
        return bundle != null && bundle.getInt(LocalRequest.EXTRA_QUEUE_TYPE,
                TextToSpeech.QUEUE_FLUSH) == TextToSpeech.QUEUE_ADD;
    }

    /**
     * Get the {@link Locale} of the Voice Recognition request
     *
     * @return the Voice Recognition request language
     */
    public Locale getVRLocale(final boolean servingRemote) {

        if (servingRemote) {

            switch (callback.getParcel().getProviderVR()) {

                case GOOGLE_CHROMIUM:
                case GOOGLE_CLOUD:
                    return UtilsLocale.stringToLocale(getCallback().getParcel().getVRLanguageGoogle().getLocaleString());
                case NUANCE:
                    return UtilsLocale.stringToLocale(getCallback().getParcel().getVRLanguageNuance().getLocaleString());
                case MICROSOFT:
                    return UtilsLocale.stringToLocale(getCallback().getParcel().getVRLanguageMicrosoft().getLocaleString());
                case REMOTE:
                    return getCallback().getParcel().getVRLanguageRemote().getLocale();
                case NATIVE:
                    return getCallback().getParcel().getVRLanguageNative().getLocale();
                case IBM:
                    return UtilsLocale.stringToLocale(getCallback().getParcel().getVRLanguageIBM().getLocaleString());
                case WIT:
                    return UtilsLocale.stringToLocale(getCallback().getParcel().getVRLanguageWit().getLocaleString());
                default:
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "getVRLocale: unknown VRProvider");
                    }
                    return getVRLocale();
            }
        }

        return getVRLocale();
    }

    /**
     * Get the {@link Locale} of the Voice Recognition request
     *
     * @return the Voice Recognition request language
     */
    private Locale getVRLocale() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getVRLocale");
        }

        if (getBundle().containsKey(LocalRequest.EXTRA_RECOGNITION_LANGUAGE)) {

            final String localeString = getBundle().getString(LocalRequest.EXTRA_RECOGNITION_LANGUAGE);

            if (UtilsString.notNaked(localeString)) {
                return UtilsLocale.stringToLocale(localeString);
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getVRLocale: no value for EXTRA_RECOGNITION_LANGUAGE");
                }
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getVRLocale: no extra for EXTRA_RECOGNITION_LANGUAGE");
            }
        }

        return SPH.getVRLocale(mContext);
    }

    /**
     * Get the supported language of the {@link Locale} which will enable us to apply any resource
     * localisation when resolving a command.
     *
     * @param servingRemote true if the request is remote
     * @return the {@link SupportedLanguage}
     */
    public SupportedLanguage getSupportedLanguage(final boolean servingRemote) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getSupportedLanguage");
        }

        SupportedLanguage sl = (SupportedLanguage) getBundle().getSerializable(LocalRequest.EXTRA_SUPPORTED_LANGUAGE);

        if (sl != null) {
            return sl;
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getSupportedLanguage: adding to bundle");
            }

            sl = SupportedLanguage.getSupportedLanguage(getVRLocale(servingRemote));
            getBundle().putSerializable(LocalRequest.EXTRA_SUPPORTED_LANGUAGE, sl);
            return sl;
        }
    }

    /**
     * Utility method to construct the {@link EngineNuance} instance
     *
     * @param params           the {@link SelfAwareParameters}
     * @param progressListener the {@link SaiyProgressListener}
     * @return the {@link EngineNuance} instance
     */
    public EngineNuance getEngineNuance(@NonNull final SelfAwareParameters params,
                                        @NonNull final SaiyProgressListener progressListener) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getEngineNuance");
        }

        if (servingRemote()) {
            return new EngineNuance(mContext, progressListener,
                    getCallback().getParcel().getTTSLanguageNuance().getLocaleString(),
                    getCallback().getParcel().getTTSLanguageNuance().getName(),
                    params.getUtteranceId(), getCallback().getParcel().getNUANCE_SERVER_URI(),
                    getCallback().getParcel().getNUANCE_APP_KEY());
        } else {

            final TTSLanguageNuance ttsLanguageNuance = TTSLanguageNuance.getVoice(getTTSLocale(),
                    SPH.getDefaultTTSGender(mContext).getRemoteGender());

            return new EngineNuance(mContext, progressListener,
                    ttsLanguageNuance.getLocaleString(),
                    ttsLanguageNuance.getName(),
                    params.getUtteranceId(), getNuanceUri(true),
                    NuanceConfiguration.APP_KEY);
        }
    }

    /**
     * Utility method to construct the {@link RecognitionNuance} instance
     *
     * @param recognitionListener the {@link RecognitionListener}
     * @return the {@link RecognitionNuance} instance
     */
    public RecognitionNuance getNuanceRecognition(@NonNull final SaiyRecognitionListener recognitionListener) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getNuanceRecognition");
        }

        if (servingRemote()) {
            return new RecognitionNuance(mContext, recognitionListener,
                    DetectionType.Long,
                    getCallback().getParcel().getNUANCE_SERVER_URI(),
                    getCallback().getParcel().getNUANCE_SERVER_URI_NLU(),
                    getCallback().getParcel().getNUANCE_APP_KEY(),
                    getCallback().getParcel().getNUANCE_CONTEXT_TAG(), true,
                    SaiyDefaults.LanguageModel.remoteToLocal(getCallback().getParcel().getLanguageModel()),
                    getTTSLocale(true),
                    getCallback().getParcel().getVRLanguageNuance(),
                    getSupportedLanguage(true));
        } else {
            return new RecognitionNuance(mContext, recognitionListener,
                    DetectionType.Long, getNuanceUri(false),
                    getNuanceUri(false),
                    NuanceConfiguration.APP_KEY, NuanceConfiguration.CONTEXT_TAG,
                    false, getDefaultLanguageModel(), getTTSLocale(),
                    VRLanguageNuance.getLanguage(getVRLocale()),
                    getSupportedLanguage(false));
        }
    }

    /**
     * Utility method to construct the {@link RecognitionGoogleCloud} instance
     *
     * @param recognitionListener the {@link RecognitionListener}
     * @return the {@link RecognitionGoogleCloud} instance
     */
    public RecognitionGoogleCloud getGoogleCloudRecognition(@NonNull final RecognitionMic recogMic,
                                                            @NonNull final SaiyRecognitionListener recognitionListener) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getGoogleCloudRecognition");
        }

        if (servingRemote()) {

            return new RecognitionGoogleCloud(mContext, recognitionListener,
                    getCallback().getParcel().getVRLanguageGoogle(),
                    new AccessToken(getCallback().getParcel().getGOOGLE_CLOUD_ACCESS_TOKEN(),
                            new Date(System.currentTimeMillis()
                                    + getCallback().getParcel().getGOOGLE_CLOUD_ACCESS_EXPIRY())),
                    recogMic);
        } else {
            return new RecognitionGoogleCloud(mContext, recognitionListener,
                    VRLanguageGoogle.getLanguage(getVRLocale()), GoogleConfiguration.ACCESS_TOKEN, recogMic);
        }
    }

    /**
     * Utility method to construct the {@link RecognitionGoogleChromium} instance
     *
     * @param recognitionListener the {@link RecognitionListener}
     * @return the {@link RecognitionGoogleChromium} instance
     */
    public RecognitionGoogleChromium getGoogleChromiumRecognition(@NonNull final SaiyRecognitionListener
                                                                          recognitionListener) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getGoogleChromiumRecognition");
        }

        if (servingRemote()) {

            return new RecognitionGoogleChromium(recognitionListener,
                    getCallback().getParcel().getVRLanguageGoogle(),
                    getCallback().getParcel().getGOOGLE_CHROMIUM_API_KEY(), true, saiySoundPool);

        } else {

            return new RecognitionGoogleChromium(recognitionListener,
                    VRLanguageGoogle.getLanguage(getVRLocale()),
                    GoogleConfiguration.GOOGLE_SPEECH_API_KEY, true, saiySoundPool);
        }
    }

    /**
     * Utility method to construct the {@link RecognitionMicrosoft} instance
     *
     * @param recognitionListener the {@link RecognitionListener}
     * @return the {@link RecognitionMicrosoft} instance
     */
    public RecognitionMicrosoft getMicrosoftRecognition(@NonNull final SaiyRecognitionListener recognitionListener) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getMicrosoftRecognition");
        }

        if (servingRemote()) {
            return new RecognitionMicrosoft(mContext, recognitionListener,
                    getCallback().getParcel().getOXFORD_KEY_1(),
                    getCallback().getParcel().getOXFORD_KEY_2(),
                    getCallback().getParcel().getLUIS_APP_ID(),
                    getCallback().getParcel().getLUIS_SUBSCRIPTION_ID(),
                    SaiyDefaults.LanguageModel.remoteToLocal(getCallback().getParcel().getLanguageModel()),
                    getTTSLocale(true),
                    getCallback().getParcel().getVRLanguageMicrosoft(),
                    getCallback().getParcel().getNLULanguageMicrosoft(),
                    getSupportedLanguage(true), true, saiySoundPool);
        } else {
            return new RecognitionMicrosoft(mContext, recognitionListener,
                    MicrosoftConfiguration.OXFORD_KEY_1,
                    MicrosoftConfiguration.OXFORD_KEY_2, MicrosoftConfiguration.LUIS_APP_ID,
                    MicrosoftConfiguration.LUIS_SUBSCRIPTION_ID, getDefaultLanguageModel(),
                    getTTSLocale(),
                    VRLanguageMicrosoft.getLanguage(getVRLocale()),
                    NLULanguageMicrosoft.getLanguage(getVRLocale()),
                    getSupportedLanguage(false), false, saiySoundPool);
        }
    }

    /**
     * Utility method to construct the {@link RemoteAPIAI} instance
     *
     * @return the {@link Pair} with the first parameter denoting success and the second the JSON response
     */
    @SuppressWarnings("ConstantConditions")
    public Pair<Boolean, String> getAPIAIRemote(@NonNull final Bundle results) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getAPIAIRemote");
        }

        return new RemoteAPIAI(mContext, results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0),
                getCallback().getParcel().getAPI_AI_CLIENT_ACCESS_TOKEN(),
                getCallback().getParcel().getNLULanguageAPIAI()).fetch();
    }

    /**
     * Utility method to construct the {@link RecognitionWit} instance
     *
     * @param recognitionListener the {@link SaiyRecognitionListener}
     * @return the {@link RecognitionWit} instance
     */
    public RecognitionWit getWitRecognition(@NonNull final SaiyRecognitionListener recognitionListener) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getWitRecognition");
        }

        if (servingRemote()) {
            return new RecognitionWit(mContext, recognitionListener,
                    getCallback().getParcel().getWIT_SERVER_ACCESS_TOKEN(),
                    SaiyDefaults.LanguageModel.remoteToLocal(getCallback().getParcel().getLanguageModel()),
                    getTTSLocale(true),
                    getCallback().getParcel().getVRLanguageWit(),
                    getSupportedLanguage(true), true, saiySoundPool);
        } else {
            return new RecognitionWit(mContext, recognitionListener,
                    WitConfiguration.WIT_ACCESS_TOKEN,
                    getDefaultLanguageModel(),
                    getTTSLocale(),
                    VRLanguageWit.getLanguage(getVRLocale()),
                    getSupportedLanguage(false), false, saiySoundPool);
        }
    }

    /**
     * Utility method to construct the {@link RecognitionBluemix} instance
     *
     * @param recogMic            the {@link RecognitionMic}
     * @param recognitionListener the {@link SaiyRecognitionListener}
     * @return the {@link RecognitionBluemix} instance
     */
    public RecognitionBluemix getIBMRecognition(@NonNull final RecognitionMic recogMic,
                                                @NonNull final SaiyRecognitionListener recognitionListener) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getIBMRecognition");
        }

        if (servingRemote()) {
            return new RecognitionBluemix(recognitionListener,
                    getCallback().getParcel().getIBM_SERVICE_USER_NAME(),
                    getCallback().getParcel().getIBM_SERVICE_PASSWORD(),
                    SaiyDefaults.LanguageModel.remoteToLocal(getCallback().getParcel().getLanguageModel()),
                    getTTSLocale(true),
                    getCallback().getParcel().getVRLanguageIBM(),
                    getSupportedLanguage(true), true, recogMic);
        } else {
            return new RecognitionBluemix(recognitionListener,
                    BluemixConfiguration.BLUEMIX_USERNAME,
                    BluemixConfiguration.BLUEMIX_PASSWORD,
                    getDefaultLanguageModel(), getTTSLocale(),
                    VRLanguageIBM.getLanguage(getVRLocale()),
                    getSupportedLanguage(false), false, recogMic);
        }
    }

    /**
     * Utility method to construct the {@link RecognitionRemote} instance
     *
     * @param recognitionListener the {@link SaiyRecognitionListener}
     * @return the {@link RecognitionRemote} instance
     */
    public RecognitionRemote getRemoteRecognition(@NonNull final SaiyRecognitionListener recognitionListener) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getRemoteRecognition");
        }

        if (servingRemote()) {
            return new RecognitionRemote(recognitionListener, getVRLocale().toString(),
                    getCallback().getParcel().getREMOTE_SERVER_URI(),
                    getCallback().getParcel().getREMOTE_ACCESS_TOKEN(), saiySoundPool);
        } else {
            return null;
        }
    }

    /**
     * Utility method to construct the {@link RecognitionSphinx} instance
     *
     * @param listener the {@link SaiyHotwordListener}
     * @return the {@link RecognitionSphinx} instance
     */
    public RecognitionSphinx getSphinxRecognition(@NonNull final SaiyHotwordListener listener) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getSphinxRecognition");
        }
        return new RecognitionSphinx(mContext, listener, getSupportedLanguage(false));
    }

    /**
     * Utility method to construct the {@link RecognitionMic} instance
     *
     * @param recognitionListener the {@link SaiyRecognitionListener}
     * @param audioParameters     the defined {@link AudioParameters}
     * @param pauseDetection      where or not to use pause detection
     * @param pauseIgnoreTime     the delay until the pause detection will begin
     * @param enhance             whether or not to use the Android mic enhancements
     * @param writeToFile         where or not to write the audio to a file
     * @return he {@link RecognitionMic} instance
     */
    public RecognitionMic getMicRecognition(@Nullable final SaiyRecognitionListener recognitionListener,
                                            @NonNull final AudioParameters audioParameters,
                                            final boolean pauseDetection, final long pauseIgnoreTime,
                                            final boolean enhance, final boolean writeToFile) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getMicRecognition");
        }

        return new RecognitionMic(mContext, recognitionListener, audioParameters, pauseDetection, pauseIgnoreTime,
                enhance, writeToFile, saiySoundPool);
    }

    /**
     * Utility method to construct the {@link SpeechRecognizer} instance
     *
     * @param recognitionListener the {@link SaiyRecognitionListener}
     * @return the {@link Pair} containing the {@link SpeechRecognizer} and Intent with extras
     */
    public Pair<SpeechRecognizer, Intent> getNativeRecognition(
            @NonNull final SaiyRecognitionListener recognitionListener) {

        final long then = System.nanoTime();

        SpeechRecognizer recognizer = null;

        final List<ResolveInfo> recognitionServices = mContext.getPackageManager().queryIntentServices(
                new Intent(RecognitionService.SERVICE_INTERFACE), 0);

        if (UtilsList.notNaked(recognitionServices)) {

            String packageName;
            String serviceName;
            ServiceInfo serviceInfo;

            for (final ResolveInfo info : recognitionServices) {

                serviceInfo = info.serviceInfo;
                packageName = serviceInfo.packageName;
                serviceName = serviceInfo.name;

                if (packageName != null && serviceName != null) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "getNativeRecognition: Recognizer: " + packageName + " : " + serviceName);
                    }

                    if (packageName.startsWith(PACKAGE_NAME_GOOGLE)) {
                        recognizer = SpeechRecognizer.createSpeechRecognizer(mContext,
                                new ComponentName(packageName, serviceName));
                        break;
                    }
                }
            }

        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getNativeRecognition: recognitionServices: naked");
            }

            return null;
        }

        if (recognizer == null) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getNativeRecognition: recognizer: null");
            }

            return null;
        }

        recognizer.setRecognitionListener(recognitionListener);

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, "getNativeRecognition", then);
        }

        return new Pair<>(recognizer, getNativeIntent());
    }

    /**
     * Add the intent extras
     *
     * @return the {@link Intent} with the required extras added
     */
    public Intent getNativeIntent() {

        final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, mContext.getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, mContext.getString(R.string.app_name));
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, RecognitionNative.MAX_RESULTS);
        intent.putExtra(RecognitionDefaults.PREFER_OFFLINE, SPH.getUseOffline(mContext));
        intent.putExtra(RecognitionDefaults.EXTRA_SECURE,
                (getBundle().getInt(LocalRequest.EXTRA_CONDITION, Condition.CONDITION_NONE) == Condition.CONDITION_SECURE));

        final Long timeout = SPH.getPauseTimeout(mContext);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, timeout);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, timeout);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, timeout);

        if (servingRemote()) {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, getCallback().getParcel()
                    .getVRLanguageGoogle().getLocaleString());
        } else {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, getVRLocale().toString());
        }

        return intent;
    }

    /**
     * Get the enrolled profile id
     *
     * @return the profile id or an empty string is one is not set
     */
    public String getIdentityProfile() {
        return getBundle().getString(LocalRequest.EXTRA_PROFILE_ID, "");
    }

    /**
     * Get the utterance to speak, either from the remote request or the {@link Bundle} of instructions
     *
     * @return the utterance to speak
     */
    public String getUtterance() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getInput");
        }

        if (servingRemote()) {
            return getCallback().getParcel().getUtterance();
        } else {
            return getBundle().getString(LocalRequest.EXTRA_UTTERANCE, SaiyRequestParams.SILENCE);
        }
    }

    /**
     * Set whether the hotword detection should be shutdown
     *
     * @param shutdown true if the hotword should be permanently shutdown, false otherwise
     */
    public void setHotwordShutdown(@Nullable final RecognitionSphinx recogSphinx, final boolean shutdown) {
        restartHotword = !shutdown && (restartHotword || (recogSphinx != null && recogSphinx.isListening()));

        if (DEBUG) {
            MyLog.i(CLS_NAME, "setHotwordShutdown: restartHotword " + restartHotword);
        }

        releaseWakeLock();
    }

    /**
     * Check if the hotword detection should restart
     *
     * @return true if the detection should restart, false otherwise
     */
    public boolean restartHotword() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "restartHotword: " + restartHotword);
        }
        return restartHotword;
    }

    /**
     * Check if no utterance is required
     *
     * @return if the utterance String matches {@link SaiyRequestParams#SILENCE}
     */
    public boolean isSilentUtterance() {
        if (servingRemote()) {
            return getCallback().getParcel().getUtterance().equalsIgnoreCase(SaiyRequestParams.SILENCE);
        } else {
            return getBundle().getString(LocalRequest.EXTRA_UTTERANCE, SaiyRequestParams.SILENCE)
                    .equalsIgnoreCase(SaiyRequestParams.SILENCE);
        }
    }

    /**
     * Check if this was a deliberately secure request
     *
     * @return true if the request was secure or the device is in secure mode, false otherwise
     */
    public boolean isSecure() {
        return getBundle().getBoolean(RecognizerIntent.EXTRA_SECURE, false)
                || UtilsDevice.isDeviceLocked(mContext);
    }

    /**
     * Set if this request should be handled securely
     */
    private void setSecure() {
        getBundle().putBoolean(RecognizerIntent.EXTRA_SECURE, isSecure());
    }

    /**
     * Get the instruction {@link Bundle}
     *
     * @return the instruction {@link Bundle}
     */
    public Bundle getBundle() {
        if (this.bundle != null) {
            return this.bundle;
        }

        if (DEBUG) {
            MyLog.w(CLS_NAME, "getBundle: null");
        }

        return new Bundle();
    }

    /**
     * Helper method to set the instruction bundle and pass the parameter on to
     * {@link SelfAwareHelper#checkTTSConditions(SaiyTextToSpeech, SaiyDefaults.TTS)} for further inspection.
     *
     * @param tts        the {@link SaiyTextToSpeech} object
     * @param defaultTTS the default provider
     * @param bundle     of instructions
     * @return true if all conditions are met.
     */
    public boolean checkConditions(final SaiyTextToSpeech tts, final SaiyDefaults.TTS defaultTTS, final Bundle bundle) {
        this.bundle = bundle;
        setSecure();
        handleMemory();
        return checkTTSConditions(tts, defaultTTS);
    }

    public int getCondition() {
        return getBundle().getInt(LocalRequest.EXTRA_CONDITION, Condition.CONDITION_NONE);
    }

    /**
     * Check to see if we need to remember this command
     */
    private void handleMemory() {
        if (!servingRemote()
                && getBundle().getInt(LocalRequest.EXTRA_CONDITION, Condition.CONDITION_NONE)
                != Condition.CONDITION_IGNORE
                && getBundle().getInt(LocalRequest.EXTRA_ACTION, LocalRequest.ACTION_UNKNOWN)
                == LocalRequest.ACTION_SPEAK_ONLY
                && getBundle().getSerializable(LocalRequest.EXTRA_COMMAND) != null) {
            new MemoryPrepare(mContext, getBundle()).save();
        }
    }

    @Override
    public boolean checkTTSConditions(final SaiyTextToSpeech tts, final SaiyDefaults.TTS defaultTTS) {
        return super.checkTTSConditions(tts, defaultTTS);
    }

    @Override
    public Uri getNuanceUri(final boolean isTTS) {
        return super.getNuanceUri(isTTS);
    }

    @Override
    public String getPackage(final Intent intent) {
        return super.getPackage(intent);
    }

    @Override
    public int getSpeechLength(final String words) {
        return super.getSpeechLength(words);
    }

    @Override
    public boolean isInterrupted() {
        return super.isInterrupted();
    }

    @Override
    public void manageRemoteBusy(final ISaiyListener rl, final Bundle bundle) throws RemoteException {
        super.manageRemoteBusy(rl, bundle);
    }

    public Pair<Boolean, Boolean> shouldBind(final Intent intent) {
        return super.shouldBind(intent, blackListHelper.fetch(this.mContext));
    }

    @Override
    public Pair<Boolean, Boolean> shouldBind(final Intent intent, final ArrayList<String> blacklistArray) {
        return super.shouldBind(intent, blacklistArray);
    }

    /**
     * We need to include the array list of blacklisted applications before requesting the response
     * from {@link SelfAwareHelper#shouldRebind(Intent, ArrayList)}
     *
     * @param intent included in the remote request
     * @return true if the service should allow the rebind.
     */
    public boolean shouldRebind(final Intent intent) {
        return super.shouldRebind(intent, blackListHelper.fetch(this.mContext));
    }

    @Override
    public void issueNoVRProvider() {
        super.issueNoVRProvider();
    }

    @Override
    public void issueNoTTSProvider() {
        super.issueNoTTSProvider();
    }

    @Override
    public boolean shouldRebind(final Intent intent, @NonNull final ArrayList<String> blacklistArray) {
        return super.shouldRebind(intent, blacklistArray);
    }

    @Override
    public boolean shouldSpeak() {
        return super.shouldSpeak();
    }

    @Override
    public boolean shouldUnbind(final Intent intent) {
        return super.shouldUnbind(intent);
    }

    @Override
    public boolean validateRemote(final ISaiyListener rl) throws RemoteException {
        return super.validateRemote(rl);
    }

    @Override
    public boolean validateRemoteBundle(final ISaiyListener rl, final Bundle bundle) throws RemoteException {
        return super.validateRemoteBundle(rl, bundle);
    }

    @Override
    public void onTTSStarted() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onTTSStarted");
        }
        VolumeHelper.duckAudioMedia(mContext);
        removeRunnableCallback(fetchingNotification);
        removeRunnableCallback(initialisingNotification);
        NotificationHelper.cancelFetchingNotification(mContext);
        NotificationHelper.cancelInitialisingNotification(mContext);
        NotificationHelper.createSpeakingNotification(mContext, getSpeechLength(getUtterance()));
    }

    @Override
    public void onTTSEnded(@NonNull final SelfAwareCache cache, @Nullable final SaiyTextToSpeech tts,
                           @NonNull final SelfAwareParameters params) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onTTSEnded");
        }
        VolumeHelper.abandonAudioMedia(mContext);
        removeRunnableCallback(fetchingNotification);
        removeRunnableCallback(initialisingNotification);
        NotificationHelper.cancelInitialisingNotification(mContext);
        NotificationHelper.cancelFetchingNotification(mContext);
        NotificationHelper.cancelSpeakingNotification(mContext);

        if (tts != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (TTSDefaults.isApprovedVoice(tts.getInitialisedEngine())) {
                    if (!isSilentUtterance() && getUtterance().length() < SelfAwareCache.MAX_UTTERANCE_CHARS) {
                        if (!servingRemote()) {
                            final SaiyVoice voice = tts.getBoundSaiyVoice();

                            if (voice != null) {
                                cache.shouldCache(params, getTTSLocale(), getUtterance(), tts.getInitialisedEngine(), voice);
                            } else {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "onTTSEnded: not caching voice: saiyVoice null");
                                }
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "onTTSEnded: not caching voice: serving remote");
                            }
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onTTSEnded: not caching voice: utterance failed string checks");
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onTTSEnded: not caching voice: unapproved");
                    }
                }
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onTTSEnded: not caching voice: < LOLLIPOP");
                }
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onTTSEnded: not caching voice: tts null");
            }
        }
    }

    @Override
    public void onTTSError() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onTTSError");
        }
        VolumeHelper.abandonAudioMedia(mContext);
        removeRunnableCallback(fetchingNotification);
        removeRunnableCallback(initialisingNotification);
        NotificationHelper.cancelInitialisingNotification(mContext);
        NotificationHelper.cancelFetchingNotification(mContext);
        NotificationHelper.cancelSpeakingNotification(mContext);
    }

    @Override
    public void onVRStarted() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onVRStarted");
        }
        resetCancelled();
        Recognition.setState(Recognition.State.LISTENING);
        VolumeHelper.pauseAudioMedia(mContext);
        removeRunnableCallback(fetchingNotification);
        removeRunnableCallback(initialisingNotification);
        NotificationHelper.cancelInitialisingNotification(mContext);
        NotificationHelper.cancelFetchingNotification(mContext);
        NotificationHelper.createListeningNotification(mContext);

        if (getDefaultRecognition() != SaiyDefaults.VR.NATIVE) {
            vibrate(VIBRATE_MIN);
        }
    }

    @Override
    public void onVREnded() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onVREnded");
        }
        Recognition.setState(Recognition.State.PROCESSING);
        VolumeHelper.abandonAudioMedia(mContext);
        NotificationHelper.cancelListeningNotification(mContext);
        removeRunnableCallback(initialisingNotification);
        NotificationHelper.cancelInitialisingNotification(mContext);
        vibrate(VIBRATE_MIN);
        setFetchingCountdown();
    }


    @Override
    public void onVRComplete() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onVRComplete");
        }
        Recognition.setState(Recognition.State.IDLE);
        removeRunnableCallback(fetchingNotification);
        removeRunnableCallback(initialisingNotification);
        NotificationHelper.cancelInitialisingNotification(mContext);
        NotificationHelper.cancelFetchingNotification(mContext);
    }

    @Override
    public void onVRError() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onVRError");
        }
        Recognition.setState(Recognition.State.IDLE);
        VolumeHelper.abandonAudioMedia(mContext);
        removeRunnableCallback(fetchingNotification);
        removeRunnableCallback(initialisingNotification);
        NotificationHelper.cancelInitialisingNotification(mContext);
        NotificationHelper.cancelFetchingNotification(mContext);
        NotificationHelper.cancelListeningNotification(mContext);
        vibrate(VIBRATE_MIN);
    }

    /**
     * Runnable to display the fetching notification under network latency conditions.
     */
    private final Runnable fetchingNotification = new Runnable() {

        @Override
        public void run() {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "fetchingNotification: showing fetching notification");
            }

            NotificationHelper.createFetchingNotification(mContext);
        }
    };

    /**
     * Runnable to display the initialising notification under text to speech
     * lagging conditions.
     */
    private final Runnable initialisingNotification = new Runnable() {

        @Override
        public void run() {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialisingNotification: showing initialising notification");
            }

            NotificationHelper.createInitialisingNotification(mContext);
        }
    };

    /**
     * Set a countdown to show the fetching notification under network latency conditions.
     */
    public void setFetchingCountdown() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "setFetchingCountdown");
        }

        removeRunnableCallback(fetchingNotification);
        handler.postDelayed(fetchingNotification, FETCHING_DELAY);
    }

    /**
     * Set a countdown to show the initialising notification under text to speech
     * lagging conditions.
     */
    public void setInitialisingCountdown() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "setInitialisingCountdown");
        }

        removeRunnableCallback(initialisingNotification);
        handler.postDelayed(initialisingNotification, FETCHING_DELAY);
    }

    public Handler getHandler() {
        return this.handler;
    }

    /**
     * Remove callbacks associated with the {@link Runnable}
     *
     * @param runnable that callbacks should be removed from, or null if all should be removed.
     */
    public void removeRunnableCallback(final Runnable runnable) {

        if (runnable != null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "removeRunnableCallback: Single");
            }

            try {
                handler.removeCallbacks(runnable);
            } catch (final NullPointerException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "removeRunnableCallback: NullPointerException");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "removeRunnableCallback: Exception");
                    e.printStackTrace();
                }
            }

        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "removeRunnableCallback: All");
            }

            try {
                handler.removeCallbacksAndMessages(null);
            } catch (final NullPointerException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "removeRunnableCallback: NullPointerException");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "removeRunnableCallback: Exception");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Acquire a wake lock to turn on the device display for
     * the duration of {@link #SCREEN_WAKE_TIME}
     */
    public void acquireDisplayWakeLock() {
        if (UtilsDevice.isScreenOff(mContext)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "acquireDisplayWakeLock: acquired");
            }
            wakeLockDisplay.acquire(SCREEN_WAKE_TIME);
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "acquireDisplayWakeLock: device active");
            }
        }
    }

    /**
     * Acquire a wake lock to prevent the device from sleeping
     */
    public void acquireWakeLock() {
        if (SPH.getHotwordWakelock(mContext)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "acquireWakeLock: acquired");
            }
            wakeLock.acquire();
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "acquireWakeLock: user denied");
            }
        }
    }

    /**
     * Release any currently held wake lock
     */
    public void releaseWakeLock() {
        if (wakeLock.isHeld()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "releaseWakeLock: released");
            }
            wakeLock.release();
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "releaseWakeLock: not held");
            }
        }
    }

    /**
     * Get the initialised {@link SaiySoundPool} object
     *
     * @return the {@link SaiySoundPool}
     */
    public SaiySoundPool getSaiySoundPool() {
        return saiySoundPool;
    }

    /**
     * The Text to Speech provider has returned an error. It may be sufficient to just let the user know
     * something went wrong by displaying an error Toast. However, we need to check the
     * {@link Condition} to see if the user was in the middle of a command
     * and in such a case, we'll need to attempt to resolve the issue and continue from where we left off.
     *
     * @param defaultTTS one of {@link SaiyDefaults.TTS}
     * @param bundle     containing any possible {@link Condition}
     */
    @Override
    public void handleTTSError(@NonNull final SaiyDefaults.TTS defaultTTS, @NonNull final Bundle bundle) {
        super.handleTTSError(defaultTTS, bundle);
    }

    /**
     */
    public void handleVRError() {
        super.handleVRError(getDefaultRecognition(), getSupportedLanguage(false),
                getVRLocale(), getTTSLocale(), getBundle());
    }

    /**
     * The recognition provider has returned an error. It may be sufficient to just let the user know
     * something went wrong by displaying an error Toast. However, we need to check the
     * {@link Condition} to see if the user was in the middle of a command
     * and in such a case, we'll need to attempt to resolve the issue and continue from where we left off.
     *
     * @param error  the {@link SpeechRecognizer} error constant
     * @param bundle containing any possible {@link Condition}
     */
    public void handleRecognitionError(final int error, @NonNull final Bundle bundle,
                                       @NonNull final SaiyDefaults.VR defaultRecognizer) {
        super.handleRecognitionError(error, bundle, defaultRecognizer, getSupportedLanguage(false),
                getVRLocale(), getTTSLocale());
    }

    /**
     * Show an error toast on the main thread.
     *
     * @param toastWords the String to toast
     * @param length     one of {@link Toast#LENGTH_LONG} or {@link Toast#LENGTH_SHORT}
     */
    @Override
    public void showToast(final String toastWords, final int length) {
        super.showToast(toastWords, length);
    }

    /**
     * Add haptic feedback for the user.
     */
    public void vibrate(final long duration) {
        if (SPH.getVibrateCondition(mContext)) {
            ((Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(duration);
        }
    }
}
