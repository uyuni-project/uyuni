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

import com.redhat.rhn.domain.org.usergroup.OrgUserExtGroup;
import com.redhat.rhn.domain.org.usergroup.UserExtGroup;
import com.redhat.rhn.domain.role.Role;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserExternalHandlerContractTest extends BaseOpenApiTest {

    @Override
    protected String getApiNamespace() {
        return "user.external";
    }

    @Override
    protected Class<UserExternalHandler> getHandlerClass() {
        return UserExternalHandler.class;
    }

    private UserExternalHandler handler() {
        return (UserExternalHandler) handlerMock;
    }

    /**
     * Role is an interface and the real implementations are loaded from the database, so the
     * serializer is fed a mock that only has to answer getLabel().
     *
     * @return an external group carrying a single role
     */
    private UserExtGroup userExtGroup() {
        Role role = context.mock(Role.class, "role" + System.nanoTime());
        context.checking(new Expectations() {{
            allowing(role).getLabel();
            will(returnValue("org_admin"));
        }});

        UserExtGroup group = new UserExtGroup();
        group.setLabel("ipa-admins");
        group.setRoles(Set.of(role));
        return group;
    }

    private OrgUserExtGroup orgUserExtGroup() {
        ServerGroup serverGroup = new ServerGroup();
        serverGroup.setName("web-servers");

        OrgUserExtGroup group = new OrgUserExtGroup();
        group.setLabel("ipa-web-admins");
        group.setServerGroups(Set.of(serverGroup));
        return group;
    }

    @Test
    public void testSetKeepTemporaryRoles() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).setKeepTemporaryRoles(with(mockUser), with(Boolean.TRUE));
            will(returnValue(1));
        }});

        validateApiContract("/user.external/setKeepTemporaryRoles", "POST")
                .withBody(Map.of("keepRoles", true))
                .onHandlerMethod("setKeepTemporaryRoles", User.class, Boolean.class);
    }

    @Test
    public void testGetKeepTemporaryRoles() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getKeepTemporaryRoles(with(mockUser));
            will(returnValue(true));
        }});

        validateApiContract("/user.external/getKeepTemporaryRoles", "GET")
                .onHandlerMethod("getKeepTemporaryRoles", User.class);
    }

    @Test
    public void testSetUseOrgUnit() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).setUseOrgUnit(with(mockUser), with(Boolean.FALSE));
            will(returnValue(1));
        }});

        validateApiContract("/user.external/setUseOrgUnit", "POST")
                .withBody(Map.of("useOrgUnit", false))
                .onHandlerMethod("setUseOrgUnit", User.class, Boolean.class);
    }

    @Test
    public void testGetUseOrgUnit() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getUseOrgUnit(with(mockUser));
            will(returnValue(false));
        }});

        validateApiContract("/user.external/getUseOrgUnit", "GET")
                .onHandlerMethod("getUseOrgUnit", User.class);
    }

    @Test
    public void testSetDefaultOrg() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).setDefaultOrg(with(mockUser), with(1));
            will(returnValue(1));
        }});

        validateApiContract("/user.external/setDefaultOrg", "POST")
                .withBody(Map.of("orgId", 1))
                .onHandlerMethod("setDefaultOrg", User.class, Integer.class);
    }

    @Test
    public void testGetDefaultOrg() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getDefaultOrg(with(mockUser));
            will(returnValue(1));
        }});

        validateApiContract("/user.external/getDefaultOrg", "GET")
                .onHandlerMethod("getDefaultOrg", User.class);
    }

    @Test
    public void testCreateExternalGroupToRoleMap() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "ipa-admins");
        body.put("roles", List.of("org_admin"));

        context.checking(new Expectations() {{
            oneOf(handler()).createExternalGroupToRoleMap(with(mockUser), with("ipa-admins"),
                    with(List.of("org_admin")));
            will(returnValue(userExtGroup()));
        }});

        validateApiContract("/user.external/createExternalGroupToRoleMap", "POST")
                .withBody(body)
                .onHandlerMethod("createExternalGroupToRoleMap", User.class, String.class, List.class);
    }

    @Test
    public void testGetExternalGroupToRoleMap() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getExternalGroupToRoleMap(with(mockUser), with("ipa-admins"));
            will(returnValue(userExtGroup()));
        }});

        validateApiContract("/user.external/getExternalGroupToRoleMap", "GET")
                .withParams(Map.of("name", new String[] {"ipa-admins"}))
                .onHandlerMethod("getExternalGroupToRoleMap", User.class, String.class);
    }

    @Test
    public void testSetExternalGroupRoles() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "ipa-admins");
        body.put("roles", List.of("org_admin", "channel_admin"));

        context.checking(new Expectations() {{
            oneOf(handler()).setExternalGroupRoles(with(mockUser), with("ipa-admins"),
                    with(List.of("org_admin", "channel_admin")));
            will(returnValue(1));
        }});

        validateApiContract("/user.external/setExternalGroupRoles", "POST")
                .withBody(body)
                .onHandlerMethod("setExternalGroupRoles", User.class, String.class, List.class);
    }

    @Test
    public void testDeleteExternalGroupToRoleMap() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).deleteExternalGroupToRoleMap(with(mockUser), with("ipa-admins"));
            will(returnValue(1));
        }});

        validateApiContract("/user.external/deleteExternalGroupToRoleMap", "POST")
                .withBody(Map.of("name", "ipa-admins"))
                .onHandlerMethod("deleteExternalGroupToRoleMap", User.class, String.class);
    }

    @Test
    public void testListExternalGroupToRoleMaps() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listExternalGroupToRoleMaps(with(mockUser));
            will(returnValue(List.of(userExtGroup())));
        }});

        validateApiContract("/user.external/listExternalGroupToRoleMaps", "GET")
                .onHandlerMethod("listExternalGroupToRoleMaps", User.class);
    }

    @Test
    public void testCreateExternalGroupToSystemGroupMap() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "ipa-web-admins");
        body.put("groupNames", List.of("web-servers"));

        context.checking(new Expectations() {{
            oneOf(handler()).createExternalGroupToSystemGroupMap(with(mockUser), with("ipa-web-admins"),
                    with(List.of("web-servers")));
            will(returnValue(orgUserExtGroup()));
        }});

        validateApiContract("/user.external/createExternalGroupToSystemGroupMap", "POST")
                .withBody(body)
                .onHandlerMethod("createExternalGroupToSystemGroupMap", User.class, String.class, List.class);
    }

    @Test
    public void testGetExternalGroupToSystemGroupMap() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getExternalGroupToSystemGroupMap(with(mockUser), with("ipa-web-admins"));
            will(returnValue(orgUserExtGroup()));
        }});

        validateApiContract("/user.external/getExternalGroupToSystemGroupMap", "GET")
                .withParams(Map.of("name", new String[] {"ipa-web-admins"}))
                .onHandlerMethod("getExternalGroupToSystemGroupMap", User.class, String.class);
    }

    @Test
    public void testSetExternalGroupSystemGroups() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "ipa-web-admins");
        body.put("groupNames", List.of("web-servers"));

        context.checking(new Expectations() {{
            oneOf(handler()).setExternalGroupSystemGroups(with(mockUser), with("ipa-web-admins"),
                    with(List.of("web-servers")));
            will(returnValue(1));
        }});

        validateApiContract("/user.external/setExternalGroupSystemGroups", "POST")
                .withBody(body)
                .onHandlerMethod("setExternalGroupSystemGroups", User.class, String.class, List.class);
    }

    @Test
    public void testDeleteExternalGroupToSystemGroupMap() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).deleteExternalGroupToSystemGroupMap(with(mockUser), with("ipa-web-admins"));
            will(returnValue(1));
        }});

        validateApiContract("/user.external/deleteExternalGroupToSystemGroupMap", "POST")
                .withBody(Map.of("name", "ipa-web-admins"))
                .onHandlerMethod("deleteExternalGroupToSystemGroupMap", User.class, String.class);
    }

    @Test
    public void testListExternalGroupToSystemGroupMaps() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listExternalGroupToSystemGroupMaps(with(mockUser));
            will(returnValue(List.of(orgUserExtGroup())));
        }});

        validateApiContract("/user.external/listExternalGroupToSystemGroupMaps", "GET")
                .onHandlerMethod("listExternalGroupToSystemGroupMaps", User.class);
    }
}
