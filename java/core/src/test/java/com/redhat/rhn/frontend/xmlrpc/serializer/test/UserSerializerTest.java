/*
 * Copyright (c) 2024 SUSE LLC
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

package com.redhat.rhn.frontend.xmlrpc.serializer.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.frontend.xmlrpc.serializer.UserSerializer;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.io.Writer;

import redstone.xmlrpc.XmlRpcSerializer;

/**
 * UserSerializer test
 */
public class UserSerializerTest extends BaseTestCaseWithUser {

    /**
     * {@inheritDoc}
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Minimal test
     */
    @Test
    public void testSerializeUser() {
        UserSerializer serializer = new UserSerializer();
        Writer output = new StringWriter();
        serializer.serialize(user, output, new XmlRpcSerializer());

        String actual = output.toString();
        assertTrue(actual.contains("<member><name>id</name><value><i4>" +
                user.getId() + "</i4></value></member>"));
        assertTrue(actual.contains("<member><name>login</name><value><string>" +
                user.getLogin() + "</string></value></member>"));
        assertTrue(actual.contains("<member><name>login_uc</name><value><string>" +
                user.getLoginUc() + "</string></value></member>"));
        assertTrue(actual.contains("<member><name>enabled</name><value><boolean>" +
                String.valueOf(user.isDisabled() ? 0 : 1) + "</boolean></value></member>"));
    }
}
