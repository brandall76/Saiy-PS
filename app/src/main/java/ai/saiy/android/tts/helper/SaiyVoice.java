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

package ai.saiy.android.tts.helper;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Parcel;
import android.speech.tts.Voice;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import ai.saiy.android.tts.attributes.Gender;

/**
 * Created by benrandall76@gmail.com on 19/08/2016.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class SaiyVoice extends Voice {

    private String engine;
    private Gender gender = Gender.UNDEFINED;

    public SaiyVoice(@NonNull final Voice voice) {
        super(voice.getName(), voice.getLocale(), voice.getQuality(), voice.getLatency(), voice.isNetworkConnectionRequired(),
                voice.getFeatures());
    }

    public static Set<SaiyVoice> getSaiyVoices(@NonNull final Set<Voice> voiceSet, @NonNull final String initialisedEngine) {

        final Set<SaiyVoice> saiyVoiceSet = new HashSet<>(voiceSet.size());

        if (initialisedEngine.matches(TTSDefaults.TTS_PKG_NAME_GOOGLE)) {
            final TTSDefaults.Google[] googleList = TTSDefaults.Google.values();

            SaiyVoice saiyVoice;
            String voicePattern;
            for (final Voice voice : voiceSet) {
                saiyVoice = new SaiyVoice(voice);
                saiyVoice.setEngine(initialisedEngine);

                voicePattern = Pattern.quote(voice.getName());
                for (final TTSDefaults.Google g : googleList) {
                    if (g.getVoiceName().matches(voicePattern)) {
                        saiyVoice.setGender(g.getGender());
                        break;
                    }
                }

                saiyVoiceSet.add(saiyVoice);
            }
        } else {

            SaiyVoice saiyVoice;
            for (final Voice voice : voiceSet) {
                saiyVoice = new SaiyVoice(voice);
                saiyVoice.setEngine(initialisedEngine);
                saiyVoice.setGender(Gender.getGenderFromVoiceName(voice.getName()));
                saiyVoiceSet.add(saiyVoice);
            }
        }

        return saiyVoiceSet;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(@NonNull final Gender gender) {
        this.gender = gender;
    }

    public void setGender(@NonNull final String voiceName) {

        if (this.engine != null) {
            if (TTSDefaults.pTTS_PKG_NAME_GOOGLE.matcher(this.engine).matches()) {
                this.gender = TTSDefaults.Google.getGender(voiceName);
            } else {
                this.gender = Gender.getGenderFromVoiceName(voiceName);
            }
        } else {
            this.gender = Gender.UNDEFINED;
        }
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(@NonNull final String engine) {
        this.engine = engine;
    }

    public static class VoiceComparator implements Comparator<Voice> {

        @Override
        public int compare(final Voice v1, final Voice v2) {
            return v1.getLocale().toString().compareTo(v2.getLocale().toString());
        }
    }

    public static class SaiyVoiceComparator implements Comparator<SaiyVoice> {

        @Override
        public int compare(final SaiyVoice v1, final SaiyVoice v2) {
            return v1.getLocale().toString().compareTo(v2.getLocale().toString());
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        return builder.append("SaiyVoice[Name: ").append(getName())
                .append(", locale: ").append(getLocale())
                .append(", quality: ").append(getQuality())
                .append(", latency: ").append(getLatency())
                .append(", requiresNetwork: ").append(isNetworkConnectionRequired())
                .append(", features: ").append(getFeatures().toString())
                .append(", engine: ").append(engine)
                .append(", gender: ").append(gender.name())
                .append("]").toString();
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(getName());
        dest.writeSerializable(getLocale());
        dest.writeInt(getQuality());
        dest.writeInt(getLatency());
        dest.writeByte((byte) (isNetworkConnectionRequired() ? 1 : 0));
        dest.writeStringList(new ArrayList<>(getFeatures()));
        dest.writeString(engine);
        dest.writeSerializable(gender);
    }
}
