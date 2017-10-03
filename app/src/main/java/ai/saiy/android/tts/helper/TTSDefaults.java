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

package ai.saiy.android.tts.helper;

import android.support.annotation.NonNull;

import org.apache.commons.lang3.ArrayUtils;

import java.util.regex.Pattern;

import ai.saiy.android.tts.attributes.Gender;
import ai.saiy.android.utils.UtilsString;

/**
 * Created by benrandall76@gmail.com on 14/04/2016.
 */
public class TTSDefaults {

    public static final int TYPE_NETWORK = 1;
    public static final int TYPE_LOCAL = 2;

    public static final String TTS_PKG_NAME_GOOGLE = "com.google.android.tts";
    public static final String TTS_PKG_NAME_IVONA = "com.ivona.tts";
    public static final String TTS_PKG_NAME_PICO = "com.svox.pico";
    public static final String TTS_PKG_NAME_CEREPROC = "com.cereproc";
    public static final String TTS_PKG_NAME_SVOX = "com.svox.classic";
    public static final String TTS_PKG_NAME_VOCALIZER = "es.codefactory.vocalizertts";

    public static final String BOUND_ENGINE_FIELD = "mCurrentEngine";
    public static final String BOUND_ENGINE_METHOD = "getCurrentEngine";
    public static final String LEGACY_ENGINE_FIELD = "LegacySetLanguageVoice";
    public static final String EMBEDDED_TTS_FIELD = "embeddedTts";
    public static final String EXTRA_INTERRUPTED = "extra_interrupted";
    public static final String EXTRA_INTERRUPTED_FORCED = "extra_interrupted_forced";

    private static final String[] APPROVED_ENGINES = {TTS_PKG_NAME_VOCALIZER, TTS_PKG_NAME_GOOGLE};

    protected static final Pattern pTTS_PKG_NAME_GOOGLE = Pattern.compile(TTS_PKG_NAME_GOOGLE);

    /**
     * Due to only Google TTS correctly handling the new Voice API features, it is the only 'approved' package
     * to perform more advanced features. Others will be handled using depreciated methods.
     *
     * @param initialisedEngine of the tts object
     * @return true if the engine is approved for advanced voice features, false otherwise.
     */
    public static boolean isApprovedVoice(@NonNull final String initialisedEngine) {
        return UtilsString.notNaked(initialisedEngine) && ArrayUtils.contains(APPROVED_ENGINES, initialisedEngine);
    }

    public enum Google {

        GOOGLE_EN_AU_X_AFH_LOCAL("en-au-x-afh-local", Gender.FEMALE),
        GOOGLE_EN_AU_X_AFH_NETWORK("en-au-x-afh-network", Gender.FEMALE),
        GOOGLE_EN_AU_LANGUAGE("en-AU-language", Gender.FEMALE),
        GOOGLE_EN_GB_X_FIS_FEMALE_2_LOCAL("en-gb-x-fis#female_2-local", Gender.FEMALE),
        GOOGLE_EN_GB_X_FIS_MALE_3_LOCAL("en-gb-x-fis#male_3-local", Gender.MALE),
        GOOGLE_EN_GB_X_RJS_FEMALE_2_LOCAL("en-gb-x-rjs#female_2-local", Gender.FEMALE),
        GOOGLE_EN_GB_X_RJS_MALE_3_LOCAL("en-gb-x-rjs#male_3-local", Gender.FEMALE),
        GOOGLE_EN_GB_X_RJS_FEMALE_3_LOCAL("en-gb-x-rjs#female_3-local", Gender.FEMALE),
        GOOGLE_EN_GB_X_RJS_NETWORK("en-gb-x-rjs-network", Gender.MALE),
        GOOGLE_EN_GB_X_RJS_FEMALE_1_LOCAL("en-gb-x-rjs#female_1-local", Gender.FEMALE),
        GOOGLE_EN_GB_X_FIS_MALE_1_LOCAL("en-gb-x-fis#male_1-local", Gender.MALE),
        GOOGLE_EN_GB_X_FIS_LOCAL("en-gb-x-fis-local", Gender.FEMALE),
        GOOGLE_EN_GB_X_RJS_MALE_2_LOCAL("en-gb-x-rjs#male_2-local", Gender.MALE),
        GOOGLE_EN_GB_X_FIS_FEMALE_1_LOCAL("en-gb-x-fis#female_1-local", Gender.FEMALE),
        GOOGLE_EN_GB_X_RJS_MALE_1_LOCAL("en-gb-x-rjs#male_1-local", Gender.MALE),
        GOOGLE_EN_GB_X_FIS_MALE_2_LOCAL("en-gb-x-fis#male_2-local", Gender.MALE),
        GOOGLE_EN_GB_X_FIS_NETWORK("en-gb-x-fis-network", Gender.FEMALE),
        GOOGLE_EN_GB_X_FIS_FEMALE_3_LOCAL("en-gb-x-fis#female_3-local", Gender.FEMALE),
        GOOGLE_EN_GB_X_RJS_LOCAL("en-gb-x-rjs-local", Gender.MALE),
        GOOGLE_EN_GB_LANGUAGE("en-GB-language", Gender.FEMALE),
        GOOGLE_EN_IN_X_AHP_NETWORK("en-in-x-ahp-network", Gender.FEMALE),
        GOOGLE_EN_IN_X_CXX_LOCAL("en-in-x-cxx-local", Gender.FEMALE),
        GOOGLE_EN_IN_X_AHP_LOCAL("en-in-x-ahp-local", Gender.FEMALE),
        GOOGLE_EN_IN_LANGUAGE("en-IN-language", Gender.FEMALE),
        GOOGLE_EN_IN_X_CXX_NETWORK("en-in-x-cxx-network", Gender.FEMALE),
        GOOGLE_EN_US_X_SFG_FEMALE_2_LOCAL("en-us-x-sfg#female_2-local", Gender.FEMALE),
        GOOGLE_EN_US_X_SFG_MALE_3_LOCAL("en-us-x-sfg#male_3-local", Gender.MALE),
        GOOGLE_EN_US_X_SFG_FEMALE_1_LOCAL("en-us-x-sfg#female_1-local", Gender.FEMALE),
        GOOGLE_EN_US_LANGUAGE("en-US-language", Gender.FEMALE),
        GOOGLE_EN_US_X_SFG_MALE_2_LOCAL("en-us-x-sfg#male_2-local", Gender.MALE),
        GOOGLE_EN_US_X_SFG_FEMALE_3_LOCAL("en-us-x-sfg#female_3-local", Gender.FEMALE),
        GOOGLE_EN_US_X_SFG_NETWORK("en-us-x-sfg-network", Gender.FEMALE),
        GOOGLE_EN_US_X_SFG_LOCAL("en-us-x-sfg-local", Gender.FEMALE),
        GOOGLE_EN_US_X_SFG_MALE_1_LOCAL("en-us-x-sfg#male_1-local", Gender.MALE);

        private final String voiceName;
        private final Gender gender;

        Google(@NonNull final String voiceName, @NonNull final Gender gender) {
            this.voiceName = voiceName;
            this.gender = gender;
        }

        public Gender getGender() {
            return gender;
        }

        public String getVoiceName() {
            return voiceName;
        }

        public static Google getAssociatedVoice(@NonNull final Google google, final int type) {

            switch (google) {

                case GOOGLE_EN_AU_X_AFH_LOCAL:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_AU_X_AFH_LOCAL;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_AU_X_AFH_NETWORK;
                    }

                    break;
                case GOOGLE_EN_AU_X_AFH_NETWORK:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_AU_X_AFH_LOCAL;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_AU_X_AFH_NETWORK;
                    }

                    break;
                case GOOGLE_EN_AU_LANGUAGE:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_AU_LANGUAGE;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_AU_X_AFH_NETWORK;
                    }

                    break;
                case GOOGLE_EN_GB_X_FIS_FEMALE_2_LOCAL:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_GB_X_FIS_FEMALE_2_LOCAL;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_GB_X_FIS_NETWORK;
                    }

                    break;
                case GOOGLE_EN_GB_X_FIS_NETWORK:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_GB_X_FIS_FEMALE_2_LOCAL;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_GB_LANGUAGE;
                    }

                    break;
                case GOOGLE_EN_GB_X_FIS_MALE_2_LOCAL:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_GB_X_FIS_MALE_2_LOCAL;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_GB_X_RJS_NETWORK;
                    }

                    break;
                case GOOGLE_EN_GB_X_RJS_NETWORK:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_GB_X_RJS_LOCAL;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_GB_X_RJS_NETWORK;
                    }

                    break;
                case GOOGLE_EN_GB_X_FIS_MALE_3_LOCAL:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_GB_X_FIS_MALE_3_LOCAL;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_GB_X_RJS_NETWORK;
                    }

                    break;
                case GOOGLE_EN_GB_X_RJS_FEMALE_2_LOCAL:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_GB_X_RJS_FEMALE_2_LOCAL;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_GB_X_FIS_NETWORK;
                    }

                    break;
                case GOOGLE_EN_GB_X_RJS_MALE_3_LOCAL:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_GB_X_RJS_MALE_3_LOCAL;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_GB_X_RJS_NETWORK;
                    }

                    break;
                case GOOGLE_EN_GB_X_RJS_FEMALE_3_LOCAL:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_GB_X_RJS_FEMALE_3_LOCAL;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_GB_X_FIS_NETWORK;
                    }

                    break;
                case GOOGLE_EN_GB_X_RJS_FEMALE_1_LOCAL:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_GB_X_RJS_FEMALE_1_LOCAL;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_GB_X_FIS_NETWORK;
                    }

                    break;
                case GOOGLE_EN_GB_X_FIS_MALE_1_LOCAL:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_GB_X_FIS_MALE_1_LOCAL;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_GB_X_RJS_NETWORK;
                    }

                    break;
                case GOOGLE_EN_GB_X_FIS_LOCAL:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_GB_X_FIS_LOCAL;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_GB_X_FIS_NETWORK;
                    }

                    break;
                case GOOGLE_EN_GB_X_RJS_MALE_2_LOCAL:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_GB_X_RJS_MALE_2_LOCAL;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_GB_X_RJS_NETWORK;
                    }

                    break;
                case GOOGLE_EN_GB_X_FIS_FEMALE_1_LOCAL:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_GB_X_FIS_FEMALE_1_LOCAL;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_GB_X_FIS_NETWORK;
                    }

                    break;
                case GOOGLE_EN_GB_X_RJS_MALE_1_LOCAL:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_GB_X_RJS_MALE_1_LOCAL;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_GB_X_RJS_NETWORK;
                    }

                    break;
                case GOOGLE_EN_GB_X_FIS_FEMALE_3_LOCAL:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_GB_X_FIS_FEMALE_3_LOCAL;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_GB_X_FIS_NETWORK;
                    }

                    break;
                case GOOGLE_EN_GB_X_RJS_LOCAL:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_GB_X_RJS_LOCAL;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_GB_X_RJS_NETWORK;
                    }

                    break;
                case GOOGLE_EN_GB_LANGUAGE:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_GB_LANGUAGE;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_GB_X_FIS_NETWORK;
                    }

                    break;
                case GOOGLE_EN_IN_X_AHP_NETWORK:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_IN_X_AHP_LOCAL;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_IN_X_AHP_NETWORK;
                    }

                    break;
                case GOOGLE_EN_IN_X_CXX_LOCAL:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_IN_X_CXX_LOCAL;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_IN_X_CXX_NETWORK;
                    }

                    break;
                case GOOGLE_EN_IN_X_AHP_LOCAL:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_IN_X_AHP_LOCAL;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_IN_X_AHP_NETWORK;
                    }

                    break;
                case GOOGLE_EN_IN_LANGUAGE:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_IN_LANGUAGE;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_IN_X_CXX_NETWORK;
                    }

                    break;
                case GOOGLE_EN_IN_X_CXX_NETWORK:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_IN_X_CXX_LOCAL;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_IN_X_CXX_NETWORK;
                    }

                    break;
                case GOOGLE_EN_US_X_SFG_FEMALE_2_LOCAL:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_US_X_SFG_FEMALE_2_LOCAL;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_US_X_SFG_NETWORK;
                    }

                    break;
                case GOOGLE_EN_US_X_SFG_MALE_3_LOCAL:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_US_X_SFG_MALE_3_LOCAL;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_US_X_SFG_MALE_3_LOCAL;
                    }

                    break;
                case GOOGLE_EN_US_X_SFG_FEMALE_1_LOCAL:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_US_X_SFG_FEMALE_1_LOCAL;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_US_X_SFG_NETWORK;
                    }

                    break;
                case GOOGLE_EN_US_LANGUAGE:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_US_LANGUAGE;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_US_X_SFG_NETWORK;
                    }

                    break;
                case GOOGLE_EN_US_X_SFG_MALE_2_LOCAL:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_US_X_SFG_MALE_2_LOCAL;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_US_X_SFG_MALE_2_LOCAL;
                    }

                    break;
                case GOOGLE_EN_US_X_SFG_FEMALE_3_LOCAL:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_US_X_SFG_FEMALE_3_LOCAL;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_US_X_SFG_NETWORK;
                    }

                    break;
                case GOOGLE_EN_US_X_SFG_NETWORK:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_US_X_SFG_LOCAL;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_US_X_SFG_NETWORK;
                    }

                    break;
                case GOOGLE_EN_US_X_SFG_LOCAL:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_US_X_SFG_LOCAL;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_US_X_SFG_NETWORK;
                    }

                    break;
                case GOOGLE_EN_US_X_SFG_MALE_1_LOCAL:

                    switch (type) {

                        case TYPE_LOCAL:
                            return GOOGLE_EN_US_X_SFG_MALE_1_LOCAL;
                        case TYPE_NETWORK:
                            return GOOGLE_EN_US_X_SFG_MALE_1_LOCAL;
                    }

                    break;
            }

            return null;
        }

        /**
         * Get all Google voices
         *
         * @return a list of google voices
         */
        private static Google[] getEngines() {
            return Google.values();
        }

        public static Google getGoogle(@NonNull final String voiceName) {

            for (final Google g : getEngines()) {
                if (g.getVoiceName().matches("(?i)" + Pattern.quote(voiceName))) {
                    return g;
                }
            }

            return null;
        }

        /**
         * Check to see if the required gender is contained in the voice name or as part of the Gender object.
         *
         * @param voiceName      the voice name
         * @param requiredGender one of {@link Gender#FEMALE} or {@link Gender#MALE}
         * @return true if the required {@link Gender} matches for the engine name
         */

        public static boolean matchGender(@NonNull final String voiceName, @NonNull final Gender requiredGender) {

            for (final Google g : getEngines()) {
                if (g.getVoiceName().matches("(?i)" + Pattern.quote(voiceName))) {
                    return g.getGender() == requiredGender;
                }
            }

            switch (requiredGender) {
                case FEMALE:
                    return Gender.getGenderFromVoiceName(voiceName) == requiredGender;
                case MALE:
                    return Gender.getGenderFromVoiceName(voiceName) == requiredGender;
            }

            return false;
        }

        /**
         * Get the gender from the given voice name
         *
         * @param voiceName the voice name
         * @return one of {@link Gender#MALE} {@link Gender#FEMALE} or if unknown {@link Gender#UNDEFINED}
         */
        public static Gender getGender(@NonNull final String voiceName) {

            for (final Google g : getEngines()) {
                if (g.getVoiceName().matches("(?i)" + Pattern.quote(voiceName))) {
                    return g.getGender();
                }
            }

            return Gender.getGenderFromVoiceName(voiceName);
        }
    }
}
