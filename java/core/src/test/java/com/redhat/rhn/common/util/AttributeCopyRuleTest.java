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

package com.redhat.rhn.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;

import org.apache.commons.digester.Digester;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.Map;

public class AttributeCopyRuleTest extends RhnBaseTestCase {
    @Test
    public void testCopy() throws Exception {
        Digester digester = new Digester();
        digester.setValidating(false);

        digester.addObjectCreate("dummy", DummyObject.class);
        digester.addRule("dummy", new AttributeCopyRule());

        URL url = TestUtils.findTestData("dummy-test.xml");
        DummyObject result =
            (DummyObject)digester.parse(url.openStream());

        Map<String, String> expected = Map.of(
            "foo", "1",
            "bar", "baz");

        assertEquals(expected.size(), result.getValues().size());

        expected.forEach((key, value) -> {
            assertNotNull(result.getValues().get(key));
            assertEquals(result.getValues().get(key), value);
        });
    }
}


