/*
 * Copyright (c) 2021 SUSE LLC
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

package com.suse.utils.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.suse.utils.Maps;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;
import java.util.Optional;

public class MapsTest  {

    @Test
    public void testGetValueByPath() {
        Map<String, Object> map = new Yaml().load(getClass().getResourceAsStream("provider-metadata.yml"));

        Optional<Object> val = Maps.getValueByPath(map, "cluster:management_node:match");
        assertTrue(val.isPresent());
        assertEquals("I@caasp:management_node:true", val.get());
    }

    @Test
    public void testGetValueByPathWrongPath() {
        Map<String, Object> map = new Yaml().load(getClass().getResourceAsStream("provider-metadata.yml"));

        Optional<Object> val = Maps.getValueByPath(map, "cluster:foo:bar");
        assertTrue(val.isEmpty());

        val = Maps.getValueByPath(map, "foo:bar");
        assertTrue(val.isEmpty());

        val = Maps.getValueByPath(map, ":xxx");
        assertTrue(val.isEmpty());
    }
}
