/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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

package com.redhat.rhn.common.security.acl;

import org.apache.commons.lang3.StringUtils;

/**
 * Class to assist with creating Acls.  This Factory will setup
 * the Acl class as well as setup the default as well as the mixin AclHandlers
 * associated with the Acl.
 *
 * TODO - consider caching the Acl instances within the Factory so we don't have to
 * instantiate new ones each time. Not sure how to do this yet.
 *
 */
public class AclFactory {

    private final Access access;

    /**
     * hidden constructor
     * @param accessIn
     */
    public AclFactory(Access accessIn) {
        this.access = accessIn;
    }

    /**
     * Get an instance of an Acl
     * @param mixinsIn the String with a comma separated list of classnames
     * @return Acl created
     */
    public Acl getAcl(String mixinsIn) {
        Acl aclObj = new Acl();
        aclObj.registerHandler(access);

        // Add the mixin handlers as well.
        if (mixinsIn != null) {
            String[] mixin = StringUtils.split(mixinsIn, ",");
            for (String sIn : mixin) {
                if (!sIn.equals(Access.class.getName())) {
                    aclObj.registerHandler(StringUtils.trim(sIn));
                }
            }
        }
        return aclObj;
    }

}
