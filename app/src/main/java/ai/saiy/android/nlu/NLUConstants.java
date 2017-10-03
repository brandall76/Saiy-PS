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

package ai.saiy.android.nlu;

import android.support.annotation.NonNull;

import java.util.ArrayList;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.utils.MyLog;

/**
 * Constants used for Nuance NLU
 * <p/>
 * Created by benrandall76@gmail.com on 15/02/2016.
 */
public class NLUConstants {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = NLUConstants.class.getSimpleName();

    // Translate command
    public static final String TRANSLATE = "translate";
    public static final String TEXT_TO_TRANSLATE = "text_to_translate";
    public static final String TRANSLATE_LANGUAGE = "translate_language";
    public static final String LANGUAGE = "language";

    // User Name command
    public static final String USER_NAME = "user_name";
    public static final String NAME_USER = "name_user";

    // Unknown command
    public static final String UNKNOWN = "unknown";
    public static final String NO_MATCH = "NO_MATCH";
    public static final String NONE = "None";

    // Battery command
    public static final String BATTERY = "battery";
    public static final String BATTERY_TYPE = "battery_type";

    // Spell command
    public static final String SPELL = "spell";
    public static final String TEXT_TO_SPELL = "text_to_spell";

    // Music recognition command
    public static final String SONG_RECOGNITION = "music_recognition";

    // Pardon command
    public static final String PARDON = "pardon";

    // Vocal identification command
    public static final String VOCAL_IDENTITY = "vocal_identity";

    // Tasker command
    public static final String TASKER_TASK = "tasker_task";
    public static final String TASKER_TASK_NAME = "tasker_task_name";

    // Wolfram Alpha command
    public static final String WOLFRAM_ALPHA = "wolfram_alpha";
    public static final String QUESTION_CONTENT = "question_content";

    // Emotion command
    public static final String EMOTION = "emotion";

    // Emotion command
    public static final String HOTWORD = "hotword";

    public static final String LITERAL = "literal";

    /**
     * Compare the literal intent String name and match it to one of the {@link CC} constants
     *
     * @param name of the intent
     * @return the corresponding {@link CC} value
     */
    public static CC intentToCC(@NonNull final String name) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "intentToCC");
        }
        if (name.matches(TRANSLATE)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "intentToCC: COMMAND_TRANSLATE");
            }
            return CC.COMMAND_TRANSLATE;
        } else if (name.matches(USER_NAME)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "intentToCC: COMMAND_USER_NAME");
            }
            return CC.COMMAND_USER_NAME;
        } else if (name.matches(PARDON)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "intentToCC: COMMAND_PARDON");
            }
            return CC.COMMAND_PARDON;
        } else if (name.matches(BATTERY)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "intentToCC: COMMAND_BATTERY");
            }
            return CC.COMMAND_BATTERY;
        } else if (name.matches(SPELL)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "intentToCC: COMMAND_SPELL");
            }
            return CC.COMMAND_SPELL;
        } else if (name.matches(SONG_RECOGNITION)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "intentToCC: COMMAND_SONG_RECOGNITION");
            }
            return CC.COMMAND_SONG_RECOGNITION;
        } else if (name.matches(VOCAL_IDENTITY)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "intentToCC: COMMAND_VOICE_IDENTIFY");
            }
            return CC.COMMAND_VOICE_IDENTIFY;
        } else if (name.matches(TASKER_TASK)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "intentToCC: COMMAND_TASKER");
            }
            return CC.COMMAND_TASKER;
        } else if (name.matches(WOLFRAM_ALPHA)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "intentToCC: COMMAND_WOLFRAM_ALPHA");
            }
            return CC.COMMAND_WOLFRAM_ALPHA;
        } else if (name.matches(EMOTION)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "intentToCC: COMMAND_EMOTION");
            }
            return CC.COMMAND_EMOTION;
        } else if (name.matches(HOTWORD)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "intentToCC: COMMAND_HOTWORD");
            }
            return CC.COMMAND_HOTWORD;
        } else if (name.matches(UNKNOWN) || name.matches(NO_MATCH) || name.matches(NONE)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "intentToCC: UNKNOWN/NO_MATCH");
            }
            return CC.COMMAND_UNKNOWN;
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "intentToCC: none");
            }
            return CC.COMMAND_UNKNOWN;
        }
    }

    /**
     * Compare the literal intent String names and match them to one of the {@link CC} constants
     *
     * @param nameArray of intent names
     * @return an {@code ArrayList<String>} of the {@link CC} values
     */
    public static ArrayList<CC> intentToCC(@NonNull final ArrayList<String> nameArray) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "intentToCC");
        }

        final ArrayList<CC> ccArray = new ArrayList<>();

        for (final String name : nameArray) {

            if (name.matches(TRANSLATE)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "intentToCC: COMMAND_TRANSLATE");
                }
                ccArray.add(CC.COMMAND_TRANSLATE);
            } else if (name.matches(USER_NAME)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "intentToCC: COMMAND_USER_NAME");
                }
                ccArray.add(CC.COMMAND_USER_NAME);
            } else if (name.matches(SPELL)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "intentToCC: COMMAND_SPELL");
                }
                ccArray.add(CC.COMMAND_SPELL);
            } else if (name.matches(BATTERY)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "intentToCC: COMMAND_BATTERY");
                }
                ccArray.add(CC.COMMAND_BATTERY);
            } else if (name.matches(PARDON)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "intentToCC: COMMAND_PARDON");
                }
                ccArray.add(CC.COMMAND_PARDON);
            } else if (name.matches(SONG_RECOGNITION)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "intentToCC: COMMAND_SONG_RECOGNITION");
                }
                ccArray.add(CC.COMMAND_SONG_RECOGNITION);
            } else if (name.matches(UNKNOWN) || name.matches(NO_MATCH)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "intentToCC: UNKNOWN/NO_MATCH");
                }
                ccArray.add(CC.COMMAND_UNKNOWN);
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "intentToCC: none");
                }
                ccArray.add(CC.COMMAND_UNKNOWN);
            }
        }

        return ccArray;
    }

}
