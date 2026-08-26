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
import com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ActionChainHandlerContractTest extends BaseOpenApiTest {

    private static final Integer SID = 1000010000;
    private static final List<Integer> SIDS = List.of(1000010000, 1000010001);
    private static final String CHAIN_LABEL = "test-chain";
    private static final Integer ACTION_ID = 10;
    private static final List<Integer> ERRATA_IDS = List.of(100, 101);
    private static final List<Integer> PACKAGE_IDS = List.of(200, 201);
    private static final String SCHEDULE_DATE = "2026-06-01T10:00:00Z";
    private static final Date SCHEDULED_AT = Date.from(Instant.parse(SCHEDULE_DATE));

    @Override
    protected String getApiNamespace() {
        return "actionchain";
    }

    @Override
    protected Class<ActionChainHandler> getHandlerClass() {
        return ActionChainHandler.class;
    }

    private ActionChainHandler handler() {
        return (ActionChainHandler) handlerMock;
    }

    /**
     * @return one chain as the handler describes it
     */
    private Map<String, Object> chain() {
        Map<String, Object> chain = new LinkedHashMap<>();
        chain.put("label", CHAIN_LABEL);
        chain.put("entrycount", 2);
        return chain;
    }

    /**
     * @return one chain entry as the handler describes it
     */
    private Map<String, Object> chainEntry() {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("id", ACTION_ID);
        entry.put("label", "Package Install");
        entry.put("created", new Date());
        entry.put("earliest", new Date());
        entry.put("type", "Package Install");
        entry.put("modified", new Date());
        entry.put("cuid", "admin");
        return entry;
    }

    @Test
    public void testListChains() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listChains(with(mockUser));
            will(returnValue(List.of(chain())));
        }});

        validateApiContract("/actionchain/listChains", "GET")
                .onHandlerMethod("listChains", User.class);
    }

    @Test
    public void testListChainActions() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listChainActions(with(mockUser), with(CHAIN_LABEL));
            will(returnValue(List.of(chainEntry())));
        }});

        validateApiContract("/actionchain/listChainActions", "GET")
                .withParams(Map.of("chainLabel", new String[] {CHAIN_LABEL}))
                .onHandlerMethod("listChainActions", User.class, String.class);
    }

    @Test
    public void testCreateChain() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).createChain(with(mockUser), with(CHAIN_LABEL));
            will(returnValue(1));
        }});

        validateApiContract("/actionchain/createChain", "POST")
                .withBody(Map.of("chainLabel", CHAIN_LABEL))
                .onHandlerMethod("createChain", User.class, String.class);
    }

    @Test
    public void testDeleteChain() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).deleteChain(with(mockUser), with(CHAIN_LABEL));
            will(returnValue(1));
        }});

        validateApiContract("/actionchain/deleteChain", "POST")
                .withBody(Map.of("chainLabel", CHAIN_LABEL))
                .onHandlerMethod("deleteChain", User.class, String.class);
    }

    @Test
    public void testRenameChain() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("previousLabel", CHAIN_LABEL);
        body.put("newLabel", "renamed-chain");

        context.checking(new Expectations() {{
            oneOf(handler()).renameChain(with(mockUser), with(CHAIN_LABEL), with("renamed-chain"));
            will(returnValue(1));
        }});

        validateApiContract("/actionchain/renameChain", "POST")
                .withBody(body)
                .onHandlerMethod("renameChain", User.class, String.class, String.class);
    }

    @Test
    public void testRemoveAction() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("chainLabel", CHAIN_LABEL);
        body.put("actionId", ACTION_ID);

        context.checking(new Expectations() {{
            oneOf(handler()).removeAction(with(mockUser), with(CHAIN_LABEL), with(ACTION_ID));
            will(returnValue(1));
        }});

        validateApiContract("/actionchain/removeAction", "POST")
                .withBody(body)
                .onHandlerMethod("removeAction", User.class, String.class, Integer.class);
    }

    @Test
    public void testScheduleChain() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("chainLabel", CHAIN_LABEL);
        body.put("date", SCHEDULE_DATE);

        context.checking(new Expectations() {{
            oneOf(handler()).scheduleChain(with(mockUser), with(CHAIN_LABEL), with(SCHEDULED_AT));
            will(returnValue(1));
        }});

        validateApiContract("/actionchain/scheduleChain", "POST")
                .withBody(body)
                .onHandlerMethod("scheduleChain", User.class, String.class, Date.class);
    }

    @Test
    public void testAddSystemReboot() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sid", SID);
        body.put("chainLabel", CHAIN_LABEL);

        context.checking(new Expectations() {{
            oneOf(handler()).addSystemReboot(with(mockUser), with(SID), with(CHAIN_LABEL));
            will(returnValue(ACTION_ID));
        }});

        validateApiContract("/actionchain/addSystemReboot", "POST")
                .withBody(body)
                .onHandlerMethod("addSystemReboot", User.class, Integer.class, String.class);
    }

    @Test
    public void testAddApplyHighstate() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sid", SID);
        body.put("chainLabel", CHAIN_LABEL);

        context.checking(new Expectations() {{
            oneOf(handler()).addApplyHighstate(with(mockUser), with(SID), with(CHAIN_LABEL));
            will(returnValue(ACTION_ID));
        }});

        validateApiContract("/actionchain/addApplyHighstate", "POST")
                .withBody(body)
                .onHandlerMethod("addApplyHighstate", User.class, Integer.class, String.class);
    }

    @Test
    public void testAddErrataUpdateBySystem() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sid", SID);
        body.put("errataIds", ERRATA_IDS);
        body.put("chainLabel", CHAIN_LABEL);

        context.checking(new Expectations() {{
            oneOf(handler()).addErrataUpdate(with(mockUser), with(SID), with(ERRATA_IDS), with(CHAIN_LABEL));
            will(returnValue(ACTION_ID));
        }});

        validateApiContract("/actionchain/addErrataUpdate", "POST")
                .withBody(body)
                .onHandlerMethod("addErrataUpdate", User.class, Integer.class, List.class, String.class);
    }

    @Test
    public void testAddErrataUpdateBySystems() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sids", SIDS);
        body.put("errataIds", ERRATA_IDS);
        body.put("chainLabel", CHAIN_LABEL);

        context.checking(new Expectations() {{
            oneOf(handler()).addErrataUpdate(with(mockUser), with(SIDS), with(ERRATA_IDS), with(CHAIN_LABEL));
            will(returnValue(ACTION_ID));
        }});

        validateApiContract("/actionchain/addErrataUpdate", "POST")
                .withBody(body)
                .onHandlerMethod("addErrataUpdate", User.class, List.class, List.class, String.class);
    }

    @Test
    public void testAddErrataUpdateOnlyRelevant() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sids", SIDS);
        body.put("errataIds", ERRATA_IDS);
        body.put("chainLabel", CHAIN_LABEL);
        body.put("onlyRelevant", true);

        context.checking(new Expectations() {{
            oneOf(handler()).addErrataUpdate(with(mockUser), with(SIDS), with(ERRATA_IDS), with(CHAIN_LABEL),
                    with(true));
            will(returnValue(ACTION_ID));
        }});

        validateApiContract("/actionchain/addErrataUpdate", "POST")
                .withBody(body)
                .onHandlerMethod("addErrataUpdate", User.class, List.class, List.class, String.class,
                        Boolean.class);
    }

    @Test
    public void testAddPackageInstall() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sid", SID);
        body.put("packageIds", PACKAGE_IDS);
        body.put("chainLabel", CHAIN_LABEL);

        context.checking(new Expectations() {{
            oneOf(handler()).addPackageInstall(with(mockUser), with(SID), with(PACKAGE_IDS), with(CHAIN_LABEL));
            will(returnValue(ACTION_ID));
        }});

        validateApiContract("/actionchain/addPackageInstall", "POST")
                .withBody(body)
                .onHandlerMethod("addPackageInstall", User.class, Integer.class, List.class, String.class);
    }

    @Test
    public void testAddPackageRemoval() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sid", SID);
        body.put("packageIds", PACKAGE_IDS);
        body.put("chainLabel", CHAIN_LABEL);

        context.checking(new Expectations() {{
            oneOf(handler()).addPackageRemoval(with(mockUser), with(SID), with(PACKAGE_IDS), with(CHAIN_LABEL));
            will(returnValue(ACTION_ID));
        }});

        validateApiContract("/actionchain/addPackageRemoval", "POST")
                .withBody(body)
                .onHandlerMethod("addPackageRemoval", User.class, Integer.class, List.class, String.class);
    }

    @Test
    public void testAddPackageUpgrade() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sid", SID);
        body.put("packageIds", PACKAGE_IDS);
        body.put("chainLabel", CHAIN_LABEL);

        context.checking(new Expectations() {{
            oneOf(handler()).addPackageUpgrade(with(mockUser), with(SID), with(PACKAGE_IDS), with(CHAIN_LABEL));
            will(returnValue(ACTION_ID));
        }});

        validateApiContract("/actionchain/addPackageUpgrade", "POST")
                .withBody(body)
                .onHandlerMethod("addPackageUpgrade", User.class, Integer.class, List.class, String.class);
    }

    @Test
    public void testAddPackageVerify() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sid", SID);
        body.put("packageIds", PACKAGE_IDS);
        body.put("chainLabel", CHAIN_LABEL);

        context.checking(new Expectations() {{
            oneOf(handler()).addPackageVerify(with(mockUser), with(SID), with(PACKAGE_IDS), with(CHAIN_LABEL));
            will(returnValue(ACTION_ID));
        }});

        validateApiContract("/actionchain/addPackageVerify", "POST")
                .withBody(body)
                .onHandlerMethod("addPackageVerify", User.class, Integer.class, List.class, String.class);
    }

    @Test
    public void testAddScriptRun() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sid", SID);
        body.put("chainLabel", CHAIN_LABEL);
        body.put("uid", "root");
        body.put("gid", "root");
        body.put("timeout", 300);
        body.put("scriptBody", "IyEvYmluL2Jhc2gKZWNobyBoZWxsbwo=");

        context.checking(new Expectations() {{
            oneOf(handler()).addScriptRun(with(mockUser), with(SID), with(CHAIN_LABEL), with("root"),
                    with("root"), with(300), with("IyEvYmluL2Jhc2gKZWNobyBoZWxsbwo="));
            will(returnValue(ACTION_ID));
        }});

        validateApiContract("/actionchain/addScriptRun", "POST")
                .withBody(body)
                .onHandlerMethod("addScriptRun", User.class, Integer.class, String.class, String.class,
                        String.class, Integer.class, String.class);
    }

    /**
     * The longer overload gives the scheduled script a label of its own.
     */
    @Test
    public void testAddLabelledScriptRun() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sid", SID);
        body.put("chainLabel", CHAIN_LABEL);
        body.put("scriptLabel", "hello-script");
        body.put("uid", "root");
        body.put("gid", "root");
        body.put("timeout", 300);
        body.put("scriptBody", "IyEvYmluL2Jhc2gKZWNobyBoZWxsbwo=");

        context.checking(new Expectations() {{
            oneOf(handler()).addScriptRun(with(mockUser), with(SID), with(CHAIN_LABEL), with("hello-script"),
                    with("root"), with("root"), with(300), with("IyEvYmluL2Jhc2gKZWNobyBoZWxsbwo="));
            will(returnValue(ACTION_ID));
        }});

        validateApiContract("/actionchain/addScriptRun", "POST")
                .withBody(body)
                .onHandlerMethod("addScriptRun", User.class, Integer.class, String.class, String.class,
                        String.class, String.class, Integer.class, String.class);
    }

    @Test
    public void testAddConfigurationDeployment() throws Exception {
        Map<String, Object> specifier = new LinkedHashMap<>();
        specifier.put("channelLabel", "test-config-channel");
        specifier.put("filePath", "/etc/test.conf");
        specifier.put("revision", 1);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("chainLabel", CHAIN_LABEL);
        body.put("sid", SID);
        body.put("revisionSpecifiers", List.of(specifier));

        context.checking(new Expectations() {{
            oneOf(handler()).addConfigurationDeployment(with(mockUser), with(CHAIN_LABEL), with(SID),
                    with(List.of(specifier)));
            will(returnValue(ACTION_ID));
        }});

        validateApiContract("/actionchain/addConfigurationDeployment", "POST")
                .withBody(body)
                .onHandlerMethod("addConfigurationDeployment", User.class, String.class, Integer.class,
                        List.class);
    }
}
