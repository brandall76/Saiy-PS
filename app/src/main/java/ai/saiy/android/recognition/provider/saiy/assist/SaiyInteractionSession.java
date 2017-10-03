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

package ai.saiy.android.recognition.provider.saiy.assist;

import android.app.assist.AssistContent;
import android.app.assist.AssistStructure;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import android.support.annotation.RequiresApi;

/**
 * Created by benrandall76@gmail.com on 22/08/2016.
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class SaiyInteractionSession extends VoiceInteractionSession {

    SaiyInteractionSession(Context context) {
        super(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onHandleAssist(Bundle data, AssistStructure structure, AssistContent content) {
    }

    @Override
    public void onHandleScreenshot(Bitmap screenshot) {
    }


    @Override
    public void onLockscreenShown() {

    }

    @Override
    public boolean[] onGetSupportedCommands(String[] commands) {
        boolean[] res = new boolean[commands.length];
        return res;
    }


    @Override
    public void onRequestConfirmation(ConfirmationRequest request) {
    }

    @Override
    public void onRequestPickOption(PickOptionRequest request) {
    }


    @Override
    public void onRequestCompleteVoice(CompleteVoiceRequest request) {
    }

    @Override
    public void onRequestAbortVoice(AbortVoiceRequest request) {
    }

    @Override
    public void onRequestCommand(CommandRequest request) {
    }

    @Override
    public void onCancelRequest(Request request) {
        request.cancel();
    }
}