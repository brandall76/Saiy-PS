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

@Root(name = "queryresult") //, strict = false
public class QueryResult {

    @Attribute(name = "success")
    private boolean success;

    @Attribute(name = "error")
    private boolean error;

    @Attribute(name = "nodata", required = false)
    private boolean noData;

    @Attribute(name = "numpods")
    private int numpods;

    @Attribute(name = "datatypes")
    private String datatypes;

    @Attribute(name = "timedout")
    private String timedout;

    @Attribute(name = "timedoutpods")
    private String timedoutpods;

    @Attribute(name = "timing")
    private double timing;

    @Attribute(name = "parsetimedout")
    private boolean parsetimedout;

    @Attribute(name = "parsetiming")
    private double parsetiming;

    @Attribute(name = "recalculate")
    private String recalculate;

    @Attribute(name = "id")
    private String id;

    @Attribute(name = "host")
    private String host;

    @Attribute(name = "server")
    private int server;

    @Attribute(name = "related")
    private String related;

    @Attribute(name = "version")
    private double version;

    @ElementList(inline = true, name = "pod")
    private List<Pod> pods;

    @Element(name = "assumptions", required = false)
    private Assumptions assumptions;

    @Element(name = "sources", required = false)
    private Sources sources;

    @Element(name = "warnings", required = false)
    private Warnings warnings;

    public QueryResult() {
    }

    public QueryResult(@Attribute(name = "datatypes") final String datatypes,
                       @Attribute(name = "success") final boolean success,
                       @Attribute(name = "nodata") final boolean noData,
                       @Attribute(name = "error") final boolean error,
                       @Attribute(name = "numpods") final int numpods,
                       @Attribute(name = "timedout") final String timedout,
                       @Attribute(name = "timedoutpods") final String timedoutpods,
                       @Attribute(name = "timing") final double timing,
                       @Attribute(name = "parsetimedout") final boolean parsetimedout,
                       @Attribute(name = "parsetiming") final double parsetiming,
                       @Attribute(name = "recalculate") final String recalculate,
                       @Attribute(name = "id") final String id,
                       @Attribute(name = "host") final String host,
                       @Attribute(name = "server") final int server,
                       @Attribute(name = "related") final String related,
                       @Attribute(name = "version") final double version,
                       @ElementList(inline = true, name = "pod") final List<Pod> pods,
                       @Element(name = "assumptions") final Assumptions assumptions,
                       @Element(name = "sources", required = false) final Sources sources,
                       @Element(name = "warnings", required = false) final Warnings warnings) {
        this.datatypes = datatypes;
        this.success = success;
        this.noData = noData;
        this.error = error;
        this.numpods = numpods;
        this.timedout = timedout;
        this.timedoutpods = timedoutpods;
        this.timing = timing;
        this.parsetimedout = parsetimedout;
        this.parsetiming = parsetiming;
        this.recalculate = recalculate;
        this.id = id;
        this.host = host;
        this.server = server;
        this.related = related;
        this.version = version;
        this.pods = pods;
        this.assumptions = assumptions;
        this.sources = sources;
        this.warnings = warnings;
    }

    public boolean noData() {
        return noData;
    }

    public Warnings getWarnings() {
        return warnings;
    }

    public Sources getSources() {
        return sources;
    }

    public boolean hasSources() {
        return sources != null;
    }

    public Assumptions getAssumptions() {
        return assumptions;
    }

    public boolean hasAssumptions() {
        return assumptions != null;
    }

    public String getDatatypes() {
        return datatypes;
    }

    public boolean isError() {
        return error;
    }

    public String getHost() {
        return host;
    }

    public String getId() {
        return id;
    }

    public int getNumpods() {
        return numpods;
    }

    public boolean isParsetimedout() {
        return parsetimedout;
    }

    public double getParsetiming() {
        return parsetiming;
    }

    public boolean hasPods() {
        return UtilsList.notNaked(pods);
    }

    public List<Pod> getPods() {
        return pods;
    }

    public String getRecalculate() {
        return recalculate;
    }

    public String getRelated() {
        return related;
    }

    public int getServer() {
        return server;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getTimedout() {
        return timedout;
    }

    public String getTimedoutpods() {
        return timedoutpods;
    }

    public double getTiming() {
        return timing;
    }

    public double getVersion() {
        return version;
    }
}
