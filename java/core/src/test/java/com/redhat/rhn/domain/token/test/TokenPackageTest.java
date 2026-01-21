/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.domain.token.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageName;
import com.redhat.rhn.domain.rhnpackage.test.PackageNameTest;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.TokenPackage;
import com.redhat.rhn.domain.token.TokenPackageFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * TokenPackageTest
 */
public class TokenPackageTest extends BaseTestCaseWithUser {

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
    }

    @Test
    public void testTokenPackage() throws Exception {

        ActivationKey key = ActivationKeyTest.createTestActivationKey(user);
        TokenPackage pkg = createTestPackage(key);
        assertNotNull(pkg);

        //make sure we got written to the db
        assertNotNull(pkg.getId());

        TokenPackage lookup = TokenPackageFactory.lookupPackage(pkg.getToken(),
                pkg.getPackageName(), pkg.getPackageArch());

        assertNotNull(lookup);
        assertEquals(pkg, lookup);
        assertNotNull(lookup.getToken());
        assertNotNull(lookup.getPackageName());
        assertNotNull(lookup.getPackageArch());
    }

    public static TokenPackage createTestPackage(ActivationKey key)
        throws Exception {

        TokenPackage p = new TokenPackage();

        p = populateTestPackage(key, p);
        TestUtils.saveAndFlush(p);

        return p;
    }

    public static TokenPackage populateTestPackage(ActivationKey key,
            TokenPackage p) {

        PackageName pname = PackageNameTest.createTestPackageName();

        Long testid = 100L;
        PackageArch parch = HibernateFactory.getSession().createNativeQuery("""
                SELECT p.* from rhnPackageArch as p WHERE p.id = :id
                """, PackageArch.class).setParameter("id", testid).getSingleResult();

        p.setToken(key.getToken());
        p.setPackageName(pname);
        p.setPackageArch(parch);
        key.getPackages().add(p);

        return p;
    }
}
