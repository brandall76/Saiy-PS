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

package ai.saiy.android.command.vocalrecognition;

/**
 * Created by benrandall76@gmail.com on 13/08/2016.
 */

import android.content.Context;
import android.support.annotation.NonNull;

import ai.saiy.android.R;
import ai.saiy.android.cognitive.identity.provider.microsoft.Speaker;
import ai.saiy.android.cognitive.identity.provider.microsoft.containers.ProfileItem;
import ai.saiy.android.localisation.SaiyResourcesHelper;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Qubit;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.user.SaiyAccount;
import ai.saiy.android.user.SaiyAccountHelper;
import ai.saiy.android.user.SaiyAccountList;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

/**
 * Class to process a command request. Used only to decide which introduction the user should hear.
 * <p/>
 * Created by benrandall76@gmail.com on 10/02/2016.
 */
public class CommandVocalRecognition {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandVocalRecognition.class.getSimpleName();

    /**
     * Resolve the required command returning an {@link Outcome} object
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage} we are using to analyse the voice data.
     * @return {@link Outcome} containing everything we need to respond to the command.
     */
    public Outcome getResponse(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {

        final long then = System.nanoTime();

        final Outcome outcome = new Outcome();
        outcome.setQubit(new Qubit());

        final SaiyAccountList saiyAccountList = SaiyAccountHelper.getAccounts(ctx);

        if (saiyAccountList != null && saiyAccountList.size() > 0) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "saiyAccountList.size: " + saiyAccountList.size());
            }

            // TODO - handle multiple accounts

            switch (saiyAccountList.size()) {

                case 1:
                default:

                    final SaiyAccount saiyAccount = saiyAccountList.getSaiyAccountList().get(0);

                    if (saiyAccount != null) {

                        final ProfileItem profileItem = saiyAccount.getProfileItem();

                        if (profileItem != null) {

                            final String profileId = profileItem.getId();

                            if (UtilsString.notNaked(profileId)) {
                                if (DEBUG) {
                                    MyLog.d(CLS_NAME, "profileId: " + profileId);
                                }

                                final Speaker.Status status = Speaker.Status.getStatus(profileItem.getStatus());
                                if (DEBUG) {
                                    MyLog.d(CLS_NAME, "status: " + status.name());
                                }

                                switch (status) {

                                    case SUCCEEDED:

                                        outcome.setUtterance(SaiyResourcesHelper.getStringResource(
                                                ctx, sl, R.string.speech_enroll_instructions_15));
                                        outcome.setAction(LocalRequest.ACTION_SPEAK_LISTEN);
                                        outcome.setOutcome(Outcome.SUCCESS);
                                        outcome.setExtra(profileId);

                                        break;
                                    default:
                                        if (DEBUG) {
                                            MyLog.w(CLS_NAME, "enrollment status");
                                        }

                                        outcome.setOutcome(Outcome.FAILURE);
                                        outcome.setUtterance(SaiyResourcesHelper.getStringResource(
                                                ctx, sl, R.string.error_vi_status));
                                        break;
                                }
                            } else {
                                if (DEBUG) {
                                    MyLog.w(CLS_NAME, "profile id naked");
                                }

                                outcome.setOutcome(Outcome.FAILURE);
                                outcome.setUtterance(SaiyResourcesHelper.getStringResource(
                                        ctx, sl, R.string.error_vi_status));
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "profile item null");
                            }

                            outcome.setOutcome(Outcome.FAILURE);
                            outcome.setUtterance(SaiyResourcesHelper.getStringResource(
                                    ctx, sl, R.string.error_vi_status));
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "account null");
                        }

                        outcome.setOutcome(Outcome.FAILURE);
                        outcome.setUtterance(SaiyResourcesHelper.getStringResource(
                                ctx, sl, R.string.error_vi_no_account));
                    }

                    break;
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "no accounts");
            }

            outcome.setOutcome(Outcome.FAILURE);
            outcome.setUtterance(SaiyResourcesHelper.getStringResource(
                    ctx, sl, R.string.error_vi_no_account));
        }

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }

        return outcome;

    }
}
