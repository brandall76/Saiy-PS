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

package ai.saiy.android.error;

import android.support.annotation.NonNull;

import java.io.Serializable;

import ai.saiy.android.ui.activity.ActivityIssue;

/**
 * Holder to provide information to display in {@link ActivityIssue}
 * <p/>
 * Created by benrandall76@gmail.com on 16/04/2016.
 */
public class IssueContent implements Serializable {

    private static final long serialVersionUID = -593683149969450529L;

    private final int issueConstant;
    private String issueText;

    public IssueContent(final int issueConstant) {
        this.issueConstant = issueConstant;
    }

    public void setIssueText(@NonNull final String issueText) {
        this.issueText = issueText;
    }

    public int getIssueConstant() {
        return issueConstant;
    }

    public String getIssueText() {
        return issueText;
    }
}
