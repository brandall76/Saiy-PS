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

package ai.saiy.android.nlu.wit;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * Created by benrandall76@gmail.com on 04/08/2016.
 */

public class MessageBody {

    @SerializedName("type")
    private final String type;

    @SerializedName("value")
    private final String value;

    @SerializedName("confidence")
    private final float confidence;

    @SerializedName("suggested")
    private final boolean suggested;

    public MessageBody(final float confidence, @NonNull final String type, @NonNull final String value,
                       final boolean suggested) {
        this.confidence = confidence;
        this.type = type;
        this.value = value;
        this.suggested = suggested;
    }

    public float getConfidence() {
        return confidence;
    }

    public boolean isSuggested() {
        return suggested;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
