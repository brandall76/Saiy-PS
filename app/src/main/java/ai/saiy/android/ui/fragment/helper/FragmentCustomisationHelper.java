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
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.components.DividerItemDecoration;
import ai.saiy.android.ui.components.UIMainAdapter;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.FragmentCustomisation;
import ai.saiy.android.ui.fragment.FragmentHome;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsString;

/**
 * Utility class to assist its parent fragment and avoid clutter there
 * <p>
 * Created by benrandall76@gmail.com on 25/07/2016.
 */

public class FragmentCustomisationHelper {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentCustomisationHelper.class.getSimpleName();

    private final FragmentCustomisation parentFragment;

    /**
     * Constructor
     *
     * @param parentFragment the parent fragment for this helper class
     */
    public FragmentCustomisationHelper(@NonNull final FragmentCustomisation parentFragment) {
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
        final int chevronResource = R.drawable.chevron;

        ContainerUI containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_custom_intro));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_configure));
        containerUI.setIconMain(R.drawable.ic_crown);
        containerUI.setIconExtra(chevronResource);
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
     * Update the parent fragment with the UI components
     */
    public void finaliseUI() {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final ArrayList<ContainerUI> tempArray = FragmentCustomisationHelper.this.getUIComponents();

                if (FragmentCustomisationHelper.this.getParentActivity().getDrawer().isDrawerOpen(GravityCompat.START)) {

                    try {
                        Thread.sleep(FragmentHome.DRAWER_CLOSE_DELAY);
                    } catch (final InterruptedException e) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "finaliseUI InterruptedException");
                            e.printStackTrace();
                        }
                    }
                }

                if (FragmentCustomisationHelper.this.getParent().isActive()) {

                    FragmentCustomisationHelper.this.getParentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        FragmentCustomisationHelper.this.getParent().getObjects().addAll(tempArray);
                        FragmentCustomisationHelper.this.getParent().getAdapter().notifyItemRangeInserted(0, FragmentCustomisationHelper.this.getParent().getObjects().size());
                    }});

                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "finaliseUI Fragment detached");
                    }
                }
            }
        });
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
     * Show the custom intro dialog
     */
    @SuppressWarnings("ConstantConditions")
    public void showCustomIntroDialog() {

        final String currentIntro = SPH.getCustomIntro(getApplicationContext());
        String hint = null;
        String content = null;

        if (currentIntro != null) {
            if (currentIntro.isEmpty()) {
                hint = getApplicationContext().getString(R.string.silence);
            } else {
                content = currentIntro;
            }
        } else {
            hint = getApplicationContext().getString(R.string.custom_intro_hint);
        }

        final MaterialDialog materialDialog = new MaterialDialog.Builder(getParentActivity())
                .title(R.string.menu_custom_intro)
                .positiveText(R.string.save)
                .iconRes(R.drawable.ic_crown)
                .backgroundColorRes(R.color.colorTint)
                .content(R.string.custom_intro_text)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .positiveText(R.string.save)
                .negativeText(android.R.string.cancel)
                .checkBoxPromptRes(R.string.custom_intro_checkbox_title,
                        SPH.getCustomIntroRandom(getApplicationContext()), null)

                .input(hint, content, true, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull final MaterialDialog dialog, final CharSequence input) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showCustomIntroDialog: input: " + input);
                        }

                        if (input != null) {
                            if (UtilsString.notNaked(input.toString())) {
                                SPH.setCustomIntroRandom(FragmentCustomisationHelper.this.getApplicationContext(),
                                        dialog.isPromptCheckBoxChecked());
                                SPH.setCustomIntro(FragmentCustomisationHelper.this.getApplicationContext(), input.toString().trim());
                            } else {
                                SPH.setCustomIntroRandom(FragmentCustomisationHelper.this.getApplicationContext(), false);
                                SPH.setCustomIntro(FragmentCustomisationHelper.this.getApplicationContext(), "");
                            }
                        }
                    }
                }).build();

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
    public FragmentCustomisation getParent() {
        return parentFragment;
    }
}
