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

package ai.saiy.android.ui.service;

import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.TileService;
import android.support.annotation.RequiresApi;

import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;

/**
 * Class to handle the Tile that can be added in API 24+
 * <p>
 * Created by benrandall76@gmail.com on 13/07/2016.
 */

@RequiresApi(api = Build.VERSION_CODES.N)
public class SaiyTileService extends TileService {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = SaiyTileService.class.getSimpleName();

    public SaiyTileService() {
    }

    @Override
    public void onClick() {
        super.onClick();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onClick");
        }

        final LocalRequest lr = new LocalRequest(getApplicationContext());
        lr.prepareIntro();
        lr.execute();

        final Intent closeShadeIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        sendBroadcast(closeShadeIntent);

//        final Intent preferenceIntent = new Intent(getApplicationContext(), ActivityTilePreferences.class);
//        startActivityAndCollapse(preferenceIntent);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onStartListening");
        }
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onStopListening");
        }
    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onTileAdded");
        }
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onTileRemoved");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDestroy");
        }
    }
}
