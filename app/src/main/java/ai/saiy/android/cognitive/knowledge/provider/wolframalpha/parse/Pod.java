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

import ai.saiy.android.utils.UtilsList;

/**
 * Created by benrandall76@gmail.com on 06/08/2016.
 */

@Root(name = "pod")
public class Pod {

    public static final String ID_INPUT = "Input";
    public static final String ID_RESULT = "Result";

    @Attribute(name = "title")
    private String title;

    @Attribute(name = "scanner")
    private String scanner;

    @Attribute(name = "id")
    private String id;

    @Attribute(name = "position")
    private long position;

    @Attribute(name = "primary", required = false)
    private boolean primary;

    @Attribute(name = "error")
    private boolean error;

    @Attribute(name = "numsubpods")
    private long numsubpods;

    @ElementList(inline = true, name = "subpods")
    private List<SubPod> subpods;

    @Element(name = "states", required = false)
    private States states;

    @Element(name = "infos", required = false)
    private Infos infos;

    @Element(name = "definitions", required = false)
    private Definitions definitions;

    public Pod() {
    }

    public Pod(@Attribute(name = "error") final boolean error,
               @Attribute(name = "primary") final boolean primary,
               @Attribute(name = "title") final String title,
               @Attribute(name = "scanner") final String scanner,
               @Attribute(name = "id") final String id,
               @Attribute(name = "position") final long position,
               @Attribute(name = "numsubpods") final long numsubpods,
               @ElementList(inline = true, name = "subpods") final List<SubPod> subpods,
               @Element(name = "states") final States states,
               @Element(name = "infos") final Infos infos,
               @Element(name = "definitions", required = false) final Definitions definitions) {
        this.error = error;
        this.title = title;
        this.scanner = scanner;
        this.id = id;
        this.position = position;
        this.numsubpods = numsubpods;
        this.subpods = subpods;
        this.primary = primary;
        this.states = states;
        this.infos = infos;
        this.definitions = definitions;
    }

    public boolean hasDefinitions() {
        return definitions != null;
    }

    public Definitions getDefinitions() {
        return definitions;
    }

    public boolean hasInfos() {
        return infos != null;
    }

    public Infos getInfos() {
        return infos;
    }

    public boolean hasStates() {
        return states != null;
    }

    public States getStates() {
        return states;
    }

    public boolean isPrimary() {
        return primary;
    }

    public boolean isError() {
        return error;
    }

    public String getId() {
        return id;
    }

    public long getNumsubpods() {
        return numsubpods;
    }

    public long getPosition() {
        return position;
    }

    public String getScanner() {
        return scanner;
    }

    public boolean hasSubPods() {
        return UtilsList.notNaked(subpods);
    }

    public List<SubPod> getSubPods() {
        return subpods;
    }

    public String getTitle() {
        return title;
    }
}
