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
package com.redhat.rhn.domain.audit;

/**
 * XccdfIdent - Class representation of the table rhnXccdfIdent.
 */
public class XccdfIdent {

    private Long id;

    private XccdfIdentSystem identSystem;

    private String identifier;

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param idIn to set
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @return the identSystem
     */
    public XccdfIdentSystem getIdentSystem() {
        return identSystem;
    }

    /**
     * @param identSystemIn to set
     */
    public void setIdentSystem(XccdfIdentSystem identSystemIn) {
        this.identSystem = identSystemIn;
    }

    /**
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifierIn to set
     */
    public void setIdentifier(String identifierIn) {
        this.identifier = identifierIn;
    }
}
