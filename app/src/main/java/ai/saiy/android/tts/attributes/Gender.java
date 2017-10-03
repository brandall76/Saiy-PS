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

package ai.saiy.android.tts.attributes;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.regex.Pattern;

import ai.saiy.android.utils.MyLog;

/**
 * Created by benrandall76@gmail.com on 19/08/2016.
 */

public enum Gender {

    MALE(ai.saiy.android.api.attributes.Gender.MALE),
    FEMALE(ai.saiy.android.api.attributes.Gender.FEMALE),
    UNDEFINED(ai.saiy.android.api.attributes.Gender.UNDEFINED);

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Gender.class.getSimpleName();

    public static final String EN_MALE_STRING = "male";
    public static final String EN_FEMALE_STRING = "female";

    public static final Pattern pEN_MALE_STRING = Pattern.compile(".*" + EN_MALE_STRING + ".*", Pattern.CASE_INSENSITIVE);
    public static final Pattern pEN_FEMALE_STRING = Pattern.compile(".*" + EN_FEMALE_STRING + ".*", Pattern.CASE_INSENSITIVE);

    private final ai.saiy.android.api.attributes.Gender gender;

    Gender(final ai.saiy.android.api.attributes.Gender gender) {
        this.gender = gender;
    }

    public ai.saiy.android.api.attributes.Gender getRemoteGender() {
        return this.gender;
    }

    /**
     * Convert a remote Gender object to the local variant
     *
     * @param gender the remote {@link ai.saiy.android.api.attributes.Gender}
     * @return the local variant of {@link Gender}
     */
    public static Gender remoteToLocal(@NonNull final ai.saiy.android.api.attributes.Gender gender) {

        switch (gender) {

            case FEMALE:
                return FEMALE;
            case MALE:
                return MALE;
            case UNDEFINED:
                return UNDEFINED;
            default:
                return UNDEFINED;
        }
    }

    public static Gender getGender(@Nullable final String name) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getGender: " + name);
        }

        if (name != null) {

            try {
                return Enum.valueOf(Gender.class, name.trim());
            } catch (final IllegalArgumentException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getGender: IllegalArgumentException");
                    e.printStackTrace();
                }
                return Gender.UNDEFINED;
            }

        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getGender: name null");
            }
            return Gender.UNDEFINED;
        }
    }

    public static Gender getGenderFromVoiceName(@NonNull final String engineName) {

        if (pEN_FEMALE_STRING.matcher(engineName).matches()) {
            return FEMALE;
        } else if (pEN_MALE_STRING.matcher(engineName).matches()) {
            return MALE;
        } else {
            return UNDEFINED;
        }

//        if (StringUtils.containsIgnoreCase(engineName, EN_FEMALE_STRING)) {
//            return FEMALE;
//        } else if (StringUtils.containsIgnoreCase(engineName, EN_MALE_STRING)) {
//            return MALE;
//        } else {
//            return UNDEFINED;
//        }
    }
}
