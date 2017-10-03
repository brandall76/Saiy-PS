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
 * Created by benrandall76@gmail.com on 06/08/2016.
 */

@Root(name = "subpod")
public class SubPod {

    public static final String DATA_UNAVAILABLE = "(data not available)";

    @Attribute(name = "title")
    private String title;

    @Element(name = "plaintext", required = false)
    private String plaintext;

    @Element(name = "imagesource", required = false)
    private String imagesource;

    public SubPod() {
    }

    public SubPod(@Element(name = "plaintext") final String plaintext,
                  @Attribute(name = "title") final String title,
                  @Element(name = "imagesource", required = false) final String imagesource) {
        this.plaintext = plaintext;
        this.title = title;
        this.imagesource = imagesource;
    }

    public String getImagesource() {
        return imagesource;
    }

    public String getPlaintext() {
        return plaintext;
    }

    public String getTitle() {
        return title;
    }
}
