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

package ai.saiy.android.cognitive.emotion.provider.beyondverbal.containers;

import com.google.gson.annotations.SerializedName;

/**
 * Class to serialise the response from Beyond Verbal
 * <p>
 * Created by benrandall76@gmail.com on 09/06/2016.
 */
public class StartResponse {

    private static final String SUCCESS = "success";
    private static final String FAILURE = "failure";

    @SerializedName("status")
    private final String status;

    @SerializedName("recordingId")
    private final String recordingId;

    @SerializedName("reason")
    private final String reason;

    public StartResponse(final String reason, final String status, final String recordingId) {
        this.reason = reason;
        this.status = status;
        this.recordingId = recordingId;
    }

    public boolean isSuccessful() {
        return getStatus().matches(SUCCESS);
    }

    public String getReason() {
        return reason;
    }

    public String getRecordingId() {
        return recordingId;
    }

    public String getStatus() {
        return status;
    }
}
