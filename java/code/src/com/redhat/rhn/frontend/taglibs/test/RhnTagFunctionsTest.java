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
package com.redhat.rhn.frontend.taglibs.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.frontend.taglibs.RhnTagFunctions;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * RhnTagFunctionsTest
 */
public class RhnTagFunctionsTest extends RhnBaseTestCase {

    @Override
    @BeforeEach
    public void setUp() {
        TestUtils.disableLocalizationLogging();
    }

    @Test
    public void testConfig() {
        assertNotNull(RhnTagFunctions.getConfig("web.version"));
    }

    @Test
    public void testLocalize() {
        assertNotNull(RhnTagFunctions.localizedValue("test.string"));
        assertNotNull(RhnTagFunctions.localizedValue("test.string", "asdf|asdf"));
    }
}
