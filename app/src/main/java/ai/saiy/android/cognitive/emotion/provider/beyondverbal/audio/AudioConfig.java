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

package ai.saiy.android.cognitive.emotion.provider.beyondverbal.audio;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class to set the audio configuration details, that will be sent with API requests.
 * <p>
 * Created by benrandall76@gmail.com on 08/06/2016.
 */
public class AudioConfig {

    private static final String TYPE = "type";
    private static final String CHANNELS = "channels";
    private static final String SAMPLE_RATE = "sample_rate";
    private static final String BITS_PER_SAMPLE = "bits_per_sample";
    private static final String AUTO_DETECT = "auto_detect";

    private AudioType type;
    private int channels;
    private int sampleRate;
    private int bitsPerSample;
    private boolean autoDetect;

    /**
     * Default constructor
     */
    public AudioConfig() {
    }

    /**
     * Constructor
     *
     * @param type          the audio type, one of {@link AudioType#PCM} or {@link AudioType#WAV}
     * @param bitsPerSample the bits per sample
     * @param channels      number of channels
     * @param sampleRate    the sampling rate
     * @param autoDetect    ???
     */
    public AudioConfig(@NonNull final AudioType type, final int bitsPerSample,
                       final int channels, final int sampleRate, final boolean autoDetect) {
        this.autoDetect = autoDetect;
        this.bitsPerSample = bitsPerSample;
        this.channels = channels;
        this.sampleRate = sampleRate;
        this.type = type;
    }

    public static AudioConfig getDefault() {
        return new AudioConfig(AudioType.PCM, 16, 1, 8000, true);
    }

    /**
     * Method to prepare the configuration details in a JSON format that the API will accept.
     *
     * @return a JSON formatted representation of the audio configuration
     */
    public JSONObject getConfigJson() {

        final JSONObject object = new JSONObject();

        try {
            object.put(TYPE, getType());
            object.put(CHANNELS, getChannels());
            object.put(SAMPLE_RATE, getSampleRate());
            object.put(BITS_PER_SAMPLE, getBitsPerSample());
            object.put(AUTO_DETECT, isAutoDetect());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object;
    }

    public int getBitsPerSample() {
        return bitsPerSample;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public boolean isAutoDetect() {
        return autoDetect;
    }

    public void setAutoDetect(final boolean autoDetect) {
        this.autoDetect = autoDetect;
    }


    public void setBitsPerSample(final int bitsPerSample) {
        this.bitsPerSample = bitsPerSample;
    }

    public int getChannels() {
        return channels;
    }

    public void setChannels(final int channels) {
        this.channels = channels;
    }

    public void setSampleRate(final int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public AudioType getType() {
        return type;
    }

    public void setType(@NonNull final AudioType type) {
        this.type = type;
    }
}

