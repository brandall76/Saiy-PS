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

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.provider.Settings;
import android.speech.RecognitionService;
import android.support.annotation.NonNull;

import java.util.List;
import java.util.Locale;

import ai.saiy.android.BuildConfig;
import ai.saiy.android.R;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsString;

/**
 * Utility class for quick access to device specific settings
 * <p>
 * Created by benrandall76@gmail.com on 25/08/2016.
 */

public class DeviceInfo {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = DeviceInfo.class.getSimpleName();

    /**
     * Utility method to prepare certain device information to send along with any feedback.
     *
     * @param ctx the application context
     * @return a formatted string containing the required device info.
     */
    public static String getDeviceInfo(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getDeviceInfo");
        }

        return "\n\n\n" +
                "--------------------" +
                "\n" +
                ctx.getString(R.string.app_name) +
                " V" +
                BuildConfig.VERSION_NAME +
                "B\n" +
                ctx.getString(R.string.model) +
                ": " +
                Build.MODEL +
                "\n" +
                ctx.getString(R.string.manufacturer) +
                ": " +
                Build.MANUFACTURER +
                "\n" +
                ctx.getString(R.string.android) +
                ": " +
                Build.VERSION.SDK_INT +
                "\n" +
                ctx.getString(R.string.locale) +
                ": " +
                Locale.getDefault().toString() +
                "\n" +
                "VR " + ctx.getString(R.string.locale) +
                ": " +
                SPH.getVRLocale(ctx).toString() +
                "\n" +
                "TTS " + ctx.getString(R.string.locale) +
                ": " +
                SPH.getTTSLocale(ctx).toString() +
                "\n" +
                "TTS " + ctx.getString(R.string.engine) +
                ": " +
                getDefaultTTSProvider(ctx) +
                "\n" +
                "VR " + ctx.getString(R.string.engine) +
                ": " +
                getDefaultVRProvider(ctx) +
                "\n" +
                "--------------------";
    }

    /**
     * Get the default Text to Speech provider
     *
     * @param ctx the application context
     * @return the default or an empty string if one is not present
     */
    public static String getDefaultTTSProvider(@NonNull final Context ctx) {
        final String engine = Settings.Secure.getString(ctx.getContentResolver(),
                Settings.Secure.TTS_DEFAULT_SYNTH);
        return UtilsString.notNaked(engine) ? engine : "";
    }

    /**
     * Get the default voice recognition provider
     *
     * @param ctx the application context
     * @return the default or an empty string if one is not present
     */
    public static String getDefaultVRProvider(@NonNull final Context ctx) {
        final List<ResolveInfo> services = ctx.getPackageManager().queryIntentServices(
                new Intent(RecognitionService.SERVICE_INTERFACE), 0);
        return UtilsList.notNaked(services) ? services.get(0).serviceInfo.packageName : "";
    }
}
