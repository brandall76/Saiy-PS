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

package ai.saiy.android.cognitive.identity.provider.microsoft;

import android.support.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import ai.saiy.android.utils.UtilsString;

/**
 * Created by benrandall76@gmail.com on 16/09/2016.
 */

public class Speaker {

    public static final String EXTRA_IDENTIFY_OUTCOME = "extra_identify_outcome";
    public static final String EXTRA_IDENTITY_OUTCOME = "extra_identity_outcome";

    public static final int ERROR_USER_CANCELLED = 1;
    public static final int ERROR_AUDIO = 2;
    public static final int ERROR_NETWORK = 3;
    public static final int ERROR_FILE = 4;

    public enum Confidence {

        HIGH,
        NORMAL,
        LOW,
        UNDEFINED,
        ERROR;

        /**
         * Get the {@link Confidence} from the string equivalent
         *
         * @param level the string level
         * @return the equivalent {@link Confidence}
         */
        public static Confidence getConfidence(@Nullable final String level) {

            if (UtilsString.notNaked(level)) {

                if (StringUtils.containsIgnoreCase(HIGH.name(), level)) {
                    return HIGH;
                } else if (StringUtils.containsIgnoreCase(NORMAL.name(), level)) {
                    return NORMAL;
                } else if (StringUtils.containsIgnoreCase(LOW.name(), level)) {
                    return LOW;
                }
            }

            return UNDEFINED;
        }
    }

    public enum Status {

        NOTSTARTED,
        RUNNING,
        FAILED,
        SUCCEEDED,
        UNDEFINED;

        /**
         * Get the {@link Status} from the string equivalent
         *
         * @param status the string status
         * @return the equivalent {@link Status}
         */
        public static Status getStatus(@Nullable final String status) {

            if (UtilsString.notNaked(status)) {

                if (StringUtils.containsIgnoreCase(NOTSTARTED.name(), status)) {
                    return NOTSTARTED;
                } else if (StringUtils.containsIgnoreCase(RUNNING.name(), status)) {
                    return RUNNING;
                } else if (StringUtils.containsIgnoreCase(FAILED.name(), status)) {
                    return FAILED;
                } else if (StringUtils.containsIgnoreCase(SUCCEEDED.name(), status)) {
                    return SUCCEEDED;
                }
            }

            return UNDEFINED;
        }
    }

}
