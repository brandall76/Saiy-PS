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

package ai.saiy.android.permissions;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;

import ai.saiy.android.Manifest;
import ai.saiy.android.ui.activity.ActivityPermissionDialog;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

/**
 * Created by benrandall76@gmail.com on 12/04/2016.
 */
public class PermissionHelper {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = PermissionHelper.class.getSimpleName();

    public static final String REQUESTED_PERMISSION = "requested_permission";
    public static final String REQUESTED_PERMISSION_ID = "requested_permission_id";

    public static final int REQUEST_UNKNOWN = 0;
    public static final int REQUEST_AUDIO = 1;
    public static final int REQUEST_FILE = 2;
    public static final int REQUEST_GROUP_CONTACTS = 3;

    /**
     * Check if the calling application has the correct permission to control Saiy.
     *
     * @param ctx the application context
     * @return true if the permission has been granted.
     */
    public static boolean checkSaiyRemotePermission(@NonNull final Context ctx) {

        switch (ctx.checkCallingPermission(Manifest.permission.CONTROL_SAIY)) {

            case PackageManager.PERMISSION_GRANTED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "checkSaiyRemotePermission: PERMISSION_GRANTED");
                }
                return true;
            case PackageManager.PERMISSION_DENIED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "checkSaiyRemotePermission: PERMISSION_DENIED");
                }
            default:
                return false;
        }
    }

    /**
     * Check if the calling application has the correct permission to control Saiy.
     *
     * @param ctx        the application context
     * @param callingUid of the remote request
     * @return true if the permission has been granted.
     */
    public static boolean checkSaiyPermission(@NonNull final Context ctx, final int callingUid) {

        final String packageName = ctx.getPackageManager().getNameForUid(callingUid);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkSaiyPermission: " + packageName);
        }

        if (UtilsString.notNaked(packageName) && callingUid > 0) {

            if (!packageName.matches(ctx.getPackageName())) {

                switch (ctx.checkCallingPermission(Manifest.permission.CONTROL_SAIY)) {

                    case PackageManager.PERMISSION_GRANTED:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "checkSaiyPermission: PERMISSION_GRANTED");
                        }
                        return true;
                    case PackageManager.PERMISSION_DENIED:
                    default:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "checkSaiyPermission: PERMISSION_DENIED");
                        }
                        return false;
                }
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "checkSaiyPermission: self");
                }
                return true;
            }
        } else {
            MyLog.e(CLS_NAME, ctx.getString(ai.saiy.android.R.string.error_package_name_null));
        }

        return false;
    }

    /**
     * Check if the user has granted microphone permissions
     *
     * @param ctx the application context
     * @return true if the permissions have been granted. False if they have been denied or are
     * required to be requested.
     */
    public static boolean checkAudioPermissions(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkAudioPermissions");
        }

        switch (ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.RECORD_AUDIO)) {

            case PackageManager.PERMISSION_GRANTED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "checkAudioPermissions: PERMISSION_GRANTED");
                }
                return true;
            case PackageManager.PERMISSION_DENIED:
            default:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "checkAudioPermissions: PERMISSION_DENIED");
                }

                final Intent intent = new Intent(ctx, ActivityPermissionDialog.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                final Bundle bundle = new Bundle();
                bundle.putStringArray(REQUESTED_PERMISSION, new String[]{android.Manifest.permission.RECORD_AUDIO});
                bundle.putInt(REQUESTED_PERMISSION_ID, REQUEST_AUDIO);

                intent.putExtras(bundle);

                ctx.startActivity(intent);
                return false;
        }
    }

    /**
     * Check if the user has granted write files permissions
     *
     * @param ctx the application context
     * @return true if the permissions have been granted. False if they have been denied or are
     * required to be requested.
     */
    public static boolean checkFilePermissions(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkFilePermissions");
        }

        switch (ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            case PackageManager.PERMISSION_GRANTED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "checkFilePermissions: PERMISSION_GRANTED");
                }
                return true;
            case PackageManager.PERMISSION_DENIED:
            default:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "checkFilePermissions: PERMISSION_DENIED");
                }

                final Intent intent = new Intent(ctx, ActivityPermissionDialog.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                final Bundle bundle = new Bundle();
                bundle.putStringArray(REQUESTED_PERMISSION, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE});
                bundle.putInt(REQUESTED_PERMISSION_ID, REQUEST_FILE);

                intent.putExtras(bundle);

                ctx.startActivity(intent);
                return false;
        }
    }

    /**
     * Check if the user has granted the contacts group permission
     *
     * @param ctx the application context
     * @return true if the permissions have been granted. False if they have been denied or are
     * required to be requested.
     */
    public static boolean checkContactGroupPermissions(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkContactGroupPermissions");
        }

        switch (ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.GET_ACCOUNTS)) {

            case PackageManager.PERMISSION_GRANTED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "checkContactGroupPermissions: PERMISSION_GRANTED");
                }
                return true;
            case PackageManager.PERMISSION_DENIED:
            default:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "checkContactGroupPermissions: PERMISSION_DENIED");
                }

                final Intent intent = new Intent(ctx, ActivityPermissionDialog.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                final Bundle bundle = new Bundle();
                bundle.putStringArray(REQUESTED_PERMISSION, new String[]{android.Manifest.permission.GET_ACCOUNTS});
                bundle.putInt(REQUESTED_PERMISSION_ID, REQUEST_GROUP_CONTACTS);

                intent.putExtras(bundle);

                ctx.startActivity(intent);
                return false;
        }
    }

    /**
     * Check if the user has granted the usage stats permission, which must be manually complete via the
     * settings.
     *
     * @param ctx the application context
     * @return true if the permissions have been granted, false otherwise
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static boolean checkUsageStatsPermission(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkUsageStatsPermission");
        }

        final AppOpsManager appOps = (AppOpsManager) ctx.getSystemService(Context.APP_OPS_SERVICE);

        return appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), ctx.getPackageName()) == AppOpsManager.MODE_ALLOWED;
    }
}
