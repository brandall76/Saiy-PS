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

package ai.saiy.android.recognition.provider.google.cloud;

import android.content.Context;
import android.os.Bundle;
import android.os.Process;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1beta1.RecognitionConfig;
import com.google.cloud.speech.v1beta1.SpeechGrpc;
import com.google.cloud.speech.v1beta1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1beta1.StreamingRecognitionConfig;
import com.google.cloud.speech.v1beta1.StreamingRecognitionResult;
import com.google.cloud.speech.v1beta1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1beta1.StreamingRecognizeResponse;
import com.google.protobuf.ByteString;
import com.google.protobuf.TextFormat;

import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import ai.saiy.android.api.language.vr.VRLanguageGoogle;
import ai.saiy.android.audio.IMic;
import ai.saiy.android.audio.RecognitionMic;
import ai.saiy.android.cognitive.identity.provider.microsoft.Speaker;
import ai.saiy.android.recognition.Recognition;
import ai.saiy.android.recognition.SaiyRecognitionListener;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import io.grpc.internal.DnsNameResolverProvider;
import io.grpc.okhttp.OkHttpChannelProvider;
import io.grpc.stub.StreamObserver;

/**
 * Created by benrandall76@gmail.com on 21/09/2016.
 */

public class RecognitionGoogleCloud implements IMic, StreamObserver<StreamingRecognizeResponse> {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = RecognitionGoogleCloud.class.getSimpleName();

    private static final int UNRECOVERABLE = -99;

    private static final List<String> OAUTH2_SCOPES = Collections.singletonList(
            "https://www.googleapis.com/auth/cloud-platform");
    private static final String HOSTNAME = "speech.googleapis.com";
    private static final int PORT = 443;

    private final AtomicBoolean isRecording = new AtomicBoolean();
    private final AtomicBoolean doBeginning = new AtomicBoolean(true);
    private final AtomicBoolean doError = new AtomicBoolean(true);
    private final AtomicBoolean doEnd = new AtomicBoolean(true);
    private final AtomicBoolean doResults = new AtomicBoolean(true);
    private final AtomicBoolean doRecordingEnded = new AtomicBoolean(true);

    private final ArrayList<String> partialArray = new ArrayList<>();
    private final ArrayList<String> resultsArray = new ArrayList<>();
    private final ArrayList<Float> confidenceArray = new ArrayList<>();
    private final Bundle bundle = new Bundle();

    private final SaiyRecognitionListener listener;
    private final RecognitionMic mic;
    private final Context mContext;

    private volatile StreamObserver<StreamingRecognizeRequest> requestObserver;
    private volatile StreamingRecognizeRequest initial;
    private volatile SpeechGrpc.SpeechStub mApi;

    /**
     * Constructor
     *
     * @param mContext    the application context
     * @param listener    the associated {@link SaiyRecognitionListener}
     * @param language    the Locale we are using to analyse the voice data. This is not necessarily the
     *                    Locale of the device, as the user may be multi-lingual and have set a custom
     *                    recognition language in a launcher short-cut.
     * @param accessToken the Chromium Google API key
     */
    public RecognitionGoogleCloud(@NonNull final Context mContext, @NonNull final SaiyRecognitionListener listener,
                                  @NonNull final VRLanguageGoogle language, @NonNull final AccessToken accessToken,
                                  @NonNull final RecognitionMic mic) {
        this.listener = listener;
        this.mic = mic;
        this.mContext = mContext;

        this.mic.setMicListener(this);

        try {

            ProviderInstaller.installIfNeeded(mContext);

            final GoogleCredentials googleCredentials = new GoogleCredentials(accessToken) {
                @Override
                public AccessToken refreshAccessToken() throws IOException {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "refreshAccessToken");
                    }
                    return accessToken;
                }
            }.createScoped(OAUTH2_SCOPES);

            if (DEBUG) {
                MyLog.i(CLS_NAME, "language: " + language);
            }

            final ManagedChannel channel = new OkHttpChannelProvider()
                    .builderForAddress(HOSTNAME, PORT)
                    .nameResolverFactory(new DnsNameResolverProvider())
                    .intercept(new GoogleCredentialsInterceptor(googleCredentials.createScoped(OAUTH2_SCOPES)))
                    .enableKeepAlive(false)
                    .build();

            final long then = System.nanoTime();

            mApi = SpeechGrpc.newStub(channel);

            if (DEBUG) {
                MyLog.getElapsed(CLS_NAME + " End of requestObserver", then);
            }

            requestObserver = mApi.streamingRecognize(this);

            initial = StreamingRecognizeRequest.newBuilder().setStreamingConfig(
                    StreamingRecognitionConfig.newBuilder()
                            .setConfig(RecognitionConfig.newBuilder()
                                    .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                                    .setSampleRate(RecognitionMic.SAMPLE_RATE_HZ_16000)
                                    .setMaxAlternatives(10)
                                    .setProfanityFilter(false)
                                    .setLanguageCode(language.getLocaleString())
                                    .build())
                            .setInterimResults(true)
                            .setSingleUtterance(true)
                            .build()).build();

        } catch (final GooglePlayServicesRepairableException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "Constructor GooglePlayServicesRepairableException");
                e.printStackTrace();
            }
            showPlayServicesError(e.getConnectionStatusCode());
        } catch (final GooglePlayServicesNotAvailableException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "Constructor GooglePlayServicesNotAvailableException");
                e.printStackTrace();
            }
            showPlayServicesError(UNRECOVERABLE);
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "Constructor Exception");
                e.printStackTrace();
            }
            onError(SpeechRecognizer.ERROR_CLIENT);
        }
    }

    private void showPlayServicesError(final int errorCode) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "showPlayServicesError");
        }

        onError(SpeechRecognizer.ERROR_CLIENT);

        switch (errorCode) {

            case UNRECOVERABLE:
                // TODO
                break;
            default:
                final GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
                apiAvailability.showErrorNotification(mContext, errorCode);
                break;
        }
    }


    /**
     * Start the recognition.
     */
    public void startListening() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "startListening");
        }

        if (doError.get()) {
            if (mic.isAvailable()) {
                isRecording.set(true);
                mic.startRecording();

                requestObserver.onNext(initial);

                new Thread() {
                    public void run() {
                        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);

                        try {

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
                                MyLog.i(CLS_NAME, "record lock released: interrupted: " + mic.isInterrupted());
                            }

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
                            stopListening();
                        }
                    }
                }.start();

            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "startListening mic unavailable");
                }

                onError(Speaker.ERROR_AUDIO);
                mic.forceAudioShutdown();
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "startListening error already thrown");
            }

            mic.forceAudioShutdown();
        }
    }


    @Override
    public void onBufferReceived(final int bufferReadResult, final byte[] buffer) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onBufferReceived");
        }

        if (isRecording.get()) {

            try {

                requestObserver.onNext(StreamingRecognizeRequest.newBuilder()
                        .setAudioContent(ByteString.copyFrom(buffer, 0, bufferReadResult))
                        .build());

            } catch (final IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "onBufferReceived IllegalStateException");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "onBufferReceived Exception");
                    e.printStackTrace();
                }
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onBufferReceived: recording finished");
            }
        }
    }

    @Override
    public void onError(final int error) {
        if (DEBUG) {
            MyLog.w(CLS_NAME, "onError");
        }

        if (doError.get()) {
            doError.set(false);
            stopListening();

            Recognition.setState(Recognition.State.IDLE);

            if (mic.isInterrupted()) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onError: isInterrupted");
                }

                listener.onError(error);
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onError: listener onComplete");
                }

                listener.onComplete();
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onError: doError false");
            }
        }
    }

    @Override
    public void onPauseDetected() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onPauseDetected");
        }

        if (isRecording.get()) {
            stopListening();
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onPauseDetected: isRecording false");
            }
        }
    }

    @Override
    public void onRecordingStarted() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onRecordingStarted");
        }
        listener.onReadyForSpeech(null);
    }

    @Override
    public void onRecordingEnded() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onRecordingEnded");
        }

        if (doRecordingEnded.get()) {
            doRecordingEnded.set(false);
            listener.onEndOfSpeech();
            listener.onComplete();
            Recognition.setState(Recognition.State.IDLE);
        }
    }

    @Override
    public void onFileWriteComplete(final boolean success) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onFileWriteComplete: " + success);
        }
    }

    /**
     * Stop the recognition.
     */
    public void stopListening() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "called stopRecording");
        }

        if (isRecording.get()) {
            isRecording.set(false);

            new Thread() {
                public void run() {
                    android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);

                    mic.stopRecording();
                    requestObserver.onCompleted();
                    Recognition.setState(Recognition.State.IDLE);

                    final ManagedChannel channel = (ManagedChannel) mApi.getChannel();
                    if (channel != null && !channel.isShutdown()) {
                        try {
                            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                        } catch (final InterruptedException e) {
                            MyLog.e(CLS_NAME, "Error shutting down the gRPC channel.");
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "called stopRecording: isRecording false");
            }
        }
    }


    /**
     * Receives a value from the stream.
     * <p>
     * <p>Can be called many times but is never called after {@link #onError(Throwable)} or {@link
     * #onCompleted()} are called.
     * <p>
     * <p>Unary calls must invoke onNext at most once.  Clients may invoke onNext at most once for
     * server streaming calls, but may receive many onNext callbacks.  Servers may invoke onNext at
     * most once for client streaming calls, but may receive many onNext callbacks.
     * <p>
     * <p>If an exception is thrown by an implementation the caller is expected to terminate the
     * stream by calling {@link #onError(Throwable)} with the caught exception prior to
     * propagating it.
     *
     * @param value the value passed to the stream
     */
    @Override
    public void onNext(final StreamingRecognizeResponse value) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onNext: " + TextFormat.printToString(value));
        }

        final StreamingRecognizeResponse.EndpointerType endpointerType = value.getEndpointerType();

        switch (endpointerType) {

            case START_OF_SPEECH:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onNext: START_OF_SPEECH");
                }
                if (doBeginning.get()) {
                    doBeginning.set(false);
                    listener.onBeginningOfSpeech();
                }
                break;
            case END_OF_SPEECH:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onNext: END_OF_SPEECH");
                }
                if (doEnd.get()) {
                    doEnd.set(false);
                    stopListening();
                }
                break;
            case END_OF_AUDIO:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onNext: END_OF_AUDIO");
                }
                if (doEnd.get()) {
                    doEnd.set(false);
                    stopListening();
                }
                break;
            case END_OF_UTTERANCE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onNext: END_OF_UTTERANCE");
                }
                if (doEnd.get()) {
                    doEnd.set(false);
                    stopListening();
                }
                break;
            case UNRECOGNIZED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onNext: UNRECOGNIZED");
                }
                break;
            case ENDPOINTER_EVENT_UNSPECIFIED:
            default:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onNext: ENDPOINTER_EVENT_UNSPECIFIED");
                }
                break;
        }

        if (doResults.get()) {

            if (UtilsList.notNaked(value.getResultsList())) {

                partialArray.clear();
                resultsArray.clear();
                confidenceArray.clear();
                bundle.clear();

                boolean isFinal = false;
                for (final StreamingRecognitionResult recognitionResult : value.getResultsList()) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "recognitionResult stability: " + recognitionResult.getStability());
                    }

                    isFinal = recognitionResult.getIsFinal();

                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "isFinal: " + isFinal);
                    }

                    for (final SpeechRecognitionAlternative alternative : recognitionResult.getAlternativesList()) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "alternative: " + alternative.getTranscript());
                        }

                        if (isFinal) {
                            resultsArray.add(alternative.getTranscript());
                            confidenceArray.add(alternative.getConfidence());
                        } else {

                            if (partialArray.isEmpty()) {
                                partialArray.add(alternative.getTranscript());
                            } else {
                                partialArray.add(partialArray.get(0) + " " + alternative.getTranscript());
                            }
                        }
                    }
                }

                doResults.set(!isFinal);

                if (isFinal) {
                    bundle.putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, resultsArray);
                    bundle.putFloatArray(SpeechRecognizer.CONFIDENCE_SCORES,
                            ArrayUtils.toPrimitive(confidenceArray.toArray(new Float[0]), 0.0F));
                    listener.onResults(bundle);
                    stopListening();
                } else {
                    bundle.putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, partialArray);
                    listener.onPartialResults(bundle);
                }
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onNext: results list naked");
                }
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onNext: doResults false");
            }
        }
    }

    /**
     * Receives a terminating error from the stream.
     * <p>
     * <p>May only be called once and if called it must be the last method called. In particular if an
     * exception is thrown by an implementation of {@code onError} no further calls to any method are
     * allowed.
     * <p>
     * <p>{@code t} should be a {@link StatusException} or {@link
     * StatusRuntimeException}, but other {@code Throwable} types are possible. Callers should
     * generally convert from a {@link Status} via {@link Status#asException()} or
     * {@link Status#asRuntimeException()}. Implementations should generally convert to a
     * {@code Status} via {@link Status#fromThrowable(Throwable)}.
     *
     * @param throwable the error occurred on the stream
     */
    @Override
    public void onError(final Throwable throwable) {
        if (DEBUG) {
            MyLog.w(CLS_NAME, "onError");
            throwable.printStackTrace();
            final Status status = Status.fromThrowable(throwable);
            MyLog.w(CLS_NAME, "onError: " + status.toString());
        }

        if (doError.get()) {
            doError.set(false);
            stopListening();
            listener.onError(SpeechRecognizer.ERROR_NETWORK);
        }
    }

    /**
     * Receives a notification of successful stream completion.
     * <p>
     * <p>May only be called once and if called it must be the last method called. In particular if an
     * exception is thrown by an implementation of {@code onCompleted} no further calls to any method
     * are allowed.
     */
    @Override
    public void onCompleted() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCompleted");
        }
        Recognition.setState(Recognition.State.IDLE);
    }
}
