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

package ai.saiy.android.applications;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Pair;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

/**
 * Utility class for application related methods.
 * <p>
 * Created by benrandall76@gmail.com on 26/04/2016.
 */
public class UtilsApplication {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = UtilsApplication.class.getSimpleName();

    public static final String USAGE_STATS_SERVICE = "usagestats";

    /**
     * Helper method to get an application name from a package name
     *
     * @param ctx         the application context
     * @param packageName the application package name
     * @return a {@link Pair} of which the first parameter denotes success and the second the application
     * name or null if the process failed.
     */
    public static Pair<Boolean, String> getAppNameFromPackage(@NonNull final Context ctx, @NonNull final String packageName) {

        final PackageManager packageManager = ctx.getPackageManager();
        final ApplicationInfo applicationInfo;

        try {
            applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            return new Pair<>(true, packageManager.getApplicationLabel(applicationInfo).toString());
        } catch (final PackageManager.NameNotFoundException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getAppNameFromPackage: NameNotFoundException");
                e.printStackTrace();
            }
        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getAppNameFromPackage: NullPointerException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getAppNameFromPackage: Exception");
                e.printStackTrace();
            }
        }

        return new Pair<>(false, null);
    }

    /**
     * Kill a specific application
     *
     * @param ctx         the application context
     * @param packageName of the application to kill
     * @return true if the process was killed successfully, false otherwise
     */
    public static boolean killPackage(@NonNull final Context ctx, @NonNull final String packageName) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "killPackage");
        }

        try {
            ((ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE))
                    .killBackgroundProcesses(packageName);
            return true;

        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "killPackage: Exception");
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Check if a specific application package is installed
     *
     * @param ctx         the application context
     * @param packageName of the application
     * @return true if the application is installed, false otherwise
     */
    public static boolean isAppInstalled(@NonNull final Context ctx, @NonNull final String packageName) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "isAppInstalled");
        }

        try {
            ctx.getApplicationContext().getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (final PackageManager.NameNotFoundException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "isAppInstalled: NameNotFoundException");
                e.printStackTrace();
            }
        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "isAppInstalled: NullPointerException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "isAppInstalled: Exception");
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Launch an application from its package name
     *
     * @param ctx         the application context
     * @param packageName of the application
     * @return true if the application was successfully launched, false otherwise
     */
    public static boolean launchAppFromPackageName(final Context ctx, final String packageName) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "launchAppFromPackageName");
        }

        final Intent intent = ctx.getPackageManager().getLaunchIntentForPackage(packageName);

        if (intent != null) {
            try {
                ctx.startActivity(intent);
                return true;
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "launchAppFromPackageName: Exception");
                    e.printStackTrace();
                }
            }
        }

        return false;

    }

    /**
     * Get the package name of the current foreground application
     *
     * @param ctx the application context
     * @return the package name or null
     */
    @SuppressLint("InlinedApi")
    public static String getForegroundPackage(@NonNull final Context ctx, final long history) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getForegroundPackage");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //noinspection NewApi
            return getForegroundPackage21(ctx, history);
        } else {
            return getForegroundPackageDeprecated(ctx);
        }
    }

    /**
     * Get the package name of the current foreground application
     *
     * @param ctx the application context
     * @return the package name or null
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static String getForegroundPackage21(@NonNull final Context ctx, final long history) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getForegroundPackage21");
        }

        try {

            @SuppressWarnings("WrongConstant")
            final UsageStatsManager mUsageStatsManager = (UsageStatsManager)
                    ctx.getSystemService(USAGE_STATS_SERVICE);

            final long currentTime = System.currentTimeMillis();

            final List<UsageStats> statsList = mUsageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY, currentTime - history, currentTime);

            if (UtilsList.notNaked(statsList)) {

                final SortedMap<Long, UsageStats> sortedMap = new TreeMap<>();

                for (final UsageStats usageStats : statsList) {
                    sortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }

                if (!sortedMap.isEmpty()) {
                    final String packageName = sortedMap.get(sortedMap.lastKey()).getPackageName();

                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "getForegroundPackage21: packageName: " + packageName);
                    }

                    return packageName;
                }
            }
        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getForegroundPackage21 NullPointerException");
                e.printStackTrace();
            }
        } catch (final IndexOutOfBoundsException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getForegroundPackage21 IndexOutOfBoundsException");
                e.printStackTrace();
            }
        } catch (final SecurityException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getForegroundPackage21 SecurityException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getForegroundPackage21 Exception");
                e.printStackTrace();
            }
        }

        return null;
    }


    /**
     * Get the package name of the current foreground application
     *
     * @param ctx the application context
     * @return the package name or null
     */
    @SuppressWarnings("deprecation")
    private static String getForegroundPackageDeprecated(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getForegroundPackageDeprecated");
        }

        try {

            final ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);

            final List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);

            if (taskInfo != null && taskInfo.size() > 0) {

                final ActivityManager.RunningTaskInfo ti = taskInfo.get(0);

                if (ti != null) {

                    final PackageManager pm = ctx.getPackageManager();

                    final ComponentName cn = ti.topActivity;

                    if (cn != null) {

                        final PackageInfo foregroundAppPackageInfo = pm.getPackageInfo(cn.getPackageName(), 0);

                        if (foregroundAppPackageInfo != null) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "getForegroundPackageDeprecated: packageName: "
                                        + foregroundAppPackageInfo.packageName);
                            }

                            return foregroundAppPackageInfo.packageName;
                        }
                    }
                }
            }

        } catch (final PackageManager.NameNotFoundException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getForegroundPackageDeprecated NameNotFoundException");
                e.printStackTrace();
            }
        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getForegroundPackageDeprecated NullPointerException");
                e.printStackTrace();
            }
        } catch (final IndexOutOfBoundsException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getForegroundPackageDeprecated IndexOutOfBoundsException");
                e.printStackTrace();
            }
        } catch (final SecurityException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getForegroundPackageDeprecated SecurityException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getForegroundPackageDeprecated Exception");
                e.printStackTrace();
            }
        }

        return null;
    }
}
