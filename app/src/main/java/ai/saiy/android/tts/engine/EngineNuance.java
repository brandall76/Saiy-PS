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

package ai.saiy.android.tts.engine;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.nuance.speechkit.Audio;
import com.nuance.speechkit.AudioPlayer;
import com.nuance.speechkit.Language;
import com.nuance.speechkit.Session;
import com.nuance.speechkit.Transaction;
import com.nuance.speechkit.TransactionException;
import com.nuance.speechkit.Voice;

import ai.saiy.android.tts.SaiyProgressListener;
import ai.saiy.android.tts.TTS;
import ai.saiy.android.utils.MyLog;

/**
 * Class to use the Dragon Nuance SpeechKit for our Text to Speech. The Android sdk demonstrated here
 * is very limited in its functionality. The use of the HTTP interface is a much better and more
 * flexible option, but requires an expensive Gold Membership.
 * <p>
 * BUG - The ability to pause or stop the TTS playback is currently broken
 * <a href="https://developer.nuance.com/phpbb/viewtopic.php?f=16&t=1151&sid=f35fca93a9a735e4fa2ef85abd6d2b64">Pause Stop TTS Playback/a>
 * <p>
 * BUG - Must call Looper.getMainLooper() - Handles Nuance Speech kit failing to call Looper.getMainLooper()
 * <a href="https://developer.nuance.com/phpbb/viewtopic.php?f=16&t=1152&sid=b166db2117e306f594dcacc3972c955b">Can't create handler not called Looper.prepare()/a>
 * <p>
 * Created by benrandall76@gmail.com on 09/03/2016.
 */
public class EngineNuance implements AudioPlayer.Listener {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = EngineNuance.class.getSimpleName();

    private Session speechSession;
    private Transaction ttsTransaction;
    private final Transaction.Options options;
    private final SaiyProgressListener listener;

    private final String utteranceId;

    /**
     * Constructor
     *
     * @param mContext  the application context
     * @param listener  the associated {@link SaiyProgressListener}
     * @param language  the Locale we are using to analyse the voice data. This is not necessarily the
     *                  Locale of the device, as the user may be multi-lingual and have set a custom
     *                  recognition language in a launcher short-cut.
     * @param serverUri the Nuance Server_URI
     * @param appKey    the Nuance APP_KEY
     */
    public EngineNuance(@NonNull final Context mContext, @NonNull final SaiyProgressListener listener,
                        @NonNull final String language, final String languageName,
                        final String utteranceId, @NonNull final Uri serverUri,
                        @NonNull final String appKey) {
        this.listener = listener;
        this.utteranceId = utteranceId;

        if (DEBUG) {
            MyLog.i(CLS_NAME, "language: " + language);
            MyLog.i(CLS_NAME, "languageName: " + languageName);
        }

        options = new Transaction.Options();
        options.setLanguage(new Language(language));
        options.setAutoplay(false);

        if (languageName != null) {
            options.setVoice(new Voice(languageName));
        }

        try {
            speechSession = Session.Factory.session(mContext.getApplicationContext(), serverUri, appKey);
            speechSession.getAudioPlayer().setListener(this);
        } catch (final RuntimeException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "RuntimeException");
                e.printStackTrace();
            }

            this.listener.onError(utteranceId, 0);
        }

    }

    /**
     * Stop the speech
     * <p>
     * BUG - this functionality is broken in the latest SDK
     * <a href="https://developer.nuance.com/phpbb/viewtopic.php?f=16&t=1151&sid=f35fca93a9a735e4fa2ef85abd6d2b64">Pause Stop TTS Playback/a>
     */
    public void stopSpeech() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "stopSpeech");
        }

        try {
            speechSession.getAudioPlayer().pause();
            speechSession.getAudioPlayer().stop();
        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "stopSpeech: NullPointerException");
                e.printStackTrace();
            }
        } catch (final IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "stopSpeech: IllegalStateException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "stopSpeech: Exception");
                e.printStackTrace();
            }
        }

        try {
            ttsTransaction.cancel();
            ttsTransaction.stopRecording();
        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "stopSpeech: ttsTransaction NullPointerException");
                e.printStackTrace();
            }
        } catch (final IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "stopSpeech: ttsTransaction IllegalStateException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "stopSpeech: ttsTransaction Exception");
                e.printStackTrace();
            }
        } finally {
            TTS.setState(TTS.State.IDLE);
        }
    }

    /**
     * TODO - Extract listener to handle utterances of length greater than 500 characters that need to be looped
     * <p>
     * Start the speech
     *
     * @param utterance to speak
     */
    public void startSpeech(@NonNull final String utterance) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "startSpeech");
        }

        ttsTransaction = speechSession.speakString(utterance, options, new Transaction.Listener() {
            @Override
            public void onAudio(final Transaction transaction, final Audio audio) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAudio");
                }
                speechSession.getAudioPlayer().playAudio(audio);
            }

            @Override
            public void onSuccess(final Transaction transaction, final String s) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onSuccess");
                }
            }

            @Override
            public void onError(final Transaction transaction, final String s, final TransactionException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "onError: " + e.getMessage() + ". " + s);
                    e.printStackTrace();
                }

                TTS.setState(TTS.State.IDLE);

                // TODO - more verbose error handling
                listener.onError(utteranceId, 0);
                ttsTransaction = null;
            }

        });
    }

    @Override
    public void onBeginPlaying(final AudioPlayer audioPlayer, final Audio audio) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onBeginPlaying");
        }

        TTS.setState(TTS.State.SPEAKING);
        listener.onStart(utteranceId);
    }

    @Override
    public void onFinishedPlaying(final AudioPlayer audioPlayer, final Audio audio) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onFinishedPlaying");
        }

        TTS.setState(TTS.State.IDLE);
        listener.onDone(utteranceId);
        ttsTransaction = null;
        speechSession = null;
    }
}
