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

import android.media.AudioFormat;
import android.media.MediaRecorder;

/**
 * Created by benrandall76@gmail.com on 12/08/2016.
 */

public class AudioParameters {

    private int nChannels;
    private int bSamples;
    private int audioSource;
    private int sampleRateInHz;
    private int channelConfig;
    private int audioFormat;

    public AudioParameters() {
    }

    public AudioParameters(final int audioFormat, final int audioSource, final int channelConfig,
                           final int nChannels, final int sampleRateInHz, final int bSamples) {
        this.audioFormat = audioFormat;
        this.audioSource = audioSource;
        this.channelConfig = channelConfig;
        this.nChannels = nChannels;
        this.sampleRateInHz = sampleRateInHz;
        this.bSamples = bSamples;
    }

    public static AudioParameters getDefault() {
        return new AudioParameters(AudioFormat.ENCODING_PCM_16BIT,
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                AudioFormat.CHANNEL_IN_MONO, 1, 16000, 16);
    }

    public static AudioParameters getDefaultBeyondVerbal(){
        return new AudioParameters(AudioFormat.ENCODING_PCM_16BIT,
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                AudioFormat.CHANNEL_IN_MONO, 1, 8000, 16);
    }

    public static AudioParameters getDefaultMicrosoft(){
        return new AudioParameters(AudioFormat.ENCODING_PCM_16BIT,
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                AudioFormat.CHANNEL_IN_MONO, 1, 16000, 16);
    }

    public int getbSamples() {
        return bSamples;
    }

    public void setbSamples(final int bSamples) {
        this.bSamples = bSamples;
    }

    public int getAudioFormat() {
        return audioFormat;
    }

    public void setAudioFormat(final int audioFormat) {
        this.audioFormat = audioFormat;
    }

    public int getAudioSource() {
        return audioSource;
    }

    public void setAudioSource(final int audioSource) {
        this.audioSource = audioSource;
    }

    public int getChannelConfig() {
        return channelConfig;
    }

    public void setChannelConfig(final int channelConfig) {
        this.channelConfig = channelConfig;
    }

    public int getnChannels() {
        return nChannels;
    }

    public void setnChannels(final int nChannels) {
        this.nChannels = nChannels;
    }

    public int getSampleRateInHz() {
        return sampleRateInHz;
    }

    public void setSampleRateInHz(final int sampleRateInHz) {
        this.sampleRateInHz = sampleRateInHz;
    }
}
