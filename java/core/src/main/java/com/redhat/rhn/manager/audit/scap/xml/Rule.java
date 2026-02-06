/*
 * Copyright (c) 2017--2025 SUSE LLC
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
import org.simpleframework.xml.Element;

/**
 * Rule bean for unmarshalling SCAP reports.
 */
public class Rule {

    @Attribute(name = "id", required = false)
    private String id;

    @Element(name = "remediation", required = false)
    private String remediation;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param idIn the id to set
     */
    public void setId(String idIn) {
        this.id = idIn;
    }

    /**
     * @return the remediation
     */
    public String getRemediation() {
        return remediation;
    }

    /**
     * @param remediationIn the remediation to set
     */
    public void setRemediation(String remediationIn) {
        this.remediation = remediationIn;
    }
}

