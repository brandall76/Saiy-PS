/*
 * Copyright (c) 2017. Saiy® Ltd. All Rights Reserved.
 *
 * Unauthorised copying of this file, via any medium is strictly prohibited. Proprietary and confidential
 */

package ai.saiy.android.ui.containers;

import android.support.annotation.NonNull;

import java.io.Serializable;

import ai.saiy.android.custom.Custom;

/**
 * Created by benrandall76@gmail.com on 27/01/2017.
 */

public class ContainerCustomisation implements Serializable {

    private static final long serialVersionUID = -630509878907795203L;

    private final Custom custom;
    private String serialised;

    private String title;
    private String subtitle;

    private int iconMain;
    private int iconExtra;

    private long rowId;


    /**
     * Constructor
     *
     * @param custom     the {@link Custom} type
     * @param serialised the serialised string of the customisation
     * @param title      of the element
     * @param subtitle   of the element
     * @param iconMain   main icon
     * @param iconExtra  secondary icon
     */
    public ContainerCustomisation(@NonNull final Custom custom, @NonNull final String serialised, @NonNull final String title,
                                  @NonNull final String subtitle, final long rowId, final int iconMain, final int iconExtra) {
        this.custom = custom;
        this.serialised = serialised;
        this.title = title;
        this.subtitle = subtitle;
        this.iconMain = iconMain;
        this.iconExtra = iconExtra;
        this.rowId = rowId;
    }

    public long getRowId() {
        return rowId;
    }

    public Custom getCustom() {
        return custom;
    }

    public String getSerialised() {
        return serialised;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public int getIconMain() {
        return iconMain;
    }

    public int getIconExtra() {
        return iconExtra;
    }

    public void setIconExtra(final int iconExtra) {
        this.iconExtra = iconExtra;
    }

    public void setIconMain(final int iconMain) {
        this.iconMain = iconMain;
    }

    public void setRowId(final long rowId) {
        this.rowId = rowId;
    }

    public void setSerialised(final String serialised) {
        this.serialised = serialised;
    }

    public void setSubtitle(final String subtitle) {
        this.subtitle = subtitle;
    }

    public void setTitle(final String title) {
        this.title = title;
    }
}
