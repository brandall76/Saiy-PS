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

import ai.saiy.android.cognitive.knowledge.provider.wolframalpha.parse.QueryResult;

/**
 * Class to package a response from Wolfram Alpha
 * <p>
 * Created by benrandall76@gmail.com on 09/08/2016.
 */

public class WolframAlphaResponse {

    private QueryResult queryResult;
    private String question;
    private String interpretation;
    private String result;

    public WolframAlphaResponse() {
    }

    /**
     * Constructor
     *
     * @param queryResult    a {@link QueryResult} object containing the full response
     * @param question       the question that was asked
     * @param interpretation the interpretation of the question by Wolfram Alpha
     * @param result         the results supplied by Wolfram Alpha
     */
    public WolframAlphaResponse(@NonNull final QueryResult queryResult, @NonNull final String question,
                                @NonNull final String interpretation, @NonNull final String result) {
        this.interpretation = interpretation;
        this.queryResult = queryResult;
        this.question = question;
        this.result = result;
    }

    public void setInterpretation(final String interpretation) {
        this.interpretation = interpretation;
    }

    public void setQueryResult(final QueryResult queryResult) {
        this.queryResult = queryResult;
    }

    public void setQuestion(final String question) {
        this.question = question;
    }

    public void setResult(final String result) {
        this.result = result;
    }

    public String getInterpretation() {
        return interpretation;
    }

    public QueryResult getQueryResult() {
        return queryResult;
    }

    public String getQuestion() {
        return question;
    }

    public String getResult() {
        return result;
    }
}
