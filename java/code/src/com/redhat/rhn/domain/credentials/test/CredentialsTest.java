/*
 * Copyright (c) 2023 SUSE LLC
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
package com.redhat.rhn.domain.credentials.test;

import static com.redhat.rhn.domain.role.RoleFactory.ORG_ADMIN;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.SCCCredentials;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.UserTestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class CredentialsTest extends JMockBaseTestCaseWithUser {

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        UserTestUtils.addUserRole(user, ORG_ADMIN);
    }

    @Test
    public void testSCCCredentials() throws Exception {
        SCCCredentials sccCreds = CredentialsFactory.createSCCCredentials("admin", "secret");
        CredentialsFactory.storeCredentials(sccCreds);
        HibernateFactory.getSession().flush();

        Optional<SCCCredentials> loadedSccCreds = CredentialsFactory.lookupSCCCredentialsById(sccCreds.getId());
        SCCCredentials creds = loadedSccCreds.orElseThrow();

        assertEquals("admin", creds.getUsername());
        assertEquals("secret", creds.getPassword());

        Optional r = HibernateFactory.getSession()
                .createSQLQuery("select password from suseCredentials where username = 'admin';")
                .uniqueResultOptional();
        // this prove that we really store encoded content in DB
        assertEquals("c2VjcmV0", r.orElseThrow().toString());
    }
}
