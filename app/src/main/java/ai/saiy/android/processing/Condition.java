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

package ai.saiy.android.processing;

/**
 * Class of constants to identify how Saiy should process the voice data. This isn't always to
 * resolve a command, as the application could be in conversation mode or expecting the user to
 * acknowledge interaction from a previous command.
 * <p/>
 * The danger with constants such as these, would be the failure to reset/remove the Condition under
 * weird and wonderful circumstances. This could potentially leave the application stuck sending the voice
 * data to the wrong part of the application. Great care is needed to make sure this doesn't happen.
 * <p/>
 * Created by benrandall76@gmail.com on 01/04/2016.
 */
public final class Condition {

    public static final int CONDITION_NONE = 0;
    public static final int CONDITION_CONVERSATION = 1;
    public static final int CONDITION_ROOT = 2;
    public static final int CONDITION_TRANSLATION = 3;
    public static final int CONDITION_USER_CUSTOM = 4;
    public static final int CONDITION_EMOTION = 5;
    public static final int CONDITION_IDENTITY = 6;
    public static final int CONDITION_IDENTIFY = 7;
    public static final int CONDITION_GOOGLE_NOW = 8;
    public static final int CONDITION_SECURE = 9;
    public static final int CONDITION_IGNORE = 99;

}
