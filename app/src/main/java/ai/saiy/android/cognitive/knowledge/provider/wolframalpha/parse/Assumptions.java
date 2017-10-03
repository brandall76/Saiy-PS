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

@Root(name = "assumptions")
public class Assumptions {

    @Attribute(name = "count")
    private long count;

    @ElementList(inline = true, name = "assumption")
    private List<Assumption> assumptions;

    public Assumptions() {
    }

    public Assumptions(@ElementList(inline = true, name = "assumption") final List<Assumption> assumptions,
                       @Attribute(name = "count") final long count) {
        this.assumptions = assumptions;
        this.count = count;
    }

    public long getCount() {
        return count;
    }

    public List<Assumption> getAssumptions() {
        return assumptions;
    }
}
