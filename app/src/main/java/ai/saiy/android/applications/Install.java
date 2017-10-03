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
import android.support.annotation.WorkerThread;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;

import java.io.IOException;

import ai.saiy.android.utils.Global;
import ai.saiy.android.utils.MyLog;

/**
 * Created by benrandall76@gmail.com on 11/08/2016.
 */

public class Install {

    public enum Location {
        PLAYSTORE, AMAZON
    }

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Install.class.getSimpleName();

    /**
     * Depending on the version of Saiy, the install link needs to vary. This class handles all
     * eventualities, so no changes will be needed elsewhere in the code.
     *
     * @param ctx         the application context
     * @param packageName the package name to open
     * @return true if the intent was successful. False otherwise.
     */
    public static boolean showInstallLink(@NonNull final Context ctx, @NonNull final String packageName) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "showInstall");
        }

        switch (Global.installLocation) {

            case PLAYSTORE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "showInstall PLAYSTORE");
                }
                return InstallPlayStore.showInstall(ctx, packageName);
            case AMAZON:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "showInstall AMAZON");
                }
                return InstallAmazon.showInstall(ctx, packageName);
            default:
                return false;
        }
    }

    /**
     * Get the account type associated with the install location of the app
     *
     * @return the account type
     */
    public static String getAccountType() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getAccountType");
        }

        switch (Global.installLocation) {

            case PLAYSTORE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getAccountType PLAYSTORE");
                }
                return GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE;
            case AMAZON:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getAccountType AMAZON");
                }
                return "";
            default:
                return "";
        }
    }

    /**
     * Asynchronously get the account id associated with the email address
     *
     * @param ctx         the application context
     * @param accountName the email address
     * @return the account id or null if the operation failed
     */
    @WorkerThread
    public static String getAccountId(@NonNull final Context ctx, @NonNull final String accountName) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getAccountId");
        }

        try {

            switch (Global.installLocation) {

                case PLAYSTORE:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "getAccountId PLAYSTORE");
                    }
                    final String accountId = GoogleAuthUtil.getAccountId(ctx, accountName);

                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "getAccountId PLAYSTORE: " + accountId);
                    }
                    return accountId;
                case AMAZON:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "getAccountId AMAZON");
                    }
                default:
                    break;
            }

        } catch (final GoogleAuthException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getAccountId GoogleAuthException");
                e.printStackTrace();
            }
        } catch (final IOException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getAccountId IOException");
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Get the install link for Saiy
     *
     * @return the String install link
     */
    public static String getSaiyInstallLink(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getSaiyInstallLink");
        }

        switch (Global.installLocation) {

            case PLAYSTORE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getSaiyInstallLink PLAYSTORE");
                }
                return InstallPlayStore.getSaiyInstallLink(ctx);
            case AMAZON:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getSaiyInstallLink AMAZON");
                }
                return InstallAmazon.getSaiyInstallLink(ctx);
            default:
                return "";
        }
    }
}
