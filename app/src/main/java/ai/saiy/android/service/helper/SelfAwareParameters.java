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

package ai.saiy.android.service.helper;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Pair;
import android.widget.Toast;

import java.util.HashMap;

import ai.saiy.android.R;
import ai.saiy.android.sound.VolumeHelper;
import ai.saiy.android.tts.SaiyProgressListener;
import ai.saiy.android.tts.SaiyTextToSpeech;
import ai.saiy.android.tts.helper.TTSDefaults;
import ai.saiy.android.utils.Conditions.Network;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsString;

/**
 * Helper class to manage the ({@link SaiyTextToSpeech} parameters.
 * <p/>
 * Created by benrandall76@gmail.com on 20/03/2016.
 */
public class SelfAwareParameters extends HashMap<String, String> {

    // Unused
    private static final long serialVersionUID = 516920492195138564L;

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = SelfAwareParameters.class.getSimpleName();

    private final Context mContext;
    private final SelfAwareParameters21 bundle;

    /**
     * Constructor
     *
     * @param mContext the application context
     */
    public SelfAwareParameters(@NonNull final Context mContext) {
        super(new HashMap<String, String>());
        this.mContext = mContext.getApplicationContext();
        bundle = new SelfAwareParameters21();
    }

    public Bundle getBundle() {
        return bundle.getBundle();
    }

    /**
     * Check if the user wants to use network voice engines and if the minimum required connection
     * speed is met.
     *
     * @return true if conditions are satisfied.
     */
    public boolean shouldNetwork() {
        return Network.shouldTTSNetwork(mContext);
    }


    /**
     * Check if the Text to Speech request requires a network synthesised voice.
     *
     * @return true if network synthesis is required. False otherwise.
     */
    @SuppressWarnings("deprecation")
    public boolean isNetworkAllowed() {

        final String networkAllowed = get(TextToSpeech.Engine.KEY_FEATURE_NETWORK_SYNTHESIS);

        if (networkAllowed != null) {
            return Boolean.parseBoolean(networkAllowed);
        } else {
            return SPH.getNetworkSynthesis(mContext);
        }
    }

    /**
     * Get the volume the Text to Speech object should use. This can be manipulated by the user, if they
     * prefer a certain volume.
     *
     * @return the value the user has chosen or the default.
     */
    public float getVolume() {

        final Pair<Boolean, Float> volumePair = VolumeHelper.getUserMediaVolumeValue(mContext);

        if (volumePair.first) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getVolume: volumePair.second: " + volumePair.second);
            }

            if (volumePair.second < 0.25F) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext,
                                    mContext.getString(R.string.error_tts_volume), Toast.LENGTH_SHORT).show();
                        }
                    });
            }

            return volumePair.second;
        } else {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext,
                                mContext.getString(R.string.error_tts_volume), Toast.LENGTH_SHORT).show();
                    }
                });
        }

        return 1F;
    }

    /**
     * Currently constant pan value
     *
     * @return the pan value
     */
    private float getPan() {
        return 0;
    }

    /**
     * Set the parameters the {@link SaiyTextToSpeech} engine will use. This
     * is now deprecated, however, currently most voice engines are not responding correctly to the
     * usage of the new APIs
     *
     * @param isSpeakListen whether the speech should start a recognition request on completion
     * @param conditions    the {@link SelfAwareConditions}
     */
    @SuppressWarnings("deprecation")
    public void setParams(final boolean isSpeakListen, @NonNull final SelfAwareConditions conditions) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "setParams: isSpeakListen: " + isSpeakListen);
        }

        if (isSpeakListen) {
            putObject(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, String.valueOf(LocalRequest.ACTION_SPEAK_LISTEN));
        } else {
            putObject(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, String.valueOf(LocalRequest.ACTION_SPEAK_ONLY));
        }

        putObject(TextToSpeech.Engine.KEY_PARAM_VOLUME, getVolume());
        putObject(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC);
        putObject(TextToSpeech.Engine.KEY_PARAM_PAN, getPan());

        switch (conditions.getDefaultTTS()) {

            case LOCAL:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "SpeechDefault.LOCAL");
                }
                putObject(TextToSpeech.Engine.KEY_FEATURE_NETWORK_SYNTHESIS, String.valueOf(shouldNetwork()));
                break;
            case NETWORK_NUANCE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "SpeechDefault.NETWORK_NUANCE");
                }
                break;
        }
    }

    /**
     * Check if the current utterance is part of an array of speech. If so, the
     * {@link SaiyProgressListener} will only react to certain positions in
     * the array of speech. The utterance id may have been altered during the array of speech with
     * one of {@link SaiyTextToSpeech#ARRAY_FIRST} {@link SaiyTextToSpeech#ARRAY_INTERIM}
     * {@link SaiyTextToSpeech#ARRAY_LAST}
     *
     * @param utteranceId the current utterance id
     * @return true if the {@link SaiyProgressListener} should react
     */
    public boolean validateOnStart(@NonNull final String utteranceId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "validateOnStart: " + utteranceId);
        }

        if (get(TTSDefaults.EXTRA_INTERRUPTED) != null || utteranceId.startsWith(SaiyTextToSpeech.ARRAY_FIRST)
                || utteranceId.startsWith(SaiyTextToSpeech.ARRAY_SINGLE)) {
            remove(TTSDefaults.EXTRA_INTERRUPTED);
            if (DEBUG) {
                MyLog.i(CLS_NAME, "validateOnStart: ARRAY_FIRST/EXTRA_INTERRUPTED/ARRAY_SINGLE");
            }
            return true;
        } else if (utteranceId.startsWith(SaiyTextToSpeech.ARRAY_INTERIM)
                || utteranceId.startsWith(SaiyTextToSpeech.ARRAY_LAST)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "validateOnStart: ARRAY_INTERIM/ARRAY_LAST");
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * Check if the current utterance is part of an array of speech. If so, the
     * {@link SaiyProgressListener} will only react to certain positions in
     * the array of speech. The utterance id may have been altered during the array of speech with
     * {@link SaiyTextToSpeech#ARRAY_LAST} appended with the true utterance id.
     *
     * @param utteranceId the current utterance id
     * @return a Pair with the first parameter denoting if the {@link SaiyProgressListener}
     * should react and the second the actual utterance id.
     */
    public Pair<Boolean, String> validateOnDone(@NonNull final String utteranceId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "validateOnDone: " + utteranceId);
        }

        if (get(TTSDefaults.EXTRA_INTERRUPTED) != null || utteranceId.startsWith(SaiyTextToSpeech.ARRAY_LAST)
                || utteranceId.startsWith(SaiyTextToSpeech.ARRAY_SINGLE)) {
            remove(TTSDefaults.EXTRA_INTERRUPTED);
            if (DEBUG) {
                MyLog.i(CLS_NAME, "validateOnDone: ARRAY_LAST/EXTRA_INTERRUPTED/ARRAY_SINGLE");
            }

            if (get(TTSDefaults.EXTRA_INTERRUPTED_FORCED) != null) {
                remove(TTSDefaults.EXTRA_INTERRUPTED_FORCED);
                return new Pair<>(true, String.valueOf(LocalRequest.ACTION_SPEAK_ONLY));
            } else if (utteranceId.contains(SaiyTextToSpeech.ARRAY_DELIMITER)) {
                final String extracted = TextUtils.split(utteranceId, SaiyTextToSpeech.ARRAY_DELIMITER)[1];
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "validateOnDone: extracted: " + extracted);
                }
                return new Pair<>(true, extracted);
            } else {
                return new Pair<>(true, utteranceId);
            }

        } else if (utteranceId.startsWith(SaiyTextToSpeech.ARRAY_FIRST)
                || utteranceId.startsWith(SaiyTextToSpeech.ARRAY_INTERIM)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "validateOnDone: ARRAY_FIRST/ARRAY_INTERIM");
            }
            return new Pair<>(false, null);
        } else {
            return new Pair<>(true, utteranceId);
        }
    }

    /**
     * Set the utterance id
     *
     * @param utteranceId the utterance id to set
     */
    public void setUtteranceId(@NonNull final String utteranceId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "setUtteranceId: " + utteranceId);
        }

        putObject(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
    }

    /**
     * Get the id of the most recent utterance
     *
     * @return the utterance id
     */
    public String getUtteranceId() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getUtteranceId");
        }

        final String utteranceId = get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);

        if (DEBUG) {
            MyLog.i(CLS_NAME, "getUtteranceId: " + utteranceId);
        }

        if (UtilsString.notNaked(utteranceId)) {
            return utteranceId;
        }

        return String.valueOf(LocalRequest.ACTION_SPEAK_ONLY);
    }

    protected void putObject(@NonNull final String key, @NonNull final Object value) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            if (value instanceof String) {
                bundle.putString(key, String.valueOf(value));
            } else if (value instanceof Integer) {
                bundle.putInt(key, (Integer) value);
            } else if (value instanceof Float) {
                bundle.putFloat(key, (Float) value);
            } else if (value instanceof Double) {
                bundle.putDouble(key, (Double) value);
            } else if (value instanceof Boolean) {
                bundle.putBoolean(key, (Boolean) value);
            }
        } else {
            super.put(key, String.valueOf(value));
        }
    }

    @Override
    public String get(final Object key) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return bundle.get(key);
        } else {
            return super.get(key);
        }
    }

    @Override
    public String remove(final Object key) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return bundle.remove(key);
        } else {
            return super.remove(key);
        }
    }

    @Override
    public void clear() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bundle.clear();
        } else {
            super.clear();
        }
    }
}
