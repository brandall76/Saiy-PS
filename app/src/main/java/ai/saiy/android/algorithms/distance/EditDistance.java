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
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 *      Licensed to the Apache Software Foundation (ASF) under one or more
 *      contributor license agreements.  See the NOTICE file distributed with
 *      this work for additional information regarding copyright ownership.
 *      The ASF licenses this file to You under the Apache License, Version 2.0
 *      (the "License"); you may not use this file except in compliance with
 *      the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package ai.saiy.android.algorithms.distance;

/**
 * Created by benrandall76@gmail.com on 21/04/2016.
 */

/**
 * Interface for <a href="http://en.wikipedia.org/wiki/Edit_distance">Edit Distances</a>.
 * <p/>
 * A edit distance measures the similarity between two character sequences. Closer strings
 * have shorter distances, and vice-versa.
 * <p>
 * This is a BiFunction CharSequence, CharSequence.
 * <p>
 * The <code>apply</code> method accepts a pair of {@link CharSequence} parameters
 * and returns an <code>R</code> type similarity score.
 * </p>
 *
 * @param <R> The type of similarity score unit used by this EditDistance.
 */
public interface EditDistance<R> {

    /**
     * Compares two CharSequences.
     *
     * @param left  the first CharSequence
     * @param right the second CharSequence
     * @return the similarity score between two CharSequences
     */
    R apply(final CharSequence left, final CharSequence right);

}
