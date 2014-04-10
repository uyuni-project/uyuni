/**
 * Copyright (c) 2014 SUSE
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

package com.suse.manager.model.ncc;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Root element of subscriptions list as sent by NCC.
 */
@Root(name = "listsubscriptions")
public class ListSubscriptions {

    @Attribute(name = "xmlns")
    private static final String XMLNS = "http://www.novell.com/xml/center/regsvc-1_0";

    @Attribute(name = "lang")
    private static final String LANG = "en";

    @Attribute(name = "includeall")
    private static final String INCLUDEALL = "no";

    // TODO: Read as 'username' from /etc/zypp/credentials.d/NCCcredentials
    @Element(name = "smtguid")
    private static final String SMTGUID = "FIXME";

    @Element(name = "authuser")
    private String user;

    @Element(name = "authpass")
    private String password;

    /**
     * Get the user.
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * Set the user
     * @param userIn the user to set
     */
    public void setUser(String userIn) {
        this.user = userIn;
    }

    /**
     * Get the password.
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the password.
     * @param passwordIn the password to set
     */
    public void setPassword(String passwordIn) {
        this.password = passwordIn;
    }
}
