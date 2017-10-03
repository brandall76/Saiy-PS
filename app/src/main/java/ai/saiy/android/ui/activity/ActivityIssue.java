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

package ai.saiy.android.ui.activity;

import android.app.Activity;
import android.os.Bundle;

import ai.saiy.android.error.Issue;
import ai.saiy.android.error.IssueContent;
import ai.saiy.android.utils.MyLog;

/**
 * Created by benrandall76@gmail.com on 16/04/2016.
 */
public class ActivityIssue extends Activity {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ActivityIssue.class.getSimpleName();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }

        final Bundle bundle = getIntent().getExtras();

        if (bundle != null && !bundle.isEmpty()) {
            if (bundle.containsKey(Issue.ISSUE_CONTENT)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "bundle contains: IssueContent.ISSUE_CONTENT");
                }

                final IssueContent issueContent = (IssueContent) bundle.getSerializable(Issue.ISSUE_CONTENT);

                if (issueContent != null) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "issueContent: " + issueContent.getIssueText());
                    }

                    switch (issueContent.getIssueConstant()) {

                        case Issue.ISSUE_NO_VR:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "ISSUE_NO_VR");
                            }
                            break;
                        case Issue.ISSUE_NO_TTS_ENGINE:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "ISSUE_NO_TTS_ENGINE");
                            }
                            break;
                        case Issue.ISSUE_NO_TTS_LANGUAGE:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "ISSUE_NO_TTS_LANGUAGE");
                            }
                            break;
                        case Issue.ISSUE_VLINGO:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "ISSUE_VLINGO");
                            }
                            break;
                        default:
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "Issue default");
                            }
                            break;
                    }
                }
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "bundle missing: IssueContent null");
                }
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "bundle missing: IssueContent.ISSUE_CONTENT");
            }
        }

        finish();
    }


    @Override
    protected void onDestroy() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDestroy");
        }
        super.onDestroy();
    }
}
