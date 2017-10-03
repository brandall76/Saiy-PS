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

package ai.saiy.android.command.battery;

import android.content.Context;
import android.support.annotation.NonNull;

import ai.saiy.android.localisation.SaiyResources;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

/**
 * Class to hold information and constants to resolve a battery command.
 * <p>
 * Created by benrandall76@gmail.com on 01/06/2016.
 */
public class CommandBatteryValues {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandBatteryValues.class.getSimpleName();

    /**
     * Constant battery request types
     */
    public enum Type {
        UNKNOWN,
        TEMPERATURE,
        PERCENTAGE,
        VOLTAGE,
        STATUS,
        HEALTH
    }

    private Type type;
    private String typeString;
    private long startIndex;
    private long endIndex;
    private int[][] ranges;

    public int[][] getRanges() {
        return ranges;
    }

    public void setRanges(@NonNull final int[][] ranges) {
        this.ranges = ranges;
    }

    public String getTypeString() {
        return typeString;
    }

    public void setTypeString(@NonNull final String typeString) {
        this.typeString = typeString;
    }

    public Type getType() {
        return type;
    }

    public void setType(@NonNull final Type type) {
        this.type = type;
    }

    public long getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(final long endIndex) {
        this.endIndex = endIndex;
    }

    public void setStartIndex(final long startIndex) {
        this.startIndex = startIndex;
    }

    public long getStartIndex() {
        return startIndex;
    }

    /**
     * Method to convert the spoken battery command information type, to an {@link Type} for use as
     * a constant in further methods.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return one of {@link Type} or {@link Type#UNKNOWN} if the voice data cannot be resolved.
     */
    public Type stringToType(@NonNull final Context ctx, @NonNull final SupportedLanguage sl,
                             @NonNull final String typeString) {

        if (UtilsString.notNaked(typeString)) {

            final SaiyResources sr = new SaiyResources(ctx, sl);
            final String type = typeString.toLowerCase(sl.getLocale()).trim();

            final String temperature = sr.getString(ai.saiy.android.R.string.temperature);
            final String percent = sr.getString(ai.saiy.android.R.string.percent);
            final String percentage = sr.getString(ai.saiy.android.R.string.percentage);
            final String level = sr.getString(ai.saiy.android.R.string.level);
            final String voltage = sr.getString(ai.saiy.android.R.string.voltage);
            final String status = sr.getString(ai.saiy.android.R.string.status);
            final String health = sr.getString(ai.saiy.android.R.string.health);
            sr.reset();

            if (type.matches(temperature)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "stringToType: " + Type.TEMPERATURE.name());
                }
                return Type.TEMPERATURE;
            } else if (type.matches(percent)
                    || type.matches(percentage)
                    || type.matches(level)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "stringToType: " + Type.PERCENTAGE.name());
                }
                return Type.PERCENTAGE;
            } else if (type.matches(voltage)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "stringToType: " + Type.VOLTAGE.name());
                }
                return Type.VOLTAGE;
            } else if (type.matches(status)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "stringToType: " + Type.STATUS.name());
                }
                return Type.STATUS;
            } else if (type.matches(health)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "stringToType: " + Type.HEALTH.name());
                }
                return Type.HEALTH;
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "stringToType: " + Type.UNKNOWN.name());
                }
                return Type.UNKNOWN;
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "stringToType: literal naked");
            }
            return Type.UNKNOWN;
        }
    }
}
