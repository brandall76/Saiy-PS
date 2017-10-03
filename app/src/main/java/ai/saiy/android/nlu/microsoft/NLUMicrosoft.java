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

import java.util.List;

/**
 * Created by benrandall76@gmail.com on 20/05/2016.
 */
public class NLUMicrosoft {

    public static final float MIN_THRESHOLD = 0.75f;

    @SerializedName("query")
    private final String query;

    @SerializedName("entities")
    private final List<Entity> entities;

    @SerializedName("intents")
    private final List<Intent> intents;

    public NLUMicrosoft(@NonNull final List<Entity> entities, @NonNull final String query,
                        @NonNull final List<Intent> intents) {
        this.entities = entities;
        this.query = query;
        this.intents = intents;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public List<Intent> getIntents() {
        return intents;
    }

    public String getQuery() {
        return query;
    }
}
