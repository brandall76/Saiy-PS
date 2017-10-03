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

package ai.saiy.android.api.helper;

import android.content.res.Resources;

/**
 * Class to hold the constant values of callback types.
 * <p>
 * Created by benrandall76@gmail.com on 03/03/2016.
 */
public final class CallbackType {

    /**
     * Prevent instantiation
     */
    public CallbackType() {
        throw new IllegalArgumentException(Resources.getSystem().getString(android.R.string.no));
    }

    public static final int CB_UNKNOWN = 0;
    public static final int CB_INTERRUPTED = 1;
    public static final int CB_ERROR_NO_MATCH = 2;
    public static final int CB_ERROR_NETWORK = 3;
    public static final int CB_ERROR_BUSY = 4;
    public static final int CB_ERROR_DENIED = 5;
    public static final int CB_ERROR_SAIY = 6;
    public static final int CB_ERROR_DEVELOPER = 7;

    public static final int CB_UTTERANCE_COMPLETED = 99;
    public static final int CB_RESULTS_RECOGNITION = 98;

}
