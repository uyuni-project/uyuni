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

import com.redhat.rhn.domain.rhnpackage.PackageKey;
import com.redhat.rhn.domain.rhnpackage.PackageKeyType;
import com.redhat.rhn.domain.rhnpackage.PackageProvider;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.packages.provider.PackagesProviderHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class PackagesProviderHandlerContractTest extends BaseOpenApiTest {

    private static final String PROVIDER_NAME = "SUSE";

    @Override
    protected String getApiNamespace() {
        return "packages.provider";
    }

    @Override
    protected Class<PackagesProviderHandler> getHandlerClass() {
        return PackagesProviderHandler.class;
    }

    private PackagesProviderHandler handler() {
        return (PackagesProviderHandler) handlerMock;
    }

    private PackageKey packageKey() {
        PackageKeyType type = new PackageKeyType();
        type.setLabel("gpg");

        PackageKey key = new PackageKey();
        key.setKey("1234567890abcdef");
        key.setType(type);
        return key;
    }

    private PackageProvider packageProvider() {
        PackageProvider provider = new PackageProvider();
        provider.setName(PROVIDER_NAME);
        provider.setKeys(Set.of(packageKey()));
        return provider;
    }

    @Test
    public void testList() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).list(with(mockUser));
            will(returnValue(List.of(packageProvider())));
        }});

        validateApiContract("/packages.provider/list", "POST")
                .onHandlerMethod("list", User.class);
    }

    @Test
    public void testListKeys() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listKeys(with(mockUser), with(PROVIDER_NAME));
            will(returnValue(Set.of(packageKey())));
        }});

        validateApiContract("/packages.provider/listKeys", "GET")
                .withParams(Map.of("providerName", new String[]{PROVIDER_NAME}))
                .onHandlerMethod("listKeys", User.class, String.class);
    }

    @Test
    public void testAssociateKey() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).associateKey(with(mockUser), with(PROVIDER_NAME),
                    with("1234567890abcdef"), with("gpg"));
            will(returnValue(1));
        }});

        validateApiContract("/packages.provider/associateKey", "POST")
                .withBody(Map.of(
                        "providerName", PROVIDER_NAME,
                        "key", "1234567890abcdef",
                        "type", "gpg"
                ))
                .onHandlerMethod("associateKey", User.class, String.class, String.class, String.class);
    }
}
