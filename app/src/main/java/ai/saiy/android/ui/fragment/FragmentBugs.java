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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.intent.ExecuteIntent;
import ai.saiy.android.intent.IntentConstants;
import ai.saiy.android.recognition.TestRecognitionAction;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.helper.FragmentBugsHelper;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

/**
 * Created by benrandall76@gmail.com on 18/07/2016.
 */

public class FragmentBugs extends Fragment implements View.OnClickListener, View.OnLongClickListener,
        TextView.OnEditorActionListener {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentBugs.class.getSimpleName();

    private static final int IME_GO = 99;

    private EditText editText;
    private ImageButton imageButton;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<ContainerUI> mObjects;
    private FragmentBugsHelper helper;

    private static final Object lock = new Object();

    private Context mContext;

    public FragmentBugs() {
    }

    @SuppressWarnings("UnusedParameters")
    public static FragmentBugs newInstance(@Nullable final Bundle args) {
        return new FragmentBugs();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }
        helper = new FragmentBugsHelper(this);
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
                getParentActivity().setTitle(getString(R.string.title_troubleshooting));
                helper.finaliseUI();
            }
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreateView");
        }

        final View rootView = inflater.inflate(R.layout.fragment_bugs_layout, container, false);
        editText = helper.getEditText(rootView);
        imageButton = helper.getImageButton(rootView);
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

        switch (view.getId()) {

            case R.id.ibRun:
                testCommand();
                break;
            default:

                switch ((int) view.getTag()) {

                    case 0:
                        if (!ExecuteIntent.settingsIntent(getApplicationContext(),
                                IntentConstants.SETTINGS_VOICE_SEARCH)) {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "onClick: SETTINGS_VOICE_SEARCH");
                            }
                            ExecuteIntent.settingsIntent(getApplicationContext(),
                                    IntentConstants.SETTINGS_INPUT_METHOD);
                        }
                        break;
                    case 1:
                        ExecuteIntent.settingsIntent(getApplicationContext(),
                                IntentConstants.SETTINGS_TEXT_TO_SPEECH);
                        break;
                    case 2:
                        getParentActivity().doFragmentAddTransaction(FragmentSettings.newInstance(null),
                                String.valueOf(ActivityHome.INDEX_FRAGMENT_SETTINGS), ActivityHome.ANIMATION_FADE,
                                ActivityHome.INDEX_FRAGMENT_BUGS);
                        break;
                    default:
                        break;
                }

                break;
        }
    }

    @Override
    public boolean onLongClick(final View view) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onLongClick: " + view.getTag());
        }

        switch ((int) view.getTag()) {

            case 0:
                return false;
            case 1:
                ExecuteIntent.settingsIntent(getApplicationContext(), IntentConstants.SETTINGS_VOLUME);
                break;
            case 2:
                getParentActivity().doFragmentAddTransaction(FragmentSuperUser.newInstance(null),
                        String.valueOf(ActivityHome.INDEX_FRAGMENT_SUPER_USER), ActivityHome.ANIMATION_FADE,
                        ActivityHome.INDEX_FRAGMENT_BUGS);
                break;
            default:
                break;
        }

        return true;
    }

    /**
     * Called when an action is being performed.
     *
     * @param v        The view that was clicked.
     * @param actionId Identifier of the action.  This will be either the
     *                 identifier you supplied, or {@link EditorInfo#IME_NULL
     *                 EditorInfo.IME_NULL} if being called due to the enter key
     *                 being pressed.
     * @param event    If triggered by an enter key, this is the event;
     *                 otherwise, this is null.
     * @return Return true if you have consumed the action, else false.
     */
    @Override
    public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onEditorAction: " + actionId);
        }

        switch (actionId) {

            case EditorInfo.IME_ACTION_GO:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onEditorAction: IME_ACTION_GO");
                }
                testCommand();
                return true;
            case IME_GO:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onEditorAction: IME_GO");
                }
                testCommand();
                return true;
            default:
                break;

        }

        return false;
    }

    /**
     * Run a test command input by the user
     */
    private void testCommand() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "testCommand");
        }

        if (editText.getText() != null) {

            final String commandText = editText.getText().toString();

            if (UtilsString.notNaked(commandText)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "testCommand: executing: " + commandText);
                }

                hideIME();

                new TestRecognitionAction(getApplicationContext(), commandText.trim());

            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "testCommand: text naked");
                }
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "testCommand: getText null");
            }
        }
    }

    /**
     * Hide the IME once the input is complete
     */
    private void hideIME() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "hideIME");
        }
        ((InputMethodManager)
                getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(editText.getApplicationWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
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
