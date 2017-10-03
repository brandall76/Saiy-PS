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

package ai.saiy.android.command.battery;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Outcome;

/**
 * Helper class to resolve a battery command locally.
 * <p>
 * Created by benrandall76@gmail.com on 13/06/2016.
 */
public class CommandBatteryLocal {

    /**
     * Resolve the required command returning an {@link Outcome} object
     *
     * @param ctx       the application context
     * @param voiceData ArrayList<String> containing the voice data
     * @param sl        the {@link SupportedLanguage} we are using to analyse the voice data.
     *                  This is not necessarily the Locale of the device, as the user may be
     *                  multi-lingual and/or have set a custom recognition language in a launcher short-cut.
     * @return {@link Outcome} containing everything we need to respond to the command.
     */
    public Outcome getResponse(@NonNull final Context ctx, @NonNull final ArrayList<String> voiceData,
                               @NonNull final SupportedLanguage sl) {

        final CommandBatteryValues values = new Battery(sl).fetch(ctx, voiceData);
        return new BatteryInformation(ctx, sl, new Outcome(), values.getTypeString()).getInfo(values.getType());
    }
}
