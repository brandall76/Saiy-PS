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

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by benrandall76@gmail.com on 07/08/2016.
 */

@Root(name = "info")
public class Info {

    @Element(name = "link", required = false)
    private Link link;

    @Element(name = "units", required = false)
    private Units units;

    public Info() {
    }

    public Info(@Element(name = "link", required = false) final Link link,
                @Element(name = "units", required = false) final Units units) {
        this.link = link;
        this.units = units;
    }

    public boolean hasUnits() {
        return units != null;
    }

    public Units getUnits() {
        return units;
    }

    public boolean hasLink() {
        return link != null;
    }

    public Link getLink() {
        return link;
    }
}
