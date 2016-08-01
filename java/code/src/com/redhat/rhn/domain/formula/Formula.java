/**
 * Copyright (c) 2016 SUSE LLC
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
package com.redhat.rhn.domain.formula;

import com.redhat.rhn.domain.org.Org;

/**
 * A formula belonging to a specific server.
 */
public class Formula {

    private Long id;
    private Long serverId;
    private Org org;
    private String formulaName;
    private String content;

    /**
     * The id of the formula.
     * @return an id.
     */
    public Long getId() {
        return id;
    }

    /**
     * Set the id
     * @param idIn the id
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }
    
    /**
     * Get the serverId of the server this formula belongs to.
     * @return the serverId.
     */
    public Long getServerId() {
        return serverId;
    }

    /**
     * Set the serverId this formula belongs to.
     * @param sidIn the serverId
     */
    public void setServerId(Long sidIn) {
        this.serverId = sidIn;
    }

    /**
     * Get the name of the formula. It's the same as the
     * name of the formula-folder.
     * @return the name
     */
    public String getFormulaName() {
        return formulaName;
    }

    /**
     * Set the name of the formula.
     * @param formulaNameIn the name
     */
    public void setFormulaName(String formulaNameIn) {
        this.formulaName = formulaNameIn;
    }

    /**
     * Get the organization to which this formula belongs.
     * @return the organization
     */
    public Org getOrg() {
        return org;
    }

    /**
     * Set the organization to which this formula belongs.
     * @param orgIn the organization
     */
    public void setOrg(Org orgIn) {
        this.org = orgIn;
    }
    
    /**
     * Get the content of this formula. The content is
     * a JSON-formatted String.
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * Set the content of this formula. The content
     * must be a JSON-formatted String
     * @param formulaNameIn the name
     */
    public void setContent(String contentIn) {
        this.content = contentIn;
    }
}
