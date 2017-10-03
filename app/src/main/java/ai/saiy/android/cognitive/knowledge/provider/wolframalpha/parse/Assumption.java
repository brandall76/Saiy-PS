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
 * Created by benrandall76@gmail.com on 06/08/2016.
 */

@Root(name = "assumption")
public class Assumption {

    @Attribute(name = "type")
    private String type;

    @Attribute(name = "word", required = false)
    private String word;

    @Attribute(name = "template", required = false)
    private String template;

    @Attribute(name = "count")
    private long count;

    @Attribute(name = "desc", required = false)
    private String desc;

    @Attribute(name = "current", required = false)
    private long current;

    @ElementList(inline = true, name = "value")
    private List<Value> values;

    public Assumption() {
    }

    public Assumption(@Attribute(name = "count") final long count,
                      @Attribute(name = "template", required = false) final String template,
                      @Attribute(name = "type") final String type,
                      @ElementList(inline = true, name = "value") final List<Value> values,
                      @Attribute(name = "word", required = false) final String word,
                      @Attribute(name = "current", required = false) final long current,
                      @Attribute(name = "desc", required = false) final String desc) {
        this.count = count;
        this.template = template;
        this.type = type;
        this.values = values;
        this.word = word;
        this.current = current;
        this.desc = desc;
    }

    public long getCurrent() {
        return current;
    }

    public String getDesc() {
        return desc;
    }

    public long getCount() {
        return count;
    }

    public String getTemplate() {
        return template;
    }

    public String getType() {
        return type;
    }

    public List<Value> getValues() {
        return values;
    }

    public String getWord() {
        return word;
    }
}
