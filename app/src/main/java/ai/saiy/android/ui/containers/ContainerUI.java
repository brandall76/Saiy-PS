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

package ai.saiy.android.ui.containers;

import android.support.annotation.NonNull;

/**
 * Class for the most commonly used UI list elements
 * <p>
 * Created by benrandall76@gmail.com on 18/07/2016.
 */

public class ContainerUI {

    private String title;
    private String subtitle;

    private int iconMain;
    private int iconExtra;

    public ContainerUI() {
    }

    /**
     * Constructor
     *
     * @param title     of the element
     * @param subtitle  of the element
     * @param iconMain  main icon
     * @param iconExtra secondary icon
     */
    public ContainerUI(@NonNull final String title, @NonNull final String subtitle, final int iconMain,
                       final int iconExtra) {
        this.title = title;
        this.subtitle = subtitle;
        this.iconMain = iconMain;
        this.iconExtra = iconExtra;

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull final String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(@NonNull final String subtitle) {
        this.subtitle = subtitle;
    }

    public int getIconMain() {
        return iconMain;
    }

    public void setIconMain(final int iconMain) {
        this.iconMain = iconMain;
    }

    public int getIconExtra() {
        return iconExtra;
    }

    public void setIconExtra(final int iconExtra) {
        this.iconExtra = iconExtra;
    }
}
