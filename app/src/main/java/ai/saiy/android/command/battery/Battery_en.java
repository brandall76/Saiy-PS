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
import android.util.Pair;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SaiyResources;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

/**
 * Helper class to resolve battery commands
 * <p>
 * Created by benrandall76@gmail.com on 12/06/2016.
 */
public class Battery_en {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Battery_en.class.getSimpleName();

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    private static String battery;
    private static String temperature;
    private static String level;
    private static String percentage;
    private static String percent;
    private static String voltage;
    private static String volts;
    private static String status;
    private static String health;


    /**
     * Constructor
     * <p>
     * Used by a {@link Callable} to prepare everything that is need when
     * {@link Callable#call()} is executed with the {@link SaiyResources} managed elsewhere.
     *
     * @param sr         the {@link SaiyResources}
     * @param sl         the {@link SupportedLanguage}
     * @param voiceData  the array of voice data
     * @param confidence the array of confidence scores
     */
    public Battery_en(@NonNull final SaiyResources sr, @NonNull final SupportedLanguage sl,
                      @NonNull final ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = sl;
        this.voiceData = voiceData;
        this.confidence = confidence;

        if (battery == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "strings initialised");
            }
        }

    }

    private static void initStrings(@NonNull final SaiyResources sr) {
        battery = sr.getString(ai.saiy.android.R.string.battery);
        temperature = sr.getString(ai.saiy.android.R.string.temperature);
        level = sr.getString(ai.saiy.android.R.string.level);
        percentage = sr.getString(ai.saiy.android.R.string.percentage);
        percent = sr.getString(ai.saiy.android.R.string.percent);
        voltage = sr.getString(ai.saiy.android.R.string.voltage);
        volts = sr.getString(ai.saiy.android.R.string.volts);
        status = sr.getString(ai.saiy.android.R.string.status);
        health = sr.getString(ai.saiy.android.R.string.health);
    }

    /**
     * Iterate through the voice data array to see if we can match the command.
     * <p>
     * Note - As the speech array will never contain more than ten entries, to consider the static
     * nature and performance issues here, perhaps implementing a matcher, would probably be overkill.
     *
     * @return an Array list of Pairs containing the {@link CC} and float confidence
     */
    public ArrayList<Pair<CC, Float>> detectCallable() {

        final long then = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();

        if (UtilsList.notNaked(voiceData) && UtilsList.notNaked(confidence)
                && voiceData.size() == confidence.length) {

            final Locale loc = sl.getLocale();

            String vdLower;
            int size = voiceData.size();
            for (int i = 0; i < size; i++) {
                vdLower = voiceData.get(i).toLowerCase(loc).trim();

                if (vdLower.contains(battery)
                        && ((vdLower.contains(temperature)
                        || vdLower.contains(level)
                        || vdLower.contains(percentage)
                        || vdLower.contains(percent)
                        || vdLower.contains(voltage)
                        || vdLower.contains(volts)
                        || vdLower.contains(status)
                        || vdLower.contains(health)))) {

                    toReturn.add(new Pair<>(CC.COMMAND_BATTERY, confidence[i]));
                }
            }
        }

        if (DEBUG) {
            MyLog.i(CLS_NAME, "battery: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }

        return toReturn;
    }

    /**
     * Static method.
     * <p>
     * Iterate through the voice data array and return an element of the highest confidence match.
     *
     * @param ctx       the application context
     * @param voiceData ArrayList<String> containing the voice data
     * @param sl        the {@link SupportedLanguage}
     * @return a {@link CommandBatteryValues} object containing the required parameters
     */
    public static CommandBatteryValues sortBattery(@NonNull final Context ctx, final ArrayList<String> voiceData,
                                                   @NonNull final SupportedLanguage sl) {

        final long then = System.nanoTime();
        final Locale loc = sl.getLocale();

        final CommandBatteryValues values = new CommandBatteryValues();
        values.setTypeString("");
        values.setType(CommandBatteryValues.Type.UNKNOWN);

        if (battery == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            final SaiyResources sr = new SaiyResources(ctx, sl);
            initStrings(sr);
            sr.reset();
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "strings initialised");
            }
        }

        String vdLower;
        for (final String vd : voiceData) {
            vdLower = vd.toLowerCase(loc).trim();

            if (vdLower.contains(battery)) {

                if (vdLower.contains(temperature)) {
                    values.setTypeString(temperature);
                    values.setType(CommandBatteryValues.Type.TEMPERATURE);
                    break;
                } else if (vdLower.contains(level)
                        || vdLower.contains(percent)
                        || vdLower.contains(percentage)) {
                    values.setTypeString(level);
                    values.setType(CommandBatteryValues.Type.PERCENTAGE);
                    break;
                } else if (vdLower.contains(voltage)
                        || vdLower.contains(volts)) {
                    values.setTypeString(voltage);
                    values.setType(CommandBatteryValues.Type.VOLTAGE);
                    break;
                } else if (vdLower.contains(status)) {
                    values.setTypeString(status);
                    values.setType(CommandBatteryValues.Type.STATUS);
                    break;
                } else if (vdLower.contains(health)) {
                    values.setTypeString(health);
                    values.setType(CommandBatteryValues.Type.HEALTH);
                    break;
                }
            }
        }

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }

        return values;
    }
}
