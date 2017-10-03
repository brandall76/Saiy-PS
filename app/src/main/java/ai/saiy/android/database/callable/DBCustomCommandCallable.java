/*
 * Copyright (c) 2017. Saiy® Ltd. All Rights Reserved.
 *
 * Unauthorised copying of this file, via any medium is strictly prohibited. Proprietary and confidential
 */

package ai.saiy.android.database.callable;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.custom.CustomCommandHelper;

/**
 * Created by benrandall76@gmail.com on 27/01/2017.
 */

public class DBCustomCommandCallable implements Callable<ArrayList<Object>> {

    private final Context mContext;

    public DBCustomCommandCallable(@NonNull final Context mContext) {
        this.mContext = mContext;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ArrayList<Object> call() throws Exception {
        return (ArrayList) new CustomCommandHelper().getCustomCommands(mContext);
    }
}
