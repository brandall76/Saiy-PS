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

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Utility Class to help manage speech responses for various locales, handling issues mentioned in
 * the {@link SaiyResources} class.
 * <p/>
 * Created by benrandall76@gmail.com on 25/03/2016.
 */
public class SaiyResourcesHelper {

    /**
     * Get the XML resource array for the {@link SupportedLanguage} which will handle and
     * necessary variation.
     *
     * @param ctx      the application context
     * @param sl       the {@link SupportedLanguage}
     * @param parentId the id of the XML resource array
     * @return the XML resource array for the current {@link SupportedLanguage} or default English.
     */
    public static String[] getArrayResource(@NonNull final Context ctx, @NonNull final SupportedLanguage sl,
                                            final int parentId) {
        final SaiyResources sr = new SaiyResources(ctx, sl);
        final String[] resourceArray = sr.getStringArray(parentId);
        sr.reset();
        return resourceArray;
    }

    /**
     * Get the XML resource String for the {@link SupportedLanguage} which will handle any
     * necessary variation.
     *
     * @param ctx      the application context
     * @param sl       the {@link SupportedLanguage}
     * @param parentId the id of the XML resource array
     * @return the XML resource String for the current {@link SupportedLanguage} or default English.
     */
    public static String getStringResource(@NonNull final Context ctx, @NonNull final SupportedLanguage sl,
                                           final int parentId) {
        final SaiyResources sr = new SaiyResources(ctx, sl);
        final String resourceString = sr.getString(parentId);
        sr.reset();
        return resourceString;
    }
}
