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
import com.redhat.rhn.frontend.dto.OrgTrustOverview;
import com.redhat.rhn.frontend.xmlrpc.org.trusts.OrgTrustHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class OrgTrustHandlerContractTest extends BaseOpenApiTest {

    private static final Integer ORG_ID = 1;
    private static final Integer TRUST_ORG_ID = 2;

    @Override
    protected String getApiNamespace() {
        return "org.trusts";
    }

    @Override
    protected Class<OrgTrustHandler> getHandlerClass() {
        return OrgTrustHandler.class;
    }

    private OrgTrustHandler handler() {
        return (OrgTrustHandler) handlerMock;
    }

    private Map<String, Object> channelInfo() {
        return Map.of(
                "channel_id", 100,
                "channel_name", "Test Channel",
                "packages", 42,
                "systems", 7
        );
    }

    private OrgTrustOverview orgTrustOverview() {
        OrgTrustOverview overview = new OrgTrustOverview();
        overview.setId(2L);
        overview.setName("Trusted Organization");
        overview.setTrusted(Boolean.TRUE);
        return overview;
    }

    @Test
    public void testListOrgs() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listOrgs(with(mockUser));
            will(returnValue(new Object[]{Map.of(
                    "org_id", ORG_ID,
                    "org_name", "Test Organization",
                    "shared_channels", 3
            )}));
        }});

        validateApiContract("/org.trusts/listOrgs", "GET")
                .onHandlerMethod("listOrgs", User.class);
    }

    @Test
    public void testListChannelsProvided() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listChannelsProvided(with(mockUser), with(ORG_ID));
            will(returnValue(new Object[]{channelInfo()}));
        }});

        validateApiContract("/org.trusts/listChannelsProvided", "GET")
                .withParams(Map.of("orgId", new String[]{ORG_ID.toString()}))
                .onHandlerMethod("listChannelsProvided", User.class, Integer.class);
    }

    @Test
    public void testListChannelsConsumed() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listChannelsConsumed(with(mockUser), with(ORG_ID));
            will(returnValue(new Object[]{channelInfo()}));
        }});

        validateApiContract("/org.trusts/listChannelsConsumed", "GET")
                .withParams(Map.of("orgId", new String[]{ORG_ID.toString()}))
                .onHandlerMethod("listChannelsConsumed", User.class, Integer.class);
    }

    @Test
    public void testGetDetails() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getDetails(with(mockUser), with(ORG_ID));
            will(returnValue(Map.of(
                    "created", new Date(),
                    "trusted_since", new Date(),
                    "channels_provided", 3,
                    "channels_consumed", 2,
                    "systems_migrated_to", 1,
                    "systems_migrated_from", 0,
                    "systems_transferred_to", 1,
                    "systems_transferred_from", 0
            )));
        }});

        validateApiContract("/org.trusts/getDetails", "GET")
                .withParams(Map.of("orgId", new String[]{ORG_ID.toString()}))
                .onHandlerMethod("getDetails", User.class, Integer.class);
    }

    @Test
    public void testListTrusts() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listTrusts(with(mockUser), with(ORG_ID));
            will(returnValue(List.of(orgTrustOverview())));
        }});

        validateApiContract("/org.trusts/listTrusts", "GET")
                .withParams(Map.of("orgId", new String[]{ORG_ID.toString()}))
                .onHandlerMethod("listTrusts", User.class, Integer.class);
    }

    @Test
    public void testListSystemsAffected() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listSystemsAffected(with(mockUser), with(ORG_ID), with(TRUST_ORG_ID));
            will(returnValue(List.of(Map.of(
                    "systemId", 1000,
                    "systemName", "test.example.com"
            ))));
        }});

        validateApiContract("/org.trusts/listSystemsAffected", "GET")
                .withParams(Map.of(
                        "orgId", new String[]{ORG_ID.toString()},
                        "trustOrgId", new String[]{TRUST_ORG_ID.toString()}
                ))
                .onHandlerMethod("listSystemsAffected", User.class, Integer.class, Integer.class);
    }

    @Test
    public void testAddTrust() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).addTrust(with(mockUser), with(ORG_ID), with(TRUST_ORG_ID));
            will(returnValue(1));
        }});

        validateApiContract("/org.trusts/addTrust", "POST")
                .withBody(Map.of("orgId", ORG_ID, "trustOrgId", TRUST_ORG_ID))
                .onHandlerMethod("addTrust", User.class, Integer.class, Integer.class);
    }

    @Test
    public void testRemoveTrust() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).removeTrust(with(mockUser), with(ORG_ID), with(TRUST_ORG_ID));
            will(returnValue(1));
        }});

        validateApiContract("/org.trusts/removeTrust", "POST")
                .withBody(Map.of("orgId", ORG_ID, "trustOrgId", TRUST_ORG_ID))
                .onHandlerMethod("removeTrust", User.class, Integer.class, Integer.class);
    }
}
