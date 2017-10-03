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

package ai.saiy.android.custom;

import android.support.annotation.NonNull;

import java.io.Serializable;

import ai.saiy.android.api.request.Regex;

/**
 * Created by benrandall76@gmail.com on 21/04/2016.
 */
public class CustomCommandContainer implements Serializable {

    private static final long serialVersionUID = -1806338054844964403L;

    private final String keyphrase;
    private final long rowId;
    private final String serialised;
    private double score;
    private boolean exactMatch;
    private String utterance;
    private final Regex regex;

    /**
     * Constructor
     * <p/>
     * Container to hold the relevant {@link CustomCommand} data to analyse during matching.
     *
     * @param rowId      the {@link ai.saiy.android.database.DBCustomCommand} row
     * @param keyphrase  the custom phrase
     * @param regex      the regular expression {@link ai.saiy.android.api.request.Regex}
     * @param serialised the serialised {@link CustomCommand}
     */
    public CustomCommandContainer(final long rowId, @NonNull final String keyphrase, @NonNull final String regex,
                                  @NonNull final String serialised) {
        this.keyphrase = keyphrase;
        this.rowId = rowId;
        this.serialised = serialised;
        this.regex = Regex.getRegex(regex);
    }

    public String getUtterance() {
        return utterance;
    }

    public void setUtterance(@NonNull final String utterance) {
        this.utterance = utterance;
    }

    public boolean isExactMatch() {
        return exactMatch;
    }

    public void setExactMatch(final boolean exactMatch) {
        this.exactMatch = exactMatch;
    }

    public double getScore() {
        return score;
    }

    public void setScore(final double score) {
        this.score = score;
    }

    public String getKeyphrase() {
        return keyphrase;
    }

    public long getRowId() {
        return rowId;
    }

    public String getSerialised() {
        return serialised;
    }

    public Regex getRegex() {
        return regex;
    }
}
