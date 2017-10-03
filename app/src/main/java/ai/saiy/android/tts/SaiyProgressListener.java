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

package ai.saiy.android.tts;


import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

/**
 * Listener for events relating to the progress of an utterance through
 * the synthesis queue. Each utterance is associated with a call to
 * {@link TextToSpeech#speak} or {@link TextToSpeech#synthesizeToFile} with an
 * associated utterance identifier, as per {@link TextToSpeech.Engine#KEY_PARAM_UTTERANCE_ID}.
 * <p>
 * The callbacks specified in this method can be called from multiple threads.
 * <p>
 * Created by benrandall76@gmail.com on 09/03/2016.
 */
public abstract class SaiyProgressListener extends UtteranceProgressListener {

    public void onAudioAvailable(final String utteranceId, final byte[] audio) {
    }

    public void onBeginSynthesis(final String utteranceId, final int sampleRateInHz, final int audioFormat,
                                 final int channelCount) {
     }

    /**
     * Called when an utterance "starts" as perceived by the caller. This will
     * be soon before audio is played back in the case of a {@link TextToSpeech#speak}
     * or before the first bytes of a file are written to storage in the case
     * of {@link TextToSpeech#synthesizeToFile}.
     *
     * @param utteranceId the utterance ID of the utterance.
     */
    public abstract void onStart(String utteranceId);

    /**
     * Called when an utterance has successfully completed processing.
     * All audio will have been played back by this point for audible output, and all
     * output will have been written to disk for file synthesis requests.
     * <p>
     * This request is guaranteed to be called after {@link #onStart(String)}.
     *
     * @param utteranceId the utterance ID of the utterance.
     */
    public abstract void onDone(String utteranceId);

    /**
     * Called when an error has occurred during processing. This can be called
     * at any point in the synthesis process. Note that there might be calls
     * to {@link #onStart(String)} for specified utteranceId but there will never
     * be a call to both {@link #onDone(String)} and this method for
     * the same utterance.
     *
     * @param utteranceId the utterance ID of the utterance.
     * @deprecated Use {@link #onError(String, int)} instead
     */
    @Deprecated
    public abstract void onError(String utteranceId);

    /**
     * Called when an error has occurred during processing. This can be called
     * at any point in the synthesis process. Note that there might be calls
     * to {@link #onStart(String)} for specified utteranceId but there will never
     * be a call to both {@link #onDone(String)} and {@link #onError(String, int)} for
     * the same utterance. The default implementation calls {@link #onError(String)}.
     *
     * @param utteranceId the utterance ID of the utterance.
     * @param errorCode   one of the ERROR_* codes from {@link TextToSpeech}
     */
    @SuppressWarnings("deprecation")
    public void onError(String utteranceId, int errorCode) {
        onError(utteranceId);
    }

    /**
     * Called when an utterance has been stopped while in progress or flushed from the
     * synthesis queue. This can happen if a client calls {@link TextToSpeech#stop()}
     * or uses {@link TextToSpeech#QUEUE_FLUSH} as an argument with the
     * {@link TextToSpeech#speak} or {@link TextToSpeech#synthesizeToFile} methods.
     *
     * @param utteranceId the utterance ID of the utterance.
     * @param interrupted If true, then the utterance was interrupted while being synthesized
     *                    and its output is incomplete. If false, then the utterance was flushed
     *                    before the synthesis started.
     */
    public void onStop(String utteranceId, boolean interrupted) {
    }

    /**
     * Wraps an old deprecated OnUtteranceCompletedListener with a shiny new
     * progress listener.
     */
    @SuppressWarnings("deprecation")
    static UtteranceProgressListener from(
            final TextToSpeech.OnUtteranceCompletedListener listener) {
        return new UtteranceProgressListener() {
            @Override
            public synchronized void onDone(String utteranceId) {
                listener.onUtteranceCompleted(utteranceId);
            }

            @Override
            public void onError(String utteranceId) {
                listener.onUtteranceCompleted(utteranceId);
            }

            @Override
            public void onStart(String utteranceId) {
                // Left unimplemented, has no equivalent in the old
                // API.
            }

            @Override
            public void onStop(String utteranceId, boolean interrupted) {
                listener.onUtteranceCompleted(utteranceId);
            }
        };
    }
}
