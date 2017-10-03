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

package ai.saiy.android.cognitive.emotion.provider.beyondverbal.containers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import ai.saiy.android.cognitive.emotion.provider.beyondverbal.http.BVAuthRequest;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

/**
 * Class to serialise the credential information from Beyond Verbal
 * <p>
 * Created by benrandall76@gmail.com on 09/06/2016.
 */
public class BVCredentials {

    private transient static final boolean DEBUG = MyLog.DEBUG;
    private transient static final String CLS_NAME = BVCredentials.class.getSimpleName();

    @SerializedName("access_token")
    private final String accessToken;

    @SerializedName("token_type")
    private final String tokenType;

    @SerializedName("expires_in")
    private final long expiresIn;

    @SerializedName("expiry_time")
    private long expiryTime;

    public BVCredentials(final String accessToken, final String tokenType, final long expiresIn,
                         final long expiryTime) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.expiryTime = expiryTime;
    }

    /**
     * Method to check if the token is valid
     *
     * @param bvCredentials the {@link BVCredentials} containing the most recent token credentials.
     * @return true if the token is valid, false otherwise.
     */
    public static boolean isTokenValid(@Nullable final BVCredentials bvCredentials) {
        return bvCredentials != null && System.currentTimeMillis() < bvCredentials.getExpiryTime();
    }

    /**
     * Convenience method to check if the most recent access token remains valid and automatically
     * request one if required.
     *
     * @param ctx    the application context
     * @param apiKey the API key
     * @return an {@link Pair} of which the first parameter will denote success and the second an
     * {@link BVCredentials} object, containing the token credentials. If the request was unsuccessful,
     * the second parameter may be null.
     */
    public static Pair<Boolean, BVCredentials> refreshTokenIfRequired(@NonNull final Context ctx,
                                                                      @NonNull final String apiKey) {
        final BVCredentials bvCredentials = getLastToken(ctx);

        if (isTokenValid(bvCredentials)) {
            return new Pair<>(true, bvCredentials);
        } else {
            return new BVAuthRequest(ctx.getApplicationContext(), apiKey).getToken();
        }
    }

    /**
     * Method to get the most recent access token stored in the user {@link SPH )
     * shared preferences.
     *
     * @param ctx the application context
     * @return the most recent {@link BVCredentials} object
     */
    private static BVCredentials getLastToken(@NonNull final Context ctx) {
        final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        final BVCredentials bvCredentials;

        try {
            bvCredentials = gson.fromJson(SPH.getBeyondVerbalCredentials(ctx), BVCredentials.class);
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getLastToken: " + gson.toJson(bvCredentials));
            }
            return bvCredentials;
        } catch (final JsonSyntaxException e) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getLastToken: JsonSyntaxException");
                e.printStackTrace();
            }
        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getLastToken: NullPointerException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getLastToken: Exception");
                e.printStackTrace();
            }
        }

        return null;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(final long expiryTime) {
        this.expiryTime = (System.currentTimeMillis() + (expiryTime * 1000));
    }
}
