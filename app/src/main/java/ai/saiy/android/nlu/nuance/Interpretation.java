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

package ai.saiy.android.nlu.nuance;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * Created by benrandall76@gmail.com on 20/05/2016.
 */
public class Interpretation {

    @SerializedName("literal")
    private final String literal;

    @SerializedName("action")
    private final Action action;

    @SerializedName("concepts")
    private final Map<String, List<Concept>> concept;

    public Interpretation(@NonNull final Action action, @NonNull final Map<String, List<Concept>> concept,
                          @NonNull final String literal) {
        this.action = action;
        this.concept = concept;
        this.literal = literal;
    }

    public Action getAction() {
        return action;
    }

    public Map<String, List<Concept>> getConcept() {
        return concept;
    }

    public String getLiteral() {
        return literal;
    }
}
