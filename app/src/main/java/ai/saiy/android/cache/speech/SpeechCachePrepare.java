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

package ai.saiy.android.cache.speech;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Process;
import android.speech.tts.Voice;
import android.support.annotation.NonNull;

import ai.saiy.android.audio.AudioCompression;
import ai.saiy.android.database.DBSpeech;

/**
 * Class to prepare an entry into {@link DBSpeech}. The method {@link #setUncompressedAudio(byte[])}
 * is passed uncompressed audio data, which is subsequently compressed with a callback of completion
 * coming from the implemented {@link IAudioCompression} interface.
 * <p/>
 * Created by benrandall76@gmail.com on 27/04/2016.
 */
public class SpeechCachePrepare implements IAudioCompression {

    private final Context mContext;
    private String engine;
    private String utterance;
    private String locale;
    private Voice voice;
    private volatile byte[] compressedAudio;

    /**
     * Constructor
     *
     * @param mContext the application context
     */
    public SpeechCachePrepare(@NonNull final Context mContext) {
        this.mContext = mContext;
    }

    /**
     * Set the uncompressed audio
     *
     * @param uncompressedAudio byte[]
     */
    public void setUncompressedAudio(@NonNull final byte[] uncompressedAudio) {
        new Thread() {
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
                AudioCompression.compressBytes(SpeechCachePrepare.this, uncompressedAudio);
            }
        }.start();
    }

    public byte[] getCompressedAudio() {
        return compressedAudio;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(@NonNull final String engine) {
        this.engine = engine;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(@NonNull final String locale) {
        this.locale = locale;
    }

    public String getUtterance() {
        return utterance;
    }

    public void setUtterance(@NonNull final String utterance) {
        this.utterance = utterance;
    }

    public Voice getVoice() {
        return voice;
    }

    public void setVoice(@NonNull final Voice voice) {
        this.voice = voice;
    }

    @Override
    public void onCompressionCompleted(final byte[] compressedAudio) {
        this.compressedAudio = compressedAudio;
        executeInsert();
    }

    /**
     * Execute the insertion of the audio data into {@link DBSpeech}
     */
    private void executeInsert() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
                final DBSpeech dbSpeech = new DBSpeech(mContext);
                dbSpeech.insertRow(SpeechCachePrepare.this);
            }
        });
    }
}
