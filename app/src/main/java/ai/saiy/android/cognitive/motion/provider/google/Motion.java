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

package ai.saiy.android.cognitive.motion.provider.google;

/**
 * Created by benrandall76@gmail.com on 05/07/2016.
 */

public class Motion {

    protected transient static final int CONFIDENCE_THRESHOLD = 75;

    private final int type;
    private final int confidence;
    private final long time;

    public Motion(final int type, final int confidence, final long time) {
        this.confidence = confidence;
        this.type = type;
        this.time = time;
    }

    public int getConfidence() {
        return confidence;
    }

    public int getType() {
        return type;
    }

    public long getTime() {
        return time;
    }
}
