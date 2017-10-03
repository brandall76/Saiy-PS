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

package ai.saiy.android.utils;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by benrandall76@gmail.com on 20/04/2016.
 */
public class UtilsList {

    public static boolean notNaked(@Nullable final List<?> array) {
        return array != null && !array.isEmpty();
    }

    public static boolean notNaked(@Nullable final ArrayList<?> array) {
        return array != null && !array.isEmpty();
    }

    public static boolean notNaked(@Nullable final float[] array) {
        return array != null && array.length > 0;
    }

    public static boolean notNaked(@Nullable final byte[] array) {
        return array != null && array.length > 0;
    }
}
