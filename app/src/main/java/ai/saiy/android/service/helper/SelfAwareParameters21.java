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

package ai.saiy.android.service.helper;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.HashMap;

/**
 * Class to simulate the behaviour of the depreciated {@link SelfAwareParameters} used in the
 * {@link ai.saiy.android.tts.SaiyTextToSpeech#speak(String, int, HashMap)}
 * <p>
 * This class provides the methods for the
 * API > 21 {@link ai.saiy.android.tts.SaiyTextToSpeech#speak(CharSequence, int, Bundle, String)}
 * <p>
 * Created by benrandall76@gmail.com on 20/06/2016.
 */

class SelfAwareParameters21 {

    protected final Bundle params;

    SelfAwareParameters21() {
        this.params = new Bundle();
    }

    protected Bundle getBundle() {
        return params;
    }

    protected String get(@NonNull final Object key) {

        final Object object = params.get((String) key);

        if (object != null) {
            return String.valueOf(object);
        } else {
            return null;
        }
    }

    protected String putBoolean(@NonNull final String key, final boolean value) {
        params.putBoolean(key, value);
        return null;
    }

    protected String putDouble(@NonNull final String key, final double value) {
        params.putDouble(key, value);
        return null;
    }

    protected String putFloat(@NonNull final String key, final float value) {
        params.putFloat(key, value);
        return null;
    }

    protected String putInt(@NonNull final String key, final int value) {
        params.putInt(key, value);
        return null;
    }

    protected String putString(@NonNull final String key, @NonNull final String value) {
        params.putString(key, value);
        return null;
    }

    protected String remove(@NonNull final Object key) {
        params.remove((String) key);
        return null;
    }

    void clear() {
        params.clear();
    }
}
