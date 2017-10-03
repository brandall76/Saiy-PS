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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.recognition.provider.android.RecognitionNative;
import ai.saiy.android.tts.attributes.Gender;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.components.DividerItemDecoration;
import ai.saiy.android.ui.components.UIMainAdapter;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.FragmentAdvancedSettings;
import ai.saiy.android.ui.fragment.FragmentHome;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

/**
 * Utility class to assist its parent fragment and avoid clutter there
 * <p>
 * Created by benrandall76@gmail.com on 25/07/2016.
 */

public class FragmentAdvancedSettingsHelper {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentAdvancedSettingsHelper.class.getSimpleName();

    private final FragmentAdvancedSettings parentFragment;

    /**
     * Constructor
     *
     * @param parentFragment the parent fragment for this helper class
     */
    public FragmentAdvancedSettingsHelper(@NonNull final FragmentAdvancedSettings parentFragment) {
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
        containerUI.setTitle(getString(R.string.menu_toast));
        containerUI.setSubtitle(getString(R.string.menu_tap_toggle));
        containerUI.setIconMain(R.drawable.ic_comment_alert_outline);
        containerUI.setIconExtra(SPH.getToastUnknown(getApplicationContext())
                ? FragmentHome.CHECKED : FragmentHome.UNCHECKED);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_haptic));
        containerUI.setSubtitle(getString(R.string.menu_tap_toggle));
        containerUI.setIconMain(R.drawable.ic_vibration);
        containerUI.setIconExtra(SPH.getVibrateCondition(getApplicationContext())
                ? FragmentHome.CHECKED : FragmentHome.UNCHECKED);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_offline));
        containerUI.setSubtitle(getString(R.string.menu_tap_toggle));
        containerUI.setIconMain(R.drawable.ic_cloud_outline_off);
        containerUI.setIconExtra(SPH.getUseOffline(getApplicationContext())
                ? FragmentHome.CHECKED : FragmentHome.UNCHECKED);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_hotword_detection));
        containerUI.setSubtitle(getString(R.string.menu_tap_configure));
        containerUI.setIconMain(R.drawable.ic_blur);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_tts_gender));
        containerUI.setSubtitle(getString(R.string.menu_tap_options));
        containerUI.setIconMain(R.drawable.ic_gender_transgender);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_motion));
        containerUI.setSubtitle(getString(R.string.menu_tap_toggle));
        containerUI.setIconMain(R.drawable.ic_bike);
        containerUI.setIconExtra(SPH.getMotionEnabled(getApplicationContext())
                ? FragmentHome.CHECKED : FragmentHome.UNCHECKED);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_pause));
        containerUI.setSubtitle(getString(R.string.menu_tap_set));
        containerUI.setIconMain(R.drawable.ic_pause_octagon_outline);
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

        final RecyclerView mRecyclerView = (RecyclerView) parent.findViewById(R.id.layout_common_fragment_recycler_view);
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
                final ArrayList<ContainerUI> tempArray = FragmentAdvancedSettingsHelper.this.getUIComponents();

                if (FragmentAdvancedSettingsHelper.this.getParentActivity().getDrawer().isDrawerOpen(GravityCompat.START)) {

                    try {
                        Thread.sleep(FragmentHome.DRAWER_CLOSE_DELAY);
                    } catch (final InterruptedException e) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "finaliseUI InterruptedException");
                            e.printStackTrace();
                        }
                    }
                }

                if (FragmentAdvancedSettingsHelper.this.getParent().isActive()) {

                    FragmentAdvancedSettingsHelper.this.getParentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            FragmentAdvancedSettingsHelper.this.getParent().getObjects().addAll(tempArray);
                            FragmentAdvancedSettingsHelper.this.getParent().getAdapter().notifyItemRangeInserted(0, FragmentAdvancedSettingsHelper.this.getParent().getObjects().size());
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
     * Show the gender selector
     */
    @SuppressWarnings("ConstantConditions")
    public void showGenderSelector() {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                final String[] gender = FragmentAdvancedSettingsHelper.this.getParent().getResources().getStringArray(R.array.array_gender);

                for (int i = 0; i < gender.length; i++) {
                    gender[i] = StringUtils.capitalize(gender[i]);
                }

                FragmentAdvancedSettingsHelper.this.getParentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        final MaterialDialog materialDialog = new MaterialDialog.Builder(FragmentAdvancedSettingsHelper.this.getParentActivity())
                                .autoDismiss(false)
                                .alwaysCallSingleChoiceCallback()
                                .title(R.string.menu_tts_gender)
                                .content(R.string.tts_gender_text)
                                .items((CharSequence[]) gender)
                                .positiveText(R.string.menu_select)
                                .negativeText(android.R.string.cancel)
                                .iconRes(R.drawable.ic_gender_transgender)
                                .backgroundColorRes(R.color.colorTint)

                                .itemsCallbackSingleChoice(SPH.getDefaultTTSGender(FragmentAdvancedSettingsHelper.this.getApplicationContext()).ordinal(),
                                        new MaterialDialog.ListCallbackSingleChoice() {
                                            @Override
                                            public boolean onSelection(final MaterialDialog dialog, final View view, final int which, final CharSequence text) {
                                                if (DEBUG) {
                                                    MyLog.i(CLS_NAME, "showGenderSelector: onSelection: " + which + ": " + text);
                                                }
                                                return true;
                                            }
                                        })

                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull final DialogAction which) {

                                        switch (dialog.getSelectedIndex()) {

                                            case 0:
                                                if (DEBUG) {
                                                    MyLog.i(CLS_NAME, "showGenderSelector: onPositive: MALE");
                                                }
                                                SPH.setDefaultTTSGender(FragmentAdvancedSettingsHelper.this.getApplicationContext(), Gender.MALE);
                                                SPH.setDefaultTTSVoice(FragmentAdvancedSettingsHelper.this.getApplicationContext(), null);
                                                break;
                                            case 1:
                                                if (DEBUG) {
                                                    MyLog.i(CLS_NAME, "showGenderSelector: onPositive: FEMALE");
                                                }
                                                SPH.setDefaultTTSGender(FragmentAdvancedSettingsHelper.this.getApplicationContext(), Gender.FEMALE);
                                                SPH.setDefaultTTSVoice(FragmentAdvancedSettingsHelper.this.getApplicationContext(), null);
                                                break;

                                        }
                                        dialog.dismiss();
                                    }
                                })

                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull final DialogAction which) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showGenderSelector: onNegative");
                                        }
                                        dialog.dismiss();
                                    }
                                })

                                .cancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(final DialogInterface dialog) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showGenderSelector: onCancel");
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
     * Show the Hotword selector
     */
    @SuppressWarnings("ConstantConditions")
    public void showHotwordSelector() {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                final String[] hotwordActions = FragmentAdvancedSettingsHelper.this.getParent().getResources().getStringArray(R.array.array_hotword);

                final ArrayList<Integer> selectedList = new ArrayList<>();

                if (SPH.getHotwordBoot(FragmentAdvancedSettingsHelper.this.getApplicationContext())) {
                    selectedList.add(0);
                }

                if (SPH.getHotwordDriving(getApplicationContext())) {
                    selectedList.add(1);
                }

                if (SPH.getHotwordWakelock(getApplicationContext())) {
                    selectedList.add(2);
                }

                if (SPH.getHotwordSecure(getApplicationContext())) {
                    selectedList.add(3);
                }

                FragmentAdvancedSettingsHelper.this.getParentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        final MaterialDialog materialDialog = new MaterialDialog.Builder(FragmentAdvancedSettingsHelper.this.getParentActivity())
                                .autoDismiss(false)
                                .title(R.string.menu_hotword_detection)
                                .content(R.string.hotword_intro_text)
                                .iconRes(R.drawable.ic_blur)
                                .positiveText(R.string.save)
                                .neutralText(R.string.clear)
                                .negativeText(android.R.string.cancel)
                                .items((CharSequence[]) hotwordActions)

                                .itemsCallbackMultiChoice(selectedList.toArray(new Integer[0]), new MaterialDialog.ListCallbackMultiChoice() {
                                    @Override
                                    public boolean onSelection(final MaterialDialog dialog, final Integer[] which, final CharSequence[] text) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showHotwordSelector: onSelection: " + which.length);
                                        }
                                        return true;
                                    }
                                })

                                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull final DialogAction which) {
                                        dialog.clearSelectedIndices();
                                    }
                                })

                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull final DialogAction which) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showHotwordSelector: onPositive");
                                        }

                                        final Integer[] selected = dialog.getSelectedIndices();

                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showHotwordSelector: onPositive: length: " + selected.length);
                                            for (final Integer aSelected : selected) {
                                                MyLog.i(CLS_NAME, "showHotwordSelector: onPositive: " + aSelected);
                                            }
                                        }

                                        SPH.setHotwordBoot(getApplicationContext(),
                                                ArrayUtils.contains(selected, 0));

                                        SPH.setHotwordDriving(getApplicationContext(),
                                                ArrayUtils.contains(selected, 1));

                                        if (SPH.getHotwordDriving(getApplicationContext())) {
                                            SPH.setMotionEnabled(getApplicationContext(), true);
                                        }

                                        SPH.setHotwordWakelock(getApplicationContext(),
                                                ArrayUtils.contains(selected, 2));

                                        SPH.setHotwordSecure(getApplicationContext(),
                                                ArrayUtils.contains(selected, 3));

                                        dialog.dismiss();
                                    }
                                })

                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull final DialogAction which) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showHotwordSelector: onNegative");
                                        }
                                        dialog.dismiss();
                                    }
                                })

                                .cancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(final DialogInterface dialog) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showHotwordSelector: onCancel");
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
    public void showPauseDetectionSlider() {

        final MaterialDialog materialDialog = new MaterialDialog.Builder(getParentActivity())
                .customView(R.layout.pause_detection_dialog_layout, false)
                .autoDismiss(false)
                .title(R.string.menu_pause)
                .iconRes(R.drawable.ic_pause_octagon_outline)
                .positiveText(R.string.save)
                .neutralText(R.string.text_default)
                .negativeText(android.R.string.cancel)

                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull final DialogAction which) {
                        ((SeekBar) dialog.getCustomView().findViewById(R.id.pauseSeekBar))
                                .setProgress((int) (RecognitionNative.PAUSE_TIMEOUT / 1000));
                    }
                })

                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull final DialogAction which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showPauseDetectionSlider: onPositive");
                        }

                        SPH.setPauseTimeout(FragmentAdvancedSettingsHelper.this.getApplicationContext(),
                                (long) ((SeekBar) dialog.getCustomView().findViewById(R.id.pauseSeekBar))
                                        .getProgress() * 1000);
                        dialog.dismiss();
                    }
                })

                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull final DialogAction which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showPauseDetectionSlider: onNegative");
                        }
                        dialog.dismiss();
                    }
                })

                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showPauseDetectionSlider: onCancel");
                        }
                        dialog.dismiss();
                    }
                }).build();

        final int currentTimeout = (int) (SPH.getPauseTimeout(getApplicationContext()) / 1000);
        final TextView seekText = (TextView) materialDialog.getCustomView().findViewById(R.id.pauseSeekBarText);

        switch (currentTimeout) {
            case 0:
                seekText.setText(getString(R.string.pause_detection_text)
                        + " " + getString(R.string.provider_default));
                break;
            case 1:
                seekText.setText(getString(R.string.pause_detection_text)
                        + " " + currentTimeout + " " + getString(R.string.second));
                break;
            default:
                seekText.setText(getString(R.string.pause_detection_text)
                        + " " + currentTimeout + " " + getString(R.string.seconds));
                break;
        }

        final SeekBar seekbar = (SeekBar) materialDialog.getCustomView().findViewById(R.id.pauseSeekBar);
        seekbar.setProgress(currentTimeout);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {

                switch (progress) {
                    case 0:
                        seekText.setText(getString(R.string.pause_detection_text)
                                + " " + getString(R.string.provider_default));
                        break;
                    case 1:
                        seekText.setText(getString(R.string.pause_detection_text)
                                + " " + progress + " " + getString(R.string.second));
                        break;
                    default:
                        seekText.setText(getString(R.string.pause_detection_text)
                                + " " + progress + " " + getString(R.string.seconds));
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

    private String getString(final int id) {
        return getApplicationContext().getString(id);
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
    public FragmentAdvancedSettings getParent() {
        return parentFragment;
    }
}
