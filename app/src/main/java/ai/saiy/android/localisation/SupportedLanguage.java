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

package ai.saiy.android.localisation;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

/**
 * Class that stores enum defaults of languages currently supported by the Recognition and Text to Speech
 * systems, as well as a language model. The application may support other languages in terms of XML
 * localisation in the UI, but the user would need to select an alternative {@link Locale} to use
 * the voice and speech features of the application itself.
 * <p>
 * Hard-coding such things is not ideal, however each language the application is translated to is a
 * laborious process and adding in an extra value each time doesn't seem too much effort in comparison...
 * <p>
 * Additional entries will be added, only when there are specific spelling or pronunciation variations
 * that the application will need to handle.
 * <p>
 * The parent locale, such as {@link Locale#ENGLISH} will not replace missing {@link Locale} variations
 * such as eng_IND. Rather, the methods will confirm a supported language and/or country variant and
 * leave any variant intact for use with Text to Speech and Voice Recognition Providers. It is a
 * specific localisation resource that we are concerned with here.
 * <p>
 * Created by benrandall76@gmail.com on 25/03/2016.
 */
public enum SupportedLanguage {

    ENGLISH("en", "", "", "eng", "", "", "English", "English", Locale.ENGLISH, null),
    ENGLISH_US("en", "US", "en_US", "eng", "USA", "eng_USA", "English", "United States", Locale.US, SupportedLanguage.ENGLISH);

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = SupportedLanguage.class.getSimpleName();

    private final String language;
    private final String country;
    private final String languageCountry;
    private final String languageISO;
    private final String countryISO;
    private final String languageCountryISO;
    private final String languageTag;
    private final String countryTag;
    private final Locale locale;
    private final SupportedLanguage parent;

    /**
     * Constructor
     *
     * @param language           string representation
     * @param country            string representation
     * @param languageCountry    string representation
     * @param languageISO        string representation
     * @param countryISO         string representation
     * @param languageCountryISO string representation
     * @param languageTag        string representation
     * @param countryTag         string representation
     * @param locale             of the Supported TranslationLanguageBing
     * @param parent             of the Supported TranslationLanguageBing (the language itself)
     */
    SupportedLanguage(final String language, final String country, final String languageCountry,
                      final String languageISO, final String countryISO, final String languageCountryISO,
                      final String languageTag, final String countryTag, final Locale locale,
                      final SupportedLanguage parent) {
        this.language = language;
        this.country = country;
        this.languageCountry = languageCountry;
        this.languageISO = languageISO;
        this.countryISO = countryISO;
        this.languageCountryISO = languageCountryISO;
        this.languageTag = languageTag;
        this.countryTag = countryTag;
        this.locale = locale;
        this.parent = parent;
    }

    /**
     * Get the parent {@link Locale}
     *
     * @return the parent Locale
     */
    public SupportedLanguage getParent() {
        return parent;
    }

    /**
     * Check if the TranslationLanguageBing has a parent language
     *
     * @return true if there is a parent language
     */
    public boolean hasParent() {
        return parent != null;
    }

    /**
     * Get the {@link Locale}
     *
     * @return the Locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Get the ISO3 language string
     *
     * @return the ISO3 language string
     */
    public String getLanguageISO() {
        return this.languageISO;
    }

    /**
     * Get the ISO3 Country string
     *
     * @return the ISO3 Country string
     */
    public String getCountryISO() {
        return this.countryISO;
    }

    /**
     * Get the Country string
     *
     * @return the Country string
     */
    public String getCountry() {
        return country;
    }

    /**
     * Get the Country tag
     *
     * @return the Country tag
     */
    public String getCountryTag() {
        return this.countryTag;
    }

    /**
     * Get the language tag
     *
     * @return the language tag
     */
    public String getLanguageTag() {
        return this.languageTag;
    }

    /**
     * Get the language string
     *
     * @return the language string
     */
    public String getLanguage() {
        return this.language;
    }

    /**
     * Get the string representation of the {@link Locale}
     *
     * @return the string representation
     */
    public String getLanguageCountry() {
        return this.languageCountry;
    }

    /**
     * Get the string representation of the ISO3 {@link Locale}
     *
     * @return the string representation
     */
    public String getLanguageCountryISO() {
        return this.languageCountryISO;
    }

    /**
     * Get all supported languages
     *
     * @return a list of supported languages
     */
    public static SupportedLanguage[] getLanguages() {
        return SupportedLanguage.values();
    }

    /**
     * Get the supported language of the locale or the parent, to use for localised resources.
     *
     * @param userLocale the user's {@link Locale}
     * @return the Supported TranslationLanguageBing
     */
    public static SupportedLanguage getSupportedLanguage(@NonNull final Locale userLocale) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getSupportedLanguage: " + userLocale.toString());
        }

        for (final SupportedLanguage sl : getLanguages()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getSupportedLanguage: comparing: " + sl.getLocale().toString() + " ~ " + userLocale.toString());
            }

            if (sl.getLocale().equals(userLocale)) {
                return sl;
            }
        }

        final ArrayList<SupportedLanguage> matches = new ArrayList<>();

        final String language = userLocale.getLanguage();
        final String country = userLocale.getCountry();

        if (UtilsString.notNaked(language)) {

            for (final SupportedLanguage sl : getLanguages()) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getSupportedLanguage: comparing: " + language + " ~ " + sl.getLanguage());
                }

                if (language.equalsIgnoreCase(sl.getLanguage())) {
                    matches.add(sl);
                }
            }

            if (!matches.isEmpty()) {
                for (final SupportedLanguage sl : matches) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "getSupportedLanguage: comparing: " + country + " ~ " + sl.getCountry());
                    }

                    if (country.equalsIgnoreCase(sl.getCountry())) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "getSupportedLanguage: full match: " + sl.getLanguageCountry());
                        }
                        return sl;
                    }
                }

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getSupportedLanguage: no country match returning first: " + matches.get(0).getLocale().toString());
                }

                if (matches.get(0).hasParent()) {
                    return matches.get(0).getParent();
                }

                return matches.get(0);

            }

            for (final SupportedLanguage sl : getLanguages()) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getSupportedLanguage: comparing: " + language + " ~ " + sl.getLanguageISO());
                }

                if (language.equalsIgnoreCase(sl.getLanguageISO())) {
                    matches.add(sl);
                }
            }

            if (!matches.isEmpty()) {
                for (final SupportedLanguage sl : matches) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "getSupportedLanguage: comparing: " + country + " ~ " + sl.getCountryISO());
                    }

                    if (country.equalsIgnoreCase(sl.getCountryISO())) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "getSupportedLanguage: full match: " + sl.getLanguageCountryISO());
                        }
                        return sl;
                    }
                }

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getSupportedLanguage: no countryISO match returning first: " + matches.get(0).getLocale().toString());
                }

                if (matches.get(0).hasParent()) {
                    return matches.get(0).getParent();
                }

                return matches.get(0);
            }

        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getSupportedLanguage: language naked" + userLocale.toString());
            }
        }

        return ENGLISH;
    }
}
