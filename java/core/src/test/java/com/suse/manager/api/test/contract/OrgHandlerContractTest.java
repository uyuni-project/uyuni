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
import com.redhat.rhn.frontend.dto.MultiOrgUserOverview;
import com.redhat.rhn.frontend.dto.OrgDto;
import com.redhat.rhn.frontend.xmlrpc.org.OrgHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class OrgHandlerContractTest extends BaseOpenApiTest {

    private static final Integer ORG_ID = 1;
    private static final String ORG_NAME = "Test Organization";

    @Override
    protected String getApiNamespace() {
        return "org";
    }

    @Override
    protected Class<OrgHandler> getHandlerClass() {
        return OrgHandler.class;
    }

    private OrgHandler handler() {
        return (OrgHandler) handlerMock;
    }

    /**
     * Builds an organization serialized by the registered OrgDtoSerializer. Every count is set,
     * so the properties the serializer only adds when they are not null are all present.
     *
     * @param name a unique mock name, so several organizations can coexist in one test run
     * @return the organization
     */
    private OrgDto orgDto(String name) {
        OrgDto org = context.mock(OrgDto.class, name);

        context.checking(new Expectations() {{
            allowing(org).getId();
            will(returnValue(ORG_ID.longValue()));
            allowing(org).getName();
            will(returnValue(ORG_NAME));
            allowing(org).getUsers();
            will(returnValue(3L));
            allowing(org).getSystems();
            will(returnValue(4L));
            allowing(org).getTrusts();
            will(returnValue(1L));
            allowing(org).getActivationKeys();
            will(returnValue(2L));
            allowing(org).getServerGroups();
            will(returnValue(5L));
            allowing(org).getKickstartProfiles();
            will(returnValue(6L));
            allowing(org).getConfigChannels();
            will(returnValue(7L));
            allowing(org).isStagingContentEnabled();
            will(returnValue(true));
        }});

        return org;
    }

    /**
     * Builds a user serialized by the registered MultiOrgUserOverviewSerializer.
     *
     * @return the organization user
     */
    private MultiOrgUserOverview orgUser() {
        MultiOrgUserOverview user = context.mock(MultiOrgUserOverview.class, "orgUser");

        context.checking(new Expectations() {{
            allowing(user).getLogin();
            will(returnValue("testuser"));
            allowing(user).getLoginUc();
            will(returnValue("TESTUSER"));
            allowing(user).getUserDisplayName();
            will(returnValue("Test User"));
            allowing(user).getAddress();
            will(returnValue("testuser@example.com"));
            allowing(user).getOrgAdmin();
            will(returnValue(1L));
        }});

        return user;
    }

    @Test
    public void testCreate() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).create(with(mockUser), with(ORG_NAME), with("testadmin"), with("testpassword"),
                    with("Mr."), with("Test"), with("Admin"), with("testadmin@example.com"), with(false));
            will(returnValue(orgDto("createdOrg")));
        }});

        validateApiContract("/org/create", "POST")
                .withBody(Map.of(
                        "orgName", ORG_NAME,
                        "adminLogin", "testadmin",
                        "adminPassword", "testpassword",
                        "prefix", "Mr.",
                        "firstName", "Test",
                        "lastName", "Admin",
                        "email", "testadmin@example.com",
                        "usePamAuth", false))
                .onHandlerMethod("create", User.class, String.class, String.class, String.class, String.class,
                        String.class, String.class, String.class, Boolean.class);
    }

    /**
     * The first organization is created before any user exists, so the endpoint is public and the
     * handler method takes no session user.
     */
    @Test
    public void testCreateFirst() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).createFirst(with(ORG_NAME), with("testadmin"), with("testpassword"),
                    with("Test"), with("Admin"), with("testadmin@example.com"));
            will(returnValue(orgDto("firstOrg")));
        }});

        validateApiContract("/org/createFirst", "POST")
                .withBody(Map.of(
                        "orgName", ORG_NAME,
                        "adminLogin", "testadmin",
                        "adminPassword", "testpassword",
                        "firstName", "Test",
                        "lastName", "Admin",
                        "email", "testadmin@example.com"))
                .onHandlerMethod("createFirst", String.class, String.class, String.class, String.class,
                        String.class, String.class);
    }

    @Test
    public void testListOrgs() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listOrgs(with(mockUser));
            will(returnValue(List.of(orgDto("listedOrg"))));
        }});

        validateApiContract("/org/listOrgs", "GET")
                .onHandlerMethod("listOrgs", User.class);
    }

    @Test
    public void testDelete() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).delete(with(mockUser), with(ORG_ID));
            will(returnValue(1));
        }});

        validateApiContract("/org/delete", "POST")
                .withBody(Map.of("orgId", ORG_ID))
                .onHandlerMethod("delete", User.class, Integer.class);
    }

    @Test
    public void testListUsers() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listUsers(with(mockUser), with(ORG_ID));
            will(returnValue(List.of(orgUser())));
        }});

        validateApiContract("/org/listUsers", "GET")
                .withParams(Map.of("orgId", new String[]{ORG_ID.toString()}))
                .onHandlerMethod("listUsers", User.class, Integer.class);
    }

    @Test
    public void testGetDetailsById() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getDetails(with(mockUser), with(ORG_ID));
            will(returnValue(orgDto("orgById")));
        }});

        validateApiContract("/org/getDetails", "GET")
                .withParams(Map.of("orgId", new String[]{ORG_ID.toString()}))
                .onHandlerMethod("getDetails", User.class, Integer.class);
    }

    /**
     * The two lookups share one path, so a request naming the organization has to reach the
     * overload taking the name rather than the one taking the id.
     */
    @Test
    public void testGetDetailsByName() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getDetails(with(mockUser), with(ORG_NAME));
            will(returnValue(orgDto("orgByName")));
        }});

        validateApiContract("/org/getDetails", "GET")
                .withParams(Map.of("name", new String[]{ORG_NAME}))
                .onHandlerMethod("getDetails", User.class, String.class);
    }

    @Test
    public void testUpdateName() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).updateName(with(mockUser), with(ORG_ID), with("Renamed Organization"));
            will(returnValue(orgDto("renamedOrg")));
        }});

        validateApiContract("/org/updateName", "POST")
                .withBody(Map.of("orgId", ORG_ID, "name", "Renamed Organization"))
                .onHandlerMethod("updateName", User.class, Integer.class, String.class);
    }

    @Test
    public void testTransferSystems() throws Exception {
        var sids = List.of(1001, 1002);

        context.checking(new Expectations() {{
            oneOf(handler()).transferSystems(with(mockUser), with(ORG_ID), with(sids));
            will(returnValue(new Object[]{1001L, 1002L}));
        }});

        validateApiContract("/org/transferSystems", "POST")
                .withBody(Map.of("toOrgId", ORG_ID, "sids", sids))
                .onHandlerMethod("transferSystems", User.class, Integer.class, List.class);
    }

    @Test
    public void testGetPolicyForScapFileUpload() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getPolicyForScapFileUpload(with(mockUser), with(ORG_ID));
            will(returnValue(Map.of("enabled", true, "size_limit", 1048576L)));
        }});

        validateApiContract("/org/getPolicyForScapFileUpload", "GET")
                .withParams(Map.of("orgId", new String[]{ORG_ID.toString()}))
                .onHandlerMethod("getPolicyForScapFileUpload", User.class, Integer.class);
    }

    @Test
    public void testSetPolicyForScapFileUpload() throws Exception {
        var newSettings = Map.<String, Object>of("enabled", true);

        context.checking(new Expectations() {{
            oneOf(handler()).setPolicyForScapFileUpload(with(mockUser), with(ORG_ID), with(newSettings));
            will(returnValue(1));
        }});

        validateApiContract("/org/setPolicyForScapFileUpload", "POST")
                .withBody(Map.of("orgId", ORG_ID, "newSettings", newSettings))
                .onHandlerMethod("setPolicyForScapFileUpload", User.class, Integer.class, Map.class);
    }

    @Test
    public void testGetPolicyForScapResultDeletion() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getPolicyForScapResultDeletion(with(mockUser), with(ORG_ID));
            will(returnValue(Map.of("enabled", true, "retention_period", 90L)));
        }});

        validateApiContract("/org/getPolicyForScapResultDeletion", "GET")
                .withParams(Map.of("orgId", new String[]{ORG_ID.toString()}))
                .onHandlerMethod("getPolicyForScapResultDeletion", User.class, Integer.class);
    }

    @Test
    public void testSetPolicyForScapResultDeletion() throws Exception {
        var newSettings = Map.<String, Object>of("enabled", true, "retention_period", 90);

        context.checking(new Expectations() {{
            oneOf(handler()).setPolicyForScapResultDeletion(with(mockUser), with(ORG_ID), with(newSettings));
            will(returnValue(1));
        }});

        validateApiContract("/org/setPolicyForScapResultDeletion", "POST")
                .withBody(Map.of("orgId", ORG_ID, "newSettings", newSettings))
                .onHandlerMethod("setPolicyForScapResultDeletion", User.class, Integer.class, Map.class);
    }

    @Test
    public void testIsOrgConfigManagedByOrgAdmin() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).isOrgConfigManagedByOrgAdmin(with(mockUser), with(ORG_ID));
            will(returnValue(true));
        }});

        validateApiContract("/org/isOrgConfigManagedByOrgAdmin", "GET")
                .withParams(Map.of("orgId", new String[]{ORG_ID.toString()}))
                .onHandlerMethod("isOrgConfigManagedByOrgAdmin", User.class, Integer.class);
    }

    @Test
    public void testSetOrgConfigManagedByOrgAdmin() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).setOrgConfigManagedByOrgAdmin(with(mockUser), with(ORG_ID), with(true));
            will(returnValue(1));
        }});

        validateApiContract("/org/setOrgConfigManagedByOrgAdmin", "POST")
                .withBody(Map.of("orgId", ORG_ID, "enable", true))
                .onHandlerMethod("setOrgConfigManagedByOrgAdmin", User.class, Integer.class, Boolean.class);
    }

    @Test
    public void testIsErrataEmailNotifsForOrg() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).isErrataEmailNotifsForOrg(with(mockUser), with(ORG_ID));
            will(returnValue(true));
        }});

        validateApiContract("/org/isErrataEmailNotifsForOrg", "GET")
                .withParams(Map.of("orgId", new String[]{ORG_ID.toString()}))
                .onHandlerMethod("isErrataEmailNotifsForOrg", User.class, Integer.class);
    }

    @Test
    public void testSetErrataEmailNotifsForOrg() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).setErrataEmailNotifsForOrg(with(mockUser), with(ORG_ID), with(true));
            will(returnValue(1));
        }});

        validateApiContract("/org/setErrataEmailNotifsForOrg", "POST")
                .withBody(Map.of("orgId", ORG_ID, "enable", true))
                .onHandlerMethod("setErrataEmailNotifsForOrg", User.class, Integer.class, Boolean.class);
    }

    @Test
    public void testIsContentStagingEnabled() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).isContentStagingEnabled(with(mockUser), with(ORG_ID));
            will(returnValue(true));
        }});

        validateApiContract("/org/isContentStagingEnabled", "GET")
                .withParams(Map.of("orgId", new String[]{ORG_ID.toString()}))
                .onHandlerMethod("isContentStagingEnabled", User.class, Integer.class);
    }

    @Test
    public void testSetContentStaging() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).setContentStaging(with(mockUser), with(ORG_ID), with(true));
            will(returnValue(1));
        }});

        validateApiContract("/org/setContentStaging", "POST")
                .withBody(Map.of("orgId", ORG_ID, "enable", true))
                .onHandlerMethod("setContentStaging", User.class, Integer.class, Boolean.class);
    }

    @Test
    public void testGetClmSyncPatchesConfig() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getClmSyncPatchesConfig(with(mockUser), with(ORG_ID));
            will(returnValue(true));
        }});

        validateApiContract("/org/getClmSyncPatchesConfig", "GET")
                .withParams(Map.of("orgId", new String[]{ORG_ID.toString()}))
                .onHandlerMethod("getClmSyncPatchesConfig", User.class, Integer.class);
    }

    @Test
    public void testSetClmSyncPatchesConfig() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).setClmSyncPatchesConfig(with(mockUser), with(ORG_ID), with(true));
            will(returnValue(1));
        }});

        validateApiContract("/org/setClmSyncPatchesConfig", "POST")
                .withBody(Map.of("orgId", ORG_ID, "value", true))
                .onHandlerMethod("setClmSyncPatchesConfig", User.class, Integer.class, Boolean.class);
    }
}
