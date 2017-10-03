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
 * Created by benrandall76@gmail.com on 03/06/2016.
 */
public class APIAIConfiguration {

    /**
     * Prevent instantiation
     */
    public APIAIConfiguration() {
        throw new IllegalArgumentException(Resources.getSystem().getString(android.R.string.no));
    }

    public static final String CLIENT_ACCESS_TOKEN = "_your_value_here_";
    public static final String DEVELOPER_ACCESS_TOKEN = "_your_value_here_";
}
