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
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by benrandall76@gmail.com on 07/08/2016.
 */

@Root(name = "reinterpret")
public class Reinterpret {

    @Attribute(name = "text", required = false)
    private String text;

    @Attribute(name = "new", required = false)
    private String replaced;

    @Attribute(name = "score", required = false)
    private double score;

    @Attribute(name = "level", required = false)
    private String level;

    @ElementList(inline = true, name = "alternative", required = false)
    private List<Alternative> alternatives;

    public Reinterpret() {
    }

    public Reinterpret(@Attribute(name = "level", required = false) final String level,
                       @Attribute(name = "new", required = false) final String replaced,
                       @Attribute(name = "score", required = false) final double score,
                       @Attribute(name = "text", required = false) final String text,
                       @ElementList(inline = true, name = "alternative", required = false)
                       final List<Alternative> alternatives) {
        this.level = level;
        this.replaced = replaced;
        this.score = score;
        this.text = text;
        this.alternatives = alternatives;
    }

    public boolean haveAlternatives() {
        return alternatives != null;
    }

    public List<Alternative> getAlternatives() {
        return alternatives;
    }

    public String getLevel() {
        return level;
    }

    public String getReplaced() {
        return replaced;
    }

    public double getScore() {
        return score;
    }

    public String getText() {
        return text;
    }
}
