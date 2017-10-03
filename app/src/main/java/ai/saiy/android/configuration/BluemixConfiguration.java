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
import android.support.annotation.NonNull;

import java.net.URI;
import java.net.URISyntaxException;

import ai.saiy.android.utils.MyLog;

/**
 * Created by benrandall76@gmail.com on 02/08/2016.
 */

public class BluemixConfiguration {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = BluemixConfiguration.class.getSimpleName();

    /**
     * Prevent instantiation
     */
    public BluemixConfiguration() {
        throw new IllegalArgumentException(Resources.getSystem().getString(android.R.string.no));
    }

    public static final String BLUEMIX_USERNAME = "_your_value_here_";
    public static final String BLUEMIX_PASSWORD = "_your_value_here_";
    public static final String BLUEMIX_SERVICE_URL = "wss://stream.watsonplatform.net/speech-to-text/api";
    public static final String BLUEMIX_SERVICE_URL_EXT = "/v1/recognize?model=";

    public static URI getSpeechURI(@NonNull final String model) {

        try {
            return new URI(BluemixConfiguration.BLUEMIX_SERVICE_URL + BluemixConfiguration.BLUEMIX_SERVICE_URL_EXT
                    + model);
        } catch (final URISyntaxException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "URISyntaxException");
                e.printStackTrace();
            }
        }

        return null;
    }

}
