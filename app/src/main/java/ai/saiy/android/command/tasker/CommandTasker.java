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

package ai.saiy.android.command.tasker;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Pair;

import java.util.ArrayList;

import ai.saiy.android.algorithms.Algorithm;
import ai.saiy.android.api.request.SaiyRequestParams;
import ai.saiy.android.applications.Install;
import ai.saiy.android.applications.Installed;
import ai.saiy.android.applications.UtilsApplication;
import ai.saiy.android.command.helper.CommandRequest;
import ai.saiy.android.intent.ExecuteIntent;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.nlu.local.AlgorithmicContainer;
import ai.saiy.android.nlu.local.AlgorithmicResolver;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.thirdparty.tasker.TaskerHelper;
import ai.saiy.android.thirdparty.tasker.TaskerTask;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsString;

/**
 * Class to process a command request. Handles both remote NLP intents and falling back to
 * resolving locally.
 * <p/>
 * Created by benrandall76@gmail.com on 10/02/2016.
 */
public class CommandTasker {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandTasker.class.getSimpleName();

    private long then;

    /**
     * Resolve the required command returning an {@link Outcome} object
     *
     * @param ctx       the application context
     * @param voiceData ArrayList<String> containing the voice data
     * @param sl        the {@link SupportedLanguage} we are using to analyse the voice data.
     * @param cr        the {@link CommandRequest}
     * @return {@link Outcome} containing everything we need to respond to the command.
     */
    public Outcome getResponse(@NonNull final Context ctx, @NonNull final ArrayList<String> voiceData,
                               @NonNull final SupportedLanguage sl, @NonNull final CommandRequest cr) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData.toString());
        }

        then = System.nanoTime();

        final Outcome outcome = new Outcome();

        final TaskerHelper taskerHelper = new TaskerHelper();
        final Pair<Boolean, String> taskerPair = taskerHelper.isTaskerInstalled(ctx);
        String taskerPackage;

        if (taskerPair.first) {

            taskerPackage = taskerPair.second;

            if (DEBUG) {
                MyLog.i(CLS_NAME, "tasker installed: " + taskerPackage);
            }

            final Pair<Boolean, Boolean> taskerStatusPair = taskerHelper.canInteract(ctx);

            if (taskerStatusPair.first) {
                if (taskerStatusPair.second) {
                    if (taskerHelper.receiverExists(ctx)) {

                        final ArrayList<TaskerTask> taskList = taskerHelper.getTasks(ctx);

                        if (UtilsList.notNaked(taskList)) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "task count: " + taskList.size());
                            }

                            final ArrayList<String> taskNames = new ArrayList<>();
                            String taskName;

                            if (cr.isResolved()) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "isResolved: true");
                                }

                                final CommandTaskerValues cwav = (CommandTaskerValues) cr.getVariableData();
                                taskName = cwav.getTaskName();

                                if (UtilsString.notNaked(taskName)) {
                                    taskNames.add(taskName);
                                }

                            } else {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "isResolved: false");
                                }
                                taskNames.addAll(new CommandTaskerLocal().getResponse(ctx, voiceData, sl));
                            }

                            if (UtilsList.notNaked(taskNames)) {
                                if (DEBUG) {
                                    MyLog.v(CLS_NAME, "taskNames size: " + taskNames.size());
                                }

                                final ArrayList<String> taskNameList = new ArrayList<>(taskList.size());

                                for (final TaskerTask taskerTask : taskList) {
                                    taskNameList.add(taskerTask.getTaskName());
                                }

                                final AlgorithmicResolver resolver = new AlgorithmicResolver(ctx,
                                        Algorithm.getAlgorithms(ctx, sl), sl.getLocale(), taskNames,
                                        taskNameList, AlgorithmicResolver.THREADS_TIMEOUT_500, false);

                                final AlgorithmicContainer container = resolver.resolve();

                                if (container != null) {

                                    final boolean exactMatch = container.isExactMatch();
                                    if (DEBUG) {
                                        MyLog.d(CLS_NAME, "container exactMatch: " + exactMatch);
                                        MyLog.d(CLS_NAME, "container getInput: " + container.getInput());
                                        MyLog.d(CLS_NAME, "container getGenericMatch: " + container.getGenericMatch());
                                        MyLog.d(CLS_NAME, "container getAlgorithm: " + container.getAlgorithm().name());
                                        MyLog.d(CLS_NAME, "container getScore: " + container.getScore());
                                        MyLog.d(CLS_NAME, "container getParentPosition: " + container.getParentPosition());
                                        MyLog.d(CLS_NAME, "container getVariableData: " + container.getVariableData());
                                    }

                                    TaskerTask taskerTask = null;

                                    try {
                                        taskerTask = taskList.get(container.getParentPosition());
                                    } catch (final IndexOutOfBoundsException e) {
                                        if (DEBUG) {
                                            MyLog.w(CLS_NAME, "taskList IndexOutOfBoundsException");
                                            e.printStackTrace();
                                        }
                                    }

                                    if (taskerTask != null) {
                                        if (DEBUG) {
                                            MyLog.d(CLS_NAME, "taskerTask getProjectName: " + taskerTask.getProjectName());
                                            MyLog.d(CLS_NAME, "taskerTask getTaskName: " + taskerTask.getTaskName());
                                        }

                                        if (taskerHelper.executeTask(ctx, taskerTask.getTaskName())) {

                                            if (SPH.getAnnounceTasker(ctx)) {
                                                outcome.setUtterance(PersonalityResponse.getTaskerTaskExecutedResponse(
                                                        ctx, sl, taskerTask.getTaskName()));
                                            } else {
                                                if (DEBUG) {
                                                    MyLog.w(CLS_NAME, "taskerTask don't announce");
                                                }

                                                outcome.setUtterance(SaiyRequestParams.SILENCE);
                                            }

                                            outcome.setOutcome(Outcome.SUCCESS);

                                        } else {
                                            if (DEBUG) {
                                                MyLog.w(CLS_NAME, "taskerTask failed to execute");
                                            }

                                            outcome.setUtterance(PersonalityResponse.getTaskerTaskFailedResponse(ctx, sl,
                                                    taskerTask.getTaskName()));
                                            outcome.setOutcome(Outcome.FAILURE);
                                            return returnOutcome(outcome);
                                        }
                                    } else {
                                        if (DEBUG) {
                                            MyLog.w(CLS_NAME, "index out of bounds");
                                        }

                                        outcome.setUtterance(PersonalityResponse.getTaskerTaskNotMatchedResponse(ctx, sl));
                                        outcome.setOutcome(Outcome.FAILURE);
                                        return returnOutcome(outcome);
                                    }
                                } else {
                                    if (DEBUG) {
                                        MyLog.w(CLS_NAME, "failed to find a match");
                                    }

                                    outcome.setUtterance(PersonalityResponse.getTaskerTaskNotMatchedResponse(ctx, sl));
                                    outcome.setOutcome(Outcome.FAILURE);
                                    return returnOutcome(outcome);
                                }
                            } else {
                                if (DEBUG) {
                                    MyLog.w(CLS_NAME, "empty task name request");
                                }

                                outcome.setUtterance(PersonalityResponse.getTaskerTaskNotMatchedResponse(ctx, sl));
                                outcome.setOutcome(Outcome.FAILURE);
                                return returnOutcome(outcome);
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "no tasks");
                            }

                            outcome.setUtterance(PersonalityResponse.getTaskerNoTasksResponse(ctx, sl));
                            outcome.setOutcome(Outcome.FAILURE);
                            return returnOutcome(outcome);
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "no receiver");
                        }

                        final Bundle bundle = new Bundle();
                        bundle.putInt(ActivityHome.FRAGMENT_INDEX, ActivityHome.INDEX_FRAGMENT_BUGS);
                        ExecuteIntent.saiyActivity(ctx, ActivityHome.class, bundle, true);

                        outcome.setUtterance(PersonalityResponse.getTaskerInstallOrderResponse(ctx, sl));
                        outcome.setOutcome(Outcome.FAILURE);
                        return returnOutcome(outcome);
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "remote access error");
                    }

                    taskerHelper.showTaskerExternalAccess(ctx);

                    outcome.setUtterance(PersonalityResponse.getTaskerExternalAccessResponse(ctx, sl));
                    outcome.setOutcome(Outcome.FAILURE);
                    return returnOutcome(outcome);
                }
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "tasker disabled");
                }

                UtilsApplication.launchAppFromPackageName(ctx, taskerPackage);

                outcome.setUtterance(PersonalityResponse.getTaskerDisabledResponse(ctx, sl));
                outcome.setOutcome(Outcome.FAILURE);
                return returnOutcome(outcome);
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "tasker not installed");
            }

            Install.showInstallLink(ctx, Installed.PACKAGE_TASKER_MARKET);

            outcome.setUtterance(PersonalityResponse.getTaskerInstallResponse(ctx, sl));
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(outcome);
        }

        return returnOutcome(outcome);
    }

    /**
     * A single point of return to check the elapsed time for debugging.
     *
     * @param outcome the constructed {@link Outcome}
     * @return the constructed {@link Outcome}
     */
    private Outcome returnOutcome(@NonNull final Outcome outcome) {
        if (DEBUG) {
            MyLog.getElapsed(CommandTasker.class.getSimpleName(), then);
        }
        return outcome;
    }
}
