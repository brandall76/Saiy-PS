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

package ai.saiy.android.nlu.bluemix;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * Created by benrandall76@gmail.com on 04/08/2016.
 */

public class Alternative {

    @SerializedName("transcript")
    private final String transcript;

    @SerializedName("confidence")
    private final float confidence;

    public Alternative(final float confidence, @NonNull final String transcript) {
        this.confidence = confidence;
        this.transcript = transcript;
    }

    public float getConfidence() {
        return confidence;
    }

    public String getTranscript() {
        return transcript;
    }
}
