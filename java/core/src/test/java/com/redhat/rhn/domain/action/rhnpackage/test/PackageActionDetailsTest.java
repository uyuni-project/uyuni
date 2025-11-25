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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.action.rhnpackage.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.rhnpackage.PackageAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageActionDetails;
import com.redhat.rhn.domain.action.rhnpackage.PackageActionResult;
import com.redhat.rhn.domain.action.rhnpackage.PackageRefreshListAction;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageName;
import com.redhat.rhn.domain.rhnpackage.test.PackageEvrFactoryTest;
import com.redhat.rhn.domain.rhnpackage.test.PackageNameTest;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * PackageActionDetailsTest
 */
public class PackageActionDetailsTest extends RhnBaseTestCase {

    @Test
    public void testBeanMethods() {
        PackageActionDetails pad = new PackageActionDetails();
        Long id = 456L;
        Date now = new Date();
        String foo = "foo";


        Long testid = 100L;

        PackageArch arch = HibernateFactory.getSession().createNativeQuery("""
                SELECT p.* from rhnPackageArch as p WHERE p.id = :id
                """, PackageArch.class).setParameter("id", testid).getSingleResult();

        PackageEvr evr = PackageEvrFactoryTest.createTestPackageEvr();
        PackageName pn = PackageNameTest.createTestPackageName();
        PackageAction action = new PackageRefreshListAction();

        pad.setCreated(now);
        assertEquals(now, pad.getCreated());

        pad.setModified(now);
        assertEquals(now, pad.getModified());

        pad.setPackageId(id);
        assertEquals(id, pad.getPackageId());

        pad.setParameter(foo);
        assertEquals(foo, pad.getParameter());

        pad.setArch(arch);
        assertEquals(arch, pad.getArch());

        pad.setEvr(evr);
        assertEquals(evr, pad.getEvr());

        pad.setPackageName(pn);
        assertEquals(pn, pad.getPackageName());

        pad.setParentAction(action);
        assertEquals(action, pad.getParentAction());
    }

    @Test
    public void testResultSetting() {
        PackageActionDetails pad = new PackageActionDetails();
        pad.setParentAction(new Action());
        PackageActionResult par = new PackageActionResult();
        PackageActionResult par1 = new PackageActionResult();
        PackageActionResult par2 = new PackageActionResult();
        par.setResultCode(20L);
        par1.setResultCode(40L); //so that none are equal

        assertNotNull(pad.getResults());
        pad.addResult(par);
        assertEquals(1, pad.getResults().size());
        assertNotNull(pad.getResults().toArray()[0]);
        assertEquals(par, pad.getResults().toArray()[0]);

        pad.addResult(par1);
        assertEquals(2, pad.getResults().size());
        assertFalse(pad.getResults().contains(null));
        assertTrue(pad.getResults().contains(par1));

        Set results = new HashSet<>();
        results.add(par);
        results.add(par1);
        results.add(par2);

        pad.setResults(results);
        assertEquals(3, pad.getResults().size());
        assertEquals(results, pad.getResults());
    }

    @Test
    public void testEquals() {
        PackageActionDetails pad = new PackageActionDetails();
        PackageActionDetails pad1 = new PackageActionDetails();

        Action parent = new Action();
        Action parent1 = new Action();
        parent.setId(3L);
        parent1.setId(2L);


        assertEquals(pad, pad1);

        pad.setParentAction(parent);
        assertNotEquals(pad, pad1);
        assertNotEquals(pad1, pad);

        pad1.setParentAction(parent1);
        assertNotEquals(pad, pad1);

        parent1.setId(3L);
        assertEquals(pad, pad1);

        pad1.setParentAction(parent);
        assertEquals(pad, pad1);

        pad.setPackageId(2L);
        assertNotEquals(pad, pad1);
        assertNotEquals(pad1, pad);

        pad1.setPackageId(3L);
        assertNotEquals(pad, pad1);

        pad.setPackageId(3L);
        assertEquals(pad, pad1);

    }

    // Some PackageActionDetails objects have package name only
    public static PackageActionDetails createTestDetailsWithName(User user, Action parent)
        throws Exception {

        PackageActionDetails pad = new PackageActionDetails();

        pad.setParameter("upgrade");
        Long testid = 100L;

        pad.setArch((HibernateFactory.getSession().createNativeQuery("""
                SELECT p.* from rhnPackageArch as p WHERE p.id = :id
                """, PackageArch.class).setParameter("id", testid).getSingleResult()));
        pad.setPackageName(PackageNameTest.createTestPackageName());

        ((PackageAction) parent).addDetail(pad);
        //add parent before result because parent needed for hashcode

        PackageActionResult par = new PackageActionResult();
        par.setServer(ServerFactoryTest.createTestServer(user));
        par.setResultCode(3L);
        par.setCreated(new Date());
        par.setModified(new Date());
        pad.addResult(par);

        return pad;
    }

    // Some PackageActionDetails objects have package name and package evr
    public static PackageActionDetails createTestDetailsWithNvre(User user, Action parent)
                                                                    throws Exception {

        PackageActionDetails pad = new PackageActionDetails();

        pad.setParameter("upgrade");
        Long testid = 100L;
        pad.setArch(HibernateFactory.getSession().createNativeQuery("""
                SELECT p.* from rhnPackageArch as p WHERE p.id = :id
                """, PackageArch.class).setParameter("id", testid).getSingleResult());
        pad.setPackageName(PackageNameTest.createTestPackageName());
        pad.setEvr(PackageEvrFactoryTest.createTestPackageEvr());

        ((PackageAction) parent).addDetail(pad);
        //add parent before result because parent needed for hashcode

        PackageActionResult par = new PackageActionResult();
        par.setServer(ServerFactoryTest.createTestServer(user));
        par.setResultCode(3L);
        par.setCreated(new Date());
        par.setModified(new Date());
        pad.addResult(par);

        return pad;

    }
}
