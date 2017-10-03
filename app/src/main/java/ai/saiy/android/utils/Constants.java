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

package ai.saiy.android.utils;

import android.content.res.Resources;

/**
 * A collection of constants that are not suitable to store in a resource file.
 * <p/>
 * Created by benrandall76@gmail.com on 10/02/2016.
 */
public final class Constants {

    /**
     * Prevent instantiation
     */
    public Constants() {
        throw new IllegalArgumentException(Resources.getSystem().getString(android.R.string.no));
    }

    public static final String SAIY = "Saiy";
    public static final String SAIY_WEB_URL = "http://saiy.ai";
    public static final String SAIY_PRIVACY_URL = "http://saiy.ai/privacy.html";
    public static final String SAIY_TOU_URL = "http://saiy.ai/terms.html";
    public static final String SAIY_ENQUIRIES_EMAIL = "commercial@saiy.ai";
    public static final String SAIY_FEEDBACK_EMAIL = "feedback@saiy.ai";
    public static final String SAIY_GITHUB_URL = "https://github.com/brandall76/saiy";
    public static final String SAIY_TWITTER_HANDLE = "http://twitter.com/brandall76";
    public static final String SAIY_GOOGLE_PLUS_URL = "https://plus.google.com/100131487913427971091";
    public static final String SAIY_XDA_URL = "http://forum.xda-developers.com/showthread.php?t=1508195";

    public static final String DEFAULT_FILE_PREFIX = "default_file";
    public static final String DEFAULT_AUDIO_FILE_SUFFIX = "wav";
    public static final String DEFAULT_AUDIO_FILE_PREFIX = "default_audio_file";
    public static final String DEFAULT_TEMP_FILE_SUFFIX = "txt";
    public static final String DEFAULT_TEMP_FILE_PREFIX = "default_temp_file";
    public static final String OGG_AUDIO_FILE_SUFFIX = "ogg";

    public static final String ENCODING_UTF8 = "UTF-8";
    public static final String HTTP_GET = "GET";
    public static final String HTTP_POST = "POST";

    public static final String STUDIO_AHREMARK_WEB_URL = "http://www.studioahremark.com/";
    public static final String SCHLAPA_WEB_URL = "https://schlapa.net";

    public static final String LICENSE_URL_GOOGLE_PLAY_SERVICES = "https://developers.google.com/android/guides/overview";
    public static final String LICENSE_URL_GSON = "https://github.com/google/gson";
    public static final String LICENSE_URL_VOLLEY = "https://github.com/mcxiaoke/android-volley";
    public static final String LICENSE_URL_TWO_FORTY_FOUR = "https://github.com/twofortyfouram/android-plugin-api-for-locale";
    public static final String LICENSE_URL_KAAREL_KALJURAND = "https://github.com/Kaljurand/speechutils";
    public static final String LICENSE_URL_MICROSOFT_TRANSLATOR = "https://github.com/boatmeme/microsoft-translator-java-api";
    public static final String LICENSE_URL_APACHE_COMMONS = "https://commons.apache.org";
    public static final String LICENSE_URL_SIMMETRICS = "https://github.com/Simmetrics/simmetrics";
    public static final String LICENSE_URL_NUANCE_SPEECHKIT = "https://github.com/NuanceDev/speechkit-android";
    public static final String LICENSE_URL_GUAVA = "https://github.com/google/guava";
    public static final String LICENSE_URL_MICROSOFT_COGNITIVE = "https://www.microsoft.com/cognitive-services";
    public static final String LICENSE_URL_API_AI = "https://github.com/api-ai/api-ai-android-sdk";
    public static final String LICENSE_URL_SIMPLE_XML = "https://github.com/ngallagher/simplexml";
    public static final String LICENSE_MATERIAL_ICONS = "https://github.com/Templarian/MaterialDesign";
    public static final String LICENSE_MATERIAL_DIALOGS = "https://github.com/afollestad/material-dialogs";
    public static final String LICENSE_POCKETSPHINX = "https://github.com/cmusphinx/pocketsphinx-android";
    public static final String LICENSE_SOUND_BIBLE = "http://soundbible.com";
}
