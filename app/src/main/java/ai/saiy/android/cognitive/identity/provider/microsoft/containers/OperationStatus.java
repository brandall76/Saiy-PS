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

public class OperationStatus {

    @SerializedName("status")
    private String status;

    @SerializedName("createdDateTime")
    private String created;

    @SerializedName("lastActionDateTime")
    private String lastAction;

    @SerializedName("message")
    private String message;

    @SerializedName("processingResult")
    private ProcessingResult processingResult;

    public OperationStatus(final String created, final String lastAction, final String message,
                           final ProcessingResult processingResult, final String status) {
        this.created = created;
        this.lastAction = lastAction;
        this.message = message;
        this.processingResult = processingResult;
        this.status = status;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(final String created) {
        this.created = created;
    }

    public String getLastAction() {
        return lastAction;
    }

    public void setLastAction(final String lastAction) {
        this.lastAction = lastAction;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public ProcessingResult getProcessingResult() {
        return processingResult;
    }

    public void setProcessingResult(final ProcessingResult processingResult) {
        this.processingResult = processingResult;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }
}
