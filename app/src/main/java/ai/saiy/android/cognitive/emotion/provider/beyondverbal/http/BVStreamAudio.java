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

package ai.saiy.android.cognitive.emotion.provider.beyondverbal.http;

import android.net.ParseException;
import android.os.Process;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

import ai.saiy.android.R;
import ai.saiy.android.audio.IMic;
import ai.saiy.android.audio.RecognitionMic;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.BeyondVerbal;
import ai.saiy.android.cognitive.identity.provider.microsoft.Speaker;
import ai.saiy.android.localisation.SaiyResourcesHelper;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.recognition.Recognition;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsString;

/**
 * Class to stream raw audio to the BV API
 * <p>
 * Created by benrandall76@gmail.com on 10/06/2016.
 */
public class BVStreamAudio implements IMic {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = BVStreamAudio.class.getSimpleName();

    private static final String RECORDING_URL = "https://apiv4.beyondverbal.com/v4/recording/";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_ = "Bearer ";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String HEADER_CONTENT_TYPE = "application/x-www-form-urlencoded; charset=UTF-8";

    private static final String TRANSFER_ENCODING = "Transfer-Encoding";
    private static final String CHUNKED = "chunked";
    private static final String CONTENT_TYPE_AUDIO_PARAMS = "audio/l16; rate=8000";

    private volatile HttpsURLConnection urlConnection;
    private volatile OutputStream outputStream;

    private volatile int retryCount;

    private final String token;
    private final String recordingId;
    private final RecognitionMic mic;
    private final SupportedLanguage sl;

    /**
     * Constructor
     *
     * @param mic         the initialised {@link RecognitionMic} object
     * @param sl          the {@link SupportedLanguage} object
     * @param token       the Beyond Verbal access token
     * @param recordingId the Beyond Verbal recording id.
     */
    public BVStreamAudio(@NonNull final RecognitionMic mic, @NonNull final SupportedLanguage sl,
                         @NonNull final String token,
                         @NonNull final String recordingId) {
        this.mic = mic;
        this.token = token;
        this.recordingId = recordingId;
        this.sl = sl;

        this.mic.setMicListener(this);
    }

    /**
     * Start streaming the audio to the BV servers
     */
    public void stream() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "stream");
        }

        mic.startRecording();

        final Thread httpThread = new Thread() {

            public void run() {
                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);

                try {

                    urlConnection = (HttpsURLConnection) new URL(RECORDING_URL + recordingId).openConnection();
                    urlConnection.setAllowUserInteraction(false);
                    urlConnection.setInstanceFollowRedirects(true);
                    urlConnection.setRequestMethod(Constants.HTTP_POST);
                    urlConnection.setRequestProperty(CONTENT_TYPE, HEADER_CONTENT_TYPE);
                    urlConnection.setRequestProperty(AUTHORIZATION, BEARER_ + token);
                    urlConnection.setUseCaches(false);
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestProperty(TRANSFER_ENCODING, CHUNKED);
                    urlConnection.setChunkedStreamingMode(0);
                    urlConnection.setRequestProperty(CONTENT_TYPE, CONTENT_TYPE_AUDIO_PARAMS);
                    urlConnection.connect();

                    outputStream = urlConnection.getOutputStream();

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
                        MyLog.i(CLS_NAME, "requesting response");
                    }

                    final int responseCode = urlConnection.getResponseCode();

                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "responseCode: " + responseCode);
                    }

                    if (responseCode != HttpsURLConnection.HTTP_OK) {
                        if (DEBUG) {
                            MyLog.e(CLS_NAME, "audioThread ErrorStream: "
                                    + UtilsString.streamToString(urlConnection.getErrorStream()));
                        }

                        onError(SpeechRecognizer.ERROR_NETWORK);
                    } else {
                        if (DEBUG) {
                            MyLog.d(CLS_NAME, "response: HTTP_OK. Setting timer");
                        }

                        if (mic.isInterrupted()) {
                            onError(Speaker.ERROR_USER_CANCELLED);
                        } else {
                            mic.getRecognitionListener().onComplete();
                            proceedAndNotify();
                        }
                    }

                } catch (final MalformedURLException e) {
                    if (DEBUG) {
                        MyLog.e(CLS_NAME, "MalformedURLException");
                        e.printStackTrace();
                    }
                    onError(SpeechRecognizer.ERROR_NETWORK);
                } catch (final ParseException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "ParseException");
                        e.printStackTrace();
                    }
                    onError(SpeechRecognizer.ERROR_NETWORK);
                } catch (final UnknownHostException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "UnknownHostException");
                        e.printStackTrace();
                    }
                    onError(SpeechRecognizer.ERROR_NETWORK);
                } catch (final IOException e) {
                    if (DEBUG) {
                        MyLog.e(CLS_NAME, "IOException");
                        e.printStackTrace();
                    }
                    onError(SpeechRecognizer.ERROR_NETWORK);
                } catch (final IllegalStateException e) {
                    if (DEBUG) {
                        MyLog.e(CLS_NAME, "IllegalStateException");
                        e.printStackTrace();
                    }
                    onError(SpeechRecognizer.ERROR_NETWORK);
                } catch (final NullPointerException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "NullPointerException");
                        e.printStackTrace();
                    }
                    onError(SpeechRecognizer.ERROR_NETWORK);
                } catch (final Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "Exception");
                        e.printStackTrace();
                    }
                    onError(SpeechRecognizer.ERROR_NETWORK);
                } finally {
                    closeConnection();
                    mic.stopRecording();
                }
            }
        };

        httpThread.start();
    }

    @Override
    public void onBufferReceived(final int bufferReadResult, final byte[] buffer) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onBufferReceived");
        }

        try {
            for (int i = 0; i < bufferReadResult; i++) {
                if (outputStream != null) {
                    outputStream.write(buffer[i]);
                }
            }
        } catch (final IOException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "onBufferReceived: IOException");
                e.printStackTrace();
            }
        }
    }

    private void proceedAndNotify() {

        if (retryCount < 2) {

            if (retryCount == 0) {
                final LocalRequest localRequest = new LocalRequest(mic.getContext());
                localRequest.setSupportedLanguage(sl);
                localRequest.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                localRequest.setTTSLocale(SPH.getTTSLocale(mic.getContext()));
                localRequest.setVRLocale(SPH.getVRLocale(mic.getContext()));
                localRequest.setUtterance(PersonalityResponse.getBVAnalysisCompleteResponse(mic.getContext(), sl));
                localRequest.execute();
            }

            retryCount++;

            final TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "timerTask: fetching analysis");
                    }

                    if (!new BVEmotionAnalysis(mic.getContext(), sl, token).getAnalysis(recordingId, 0)
                            .first) {
                        proceedAndNotify();
                    }
                }
            };

            new Timer().schedule(timerTask, BeyondVerbal.FETCH_ANALYSIS_DELAY);
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
            MyLog.i(CLS_NAME, "onRecordingEnded");
        }
    }

    private void closeConnection() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "closeConnection");
        }

        if (outputStream != null) {

            try {
                outputStream.close();
            } catch (final Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            }
        }

        if (urlConnection != null) {

            try {
                urlConnection.disconnect();
            } catch (final Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            }
        }
    }
}
