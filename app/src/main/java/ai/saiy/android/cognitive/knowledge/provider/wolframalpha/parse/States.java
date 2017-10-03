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
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by benrandall76@gmail.com on 06/08/2016.
 */

@Root(name = "states")
public class States {

    @Attribute(name = "count")
    private long count;

    @Element(name = "statelist", required = false)
    private StateList stateList;

    @ElementList(inline = true, name = "state")
    private List<State> state;

    public States() {
    }

    public States(@Attribute(name = "count") final long count,
                  @ElementList(inline = true, name = "state") final List<State> state,
                  @Element(name = "statelist", required = false) final StateList stateList) {
        this.count = count;
        this.state = state;
        this.stateList = stateList;
    }

    public boolean hasStateList() {
        return stateList != null;
    }

    public StateList getStateList() {
        return stateList;
    }

    public long getCount() {
        return count;
    }

    public List<State> getState() {
        return state;
    }
}
