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

package ai.saiy.android.nlu.local;

import android.support.annotation.NonNull;
import android.util.Pair;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Map;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.utils.MyLog;

/**
 * Created by benrandall76@gmail.com on 09/02/2016.
 * <p/>
 * This class analyses the frequency of detected commands and weights them based on quantity
 * and corresponding confidence scores.
 * <p/>
 * If the command detected to be the most frequent is not above the threshold of the sum of the
 * first n commands (in this deliberate case, n=2) then further analysis is done.
 * <p/>
 * NOTE - based on studying many recognition providers results, which use association models to
 * return results, the global speech data becomes diluted in favour of related possibilities. If
 * you say quite clearly to Google Voice Search 'turn on Bluetooth', it will return 'turn off
 * Bluetooth' and 'turn on WiFi' in the results, albeit with a low(er) confidence score.
 * <p/>
 * Additionally, it is possible that amalgamating the speech data could result in the positive
 * detection of a command. Consider the response of 'What's the weather like in blue stork' as well as
 * 'What's the feather spike in new york', combined would identify a weather request and the relevant
 * location detail. This may seem like an usual case, but if you wonder why I have so much logging
 * in the app, then now you know.............
 * <p/>
 * Not using a deliberate language model will negate the above, but I mention now to allow you
 * to learn from my greyhound (=grey hair (Nuance recognition joke (to prove a point))).
 */
public final class FrequencyAnalysis {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FrequencyAnalysis.class.getSimpleName();

    private static final int THRESHOLD = 65;

    private final ArrayList<Pair<CC, Float>> commandArray;

    /**
     * Constructor
     *
     * @param commandArray containing the command frequency
     */
    public FrequencyAnalysis(@NonNull final ArrayList<Pair<CC, Float>> commandArray) {
        this.commandArray = commandArray;
    }

    /**
     * Sort the ArrayList<Integer> to decide on the most probable command.
     *
     * @return the constant command integer {@link CC}
     */
    public CC analyse() {
        if (DEBUG) {
            MyLog.v(CLS_NAME, "analyse");
        }

        long then = System.nanoTime();
        final CC commandInt;

        switch (commandArray.size()) {

            case 0:
                if (DEBUG) {
                    MyLog.v(CLS_NAME, "empty commandArray");
                }
                commandInt = CC.COMMAND_UNKNOWN;
                break;
            case 1:
                if (DEBUG) {
                    MyLog.v(CLS_NAME, "Only one - unanimous");
                }
                commandInt = commandArray.get(0).first;
                break;
            default:

                final ArrayList<CC> ccArray = new ArrayList<>();
                for (final Pair<CC, Float> pair : commandArray) {
                    ccArray.add(pair.first);
                }

                if (ccArray.size() > 1) {

                    final Map<CC, Integer> cardinalityMap = CollectionUtils.getCardinalityMap(ccArray);

                    CC firstCommand = CC.COMMAND_UNKNOWN;
                    int firstQuantity = 0;
                    CC secondCommand = CC.COMMAND_UNKNOWN;
                    int secondQuantity = 0;

                    int count = 0;

                    for (final Map.Entry<CC, Integer> e : cardinalityMap.entrySet()) {
                        if (DEBUG) {
                            MyLog.v(CLS_NAME, "cardinalityMap: Quantity: "
                                    + e.getValue() + " Command: " + e.getKey());
                        }

                        if (count == 0) {
                            firstCommand = e.getKey();
                            firstQuantity = e.getValue();
                        } else if (count == 1) {
                            secondCommand = e.getKey();
                            secondQuantity = e.getValue();
                            break;
                        }

                        count++;
                    }

                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "firstCommand: " + firstCommand.name());
                        MyLog.d(CLS_NAME, "firstQuantity: " + firstQuantity);
                        MyLog.d(CLS_NAME, "secondCommand: " + secondCommand.name());
                        MyLog.d(CLS_NAME, "secondQuantity: " + secondQuantity);
                    }

                    final double total = firstQuantity + secondQuantity;

                    final double percentage = ((firstQuantity / total) * 100);

                    if (percentage > THRESHOLD) {
                        if (DEBUG) {
                            MyLog.v(CLS_NAME, "Percentage: " + percentage + "%");
                        }

                        commandInt = firstCommand;

                    } else {
                        if (DEBUG) {
                            MyLog.v(CLS_NAME, "Percentage: " + percentage + "%");
                        }

                        final float firstConfidence = commandArray.get(ccArray.indexOf(firstCommand)).second;
                        final float secondConfidence = commandArray.get(ccArray.indexOf(secondCommand)).second;

                        if (DEBUG) {
                            MyLog.v(CLS_NAME, "firstConfidence: " + String.valueOf(firstConfidence));
                            MyLog.v(CLS_NAME, "secondConfidence: " + String.valueOf(secondConfidence));
                        }

                        if (firstConfidence > secondConfidence) {
                            if (DEBUG) {
                                MyLog.v(CLS_NAME, "firstConfidence: higher confidence");
                            }
                            commandInt = firstCommand;
                        } else if (secondConfidence > firstConfidence) {
                            if (DEBUG) {
                                MyLog.v(CLS_NAME, "secondConfidence: higher confidence");
                            }
                            commandInt = secondCommand;
                        } else {
                            if (DEBUG) {
                                MyLog.v(CLS_NAME, "equal confidence");
                            }
                            if (firstQuantity > secondQuantity) {
                                if (DEBUG) {
                                    MyLog.v(CLS_NAME, "first: had more entries");
                                }
                                commandInt = firstCommand;
                            } else if (secondQuantity > firstQuantity) {
                                if (DEBUG) {
                                    MyLog.v(CLS_NAME, "second: had more entries");
                                }
                                commandInt = secondCommand;
                            } else {
                                if (DEBUG) {
                                    MyLog.v(CLS_NAME, "dead-heat: Selecting priority commands or first-come-first-serve");
                                }

                                if (secondCommand == CC.COMMAND_USER_NAME) {
                                    commandInt = secondCommand;
                                } else {
                                    commandInt = firstCommand;
                                }
                            }
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.v(CLS_NAME, "unanimous");
                    }
                    commandInt = commandArray.get(0).first;
                }

                break;
        }

        if (DEBUG) {
            MyLog.i(CLS_NAME, "returning command ~ " + commandInt);
            MyLog.getElapsed(CLS_NAME, then);
        }

        return commandInt;
    }
}
