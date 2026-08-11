/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.api.test.contract;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.CustomDataKeyOverview;
import com.redhat.rhn.frontend.xmlrpc.system.custominfo.CustomInfoHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class CustomInfoHandlerContractTest extends BaseOpenApiTest {

    @Override
    protected String getApiNamespace() {
        return "system.custominfo";
    }

    @Override
    protected Class<CustomInfoHandler> getHandlerClass() {
        return CustomInfoHandler.class;
    }

    private CustomInfoHandler handler() {
        return (CustomInfoHandler) handlerMock;
    }

    private CustomDataKeyOverview customKey() {
        CustomDataKeyOverview key = new CustomDataKeyOverview();
        key.setId(100L);
        key.setLabel("asset-tag");
        key.setDescription("The asset tag of the system");
        key.setServerCount(3L);
        key.setLastModified(new Date());
        return key;
    }

    @Test
    public void testCreateKey() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("keyLabel", "asset-tag");
        body.put("keyDescription", "The asset tag of the system");

        context.checking(new Expectations() {{
            oneOf(handler()).createKey(with(mockUser), with("asset-tag"),
                    with("The asset tag of the system"));
            will(returnValue(1));
        }});

        validateApiContract("/system.custominfo/createKey", "POST")
                .withBody(body)
                .onHandlerMethod("createKey", User.class, String.class, String.class);
    }

    @Test
    public void testDeleteKey() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).deleteKey(with(mockUser), with("asset-tag"));
            will(returnValue(1));
        }});

        validateApiContract("/system.custominfo/deleteKey", "POST")
                .withBody(Map.of("keyLabel", "asset-tag"))
                .onHandlerMethod("deleteKey", User.class, String.class);
    }

    @Test
    public void testUpdateKey() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("keyLabel", "asset-tag");
        body.put("keyDescription", "Updated description");

        context.checking(new Expectations() {{
            oneOf(handler()).updateKey(with(mockUser), with("asset-tag"), with("Updated description"));
            will(returnValue(1));
        }});

        validateApiContract("/system.custominfo/updateKey", "POST")
                .withBody(body)
                .onHandlerMethod("updateKey", User.class, String.class, String.class);
    }

    @Test
    public void testListAllKeys() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listAllKeys(with(mockUser));
            will(returnValue(new Object[] {customKey()}));
        }});

        validateApiContract("/system.custominfo/listAllKeys", "GET")
                .onHandlerMethod("listAllKeys", User.class);
    }
}
