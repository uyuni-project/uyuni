/**
 * Copyright (c) 2020 SUSE LLC
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

package com.redhat.rhn.domain.formula.test;

import com.redhat.rhn.domain.formula.FormulaFactory;
import junit.framework.TestCase;
import org.yaml.snakeyaml.Yaml;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public class FormulaFactoryTest extends TestCase {

    public void testGetValueByPath() {
        Map<String, Object> map = (Map<String, Object>)new Yaml().load(getClass().getResourceAsStream("provider-metadata.yml"));

        Optional<Object> val = FormulaFactory.getValueByPath(map, "cluster:management_node:match");
        assertTrue(val.isPresent());
        assertEquals("I@caasp:management_node:true", val.get());
    }

    public void testGetValueByPath_WrongPath() {
        Map<String, Object> map = (Map<String, Object>)new Yaml().load(getClass().getResourceAsStream("provider-metadata.yml"));

        Optional<Object> val = FormulaFactory.getValueByPath(map, "cluster:foo:bar");
        assertTrue(val.isEmpty());

        val = FormulaFactory.getValueByPath(map, "foo:bar");
        assertTrue(val.isEmpty());

        val = FormulaFactory.getValueByPath(map, ":xxx");
        assertTrue(val.isEmpty());

    }

}
