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

import android.support.annotation.NonNull;

import ai.saiy.android.command.helper.CC;

/**
 * Created by benrandall76@gmail.com on 12/06/2016.
 */
public class EntangledPair {

    private final Position position;
    private final CC cc;
    private String toastContent;
    private String utterance;

    public EntangledPair(@NonNull final Position position, @NonNull final CC cc) {
        this.position = position;
        this.cc = cc;
    }

    public Position getPosition() {
        return position;
    }

    public CC getCC() {
        return cc;
    }

    public String getUtterance() {
        return utterance;
    }

    public void setUtterance(@NonNull final String utterance) {
        this.utterance = utterance;
    }

    public String getToastContent() {
        return toastContent;
    }

    public void setToastContent(@NonNull final String toastContent) {
        this.toastContent = toastContent;
    }
}
