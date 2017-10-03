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

package ai.saiy.android.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.support.annotation.NonNull;

import ai.saiy.android.R;
import ai.saiy.android.utils.MyLog;

/**
 * Wrapper class around {@link SoundPool} to handle short sounds
 * <p>
 * Created by benrandall76@gmail.com on 14/09/2016.
 */

public class SaiySoundPool implements SoundPool.OnLoadCompleteListener {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = SaiySoundPool.class.getSimpleName();

    public static final int VOICE_RECOGNITION = 1;
    private static final int MAX_STREAMS = 2;

    private volatile SoundPool sp;

    private int beepStart;
    private int beepStop;
    private boolean beepStopInitialised;
    private boolean beepStartInitialised;

    /**
     * Set up the {@link SoundPool} object, adding the relevant sounds automatically and
     * generating their id for future use.
     *
     * @param ctx  the application context
     * @param type the type of media the {@link SoundPool} will be used for
     */
    public SaiySoundPool setUp(@NonNull final Context ctx, final int type) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            sp = new SoundPool.Builder().setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()).build();
        } else {
            //noinspection deprecation
            sp = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        }

        sp.setOnLoadCompleteListener(this);

        switch (type) {
            case VOICE_RECOGNITION:
                new Thread() {
                    public void run() {
                        beepStart = sp.load(ctx, R.raw.beep_high, 1);
                        beepStop = sp.load(ctx, R.raw.beep_low, 1);
                    }
                }.start();
                break;
            default:
                break;
        }

        return this;
    }

    /**
     * Play an already loaded sound
     *
     * @param soundId the id of the sound generated in {@link #setUp(Context, int)}
     * @return non-zero stream id if successful
     */
    public int play(final int soundId) {

        if (sp == null) {
            return 0;
        }

        return sp.play(soundId, 0.05f, 0.05f, 1, 0, 1f);
    }

    /**
     * Release the {@link SoundPool} and resources
     */
    public void release() {
        sp.release();
        sp = null;
    }

    public int getBeepStart() {
        return beepStart;
    }

    public int getBeepStop() {
        return beepStop;
    }

    public boolean isBeepStartInitialised() {
        return beepStartInitialised;
    }

    public boolean isBeepStopInitialised() {
        return beepStopInitialised;
    }

    /**
     * Called when a sound has completed loading.
     *
     * @param soundPool SoundPool object from the load() method
     * @param sampleId  the sample ID of the sound loaded.
     * @param status    the status of the load operation (0 = success)
     */
    @Override
    public void onLoadComplete(final SoundPool soundPool, final int sampleId, final int status) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onLoadComplete: " + sampleId + " ~ " + status);
        }

        if (sampleId == beepStart) {
            beepStartInitialised = true;
        } else if (sampleId == beepStop) {
            beepStopInitialised = true;
        }
    }
}
