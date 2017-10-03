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

package ai.saiy.android.utils;

import android.content.res.Resources;
import android.support.annotation.Nullable;

import java.util.Comparator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Created by benrandall76@gmail.com on 24/03/2016.
 */
public class UtilsLocale {

    /**
     * Prevent instantiation
     */
    public UtilsLocale() {
        throw new IllegalArgumentException(Resources.getSystem().getString(android.R.string.no));
    }

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = UtilsLocale.class.getSimpleName();

    public static final String LOCALE_DELIMITER_1 = "-";
    public static final String LOCALE_DELIMITER_2 = "_";

    public static final Locale DEFAULT_LOCALE = Locale.getDefault();
    public static final String DEFAULT_LOCALE_STRING = Locale.getDefault().toString();

    /**
     * Utility method to convert a string to a {@link Locale}. If this process fails, the best
     * possible format will be returned.
     *
     * @param stringLocale the string {@link Locale} to convert
     * @return the converted {@link Locale}
     */
    public static Locale stringToLocale(@Nullable final String stringLocale) {

        if (UtilsString.notNaked(stringLocale)) {

            try {
                StringTokenizer tokens;

                if (stringLocale.contains(LOCALE_DELIMITER_1)) {
                    tokens = new StringTokenizer(stringLocale, LOCALE_DELIMITER_1);

                    switch (tokens.countTokens()) {

                        case 0:
                            return new Locale(stringLocale);
                        case 1:
                            return new Locale(tokens.nextToken());
                        case 2:
                            return new Locale(tokens.nextToken(), tokens.nextToken());
                        case 3:
                            return new Locale(tokens.nextToken(), tokens.nextToken(), tokens.nextToken());
                        default:
                            return new Locale(stringLocale);
                    }

                } else if (stringLocale.contains(LOCALE_DELIMITER_2)) {
                    tokens = new StringTokenizer(stringLocale, LOCALE_DELIMITER_2);

                    switch (tokens.countTokens()) {

                        case 0:
                            return new Locale(stringLocale);
                        case 1:
                            return new Locale(tokens.nextToken());
                        case 2:
                            return new Locale(tokens.nextToken(), tokens.nextToken());
                        case 3:
                            return new Locale(tokens.nextToken(), tokens.nextToken(), tokens.nextToken());
                        default:
                            return new Locale(stringLocale);
                    }
                } else {
                    return new Locale(stringLocale);
                }
            } catch (final NoSuchElementException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "stringToLocale: NoSuchElementException");
                }
            } catch (final MissingResourceException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "stringToLocale: MissingResourceException");
                }
            } catch (final NullPointerException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "stringToLocale: NullPointerException");
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "stringToLocale: Exception");
                }
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "stringToLocale: naked");
            }
        }

        return DEFAULT_LOCALE;
    }

    public static boolean localesLanguageMatch(@Nullable final Locale localeOne, @Nullable final Locale localeTwo) {

        if (localeOne != null && localeTwo != null) {

            try {

                return localeOne.equals(localeTwo)
                        || localeOne.getLanguage().matches(localeTwo.getLanguage())
                        || localeOne.getISO3Language().matches(localeTwo.getISO3Language());

            } catch (final MissingResourceException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "localesLanguageMatch: MissingResourceException");
                }
            } catch (final NullPointerException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "localesLanguageMatch: NullPointerException");
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "localesLanguageMatch: Exception");
                }
            }
        }

        return false;
    }

    public static boolean localesMatch(@Nullable final Locale localeOne, @Nullable final Locale localeTwo) {

        if (localeOne != null && localeTwo != null) {

            try {

                if (localeOne.equals(localeTwo)) {
                    return true;
                } else {
                    if (localeOne.getLanguage().matches(localeTwo.getLanguage())) {
                        return localeOne.getCountry().matches(localeTwo.getCountry());
                    } else {
                        return localeOne.getISO3Language().matches(localeTwo.getISO3Language())
                                && localeOne.getISO3Country().matches(localeTwo.getISO3Country());
                    }
                }
            } catch (final MissingResourceException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "localesMatch: MissingResourceException");
                }
            } catch (final NullPointerException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "localesMatch: NullPointerException");
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "localesMatch: Exception");
                }
            }
        }

        return false;
    }

    public static class LocaleComparator implements Comparator<Locale> {
        @Override
        public int compare(final Locale l1, final Locale l2) {
            return l1.toString().compareTo(l2.toString());
        }
    }
}