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

package ai.saiy.android.api.helper;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import ai.saiy.android.custom.CustomCommandHelper;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsList;

/**
 * Created by benrandall76@gmail.com on 14/07/2016.
 */

public class BlackListHelper {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = BlackListHelper.class.getSimpleName();

    private final Type type = new TypeToken<ArrayList<String>>() {
    }.getType();

    /**
     * Fetch an ArrayList of currently blacklisted application package names
     *
     * @param ctx the application context
     * @return an ArrayList containing blacklisted application package names or an empty array
     */
    public ArrayList<String> fetch(@NonNull final Context ctx) {

        final ArrayList<String> blackListArray;

        if (haveBlacklist(ctx)) {

            final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

            try {
                blackListArray = gson.fromJson(SPH.getBlacklistArray(ctx), type);
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "blackListArray: " + gson.toJson(blackListArray));
                }
                return blackListArray;
            } catch (final JsonSyntaxException e) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "blackListArray: JsonSyntaxException");
                    e.printStackTrace();
                }
            } catch (final NullPointerException e) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "blackListArray: NullPointerException");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "blackListArray: Exception");
                    e.printStackTrace();
                }
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "blackListArray: empty");
            }
        }

        return new ArrayList<>();
    }

    /**
     * Save the array list of blacklisted application package names into the the user's shared preferences,
     * once it has been serialised.
     * <p>
     * Additionally, remove any current custom commands the application may have registered.
     *
     * @param ctx            the application context
     * @param blackListArray the array list of blacklisted application package names
     */
    public void save(@NonNull final Context ctx, @Nullable final ArrayList<String> blackListArray) {

        if (UtilsList.notNaked(blackListArray)) {

            CustomCommandHelper.deleteCommandsForPackage(ctx, blackListArray);

            final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            final String gsonString = gson.toJson(blackListArray, type);

            if (DEBUG) {
                MyLog.i(CLS_NAME, "save: gsonString: " + gsonString);
            }

            SPH.setBlacklistArray(ctx, gsonString);
        } else {
            SPH.setBlacklistArray(ctx, null);
        }
    }

    /**
     * Check if the calling package is currently blacklisted
     *
     * @param ctx         the application context
     * @param packageName the calling package name
     * @return true if the application is blacklisted, false otherwise
     */
    public boolean isBlacklisted(@NonNull final Context ctx, @NonNull final String packageName) {

        final ArrayList<String> blacklistArray = fetch(ctx);

        for (final String name : blacklistArray) {
            if (name.matches(packageName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if we currently have any applications blacklisted
     *
     * @param ctx the application context
     * @return true if there are blacklisted applications, false otherwise
     */
    private boolean haveBlacklist(@NonNull final Context ctx) {
        return SPH.getBlacklistArray(ctx) != null;
    }
}
