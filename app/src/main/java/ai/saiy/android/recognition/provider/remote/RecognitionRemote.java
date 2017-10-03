package ai.saiy.android.recognition.provider.remote;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.ParseException;
import android.net.Uri;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.HttpsURLConnection;

import ai.saiy.android.api.remote.Request;
import ai.saiy.android.audio.SaiyRecorder;
import ai.saiy.android.audio.SaiySoundPool;
import ai.saiy.android.audio.pause.PauseDetector;
import ai.saiy.android.audio.pause.PauseListener;
import ai.saiy.android.recognition.Recognition;
import ai.saiy.android.recognition.SaiyRecognitionListener;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

/**
 * Created by benrandall76@gmail.com on 29/06/2016.
 */

public class RecognitionRemote implements PauseListener {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = RecognitionRemote.class.getSimpleName();

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_ = "Bearer ";
    private static final String LANGUAGE = "language";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String HEADER_CONTENT_TYPE = "application/x-www-form-urlencoded; charset=UTF-8";
    private static final String TRANSFER_ENCODING = "Transfer-Encoding";
    private static final String CHUNKED = "chunked";
    private static final String CONTENT_TYPE_AUDIO_PARAMS = "audio/l16; rate=8000";

    // May become part of the constructor.
    private static final short nChannels = 1;
    private final int audioSource = MediaRecorder.AudioSource.VOICE_RECOGNITION;
    private final int sampleRateInHz = 8000;
    private final int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    public static int bufferSize;

    private volatile SaiyRecorder saiyRecorder;
    private final AtomicBoolean isRecording = new AtomicBoolean();
    private final SaiySoundPool ssp;

    private HttpsURLConnection urlConnection;
    private OutputStream outputStream;
    private InputStream inputStream;

    private final PauseDetector pauseDetector;

    private final SaiyRecognitionListener listener;
    private final String language;
    private final String apiKey;
    private final Uri remoteUri;

    /**
     * Constructor
     *
     * @param listener  the associated {@link SaiyRecognitionListener}
     * @param language  the Locale we are using to analyse the voice data. This is not necessarily the
     *                  Locale of the device, as the user may be multi-lingual and have set a custom
     *                  recognition language in a launcher short-cut.
     * @param remoteUri the Uri of the remote server
     * @param apiKey    the Remote API key or access token
     */
    public RecognitionRemote(@NonNull final SaiyRecognitionListener listener,
                             @NonNull final String language, @NonNull final Uri remoteUri,
                             @NonNull final String apiKey, @NonNull final SaiySoundPool ssp) {
        this.listener = listener;
        this.language = language;
        this.apiKey = apiKey;
        this.remoteUri = remoteUri;
        this.ssp = ssp;

        if (DEBUG) {
            MyLog.i(CLS_NAME, "language: " + language);
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
        Recognition.setState(Recognition.State.PROCESSING);
    }

    public void startListening() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "startRecording");
        }
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

        isRecording.set(true);
        ssp.play(ssp.getBeepStart());
        pauseDetector.begin();

        bufferSize = saiyRecorder.getBufferSize();

        if (DEBUG) {
            MyLog.i(CLS_NAME, "bufferSize: " + bufferSize);
        }

        final byte[] buffer = new byte[bufferSize];

        switch (saiyRecorder.initialise()) {

            case AudioRecord.STATE_INITIALIZED:

                try {

                    urlConnection = (HttpsURLConnection) new URL(remoteUri.toString()).openConnection();
                    urlConnection.setAllowUserInteraction(false);
                    urlConnection.setInstanceFollowRedirects(true);
                    urlConnection.setRequestMethod(Constants.HTTP_POST);
                    urlConnection.setRequestProperty(CONTENT_TYPE, HEADER_CONTENT_TYPE);
                    urlConnection.setRequestProperty(AUTHORIZATION, BEARER_ + apiKey);
                    urlConnection.setRequestProperty(LANGUAGE, language);
                    urlConnection.setUseCaches(false);
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestProperty(TRANSFER_ENCODING, CHUNKED);
                    urlConnection.setChunkedStreamingMode(0);
                    urlConnection.setRequestProperty(CONTENT_TYPE, CONTENT_TYPE_AUDIO_PARAMS);
                    urlConnection.connect();

                    outputStream = urlConnection.getOutputStream();

                    switch (saiyRecorder.startRecording()) {

                        case AudioRecord.RECORDSTATE_RECORDING: {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "AudioRecord.RECORDSTATE_RECORDING");
                            }

                            int count = 0;
                            int bufferReadResult;
                            while (isRecording.get() && saiyRecorder != null
                                    && saiyRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {

                                if (count == 0) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "Recording Started");
                                    }

                                    Recognition.setState(Recognition.State.LISTENING);
                                    listener.onReadyForSpeech(null);
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
                            } else {
                                inputStream = urlConnection.getInputStream();

                                final String response = UtilsString.streamToString(inputStream);

                                if (DEBUG) {
                                    MyLog.d(CLS_NAME, "response: " + response);
                                }

                                final JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
                                final JSONArray arrayCategories = object.getJSONArray(Request.RESULTS);

                                final int size = arrayCategories.length();
                                if (DEBUG) {
                                    MyLog.d(CLS_NAME, "response size: " + size);
                                }

                                final float[] floatsArray = new float[size];
                                final ArrayList<String> resultsArray = new ArrayList<>(size);

                                JSONObject objectSegment;
                                for (int i = 0; i < size; i++) {
                                    objectSegment = arrayCategories.getJSONObject(i);

                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "segment: " + objectSegment.getString(Request.SPEECH)
                                                + " ~" + objectSegment.getDouble(Request.CONFIDENCE));
                                    }

                                    resultsArray.add(objectSegment.getString(Request.SPEECH));
                                    floatsArray[i] = (float) objectSegment.getDouble(Request.CONFIDENCE);
                                }

                                final Bundle results = new Bundle();
                                results.putStringArrayList(Request.RESULTS_RECOGNITION, resultsArray);
                                results.putFloatArray(Request.CONFIDENCE_SCORES, floatsArray);
                                results.putString(Request.RESULTS_NLU, response);
                                listener.onResults(results);
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

        synchronized (this) {

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
