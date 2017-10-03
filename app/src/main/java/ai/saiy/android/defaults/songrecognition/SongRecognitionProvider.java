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

package ai.saiy.android.defaults.songrecognition;

import android.content.Context;
import android.support.annotation.NonNull;

import ai.saiy.android.R;
import ai.saiy.android.localisation.SaiyResourcesHelper;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;

/**
 * Created by benrandall76@gmail.com on 10/06/2016.
 */
public enum SongRecognitionProvider {

    UNKNOWN,
    SHAZAM,
    SHAZAM_ENCORE,
    SOUND_HOUND,
    SOUND_HOUND_PREMIUM,
    TRACK_ID,
    GOOGLE;

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = SongRecognitionProvider.class.getSimpleName();

    public static final String MEDIA_RECOGNIZE = "saiy.intent.action.MEDIA_RECOGNIZE";

    public static final String SHAZAM_ACTION = "com.shazam.android.intent.actions.START_TAGGING";
    public static final String SOUND_HOUND_ACTION = "com.soundhound.android.ID_NOW_EXTERNAL";
    public static final String TRACK_ID_ACTION = "com.sonyericsson.trackid.intent.action.LAUNCH";
    public static final String GOOGLE_ACTION = "com.google.android.googlequicksearchbox.MUSIC_SEARCH";

    /**
     * Get the supported application's name
     *
     * @param ctx      the application context
     * @param sl       the {@link SupportedLanguage}
     * @param provider the {@link SongRecognitionProvider}
     * @return the application's name
     */
    public static String getApplicationName(@NonNull final Context ctx, @NonNull final SupportedLanguage sl,
                                            @NonNull final SongRecognitionProvider provider) {

        switch (provider) {

            case SHAZAM:
                return ctx.getString(ai.saiy.android.R.string.shazam);
            case SHAZAM_ENCORE:
                return ctx.getString(ai.saiy.android.R.string.shazam_encore);
            case SOUND_HOUND:
                return ctx.getString(ai.saiy.android.R.string.sound_hound);
            case SOUND_HOUND_PREMIUM:
                return ctx.getString(ai.saiy.android.R.string.sound_hound_premium);
            case TRACK_ID:
                return ctx.getString(ai.saiy.android.R.string.track_id);
            case GOOGLE:
                return ctx.getString(R.string.google_capital) +
                        SaiyResourcesHelper.getStringResource(ctx, sl, ai.saiy.android.R.string.sound_search);
        }

        return "";
    }

    public static SongRecognitionProvider getProvider(int provider) {

        final SongRecognitionProvider[] providers = values();

        if (providers.length > provider) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getProvider: " + providers[provider].toString());
            }
            return providers[provider];
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getProvider: out of bounds. Returning UNKNOWN");
            }
            return UNKNOWN;
        }
    }
}
