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

package ai.saiy.android.memory;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

/**
 * Helper class to handle retrieving {@link Memory} objects
 * <p/>
 * Created by benrandall76@gmail.com on 20/04/2016.
 */
public class MemoryHelper {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = MemoryHelper.class.getSimpleName();

    /**
     * Check if we have a memory of a previous action or utterance
     *
     * @param ctx the application context
     * @return true if a {@link Memory} is stored
     */
    public static boolean hasMemory(@NonNull final Context ctx) {
        return SPH.getMemory(ctx) != null;
    }

    /**
     * Get the {@link Memory} we have stored
     *
     * @param ctx the application context
     * @return the {@link Memory} object
     */
    public static Memory getMemory(@NonNull final Context ctx) {

        final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        final Memory memory;

        if (hasMemory(ctx)) {

            try {
                memory = gson.fromJson(SPH.getMemory(ctx), Memory.class);
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "memory: " + gson.toJson(memory));
                }
                return memory;
            } catch (final JsonSyntaxException e) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "memory: JsonSyntaxException");
                    e.printStackTrace();
                }
            } catch (final NullPointerException e) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "memory: NullPointerException");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "memory: Exception");
                    e.printStackTrace();
                }
            }
        }

        return getUnknown(ctx);
    }

    /**
     * No {@link Memory} was stored, so we need to let the user know. We get the utterance we will
     * announce from the {@link PersonalityResponse} class.
     *
     * @param ctx the application context
     * @return a constructed {@link Memory} object
     */
    private static Memory getUnknown(@NonNull final Context ctx) {

        final String vrLanguage = SPH.getVRLocale(ctx).toString();
        final String ttsLanguage = SPH.getTTSLocale(ctx).toString();
        final ArrayList<String> utteranceArray = new ArrayList<>();
        final int action = LocalRequest.ACTION_SPEAK_ONLY;
        final CC command = CC.COMMAND_UNKNOWN;
        final int condition = Condition.CONDITION_NONE;
        final SupportedLanguage sl = SupportedLanguage.getSupportedLanguage(SPH.getVRLocale(ctx));
        final String utterance = PersonalityResponse.getNoMemory(ctx, sl);

        return new Memory(action, vrLanguage, ttsLanguage, utterance, utteranceArray,
                command, condition, sl);
    }
}
