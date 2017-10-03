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

package ai.saiy.android.personality;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import ai.saiy.android.R;
import ai.saiy.android.api.request.SaiyRequestParams;
import ai.saiy.android.applications.Installed;
import ai.saiy.android.localisation.SaiyResourcesHelper;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsString;

/**
 * Helper Class to get standard speech responses. Responses that are user defined,
 * or more personal should be handled in {@link PersonalityHelper}
 * <p>
 * Created by benrandall76@gmail.com on 13/02/2016.
 */
public final class PersonalityResponse {

    /**
     * Prevent instantiation
     */
    public PersonalityResponse() {
        throw new IllegalArgumentException(Resources.getSystem().getString(android.R.string.no));
    }

    /**
     * Get the Beyond Verbal intro response
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the introduction
     */
    public static String getBeyondVerbalIntroResponse(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_beyond_verbal);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the Beyond Verbal error response
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the introduction
     */
    public static String getBeyondVerbalErrorResponse(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_beyond_verbal_error);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the Beyond Verbal verbose introduction.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getBeyondVerbalVerboseResponse(@NonNull final Context ctx,
                                                        @NonNull final SupportedLanguage sl) {
        return UtilsString.stripNameSpace(String.format(SaiyResourcesHelper.getStringResource(ctx, sl,
                R.string.beyond_verbal_verbose), PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the Beyond Verbal extra verbose introduction.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getBeyondVerbalExtraVerboseResponse(@NonNull final Context ctx,
                                                             @NonNull final SupportedLanguage sl) {
        return UtilsString.stripNameSpace(String.format(SaiyResourcesHelper.getStringResource(ctx, sl,
                R.string.beyond_verbal_extra_verbose), PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the Beyond Verbal connection error response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getBeyondVerbalServerErrorResponse(@NonNull final Context ctx,
                                                            @NonNull final SupportedLanguage sl) {
        return UtilsString.stripNameSpace(String.format(SaiyResourcesHelper.getStringResource(ctx, sl,
                R.string.error_beyond_verbal_connection), PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the standard Tasker task executed response.
     *
     * @param ctx      the application context
     * @param sl       the {@link SupportedLanguage}
     * @param taskName the task name
     * @return the required response
     */
    public static String getTaskerTaskExecutedResponse(@NonNull final Context ctx,
                                                       @NonNull final SupportedLanguage sl,
                                                       @NonNull final String taskName) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_tasker);
        return String.format(stringArray[new Random().nextInt(stringArray.length)], taskName);
    }

    /**
     * Get the standard Tasker task executed response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getTaskerTaskNotMatchedResponse(@NonNull final Context ctx,
                                                         @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl,
                R.array.array_error_tasker_match);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }


    /**
     * Get the standard Task task failed response.
     *
     * @param ctx      the application context
     * @param sl       the {@link SupportedLanguage}
     * @param taskName the task name
     * @return the required response
     */
    public static String getTaskerTaskFailedResponse(@NonNull final Context ctx, @NonNull final SupportedLanguage sl,
                                                     @NonNull final String taskName) {
        return String.format(SaiyResourcesHelper.getStringResource(ctx, sl,
                R.string.error_tasker_execute), taskName);
    }

    /**
     * Get the standard no Tasker tasks response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getTaskerNoTasksResponse(@NonNull final Context ctx,
                                                  @NonNull final SupportedLanguage sl) {
        return SaiyResourcesHelper.getStringResource(ctx, sl, R.string.error_tasker_no_tasks);
    }

    /**
     * Get the standard no Tasker external access response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getTaskerExternalAccessResponse(@NonNull final Context ctx,
                                                         @NonNull final SupportedLanguage sl) {
        return SaiyResourcesHelper.getStringResource(ctx, sl, R.string.error_tasker_external_access);
    }

    /**
     * Get the standard no Tasker disabled response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getTaskerDisabledResponse(@NonNull final Context ctx,
                                                   @NonNull final SupportedLanguage sl) {
        return SaiyResourcesHelper.getStringResource(ctx, sl, R.string.error_tasker_enable);
    }

    /**
     * Get the standard no Tasker not installed response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getTaskerInstallResponse(@NonNull final Context ctx,
                                                  @NonNull final SupportedLanguage sl) {
        return SaiyResourcesHelper.getStringResource(ctx, sl, R.string.error_tasker_install);
    }

    /**
     * Get the standard no Tasker install order issue response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getTaskerInstallOrderResponse(@NonNull final Context ctx,
                                                       @NonNull final SupportedLanguage sl) {
        return SaiyResourcesHelper.getStringResource(ctx, sl, R.string.error_tasker_permissions);
    }

    /**
     * Get the speech introduction, either a user defined one or an inbuilt intro, adding the user's
     * name if known.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the introduction
     */
    public static String getIntro(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {

        final String userIntro = SPH.getCustomIntro(ctx);

        if (userIntro != null) {

            if (userIntro.isEmpty()) {
                return SaiyRequestParams.SILENCE;
            } else {

                if (SPH.getCustomIntroRandom(ctx)) {

                    final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl,
                            R.array.array_intro);

                    final ArrayList<String> list = new ArrayList<>(stringArray.length);
                    Collections.addAll(list, stringArray);
                    list.add(userIntro);

                    return UtilsString.stripNameSpace(String.format(list.get(new Random()
                            .nextInt(list.size())), PersonalityHelper.getUserNameOrNot(ctx)));
                } else {
                    return userIntro;
                }
            }
        } else {
            final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl,
                    R.array.array_intro);
            return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                    PersonalityHelper.getUserNameOrNot(ctx)));
        }
    }

    /**
     * Get the standard profanity filter response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getErrorProfanityFilter(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        return SaiyResourcesHelper.getStringResource(ctx, sl, R.string.error_empty_profanity);
    }

    /**
     * Get the standard empty voice data response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getErrorEmptyVoiceData(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        return SaiyResourcesHelper.getStringResource(ctx, sl, R.string.error_empty_voice_data);
    }

    /**
     * Get the standard action unknown response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getErrorActionUnknown(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        return SaiyResourcesHelper.getStringResource(ctx, sl, R.string.error_unknown_action);
    }

    /**
     * Get the standard remote command failed response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getErrorRemoteFailed(@NonNull final Context ctx, @NonNull final SupportedLanguage sl,
                                              @NonNull final String appName) {
        return String.format(SaiyResourcesHelper.getStringResource(ctx, sl,
                R.string.error_remote), appName);
    }

    /**
     * Get the standard remote command failed unknown response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getErrorRemoteFailedUnknown(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        return SaiyResourcesHelper.getStringResource(ctx, sl, R.string.error_remote_unknown);
    }

    /**
     * Get the standard remote command registration failure response.
     *
     * @param ctx     the application context
     * @param sl      the {@link SupportedLanguage}
     * @param appName the remote application name
     * @return the required response
     */
    public static String getErrorRemoteCommandRegister(@NonNull final Context ctx, @NonNull final SupportedLanguage sl,
                                                       @NonNull final String appName) {
        return String.format(SaiyResourcesHelper.getStringResource(ctx, sl,
                R.string.error_remote_command_register), appName);
    }

    /**
     * Get the standard remote command registration success response.
     *
     * @param ctx     the application context
     * @param sl      the {@link SupportedLanguage}
     * @param appName the remote application name
     * @return the required response
     */
    public static String getRemoteCommandRegisterSuccess(@NonNull final Context ctx,
                                                         @NonNull final SupportedLanguage sl,
                                                         @NonNull final String appName,
                                                         @NonNull final String keyphrase) {
        return String.format(SaiyResourcesHelper.getStringResource(ctx, sl,
                R.string.remote_command_register_response), appName, keyphrase);
    }

    /**
     * Get the standard remote command success response.
     *
     * @param ctx     the application context
     * @param sl      the {@link SupportedLanguage}
     * @param appName the remote application name
     * @return the required response
     */
    public static String getRemoteSuccess(@NonNull final Context ctx, @NonNull final SupportedLanguage sl,
                                          @NonNull final String appName) {
        return String.format(SaiyResourcesHelper.getStringResource(ctx, sl, R.string.remote_success), appName);
    }

    /**
     * Get the standard no network connection response, adding the user's name if defined.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getNoNetwork(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_no_network);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the standard no comprende response, adding the user's name if defined.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getNoComprende(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_no_comprende);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the standard repeat command response, adding the user's name if defined.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getRepeatCommand(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_repeat_command);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the standard cancel response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getCancelled(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_cancel);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    /**
     * Get the standard user name response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getUserName(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_user_name);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    /**
     * Get the standard song recognition response.
     *
     * @param ctx     the application context
     * @param sl      the {@link SupportedLanguage}
     * @param appName the default provider
     * @return the required response
     */
    public static String getSongRecognitionResponse(@NonNull final Context ctx, @NonNull final SupportedLanguage sl,
                                                    @NonNull final String appName) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_song_recognition);
        return String.format(stringArray[new Random().nextInt(stringArray.length)], appName);
    }

    /**
     * Get the secure error response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getSecureErrorResponse(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_error_secure);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the standard battery information response.
     *
     * @param ctx   the application context
     * @param sl    the {@link SupportedLanguage}
     * @param type  the requested battery information type
     * @param value the value of the requested type
     * @return the required response
     */
    public static String getBatteryResponse(@NonNull final Context ctx, @NonNull final SupportedLanguage sl,
                                            @NonNull final String type, @NonNull final String value) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_battery);
        return String.format(stringArray[new Random().nextInt(stringArray.length)], type, value);
    }

    /**
     * Get the battery error unknown request response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getBatteryErrorUnknownResponse(@NonNull final Context ctx,
                                                        @NonNull final SupportedLanguage sl) {
        return UtilsString.stripNameSpace(String.format(SaiyResourcesHelper.getStringResource(ctx, sl,
                R.string.error_battery_unknown), PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the battery error access request response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getBatteryErrorAccessResponse(@NonNull final Context ctx,
                                                       @NonNull final SupportedLanguage sl) {
        return UtilsString.stripNameSpace(String.format(SaiyResourcesHelper.getStringResource(ctx, sl,
                R.string.error_battery_access), PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the song recognition app opening error response.
     *
     * @param ctx     the application context
     * @param sl      the {@link SupportedLanguage}
     * @param appName the default provider
     * @return the required response
     */
    public static String getSongRecognitionErrorAppResponse(@NonNull final Context ctx,
                                                            @NonNull final SupportedLanguage sl,
                                                            @NonNull final String appName) {
        return UtilsString.stripNameSpace(String.format(SaiyResourcesHelper.getStringResource(ctx, sl,
                R.string.error_song_recognition_app_failed), PersonalityHelper.getUserNameOrNot(ctx), appName));
    }

    /**
     * Get the song recognition app no longer installed error.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getSongRecognitionErrorNoApp(@NonNull final Context ctx,
                                                      @NonNull final SupportedLanguage sl) {
        return SaiyResourcesHelper.getStringResource(ctx, sl, R.string.error_song_recognition_chooser);
    }

    /**
     * Get the song recognition app no longer installed error.
     *
     * @param ctx     the application context
     * @param sl      the {@link SupportedLanguage}
     * @param appName the default provider
     * @return the required response
     */
    public static String getSongRecognitionErrorAppUninstalled(@NonNull final Context ctx,
                                                               @NonNull final SupportedLanguage sl,
                                                               @NonNull final String appName) {
        return String.format(SaiyResourcesHelper.getStringResource(ctx, sl,
                R.string.error_song_recognition_default_app), appName);
    }

    /**
     * Get the standard user name response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getUserNameRepeat(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_user_name_repeat);
        return UtilsString.stripNameSpace(stringArray[new Random().nextInt(stringArray.length)]);
    }

    /**
     * Get the standard user name error response, adding the user's name if defined.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getUserNameError(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_user_name_error);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the standard Wolfram Alpha error response, adding the user's name if defined.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getWolframAlphaError(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl,
                R.array.array_error_wolfram_alpha_unknown);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the Wolfram Alpha response intro.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getWolframAlphaIntro(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_wolfram_alpha_intro);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    /**
     * Get the standard no memory response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getNoMemory(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] responseArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_no_memory);
        final String[] extraArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_no_memory_extra);
        final String[] extraUnknownArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_no_memory_extra_unknown);

        String extra = extraArray[new Random().nextInt(extraArray.length)];

        if (extra.matches(ctx.getString(R.string.memory_extra_facebook)) &&
                !Installed.isPackageInstalled(ctx, Installed.PACKAGE_FACEBOOK)) {
            extra = extraUnknownArray[new Random().nextInt(extraUnknownArray.length)];
        } else if (extra.matches(ctx.getString(R.string.memory_extra_twitter)) &&
                !Installed.isPackageInstalled(ctx, Installed.PACKAGE_TWITTER)) {
            extra = extraUnknownArray[new Random().nextInt(extraUnknownArray.length)];
        } else if (extra.matches(ctx.getString(R.string.memory_extra_tinder)) &&
                !Installed.isPackageInstalled(ctx, Installed.PACKAGE_TINDER)) {
            extra = extraUnknownArray[new Random().nextInt(extraUnknownArray.length)];
        } else if (extra.matches(ctx.getString(R.string.memory_extra_whatsapp)) &&
                !Installed.isPackageInstalled(ctx, Installed.PACKAGE_WHATSAPP)) {
            extra = extraUnknownArray[new Random().nextInt(extraUnknownArray.length)];
        } else if (extra.matches(ctx.getString(R.string.memory_extra_snapchat)) &&
                !Installed.isPackageInstalled(ctx, Installed.PACKAGE_SNAPCHAT)) {
            extra = extraUnknownArray[new Random().nextInt(extraUnknownArray.length)];
        }

        return UtilsString.stripNameSpace(String.format(responseArray[new Random().nextInt(responseArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx), extra));
    }

    /**
     * Get the standard clipboard response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getClipboardSpell(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_clipboard_copy);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    /**
     * Get the standard clipboard error data response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getClipboardDataError(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_error_clipboard_data);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    /**
     * Get the standard clipboard error access response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getClipboardAccessError(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_error_clipboard_access);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    /**
     * Get the standard spell error response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getSpellError(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_error_spell);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    /**
     * Get the vocal enrollment error response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getEnrollmentError(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl,
                R.array.array_error_enrollment);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the vocal id error response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getVocalIDError(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl,
                R.array.array_error_vocal_id);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the vocal id high response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getVocalIDHigh(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl,
                R.array.array_vocal_id_high);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the vocal id medium response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getVocalIDMedium(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl,
                R.array.array_vocal_id_medium);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the vocal id low response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getVocalIDLow(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_vocal_id_low);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    /**
     * Get the vocal enrollment error response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getEnrollmentAPIError(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl,
                R.array.array_error_enrollment_api);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the BV analysis complete response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getBVAnalysisCompleteResponse(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_bv_analysis_complete);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }
}