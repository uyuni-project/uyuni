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

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.kickstart.KickstartInstallType;
import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.kickstart.KickstartableTreeDetail;
import com.redhat.rhn.frontend.xmlrpc.kickstart.tree.KickstartTreeHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class KickstartTreeHandlerContractTest extends BaseOpenApiTest {

    private static final String TREE_LABEL = "sles15-sp6-x86_64";
    private static final String CHANNEL_LABEL = "sle-product-sles15-sp6-pool-x86_64";
    private static final String BASE_PATH = "/srv/www/distributions/sles15-sp6";

    @Override
    protected String getApiNamespace() {
        return "kickstart.tree";
    }

    @Override
    protected Class<KickstartTreeHandler> getHandlerClass() {
        return KickstartTreeHandler.class;
    }

    private KickstartTreeHandler handler() {
        return (KickstartTreeHandler) handlerMock;
    }

    /**
     * Both domain classes declare {@code setId} as protected and the serializers document the id
     * as a required property, so the fixtures set it reflectively. A subclass is not an option
     * here: Gson serializes anonymous and local classes as {@code null}.
     *
     * @param entity the entity to set the id on
     * @param declaringClass the class declaring the setter
     * @param id the id to set
     * @throws Exception if the setter cannot be invoked
     */
    private void setId(Object entity, Class<?> declaringClass, Long id) throws Exception {
        Method setter = declaringClass.getDeclaredMethod("setId", Long.class);
        setter.setAccessible(true);
        setter.invoke(entity, id);
    }

    private KickstartInstallType installType() throws Exception {
        KickstartInstallType installType = new KickstartInstallType();
        setId(installType, KickstartInstallType.class, 1L);
        installType.setLabel("sles15generic");
        installType.setName("SUSE Linux Enterprise 15");
        return installType;
    }

    private KickstartableTree tree() throws Exception {
        Channel channel = new Channel();
        channel.setId(101L);

        KickstartableTree tree = new KickstartableTree();
        setId(tree, KickstartableTree.class, 42L);
        tree.setLabel(TREE_LABEL);
        tree.setBasePath(BASE_PATH);
        tree.setChannel(channel);
        tree.setOrg(fakeOrg);
        tree.setInstallType(installType());
        tree.setKernelOptions("net.ifnames=0");
        tree.setKernelOptionsPost("quiet");
        return tree;
    }

    @Test
    public void testGetDetails() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getDetails(with(mockUser), with(TREE_LABEL));
            will(returnValue(new KickstartableTreeDetail(tree())));
        }});

        validateApiContract("/kickstart.tree/getDetails", "GET")
                .withParams(Map.of("treeLabel", new String[] {TREE_LABEL}))
                .onHandlerMethod("getDetails", User.class, String.class);
    }

    @Test
    public void testList() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).list(with(mockUser), with(CHANNEL_LABEL));
            will(returnValue(List.of(tree())));
        }});

        validateApiContract("/kickstart.tree/list", "POST")
                .withBody(Map.of("channelLabel", CHANNEL_LABEL))
                .onHandlerMethod("list", User.class, String.class);
    }

    @Test
    public void testListInstallTypes() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listInstallTypes(with(mockUser));
            will(returnValue(List.of(installType())));
        }});

        validateApiContract("/kickstart.tree/listInstallTypes", "GET")
                .onHandlerMethod("listInstallTypes", User.class);
    }

    @Test
    public void testCreate() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("treeLabel", TREE_LABEL);
        body.put("basePath", BASE_PATH);
        body.put("channelLabel", CHANNEL_LABEL);
        body.put("installType", "sles15generic");
        body.put("kernelOptions", "net.ifnames=0");
        body.put("postKernelOptions", "quiet");

        context.checking(new Expectations() {{
            oneOf(handler()).create(with(mockUser), with(TREE_LABEL), with(BASE_PATH), with(CHANNEL_LABEL),
                    with("sles15generic"), with("net.ifnames=0"), with("quiet"));
            will(returnValue(1));
        }});

        validateApiContract("/kickstart.tree/create", "POST")
                .withBody(body)
                .onHandlerMethod("create", User.class, String.class, String.class, String.class, String.class,
                        String.class, String.class);
    }

    @Test
    public void testUpdate() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("treeLabel", TREE_LABEL);
        body.put("basePath", BASE_PATH);
        body.put("channelLabel", CHANNEL_LABEL);
        body.put("installType", "sles15generic");
        body.put("kernelOptions", "net.ifnames=0");
        body.put("postKernelOptions", "quiet");

        context.checking(new Expectations() {{
            oneOf(handler()).update(with(mockUser), with(TREE_LABEL), with(BASE_PATH), with(CHANNEL_LABEL),
                    with("sles15generic"), with("net.ifnames=0"), with("quiet"));
            will(returnValue(1));
        }});

        validateApiContract("/kickstart.tree/update", "POST")
                .withBody(body)
                .onHandlerMethod("update", User.class, String.class, String.class, String.class, String.class,
                        String.class, String.class);
    }

    @Test
    public void testRename() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("originalLabel", TREE_LABEL);
        body.put("newLabel", "sles15-sp7-x86_64");

        context.checking(new Expectations() {{
            oneOf(handler()).rename(with(mockUser), with(TREE_LABEL), with("sles15-sp7-x86_64"));
            will(returnValue(1));
        }});

        validateApiContract("/kickstart.tree/rename", "POST")
                .withBody(body)
                .onHandlerMethod("rename", User.class, String.class, String.class);
    }

    @Test
    public void testDelete() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).delete(with(mockUser), with(TREE_LABEL));
            will(returnValue(1));
        }});

        validateApiContract("/kickstart.tree/delete", "POST")
                .withBody(Map.of("treeLabel", TREE_LABEL))
                .onHandlerMethod("delete", User.class, String.class);
    }

    @Test
    public void testDeleteTreeAndProfiles() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).deleteTreeAndProfiles(with(mockUser), with(TREE_LABEL));
            will(returnValue(1));
        }});

        validateApiContract("/kickstart.tree/deleteTreeAndProfiles", "POST")
                .withBody(Map.of("treeLabel", TREE_LABEL))
                .onHandlerMethod("deleteTreeAndProfiles", User.class, String.class);
    }
}
