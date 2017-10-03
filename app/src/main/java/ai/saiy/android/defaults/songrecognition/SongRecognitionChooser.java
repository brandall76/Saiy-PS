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

package ai.saiy.android.defaults.songrecognition;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ai.saiy.android.applications.Installed;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

/**
 * Created by benrandall76@gmail.com on 12/06/2016.
 */
public class SongRecognitionChooser implements Parcelable {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = SongRecognitionChooser.class.getSimpleName();

    public static final String PARCEL_KEY = "song_recognition_chooser";

    private String applicationName;
    private String packageName;
    private boolean installed;

    public static final Creator<SongRecognitionChooser> CREATOR = new
            Creator<SongRecognitionChooser>() {
                public SongRecognitionChooser createFromParcel(@NonNull final Parcel in) {
                    return new SongRecognitionChooser(in);
                }

                public SongRecognitionChooser[] newArray(int size) {
                    return new SongRecognitionChooser[size];
                }
            };

    public SongRecognitionChooser() {
    }

    private SongRecognitionChooser(@NonNull final Parcel in) {
        readFromParcel(in);
    }

    public SongRecognitionChooser(@NonNull final String applicationName, @NonNull final String packageName,
                                  final boolean installed) {
        this.applicationName = applicationName;
        this.packageName = packageName;
        this.installed = installed;
    }

    public void readFromParcel(@NonNull final Parcel in) {
        this.applicationName = in.readString();
        this.packageName = in.readString();
        this.installed = (in.readInt() == 1);
    }

    @Override
    public void writeToParcel(final Parcel out, final int flags) {
        out.writeString(this.applicationName);
        out.writeString(this.packageName);
        out.writeInt(installed ? 1 : 0);
    }

    public String getApplicationName() {
        return applicationName;
    }

    public boolean isInstalled() {
        return installed;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setApplicationName(final String applicationName) {
        this.applicationName = applicationName;
    }

    public void setInstalled(final boolean installed) {
        this.installed = installed;
    }

    public void setPackageName(final String packageName) {
        this.packageName = packageName;
    }

    public static ArrayList<SongRecognitionChooser> prepareChooser(@NonNull final Context ctx,
                                                                   @NonNull final SupportedLanguage sl) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "prepareChooser");
        }

        final ArrayList<SongRecognitionChooser> chooserList = new ArrayList<>();

        SongRecognitionChooser src;

        src = new SongRecognitionChooser(
                SongRecognitionProvider.getApplicationName(ctx, sl, SongRecognitionProvider.SHAZAM),
                Installed.PACKAGE_SHAZAM, Installed.isPackageInstalled(ctx, Installed.PACKAGE_SHAZAM));

        chooserList.add(src);

        src = new SongRecognitionChooser(
                SongRecognitionProvider.getApplicationName(ctx, sl, SongRecognitionProvider.SHAZAM_ENCORE),
                Installed.PACKAGE_SHAZAM_ENCORE, Installed.isPackageInstalled(ctx,
                Installed.PACKAGE_SHAZAM_ENCORE));

        chooserList.add(src);

        src = new SongRecognitionChooser(
                SongRecognitionProvider.getApplicationName(ctx, sl, SongRecognitionProvider.SOUND_HOUND),
                Installed.PACKAGE_SOUND_HOUND, Installed.isPackageInstalled(ctx,
                Installed.PACKAGE_SOUND_HOUND));

        chooserList.add(src);

        src = new SongRecognitionChooser(
                SongRecognitionProvider.getApplicationName(ctx, sl, SongRecognitionProvider.SOUND_HOUND_PREMIUM),
                Installed.PACKAGE_SOUND_HOUND_PREMIUM, Installed.isPackageInstalled(ctx,
                Installed.PACKAGE_SOUND_HOUND_PREMIUM));

        chooserList.add(src);

        src = new SongRecognitionChooser(
                SongRecognitionProvider.getApplicationName(ctx, sl, SongRecognitionProvider.TRACK_ID),
                Installed.PACKAGE_TRACK_ID, Installed.isPackageInstalled(ctx, Installed.PACKAGE_TRACK_ID));

        chooserList.add(src);

        src = new SongRecognitionChooser(
                SongRecognitionProvider.getApplicationName(ctx, sl, SongRecognitionProvider.GOOGLE),
                Installed.PACKAGE_GOOGLE_SOUND_SEARCH, Installed.isPackageInstalled(ctx,
                Installed.PACKAGE_GOOGLE_SOUND_SEARCH));

        chooserList.add(src);

        chooserList.addAll(prepareChooserDefault(ctx));

        return removeDuplicates(chooserList);
    }

    private static ArrayList<SongRecognitionChooser> prepareChooserDefault(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "prepareChooser");
        }

        final ArrayList<SongRecognitionChooser> chooserList = new ArrayList<>();

        final Intent recognitionIntent = new Intent();
        recognitionIntent.setAction(SongRecognitionProvider.MEDIA_RECOGNIZE);

        final PackageManager pm = ctx.getPackageManager();
        final List<ResolveInfo> list = pm.queryIntentActivities(recognitionIntent, PackageManager.GET_META_DATA);

        final int size = list.size();

        if (size > 0) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "prepareChooser: apps: " + size);
            }

            String packageName;
            String applicationName;
            SongRecognitionChooser src;

            for (int i = 0; i < size; i++) {
                src = new SongRecognitionChooser();
                applicationName = list.get(i).loadLabel(pm).toString();

                if (!UtilsString.notNaked(applicationName)) {
                    src.setApplicationName(applicationName);
                    packageName = list.get(i).activityInfo.packageName;

                    if (UtilsString.notNaked(packageName)) {
                        src.setPackageName(packageName);
                        src.setInstalled(true);
                        chooserList.add(src);
                    }
                }
            }

        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "prepareChooser: no apps responded to intent");
            }
        }

        return chooserList;
    }

    private static ArrayList<SongRecognitionChooser> removeDuplicates(
            @NonNull final ArrayList<SongRecognitionChooser> chooserList) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "prepareChooser");
        }

        final ArrayList<String> packageList = new ArrayList<>();
        final Iterator<SongRecognitionChooser> itr = chooserList.iterator();

        String packageName;
        SongRecognitionChooser src;
        while (itr.hasNext()) {
            src = itr.next();
            packageName = src.getPackageName();

            if (packageList.contains(packageName)) {
                if (DEBUG) {
                    MyLog.v(CLS_NAME, "src removed: " + packageName);
                }

                itr.remove();
            } else {
                packageList.add(packageName);
            }
        }

        return chooserList;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
