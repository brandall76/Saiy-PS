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

package ai.saiy.android.applications;

import android.content.Context;
import android.support.annotation.NonNull;

import ai.saiy.android.intent.IntentConstants;
import ai.saiy.android.utils.MyLog;

/**
 * Created by benrandall76@gmail.com on 11/08/2016.
 */

public class InstallAmazon {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = InstallAmazon.class.getSimpleName();

    /**
     * Open the Amazon App Store using the package name of the desired application. If the user has the
     * Amazon application installed and defaulted, this URL will open directly in the application.
     * Otherwise, it will default to a browser search.
     *
     * @param ctx         the application context
     * @param packageName the package name to open
     * @return true if an Activity was available to handle the intent. False otherwise.
     */
    public static boolean showInstall(@NonNull final Context ctx, @NonNull final String packageName) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "showInstall");
        }

        return false;
    }

    /**
     * Get the install link for Saiy
     *
     * @param ctx the application context
     * @return the String install link
     */
    public static String getSaiyInstallLink(@NonNull final Context ctx) {
        return IntentConstants.AMAZON_PACKAGE_URL + ctx.getPackageName();
    }
}
