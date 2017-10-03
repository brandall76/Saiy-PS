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

package ai.saiy.android.command.translate.provider.bing;

import android.support.annotation.NonNull;

import java.util.Locale;

import ai.saiy.android.utils.UtilsLocale;

/**
 * Enum class that details all translation languages supported by Bing. As well as the language codes
 * that are required to send to Bing, the equivalent {@link Locale} is included, so that the Text to
 * Speech engine will be able to initialise any region specific engine.
 * <p>
 * Created by benrandall76@gmail.com on 16/04/2016.
 */
public enum TranslationLanguageBing {
    AUTO_DETECT("", ""),
    ARABIC("ar", "ar"),
    BOSNIAN("bs-Latn", "bs_BA"),
    BULGARIAN("bg", "bg_BG"),
    CATALAN("ca", "ca_ES"),
    CHINESE("zh-CHS", "zh_CN"),
    MANDARIN("zh-CHS", "zh_CN"),
    CANTONESE("zh-CHT", "zh_TW"),
    CHINESE_SIMPLIFIED("zh-CHS", "zh_CN"),
    CHINESE_TRADITIONAL("zh-CHT", "zh_TW"),
    CROATIAN("hr", "hr_HR"),
    CZECH("cs", "cs_CZ"),
    CZECHOSLOVAKIAN("cs", "cs_CZ"),
    DANISH("da", "da_DK"),
    DUTCH("nl", "nl_NL"),
    ENGLISH("en", "en"),
    ESTONIAN("et", "et_EE"),
    FINNISH("fi", "fi_FI"),
    FRENCH("fr", "fr_FR"),
    GERMAN("de", "de_DE"),
    GREEK("el", "el_GR"),
    HAITIAN_CREOLE("ht", "ht_HT"),
    HAITIAN("ht", "ht_HT"),
    HEBREW("he", "iw_IL"),
    HINDI("hi", "hi_IN"),
    HUNGARIAN("hu", "hu_HU"),
    INDONESIAN("id", "in_ID"),
    ITALIAN("it", "it_IT"),
    JAPANESE("ja", "ja_JP"),
    KISWAHILI("sw", "sw"),
    SWAHILI("sw", "sw"),
    KLINGON("tlh", "tlh"),
    KOREAN("ko", "ko_KR"),
    LATVIAN("lv", "lv_LV"),
    LITHUANIAN("lt", "lt_LT"),
    MALAY("ms", "ms_MY"),
    MALAYSIAN("ms", "ms_MY"),
    MALTESE("mt", "mt_MT"),
    NORWEGIAN("no", "no_NO"),
    PERSIAN("fa", "fa_IR"),
    POLISH("pl", "pl_PL"),
    PORTUGUESE("pt", "pt_PT"),
    PORTUGUESE_BRAZILIAN("pt", "pt_BR"),
    BRAZILIAN("pt", "pt_BR"),
    ROMANIAN("ro", "ro_RO"),
    RUSSIAN("ru", "ru_RU"),
    SERBIAN("sr-Cyrl", "sr_RS"),
    SERBIAN_CYRILLIC("sr-Cyrl", "sr_RS"),
    SERBIAN_LATIN("sr-Latn", "sr_Latn_RS"),
    SLOVAK("sk", "sk_SK"),
    SLOVAKIAN("sk", "sk_SK"),
    SLOVENIAN("sl", "sl_SI"),
    SPANISH("es", "es_ES"),
    SWEDISH("sv", "sv_SE"),
    THAI("th", "th_TH"),
    TURKISH("tr", "tr_TR"),
    UKRAINIAN("uk", "uk_UA"),
    URDU("ur", "ur_PK"),
    VIETNAMESE("vi", "vi_VN"),
    WELSH("cy", "cy_GB");

    private final String language;
    private final String locale;

    /**
     * Constructor.
     *
     * @param language the Bing language identifier.
     * @param locale   the String locale
     */
    TranslationLanguageBing(@NonNull final String language, @NonNull final String locale) {
        this.language = language;
        this.locale = locale;
    }

    /**
     * Get the {@link Locale} of the Bing language
     *
     * @return the {@link Locale}
     */
    public Locale getLocale() {
        return UtilsLocale.stringToLocale(locale);
    }

    /**
     * Get the language code used by Bing
     *
     * @return the language code
     */
    public String getLanguage() {
        return language;
    }
}