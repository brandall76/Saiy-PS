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

package ai.saiy.android.command.translate;

import android.support.annotation.NonNull;

/**
 * Created by benrandall76@gmail.com on 01/06/2016.
 */
public class CommandTranslateValues {

    private String language;
    private String text;
    private int[][] LanguageRanges;
    private int[][] textRanges;
    private long languageStartIndex;
    private long languageEndIndex;
    private long textStartIndex;
    private long textEndIndex;

    public long getLanguageEndIndex() {
        return languageEndIndex;
    }

    public void setLanguageEndIndex(final long languageEndIndex) {
        this.languageEndIndex = languageEndIndex;
    }

    public long getLanguageStartIndex() {
        return languageStartIndex;
    }

    public void setLanguageStartIndex(final long languageStartIndex) {
        this.languageStartIndex = languageStartIndex;
    }

    public long getTextEndIndex() {
        return textEndIndex;
    }

    public void setTextEndIndex(final long textEndIndex) {
        this.textEndIndex = textEndIndex;
    }

    public long getTextStartIndex() {
        return textStartIndex;
    }

    public void setTextStartIndex(final long textStartIndex) {
        this.textStartIndex = textStartIndex;
    }

    public int[][] getLanguageRanges() {
        return LanguageRanges;
    }

    public void setLanguageRanges(@NonNull final int[][] languageRanges) {
        LanguageRanges = languageRanges;
    }

    public int[][] getTextRanges() {
        return textRanges;
    }

    public void setTextRanges(final int[][] textRanges) {
        this.textRanges = textRanges;
    }

    public void setLanguage(@NonNull final String language) {
        this.language = language;
    }

    public String getLanguage() {
        return language;
    }

    public void setText(@NonNull final String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
