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

package ai.saiy.android.nlu.apiai;

import android.support.annotation.NonNull;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by benrandall76@gmail.com on 03/06/2016.
 */
public class NLUAPIAI {

    private final ArrayList<String> results;
    private final float[] confidence;
    private final String intent;
    private final HashMap<String, JsonElement> parameters;

    public NLUAPIAI(@NonNull final float[] confidence, @NonNull final ArrayList<String> results,
                    @NonNull final String intent, @NonNull final HashMap<String, JsonElement> parameters) {
        this.confidence = confidence;
        this.results = results;
        this.intent = intent;
        this.parameters = parameters;
    }

    public float[] getConfidence() {
        return confidence;
    }

    public ArrayList<String> getResults() {
        return results;
    }

    public String getIntent() {
        return intent;
    }

    public HashMap<String, JsonElement> getParameters() {
        return parameters;
    }
}
