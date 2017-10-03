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

package ai.saiy.android.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ai.saiy.android.utils.MyLog;

/**
 * Class to map the {@link Defaults} exposed in the library to default values used by Saiy, which
 * may differ and offer further functionality.
 * <p>
 * Created by benrandall76@gmail.com on 12/08/2016.
 */

public class SaiyDefaults {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = SaiyDefaults.class.getSimpleName();

    public enum LanguageModel {

        LOCAL(Defaults.LanguageModel.LOCAL),
        NUANCE(Defaults.LanguageModel.NUANCE),
        MICROSOFT(Defaults.LanguageModel.MICROSOFT),
        API_AI(Defaults.LanguageModel.API_AI),
        WIT(Defaults.LanguageModel.WIT),
        IBM(null),
        REMOTE(Defaults.LanguageModel.REMOTE);

        private final Defaults.LanguageModel languageModel;

        LanguageModel(final Defaults.LanguageModel languageModel) {
            this.languageModel = languageModel;
        }

        public Defaults.LanguageModel getRemoteLanguageModel() {
            return this.languageModel;
        }

        /**
         * Convert a remote LanguageModel object to the local variant
         *
         * @param languageModel the remote {@link ai.saiy.android.api.Defaults.LanguageModel}
         * @return the local variant of {@link SaiyDefaults.LanguageModel}
         */
        public static LanguageModel remoteToLocal(@NonNull final Defaults.LanguageModel languageModel) {

            switch (languageModel) {

                case LOCAL:
                    return LanguageModel.LOCAL;
                case NUANCE:
                    return LanguageModel.NUANCE;
                case MICROSOFT:
                    return LanguageModel.MICROSOFT;
                case API_AI:
                    return LanguageModel.API_AI;
                case WIT:
                    return LanguageModel.WIT;
                case REMOTE:
                    return LanguageModel.REMOTE;
                default:
                    return LanguageModel.LOCAL;
            }
        }
    }

    public enum TTS {

        LOCAL(Defaults.TTS.LOCAL),
        NETWORK_NUANCE(Defaults.TTS.NETWORK_NUANCE);

        private final Defaults.TTS tts;

        TTS(final Defaults.TTS tts) {
            this.tts = tts;
        }

        public Defaults.TTS getRemoteTTS() {
            return this.tts;
        }

        /**
         * Convert a remote TTS Default object to the local variant
         *
         * @param remoteTTS the remote {@link ai.saiy.android.api.Defaults.TTS}
         * @return the local variant of {@link SaiyDefaults.TTS}
         */
        public static TTS remoteToLocal(@NonNull final Defaults.TTS remoteTTS) {

            switch (remoteTTS) {

                case LOCAL:
                    return TTS.LOCAL;
                case NETWORK_NUANCE:
                    return TTS.NETWORK_NUANCE;
                default:
                    return TTS.LOCAL;
            }
        }
    }

    public enum VR {

        NATIVE(Defaults.VR.NATIVE),
        GOOGLE_CLOUD(Defaults.VR.GOOGLE_CLOUD),
        GOOGLE_CHROMIUM(Defaults.VR.GOOGLE_CHROMIUM),
        NUANCE(Defaults.VR.NUANCE),
        MICROSOFT(Defaults.VR.MICROSOFT),
        WIT(Defaults.VR.WIT),
        IBM(Defaults.VR.IBM),
        REMOTE(Defaults.VR.REMOTE),
        MIC(null);

        private final Defaults.VR vr;

        VR(final Defaults.VR vr) {
            this.vr = vr;
        }

        public Defaults.VR getRemoteVR() {
            return vr;
        }

        /**
         * Convert a remote VR Default object to the local variant
         *
         * @param remoteVR the remote {@link ai.saiy.android.api.Defaults.VR}
         * @return the local variant of {@link SaiyDefaults.VR}
         */
        public static VR remoteToLocal(@NonNull final Defaults.VR remoteVR) {

            switch (remoteVR) {

                case NATIVE:
                    return VR.NATIVE;
                case GOOGLE_CLOUD:
                    return VR.GOOGLE_CLOUD;
                case GOOGLE_CHROMIUM:
                    return VR.GOOGLE_CHROMIUM;
                case NUANCE:
                    return VR.NUANCE;
                case MICROSOFT:
                    return VR.MICROSOFT;
                case WIT:
                    return VR.WIT;
                case IBM:
                    return VR.IBM;
                case REMOTE:
                    return VR.REMOTE;
                default:
                    return VR.NATIVE;
            }
        }
    }

    public static TTS getProviderTTS(@Nullable final String name) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getProviderTTS: " + name);
        }

        if (name != null) {

            try {
                return Enum.valueOf(TTS.class, name.trim());
            } catch (final IllegalArgumentException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getProviderTTS: IllegalArgumentException");
                    e.printStackTrace();
                }
                return TTS.LOCAL;
            }

        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getProviderTTS: name null");
            }
            return TTS.LOCAL;
        }
    }

    public static VR getProviderVR(final String name) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getProviderVR: " + name);
        }

        if (name != null) {

            try {
                return Enum.valueOf(VR.class, name.trim());
            } catch (final IllegalArgumentException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getProviderVR: IllegalArgumentException");
                    e.printStackTrace();
                }
                return VR.NATIVE;
            }

        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getProviderVR: name null");
            }
            return VR.NATIVE;
        }
    }

    public static LanguageModel getLanguageModel(final String name) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getLanguageModel: " + name);
        }

        if (name != null) {

            try {
                return Enum.valueOf(LanguageModel.class, name.trim());
            } catch (final IllegalArgumentException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getLanguageModel: IllegalArgumentException");
                    e.printStackTrace();
                }
                return LanguageModel.LOCAL;
            }

        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getLanguageModel: name null");
            }
            return LanguageModel.LOCAL;
        }
    }
}
