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

/**
 * Created by benrandall76@gmail.com on 07/08/2016.
 */

@Root(name = "definition")
public class Definition {

    @Attribute(name = "word", required = false)
    private String word;

    @Attribute(name = "desc", required = false)
    private String desc;

    public Definition() {
    }

    public Definition(@Attribute(name = "desc", required = false) final String desc,
                      @Attribute(name = "word", required = false) final String word) {
        this.desc = desc;
        this.word = word;
    }

    public String getDesc() {
        return desc;
    }

    public String getWord() {
        return word;
    }
}
