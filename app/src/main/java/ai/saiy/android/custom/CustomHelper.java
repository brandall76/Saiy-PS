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

package ai.saiy.android.custom;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import ai.saiy.android.R;
import ai.saiy.android.applications.UtilsApplication;
import ai.saiy.android.database.callable.DBCustomCommandCallable;
import ai.saiy.android.ui.containers.ContainerCustomisation;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

/**
 * Created by benrandall76@gmail.com on 27/01/2017.
 */

public class CustomHelper {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CustomHelper.class.getSimpleName();

    private static final long THREADS_TIMEOUT = 1000L;

    public static final int CHEVRON_RESOURCE_ID = R.drawable.chevron;
    public static final int CUSTOM_COMMAND_RESOURCE_ID = R.drawable.ic_account_switch;

    private static final Object lock = new Object();

    public CustomHelperHolder getCustomisationHolder(@NonNull final Context ctx) {

        synchronized (lock) {

            final long then = System.nanoTime();

            final List<Callable<ArrayList<Object>>> callableList = new ArrayList<>();

            callableList.add(new DBCustomCommandCallable(ctx));

            final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            final ArrayList<Object> objectArray = new ArrayList<>();

            try {

                final List<Future<ArrayList<Object>>> futures = executorService.invokeAll(callableList,
                        THREADS_TIMEOUT, TimeUnit.MILLISECONDS);

                for (final Future<ArrayList<Object>> future : futures) {
                    objectArray.add(future.get());
                }

            } catch (final ExecutionException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "future: ExecutionException");
                    e.printStackTrace();
                }
            } catch (final CancellationException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "future: CancellationException");
                    e.printStackTrace();
                }
            } catch (final InterruptedException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "future: InterruptedException");
                    e.printStackTrace();
                }
            } finally {
                executorService.shutdown();
            }

            if (DEBUG) {
                MyLog.getElapsed(CLS_NAME, "callables", then);
            }

            final CustomHelperHolder holder;

            if (UtilsList.notNaked(objectArray)) {
                holder = completeCustomisationHolder(ctx, objectArray);
            } else {
                holder = new CustomHelperHolder();
            }

            if (DEBUG) {
                MyLog.getElapsed(CLS_NAME, then);
            }

            return holder;
        }
    }

    @SuppressWarnings("unchecked")
    private CustomHelperHolder completeCustomisationHolder(@NonNull final Context ctx,
                                                           @NonNull final ArrayList<Object> objectArray) {

        final CustomHelperHolder holder = new CustomHelperHolder();

        Object object;
        ArrayList<Object> tempArray;
        final int size = objectArray.size();

        for (int i = 0; i < size; i++) {

            tempArray = (ArrayList<Object>) objectArray.get(i);

            if (UtilsList.notNaked(tempArray)) {

                object = tempArray.get(0);

                if (object instanceof CustomCommandContainer) {
                    holder.setCustomCommandArray((ArrayList) tempArray);

                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "instanceof default");
                    }
                }
            }
        }

        return holder;
    }

    public ArrayList<ContainerCustomisation> getCustomisations(@NonNull final Context ctx) {

        synchronized (lock) {

            final long then = System.nanoTime();

            final List<Callable<ArrayList<Object>>> callableList = new ArrayList<>();
            final ArrayList<ContainerCustomisation> containerCustomisationArray = new ArrayList<>();

            callableList.add(new DBCustomCommandCallable(ctx));

            final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            final ArrayList<Object> objectArray = new ArrayList<>();

            try {

                final List<Future<ArrayList<Object>>> futures = executorService.invokeAll(callableList,
                        THREADS_TIMEOUT, TimeUnit.MILLISECONDS);

                for (final Future<ArrayList<Object>> future : futures) {
                    objectArray.addAll(future.get());
                }

            } catch (final ExecutionException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "future: ExecutionException");
                    e.printStackTrace();
                }
            } catch (final CancellationException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "future: CancellationException");
                    e.printStackTrace();
                }
            } catch (final InterruptedException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "future: InterruptedException");
                    e.printStackTrace();
                }
            } finally {
                executorService.shutdown();
            }

            if (DEBUG) {
                MyLog.getElapsed(CLS_NAME, "callables", then);
            }

            if (UtilsList.notNaked(objectArray)) {

                final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

                Object object;
                ContainerCustomisation containerCustomisation;
                CustomCommandContainer customCommandContainer;
                CustomCommand customCommand;
                String serialised;
                String extra = null;
                String label;
                Intent remoteIntent = null;

                final int size = objectArray.size();

                for (int i = 0; i < size; i++) {

                    object = objectArray.get(i);

                    if (object instanceof CustomCommandContainer) {

                        customCommandContainer = (CustomCommandContainer) object;

                        serialised = customCommandContainer.getSerialised();
                        customCommand = gson.fromJson(serialised, CustomCommand.class);

                        if (customCommand.getCustomAction() == CCC.CUSTOM_INTENT_SERVICE) {

                            try {
                                remoteIntent = Intent.parseUri(customCommand.getIntent(), 0);
                            } catch (final URISyntaxException e) {
                                if (DEBUG) {
                                    MyLog.w(CLS_NAME, "remoteIntent.parseUri: URISyntaxException");
                                    e.printStackTrace();
                                }
                            } catch (final NullPointerException e) {
                                if (DEBUG) {
                                    MyLog.w(CLS_NAME, "remoteIntent.parseUri: NullPointerException");
                                    e.printStackTrace();
                                }
                            }

                            if (remoteIntent != null) {

                                final Pair<Boolean, String> pair = UtilsApplication.getAppNameFromPackage(ctx,
                                        remoteIntent.getPackage());

                                if (pair.first) {
                                    extra = pair.second;
                                } else {
                                    extra = remoteIntent.getPackage();
                                }
                            } else {
                                if (DEBUG) {
                                    MyLog.w(CLS_NAME, "remoteIntent null");
                                }
                                extra = ctx.getString(R.string.an_unknown_application);
                            }

                            label = customCommand.getCustomAction().name();

                        } else {
                            label = customCommand.getCustomAction().name();
                        }

                        containerCustomisation = new ContainerCustomisation(Custom.CUSTOM_COMMAND,
                                serialised,
                                customCommandContainer.getKeyphrase(),
                                label,
                                customCommandContainer.getRowId(),
                                CUSTOM_COMMAND_RESOURCE_ID, CHEVRON_RESOURCE_ID);

                        containerCustomisationArray.add(containerCustomisation);

                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "instanceof default");
                        }
                    }
                }
            }


            if (DEBUG) {
                MyLog.getElapsed(CLS_NAME, then);
            }

            return containerCustomisationArray;

        }
    }
}
