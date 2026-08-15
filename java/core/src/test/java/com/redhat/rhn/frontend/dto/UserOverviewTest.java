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
package com.redhat.rhn.frontend.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.testing.BaseTestCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * UserOverviewTest
 */
public class UserOverviewTest extends BaseTestCase {
    private UserOverview uo;

    @BeforeEach
    public void setUp() {
        uo = new UserOverview();
    }

    @Test
    public void testHtmlEscapeOfLogin() {
        assertUserOverview("hello<sometext>user", "hello&lt;sometext&gt;user");
        assertUserOverview("jesusr_redhat", "jesusr_redhat");
        assertUserOverview("joe&me", "joe&amp;me");
        assertUserOverview("joe me", "joe me");
    }

    @Test
    public void authorizedByShowsInternalForLocalUsers() {
        LocalizationService ls = LocalizationService.getInstance();
        uo.setAuthType("LOCAL");
        assertEquals(ls.getMessage("userlist.jsp.authorized.internal"), uo.getAuthorizedBy());

        uo.setAuthType(null);
        assertEquals(ls.getMessage("userlist.jsp.authorized.internal"), uo.getAuthorizedBy());
    }

    @Test
    public void authorizedByShowsPamAndLdapSources() {
        LocalizationService ls = LocalizationService.getInstance();

        uo.setAuthType("PAM");
        assertEquals(ls.getMessage("userlist.jsp.authorized.pam"), uo.getAuthorizedBy());

        uo.setAuthType("LDAP");
        assertEquals(ls.getMessage("userlist.jsp.authorized.ldap"), uo.getAuthorizedBy());

        uo.setLdapServerLabel("dev-openldap");
        assertEquals(ls.getMessage("userlist.jsp.authorized.ldap_server", "dev-openldap"),
                uo.getAuthorizedBy());
    }

    private void assertUserOverview(String login, String compare) {
        uo.setUserLogin(login);
        assertEquals(compare, uo.getUserLogin());
    }
}
