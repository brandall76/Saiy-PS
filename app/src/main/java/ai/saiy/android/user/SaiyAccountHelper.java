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

package ai.saiy.android.user;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Pattern;

import ai.saiy.android.cognitive.identity.provider.microsoft.containers.OperationStatus;
import ai.saiy.android.cognitive.identity.provider.microsoft.containers.ProfileItem;
import ai.saiy.android.cognitive.identity.provider.microsoft.http.DeleteIDProfile;
import ai.saiy.android.configuration.MicrosoftConfiguration;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsString;

/**
 * Created by benrandall76@gmail.com on 09/09/2016.
 */

public class SaiyAccountHelper {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = SaiyAccountHelper.class.getSimpleName();

    /**
     * Check if we have Saiy accounts stored
     *
     * @param ctx the application context
     * @return true if an {@link SaiyAccountList} is stored
     */
    public static boolean haveAccounts(@NonNull final Context ctx) {
        return SPH.getSaiyAccounts(ctx) != null;
    }

    /**
     * Get the current Saiy account list. Must not be called unless accounts have been confirmed
     * to exists by {@link #haveAccounts(Context)}
     *
     * @param ctx the application context
     * @return the {@link SaiyAccountList}
     */
    public static SaiyAccountList getAccountList(@NonNull final Context ctx) {
        final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return decode(gson.fromJson(SPH.getSaiyAccounts(ctx), SaiyAccountList.class));
    }

    /**
     * Get all {@link SaiyAccount}
     *
     * @param ctx the application context
     * @return the {@link SaiyAccountList} or null if there are no accounts
     */
    public static SaiyAccountList getAccounts(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getAccounts");
        }

        if (haveAccounts(ctx)) {
            return getAccountList(ctx);
        }

        return null;
    }

    /**
     * Get the {@link SaiyAccount} from the account name or id.
     *
     * @param ctx         the application context
     * @param accountName the name of the account to check
     * @param accountId   the account id
     * @return the {@link SaiyAccount} if one exists, null otherwise
     */
    public static SaiyAccount getAccount(@NonNull final Context ctx, @Nullable final String accountName,
                                         @Nullable final String accountId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getAccount");
        }

        if (haveAccounts(ctx)) {

            final SaiyAccountList accountList = getAccountList(ctx);

            for (final SaiyAccount account : accountList.getSaiyAccountList()) {

                if (accountName != null && accountId != null) {
                    if (account.getAccountName().matches(Pattern.quote(accountName))
                            || (UtilsString.notNaked(account.getAccountId())
                            && account.getAccountId().matches(Pattern.quote(accountId)))) {
                        return account;
                    }
                } else if (accountName != null) {
                    if (account.getAccountName().matches(Pattern.quote(accountName))) {
                        return account;
                    }
                } else {
                    if (accountId != null && UtilsString.notNaked(account.getAccountId())) {
                        if (account.getAccountId().matches(Pattern.quote(accountId))) {
                            return account;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Add a Saiy account to the current list, is one exists
     *
     * @param ctx         the application context
     * @param saiyAccount the {@link SaiyAccount} object
     */
    public static void addSaiyAccount(@NonNull final Context ctx, @NonNull final SaiyAccount saiyAccount) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "addSaiyAccount");
        }

        final SaiyAccountList accountList;

        if (haveAccounts(ctx)) {

            accountList = getAccountList(ctx);
            if (DEBUG) {
                MyLog.i(CLS_NAME, "accountList: " + accountList.size());

                for (SaiyAccount account : accountList.getSaiyAccountList()) {
                    MyLog.v(CLS_NAME, "getAccountName: " + account.getAccountName());
                    MyLog.v(CLS_NAME, "getAccountId: " + account.getAccountId());
                    MyLog.v(CLS_NAME, "getPseudonym: " + account.getPseudonym());
                }
            }

        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "addSaiyAccount: no current");
            }

            final List<SaiyAccount> list = new ArrayList<>(1);
            list.add(saiyAccount);

            accountList = new SaiyAccountList(list);
        }

        saveSaiyAccountList(ctx, accountList);
    }

    /**
     * Save the Saiy account list to the shared preferences
     *
     * @param ctx         the application context
     * @param accountList the {@link SaiyAccountList}
     */
    public static boolean saveSaiyAccountList(@NonNull final Context ctx,
                                              @NonNull final SaiyAccountList accountList) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "saveSaiyAccountList");
        }

        if (accountList.size() > 0) {

            final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            final String gsonString = gson.toJson(encode(accountList));

            if (DEBUG) {
                MyLog.i(CLS_NAME, "saveSaiyAccountList: gsonString: " + gsonString);
            }

            SPH.setSaiyAccounts(ctx, gsonString);

        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "saveSaiyAccountList empty");
            }
            SPH.setSaiyAccounts(ctx, null);
        }

        return true;
    }

    /**
     * Update the enrollment status of a profile
     *
     * @param ctx             the application context
     * @param operationStatus the {@link OperationStatus} object
     * @param profileId       of the user
     * @return true if the profile was successfully updated, false otherwise
     */
    public static boolean updateEnrollmentStatus(@NonNull final Context ctx,
                                                 @Nullable final OperationStatus operationStatus,
                                                 @NonNull final String profileId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "updateEnrollmentStatus");
        }

        if (operationStatus == null || operationStatus.getProcessingResult() == null) {
            return false;
        }

        if (haveAccounts(ctx)) {

            boolean save = false;
            final SaiyAccountList accountList = getAccountList(ctx);

            for (final SaiyAccount account : accountList.getSaiyAccountList()) {

                if (account.getProfileItem() != null) {

                    final ProfileItem item = account.getProfileItem();
                    final String id = item.getId();

                    if (UtilsString.notNaked(id)) {
                        if (id.matches(Pattern.quote(profileId))) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "updateEnrollmentStatus: have match");
                            }

                            item.setCreated(operationStatus.getCreated());
                            item.setLastAction(operationStatus.getLastAction());
                            item.setRemainingSpeechTime(operationStatus.getProcessingResult().getRemainingSpeechTime());
                            item.setSpeechTime(operationStatus.getProcessingResult().getSpeechTime());
                            item.setStatus(operationStatus.getStatus());

                            save = true;
                            break;
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "updateEnrollmentStatus: id naked");
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "updateEnrollmentStatus: getProfileItem null");
                    }
                }
            }

            if (save) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "updateEnrollmentStatus: saving");
                }
                saveSaiyAccountList(ctx, accountList);

                if (DEBUG) {
                    debugAccountList(ctx);
                }

                return true;
            }

        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "updateEnrollmentStatus: no account");
            }
        }

        return false;
    }

    /**
     * Verbosely debug all accounts
     *
     * @param ctx the application context
     */
    private static void debugAccountList(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "debugAccountList");

            if (haveAccounts(ctx)) {

                final SaiyAccountList accountList = getAccountList(ctx);

                for (final SaiyAccount account : accountList.getSaiyAccountList()) {

                    final ProfileItem item = account.getProfileItem();

                    MyLog.i(CLS_NAME, "debugAccountList getAccountId: " + account.getAccountId());
                    MyLog.i(CLS_NAME, "debugAccountList getAccountName: " + account.getAccountName());
                    MyLog.i(CLS_NAME, "debugAccountList getPseudonym: " + account.getPseudonym());

                    if (item != null) {
                        MyLog.i(CLS_NAME, "debugAccountList getId: " + item.getId());
                        MyLog.i(CLS_NAME, "debugAccountList getStatus: " + item.getStatus());
                        MyLog.i(CLS_NAME, "debugAccountList getSpeechTime: " + item.getSpeechTime());
                        MyLog.i(CLS_NAME, "debugAccountList getRemainingSpeechTime: " + item.getRemainingSpeechTime());
                        MyLog.i(CLS_NAME, "debugAccountList getLastAction: " + item.getLastAction());
                        MyLog.i(CLS_NAME, "debugAccountList getCreated: " + item.getCreated());
                    } else {
                        MyLog.w(CLS_NAME, "debugAccountList: getProfileItem null");
                    }
                }
            } else {
                MyLog.w(CLS_NAME, "debugAccountList: no account");
            }
        }
    }


    /**
     * Check if we already have an existing Saiy account saved.
     *
     * @param ctx         the application context
     * @param accountName the name of the account to check
     * @param accountId   the account id
     * @return true if the account exist, false otherwise
     */
    public static boolean accountExists(@NonNull final Context ctx, @Nullable final String accountName,
                                        @Nullable final String accountId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "accountExists");
        }

        if (haveAccounts(ctx)) {

            final SaiyAccountList accountList = getAccountList(ctx);

            for (final SaiyAccount account : accountList.getSaiyAccountList()) {

                if (accountName != null && accountId != null) {
                    if (account.getAccountName().matches(Pattern.quote(accountName))
                            || (UtilsString.notNaked(account.getAccountId())
                            && account.getAccountId().matches(Pattern.quote(accountId)))) {
                        return true;
                    }
                } else if (accountName != null) {
                    if (account.getAccountName().matches(Pattern.quote(accountName))) {
                        return true;
                    }
                } else {
                    if (accountId != null && UtilsString.notNaked(account.getAccountId())) {
                        if (account.getAccountId().matches(Pattern.quote(accountId))) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Delete a current {@link SaiyAccount}
     *
     * @param ctx         the application context
     * @param accountName the account name
     * @param accountId   the account id
     */
    public static boolean deleteAccount(@NonNull final Context ctx, @Nullable final String accountName,
                                        @Nullable final String accountId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "deleteAccount");
        }

        if (haveAccounts(ctx)) {

            final SaiyAccountList saiyAccountList = getAccountList(ctx);
            final List<SaiyAccount> accountList = saiyAccountList.getSaiyAccountList();

            if (DEBUG) {
                MyLog.i(CLS_NAME, "deleteAccount have: " + accountList.size());
            }

            final ListIterator<SaiyAccount> itr = accountList.listIterator();

            SaiyAccount account;
            ProfileItem profileItem;
            String profileId;
            while (itr.hasNext()) {
                account = itr.next();

                if (accountName != null && accountId != null) {
                    if (account.getAccountName().matches(Pattern.quote(accountName))
                            || (UtilsString.notNaked(account.getAccountId())
                            && account.getAccountId().matches(Pattern.quote(accountId)))) {

                        if (account.getProfileItem() != null) {
                            profileItem = account.getProfileItem();
                            profileId = profileItem.getId();
                            removeProfile(ctx, profileId);
                        }

                        itr.remove();
                    }
                } else if (accountName != null) {
                    if (account.getAccountName().matches(Pattern.quote(accountName))) {

                        if (account.getProfileItem() != null) {
                            profileItem = account.getProfileItem();
                            profileId = profileItem.getId();
                            removeProfile(ctx, profileId);
                        }

                        itr.remove();
                    }
                } else {
                    if (accountId != null && UtilsString.notNaked(account.getAccountId())) {
                        if (account.getAccountId().matches(Pattern.quote(accountId))) {

                            if (account.getProfileItem() != null) {
                                profileItem = account.getProfileItem();
                                profileId = profileItem.getId();
                                removeProfile(ctx, profileId);
                            }

                            itr.remove();
                        }
                    }
                }
            }

            if (DEBUG) {
                MyLog.i(CLS_NAME, "deleteAccount post have: " + accountList.size());
            }

            return saveSaiyAccountList(ctx, new SaiyAccountList(accountList));
        }

        return true;
    }

    /**
     * Asynchronously remove any remote profile. If for some reason there are multiple profiles,
     * the requests will queue without blocking the parent loop.
     *
     * @param ctx       the application context
     * @param profileId the remote profile id
     */
    private static void removeProfile(@NonNull final Context ctx, @Nullable final String profileId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "removeProfile: " + profileId);
        }

        if (UtilsString.notNaked(profileId)) {

            new Thread() {
                public void run() {

                    new DeleteIDProfile(ctx, MicrosoftConfiguration.OCP_APIM_KEY_1,
                            profileId).delete();

                }
            }.start();
        }
    }


    /**
     * As we are storing the user's email address, we'll do it in Base64, just because. If a rouge
     * application wanted the user's email address, there are easier ways than hacking into our
     * shared preferences...
     *
     * @param accountList the {@link SaiyAccountList} object
     * @return the updated {@link SaiyAccountList}
     */
    private static SaiyAccountList encode(@NonNull final SaiyAccountList accountList) {

        try {

            for (final SaiyAccount account : accountList.getSaiyAccountList()) {
                account.setAccountName(Base64.encodeToString(account.getAccountName().getBytes(
                        Constants.ENCODING_UTF8), Base64.NO_WRAP));
            }

        } catch (final UnsupportedEncodingException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "encode UnsupportedEncodingException");
                e.printStackTrace();
            }
        }

        return accountList;
    }

    /**
     * Decode the user's email address from Base64
     *
     * @param accountList the {@link SaiyAccountList} object
     * @return the updated {@link SaiyAccountList}
     */
    private static SaiyAccountList decode(@NonNull final SaiyAccountList accountList) {

        try {

            for (final SaiyAccount account : accountList.getSaiyAccountList()) {
                account.setAccountName(new String(Base64.decode(account.getAccountName(), Base64.NO_WRAP),
                        Constants.ENCODING_UTF8));
            }

        } catch (final UnsupportedEncodingException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "encode UnsupportedEncodingException");
                e.printStackTrace();
            }
        }

        return accountList;
    }
}
