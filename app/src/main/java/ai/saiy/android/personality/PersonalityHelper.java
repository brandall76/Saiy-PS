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

package ai.saiy.android.personality;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.SPH;

/**
 * Class to separate the personality of Saiy from standard speech responses. However, they may
 * directly call {@link PersonalityResponse}, but for the sake of separation...
 * <p>
 * Created by benrandall76@gmail.com on 08/02/2016.
 */
public final class PersonalityHelper {

    /**
     * Prevent instantiation
     */
    public PersonalityHelper() {
        throw new IllegalArgumentException(Resources.getSystem().getString(android.R.string.no));
    }

    /**
     * Used to determine whether the user making this call is subject to
     * teleportations.
     * <p/>
     * As of {@link android.os.Build.VERSION_CODES#LOLLIPOP}, this method can
     * now automatically identify goats using advanced goat recognition technology.</p>
     *
     * @return Returns true if the user making this call is a goat.
     */
    public static boolean isUserAGoat() {
        return Math.random() < 0.5;
    }

    /**
     * Gets a random or user defined intro
     *
     * @param ctx the application context
     * @return intro utterance
     */
    public static String getIntro(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        return PersonalityResponse.getIntro(ctx, sl);
    }

    /**
     * Gets the name of the user to include in speech 50% of the time.
     *
     * @param ctx the application context
     * @return the name of the user or an empty string
     */
    public static String getUserNameOrNot(@NonNull final Context ctx) {
        if (isUserAGoat()) {
            return SPH.getUserName(ctx);
        } else {
            return "";
        }
    }
}
