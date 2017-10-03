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
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.components.UIBugsAdapter;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.FragmentBugs;
import ai.saiy.android.ui.fragment.FragmentHome;
import ai.saiy.android.utils.MyLog;

/**
 * Utility class to assist its parent fragment and avoid clutter there
 * <p>
 * Created by benrandall76@gmail.com on 25/07/2016.
 */

public class FragmentBugsHelper {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentHomeHelper.class.getSimpleName();

    private final FragmentBugs parentFragment;

    /**
     * Constructor
     *
     * @param parentFragment the parent fragment for this helper class
     */
    public FragmentBugsHelper(@NonNull final FragmentBugs parentFragment) {
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
        containerUI.setTitle(getParent().getString(R.string.bug_title_offline_recognition));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_offline_recognition));
        containerUI.setIconMain(R.drawable.ic_bug);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_no_speech));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_no_speech));
        containerUI.setIconMain(R.drawable.ic_bug);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_delay_speech));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_delay_speech));
        containerUI.setIconMain(R.drawable.ic_bug);
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
                parent.findViewById(R.id.layout_bugs_fragment_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getParentActivity()));
        return mRecyclerView;
    }

    /**
     * Get the Edit Text for this fragment
     *
     * @param parent the view parent
     * @return the {@link EditText}
     */
    public EditText getEditText(@NonNull final View parent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getEditText");
        }

        final EditText editText = (EditText) parent.findViewById(R.id.etCommand);
        editText.setOnEditorActionListener(getParent());
        editText.setImeActionLabel(getParent().getString(R.string.menu_run), EditorInfo.IME_ACTION_GO);
        return editText;
    }

    /**
     * Get the Image Button for this fragment
     *
     * @param parent the view parent
     * @return the {@link ImageButton}
     */
    public ImageButton getImageButton(@NonNull final View parent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getImageButton");
        }
        final ImageButton imageButton = (ImageButton) parent.findViewById(R.id.ibRun);
        imageButton.setOnClickListener(getParent());
        return imageButton;
    }

    /**
     * Get the adapter for this fragment
     *
     * @param mObjects list of {@link ContainerUI} elements
     * @return the {@link UIBugsAdapter}
     */
    public UIBugsAdapter getAdapter(@NonNull final ArrayList<ContainerUI> mObjects) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getAdapter");
        }
        return new UIBugsAdapter(mObjects, getParent(), getParent());
    }

    /**
     * Update the parent fragment with the UI components. If the drawer is not open in the parent
     * Activity, we can assume this method is called as a result of the back button being pressed, or
     * the first initialisation of the application - neither of which require a delay.
     */
    public void finaliseUI() {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final ArrayList<ContainerUI> tempArray = FragmentBugsHelper.this.getUIComponents();

                if (FragmentBugsHelper.this.getParentActivity().getDrawer().isDrawerOpen(GravityCompat.START)) {

                    try {
                        Thread.sleep(FragmentHome.DRAWER_CLOSE_DELAY);
                    } catch (final InterruptedException e) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "finaliseUI InterruptedException");
                            e.printStackTrace();
                        }
                    }
                }

                if (FragmentBugsHelper.this.getParent().isActive()) {

                    FragmentBugsHelper.this.getParentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        FragmentBugsHelper.this.getParent().getObjects().addAll(tempArray);
                        FragmentBugsHelper.this.getParent().getAdapter().notifyItemRangeInserted(0, FragmentBugsHelper.this.getParent().getObjects().size());
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
    public FragmentBugs getParent() {
        return parentFragment;
    }
}
