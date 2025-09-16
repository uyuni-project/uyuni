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
package com.redhat.rhn.frontend.xmlrpc.system.custominfo.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.frontend.dto.CustomDataKeyOverview;
import com.redhat.rhn.frontend.xmlrpc.system.custominfo.CustomInfoHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.manager.system.SystemManager;

import org.junit.jupiter.api.Test;

/**
 * CustomInfoHandlerTest
 */
public class CustomInfoHandlerTest extends BaseHandlerTestCase {

    private CustomInfoHandler handler = new CustomInfoHandler();

    @Test
    public void testCreateKey() {

        // default setup already includes a custom key; therefore, let's
        // grab the initial size
        int initialSize = SystemManager.listDataKeys(admin).size();

        handler.createKey(admin, "testlabel", "test description");

        DataResult result = SystemManager.listDataKeys(admin);

        assertEquals(initialSize + 1, result.size());

        boolean foundKey = false;
        for (Object oIn : result) {
            CustomDataKeyOverview key = (CustomDataKeyOverview) oIn;
            if (key.getLabel().equals("testlabel") &&
                    key.getDescription().equals("test description")) {
                foundKey = true;
                break;
            }
        }
        assertTrue(foundKey);
    }

    @Test
    public void testDeleteKey() {

        // default setup already includes a custom key; therefore, let's
        // grab the initial size
        DataResult initialKeys = SystemManager.listDataKeys(admin);

        handler.createKey(admin, "testlabel", "test description");
        DataResult result = SystemManager.listDataKeys(admin);
        assertEquals(initialKeys.size() + 1, result.size());

        boolean foundKey = false;
        for (Object valueIn : result) {
            CustomDataKeyOverview key = (CustomDataKeyOverview) valueIn;
            if (key.getLabel().equals("testlabel") &&
                    key.getDescription().equals("test description")) {
                foundKey = true;
                break;
            }
        }
        assertTrue(foundKey);

        handler.deleteKey(admin, "testlabel");
        result = SystemManager.listDataKeys(admin);
        assertEquals(initialKeys.size(), result.size());

        foundKey = false;
        for (Object oIn : result) {
            CustomDataKeyOverview key = (CustomDataKeyOverview) oIn;
            if (key.getLabel().equals("testlabel") &&
                    key.getDescription().equals("test description")) {
                foundKey = true;
                break;
            }
        }
        assertFalse(foundKey);
    }

    @Test
    public void testListAllKeys() {

        // default setup already includes a custom key; therefore, we don't
        // need to add any as part of this test.

        Object[] keys = handler.listAllKeys(admin);

        assertEquals(SystemManager.listDataKeys(admin).size(),
                keys.length);
    }
}

