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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import ai.saiy.android.R;
import ai.saiy.android.api.request.SaiyRequestParams;
import ai.saiy.android.ui.activity.ActivityIssue;
import ai.saiy.android.utils.MyLog;

/**
 * Class to handle issues that the user needs to act upon. The issue is constructed using
 * {@link IssueContent} and forwarded to {@link ActivityIssue}
 * <p/>
 * Created by benrandall76@gmail.com on 16/04/2016.
 */
public class Issue {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Issue.class.getSimpleName();

    public static final String ISSUE_CONTENT = "issue_content";

    public static final int ISSUE_UNKNOWN = 0;
    public static final int ISSUE_NO_VR = 1;
    public static final int ISSUE_NO_TTS_ENGINE = 2;
    public static final int ISSUE_NO_TTS_LANGUAGE = 3;
    public static final int ISSUE_VLINGO = 4;

    private final Intent intent;
    private final Context mContext;

    /**
     * Constructor
     *
     * @param mContext      the application context
     * @param issueConstant that identifies the specific issue
     */
    public Issue(@NonNull final Context mContext, final int issueConstant) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "Constructor");
        }

        this.mContext = mContext;

        final IssueContent issueContent = new IssueContent(issueConstant);
        issueContent.setIssueText(getIssueText(issueConstant));

        intent = new Intent(this.mContext, ActivityIssue.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Bundle bundle = new Bundle();
        bundle.putSerializable(ISSUE_CONTENT, issueContent);

        intent.putExtras(bundle);

    }

    /**
     * Action the intent
     */
    public void execute() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "execute");
        }
        mContext.startActivity(intent);
    }

    /**
     * Get the text that Saiy will announce or display to the user in {@link ActivityIssue}
     *
     * @param issueConstant that identifies the specific issue
     * @return the String that Saiy will announce or display
     */
    private String getIssueText(final int issueConstant) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getIssueText");
        }

        switch (issueConstant) {

            case ISSUE_NO_VR:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getIssueText: ISSUE_NO_VR");
                }
                return mContext.getString(R.string.issue_vr_text);
            case ISSUE_NO_TTS_ENGINE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getIssueText: ISSUE_NO_TTS_ENGINE");
                }
                return mContext.getString(R.string.issue_tts_engine_text);
            case ISSUE_NO_TTS_LANGUAGE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getIssueText: ISSUE_NO_TTS_LANGUAGE");
                }
                return mContext.getString(R.string.issue_tts_language_text);
            case ISSUE_VLINGO:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getIssueText: ISSUE_VLINGO");
                }
                return mContext.getString(R.string.issue_vlingo_text);
            case ISSUE_UNKNOWN:
            default:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getIssueText: ISSUE_UNKNOWN");
                }
                return SaiyRequestParams.SILENCE;
        }
    }
}
