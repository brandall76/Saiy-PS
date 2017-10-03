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
import android.content.Intent;
import android.os.Bundle;

import ai.saiy.android.service.helper.AssistantIntentService;
import ai.saiy.android.utils.MyLog;

/**
 * Created by benrandall76@gmail.com on 06/02/2016.
 */
public class ActivityLauncherShortcut extends Activity {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ActivityLauncherShortcut.class.getSimpleName();

    long then;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }

        then = System.nanoTime();

        final Intent intent = new Intent(getApplicationContext(), AssistantIntentService.class);
        intent.setAction(getIntent().getAction());
        intent.putExtras(getIntent());
        getApplicationContext().startService(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDestroy");
            MyLog.getElapsed(CLS_NAME, then);
        }
    }
}
