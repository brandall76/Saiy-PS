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

package ai.saiy.android.cognitive.knowledge.provider.wolframalpha.parse;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

/**
 * Created by benrandall76@gmail.com on 07/08/2016.
 */

@Root(name = "alternative")
public class Alternative {

    @Attribute(name = "score", required = false)
    private double score;

    @Attribute(name = "level", required = false)
    private String level;

    private String text;

    public Alternative() {
    }

    public Alternative(@Attribute(name = "level", required = false) final String level,
                       @Attribute(name = "score", required = false) final double score,
                       @Text final String text) {
        this.level = level;
        this.score = score;
        this.text = text;
    }

    public String getLevel() {
        return level;
    }

    public double getScore() {
        return score;
    }

    @Text
    public String getText() {
        return text;
    }
}
