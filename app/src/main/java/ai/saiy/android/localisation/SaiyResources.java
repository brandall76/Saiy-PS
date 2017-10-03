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

package ai.saiy.android.localisation;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;

import java.util.Formatter;
import java.util.Locale;

import ai.saiy.android.utils.MyLog;

/**
 * Class to manage fetching {@link Resources} for a specific {@link Locale}. API levels less
 * than {@link Build.VERSION_CODES#JELLY_BEAN_MR1} require an ugly implementation.
 * <p>
 * Subclass implements {@link Resources} in case of further functionality requirements, such as
 * possible resource not found exception handling.
 * <p>
 * Created by benrandall76@gmail.com on 27/03/2016.
 */
public class SaiyResources {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = SaiyResources.class.getSimpleName();

    private final Context mContext;
    private final AssetManager assetManager;
    private final DisplayMetrics metrics;
    private final Configuration configuration;
    private final Locale targetLocale;

    /**
     * Constructor
     *
     * @param mContext the application context
     * @param sl       the {@link SupportedLanguage}
     */
    public SaiyResources(@NonNull final Context mContext, @NonNull final SupportedLanguage sl) {

        this.mContext = mContext;
        final Resources resources = this.mContext.getResources();
        this.assetManager = resources.getAssets();
        this.metrics = resources.getDisplayMetrics();
        this.configuration = new Configuration(resources.getConfiguration());
        this.targetLocale = sl.getLocale();
    }

    /**
     * Must be called once no further localised resources are required. If it is not,
     * {@link Build.VERSION_CODES#JELLY_BEAN_MR1} devices will have their global system local changed.
     */
    @SuppressWarnings("deprecation")
    public void reset() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "reset");
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 && checkNotNull()) {
            configuration.locale = Locale.getDefault(); // reset
            new ResourceManager(assetManager, metrics, configuration); // reset
        }
    }

    /**
     * Simple check to avoid null pointers prior to resetting.
     *
     * @return if the configuration can be safely reset. False otherwise.
     */
    private boolean checkNotNull() {
        return configuration != null && assetManager != null && metrics != null;
    }

    /**
     * Get a localised string array
     *
     * @param resourceId of the string array
     * @return the string array
     */
    @SuppressWarnings("deprecation")
    public String[] getStringArray(final int resourceId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getStringArray");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(targetLocale);
            return mContext.createConfigurationContext(configuration).getResources().getStringArray(resourceId);
        } else {
            configuration.locale = targetLocale;
            return new ResourceManager(assetManager, metrics, configuration).getStringArray(resourceId);
        }
    }

    /**
     * Get a localised string
     *
     * @param resourceId of the string
     * @return the string
     */
    @SuppressWarnings("deprecation")
    public String getString(final int resourceId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getString");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(targetLocale);
            return mContext.createConfigurationContext(configuration).getResources().getString(resourceId);
        } else {
            configuration.locale = targetLocale;
            return new ResourceManager(assetManager, metrics, configuration).getString(resourceId);
        }
    }

    /**
     * Only here in case of future functionality requirements.
     */
    private final class ResourceManager extends Resources {
        public ResourceManager(final AssetManager assets, final DisplayMetrics metrics, final Configuration config) {
            super(assets, metrics, config);
        }

        /**
         * Return the string array associated with a particular resource ID.
         *
         * @param id The desired resource identifier, as generated by the aapt
         *           tool. This integer encodes the package, type, and resource
         *           entry. The value 0 is an invalid identifier.
         * @return The string array associated with the resource.
         * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
         */
        @Override
        public String[] getStringArray(final int id) throws NotFoundException {
            return super.getStringArray(id);
        }

        /**
         * Return the string value associated with a particular resource ID,
         * substituting the format arguments as defined in {@link Formatter}
         * and {@link String#format}. It will be stripped of any styled text
         * information.
         *
         * @param id         The desired resource identifier, as generated by the aapt
         *                   tool. This integer encodes the package, type, and resource
         *                   entry. The value 0 is an invalid identifier.
         * @param formatArgs The format arguments that will be used for substitution.
         * @return String The string data associated with the resource,
         * stripped of styled text information.
         * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
         */
        @NonNull
        @Override
        public String getString(final int id, final Object... formatArgs) throws NotFoundException {
            return super.getString(id, formatArgs);
        }

        /**
         * Return the string value associated with a particular resource ID.  It
         * will be stripped of any styled text information.
         *
         * @param id The desired resource identifier, as generated by the aapt
         *           tool. This integer encodes the package, type, and resource
         *           entry. The value 0 is an invalid identifier.
         * @return String The string data associated with the resource,
         * stripped of styled text information.
         * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
         */
        @NonNull
        @Override
        public String getString(final int id) throws NotFoundException {
            return super.getString(id);
        }
    }
}
