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

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * Created by benrandall76@gmail.com on 07/09/2016.
 */

public class ProfileItem {

    @SerializedName("identificationProfileId")
    private final String id;

    @SerializedName("locale")
    private String locale;

    @SerializedName("createdDateTime")
    private String created;

    @SerializedName("lastActionDateTime")
    private String lastAction;

    @SerializedName("enrollmentStatus")
    private String status;

    @SerializedName("enrollmentSpeechTime")
    private double speechTime;

    @SerializedName("remainingEnrollmentSpeechTime")
    private double remainingSpeechTime;

    public ProfileItem(final String created, final String id, final String locale, final String lastAction,
                       final String status, final double speechTime, final double remainingSpeechTime) {
        this.created = created;
        this.id = id;
        this.locale = locale;
        this.lastAction = lastAction;
        this.status = status;
        this.speechTime = speechTime;
        this.remainingSpeechTime = remainingSpeechTime;
    }

    public ProfileItem(@NonNull final String id) {
        this.id = id;
    }

    public String getCreated() {
        return created;
    }

    public String getId() {
        return id;
    }

    public String getLastAction() {
        return lastAction;
    }

    public String getLocale() {
        return locale;
    }

    public double getRemainingSpeechTime() {
        return remainingSpeechTime;
    }

    public double getSpeechTime() {
        return speechTime;
    }

    public String getStatus() {
        return status;
    }

    public void setCreated(final String created) {
        this.created = created;
    }

    public void setLastAction(final String lastAction) {
        this.lastAction = lastAction;
    }

    public void setLocale(final String locale) {
        this.locale = locale;
    }

    public void setRemainingSpeechTime(final double remainingSpeechTime) {
        this.remainingSpeechTime = remainingSpeechTime;
    }

    public void setSpeechTime(final double speechTime) {
        this.speechTime = speechTime;
    }

    public void setStatus(final String status) {
        this.status = status;
    }
}
