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

import ai.saiy.android.R;
import ai.saiy.android.intent.ExecuteIntent;
import ai.saiy.android.intent.IntentConstants;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.service.helper.SelfAwareHelper;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.helper.FragmentSuperuserHelper;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

/**
 * Created by benrandall76@gmail.com on 18/07/2016.
 */

public class FragmentSuperUser extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentSuperUser.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<ContainerUI> mObjects;
    private FragmentSuperuserHelper helper;

    private static final Object lock = new Object();

    private Context mContext;

    public FragmentSuperUser() {
    }

    @SuppressWarnings("UnusedParameters")
    public static FragmentSuperUser newInstance(@Nullable final Bundle args) {
        return new FragmentSuperUser();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }

        helper = new FragmentSuperuserHelper(this);
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
                getParentActivity().setTitle(getString(R.string.title_superuser));
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
                getParentActivity().toast(getString(R.string.menu_root), Toast.LENGTH_SHORT);
                break;
            case 1:
                getParentActivity().vibrate();
                SPH.setStartAtBoot(getApplicationContext(), !SPH.getStartAtBoot(getApplicationContext()));
                mObjects.get(position).setIconExtra(SPH.getStartAtBoot(getApplicationContext()) ?
                        FragmentHome.CHECKED : FragmentHome.UNCHECKED);
                mAdapter.notifyItemChanged(position);
                break;
            case 2:
                helper.showAccountPicker();
                break;
            case 3:
                getParentActivity().vibrate();
                SPH.setPingCheck(getApplicationContext(), !SPH.getPingCheck(getApplicationContext()));
                mObjects.get(position).setIconExtra(SPH.getPingCheck(getApplicationContext()) ?
                        FragmentHome.CHECKED : FragmentHome.UNCHECKED);
                mAdapter.notifyItemChanged(position);
                break;
            case 4:
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (!helper.showBlackListSelector()) {
                            FragmentSuperUser.this.getParentActivity().toast(FragmentSuperUser.this.getString(R.string.blacklist_no_apps),
                                    Toast.LENGTH_LONG);
                        }
                    }
                });
                break;
            case 5:
                helper.showMemorySlider();
                break;
            case 6:
                getParentActivity().vibrate();

                final boolean enabled = SPH.getInterceptGoogle(getApplicationContext());

                SPH.setInterceptGoogle(getApplicationContext(), !enabled);
                mObjects.get(position).setIconExtra(!enabled ? FragmentHome.CHECKED : FragmentHome.UNCHECKED);
                mAdapter.notifyItemChanged(position);

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (!enabled && !SelfAwareHelper.saiyAccessibilityRunning(FragmentSuperUser.this.getApplicationContext())) {
                            ExecuteIntent.settingsIntent(FragmentSuperUser.this.getApplicationContext(), IntentConstants.SETTINGS_ACCESSIBILITY);
                            FragmentSuperUser.this.getParentActivity().speak(R.string.accessibility_enable, LocalRequest.ACTION_SPEAK_ONLY);
                        } else if (!enabled) {
                            SelfAwareHelper.startAccessibilityService(FragmentSuperUser.this.getApplicationContext());
                        }
                    }
                });
                break;
            case 7:
                getParentActivity().toast(getString(R.string.menu_algorithms), Toast.LENGTH_SHORT);
                break;
            case 8:
                getParentActivity().vibrate();
                SPH.setRecogniserBusyFix(getApplicationContext(), !SPH.getRecogniserBusyFix(getApplicationContext()));
                mObjects.get(position).setIconExtra(SPH.getRecogniserBusyFix(getApplicationContext()) ?
                        FragmentHome.CHECKED : FragmentHome.UNCHECKED);
                mAdapter.notifyItemChanged(position);
                break;
            case 9:
                getParentActivity().vibrate();
                SPH.setOkayGoogleFix(getApplicationContext(), !SPH.getOkayGoogleFix(getApplicationContext()));
                mObjects.get(position).setIconExtra(SPH.getOkayGoogleFix(getApplicationContext()) ?
                        FragmentHome.CHECKED : FragmentHome.UNCHECKED);
                mAdapter.notifyItemChanged(position);
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

        helper.enrollmentCancelled.set(true);
        helper.cancelTimer();
        getParentActivity().showProgress(false);
    }
}
