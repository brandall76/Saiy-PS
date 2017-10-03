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

package ai.saiy.android.cognitive.knowledge.provider.wolframalpha.resolve;

import android.support.annotation.NonNull;

/**
 * Class to package up a Wolfram Alpha request
 * <p>
 * Created by benrandall76@gmail.com on 09/08/2016.
 */

public class WolframAlphaRequest {

    public enum Type {
        GENERAL,
        MATHEMATICA,
        AUDIO,
        IMAGERY
    }

    private String query;
    private Type type;
    private boolean autoShow;

    public WolframAlphaRequest() {
    }

    /**
     * Constructor
     *
     * @param type     one of {@link Type}
     * @param query    the string of the query to request
     * @param autoShow whether or not to show the results once they are complete
     */
    public WolframAlphaRequest(@NonNull final Type type, @NonNull final String query,
                               final boolean autoShow) {
        this.autoShow = autoShow;
        this.query = query;
        this.type = type;
    }

    public boolean isAutoShow() {
        return autoShow;
    }

    public void setAutoShow(final boolean autoShow) {
        this.autoShow = autoShow;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(@NonNull final String query) {
        this.query = query;
    }

    public Type getType() {
        return type;
    }

    public void setType(@NonNull final Type type) {
        this.type = type;
    }
}
