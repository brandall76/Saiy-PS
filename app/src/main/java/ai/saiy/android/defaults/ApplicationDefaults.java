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

package ai.saiy.android.defaults;

import android.content.Context;
import android.support.annotation.NonNull;

import ai.saiy.android.defaults.songrecognition.SongRecognitionProvider;
import ai.saiy.android.utils.SPH;

/**
 * Created by benrandall76@gmail.com on 10/06/2016.
 */
public class ApplicationDefaults {

    /**
     * Check if the user has already set a default song recognition provider
     *
     * @param ctx the application context
     * @return the default {@link SongRecognitionProvider} object or {@link SongRecognitionProvider#UNKNOWN} if
     * the user has not set a default
     */
    public static SongRecognitionProvider getSongRecognitionProvider(@NonNull final Context ctx) {
        return SPH.getDefaultSongRecognition(ctx);
    }

}
