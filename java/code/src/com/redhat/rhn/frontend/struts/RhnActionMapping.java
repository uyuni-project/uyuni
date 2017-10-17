/**
 * Copyright (c) 2009--2011 Red Hat, Inc.
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
package com.redhat.rhn.frontend.struts;

import org.apache.struts.action.ActionMapping;

/**
 * RhnActionMapping
 * @version $Rev$
 */
public class RhnActionMapping extends ActionMapping {

    private String acls;
    private String mixins;
    private String postRequired;
    private String postRequiredIfSubmitted;


    /**
     * @return Returns the mixins.
     */
    public String getMixins() {
        return mixins;
    }

    /**
     * @param mixinsIn The mixins to set.
     */
    public void setMixins(String mixinsIn) {
        this.mixins = mixinsIn;
    }

    /**
     * @return Returns the acls.
     */
    public String getAcls() {
        return acls;
    }

    /**
     * Takes a comma delimited string and populates aclList
     * @param aclsIn The acls to set.
     */
    public void setAcls(String aclsIn) {
        this.acls = aclsIn;
    }

    /**
     * @return Returns the postRequired attribute.
     */
    public String getPostRequired() {
        return postRequired;
    }

    /**
     * @param postRequiredIn The postRequired attribute to set.
     */
    public void setPostRequired(String postRequiredIn) {
        this.postRequired = postRequiredIn;
    }

    /**
     * @return true if postRequired attribute is present and set to "true", false otherwise
     */
    public boolean postRequired() {
        return postRequired != null && postRequired.equals("true");
    }

    /**
     * @return Returns the postRequiredIfSubmitted attribute.
     */
    public String getPostRequiredIfSubmitted() {
        return postRequiredIfSubmitted;
    }

    /**
     * @param postRequiredIfSubmittedIn The postRequiredIfSubmitted attribute to set.
     */
    public void setPostRequiredIfSubmitted(String postRequiredIfSubmittedIn) {
        this.postRequiredIfSubmitted = postRequiredIfSubmittedIn;
    }

    /**
     * @return true if postRequiredIfSubmitted attribute is present and set to "true"
     */
    public boolean postRequiredIfSubmitted() {
        return postRequiredIfSubmitted != null && postRequiredIfSubmitted.equals("true");
    }
}
