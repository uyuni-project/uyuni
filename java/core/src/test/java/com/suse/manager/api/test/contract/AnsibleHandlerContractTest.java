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

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ansible.AnsiblePath;
import com.redhat.rhn.domain.server.ansible.PlaybookPath;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.ansible.AnsibleHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AnsibleHandlerContractTest extends BaseOpenApiTest {

    private static final Integer CONTROL_NODE_ID = 1000010000;
    private static final Integer PATH_ID = 10;
    private static final String PLAYBOOK_PATH = "/etc/ansible/playbooks/site.yml";
    private static final String INVENTORY_PATH = "/etc/ansible/hosts";
    private static final String ACTION_CHAIN_LABEL = "test-chain";
    private static final String EARLIEST = "2026-06-01T10:00:00Z";
    private static final Date EARLIEST_AT = Date.from(Instant.parse(EARLIEST));

    @Override
    protected String getApiNamespace() {
        return "ansible";
    }

    @Override
    protected Class<AnsibleHandler> getHandlerClass() {
        return AnsibleHandler.class;
    }

    private AnsibleHandler handler() {
        return (AnsibleHandler) handlerMock;
    }

    /**
     * Instantiates a domain class that only lets its own package construct it.
     *
     * @param type the class to instantiate
     * @param <T> the type of the instance
     * @return a new instance
     * @throws Exception if the constructor cannot be invoked
     */
    private <T> T newInstance(Class<T> type) throws Exception {
        Constructor<T> constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private AnsiblePath ansiblePath() throws Exception {
        MinionServer controlNode = new MinionServer();
        controlNode.setId(CONTROL_NODE_ID.longValue());

        PlaybookPath path = newInstance(PlaybookPath.class);
        path.setId(PATH_ID.longValue());
        path.setMinionServer(controlNode);
        path.setPath(Path.of(PLAYBOOK_PATH));
        return path;
    }

    @Test
    public void testListAnsiblePaths() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listAnsiblePaths(with(mockUser), with(CONTROL_NODE_ID));
            will(returnValue(List.of(ansiblePath())));
        }});

        validateApiContract("/ansible/listAnsiblePaths", "GET")
                .withParams(Map.of("controlNodeId", new String[] {CONTROL_NODE_ID.toString()}))
                .onHandlerMethod("listAnsiblePaths", User.class, Integer.class);
    }

    @Test
    public void testLookupAnsiblePathById() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).lookupAnsiblePathById(with(mockUser), with(PATH_ID));
            will(returnValue(ansiblePath()));
        }});

        validateApiContract("/ansible/lookupAnsiblePathById", "GET")
                .withParams(Map.of("pathId", new String[] {PATH_ID.toString()}))
                .onHandlerMethod("lookupAnsiblePathById", User.class, Integer.class);
    }

    @Test
    public void testCreateAnsiblePath() throws Exception {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("type", "playbook");
        props.put("server_id", CONTROL_NODE_ID);
        props.put("path", PLAYBOOK_PATH);

        context.checking(new Expectations() {{
            oneOf(handler()).createAnsiblePath(with(mockUser), with(props));
            will(returnValue(ansiblePath()));
        }});

        validateApiContract("/ansible/createAnsiblePath", "POST")
                .withBody(Map.of("props", props))
                .onHandlerMethod("createAnsiblePath", User.class, Map.class);
    }

    @Test
    public void testUpdateAnsiblePath() throws Exception {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("path", PLAYBOOK_PATH);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("pathId", PATH_ID);
        body.put("props", props);

        context.checking(new Expectations() {{
            oneOf(handler()).updateAnsiblePath(with(mockUser), with(PATH_ID), with(props));
            will(returnValue(ansiblePath()));
        }});

        validateApiContract("/ansible/updateAnsiblePath", "POST")
                .withBody(body)
                .onHandlerMethod("updateAnsiblePath", User.class, Integer.class, Map.class);
    }

    @Test
    public void testRemoveAnsiblePath() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).removeAnsiblePath(with(mockUser), with(PATH_ID));
            will(returnValue(1));
        }});

        validateApiContract("/ansible/removeAnsiblePath", "POST")
                .withBody(Map.of("pathId", PATH_ID))
                .onHandlerMethod("removeAnsiblePath", User.class, Integer.class);
    }

    @Test
    public void testFetchPlaybookContents() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("pathId", PATH_ID);
        body.put("playbookRelPath", "site.yml");

        context.checking(new Expectations() {{
            oneOf(handler()).fetchPlaybookContents(with(mockUser), with(PATH_ID), with("site.yml"));
            will(returnValue("---\n- hosts: all\n"));
        }});

        validateApiContract("/ansible/fetchPlaybookContents", "POST")
                .withBody(body)
                .onHandlerMethod("fetchPlaybookContents", User.class, Integer.class, String.class);
    }

    @Test
    public void testSchedulePlaybook() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("playbookPath", PLAYBOOK_PATH);
        body.put("inventoryPath", INVENTORY_PATH);
        body.put("controlNodeId", CONTROL_NODE_ID);
        body.put("earliestOccurrence", EARLIEST);
        body.put("actionChainLabel", ACTION_CHAIN_LABEL);

        context.checking(new Expectations() {{
            oneOf(handler()).schedulePlaybook(with(mockUser), with(PLAYBOOK_PATH), with(INVENTORY_PATH),
                    with(CONTROL_NODE_ID), with(EARLIEST_AT), with(ACTION_CHAIN_LABEL));
            will(returnValue(1L));
        }});

        validateApiContract("/ansible/schedulePlaybook", "POST")
                .withBody(body)
                .onHandlerMethod("schedulePlaybook", User.class, String.class, String.class, Integer.class,
                        Date.class, String.class);
    }

    @Test
    public void testSchedulePlaybookInTestMode() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("playbookPath", PLAYBOOK_PATH);
        body.put("inventoryPath", INVENTORY_PATH);
        body.put("controlNodeId", CONTROL_NODE_ID);
        body.put("earliestOccurrence", EARLIEST);
        body.put("actionChainLabel", ACTION_CHAIN_LABEL);
        body.put("testMode", true);

        context.checking(new Expectations() {{
            oneOf(handler()).schedulePlaybook(with(mockUser), with(PLAYBOOK_PATH), with(INVENTORY_PATH),
                    with(CONTROL_NODE_ID), with(EARLIEST_AT), with(ACTION_CHAIN_LABEL), with(true));
            will(returnValue(1L));
        }});

        validateApiContract("/ansible/schedulePlaybook", "POST")
                .withBody(body)
                .onHandlerMethod("schedulePlaybook", User.class, String.class, String.class, Integer.class,
                        Date.class, String.class, boolean.class);
    }

    @Test
    public void testSchedulePlaybookWithAnsibleArgs() throws Exception {
        Map<String, Object> ansibleArgs = new LinkedHashMap<>();
        ansibleArgs.put("extraVars", "key=value");
        ansibleArgs.put("flushCache", true);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("playbookPath", PLAYBOOK_PATH);
        body.put("inventoryPath", INVENTORY_PATH);
        body.put("controlNodeId", CONTROL_NODE_ID);
        body.put("earliestOccurrence", EARLIEST);
        body.put("actionChainLabel", ACTION_CHAIN_LABEL);
        body.put("ansibleArgs", ansibleArgs);

        context.checking(new Expectations() {{
            oneOf(handler()).schedulePlaybook(with(mockUser), with(PLAYBOOK_PATH), with(INVENTORY_PATH),
                    with(CONTROL_NODE_ID), with(EARLIEST_AT), with(ACTION_CHAIN_LABEL), with(ansibleArgs));
            will(returnValue(1L));
        }});

        validateApiContract("/ansible/schedulePlaybook", "POST")
                .withBody(body)
                .onHandlerMethod("schedulePlaybook", User.class, String.class, String.class, Integer.class,
                        Date.class, String.class, Map.class);
    }

    @Test
    public void testSchedulePlaybookInTestModeWithAnsibleArgs() throws Exception {
        Map<String, Object> ansibleArgs = new LinkedHashMap<>();
        ansibleArgs.put("extraVars", "key=value");
        ansibleArgs.put("flushCache", false);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("playbookPath", PLAYBOOK_PATH);
        body.put("inventoryPath", INVENTORY_PATH);
        body.put("controlNodeId", CONTROL_NODE_ID);
        body.put("earliestOccurrence", EARLIEST);
        body.put("actionChainLabel", ACTION_CHAIN_LABEL);
        body.put("testMode", false);
        body.put("ansibleArgs", ansibleArgs);

        context.checking(new Expectations() {{
            oneOf(handler()).schedulePlaybook(with(mockUser), with(PLAYBOOK_PATH), with(INVENTORY_PATH),
                    with(CONTROL_NODE_ID), with(EARLIEST_AT), with(ACTION_CHAIN_LABEL), with(false),
                    with(ansibleArgs));
            will(returnValue(1L));
        }});

        validateApiContract("/ansible/schedulePlaybook", "POST")
                .withBody(body)
                .onHandlerMethod("schedulePlaybook", User.class, String.class, String.class, Integer.class,
                        Date.class, String.class, boolean.class, Map.class);
    }

    /*
     * discoverPlaybooks and introspectInventory answer a free-form map keyed by the discovered
     * playbook or inventory group, while the legacy documentation sketches that map as a struct
     * with a single fixed property ("playbook" and "inventoryItem"). The OpenAPI documentation
     * mirrors the legacy one faithfully, so the fixtures below are keyed the way both documents
     * describe rather than the way a Salt response really is.
     */

    @Test
    public void testDiscoverPlaybooks() throws Exception {
        Map<String, Object> playbook = new LinkedHashMap<>();
        playbook.put("id", PATH_ID);
        playbook.put("type", "playbook");
        playbook.put("server_id", CONTROL_NODE_ID);
        playbook.put("path", PLAYBOOK_PATH);

        Map<String, Object> discovered = new LinkedHashMap<>();
        discovered.put("playbook", playbook);

        context.checking(new Expectations() {{
            oneOf(handler()).discoverPlaybooks(with(mockUser), with(PATH_ID));
            will(returnValue(discovered));
        }});

        validateApiContract("/ansible/discoverPlaybooks", "POST")
                .withBody(Map.of("pathId", PATH_ID))
                .onHandlerMethod("discoverPlaybooks", User.class, Integer.class);
    }

    @Test
    public void testIntrospectInventory() throws Exception {
        Map<String, Object> inventoryItem = new LinkedHashMap<>();
        inventoryItem.put("hosts", List.of("minion.example.com"));

        Map<String, Object> inventory = new LinkedHashMap<>();
        inventory.put("inventoryItem", inventoryItem);

        context.checking(new Expectations() {{
            oneOf(handler()).introspectInventory(with(mockUser), with(PATH_ID));
            will(returnValue(inventory));
        }});

        validateApiContract("/ansible/introspectInventory", "POST")
                .withBody(Map.of("pathId", PATH_ID))
                .onHandlerMethod("introspectInventory", User.class, Integer.class);
    }
}
