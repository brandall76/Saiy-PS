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

package ai.saiy.android.command.tasker;

import android.support.annotation.NonNull;

/**
 * Class to package the remote NLP request
 * <p>
 * Created by benrandall76@gmail.com on 10/08/2016.
 */

public class CommandTaskerValues {

    private String taskName;
    private int[][] ranges;
    private long startIndex;
    private long endIndex;

    public int[][] getRanges() {
        return ranges;
    }

    public void setRanges(@NonNull final int[][] ranges) {
        this.ranges = ranges;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(@NonNull final String taskName) {
        this.taskName = taskName;
    }

    public long getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(final long endIndex) {
        this.endIndex = endIndex;
    }

    public void setStartIndex(final long startIndex) {
        this.startIndex = startIndex;
    }

    public long getStartIndex() {
        return startIndex;
    }
}
