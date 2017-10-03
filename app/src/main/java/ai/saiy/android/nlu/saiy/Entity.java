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

package ai.saiy.android.nlu.saiy;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by benrandall76@gmail.com on 03/06/2016.
 */
public class Entity {

    @SerializedName("name")
    private final String name;

    @SerializedName("value")
    private final String value;

    @SerializedName("contextual")
    private final List<Context> contextual;

    @SerializedName("index")
    private final int[] index;

    @SerializedName("confidence")
    private final double confidence;

    public Entity(final double confidence, @NonNull final String name, @NonNull final String value,
                  @NonNull final List<Context> contextual, @NonNull final int[] index) {
        this.confidence = confidence;
        this.name = name;
        this.value = value;
        this.contextual = contextual;
        this.index = index;
    }

    public double getConfidence() {
        return confidence;
    }

    public List<Context> getContextual() {
        return contextual;
    }

    public int[] getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
