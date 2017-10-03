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

package ai.saiy.android.cognitive.identity.provider.microsoft;

import android.os.Process;
import android.support.annotation.NonNull;

import ai.saiy.android.R;
import ai.saiy.android.audio.IMic;
import ai.saiy.android.audio.RecognitionMic;
import ai.saiy.android.cognitive.identity.provider.microsoft.http.ValidateID;
import ai.saiy.android.localisation.SaiyResourcesHelper;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.recognition.Recognition;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

/**
 * Created by benrandall76@gmail.com on 07/09/2016.
 */

public class SpeakerIdentification implements IMic {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = SpeakerIdentification.class.getSimpleName();

    public static final long MINIMUM_AUDIO_TIME = 3000;

    private final SupportedLanguage sl;
    private final RecognitionMic mic;
    private final String apiKey;
    private final String profileId;
    private final boolean shortAudio;

    /**
     * Constructor
     *
     * @param mic        the initialised {@link RecognitionMic} object
     * @param sl         the {@link SupportedLanguage}
     * @param apiKey     the OCP APIM key
     * @param profileId  of the user
     * @param shortAudio true if the capture length will be below the recommended threshold, false otherwise
     */
    public SpeakerIdentification(@NonNull final RecognitionMic mic,
                                 @NonNull final SupportedLanguage sl, @NonNull final String apiKey,
                                 @NonNull final String profileId, final boolean shortAudio) {
        this.mic = mic;
        this.sl = sl;
        this.apiKey = apiKey;
        this.profileId = profileId;
        this.shortAudio = shortAudio;

        this.mic.setMicListener(this);
    }

    /**
     * Start recording the enrollment file
     */
    public void record() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "record");
        }

        if (mic.isAvailable()) {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);

            mic.startRecording();

            synchronized (mic.getLock()) {
                while (mic.isRecording()) {
                    try {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "mic lock waiting");
                        }
                        mic.getLock().wait();
                    } catch (final InterruptedException e) {
                        if (DEBUG) {
                            MyLog.e(CLS_NAME, "InterruptedException");
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }

            if (DEBUG) {
                MyLog.i(CLS_NAME, "record lock released");
            }

            mic.getRecognitionListener().onComplete();

        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "mic unavailable");
            }

            onError(Speaker.ERROR_AUDIO);
        }
    }

    @Override
    public void onError(int error) {
        if (DEBUG) {
            MyLog.w(CLS_NAME, "onError");
        }

        mic.getRecognitionListener().onComplete();
        Recognition.setState(Recognition.State.IDLE);

        if (mic.isInterrupted()) {
            error = Speaker.ERROR_USER_CANCELLED;
        }

        final LocalRequest localRequest = new LocalRequest(mic.getContext());
        localRequest.setSupportedLanguage(sl);
        localRequest.setAction(LocalRequest.ACTION_SPEAK_ONLY);
        localRequest.setTTSLocale(SPH.getTTSLocale(mic.getContext()));
        localRequest.setVRLocale(SPH.getVRLocale(mic.getContext()));

        switch (error) {

            case Speaker.ERROR_USER_CANCELLED:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "onError ERROR_USER_CANCELLED");
                }
                localRequest.setUtterance(SaiyResourcesHelper.getStringResource(mic.getContext(), sl,
                        R.string.cancelled));
                break;
            case Speaker.ERROR_NETWORK:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "onError ERROR_NETWORK");
                }
                localRequest.setUtterance(PersonalityResponse.getNoNetwork(mic.getContext(), sl));
                break;
            case Speaker.ERROR_AUDIO:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "onError ERROR_AUDIO");
                }
                localRequest.setUtterance(SaiyResourcesHelper.getStringResource(mic.getContext(), sl,
                        R.string.error_audio));
                break;
            case Speaker.ERROR_FILE:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "onError ERROR_FILE");
                }
                localRequest.setUtterance(SaiyResourcesHelper.getStringResource(mic.getContext(), sl,
                        R.string.error_audio));
                break;
            default:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "onError default");
                }
                localRequest.setUtterance(SaiyResourcesHelper.getStringResource(mic.getContext(), sl,
                        R.string.error_audio));
                break;
        }

        localRequest.execute();
    }

    @Override
    public void onBufferReceived(final int bufferReadResult, final byte[] buffer) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onBufferReceived");
        }
    }

    @Override
    public void onPauseDetected() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onPauseDetected");
        }
    }

    @Override
    public void onRecordingStarted() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onRecordingStarted");
        }
    }

    @Override
    public void onRecordingEnded() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onRecordingEnded");
        }
        Recognition.setState(Recognition.State.IDLE);
    }

    @Override
    public void onFileWriteComplete(final boolean success) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onFileWriteComplete: " + success);
        }

        if (!mic.isInterrupted()) {

            if (success) {

                final LocalRequest localRequest = new LocalRequest(mic.getContext());
                localRequest.setSupportedLanguage(sl);
                localRequest.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                localRequest.setTTSLocale(SPH.getTTSLocale(mic.getContext()));
                localRequest.setVRLocale(SPH.getVRLocale(mic.getContext()));
                localRequest.setUtterance(SaiyResourcesHelper.getStringResource(mic.getContext(), sl,
                        R.string.vocal_notify_verify));
                localRequest.execute();

                new ValidateID(mic, sl, apiKey, profileId, shortAudio, mic.getFile()).stream();
            } else {
                onError(Speaker.ERROR_FILE);
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onFileWriteComplete: mic interrupted");
            }
        }
    }
}
