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

import com.redhat.rhn.domain.access.AccessGroup;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.access.AccessHandler;
import org.jmock.Expectations;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class AccessHandlerContractTest extends BaseOpenApiTest {

    @Override protected String getApiNamespace() { return "access"; }
    @Override protected Class<AccessHandler> getHandlerClass() { return AccessHandler.class; }

    private AccessHandler handler() {
        return (AccessHandler) handlerMock;
    }

    @Test
    public void testCreateRole() throws Exception {
        var label = "simple-role";
        var desc = "Just a role";

        context.checking(new Expectations() {{
            oneOf(handler()).createRole(with(mockUser), with(label), with(desc));
            will(returnValue(createFakeAccessGroup(label, desc)));
        }});

        validateApiContract("/access/createRole", "POST")
                .withBody(Map.of("label", label, "description", desc))
                .onHandlerMethod("createRole", User.class, String.class, String.class);
    }

    @Test
    public void testCreateRoleWithPermissions() throws Exception {
        var label = "admin-copy";
        var desc = "Copy of admin";
        var perms = List.of("org_admin");

        context.checking(new Expectations() {{
            oneOf(handler()).createRole(with(mockUser), with(label), with(desc), with(perms));
            will(returnValue(createFakeAccessGroup(label, desc)));
        }});

        validateApiContract("/access/createRole", "POST")
                .withBody(Map.of("label", label, "description", desc, "permissionsFrom", perms))
                .onHandlerMethod("createRole", User.class, String.class, String.class, List.class);
    }

    @Test
    public void testListRoles() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listRoles(with(mockUser));
            will(returnValue(List.of(createFakeAccessGroup("role1", "desc1"))));
        }});
        validateApiContract("/access/listRoles", "GET").onHandlerMethod("listRoles");
    }

    @Test
    public void testListPermissions() throws Exception {
        var label = "manager-role";

        context.checking(new Expectations() {{
            oneOf(handler()).listPermissions(with(mockUser), with(label));
            will(returnValue(Set.of()));
        }});

        validateApiContract("/access/listPermissions", "GET")
                .withParams(Map.of("label", new String[]{label}))
                .onHandlerMethod("listPermissions");
    }

    @Test
    public void testGrantAccess() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).grantAccess(with(mockUser), with("role1"), with(List.of("system")));
            will(returnValue(1));
        }});

        validateApiContract("/access/grantAccess", "POST")
                .withBody(Map.of("label", "role1", "namespaces", List.of("system")))
                .onHandlerMethod("grantAccess", User.class, String.class, List.class);
    }

    @Test
    public void testGrantAccessWithModes() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).grantAccess(with(mockUser), with("role1"), with(List.of("system")), with(List.of("R")));
            will(returnValue(1));
        }});

        validateApiContract("/access/grantAccess", "POST")
                .withBody(Map.of("label", "role1", "namespaces", List.of("system"), "modes", List.of("R")))
                .onHandlerMethod("grantAccess", User.class, String.class, List.class, List.class);
    }

    @Test
    public void testDeleteRole() throws Exception {
        var label = "to-delete";

        context.checking(new Expectations() {{
            oneOf(handler()).deleteRole(with(mockUser), with(label));
            will(returnValue(1));
        }});

        validateApiContract("/access/deleteRole", "POST")
                .withParams(Map.of("label", new String[]{label}))
                .onHandlerMethod("deleteRole");
    }

    @Test
    public void testRevokeAccess() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).revokeAccess(with(mockUser), with("role1"), with(List.of("system")));
            will(returnValue(1));
        }});

        validateApiContract("/access/revokeAccess", "POST")
                .withBody(Map.of("label", "role1", "namespaces", List.of("system")))
                .onHandlerMethod("revokeAccess", User.class, String.class, List.class);
    }

    private AccessGroup createFakeAccessGroup(String label, String desc) {
        var g = new AccessGroup();
        g.setLabel(label);
        g.setDescription(desc);
        g.setOrg(fakeOrg);
        return g;
    }
}
