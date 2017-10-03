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

package ai.saiy.android.command.clipboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.util.Pair;

import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

/**
 * Helper class to query and place items on the clipboard. Static implementation for ease of access.
 * <p/>
 * Created by benrandall76@gmail.com on 10/02/2016.
 */
public final class ClipboardHelper {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = ClipboardHelper.class.getSimpleName();

    private static String clipboardContent;

    public static String getClipboardContent() {
        return clipboardContent;
    }

    /**
     * Prevent instantiation
     */
    public ClipboardHelper() {
        throw new IllegalArgumentException(Resources.getSystem().getString(android.R.string.no));
    }

    @MainThread
    public static void saveClipboardContent(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "saveClipboardContent");
        }

        clipboardContent = null;

        try {

            final ClipboardManager clipboard = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);

            if (clipboard.getPrimaryClip() != null && clipboard.getPrimaryClip().getItemAt(0) != null
                    && !clipboard.getPrimaryClip().getItemAt(0).getText().toString().trim().isEmpty()) {

                clipboardContent = clipboard.getPrimaryClip().getItemAt(0).getText().toString();

            } else {
                clipboardContent = null;
            }


        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "saveClipboardContent NullPointerException");
                e.printStackTrace();
            }
            clipboardContent = null;
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "saveClipboardContent Exception");
                e.printStackTrace();
            }
            clipboardContent = null;
        }

        if (DEBUG) {
            MyLog.i(CLS_NAME, "saveClipboardContent complete");
        }
    }

    /**
     * Static method to set clipboard content.
     *
     * @param ctx     the application context
     * @param content the content to add to the clipboard
     * @return true if the content was successfully set
     */
    public static boolean setClipboardContent(@NonNull final Context ctx, @NonNull final String content) {

        // Can only be called from UI thread

        try {

            final ClipboardManager clipboard = (ClipboardManager) ctx
                    .getSystemService(Context.CLIPBOARD_SERVICE);

            final ClipData clip = ClipData.newPlainText(Constants.SAIY, content);
            clipboard.setPrimaryClip(clip);

            if (clipboard.getPrimaryClip() != null && clipboard.getPrimaryClip().getItemAt(0) != null
                    && clipboard.getPrimaryClip().getItemAt(0).getText().toString().matches(content)) {
                return true;
            }


        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "setClipboardContent NullPointerException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "setClipboardContent Exception");
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Static method to get the primary clipboard content.
     *
     * @param ctx the application context
     * @return the clipboard content or error text
     */
    @MainThread
    public static String getClipboardContent(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {

        try {

            final ClipboardManager clipboard = (ClipboardManager) ctx
                    .getSystemService(Context.CLIPBOARD_SERVICE);

            if (clipboard.getPrimaryClip() != null && clipboard.getPrimaryClip().getItemAt(0) != null
                    && !clipboard.getPrimaryClip().getItemAt(0).getText().toString().trim().isEmpty()) {

                return clipboard.getPrimaryClip().getItemAt(0).getText().toString();

            } else {

                return PersonalityResponse.getClipboardDataError(ctx, sl);
            }


        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getClipboardContent NullPointerException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getClipboardContent Exception");
                e.printStackTrace();
            }
        }

        return PersonalityResponse.getClipboardAccessError(ctx, sl);
    }

    /**
     * Static method to get the primary clipboard content, or the relevant error string.
     *
     * @param ctx the application context
     * @return the clipboard content or error text
     */
    @MainThread
    public static Pair<Boolean, String> getClipboardContentPair(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {

        final String content = getClipboardContent();

        if (UtilsString.notNaked(content)) {
            return new Pair<>(true, content);
        } else {
            return new Pair<>(false, PersonalityResponse.getClipboardDataError(ctx, sl));
        }
    }

    /**
     * Static method to check if the clipboard has primary content.
     *
     * @param ctx the application context
     * @return true if there is content
     */
    public static boolean clipboardHasContent(@NonNull final Context ctx) {

        try {

            final ClipboardManager clipboard = (ClipboardManager) ctx
                    .getSystemService(Context.CLIPBOARD_SERVICE);

            return clipboard.getPrimaryClip() != null && clipboard.getPrimaryClip().getItemAt(0) != null
                    && !clipboard.getPrimaryClip().getItemAt(0).getText().toString().isEmpty();


        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "clipboardHasContent NullPointerException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "clipboardHasContent Exception");
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * See if the command required the translation to be from the current clipboard content
     *
     * @param utterance the utterance
     * @return true if the translation is required from the clipboard content. False otherwise
     */
    public static boolean isClipboard(@NonNull final Context ctx, @NonNull final String utterance) {
        return utterance.contains(ctx.getString(ai.saiy.android.R.string.clip_board))
                || utterance.contains(ctx.getString(ai.saiy.android.R.string.clipboard));
    }
}
