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

@Root(name = "statelist")
public class StateList {

    @Attribute(name = "count")
    private long count;

    @Attribute(name = "value", required = false)
    private String value;

    @Attribute(name = "delimiters", required = false)
    private String delimiters;

    @ElementList(inline = true, name = "state")
    private List<State> state;

    public StateList() {
    }

    public StateList(@Attribute(name = "count") final long count,
                     @Attribute(name = "delimiters", required = false) final String delimiters,
                     @ElementList(inline = true, name = "state") final List<State> state,
                     @Attribute(name = "value", required = false) final String value) {
        this.count = count;
        this.delimiters = delimiters;
        this.state = state;
        this.value = value;
    }

    public long getCount() {
        return count;
    }

    public String getDelimiters() {
        return delimiters;
    }

    public List<State> getState() {
        return state;
    }

    public String getValue() {
        return value;
    }
}
