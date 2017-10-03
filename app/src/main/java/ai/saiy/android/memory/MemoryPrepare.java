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

package ai.saiy.android.memory;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

import ai.saiy.android.api.request.SaiyRequestParams;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

/**
 * Helper class to prepare the {@link Memory} data
 * <p/>
 * Created by benrandall76@gmail.com on 20/04/2016.
 */
public class MemoryPrepare {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = MemoryPrepare.class.getSimpleName();

    private final String vrLanguage;
    private final String ttsLanguage;
    private final String utterance;
    private final ArrayList<String> utteranceArray;
    private final int action;
    private final CC command;
    private final int condition;
    private final SupportedLanguage sl;
    private final Context mContext;

    /**
     * Constructor
     *
     * @param mContext the application context
     * @param bundle   the bundle of data containing all necessary actions and parameters
     */
    public MemoryPrepare(@NonNull final Context mContext, @NonNull final Bundle bundle) {
        this.mContext = mContext;

        vrLanguage = bundle.getString(LocalRequest.EXTRA_RECOGNITION_LANGUAGE, SPH.getVRLocale(mContext).toString());
        ttsLanguage = bundle.getString(LocalRequest.EXTRA_TTS_LANGUAGE, SPH.getTTSLocale(mContext).toString());
        utterance = bundle.getString(LocalRequest.EXTRA_UTTERANCE, SaiyRequestParams.SILENCE);

        if (bundle.containsKey(LocalRequest.EXTRA_UTTERANCE_ARRAY)) {
            utteranceArray = bundle.getStringArrayList(LocalRequest.EXTRA_UTTERANCE_ARRAY);
        } else {
            utteranceArray = new ArrayList<>();
        }

        action = bundle.getInt(LocalRequest.EXTRA_ACTION, LocalRequest.ACTION_UNKNOWN);
        command = (CC) bundle.getSerializable(LocalRequest.EXTRA_COMMAND);
        condition = bundle.getInt(LocalRequest.EXTRA_CONDITION, Condition.CONDITION_NONE);
        sl = (SupportedLanguage) bundle.getSerializable(LocalRequest.EXTRA_SUPPORTED_LANGUAGE);
    }

    /**
     * Save the memory into the the user's shared preferences, once it has been serialised.
     */
    public void save() {

        final Memory memory = new Memory(action, vrLanguage, ttsLanguage, utterance, utteranceArray,
                command, condition, sl);

        final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        final String gsonString = gson.toJson(memory);

        if (DEBUG) {
            MyLog.i(CLS_NAME, "save: gsonString: " + gsonString);
        }

        SPH.setMemory(mContext, gsonString);
    }
}
