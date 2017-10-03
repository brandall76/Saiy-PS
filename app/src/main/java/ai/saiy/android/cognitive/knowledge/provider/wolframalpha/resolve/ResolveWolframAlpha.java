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

package ai.saiy.android.cognitive.knowledge.provider.wolframalpha.resolve;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.util.List;
import java.util.regex.Pattern;

import ai.saiy.android.cognitive.knowledge.provider.wolframalpha.parse.Assumption;
import ai.saiy.android.cognitive.knowledge.provider.wolframalpha.parse.Assumptions;
import ai.saiy.android.cognitive.knowledge.provider.wolframalpha.parse.Pod;
import ai.saiy.android.cognitive.knowledge.provider.wolframalpha.parse.QueryResult;
import ai.saiy.android.cognitive.knowledge.provider.wolframalpha.parse.Source;
import ai.saiy.android.cognitive.knowledge.provider.wolframalpha.parse.Sources;
import ai.saiy.android.cognitive.knowledge.provider.wolframalpha.parse.State;
import ai.saiy.android.cognitive.knowledge.provider.wolframalpha.parse.States;
import ai.saiy.android.cognitive.knowledge.provider.wolframalpha.parse.SubPod;
import ai.saiy.android.cognitive.knowledge.provider.wolframalpha.parse.ValidateQueryResult;
import ai.saiy.android.cognitive.knowledge.provider.wolframalpha.parse.Value;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsString;

/**
 * Created by benrandall76@gmail.com on 08/08/2016.
 */

public class ResolveWolframAlpha {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ResolveWolframAlpha.class.getSimpleName();

    private static final String DELIMITER = "\\s\\|";

    private List<Pod> podList;
    private WolframAlphaResponse wolframAlphaResponse = null;

    public boolean validate(@NonNull final String question) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "validate");
        }

        final Serializer serializer = new Persister();

        try {

            final ValidateQueryResult result = serializer.read(ValidateQueryResult.class, question, false);
            return result.passedValidation();

        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "Exception");
                e.printStackTrace();
            }
        }

        return false;
    }

    public Pair<Boolean, WolframAlphaResponse> resolve(@NonNull final WolframAlphaRequest request,
                                                       @NonNull final String xmlResponse) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "resolve");
        }

        final Serializer serializer = new Persister();

        try {

            final QueryResult queryResult = serializer.read(QueryResult.class, xmlResponse, false);

            if (isSuccess(queryResult)) {

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "result: " + queryResult.getId());
                    MyLog.i(CLS_NAME, "result: " + queryResult.getDatatypes());
                    MyLog.i(CLS_NAME, "result: " + queryResult.getHost());
                    MyLog.i(CLS_NAME, "result: " + queryResult.getRecalculate());
                    MyLog.i(CLS_NAME, "result: " + queryResult.getTimedout());
                    MyLog.i(CLS_NAME, "result: " + queryResult.getNumpods());
                    MyLog.i(CLS_NAME, "result: " + queryResult.getRelated());
                    MyLog.i(CLS_NAME, "result: " + queryResult.getTimedoutpods());
                    MyLog.i(CLS_NAME, "result: " + queryResult.isParsetimedout());
                    MyLog.i(CLS_NAME, "result: " + queryResult.getVersion());
                    MyLog.i(CLS_NAME, "result: " + queryResult.getTiming());
                    MyLog.i(CLS_NAME, "result: " + queryResult.getServer());
                }

                if (canResolveResponse(queryResult)) {

                    for (final Pod pod : podList) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "pod: " + pod.getId());
                            MyLog.i(CLS_NAME, "pod: " + pod.getScanner());
                            MyLog.i(CLS_NAME, "pod: " + pod.getTitle());
                            MyLog.i(CLS_NAME, "pod: " + pod.getNumsubpods());
                            MyLog.i(CLS_NAME, "pod: " + pod.getPosition());
                        }

                        final List<SubPod> subPodList = pod.getSubPods();

                        for (final SubPod subPod : subPodList) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "subPod: " + subPod.getTitle());
                                MyLog.i(CLS_NAME, "subPod: " + subPod.getPlaintext());
                            }
                        }

                        if (pod.hasStates()) {

                            final States states = pod.getStates();
                            final List<State> stateList = states.getState();

                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "states: " + states.getCount());
                            }

                            for (final State state : stateList) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "state: " + state.getInput());
                                    MyLog.i(CLS_NAME, "state: " + state.getName());
                                }
                            }

                        } else {
                            MyLog.i(CLS_NAME, "pod: no states");
                        }
                    }

                    if (queryResult.hasAssumptions()) {

                        final Assumptions assumptions = queryResult.getAssumptions();

                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "assumptions: " + assumptions.getCount());
                        }

                        final List<Assumption> assumptionList = assumptions.getAssumptions();

                        for (final Assumption assumption : assumptionList) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "assumption: " + assumption.getCount());
                                MyLog.i(CLS_NAME, "assumption: " + assumption.getTemplate());
                                MyLog.i(CLS_NAME, "assumption: " + assumption.getType());
                                MyLog.i(CLS_NAME, "assumption: " + assumption.getWord());
                            }

                            final List<Value> valueList = assumption.getValues();

                            for (final Value value : valueList) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "value: " + value.getDesc());
                                    MyLog.i(CLS_NAME, "value: " + value.getInput());
                                    MyLog.i(CLS_NAME, "value: " + value.getName());
                                }
                            }
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "result: no assumptions");
                        }
                    }

                    if (queryResult.hasSources()) {

                        final Sources sources = queryResult.getSources();

                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "sources: " + sources.getCount());
                        }

                        final List<Source> sourceList = sources.getSources();

                        for (final Source source : sourceList) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "source: " + source.getText());
                                MyLog.i(CLS_NAME, "source: " + source.getUrl());
                            }
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "result: no sources");
                        }
                    }

                    resolveResponse(podList);
                    wolframAlphaResponse.setQueryResult(queryResult);
                    wolframAlphaResponse.setQuestion(request.getQuery());
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "result: can't resolve response");
                    }
                }
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "result: isSuccess: false");
                }
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "Exception");
                e.printStackTrace();
            }
        }

        return new Pair<>(wolframAlphaResponse != null, wolframAlphaResponse);
    }

    private boolean isSuccess(@Nullable final QueryResult result) {
        return result != null && result.isSuccess() && !result.isError() && !result.noData();
    }

    private boolean canResolveResponse(@NonNull final QueryResult result) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "canResolveResponse");
        }

        if (result.hasPods()) {

            podList = result.getPods();
            if (haveInputPod(podList)) {
                if (haveResultPod(podList)) {
                    return true;
                } else {
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "canResolveResponse: no result pod");
                    }
                }
            } else {
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "canResolveResponse: no input pod");
                }
            }
        } else {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "canResolveResponse: no pods");
            }
        }

        return result.hasPods() && haveInputPod(result.getPods()) && haveResultPod(result.getPods());
    }

    private boolean haveResultPod(@NonNull final List<Pod> podList) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "haveResultPod");
        }

        String id;
        for (final Pod pod : podList) {
            id = pod.getId();
            if (UtilsString.notNaked(id) && id.matches(Pod.ID_RESULT)
                    && haveSubPodPlainText(pod)) {
                return true;
            }
        }

        return false;
    }

    private boolean haveInputPod(@NonNull final List<Pod> podList) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "haveInputPod");
        }

        String id;
        for (final Pod pod : podList) {
            id = pod.getId();
            if (UtilsString.notNaked(id) && Pod.ID_INPUT.matches(Pattern.quote(id))
                    && haveSubPodPlainText(pod)) {
                return true;
            }
        }

        return false;
    }

    private String getInputText(@NonNull final List<Pod> podList) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getInputText");
        }

        String id;
        for (final Pod pod : podList) {
            id = pod.getId();
            if (Pod.ID_INPUT.matches(Pattern.quote(id))) {
                return pod.getSubPods().get(0).getPlaintext();
            }
        }

        return null;
    }

    private String getResultText(@NonNull final List<Pod> podList) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getResultText");
        }

        String id;
        for (final Pod pod : podList) {
            id = pod.getId();
            if (Pod.ID_RESULT.matches(Pattern.quote(id))) {
                return pod.getSubPods().get(0).getPlaintext();
            }
        }

        return null;
    }

    private boolean haveSubPodPlainText(@NonNull final Pod pod) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "haveSubPodPlainText");
        }

        final List<SubPod> subPodList = pod.getSubPods();
        return UtilsList.notNaked(subPodList) && UtilsString.notNaked(subPodList.get(0).getPlaintext())
                && !SubPod.DATA_UNAVAILABLE.matches(Pattern.quote(subPodList.get(0).getPlaintext()));
    }

    private void resolveResponse(@NonNull final List<Pod> podList) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "resolveResponse");
        }

        wolframAlphaResponse = new WolframAlphaResponse();

        final String input = getInputText(podList);
        final String result = getResultText(podList);

        if (DEBUG) {
            MyLog.i(CLS_NAME, "input: " + input);
            MyLog.i(CLS_NAME, "result: " + result);
        }

        wolframAlphaResponse.setInterpretation(formatString(input));
        wolframAlphaResponse.setResult(formatString(result));

    }

    private String formatString(@NonNull final String toFormat) {
        return toFormat.replaceAll(DELIMITER, ",").replaceAll("\\s+", " ").trim() + ". ";
    }
}