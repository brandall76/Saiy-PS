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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import ai.saiy.android.cognitive.emotion.provider.beyondverbal.audio.AudioConfig;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.language.SupportedLanguageBV;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.user.MetaData;
import ai.saiy.android.utils.MyLog;

/**
 * Utility class to format the request body parameters into a format the API will accept.
 * <p>
 * Created by benrandall76@gmail.com on 08/06/2016.
 */
public class StartRequestBody {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = StartRequestBody.class.getSimpleName();

    private static final String DATA_FORMAT = "dataFormat";
    private static final String META_DATA = "metadata";
    private static final String DISPLAY_LANGUAGE = "displayLang";

    private final AudioConfig config;
    private final MetaData metaData;
    private final SupportedLanguageBV sl;

    /**
     * Constructor
     *
     * @param config   the {@link AudioConfig}
     * @param metaData the {@link MetaData}. Optional, so can be null
     * @param sl       the {@link SupportedLanguageBV}. Optional, so can be null
     */
    public StartRequestBody(@NonNull final AudioConfig config, @Nullable final MetaData metaData,
                            @Nullable final SupportedLanguageBV sl) {
        this.config = config;
        this.metaData = metaData;
        this.sl = sl;
    }

    /**
     * Method to format the body parameters into an accepted JSON format
     *
     * @return an {@link JSONObject} containing the body parameters
     */
    public JSONObject prepare() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "prepare");
        }

        final JSONObject object = new JSONObject();

        try {

            object.put(DATA_FORMAT, config.getConfigJson());

            if (metaData != null) {
                object.put(META_DATA, metaData.getMetaJSON());
            }

            if (sl != null) {
                object.put(DISPLAY_LANGUAGE, sl.getServerFormat());
            }

        } catch (final JSONException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "prepare JSONException");
                e.printStackTrace();
            }
        }

        return object;
    }
}
