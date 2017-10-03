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
 * Created by benrandall76@gmail.com on 06/08/2016.
 */

@Root(name = "value")
public class Value {

    @Attribute(name = "name")
    private String name;

    @Attribute(name = "desc")
    private String desc;

    @Attribute(name = "input")
    private String input;

    @Attribute(name = "word", required = false)
    private String word;

    @Attribute(name = "valid", required = false)
    private boolean valid;

    public Value() {
    }

    public Value(@Attribute(name = "input") final String input,
                 @Attribute(name = "name") final String name,
                 @Attribute(name = "desc") final String desc,
                 @Attribute(name = "word") final String word,
                 @Attribute(name = "valid", required = false) final boolean valid) {
        this.input = input;
        this.name = name;
        this.desc = desc;
        this.word = word;
        this.valid = valid;
    }

    public boolean isValid() {
        return valid;
    }

    public String getWord() {
        return word;
    }

    public String getDesc() {
        return desc;
    }

    public String getInput() {
        return input;
    }

    public String getName() {
        return name;
    }
}
