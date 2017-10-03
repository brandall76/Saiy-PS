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

package ai.saiy.android.cognitive.emotion.provider.beyondverbal.analysis;

import com.google.gson.annotations.SerializedName;

/**
 * Helper class to serialise the JSON response from Beyond Verbal
 * <p>
 * Created by benrandall76@gmail.com on 10/06/2016.
 */
public class Analysis {

    public static final String LOW = "low";
    public static final String MED = "med";
    public static final String HIGH = "high";

    @SerializedName("Temper")
    private final Temper temper;

    @SerializedName("Valence")
    private final Valence valence;

    @SerializedName("Gender")
    private final Gender gender;

    @SerializedName("Mood")
    private final Mood mood;

    @SerializedName("Arousal")
    private final Arousal arousal;

    @SerializedName("AudioQuality")
    private final AudioQuality audioQuality;

    public Analysis(final Gender gender, final Temper temper, final Valence valence, final Mood mood,
                    final AudioQuality audioQuality, final Arousal arousal) {
        this.gender = gender;
        this.temper = temper;
        this.valence = valence;
        this.mood = mood;
        this.audioQuality = audioQuality;
        this.arousal = arousal;
    }

    public Arousal getArousal() {
        return arousal;
    }

    public AudioQuality getAudioQuality() {
        return audioQuality;
    }

    public Gender getGender() {
        return gender;
    }

    public Mood getMood() {
        return mood;
    }

    public Temper getTemper() {
        return temper;
    }

    public Valence getValence() {
        return valence;
    }
}
