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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;

import ai.saiy.android.BuildConfig;
import ai.saiy.android.R;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.components.DividerItemDecoration;
import ai.saiy.android.ui.components.UIMainAdapter;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.FragmentAbout;
import ai.saiy.android.ui.fragment.FragmentHome;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.BSD2ClauseLicense;
import de.psdev.licensesdialog.licenses.CreativeCommonsAttributionShareAlike30Unported;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;

/**
 * Utility class to assist its parent fragment and avoid clutter there
 * <p>
 * Created by benrandall76@gmail.com on 25/07/2016.
 */

public class FragmentAboutHelper {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentAboutHelper.class.getSimpleName();

    private final FragmentAbout parentFragment;

    /**
     * Constructor
     *
     * @param parentFragment the parent fragment for this helper class
     */
    public FragmentAboutHelper(@NonNull final FragmentAbout parentFragment) {
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
        containerUI.setTitle(getString(R.string.app_name)
                + " V" + BuildConfig.VERSION_NAME + "A");
        containerUI.setSubtitle(getString(R.string.menu_saiy));
        containerUI.setIconMain(R.drawable.ic_beta);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_release_notes));
        containerUI.setSubtitle(getString(R.string.menu_tap_view));
        containerUI.setIconMain(R.drawable.ic_note_text);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_privacy_policy));
        containerUI.setSubtitle(getString(R.string.menu_tap_view));
        containerUI.setIconMain(R.drawable.ic_blinds);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_legal));
        containerUI.setSubtitle(getString(R.string.menu_tap_view));
        containerUI.setIconMain(R.drawable.ic_gavel);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_source_code));
        containerUI.setSubtitle(getString(R.string.menu_tap_view));
        containerUI.setIconMain(R.drawable.ic_github);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_developer_api));
        containerUI.setSubtitle(getString(R.string.menu_tap_view));
        containerUI.setIconMain(R.drawable.ic_github);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_hannes_ahremark) + " - "
                + getString(R.string.menu_app_icon));
        containerUI.setSubtitle(getString(R.string.submenu_studio_ahremark));
        containerUI.setIconMain(R.drawable.ic_palette);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_daniel_schlapa) + " - "
                + getString(R.string.menu_testing));
        containerUI.setSubtitle(getString(R.string.submenu_daniel_schlapa));
        containerUI.setIconMain(R.drawable.ic_emoticon_cool);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_special_thanks));
        containerUI.setSubtitle(getString(R.string.menu_tap_legends));
        containerUI.setIconMain(R.drawable.ic_xda);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_commercial_enquiries));
        containerUI.setSubtitle(getString(R.string.menu_tap_send));
        containerUI.setIconMain(R.drawable.ic_email);
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
                final ArrayList<ContainerUI> tempArray = FragmentAboutHelper.this.getUIComponents();

                try {
                    Thread.sleep(FragmentHome.DRAWER_CLOSE_DELAY);
                } catch (final InterruptedException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "finaliseUI InterruptedException");
                        e.printStackTrace();
                    }
                }

                if (FragmentAboutHelper.this.getParent().isActive()) {

                    FragmentAboutHelper.this.getParentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            FragmentAboutHelper.this.getParent().getObjects().addAll(tempArray);
                            FragmentAboutHelper.this.getParent().getAdapter().notifyItemRangeInserted(0, FragmentAboutHelper.this.getParent().getObjects().size());
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
     * Get the license information we need to display
     *
     * @return the {@link Notices} object containing the required {@link Notice} elements
     */
    public Notices getLicenses() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getLicenses");
        }

        final Notices notices = new Notices();

        notices.addNotice(new Notice(getString(R.string.google_play_services),
                Constants.LICENSE_URL_GOOGLE_PLAY_SERVICES,
                getString(R.string.license_android_open_source),
                new ApacheSoftwareLicense20()));

        notices.addNotice(new Notice(getString(R.string.gson),
                Constants.LICENSE_URL_GSON,
                getString(R.string.license_google_inc),
                new ApacheSoftwareLicense20()));

        notices.addNotice(new Notice(getString(R.string.volley),
                Constants.LICENSE_URL_VOLLEY,
                getString(R.string.license_volley),
                new ApacheSoftwareLicense20()));

        notices.addNotice(new Notice(getString(R.string.two_forty_four),
                Constants.LICENSE_URL_TWO_FORTY_FOUR,
                getString(R.string.license_two_forty_four),
                new ApacheSoftwareLicense20()));

        notices.addNotice(new Notice(getString(R.string.speechutils),
                Constants.LICENSE_URL_KAAREL_KALJURAND,
                getString(R.string.license_kaarel_kaljurand),
                new ApacheSoftwareLicense20()));

        notices.addNotice(new Notice(getString(R.string.microsoft_translator),
                Constants.LICENSE_URL_MICROSOFT_TRANSLATOR,
                getString(R.string.license_microsoft_translator),
                new ApacheSoftwareLicense20()));

        notices.addNotice(new Notice(getString(R.string.apache_commons),
                Constants.LICENSE_URL_APACHE_COMMONS,
                getString(R.string.license_apache_commons),
                new ApacheSoftwareLicense20()));

        notices.addNotice(new Notice(getString(R.string.simmetrics),
                Constants.LICENSE_URL_SIMMETRICS,
                getString(R.string.license_simmetrics),
                new ApacheSoftwareLicense20()));

        notices.addNotice(new Notice(getString(R.string.nuance_speechkit),
                Constants.LICENSE_URL_NUANCE_SPEECHKIT,
                getString(R.string.license_nuance_speechkit),
                new BSD2ClauseLicense()));

        notices.addNotice(new Notice(getString(R.string.guava),
                Constants.LICENSE_URL_GUAVA,
                getString(R.string.license_guava),
                new ApacheSoftwareLicense20()));

        notices.addNotice(new Notice(getString(R.string.microsoft_cognitive),
                Constants.LICENSE_URL_MICROSOFT_COGNITIVE,
                getString(R.string.license_microsoft_cognitive),
                new MITLicense()));

        notices.addNotice(new Notice(getString(R.string.api_ai),
                Constants.LICENSE_URL_API_AI,
                getString(R.string.license_api_ai),
                new ApacheSoftwareLicense20()));

        notices.addNotice(new Notice(getString(R.string.simple_xml),
                Constants.LICENSE_URL_SIMPLE_XML,
                getString(R.string.license_simple_xml),
                new ApacheSoftwareLicense20()));

        notices.addNotice(new Notice(getString(R.string.material_icons),
                Constants.LICENSE_MATERIAL_ICONS,
                getString(R.string.license_material_icons),
                new ApacheSoftwareLicense20()));

        notices.addNotice(new Notice(getString(R.string.material_dialogs),
                Constants.LICENSE_MATERIAL_DIALOGS,
                getString(R.string.license_material_dialogs),
                new MITLicense()));

        notices.addNotice(new Notice(getString(R.string.pocketsphinx),
                Constants.LICENSE_POCKETSPHINX,
                getString(R.string.license_pocketsphinx),
                new BSD2ClauseLicense()));

        notices.addNotice(new Notice(getString(R.string.sound_bible),
                Constants.LICENSE_SOUND_BIBLE,
                getString(R.string.license_sound_bible),
                new CreativeCommonsAttributionShareAlike30Unported()));

        return notices;
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
    public FragmentAbout getParent() {
        return parentFragment;
    }
}
