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

package ai.saiy.android.command.translate.provider.bing;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import ai.saiy.android.command.translate.provider.TranslationProvider;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsString;

/**
 * Class to hold OAuth credentials for Bing
 * <p/>
 * Created by benrandall76@gmail.com on 18/04/2016.
 */
public class BingCredentials {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = BingCredentials.class.getSimpleName();

    private static long then;

    @SerializedName("access_token")
    private final String refreshToken;

    @SerializedName("expires_in")
    private final long expires;

    public long getExpires() {
        return expires;
    }

    /**
     * Constructor
     *
     * @param refreshToken the token
     * @param expires      the token expiry time
     */
    public BingCredentials(@NonNull final String refreshToken, final long expires) {
        this.refreshToken = refreshToken;
        this.expires = expires;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * Check to see if the Bing OAuth token is valid
     *
     * @param ctx the application context
     * @return true if the token is valid. False otherwise
     */
    public static boolean isTokenValid(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "isTokenValid");
        }

        if (UtilsString.notNaked(SPH.getBingToken(ctx)) && System.currentTimeMillis() < SPH.getBingTokenExpiryTime(ctx)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isTokenValid: valid");
            }
            return true;
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isTokenValid: invalid");
            }
            return false;
        }
    }

    /**
     * Check to see if the Bing OAuth token is valid and asynchronously validate if required
     *
     * @param ctx the application context
     */
    public static void refreshTokenIfRequired(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "refreshTokenIfRequired");
        }

        if (SPH.getDefaultTranslationProvider(ctx) == TranslationProvider.TRANSLATION_PROVIDER_BING) {
            if (!isTokenValid(ctx)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "refreshTokenIfRequired invalid. Requesting async");
                }

                if (System.currentTimeMillis() > (then + 5000)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "refreshTokenIfRequired invalid. Requesting async: delay ok");
                    }
                    then = System.currentTimeMillis();
                    new BingOAuth().execute(ctx, false);
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "refreshTokenIfRequired invalid. Requesting async: delay too short");
                    }
                }
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "refreshTokenIfRequired: not using Bing");
            }
        }
    }
}
