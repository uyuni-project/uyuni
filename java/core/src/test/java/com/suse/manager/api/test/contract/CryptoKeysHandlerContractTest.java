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

import com.redhat.rhn.domain.kickstart.crypto.CryptoKey;
import com.redhat.rhn.domain.kickstart.crypto.CryptoKeyType;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.CryptoKeyDto;
import com.redhat.rhn.frontend.xmlrpc.kickstart.keys.CryptoKeysHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class CryptoKeysHandlerContractTest extends BaseOpenApiTest {

    private static final String DESCRIPTION = "test-key";
    private static final String TYPE = "GPG";
    private static final String CONTENT = "-----BEGIN PGP PUBLIC KEY BLOCK-----";

    @Override
    protected String getApiNamespace() {
        return "kickstart.keys";
    }

    @Override
    protected Class<CryptoKeysHandler> getHandlerClass() {
        return CryptoKeysHandler.class;
    }

    private CryptoKeysHandler handler() {
        return (CryptoKeysHandler) handlerMock;
    }

    private CryptoKeyDto cryptoKeyDto() {
        CryptoKeyDto dto = new CryptoKeyDto();
        dto.setId(1000L);
        dto.setDescription(DESCRIPTION);
        dto.setLabel(TYPE);
        dto.setOrgId(1L);
        return dto;
    }

    private CryptoKey cryptoKey() {
        CryptoKeyType type = new CryptoKeyType();
        type.setLabel(TYPE);
        type.setDescription("GPG key");

        CryptoKey key = new CryptoKey();
        key.setDescription(DESCRIPTION);
        key.setCryptoKeyType(type);
        key.setKey(CONTENT.getBytes(StandardCharsets.UTF_8));
        return key;
    }

    @Test
    public void testListAllKeys() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listAllKeys(with(mockUser));
            will(returnValue(List.of(cryptoKeyDto())));
        }});

        validateApiContract("/kickstart.keys/listAllKeys", "GET")
                .onHandlerMethod("listAllKeys", User.class);
    }

    @Test
    public void testGetDetails() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getDetails(with(mockUser), with(DESCRIPTION));
            will(returnValue(cryptoKey()));
        }});

        validateApiContract("/kickstart.keys/getDetails", "GET")
                .withParams(Map.of("description", new String[]{DESCRIPTION}))
                .onHandlerMethod("getDetails", User.class, String.class);
    }

    @Test
    public void testCreate() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).create(with(mockUser), with(DESCRIPTION), with(TYPE), with(CONTENT));
            will(returnValue(1));
        }});

        validateApiContract("/kickstart.keys/create", "POST")
                .withBody(Map.of("description", DESCRIPTION, "type", TYPE, "content", CONTENT))
                .onHandlerMethod("create", User.class, String.class, String.class, String.class);
    }

    @Test
    public void testUpdate() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).update(with(mockUser), with(DESCRIPTION), with(TYPE), with(CONTENT));
            will(returnValue(1));
        }});

        validateApiContract("/kickstart.keys/update", "POST")
                .withBody(Map.of("description", DESCRIPTION, "type", TYPE, "content", CONTENT))
                .onHandlerMethod("update", User.class, String.class, String.class, String.class);
    }

    @Test
    public void testDelete() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).delete(with(mockUser), with(DESCRIPTION));
            will(returnValue(1));
        }});

        validateApiContract("/kickstart.keys/delete", "POST")
                .withBody(Map.of("description", DESCRIPTION))
                .onHandlerMethod("delete", User.class, String.class);
    }
}
