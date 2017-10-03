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

package ai.saiy.android.tts.sound;

import android.support.annotation.NonNull;

import java.util.Random;

import ai.saiy.android.tts.attributes.Gender;

/**
 * Created by benrandall76@gmail.com on 12/09/2016.
 */

public class SoundEffect {

    public static final long SILENCE_DURATION = 350L;

    public static final String FART = "fart";
    private static final String FART_1 = "fart1";
    private static final String FART_2 = "fart2";
    private static final String FART_3 = "fart3";
    public static final String BURP = "burp";
    private static final String BURP_1 = "burp1";
    private static final String BURP_2 = "burp2";
    public static final String COUGH = "cough";
    private static final String COUGH_1 = "cough1";
    private static final String COUGH_2 = "cough2";
    public static final String GIGGLE = "giggle";
    private static final String GIGGLE_1 = "giggle1";
    private static final String GIGGLE_2 = "giggle2";
    public static final String CRY = "cry";
    private static final String CRY_1 = "cry1";
    public static final String PEE = "pee";
    private static final String PEE_1 = "pee1";
    public static final String SNEEZE = "sneeze";
    private static final String SNEEZE_1 = "sneeze1";
    public static final String PUKE = "puke";
    private static final String PUKE_1 = "puke1";
    public static final String FLUSH = "flush";
    private static final String FLUSH_1 = "flush1";

    private static final String[] fartArrayMale = new String[]{FART_1, FART_2, FART_3};
    private static final String[] fartArrayFemale = new String[]{FART_1, FART_2, FART_3};

    private static final String[] burpArrayMale = new String[]{BURP_1, BURP_2};
    private static final String[] burpArrayFemale = new String[]{BURP_1, BURP_2};

    private static final String[] coughArrayMale = new String[]{COUGH_2};
    private static final String[] coughArrayFemale = new String[]{COUGH_1};

    private static final String[] giggleArrayMale = new String[]{GIGGLE_2};
    private static final String[] giggleArrayFemale = new String[]{GIGGLE_1};

    private static final String[] cryArrayMale = new String[]{CRY_1};
    private static final String[] cryArrayFemale = new String[]{CRY_1};

    private static final String[] peeArrayMale = new String[]{PEE_1};
    private static final String[] peeArrayFemale = new String[]{PEE_1};

    private static final String[] sneezeArrayMale = new String[]{SNEEZE_1};
    private static final String[] sneezeArrayFemale = new String[]{SNEEZE_1};

    private static final String[] pukeArrayMale = new String[]{PUKE_1};
    private static final String[] pukeArrayFemale = new String[]{PUKE_1};

    private static final String[] flushArrayMale = new String[]{FLUSH_1};
    private static final String[] flushArrayFemale = new String[]{FLUSH_1};

    public static String getSoundEffect(@NonNull final String name, @NonNull final Gender gender) {

        switch (name) {

            case FART:
                return getFart(gender);
            case BURP:
                return getBurp(gender);
            case COUGH:
                return getCough(gender);
            case GIGGLE:
                return getGiggle(gender);
            case CRY:
                return getCry(gender);
            case PEE:
                return getPee(gender);
            case SNEEZE:
                return getSneeze(gender);
            case PUKE:
                return getPuke(gender);
            case FLUSH:
                return getFlush(gender);
            default:
                return name;
        }
    }

    public static String getFart(@NonNull final Gender gender) {

        switch (gender) {

            case MALE:
                return fartArrayMale[new Random().nextInt(fartArrayMale.length)];
            case FEMALE:
                return fartArrayFemale[new Random().nextInt(fartArrayFemale.length)];
            case UNDEFINED:
            default:
                return fartArrayFemale[new Random().nextInt(fartArrayFemale.length)];
        }
    }

    public static String getBurp(@NonNull final Gender gender) {

        switch (gender) {

            case MALE:
                return burpArrayMale[new Random().nextInt(burpArrayMale.length)];
            case FEMALE:
                return burpArrayFemale[new Random().nextInt(burpArrayFemale.length)];
            case UNDEFINED:
            default:
                return burpArrayFemale[new Random().nextInt(burpArrayFemale.length)];
        }
    }

    public static String getCough(@NonNull final Gender gender) {

        switch (gender) {

            case MALE:
                return coughArrayMale[new Random().nextInt(coughArrayMale.length)];
            case FEMALE:
                return coughArrayFemale[new Random().nextInt(coughArrayFemale.length)];
            case UNDEFINED:
            default:
                return coughArrayFemale[new Random().nextInt(coughArrayFemale.length)];
        }
    }

    public static String getGiggle(@NonNull final Gender gender) {

        switch (gender) {

            case MALE:
                return giggleArrayMale[new Random().nextInt(giggleArrayMale.length)];
            case FEMALE:
                return giggleArrayFemale[new Random().nextInt(giggleArrayFemale.length)];
            case UNDEFINED:
            default:
                return giggleArrayFemale[new Random().nextInt(giggleArrayFemale.length)];
        }
    }

    public static String getCry(@NonNull final Gender gender) {

        switch (gender) {

            case MALE:
                return cryArrayMale[new Random().nextInt(cryArrayMale.length)];
            case FEMALE:
                return cryArrayFemale[new Random().nextInt(cryArrayFemale.length)];
            case UNDEFINED:
            default:
                return cryArrayFemale[new Random().nextInt(cryArrayFemale.length)];
        }
    }

    public static String getPee(@NonNull final Gender gender) {

        switch (gender) {

            case MALE:
                return peeArrayMale[new Random().nextInt(peeArrayMale.length)];
            case FEMALE:
                return peeArrayFemale[new Random().nextInt(peeArrayFemale.length)];
            case UNDEFINED:
            default:
                return peeArrayFemale[new Random().nextInt(peeArrayFemale.length)];
        }
    }

    public static String getSneeze(@NonNull final Gender gender) {

        switch (gender) {

            case MALE:
                return sneezeArrayMale[new Random().nextInt(sneezeArrayMale.length)];
            case FEMALE:
                return sneezeArrayFemale[new Random().nextInt(sneezeArrayFemale.length)];
            case UNDEFINED:
            default:
                return sneezeArrayFemale[new Random().nextInt(sneezeArrayFemale.length)];
        }
    }

    public static String getPuke(@NonNull final Gender gender) {

        switch (gender) {

            case MALE:
                return pukeArrayMale[new Random().nextInt(pukeArrayMale.length)];
            case FEMALE:
                return pukeArrayFemale[new Random().nextInt(pukeArrayFemale.length)];
            case UNDEFINED:
            default:
                return pukeArrayFemale[new Random().nextInt(pukeArrayFemale.length)];
        }
    }

    public static String getFlush(@NonNull final Gender gender) {

        switch (gender) {

            case MALE:
                return flushArrayMale[new Random().nextInt(flushArrayMale.length)];
            case FEMALE:
                return flushArrayFemale[new Random().nextInt(flushArrayFemale.length)];
            case UNDEFINED:
            default:
                return flushArrayFemale[new Random().nextInt(flushArrayFemale.length)];
        }
    }
}
