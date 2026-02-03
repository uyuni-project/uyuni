/*
 * Copyright (c) 2026 SUSE LLC
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

package com.redhat.rhn.domain.access.test;

import com.redhat.rhn.domain.access.Namespace;
import com.redhat.rhn.domain.access.NamespaceFactory;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NamespaceFactoryTest extends RhnBaseTestCase {

    @Test
    public void generatedCoverageTestList() {
        // this test has been generated programmatically to test NamespaceFactory.list
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        NamespaceFactory.list();
    }


    @Test
    public void generatedCoverageTestFind() {
        // this test has been generated programmatically to test NamespaceFactory.find
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        NamespaceFactory.find("");
    }


    @Test
    public void generatedCoverageTestFind2() {
        // this test has been generated programmatically to test NamespaceFactory.find
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        Set<Namespace.AccessMode> arg1 = new HashSet<>();
        NamespaceFactory.find("", arg1);
    }

    @Test
    public void generatedCoverageTestlistByIds() {
        // this test has been generated programmatically to test NamespaceFactory.
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        List<Long> arg1 = new ArrayList<>();
        NamespaceFactory.listByIds(arg1);
    }
}
