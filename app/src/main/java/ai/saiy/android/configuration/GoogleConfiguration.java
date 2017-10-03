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

package ai.saiy.android.configuration;

import android.content.res.Resources;

import com.google.auth.oauth2.AccessToken;

import java.util.Date;

/**
 * Enter your Google Chromium Speech API key below. You need to register in the Google Group and
 * enable this in your API console. Without doing both, it WILL NOT WORK!
 * <p/>
 * Created by benrandall76@gmail.com on 12/02/2016.
 */
public final class GoogleConfiguration {

    /**
     * Prevent instantiation
     */
    public GoogleConfiguration() {
        throw new IllegalArgumentException(Resources.getSystem().getString(android.R.string.no));
    }

    public static final String GOOGLE_SPEECH_API_KEY = "_your_value_here_";
    public static final String GOOGLE_TRANSLATE_API_KEY = "_your_value_here_";

    private static final String GOOGLE_SPEECH_CLOUD_API_KEY = "_your_value_here_";

    public static final AccessToken ACCESS_TOKEN = new AccessToken(GoogleConfiguration.GOOGLE_SPEECH_CLOUD_API_KEY,
            new Date(System.currentTimeMillis() + 3600000L));
}
