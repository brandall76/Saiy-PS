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

package ai.saiy.android.cognitive.emotion.provider.beyondverbal.language;

import android.support.annotation.NonNull;

import java.util.Locale;

/**
 * Utility class to query and access supported languages of the Beyond Verbal API
 * <p>
 * Created by benrandall76@gmail.com on 08/06/2016.
 */
public enum SupportedLanguageBV {

    ENGLISH_US("en-us", "eng_USA", "English", "United States", Locale.US);

    private final String languageCountry;
    private final String languageCountryISO;
    private final String language;
    private final String country;
    private final Locale locale;

    SupportedLanguageBV(final String country, final String languageCountry, final String languageCountryISO,
                        final String language, final Locale locale) {
        this.country = country;
        this.languageCountry = languageCountry;
        this.languageCountryISO = languageCountryISO;
        this.language = language;
        this.locale = locale;
    }

    public static boolean isSupported(@NonNull final Locale loc) {

        for (final SupportedLanguageBV sl : getSupportedLanguages()) {
            if (sl.getLocale().equals(loc)) {
                return true;
            }
        }

        return false;
    }

    public static SupportedLanguageBV getSupportedLanguage(@NonNull final Locale loc) {

        for (final SupportedLanguageBV sl : getSupportedLanguages()) {
            if (sl.getLocale().equals(loc)) {
                return sl;
            }
        }

        return ENGLISH_US;
    }

    public static SupportedLanguageBV[] getSupportedLanguages() {
        return SupportedLanguageBV.values();
    }

    public String getCountry() {
        return country;
    }

    public String getLanguage() {
        return language;
    }

    public String getLanguageCountry() {
        return languageCountry;
    }

    public String getServerFormat() {
        return languageCountry;
    }

    public String getLanguageCountryISO() {
        return languageCountryISO;
    }

    public Locale getLocale() {
        return locale;
    }
}
