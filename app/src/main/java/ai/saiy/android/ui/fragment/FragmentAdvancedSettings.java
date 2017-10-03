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

import ai.saiy.android.R;
import ai.saiy.android.intent.ExecuteIntent;
import ai.saiy.android.intent.IntentConstants;
import ai.saiy.android.permissions.PermissionHelper;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.helper.FragmentAdvancedSettingsHelper;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

/**
 * Created by benrandall76@gmail.com on 18/07/2016.
 */

public class FragmentAdvancedSettings extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentAdvancedSettings.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<ContainerUI> mObjects;
    private FragmentAdvancedSettingsHelper helper;

    private static final Object lock = new Object();

    private Context mContext;

    public FragmentAdvancedSettings() {
    }

    public static FragmentAdvancedSettings newInstance(@Nullable final Bundle args) {
        return new FragmentAdvancedSettings();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }
        helper = new FragmentAdvancedSettingsHelper(this);
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

    @Override
    public void onStart() {
        super.onStart();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onStart");
        }

        synchronized (lock) {
            if (mObjects.isEmpty()) {
                getParentActivity().setTitle(getString(R.string.title_advanced));
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

        final int position = (int) view.getTag();

        switch (position) {

            case 0:
                getParentActivity().vibrate();
                SPH.setToastUnknown(getApplicationContext(), !SPH.getToastUnknown(getApplicationContext()));
                mObjects.get(position).setIconExtra(SPH.getToastUnknown(getApplicationContext()) ?
                        FragmentHome.CHECKED : FragmentHome.UNCHECKED);
                mAdapter.notifyItemChanged(position);
                break;
            case 1:
                getParentActivity().vibrate();
                SPH.setVibrateCondition(getApplicationContext(), !SPH.getVibrateCondition(getApplicationContext()));
                mObjects.get(position).setIconExtra(SPH.getVibrateCondition(getApplicationContext()) ?
                        FragmentHome.CHECKED : FragmentHome.UNCHECKED);
                mAdapter.notifyItemChanged(position);
                break;
            case 2:
                getParentActivity().vibrate();
                SPH.setUseOffline(getApplicationContext(), !SPH.getUseOffline(getApplicationContext()));
                mObjects.get(position).setIconExtra(SPH.getUseOffline(getApplicationContext()) ?
                        FragmentHome.CHECKED : FragmentHome.UNCHECKED);
                mAdapter.notifyItemChanged(position);
                break;
            case 3:
                //noinspection NewApi
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                        && !PermissionHelper.checkUsageStatsPermission(getApplicationContext())) {
                    getParentActivity().speak(R.string.app_speech_usage_stats, LocalRequest.ACTION_SPEAK_ONLY);
                    ExecuteIntent.settingsIntent(getApplicationContext(), IntentConstants.SETTINGS_USAGE_STATS);
                } else {
                    helper.showHotwordSelector();
                }
                break;
            case 4:
                helper.showGenderSelector();
                break;
            case 5:
                getParentActivity().vibrate();
                SPH.setMotionEnabled(getApplicationContext(), !SPH.getMotionEnabled(getApplicationContext()));
                mObjects.get(position).setIconExtra(SPH.getMotionEnabled(getApplicationContext()) ?
                        FragmentHome.CHECKED : FragmentHome.UNCHECKED);
                mAdapter.notifyItemChanged(position);
                break;
            case 6:
                helper.showPauseDetectionSlider();
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
