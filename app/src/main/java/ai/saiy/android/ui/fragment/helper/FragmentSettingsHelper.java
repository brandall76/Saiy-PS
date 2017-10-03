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

package ai.saiy.android.ui.fragment.helper;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.applications.Installed;
import ai.saiy.android.command.battery.BatteryInformation;
import ai.saiy.android.command.unknown.Unknown;
import ai.saiy.android.thirdparty.tasker.TaskerHelper;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.components.DividerItemDecoration;
import ai.saiy.android.ui.components.UIMainAdapter;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.FragmentHome;
import ai.saiy.android.ui.fragment.FragmentSettings;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

/**
 * Utility class to assist its parent fragment and avoid clutter there
 * <p>
 * Created by benrandall76@gmail.com on 25/07/2016.
 */
public class FragmentSettingsHelper {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentSettingsHelper.class.getSimpleName();

    private final FragmentSettings parentFragment;

    /**
     * Constructor
     *
     * @param parentFragment the parent fragment for this helper class
     */
    public FragmentSettingsHelper(@NonNull final FragmentSettings parentFragment) {
        this.parentFragment = parentFragment;
    }

    /**
     * Get the components for this fragment
     *
     * @return a list of {@link ContainerUI} elements
     */
    private ArrayList<ContainerUI> getUIComponents() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getUIComponents");
        }

        final ArrayList<ContainerUI> mObjects = new ArrayList<>();

        ContainerUI containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_language));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_options));
        containerUI.setIconMain(R.drawable.ic_language);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_unknown_commands));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_options));
        containerUI.setIconMain(R.drawable.ic_not_equal);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_volume_settings));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_set));
        containerUI.setIconMain(R.drawable.ic_volume_high);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_synthesised_voice));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_toggle));
        containerUI.setIconMain(R.drawable.ic_google);
        containerUI.setIconExtra(SPH.getNetworkSynthesis(getApplicationContext())
                ? FragmentHome.CHECKED : FragmentHome.UNCHECKED);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_temperature_units));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_options));
        containerUI.setIconMain(R.drawable.ic_thermometer);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_default_apps));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_set));
        containerUI.setIconMain(R.drawable.ic_apps);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        return mObjects;
    }

    /**
     * Get the recycler view for this fragment
     *
     * @param parent the view parent
     * @return the {@link RecyclerView}
     */
    public RecyclerView getRecyclerView(@NonNull final View parent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getRecyclerView");
        }

        final RecyclerView mRecyclerView = (RecyclerView)
                parent.findViewById(R.id.layout_common_fragment_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getParentActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getParentActivity(), null));

        return mRecyclerView;
    }

    /**
     * Get the adapter for this fragment
     *
     * @param mObjects list of {@link ContainerUI} elements
     * @return the {@link UIMainAdapter}
     */
    public UIMainAdapter getAdapter(@NonNull final ArrayList<ContainerUI> mObjects) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getAdapter");
        }
        return new UIMainAdapter(mObjects, getParent(), getParent());
    }

    /**
     * Update the parent fragment with the UI components
     */
    public void finaliseUI() {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final ArrayList<ContainerUI> tempArray = FragmentSettingsHelper.this.getUIComponents();

                if (FragmentSettingsHelper.this.getParentActivity().getDrawer().isDrawerOpen(GravityCompat.START)) {

                    try {
                        Thread.sleep(FragmentHome.DRAWER_CLOSE_DELAY);
                    } catch (final InterruptedException e) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "finaliseUI InterruptedException");
                            e.printStackTrace();
                        }
                    }
                }

                if (FragmentSettingsHelper.this.getParent().isActive()) {

                    FragmentSettingsHelper.this.getParentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            FragmentSettingsHelper.this.getParent().getObjects().addAll(tempArray);
                            FragmentSettingsHelper.this.getParent().getAdapter().notifyItemRangeInserted(0, FragmentSettingsHelper.this.getParent().getObjects().size());
                        }
                    });

                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "finaliseUI Fragment detached");
                    }
                }
            }
        });
    }

    /**
     * Show the temperature units selector
     */
    @SuppressWarnings("ConstantConditions")
    public void showTemperatureUnitsSelector() {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                final String[] units = FragmentSettingsHelper.this.getParent().getResources().getStringArray(R.array.array_temperature_units);

                for (int i = 0; i < units.length; i++) {
                    units[i] = StringUtils.capitalize(units[i]);
                }

                FragmentSettingsHelper.this.getParentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        final MaterialDialog materialDialog = new MaterialDialog.Builder(FragmentSettingsHelper.this.getParentActivity())
                                .autoDismiss(false)
                                .alwaysCallSingleChoiceCallback()
                                .title(R.string.menu_temperature_units)
                                .items((CharSequence[]) units)
                                .positiveText(R.string.menu_select)
                                .negativeText(android.R.string.cancel)
                                .iconRes(R.drawable.ic_thermometer)
                                .backgroundColorRes(R.color.colorTint)

                                .itemsCallbackSingleChoice(SPH.getDefaultTemperatureUnits(FragmentSettingsHelper.this.getApplicationContext()),
                                        new MaterialDialog.ListCallbackSingleChoice() {
                                            @Override
                                            public boolean onSelection(final MaterialDialog dialog, final View view, final int which, final CharSequence text) {
                                                if (DEBUG) {
                                                    MyLog.i(CLS_NAME, "showTemperatureUnitsSelector: onSelection: " + which + ": " + text);
                                                }
                                                return true;
                                            }
                                        })

                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull final DialogAction which) {

                                        switch (dialog.getSelectedIndex()) {

                                            case BatteryInformation.CELSIUS:
                                                if (DEBUG) {
                                                    MyLog.i(CLS_NAME, "showTemperatureUnitsSelector: onPositive: CELSIUS");
                                                }
                                                SPH.setDefaultTemperatureUnits(FragmentSettingsHelper.this.getApplicationContext(),
                                                        BatteryInformation.CELSIUS);
                                                break;
                                            case BatteryInformation.FAHRENHEIT:
                                                if (DEBUG) {
                                                    MyLog.i(CLS_NAME, "showTemperatureUnitsSelector: onPositive: FAHRENHEIT");
                                                }
                                                SPH.setDefaultTemperatureUnits(FragmentSettingsHelper.this.getApplicationContext(),
                                                        BatteryInformation.FAHRENHEIT);
                                                break;

                                        }
                                        dialog.dismiss();
                                    }
                                })

                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull final DialogAction which) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showTemperatureUnitsSelector: onNegative");
                                        }
                                        dialog.dismiss();
                                    }
                                })

                                .cancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(final DialogInterface dialog) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showTemperatureUnitsSelector: onCancel");
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

    /**
     * Show the unknown command action selector
     */
    @SuppressWarnings("ConstantConditions")
    public void showUnknownCommandSelector() {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                final String[] actions = FragmentSettingsHelper.this.getParent().getResources().getStringArray(R.array.array_unknown_action);

                for (int i = 0; i < actions.length; i++) {

                    switch (i) {

                    case Unknown.UNKNOWN_STATE:
                        break;
                    case Unknown.UNKNOWN_REPEAT:
                        break;
                    case Unknown.UNKNOWN_GOOGLE_SEARCH:
                        actions[i] = getParent().getString(R.string.menu_send_to) + " " + actions[i];
                        break;
                    case Unknown.UNKNOWN_WOLFRAM_ALPHA:
                        actions[i] = getParent().getString(R.string.menu_send_to) + " " + actions[i];
                        break;
                    case Unknown.UNKNOWN_TASKER:
                        actions[i] = getParent().getString(R.string.menu_send_to) + " " + actions[i];
                        break;
                }
            }

                final ArrayList<Integer> disabledIndicesList = new ArrayList<>();

                if (!Installed.isPackageInstalled(FragmentSettingsHelper.this.getApplicationContext(),
                        Installed.PACKAGE_WOLFRAM_ALPHA)) {
                    disabledIndicesList.add(Unknown.UNKNOWN_WOLFRAM_ALPHA);
                }

                if (!new TaskerHelper().isTaskerInstalled(FragmentSettingsHelper.this.getApplicationContext()).first) {
                    disabledIndicesList.add(Unknown.UNKNOWN_TASKER);
                }

                FragmentSettingsHelper.this.getParentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                final MaterialDialog materialDialog = new MaterialDialog.Builder(getParentActivity())
                        .autoDismiss(false)
                        .alwaysCallSingleChoiceCallback()
                        .title(R.string.menu_unknown_commands)
                        .items((CharSequence[]) actions)
                        .itemsDisabledIndices(disabledIndicesList.toArray(new Integer[0]))
                        .content(R.string.content_unknown_command)
                        .positiveText(R.string.menu_select)
                        .negativeText(android.R.string.cancel)
                        .iconRes(R.drawable.ic_not_equal)
                        .backgroundColorRes(R.color.colorTint)

                                .itemsCallbackSingleChoice(SPH.getCommandUnknownAction(FragmentSettingsHelper.this.getApplicationContext()),
                                        new MaterialDialog.ListCallbackSingleChoice() {
                                            @Override
                                            public boolean onSelection(final MaterialDialog dialog, final View view, final int which, final CharSequence text) {
                                                if (DEBUG) {
                                                    MyLog.i(CLS_NAME, "showUnknownCommandSelector: onSelection: " + which + ": " + text);
                                                }
                                                return true;
                                            }
                                        })

                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull final DialogAction which) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showUnknownCommandSelector: onPositive: " + dialog.getSelectedIndex());
                                        }

                                        SPH.setCommandUnknownAction(FragmentSettingsHelper.this.getApplicationContext(),
                                                dialog.getSelectedIndex());
                                        dialog.dismiss();
                                    }
                                })

                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull final DialogAction which) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showUnknownCommandSelector: onNegative");
                                        }
                                        dialog.dismiss();
                                    }
                                })

                                .cancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(final DialogInterface dialog) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showUnknownCommandSelector: onCancel");
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

    /**
     * Show the pause detection slider
     */
    @SuppressWarnings("ConstantConditions")
    public void showVolumeSettingsSlider() {

        final MaterialDialog materialDialog = new MaterialDialog.Builder(getParentActivity())
                .customView(R.layout.tts_volume_dialog_layout, false)
                .autoDismiss(false)
                .title(R.string.menu_volume_settings)
                .iconRes(R.drawable.ic_pause_octagon_outline)
                .positiveText(R.string.save)
                .neutralText(R.string.text_default)
                .negativeText(android.R.string.cancel)

                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull final DialogAction which) {
                        ((SeekBar) dialog.getCustomView().findViewById(R.id.volumeSeekBar))
                                .setProgress(4);
                    }
                })

                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull final DialogAction which) {

                    final int volume = ((SeekBar) dialog.getCustomView().findViewById(R.id.volumeSeekBar))
                            .getProgress() * 10 - 40;

                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "showVolumeSettingsSlider: onPositive: setting: " + volume);
                    }

                    SPH.setTTSVolume(getApplicationContext(), volume);

                        dialog.dismiss();
                    }
                })

                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull final DialogAction which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showVolumeSettingsSlider: onNegative");
                        }
                        dialog.dismiss();
                    }
                })

                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showVolumeSettingsSlider: onCancel");
                        }
                        dialog.dismiss();
                    }
                }).build();

        final int userVolume = SPH.getTTSVolume(getApplicationContext());
        final TextView seekText = (TextView) materialDialog.getCustomView().findViewById(R.id.volumeSeekBarText);
        final SeekBar seekbar = (SeekBar) materialDialog.getCustomView().findViewById(R.id.volumeSeekBar);

        switch (userVolume) {
            case -40:
                seekText.setText("40% " + getParent().getString(R.string.below) + " "
                        + getParent().getString(R.string.media_stream));
                seekbar.setProgress(0);
                break;
            case -30:
                seekText.setText("30% " + getParent().getString(R.string.below) + " "
                        + getParent().getString(R.string.media_stream));
                seekbar.setProgress(1);
                break;
            case -20:
                seekText.setText("20% " + getParent().getString(R.string.below) + " "
                        + getParent().getString(R.string.media_stream));
                seekbar.setProgress(2);
                break;
            case -10:
                seekText.setText("10% " + getParent().getString(R.string.below) + " "
                        + getParent().getString(R.string.media_stream));
                seekbar.setProgress(3);
                break;
            case 0:
                seekText.setText(StringUtils.capitalize(getParent().getString(R.string.adhere_to)) + " "
                        + getParent().getString(R.string.media_stream));
                seekbar.setProgress(4);
                break;
            case 10:
                seekText.setText("10% " + getParent().getString(R.string.above) + " "
                        + getParent().getString(R.string.media_stream));
                seekbar.setProgress(5);
                break;
            case 20:
                seekText.setText("20% " + getParent().getString(R.string.above) + " "
                        + getParent().getString(R.string.media_stream));
                seekbar.setProgress(6);
                break;
            case 30:
                seekText.setText("30% " + getParent().getString(R.string.above) + " "
                        + getParent().getString(R.string.media_stream));
                seekbar.setProgress(7);
                break;
            case 40:
                seekText.setText("40% " + getParent().getString(R.string.above) + " "
                        + getParent().getString(R.string.media_stream));
                seekbar.setProgress(8);
        }

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {

                switch (progress) {
                    case 0:
                        seekText.setText("40% " + getParent().getString(R.string.below) + " "
                                + getParent().getString(R.string.media_stream));
                        break;
                    case 1:
                        seekText.setText("30% " + getParent().getString(R.string.below) + " "
                                + getParent().getString(R.string.media_stream));
                        break;
                    case 2:
                        seekText.setText("20% " + getParent().getString(R.string.below) + " "
                                + getParent().getString(R.string.media_stream));
                        break;
                    case 3:
                        seekText.setText("10% " + getParent().getString(R.string.below) + " "
                                + getParent().getString(R.string.media_stream));
                        break;
                    case 4:
                        seekText.setText(StringUtils.capitalize(getParent().getString(R.string.adhere_to))
                                + " " + getParent().getString(R.string.media_stream));
                        break;
                    case 5:
                        seekText.setText("10% " + getParent().getString(R.string.above) + " "
                                + getParent().getString(R.string.media_stream));
                        break;
                    case 6:
                        seekText.setText("20% " + getParent().getString(R.string.above) + " "
                                + getParent().getString(R.string.media_stream));
                        break;
                    case 7:
                        seekText.setText("30% " + getParent().getString(R.string.above) + " "
                                + getParent().getString(R.string.media_stream));
                        break;
                    case 8:
                        seekText.setText("40% " + getParent().getString(R.string.above) + " "
                                + getParent().getString(R.string.media_stream));
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
            }
        });

        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();
    }

    /**
     * Utility method to ensure we double check the context being used.
     *
     * @return the application context
     */
    private Context getApplicationContext() {
        return parentFragment.getApplicationContext();
    }

    /**
     * Utility to return the parent activity neatly cast. No need for instanceOf as this
     * fragment helper will never be attached to another activity.
     *
     * @return the {@link ActivityHome} parent
     */

    public ActivityHome getParentActivity() {
        return parentFragment.getParentActivity();
    }

    /**
     * Utility method to return the parent fragment this helper is helping.
     *
     * @return the parent fragment
     */
    public FragmentSettings getParent() {
        return parentFragment;
    }
}
