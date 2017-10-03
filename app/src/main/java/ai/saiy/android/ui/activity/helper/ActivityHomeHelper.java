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

package ai.saiy.android.ui.activity.helper;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.apache.commons.lang3.StringUtils;

import ai.saiy.android.R;
import ai.saiy.android.intent.ExecuteIntent;
import ai.saiy.android.service.helper.SelfAwareHelper;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

/**
 * Created by benrandall76@gmail.com on 26/08/2016.
 */

public class ActivityHomeHelper {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ActivityHomeHelper.class.getSimpleName();

    /**
     * Show the applications disclaimer
     *
     * @param act the Activity context in which to display the dialog
     */
    @SuppressWarnings("deprecation, ConstantConditions")
    public void showDisclaimer(@NonNull final Activity act) {

        final MaterialDialog materialDialog = new MaterialDialog.Builder(act)
                .title(R.string.menu_application_disclaimer)
                .content(Html.fromHtml(act.getApplicationContext().getString(R.string.content_disclaimer)))
                .positiveText(R.string.menu_accept)
                .negativeText(R.string.menu_uninstall)
                .iconRes(R.drawable.ic_gavel)
                .autoDismiss(false)
                .canceledOnTouchOutside(false)
                .backgroundColorRes(R.color.colorTint)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull final DialogAction which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showDisclaimer: onPositive");
                        }

                        SPH.setAcceptedDisclaimer(act.getApplicationContext());
                        dialog.dismiss();
                        ((ActivityHome) act).runStartConfiguration();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull final DialogAction which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showDisclaimer: onNegative");
                        }

                        SelfAwareHelper.stopService(act.getApplicationContext());
                        ExecuteIntent.uninstallApp(act.getApplicationContext(), act.getPackageName());
                        dialog.dismiss();
                        act.finish();
                    }
                })
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showDisclaimer: onCancel");
                        }

                        SelfAwareHelper.stopService(act.getApplicationContext());
                        ExecuteIntent.uninstallApp(act.getApplicationContext(), act.getPackageName());
                        dialog.dismiss();
                        act.finish();
                    }
                }).build();

        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();
    }

    /**
     * Show the developer note
     *
     * @param act the Activity context in which to display the dialog
     */
    @SuppressWarnings("ConstantConditions")
    public void showDeveloperNote(@NonNull final Activity act) {

        final MaterialDialog materialDialog = new MaterialDialog.Builder(act)
                .title(R.string.menu_developer_note)
                .content(R.string.content_developer_note)
                .positiveText(R.string.menu_lets_do_it)
                .iconRes(R.drawable.ic_note_text)
                .backgroundColorRes(R.color.colorTint)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull final DialogAction which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showDeveloperNote: onPositive");
                        }

                        SPH.setDeveloperNote(act.getApplicationContext());
                        dialog.dismiss();
                        ((ActivityHome) act).runStartConfiguration();
                    }
                })
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showDeveloperNote: onCancel");
                        }

                        SPH.setDeveloperNote(act.getApplicationContext());
                        dialog.dismiss();
                        ((ActivityHome) act).runStartConfiguration();
                    }
                }).build();

        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_right;
        materialDialog.show();
    }

    /**
     * Show the what's new information
     *
     * @param act the Activity context in which to display the dialog
     */
    @SuppressWarnings("ConstantConditions")
    public void showWhatsNew(@NonNull final Activity act) {

        final MaterialDialog materialDialog = new MaterialDialog.Builder(act)
                .title(R.string.menu_whats_new)
                .content(R.string.content_whats_new)
                .positiveText(R.string.menu_excited)
                .iconRes(R.drawable.ic_info)
                .backgroundColorRes(R.color.colorTint)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull final DialogAction which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showWhatsNew: onPositive");
                        }

                        SPH.setWhatsNew(act.getApplicationContext());
                        dialog.dismiss();
                        ((ActivityHome) act).runStartConfiguration();
                    }
                })
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showWhatsNew: onCancel");
                        }

                        SPH.setWhatsNew(act.getApplicationContext());
                        dialog.dismiss();
                        ((ActivityHome) act).runStartConfiguration();
                    }
                }).build();

        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();
    }

    /**
     * Show the supported language selector
     *
     * @param act the Activity context in which to display the dialog
     */
    @SuppressWarnings("ConstantConditions")
    public void showLanguageSelector(@NonNull final Activity act) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                final String[] languages = act.getResources().getStringArray(R.array.array_supported_languages);

                for (int i = 0; i < languages.length; i++) {
                    languages[i] = StringUtils.capitalize(languages[i]);
                }

                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        final MaterialDialog materialDialog = new MaterialDialog.Builder(act)
                                .autoDismiss(false)
                                .alwaysCallSingleChoiceCallback()
                                .title(R.string.menu_supported_languages)
                                .items((CharSequence[]) languages)
                                .itemsDisabledIndices(1, 2, 3, 4, 5, 6, 7)
                                .content(R.string.content_supported_languages)
                                .positiveText(R.string.menu_select)
                                .iconRes(R.drawable.ic_language)
                                .backgroundColorRes(R.color.colorTint)
                                .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                                    @Override
                                    public boolean onSelection(final MaterialDialog dialog, final View view, final int which, final CharSequence text) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showLanguageSelector: onSelection: " + which + ": " + text);
                                        }
                                        return true;
                                    }
                                })
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull final DialogAction which) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showLanguageSelector: onPositive: " + dialog.getSelectedIndex());
                                        }

                                        dialog.dismiss();
                                    }
                                })
                                .cancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(final DialogInterface dialog) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showLanguageSelector: onCancel");
                                        }

                                        dialog.dismiss();
                                    }
                                }).build();

                        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
                        materialDialog.show();
                    }
                });
            }
        });
    }
}
