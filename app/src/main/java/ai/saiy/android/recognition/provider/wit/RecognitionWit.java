package ai.saiy.android.recognition.provider.wit;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.ParseException;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.HttpsURLConnection;

import ai.saiy.android.api.SaiyDefaults;
import ai.saiy.android.api.language.vr.VRLanguageIBM;
import ai.saiy.android.api.language.vr.VRLanguageWit;
import ai.saiy.android.api.remote.Request;
import ai.saiy.android.audio.SaiyRecorder;
import ai.saiy.android.audio.SaiySoundPool;
import ai.saiy.android.audio.pause.PauseDetector;
import ai.saiy.android.audio.pause.PauseListener;
import ai.saiy.android.configuration.WitConfiguration;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.nlu.wit.NLUWit;
import ai.saiy.android.nlu.wit.ResolveWit;
import ai.saiy.android.recognition.Recognition;
import ai.saiy.android.recognition.SaiyRecognitionListener;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsLocale;
import ai.saiy.android.utils.UtilsString;

/**
 * Created by benrandall76@gmail.com on 29/06/2016.
 */

public class RecognitionWit implements PauseListener {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = RecognitionWit.class.getSimpleName();

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_ = "Bearer ";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String HEADER_CONTENT_TYPE = "audio/raw;encoding=signed-integer;bits=16;rate=16000;endian=little";
    private static final String TRANSFER_ENCODING = "Transfer-Encoding";
    private static final String CHUNKED = "chunked";
    private static final String ACCEPT_HEADER = "Accept";
    private static final String N_HEADER = "n";
    private static final String ACCEPT_VERSION = "application/vnd.wit." + "20160526";

    // May become part of the constructor.
    private static final short nChannels = 1;
    private final int audioSource = MediaRecorder.AudioSource.VOICE_RECOGNITION;
    private final int sampleRateInHz = 16000;
    private final int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    private HttpsURLConnection urlConnection;
    private OutputStream outputStream;
    private InputStream inputStream;

    private final PauseDetector pauseDetector;

    private final Object lock = new Object();

    private volatile SaiyRecorder saiyRecorder;
    private final AtomicBoolean isRecording = new AtomicBoolean();
    private final SaiySoundPool ssp;
    private final SaiyRecognitionListener listener;
    private final Context mContext;
    private final SaiyDefaults.LanguageModel languageModel;
    private final Locale ttsLocale;
    private final VRLanguageWit vrLocale;
    private final SupportedLanguage sl;
    private final boolean servingRemote;
    private final String accessToken;

    /**
     * Constructor
     *
     * @param mContext      the application context
     * @param listener      the associated {@link SaiyRecognitionListener}
     * @param accessToken   the Bluemix service user name
     * @param languageModel the {@link SaiyDefaults.LanguageModel}
     * @param ttsLocale     the Text to Speech {@link Locale}
     * @param vrLocale      the {@link VRLanguageIBM}
     * @param sl            the {@link SupportedLanguage}
     * @param servingRemote true if the origin is a remote request
     */
    public RecognitionWit(@NonNull final Context mContext, @NonNull final SaiyRecognitionListener listener,
                          @NonNull final String accessToken, @NonNull final SaiyDefaults.LanguageModel languageModel,
                          @NonNull final Locale ttsLocale, @NonNull final VRLanguageWit vrLocale,
                          @NonNull final SupportedLanguage sl, final boolean servingRemote,
                          @NonNull final SaiySoundPool ssp) {
        this.mContext = mContext;
        this.listener = listener;
        this.languageModel = languageModel;
        this.ttsLocale = ttsLocale;
        this.vrLocale = vrLocale;
        this.sl = sl;
        this.servingRemote = servingRemote;
        this.accessToken = accessToken;
        this.ssp = ssp;

        if (DEBUG) {
            MyLog.i(CLS_NAME, "language: " + vrLocale.getLocaleString());
        }

        saiyRecorder = new SaiyRecorder(audioSource, sampleRateInHz, channelConfig, audioFormat, true);
        pauseDetector = new PauseDetector(this, sampleRateInHz, nChannels,
                PauseDetector.DEFAULT_PAUSE_IGNORE_TIME);

    }

    @Override
    public void onPauseDetected() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onPauseDetected");
        }

        if (isRecording.get()) {
            stopListening();
        }
    }

    public void cancel() {
    }

    public void stopListening() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "called stopRecording");
        }

        isRecording.set(false);
        Recognition.setState(Recognition.State.IDLE);
    }

    public void startListening() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "startRecording");
        }
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

        isRecording.set(true);
        pauseDetector.begin();

        final int bufferSize = saiyRecorder.getBufferSize();

        if (DEBUG) {
            MyLog.i(CLS_NAME, "bufferSize: " + bufferSize);
        }

        final byte[] buffer = new byte[bufferSize];

        switch (saiyRecorder.initialise()) {

            case AudioRecord.STATE_INITIALIZED:

                try {

                    urlConnection = (HttpsURLConnection) new URL(WitConfiguration.WIT_SPEECH_URL).openConnection();
                    urlConnection.setAllowUserInteraction(false);
                    urlConnection.setInstanceFollowRedirects(true);
                    urlConnection.setRequestMethod(Constants.HTTP_POST);
                    urlConnection.setRequestProperty(CONTENT_TYPE, HEADER_CONTENT_TYPE);
                    urlConnection.setRequestProperty(AUTHORIZATION, BEARER_ + accessToken);
                    urlConnection.setRequestProperty(ACCEPT_HEADER, ACCEPT_VERSION);
                    urlConnection.setRequestProperty(N_HEADER, "5");
                    urlConnection.setUseCaches(false);
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestProperty(TRANSFER_ENCODING, CHUNKED);
                    urlConnection.setChunkedStreamingMode(0);
                    urlConnection.connect();

                    outputStream = urlConnection.getOutputStream();

                    ssp.play(ssp.getBeepStart());

                    switch (saiyRecorder.startRecording()) {

                        case AudioRecord.RECORDSTATE_RECORDING: {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "AudioRecord.RECORDSTATE_RECORDING");
                            }

                            int bufferReadResult;
                            int count = 0;
                            while (isRecording.get() && saiyRecorder != null
                                    && saiyRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {

                                if (count == 0) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "Recording Started");
                                    }

                                    Recognition.setState(Recognition.State.LISTENING);
                                    listener.onReadyForSpeech(null);
                                    listener.onBeginningOfSpeech();
                                    count++;
                                }

                                if (saiyRecorder != null) {
                                    bufferReadResult = saiyRecorder.read(buffer);
                                    listener.onBufferReceived(buffer);

                                    if (!pauseDetector.hasDetected()) {
                                        pauseDetector.addLength(buffer, bufferReadResult);
                                        pauseDetector.monitor();
                                    }

                                    for (int i = 0; i < bufferReadResult; i++) {
                                        outputStream.write(buffer[i]);
                                    }
                                }
                            }

                            audioShutdown();

                            final int responseCode = urlConnection.getResponseCode();

                            if (DEBUG) {
                                MyLog.d(CLS_NAME, "responseCode: " + responseCode);
                            }

                            if (responseCode != HttpsURLConnection.HTTP_OK) {
                                if (DEBUG) {
                                    MyLog.e(CLS_NAME, "audioThread ErrorStream: "
                                            + UtilsString.streamToString(urlConnection.getErrorStream()));
                                }

                                listener.onError(SpeechRecognizer.ERROR_NETWORK);

                            } else {
                                inputStream = urlConnection.getInputStream();

                                final String response = UtilsString.streamToString(inputStream);

                                final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
                                final NLUWit nluWit = gson.fromJson(response, NLUWit.class);

                                final ArrayList<String> resultsArray = new ArrayList<>();
                                resultsArray.add(nluWit.getText());

                                final float[] floatsArray = new float[1];
                                floatsArray[0] = nluWit.getConfidence();

                                final Bundle results = new Bundle();

                                if (languageModel == SaiyDefaults.LanguageModel.WIT) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "final: nlu required");
                                    }

                                    if (servingRemote) {
                                        results.putString(Request.RESULTS_NLU, response);
                                        results.putStringArrayList(Request.RESULTS_RECOGNITION, resultsArray);
                                        results.putFloatArray(Request.CONFIDENCE_SCORES, floatsArray);
                                        listener.onResults(results);
                                    } else {
                                        new ResolveWit(mContext, sl, UtilsLocale.stringToLocale(vrLocale.toString()),
                                                ttsLocale, floatsArray, resultsArray).unpack(nluWit);
                                    }
                                } else {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "final: nlu not required");
                                    }

                                    results.putStringArrayList(Request.RESULTS_RECOGNITION, resultsArray);
                                    results.putFloatArray(Request.CONFIDENCE_SCORES, floatsArray);
                                    listener.onResults(results);
                                }
                            }
                        }

                        break;
                        case AudioRecord.ERROR:
                        default:
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "AudioRecord.ERROR");
                            }
                            listener.onError(SpeechRecognizer.ERROR_AUDIO);
                            break;
                    }

                } catch (final MalformedURLException e) {
                    if (DEBUG) {
                        MyLog.e(CLS_NAME, "MalformedURLException");
                        e.printStackTrace();
                    }
                } catch (final ParseException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "ParseException");
                        e.printStackTrace();
                    }
                } catch (final UnknownHostException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "UnknownHostException");
                        e.printStackTrace();
                    }
                } catch (final IOException e) {
                    if (DEBUG) {
                        MyLog.e(CLS_NAME, "IOException");
                        e.printStackTrace();
                    }
                } catch (final IllegalStateException e) {
                    if (DEBUG) {
                        MyLog.e(CLS_NAME, "IllegalStateException");
                        e.printStackTrace();
                    }
                } catch (final NullPointerException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "NullPointerException");
                        e.printStackTrace();
                    }
                } catch (final Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "Exception");
                        e.printStackTrace();
                    }
                } finally {
                    closeConnection();
                }

                audioShutdown();
                break;

            case AudioRecord.STATE_UNINITIALIZED:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "AudioRecord.STATE_UNINITIALIZED");
                }

                listener.onError(SpeechRecognizer.ERROR_AUDIO);
                break;
        }
    }


    /**
     * Shutdown the microphone and release the resources
     */
    private void audioShutdown() {
        if (DEBUG) {
            MyLog.v(CLS_NAME, "audioShutdown");
        }

        synchronized (lock) {

            if (saiyRecorder != null) {
                ssp.play(ssp.getBeepStop());
                Recognition.setState(Recognition.State.IDLE);
                listener.onEndOfSpeech();
                saiyRecorder.shutdown(CLS_NAME);
                saiyRecorder = null;
            } else {
                if (DEBUG) {
                    MyLog.v(CLS_NAME, "audioShutdown: saiyRecorder already null");
                }
            }

            if (DEBUG) {
                MyLog.v(CLS_NAME, "audioShutdown: finished synchronisation");
            }
        }
    }

    private void closeConnection() {

        if (outputStream != null) {

            try {
                outputStream.close();
            } catch (final Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            }
        }

        if (inputStream != null) {

            try {
                inputStream.close();
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
