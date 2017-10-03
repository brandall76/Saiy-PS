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

import java.util.ArrayList;

import ai.saiy.android.defaults.songrecognition.SongRecognitionChooser;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsBundle;

/**
 * Created by benrandall76@gmail.com on 12/06/2016.
 */
public class ActivityChooserDialog extends Activity {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ActivityChooserDialog.class.getSimpleName();

    long then;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }

        this.setFinishOnTouchOutside(false);

        then = System.nanoTime();

        final Bundle bundle = getIntent().getExtras();

        if (UtilsBundle.notNaked(bundle) && !UtilsBundle.isSuspicious(bundle)) {

            final ArrayList<SongRecognitionChooser> chooserArray = bundle.getParcelableArrayList(
                    SongRecognitionChooser.PARCEL_KEY);

            if (UtilsList.notNaked(chooserArray)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "chooserArray: " + chooserArray.size());
                }

                for (final SongRecognitionChooser src : chooserArray) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "PackageName: " + src.getPackageName());
                        MyLog.i(CLS_NAME, "AppName: " + src.getApplicationName());
                        MyLog.i(CLS_NAME, "Installed: " + src.isInstalled());
                    }
                }
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "chooserArray: naked");
                }
            }
        } else {
            // TODO - no good
        }

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
