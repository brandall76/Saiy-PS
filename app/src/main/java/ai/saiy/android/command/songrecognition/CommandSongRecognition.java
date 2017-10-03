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

package ai.saiy.android.command.songrecognition;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import ai.saiy.android.applications.Installed;
import ai.saiy.android.defaults.ApplicationDefaults;
import ai.saiy.android.defaults.songrecognition.SongRecognitionChooser;
import ai.saiy.android.defaults.songrecognition.SongRecognitionProvider;
import ai.saiy.android.intent.ExecuteIntent;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.processing.Qubit;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.MyLog;

/**
 * Created by benrandall76@gmail.com on 10/06/2016.
 */
public class CommandSongRecognition {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandSongRecognition.class.getSimpleName();

    private final Outcome outcome = new Outcome();
    private final Qubit qubit = new Qubit();
    private final Intent intent = new Intent();

    /**
     * Resolve the required command returning an {@link Outcome} object
     *
     * @param ctx       the application context
     * @param voiceData ArrayList<String> containing the voice data
     * @param sl        the {@link SupportedLanguage} we are using to analyse the voice data.
     *                  This is not necessarily the Locale of the device, as the user may be
     *                  multi-lingual and/or have set a custom recognition language in a launcher short-cut.
     * @return {@link Outcome} containing everything we need to respond to the command.
     */
    public Outcome getResponse(@NonNull final Context ctx, @NonNull final ArrayList<String> voiceData,
                               @NonNull final SupportedLanguage sl) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData.toString());
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        outcome.setOutcome(Outcome.SUCCESS);

        final SongRecognitionProvider provider = ApplicationDefaults.getSongRecognitionProvider(ctx);

        if (setProvider(provider)) {
            if (packageInstalled(ctx, provider)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "provider resolved. Starting");
                }
                startProvider(ctx, sl, provider);
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "default provider no longer installed");
                }

                // check if only one
                final ArrayList<SongRecognitionProvider> providers = getSongRecognitionProviders(ctx);
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getSongRecognitionProviders: " + providers.size());
                }

                switch (providers.size()) {

                    case 0:
                        // no applications installed, show chooser
                        outcome.setUtterance(PersonalityResponse.getSongRecognitionErrorAppUninstalled(
                                ctx, sl, SongRecognitionProvider.getApplicationName(ctx, sl, provider)));
                        outcome.setOutcome(Outcome.FAILURE);
                        prepareChooser(ctx, sl);
                        outcome.setQubit(qubit);
                        break;
                    case 1:
                        // Only one - start
                        final SongRecognitionProvider singleProvider = providers.get(0);
                        setProvider(singleProvider);
                        startProvider(ctx, sl, provider);
                        break;
                    default:
                        // More than one, show chooser
                        outcome.setUtterance(PersonalityResponse.getSongRecognitionErrorAppUninstalled(
                                ctx, sl, SongRecognitionProvider.getApplicationName(ctx, sl, provider)));
                        outcome.setOutcome(Outcome.FAILURE);
                        prepareChooser(ctx, sl);
                        outcome.setQubit(qubit);
                        break;
                }
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "no default application");
            }

            // check if only one
            final ArrayList<SongRecognitionProvider> providers = getSongRecognitionProviders(ctx);
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getSongRecognitionProviders: " + providers.size());
            }

            switch (providers.size()) {

                case 1:
                    // Only one - start
                    final SongRecognitionProvider singleProvider = providers.get(0);
                    setProvider(singleProvider);
                    startProvider(ctx, sl, provider);
                    break;
                default:
                    // More than one or none, show chooser
                    outcome.setUtterance(PersonalityResponse.getSongRecognitionErrorNoApp(ctx, sl));
                    outcome.setOutcome(Outcome.FAILURE);
                    prepareChooser(ctx, sl);
                    outcome.setQubit(qubit);
                    break;
            }
        }

        return outcome;
    }

    /**
     * Prepare the {@link Qubit} to contain an {@link ArrayList} of {@link SongRecognitionChooser}
     * so the user can select or install a default choice.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     */
    private void prepareChooser(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        qubit.setSongRecognitionChooserList(SongRecognitionChooser.prepareChooser(ctx, sl));
    }

    /**
     * Get a list of supported install song recognition providers
     *
     * @param ctx the application context
     * @return an {@link ArrayList} of {@link SongRecognitionProvider}
     */
    private ArrayList<SongRecognitionProvider> getSongRecognitionProviders(@NonNull final Context ctx) {
        return Installed.getSongRecognitionProviders(ctx);
    }

    /**
     * A provider is resolved. Attempt to start the provider.
     * <p>
     * If this process fails, it suggests there is a problem with the song recognition application.
     * Attempting to take further action to help the user resolve this, could end up with the command
     * stuck in a loop.
     *
     * @param ctx      the application context
     * @param sl       the {@link SupportedLanguage}
     * @param provider the {@link SongRecognitionProvider} provider
     */
    private void startProvider(@NonNull final Context ctx, @NonNull final SupportedLanguage sl,
                               @NonNull final SongRecognitionProvider provider) {

        outcome.setOutcome(Outcome.SUCCESS);

        if (ExecuteIntent.executeIntent(ctx, intent)) {
            outcome.setUtterance(getResponseUtterance(ctx, sl, provider));
        } else {
            outcome.setUtterance(PersonalityResponse.getSongRecognitionErrorAppResponse(
                    ctx, sl, SongRecognitionProvider.getApplicationName(ctx, sl, provider)));
        }
    }

    /**
     * Set the {@link Intent#setAction(String)}
     *
     * @param provider the user's default {@link SongRecognitionProvider} provider
     * @return true if the process was successful or false if the {@link SongRecognitionProvider#UNKNOWN}
     */
    private boolean setProvider(@NonNull final SongRecognitionProvider provider) {

        switch (provider) {

            case SHAZAM:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "setProvider: " + SongRecognitionProvider.SHAZAM.name());
                }
                intent.setAction(SongRecognitionProvider.SHAZAM_ACTION);
                break;
            case SOUND_HOUND:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "setProvider: " + SongRecognitionProvider.SOUND_HOUND.name());
                }
                intent.setAction(SongRecognitionProvider.SOUND_HOUND_ACTION);
                break;
            case TRACK_ID:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "setProvider: " + SongRecognitionProvider.TRACK_ID.name());
                }
                intent.setAction(SongRecognitionProvider.TRACK_ID_ACTION);
                break;
            case GOOGLE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "setProvider: " + SongRecognitionProvider.GOOGLE.name());
                }
                intent.setAction(SongRecognitionProvider.GOOGLE_ACTION);
                break;
            case UNKNOWN:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "setProvider: " + SongRecognitionProvider.UNKNOWN.name());
                }
                return false;
        }

        return true;
    }

    /**
     * Check if the user's default {@link SongRecognitionProvider} provider is still installed.
     *
     * @param ctx      the application context
     * @param provider the user's default {@link SongRecognitionProvider} provider
     * @return true if the package is still installed. False otherwise.
     */
    private boolean packageInstalled(@NonNull final Context ctx, @NonNull final SongRecognitionProvider provider) {

        switch (provider) {
            case SHAZAM:
                return Installed.shazamInstalled(ctx);
            case SOUND_HOUND:
                return Installed.soundHoundInstalled(ctx);
            case TRACK_ID:
                return Installed.isPackageInstalled(ctx, Installed.PACKAGE_TRACK_ID);
            case GOOGLE:
                return Installed.isPackageInstalled(ctx, Installed.PACKAGE_GOOGLE_SOUND_SEARCH);
        }

        return false;
    }

    /**
     * Get the utterance to speak whilst starting the song recognition application.
     *
     * @param ctx      the application context
     * @param sl       the {@link SupportedLanguage}
     * @param provider the {@link SongRecognitionProvider}
     * @return the String utterance
     */
    private String getResponseUtterance(@NonNull final Context ctx, @NonNull final SupportedLanguage sl,
                                        @NonNull final SongRecognitionProvider provider) {
        return PersonalityResponse.getSongRecognitionResponse(ctx, sl,
                SongRecognitionProvider.getApplicationName(ctx, sl, provider));
    }
}
