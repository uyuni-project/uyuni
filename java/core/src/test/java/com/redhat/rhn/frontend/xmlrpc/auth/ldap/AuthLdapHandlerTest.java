/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.frontend.xmlrpc.auth.ldap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.frontend.xmlrpc.BaseHandlerTestCase;
import com.redhat.rhn.frontend.xmlrpc.HandlerFactory;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class AuthLdapHandlerTest extends BaseHandlerTestCase {

    private AuthLdapHandler handler;

    @BeforeEach
    public void setUpHandler() {
        handler = new AuthLdapHandler();
    }

    @Test
    public void listRequiresSatAdmin() {
        assertThrows(PermissionCheckFailureException.class, () -> handler.list(regular));
        assertThrows(PermissionCheckFailureException.class, () -> handler.list(admin));
        List<Map<String, Object>> servers = handler.list(satAdmin);
        assertTrue(servers.isEmpty() || servers.stream().anyMatch(row -> row.containsKey("label")));
    }

    @Test
    public void namespaceIsRegistered() {
        assertEquals(AuthLdapHandler.class,
                HandlerFactory.getDefaultHandlerFactory().getHandler("auth.ldap").orElseThrow().getClass());
    }
}
