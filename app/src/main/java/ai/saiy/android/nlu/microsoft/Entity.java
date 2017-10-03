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

package ai.saiy.android.nlu.microsoft;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * Created by benrandall76@gmail.com on 20/05/2016.
 */
public class Entity {

    @SerializedName("entity")
    private final String entity;

    @SerializedName("type")
    private final String type;

    @SerializedName("startIndex")
    private final long startIndex;

    @SerializedName("endIndex")
    private final long endIndex;

    @SerializedName("score")
    private final double score;

    public Entity(final long endIndex, @NonNull final String entity, @NonNull final String type,
                  final long startIndex, final double score) {
        this.endIndex = endIndex;
        this.entity = entity;
        this.type = type;
        this.startIndex = startIndex;
        this.score = score;
    }

    public long getEndIndex() {
        return endIndex;
    }

    public String getEntity() {
        return entity;
    }

    public double getScore() {
        return score;
    }

    public long getStartIndex() {
        return startIndex;
    }

    public String getType() {
        return type;
    }
}
