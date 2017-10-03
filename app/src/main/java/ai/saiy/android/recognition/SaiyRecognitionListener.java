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

package ai.saiy.android.recognition;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import ai.saiy.android.partial.IPartial;
import ai.saiy.android.utils.MyLog;

/**
 * Class to handle the current bugs in the SpeechRecognizer implementation of Google 'Now'.
 * <p>
 * The boolean markers make sure that the {@link RecognitionListener} provides callbacks in the
 * correct order and ignores the ones that are caused by bugs.
 * <p>
 * Created by benrandall76@gmail.com on 23/04/2016.
 */
public class SaiyRecognitionListener implements RecognitionListener, IPartial {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = SaiyRecognitionListener.class.getSimpleName();

    private boolean doError;
    private boolean doEndOfSpeech;
    private boolean doBeginningOfSpeech;

    public void resetBugVariables() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "resetBugVariables");
        }

        doError = false;
        doEndOfSpeech = false;
        doBeginningOfSpeech = false;
    }

    /**
     * Called when the endpointer is ready for the user to start speaking.
     *
     * @param params parameters set by the recognition service. Reserved for future use.
     */
    @Override
    public void onReadyForSpeech(final Bundle params) {
        doError = true;
        doEndOfSpeech = true;
        doBeginningOfSpeech = true;
    }

    /**
     * The user has started to speak.
     */
    @Override
    public void onBeginningOfSpeech() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onBeginningOfSpeech: doEndOfSpeech: " + doEndOfSpeech);
            MyLog.i(CLS_NAME, "onBeginningOfSpeech: doError: " + doError);
            MyLog.i(CLS_NAME, "onBeginningOfSpeech: doBeginningOfSpeech: " + doBeginningOfSpeech);
        }

        if (doBeginningOfSpeech) {
            doBeginningOfSpeech = false;
            onBeginningOfRecognition();
        }

    }

    public void onBeginningOfRecognition() {
    }

    /**
     * The sound level in the audio stream has changed. There is no guarantee that this method will
     * be called.
     *
     * @param rmsdB the new RMS dB value
     */
    @Override
    public void onRmsChanged(final float rmsdB) {
    }

    /**
     * More sound has been received. The purpose of this function is to allow giving feedback to the
     * user regarding the captured audio. There is no guarantee that this method will be called.
     *
     * @param buffer a buffer containing a sequence of big-endian 16-bit integers representing a
     *               single channel audio stream. The sample rate is implementation dependent.
     */
    @Override
    public void onBufferReceived(final byte[] buffer) {
    }

    /**
     * Called after the user stops speaking.
     */
    @Override
    public void onEndOfSpeech() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onEndOfSpeech: doEndOfSpeech: " + doEndOfSpeech);
            MyLog.i(CLS_NAME, "onEndOfSpeech: doError: " + doError);
            MyLog.i(CLS_NAME, "onEndOfSpeech: doBeginningOfSpeech: " + doBeginningOfSpeech);
        }

        if (doEndOfSpeech) {
            onEndOfRecognition();
        }
    }

    public void onEndOfRecognition() {
    }

    /**
     * A network or recognition error occurred.
     *
     * @param error code is defined in {@link SpeechRecognizer}
     */
    @Override
    public void onError(final int error) {
        if (DEBUG) {
            MyLog.w(CLS_NAME, "onError: " + error);
            MyLog.w(CLS_NAME, "onError: doEndOfSpeech: " + doEndOfSpeech);
            MyLog.w(CLS_NAME, "onError: doError: " + doError);
            MyLog.i(CLS_NAME, "onError: doBeginningOfSpeech: " + doBeginningOfSpeech);
        }

        if (error != SpeechRecognizer.ERROR_NO_MATCH) {
            doError = true;
        }

        if (doError) {
            onRecognitionError(error);
        }
    }


    /**
     * A network or recognition error occurred.
     *
     * @param error code is defined in {@link SpeechRecognizer}
     */
    public void onRecognitionError(final int error) {
    }

    /**
     * Called when recognition results are ready.
     *
     * @param results the recognition results. To retrieve the results in {@code
     *                ArrayList&lt;String&gt;} format use {@link Bundle#getStringArrayList(String)} with
     *                {@link SpeechRecognizer#RESULTS_RECOGNITION} as a parameter. A float array of
     *                confidence values might also be given in {@link SpeechRecognizer#CONFIDENCE_SCORES}.
     */
    @Override
    public void onResults(final Bundle results) {
    }

    public void onComplete() {

    }

    /**
     * Called when partial recognition results are available. The callback might be called at any
     * time between {@link #onBeginningOfSpeech()} and {@link #onResults(Bundle)} when partial
     * results are ready. This method may be called zero, one or multiple times for each call to
     * {@link SpeechRecognizer#startListening(Intent)}, depending on the speech recognition
     * service implementation.  To request partial results, use
     * {@link RecognizerIntent#EXTRA_PARTIAL_RESULTS}
     *
     * @param partialResults the returned results. To retrieve the results in
     *                       ArrayList&lt;String&gt; format use {@link Bundle#getStringArrayList(String)} with
     *                       {@link SpeechRecognizer#RESULTS_RECOGNITION} as a parameter
     */
    @Override
    public void onPartialResults(final Bundle partialResults) {
    }

    /**
     * Reserved for adding future events.
     *
     * @param eventType the type of the occurred event
     * @param params    a Bundle containing the passed parameters
     */
    @Override
    public void onEvent(final int eventType, final Bundle params) {
    }

    @Override
    public void onCancelDetected() {
    }

    @Override
    public void onTranslateDetected() {
    }
}
