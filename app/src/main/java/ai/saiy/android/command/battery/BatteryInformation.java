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
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;

import ai.saiy.android.localisation.SaiyResources;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

/**
 * Class to resolve information regarding the device's battery and assign the parameters for the
 * vocal response.
 * <p>
 * Created by benrandall76@gmail.com on 13/06/2016.
 */
public class BatteryInformation {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = BatteryInformation.class.getSimpleName();

    public static final int CELSIUS = 0;
    public static final int FAHRENHEIT = 1;

    private final Intent batteryIntent;

    private final Context mContext;
    private final SupportedLanguage sl;
    private final Outcome outcome;
    private final String typeString;

    /**
     * Constructor
     *
     * @param mContext   the application context
     * @param sl         the {@link SupportedLanguage}
     * @param outcome    the {@link Outcome}
     * @param typeString the String representation of the battery information request type.
     */
    public BatteryInformation(@NonNull final Context mContext, @NonNull SupportedLanguage sl,
                              @NonNull final Outcome outcome, @NonNull final String typeString) {
        this.mContext = mContext;
        this.sl = sl;
        this.outcome = outcome;
        this.typeString = typeString;

        batteryIntent = mContext.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    /**
     * Method to assign the response parameters to the {@link Outcome}
     *
     * @param type the {@link CommandBatteryValues.Type}
     * @return the {@link Outcome}
     */
    public Outcome getInfo(@NonNull final CommandBatteryValues.Type type) {

        switch (type) {

            case TEMPERATURE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getInfo: " + CommandBatteryValues.Type.TEMPERATURE.name());
                }
                getTemperature();
                break;
            case PERCENTAGE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getInfo: " + CommandBatteryValues.Type.PERCENTAGE.name());
                }
                getPercentage();
                break;
            case VOLTAGE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getInfo: " + CommandBatteryValues.Type.VOLTAGE.name());
                }
                getVoltage();
                break;
            case STATUS:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getInfo: " + CommandBatteryValues.Type.STATUS.name());
                }
                getStatus();
                break;
            case HEALTH:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getInfo: " + CommandBatteryValues.Type.HEALTH.name());
                }
                getHealth();
                break;
            case UNKNOWN:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getInfo: " + CommandBatteryValues.Type.UNKNOWN.name());
                }
                outcome.setOutcome(Outcome.FAILURE);
                outcome.setUtterance(PersonalityResponse.getBatteryErrorUnknownResponse(mContext, sl));
                break;
        }

        return outcome;
    }

    /**
     * Method to resolve the battery health
     */
    private void getHealth() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getHealth");
        }

        if (batteryIntent != null) {

            final SaiyResources sr = new SaiyResources(mContext, sl);

            final int health = batteryIntent.getIntExtra(BatteryManager.EXTRA_HEALTH,
                    BatteryManager.BATTERY_HEALTH_UNKNOWN);

            switch (health) {

                case BatteryManager.BATTERY_HEALTH_COLD:
                    setHealthResponse(sr.getString(ai.saiy.android.R.string.cold));
                    break;
                case BatteryManager.BATTERY_HEALTH_DEAD:
                    setHealthResponse(sr.getString(ai.saiy.android.R.string.dead));
                    break;
                case BatteryManager.BATTERY_HEALTH_GOOD:
                    setHealthResponse(sr.getString(ai.saiy.android.R.string.good));
                    break;
                case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                    setHealthResponse(sr.getString(ai.saiy.android.R.string.over_heating));
                    break;
                case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                    setHealthResponse(sr.getString(ai.saiy.android.R.string.over_voltage));
                    break;
                case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                    setHealthResponse(sr.getString(ai.saiy.android.R.string.currently_indeterminable));
                    break;
                case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                    setHealthResponse(sr.getString(ai.saiy.android.R.string.currently_indeterminable));
                    break;
                default:
                    setHealthResponse(sr.getString(ai.saiy.android.R.string.currently_indeterminable));
                    break;
            }

            sr.reset();

        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "batteryIntent: null");
            }
            setAccessFailure();
        }
    }

    /**
     * Method to set the {@link Outcome} parameters
     *
     * @param health the resolved battery {@link CommandBatteryValues.Type#HEALTH}
     */
    private void setHealthResponse(@NonNull final String health) {
        outcome.setOutcome(Outcome.SUCCESS);
        outcome.setUtterance(PersonalityResponse.getBatteryResponse(mContext, sl, typeString, health));
    }

    /**
     * Method to resolve the battery status
     */
    private void getStatus() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getStatus");
        }

        if (batteryIntent != null) {

            final SaiyResources sr = new SaiyResources(mContext, sl);

            final int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS,
                    BatteryManager.BATTERY_STATUS_UNKNOWN);

            switch (status) {

                case BatteryManager.BATTERY_STATUS_CHARGING:

                    int plugged = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED,
                            BatteryManager.BATTERY_STATUS_UNKNOWN);

                    switch (plugged) {
                        case BatteryManager.BATTERY_PLUGGED_AC:
                            setStatusResponse(sr.getString(ai.saiy.android.R.string.ac_charging));
                            break;
                        case BatteryManager.BATTERY_PLUGGED_USB:
                            setStatusResponse(sr.getString(ai.saiy.android.R.string.usb_charging));
                            break;
                        default:
                            setStatusResponse(sr.getString(ai.saiy.android.R.string.charging));
                            break;
                    }
                    break;

                case BatteryManager.BATTERY_STATUS_DISCHARGING:
                    setStatusResponse(sr.getString(ai.saiy.android.R.string.discharging));
                    break;
                case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                    setStatusResponse(sr.getString(ai.saiy.android.R.string.discharging));
                    break;
                case BatteryManager.BATTERY_STATUS_FULL:
                    setStatusResponse(sr.getString(ai.saiy.android.R.string.fully_charged));
                    break;
                case BatteryManager.BATTERY_STATUS_UNKNOWN:
                    setStatusResponse(sr.getString(ai.saiy.android.R.string.currently_indeterminable));
                    break;
                default:
                    setStatusResponse(sr.getString(ai.saiy.android.R.string.currently_indeterminable));
                    break;
            }

            sr.reset();

        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "batteryIntent: null");
            }
            setAccessFailure();
        }
    }

    /**
     * Method to set the {@link Outcome} parameters
     *
     * @param status the resolved battery {@link CommandBatteryValues.Type#STATUS}
     */
    private void setStatusResponse(@NonNull final String status) {
        outcome.setOutcome(Outcome.SUCCESS);
        outcome.setUtterance(PersonalityResponse.getBatteryResponse(mContext, sl, typeString, status));
    }

    /**
     * Method to resolve the battery voltage
     */
    private void getVoltage() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getVoltage");
        }

        if (batteryIntent != null) {

            int voltage = batteryIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);

            if (voltage > 0) {
                voltage = voltage / 1000;

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getVoltage: " + voltage);
                }

                final SaiyResources sr = new SaiyResources(mContext, sl);
                setVoltageResponse(String.valueOf(voltage) + " " + sr.getString(ai.saiy.android.R.string.volts));
                sr.reset();
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getVoltage reporting incorrectly");
                }
                setAccessFailure();
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "batteryIntent: null");
            }
            setAccessFailure();
        }
    }

    /**
     * Method to set the {@link Outcome} parameters
     *
     * @param voltage the resolved battery {@link CommandBatteryValues.Type#VOLTAGE}
     */
    private void setVoltageResponse(@NonNull final String voltage) {
        outcome.setOutcome(Outcome.SUCCESS);
        outcome.setUtterance(PersonalityResponse.getBatteryResponse(mContext, sl, typeString, voltage));
    }

    /**
     * Method to resolve the battery percentage
     */
    private void getPercentage() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getPercentage");
        }

        if (batteryIntent != null) {

            final int percentage = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);

            if (percentage > -1) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getPercentage: " + percentage);
                }

                setPercentageResponse(String.valueOf(percentage) + "%");

            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getPercentage reporting incorrectly");
                }
                setAccessFailure();
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "batteryIntent: null");
            }
            setAccessFailure();
        }
    }

    /**
     * Method to set the {@link Outcome} parameters
     *
     * @param percentage the resolved battery {@link CommandBatteryValues.Type#PERCENTAGE}
     */
    private void setPercentageResponse(@NonNull final String percentage) {
        outcome.setOutcome(Outcome.SUCCESS);
        outcome.setUtterance(PersonalityResponse.getBatteryResponse(mContext, sl, typeString, percentage));
    }

    /**
     * Method to resolve the battery temperature
     */
    private void getTemperature() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getTemperature");
        }

        if (batteryIntent != null) {

            double celsius = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);

            if (celsius > 0) {
                celsius = celsius / 10;

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getTemperature: celsius: " + celsius);
                }

                final SaiyResources sr = new SaiyResources(mContext, sl);

                final String degrees = sr.getString(ai.saiy.android.R.string.degrees);

                String units = "";
                double temperature = 1;
                switch (SPH.getDefaultTemperatureUnits(mContext)) {

                    case CELSIUS:
                        units = sr.getString(ai.saiy.android.R.string.celsius);
                        temperature = celsius;
                        break;
                    case FAHRENHEIT:
                        final double conversion = (((celsius * 1.8) + 32));
                        final BigDecimal bd = new BigDecimal(conversion).setScale(0, RoundingMode.HALF_UP);
                        temperature = bd.doubleValue();
                        units = sr.getString(ai.saiy.android.R.string.fahrenheit);
                        break;
                }

                sr.reset();

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getTemperature: " + temperature);
                }

                setTemperatureResponse(String.valueOf(temperature) + " " + degrees + " " + units);

            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getTemperature reporting incorrectly");
                }
                setAccessFailure();
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "batteryIntent: null");
            }
            setAccessFailure();
        }
    }

    /**
     * Method to set the {@link Outcome} parameters
     *
     * @param temperature the resolved battery {@link CommandBatteryValues.Type#TEMPERATURE}
     */
    private void setTemperatureResponse(@NonNull final String temperature) {
        outcome.setOutcome(Outcome.SUCCESS);
        outcome.setUtterance(PersonalityResponse.getBatteryResponse(mContext, sl, typeString, temperature));
    }

    /**
     * Method set the {@link Outcome#FAILURE} parameters
     */
    private void setAccessFailure() {
        outcome.setOutcome(Outcome.FAILURE);
        outcome.setUtterance(PersonalityResponse.getBatteryErrorAccessResponse(mContext, sl));
    }
}
