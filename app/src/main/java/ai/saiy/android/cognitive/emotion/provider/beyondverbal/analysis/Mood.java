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

package ai.saiy.android.cognitive.emotion.provider.beyondverbal.analysis;

import com.google.gson.annotations.SerializedName;

/**
 * Helper class to serialise the JSON response from Beyond Verbal
 * <p>
 * Created by benrandall76@gmail.com on 10/06/2016.
 */
public class Mood {

    @SerializedName("Group11")
    private final Group11 group11;

    @SerializedName("Group7")
    private final Group7 group7;

    @SerializedName("Group21")
    private final Group21 group21;

    @SerializedName("Composite")
    private final Composite composite;

    public Mood(final Composite composite, final Group11 group11, final Group7 group7, final Group21 group21) {
        this.composite = composite;
        this.group11 = group11;
        this.group7 = group7;
        this.group21 = group21;
    }

    public Composite getComposite() {
        return composite;
    }

    public Group11 getGroup11() {
        return group11;
    }

    public Group21 getGroup21() {
        return group21;
    }

    public Group7 getGroup7() {
        return group7;
    }
}
