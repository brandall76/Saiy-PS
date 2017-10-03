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

package ai.saiy.android.thirdparty.tasker;

import android.support.annotation.NonNull;

/**
 * Created by benrandall76@gmail.com on 10/08/2016.
 */

public class TaskerTask {

    private String projectName;
    private String taskName;

    public TaskerTask() {
    }

    public TaskerTask(@NonNull final String projectName, @NonNull final String taskName) {
        this.projectName = projectName;
        this.taskName = taskName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(@NonNull final String projectName) {
        this.projectName = projectName;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(@NonNull final String taskName) {
        this.taskName = taskName;
    }
}
