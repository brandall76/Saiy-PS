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

import android.content.Context;
import android.support.annotation.NonNull;

import com.android.volley.Cache;
import com.android.volley.toolbox.DiskBasedCache;

/**
 * Created by benrandall76@gmail.com on 08/09/2016.
 */

public class UtilsVolley {

    private static Cache cache;

    public static Cache getCache(@NonNull final Context ctx) {

        if (cache == null) {
            cache = new DiskBasedCache(ctx.getCacheDir(), 1024 * 1024);
        }

        return cache;
    }
}
