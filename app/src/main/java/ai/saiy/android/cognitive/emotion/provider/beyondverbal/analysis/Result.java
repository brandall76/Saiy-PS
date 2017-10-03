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

import java.util.List;

/**
 * Helper class to serialise the JSON response from Beyond Verbal
 * <p>
 * Created by benrandall76@gmail.com on 10/06/2016.
 */
public class Result {

    @SerializedName("analysisSegments")
    private final List<Segment> segments;

    @SerializedName("analysisSummary")
    private final AnalysisSummary analysisSummary;

    @SerializedName("duration")
    private final double duration;

    @SerializedName("sessionStatus")
    private final String sessionStatus;

    public Result(final double duration, final List<Segment> segments, final String sessionStatus,
                  final AnalysisSummary analysisSummary) {
        this.duration = duration;
        this.segments = segments;
        this.sessionStatus = sessionStatus;
        this.analysisSummary = analysisSummary;
    }

    public AnalysisSummary getAnalysisSummary() {
        return analysisSummary;
    }

    public double getDuration() {
        return duration;
    }

    public List<Segment> getSegments() {
        return segments;
    }

    public String getSessionStatus() {
        return sessionStatus;
    }
}
