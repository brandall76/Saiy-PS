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

import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.recognition.provider.saiy.assist.SaiyInteractionService;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

/**
 * Created by benrandall76@gmail.com on 22/08/2016.
 */

public class ActivityAssistProxy extends Activity {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ActivityAssistProxy.class.getSimpleName();

    long then;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        then = System.nanoTime();

        final Intent intent = new Intent(this, SaiyInteractionService.class);
        intent.setAction(Intent.ACTION_ASSIST);

        final Bundle extras = new Bundle();
        extras.putString(SaiyInteractionService.EXTRA_VOICE_KEYPHRASE_HINT_TEXT, SPH.getHotword(getApplicationContext()));
        extras.putString(SaiyInteractionService.EXTRA_VOICE_KEYPHRASE_LOCALE,
                SupportedLanguage.ENGLISH.getLanguageCountry());
        intent.putExtras(extras);
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

