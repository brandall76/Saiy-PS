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

package ai.saiy.android.nlu.local;

import android.support.annotation.NonNull;

import ai.saiy.android.algorithms.Algorithm;

/**
 * Created by benrandall76@gmail.com on 11/08/2016.
 */

public class AlgorithmicContainer {

    private boolean exactMatch;
    private double score;
    private String input;
    private String genericMatch;
    private Algorithm algorithm;
    private Object variableData;
    private int parentPosition;

    public int getParentPosition() {
        return parentPosition;
    }

    public void setParentPosition(final int parentPosition) {
        this.parentPosition = parentPosition;
    }

    public String getInput() {
        return input;
    }

    public void setInput(@NonNull final String input) {
        this.input = input;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(@NonNull final Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    public boolean isExactMatch() {
        return exactMatch;
    }

    public void setExactMatch(final boolean exactMatch) {
        this.exactMatch = exactMatch;
    }

    public String getGenericMatch() {
        return genericMatch;
    }

    public void setGenericMatch(@NonNull final String genericMatch) {
        this.genericMatch = genericMatch;
    }

    public double getScore() {
        return score;
    }

    public void setScore(final double score) {
        this.score = score;
    }

    public Object getVariableData() {
        return variableData;
    }

    public void setVariableData(@NonNull final Object variableData) {
        this.variableData = variableData;
    }
}
