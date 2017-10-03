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

package ai.saiy.android.device;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.NonNull;

/**
 * A collection of handy device related methods. Static for easy access
 * <p>
 * Created by benrandall76@gmail.com on 06/09/2016.
 */

public class UtilsDevice {

    /**
     * Check if the device is currently locked
     *
     * @return true if the device is in secure mode, false otherwise
     */
    public static boolean isDeviceLocked(@NonNull final Context ctx) {
        return ((KeyguardManager) ctx.getSystemService(Context.KEYGUARD_SERVICE))
                .inKeyguardRestrictedInputMode();
    }

    /**
     * Check if the device screen is currently turned off
     *
     * @param ctx the application context
     * @return true if the screen is off, false otherwise
     */
    public static boolean isScreenOff(@NonNull final Context ctx) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return isScreenOff20(ctx);
        } else {
            return isScreenOffDeprecated(ctx);
        }
    }

    @SuppressWarnings("deprecation")
    private static boolean isScreenOffDeprecated(@NonNull final Context ctx) {
        return !((PowerManager) ctx.getSystemService(Context.POWER_SERVICE)).isScreenOn();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    private static boolean isScreenOff20(@NonNull final Context ctx) {
        return !((PowerManager) ctx.getSystemService(Context.POWER_SERVICE)).isInteractive();
    }
}
