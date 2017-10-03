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

package ai.saiy.android.recognition;

import android.support.annotation.NonNull;

import ai.saiy.android.utils.MyLog;

/**
 * Set the global state of recognition, so regardless of the implementation used, it can be
 * quickly cancelled if required.
 * <p/>
 * This is a necessary evil. Must be reset under all error circumstances or after multiple attempts.
 * <p/>
 * Created by benrandall76@gmail.com on 08/02/2016.
 */
public class Recognition {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Recognition.class.getSimpleName();

    private static volatile State state = State.IDLE;

    public enum State {
        IDLE,
        LISTENING,
        PROCESSING
    }

    /**
     * Set the global state of recognition
     */
    public static void setState(@NonNull final State newState) {

        state = newState;

        switch (state) {
            case IDLE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "setState: IDLE");
                }
                break;
            case LISTENING:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "setState: LISTENING");
                }
                break;
            case PROCESSING:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "setState: PROCESSING");
                }
                break;
        }
    }

    /**
     * Get the global state of recognition
     */
    public static State getState() {

        switch (state) {
            case IDLE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getState: IDLE");
                }
                break;
            case LISTENING:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getState: LISTENING");
                }
                break;
            case PROCESSING:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getState: PROCESSING");
                }
                break;
        }

        return state;
    }
}
