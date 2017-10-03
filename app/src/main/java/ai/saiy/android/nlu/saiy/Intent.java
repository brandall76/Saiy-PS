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
public class Intent {

    @SerializedName("intent")
    private final String intent;


    @SerializedName("confidence")
    private final double confidence;

    @SerializedName("entities")
    private final List<Entity> entities;

    public Intent(final double confidence, @NonNull final String intent, @NonNull final List<Entity> entities) {
        this.confidence = confidence;
        this.intent = intent;
        this.entities = entities;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getIntent() {
        return intent;
    }
}
