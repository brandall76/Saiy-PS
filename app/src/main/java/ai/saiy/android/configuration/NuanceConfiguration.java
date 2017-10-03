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

package ai.saiy.android.configuration;

import android.content.res.Resources;
import android.net.Uri;

import com.nuance.speechkit.PcmFormat;

/**
 * Enter your Nuance configuration details here. They are accessed from the app details in the
 * Nuance developer portal.
 * <p>
 * Created by benrandall76@gmail.com on 07/02/2016.
 */
public final class NuanceConfiguration {

    /**
     * Prevent instantiation
     */
    public NuanceConfiguration() {
        throw new IllegalArgumentException(Resources.getSystem().getString(android.R.string.no));
    }

    //All fields are required.
    //Your credentials can be found in your Nuance Developers portal, under "Manage My Apps".
    public static final String APP_KEY = "_your_value_here_";
    public static final String APP_ID = "_your_value_here_";
    public static final String SERVER_HOST_NLU = "nmsps.dev.nuance.com";
    public static final String SERVER_HOST = "hiy.nmdp.nuancemobility.net";
    public static final String SERVER_PORT = "443";

    public static final Uri SERVER_URI = Uri.parse("nmsps://" + APP_ID + "@" + SERVER_HOST + ":" + SERVER_PORT);
    public static final Uri SERVER_URI_NLU = Uri.parse("nmsps://" + APP_ID + "@" + SERVER_HOST_NLU + ":" + SERVER_PORT);

    //Only needed if using NLU
    public static final String CONTEXT_TAG = "_your_value_here_";

    public static final PcmFormat PCM_FORMAT = new PcmFormat(PcmFormat.SampleFormat.SignedLinear16, 16000, 1);
}

