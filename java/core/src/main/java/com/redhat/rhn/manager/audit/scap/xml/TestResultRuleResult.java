/*
 * Copyright (c) 2017 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.manager.audit.scap.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Bean used to unmarshall an intermediary SCAP report.
 */
@Root(name = "rr", strict = false)
public class TestResultRuleResult {

    @Attribute
    private String id;

    @ElementList(entry = "ident", inline = true, required = false)
    private List<TestResultRuleResultIdent> idents;

    /**
     * @return id to get
     */
    public String getId() {
        return id;
    }

    /**
     * @param idIn to set
     */
    public void setId(String idIn) {
        this.id = idIn;
    }

    /**
     * @return idents to get
     */
    public List<TestResultRuleResultIdent> getIdents() {
        return idents;
    }

    /**
     * @param identsIn to set
     */
    public void setIdents(List<TestResultRuleResultIdent> identsIn) {
        this.idents = identsIn;
    }
}
