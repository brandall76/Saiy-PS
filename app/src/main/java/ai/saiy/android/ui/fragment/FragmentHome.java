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

package ai.saiy.android.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.api.language.nlu.NLULanguageAPIAI;
import ai.saiy.android.configuration.APIAIConfiguration;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.nlu.apiai.RemoteAPIAI;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.helper.FragmentHomeHelper;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

/**
 * Created by benrandall76@gmail.com on 18/07/2016.
 */

public class FragmentHome extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentHome.class.getSimpleName();

    /**
     * This seemingly, arbitrary delay is to allow the animation of the drawer closing to complete
     * prior to doing any heavy UI work, which can cause the drawer to stutter. The consequence of this,
     * is the appearance of a blank fragment, albeit momentarily, negated somewhat by the fade-in animation.
     * Regardless, I would consider it more preferable than visual lag?
     */
    public static final long DRAWER_CLOSE_DELAY = 200L;

    public static final int CHECKED = R.drawable.ic_toggle_switch_on;
    public static final int UNCHECKED = R.drawable.ic_toggle_switch_off;
    public static final int CHEVRON = R.drawable.chevron;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<ContainerUI> mObjects;
    private FragmentHomeHelper helper;

    private static final Object lock = new Object();

    private Context mContext;

    public FragmentHome() {
    }

    public static FragmentHome newInstance(@Nullable final Bundle args) {
        return new FragmentHome();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }
        helper = new FragmentHomeHelper(this);
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        this.mContext = context.getApplicationContext();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            this.mContext = activity.getApplicationContext();
        }
    }

    /**
     * The RecyclerView in all fragments is initialised with an empty adapter. We start any heavy lifting
     * here to ensure that the transition between any fragments does not cause any visual lag (stuttering).
     * <p>
     * As our fragments don't contain any dynamic content, we only need to check if the adapter content is
     * empty, to know this is the first time we've received this callback. We synchronise the process, just
     * to avoid any weird and wonderful situations that never happen....
     */
    @Override
    public void onStart() {
        super.onStart();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onStart");
        }

        synchronized (lock) {
            if (mObjects.isEmpty()) {
                getParentActivity().setTitle(getString(R.string.title_home));
                helper.finaliseUI();
            }
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreateView");
        }

        final View rootView = inflater.inflate(R.layout.layout_common_fragment_parent, container, false);
        mRecyclerView = helper.getRecyclerView(rootView);
        mObjects = new ArrayList<>();
        mAdapter = helper.getAdapter(mObjects);
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onClick(final View view) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onClick: " + view.getTag());
        }


        switch ((int) view.getTag()) {

            case 0:
                getParentActivity().vibrate();
                getParentActivity().toast(getString(R.string.menu_voice_tutorial), Toast.LENGTH_SHORT);

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {

                        final Locale vrLocale = SPH.getVRLocale(getApplicationContext());

                        final SupportedLanguage sl = SupportedLanguage.getSupportedLanguage(vrLocale);
                        final Locale ttsLocale = SPH.getTTSLocale(getApplicationContext());

                        final float[] floatsArray = new float[1];
                        floatsArray[0] = 1F;

                        final ArrayList<String> resultsArray = new ArrayList<>();
                        resultsArray.add("What is my battery temperature");


                    }
                });

                break;
            case 1:
                getParentActivity().vibrate();
                getParentActivity().toast(getString(R.string.menu_user_guide), Toast.LENGTH_SHORT);
                break;
            case 2:
                getParentActivity().vibrate();
                getParentActivity().toast(getString(R.string.menu_development), Toast.LENGTH_SHORT);
                break;
            case 3:
                getParentActivity().doFragmentReplaceTransaction(FragmentSettings.newInstance(null),
                        String.valueOf(ActivityHome.INDEX_FRAGMENT_SETTINGS), ActivityHome.ANIMATION_FADE);
                break;
            case 4:
                getParentActivity().doFragmentReplaceTransaction(FragmentCustomisation.newInstance(null),
                        String.valueOf(ActivityHome.INDEX_FRAGMENT_CUSTOMISATION), ActivityHome.ANIMATION_FADE);
                break;
            case 5:
                getParentActivity().doFragmentReplaceTransaction(FragmentAdvancedSettings.newInstance(null),
                        String.valueOf(ActivityHome.INDEX_FRAGMENT_ADVANCED_SETTINGS), ActivityHome.ANIMATION_FADE);
                break;
            case 6:
                getParentActivity().doFragmentReplaceTransaction(FragmentBugs.newInstance(null),
                        String.valueOf(ActivityHome.INDEX_FRAGMENT_BUGS), ActivityHome.ANIMATION_FADE);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onLongClick(final View view) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onLongClick: " + view.getTag());
        }

        getParentActivity().toast("long press!", Toast.LENGTH_SHORT);

        final int position = (int) view.getTag();

        switch (position) {
            default:
                break;
        }

        return true;
    }

    public boolean isActive() {
        return getActivity() != null && getParentActivity().isActive() && isAdded() && !isRemoving();
    }

    /**
     * Utility to return the parent activity neatly cast. No need for instanceOf as this fragment will
     * never be attached to another activity.
     *
     * @return the {@link ActivityHome} parent
     */
    public ActivityHome getParentActivity() {
        return (ActivityHome) getActivity();
    }

    /**
     * Utility method to ensure we double check the context being used.
     *
     * @return the application context
     */
    public Context getApplicationContext() {
        return this.mContext;
    }

    /**
     * Get the current adapter
     *
     * @return the current adapter
     */
    public RecyclerView.Adapter getAdapter() {
        return mAdapter;
    }

    /**
     * Get the current objects in the adapter
     *
     * @return the current objects
     */
    public ArrayList<ContainerUI> getObjects() {
        return mObjects;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onPause");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onResume");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDestroy");
        }
    }
}
