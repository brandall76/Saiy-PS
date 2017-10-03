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
import org.simpleframework.xml.Root;

/**
 * Created by benrandall76@gmail.com on 07/08/2016.
 */

@Root(name = "warnings")
public class Warnings {

    @Attribute(name = "count")
    private long count;

    @Element(name = "reinterpret", required = false)
    private Reinterpret reinterpret;

    @Element(name = "spellcheck", required = false)
    private SpellCheck spellcheck;

    public Warnings() {
    }

    public Warnings(@Attribute(name = "count") final long count,
                    @Element(name = "reinterpret", required = false) final Reinterpret reinterpret,
                    @Element(name = "spellcheck", required = false) final SpellCheck spellcheck) {
        this.count = count;
        this.reinterpret = reinterpret;
        this.spellcheck = spellcheck;
    }

    public boolean hasSpellCheck() {
        return spellcheck != null;
    }

    public SpellCheck getSpellcheck() {
        return spellcheck;
    }

    public long getCount() {
        return count;
    }

    public boolean hasReinterpret() {
        return reinterpret != null;
    }

    public Reinterpret getReinterpret() {
        return reinterpret;
    }
}
