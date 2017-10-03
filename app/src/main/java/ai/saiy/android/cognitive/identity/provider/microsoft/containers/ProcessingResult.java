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

package ai.saiy.android.cognitive.identity.provider.microsoft.containers;

import com.google.gson.annotations.SerializedName;

/**
 * Created by benrandall76@gmail.com on 15/09/2016.
 */

public class ProcessingResult {

    @SerializedName("enrollmentStatus")
    private String enrollmentStatus;

    @SerializedName("speechTime")
    private double speechTime;

    @SerializedName("enrollmentSpeechTime")
    private double enrollmentSpeechTime;

    @SerializedName("remainingEnrollmentSpeechTime")
    private double remainingSpeechTime;

    @SerializedName("identifiedProfileId")
    private String profileId;

    @SerializedName("confidence")
    private String confidence;

    public ProcessingResult(final String confidence, final double enrollmentSpeechTime,
                            final String enrollmentStatus, final String profileId,
                            final double remainingSpeechTime, final double speechTime) {
        this.confidence = confidence;
        this.enrollmentSpeechTime = enrollmentSpeechTime;
        this.enrollmentStatus = enrollmentStatus;
        this.profileId = profileId;
        this.remainingSpeechTime = remainingSpeechTime;
        this.speechTime = speechTime;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(final String confidence) {
        this.confidence = confidence;
    }

    public double getEnrollmentSpeechTime() {
        return enrollmentSpeechTime;
    }

    public void setEnrollmentSpeechTime(final double enrollmentSpeechTime) {
        this.enrollmentSpeechTime = enrollmentSpeechTime;
    }

    public String getEnrollmentStatus() {
        return enrollmentStatus;
    }

    public void setEnrollmentStatus(final String enrollmentStatus) {
        this.enrollmentStatus = enrollmentStatus;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(final String profileId) {
        this.profileId = profileId;
    }

    public double getRemainingSpeechTime() {
        return remainingSpeechTime;
    }

    public void setRemainingSpeechTime(final double remainingSpeechTime) {
        this.remainingSpeechTime = remainingSpeechTime;
    }

    public double getSpeechTime() {
        return speechTime;
    }

    public void setSpeechTime(final double speechTime) {
        this.speechTime = speechTime;
    }
}
