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

package ai.saiy.android.intent;

/**
 * Created by benrandall76@gmail.com on 16/04/2016.
 */
public class IntentConstants {

    public static final int SETTINGS_ACCESSIBILITY = 1;
    public static final int SETTINGS_VOICE_SEARCH = 2;
    public static final int SETTINGS_INPUT_METHOD = 3;
    public static final int SETTINGS_USAGE_STATS = 4;
    public static final int SETTINGS_VOLUME = 5;
    public static final int SETTINGS_TEXT_TO_SPEECH = 6;
    public static final int SETTINGS_ADD_ACCOUNT = 7;

    public static final String COMPONENT_VOICE_SEARCH_PREFERENCES = "com.google.android.voicesearch.VoiceSearchPreferences";
    public static final String COMPONENT_VOICE_SEARCH_PREFERENCES_VELVET = "com.google.android.apps.gsa.velvet.ui.settings.VoiceSearchPreferences";

    public static final String ACTION_SAIY_VOICE_DATA = "ai.saiy.android.action.VOICE_DATA";
    public static final String EXTRA_VOICE_DATA = "voice_data";
    public static final String ACTION_TEXT_TO_SPEECH = "com.android.settings.TTS_SETTINGS";

    public static final String AMAZON_PACKAGE_URL = "";
    public static final String PLAY_STORE_PACKAGE_URL = "https://play.google.com/store/apps/details?id=";
    public static final String PLAY_STORE_SEARCH_URL = "https://play.google.com/store/search?q=";
    public static final String PLAY_STORE_SEARCH_URL_LEGACY = "market://search?q=";
    public static final String PLAY_STORE_APPS_EXTENSION = "&c=apps";

    public static final String PACKAGE_NAME_GOOGLE_NOW = "com.google.android.googlequicksearchbox";
    public static final String ACTIVITY_GOOGLE_NOW_SEARCH = ".SearchActivity";

    public static final String PACKAGE_NAME_WOLFRAM_ALPHA = "com.wolfram.android.alpha";
    public static final String ACTIVITY_WOLFRAM_ALPHA_SEARCH = ".activity.SearchResultsActivity";
    public static final String INTENT_SEARCH_WOLFRAM_ALPHA = "com.wolfram.android.alpha.intent.action.DO_QUERY_SUGGESTION";

    public static final String TEXT_PLAIN = "text/plain";
    public static final String MAILTO = "mailto:";
    public static final String PACKAGE = "package:";

}
