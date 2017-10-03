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

import android.support.annotation.NonNull;

import ai.saiy.android.service.SelfAware;
import ai.saiy.android.tts.SaiyTextToSpeech;

/**
 * Interface to notify of haptic feedback, notification and audio adjustments. As with many other
 * classes, this is just to remove clutter from {@link SelfAware}
 * <p/>
 * Created by benrandall76@gmail.com on 14/04/2016.
 */
public interface IConditionListener {

    void onTTSStarted();

    void onTTSEnded(@NonNull final SelfAwareCache cache, @NonNull final SaiyTextToSpeech tts,
                    @NonNull final SelfAwareParameters params);

    void onTTSError();

    void onVRStarted();

    void onVREnded();

    void onVRComplete();

    void onVRError();

}
