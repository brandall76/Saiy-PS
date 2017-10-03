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

package ai.saiy.android.cognitive.motion.provider.google;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.location.DetectedActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

/**
 * Helper class to store the most recent ActivityRecognition event in the user's shared preferences
 * <p>
 * Created by benrandall76@gmail.com on 15/08/2016.
 */
public class MotionHelper {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = MotionHelper.class.getSimpleName();

    /**
     * Check if we have a recent {@link Motion} object stored
     *
     * @param ctx the application context
     * @return true if a {@link Motion} is stored
     */
    public static boolean haveMotion(@NonNull final Context ctx) {
        return SPH.getMotion(ctx) != null;
    }


    /**
     * Store the {@link Motion} object in the shared preferences
     *
     * @param ctx    the application context
     * @param motion {@link Motion} object
     */
    public static void setMotion(@NonNull final Context ctx, @NonNull final Motion motion) {

        final String gsonString = new GsonBuilder().disableHtmlEscaping().create().toJson(motion);

        if (DEBUG) {
            MyLog.i(CLS_NAME, "setMotion: gsonString: " + gsonString);
        }

        SPH.setMotion(ctx, gsonString);
        reactMotion(ctx, motion);
    }

    /**
     * Check if we need to react to the detected ActivityRecognition type.
     *
     * @param ctx    the application context
     * @param motion the detection {@link Motion} object
     */
    private static void reactMotion(@NonNull final Context ctx, @NonNull final Motion motion) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "reactMotion");
        }

        switch (motion.getType()) {

            case DetectedActivity.WALKING:
                break;
            case DetectedActivity.IN_VEHICLE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "reactMotion: IN_VEHICLE");
                }

                if (SPH.getHotwordDriving(ctx)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "reactMotion: IN_VEHICLE: enabled");
                    }

                    final LocalRequest request = new LocalRequest(ctx);
                    request.prepareDefault(LocalRequest.ACTION_START_HOTWORD, null);
                    request.execute();
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "reactMotion: IN_VEHICLE: disabled");
                    }
                }

                break;
            case DetectedActivity.ON_BICYCLE:
                break;
            case DetectedActivity.ON_FOOT:
                break;
            case DetectedActivity.RUNNING:
                break;
            case DetectedActivity.STILL:
                break;
            case DetectedActivity.TILTING:
                break;
            case DetectedActivity.UNKNOWN:
                break;
            default:
                break;
        }
    }

    /**
     * Get the {@link Motion} we have stored
     *
     * @param ctx the application context
     * @return the {@link Motion} object or a created one if none was present
     */
    public static Motion getMotion(@NonNull final Context ctx) {

        final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        final Motion motion;

        if (haveMotion(ctx)) {

            try {
                motion = gson.fromJson(SPH.getMemory(ctx), Motion.class);
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "motion: " + gson.toJson(motion));
                }
                return motion;
            } catch (final JsonSyntaxException e) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "motion: JsonSyntaxException");
                    e.printStackTrace();
                }
            } catch (final NullPointerException e) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "motion: NullPointerException");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "motion: Exception");
                    e.printStackTrace();
                }
            }
        }

        return getUnknown();
    }

    /**
     * No {@link Motion} has been stored, so return an empty {@link Motion} object that can be identified as unknown.
     *
     * @return a constructed {@link Motion} object
     */
    private static Motion getUnknown() {
        return new Motion(DetectedActivity.UNKNOWN, 0, 0L);
    }

}
