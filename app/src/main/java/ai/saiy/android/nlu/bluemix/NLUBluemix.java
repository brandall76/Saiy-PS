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

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by benrandall76@gmail.com on 04/08/2016.
 */

public class NLUBluemix {

    @SerializedName("state")
    private final String state;

    @SerializedName("result_index")
    private final long resultIndex;

    @SerializedName("results")
    private final List<Result> results;

    public NLUBluemix(final long resultIndex, @Nullable final String state, @Nullable final List<Result> results) {
        this.resultIndex = resultIndex;
        this.state = state;
        this.results = results;
    }

    public long getResultIndex() {
        return resultIndex;
    }

    public List<Result> getResults() {
        return results;
    }

    public String getState() {
        return state;
    }
}
