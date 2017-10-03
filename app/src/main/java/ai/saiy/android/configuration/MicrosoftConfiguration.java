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

/**
 * Created by benrandall76@gmail.com on 17/04/2016.
 */
public class MicrosoftConfiguration {

    /**
     * Prevent instantiation
     */
    public MicrosoftConfiguration() {
        throw new IllegalArgumentException(Resources.getSystem().getString(android.R.string.no));
    }

    public static final String MS_TRANSLATE_SUBSCRIPTION_KEY = "_your_value_here_";

    public static final String OXFORD_KEY_1 = "_your_value_here_";
    public static final String OXFORD_KEY_2 = "_your_value_here_";
    public static final String LUIS_APP_ID = "_your_value_here_";
    public static final String LUIS_SUBSCRIPTION_ID = "_your_value_here_";

    public static final String OCP_APIM_KEY_1 = "_your_value_here_";
    public static final String OCP_APIM_KEY_2 = "_your_value_here_";

}
