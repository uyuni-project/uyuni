/**
 * Copyright (c) 2017 SUSE LLC
 * <p>
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 * <p>
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.manager.kubernetes.test;

import com.redhat.rhn.domain.image.ImageBuildHistory;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.ImageRepoDigest;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerFactory;
import com.redhat.rhn.testing.ImageTestUtils;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.suse.manager.kubernetes.KubernetesManager;
import com.suse.manager.model.kubernetes.ContainerInfo;
import com.suse.manager.model.kubernetes.ImageUsage;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.services.impl.runner.MgrK8sRunner;
import com.suse.salt.netapi.parser.JsonParser;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Test for KubernetesManager.
 */
public class KubernetesManagerTest extends JMockBaseTestCaseWithUser {

    private SaltService saltServiceMock;
    private KubernetesManager manager;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ClassImposteriser.INSTANCE);

        saltServiceMock = mock(SaltService.class);
        manager = new KubernetesManager();
        manager.setSaltService(saltServiceMock);

        for (VirtualHostManager virtHostMgr : VirtualHostManagerFactory.getInstance().listVirtualHostManagers()) {
            VirtualHostManagerFactory.getInstance().delete(virtHostMgr);
        }
    }

    /**
     * Basic tests
     * @throws Exception
     */
    public void testGetContainersUsage() throws Exception {
        expectGetAllContainers("local-context", "get_all_containers.basic.json");

        VirtualHostManager cluster1 = createVirtHostManager();

        ImageInfo imgInfo = ImageTestUtils.createImageInfo("jocatalin/kubernetes-bootcamp", "v1", user);

        ImageBuildHistory history1 = createImageBuildHistory(imgInfo, 1,
                "jocatalin/kubernetes-bootcamp@sha256:0d6b8ee63bb57c5f5b6156f446b3bc3b3c143d233037f3a2f00e279c8fcc64af");

        imgInfo.getBuildHistory().add(history1);
        imgInfo.setRevisionNumber(1);

        TestUtils.saveAndFlush(imgInfo);

        Set<ImageUsage> usages = manager.getImagesUsage();

        assertEquals(1, usages.size());
        assertEquals(imgInfo, usages.iterator().next().getImageInfo());
        assertTrue(getMatchingContainers(imgInfo, usages).allMatch(c -> c.getBuildRevision().get() == 1));
        assertTrue(getMatchingContainers(imgInfo, usages).allMatch(c -> c.getVirtualHostManager().equals(cluster1)));

        assertEquals(2,
                getMatchingContainers(imgInfo, usages).filter(c -> "default".equals(c.getPodNamespace())).count());
        assertEquals(1,
                getMatchingContainers(imgInfo, usages).filter(c -> "other".equals(c.getPodNamespace())).count());
    }

    /**
     * Containers have two different versions of the same image.
     * @throws Exception
     */
    public void testGetContainersUsageMultipleVersions() throws Exception {
        expectGetAllContainers("local-context", "get_all_containers.multiple_versions.json");

        createVirtHostManager();

        ImageInfo imgInfo = ImageTestUtils.createImageInfo("jocatalin/kubernetes-bootcamp", "v2", user);

        ImageBuildHistory history1 = createImageBuildHistory(imgInfo, 1,
                "jocatalin/kubernetes-bootcamp@sha256:0d6b8ee63bb57c5f5b6156f446b3bc3b3c143d233037f3a2f00e279c8fcc1111");
        ImageBuildHistory history2 = createImageBuildHistory(imgInfo, 2,
                "jocatalin/kubernetes-bootcamp@sha256:0d6b8ee63bb57c5f5b6156f446b3bc3b3c143d233037f3a2f00e279c8fcc2222");

        imgInfo.getBuildHistory().add(history1);
        imgInfo.getBuildHistory().add(history2);
        imgInfo.setRevisionNumber(2);

        TestUtils.saveAndFlush(imgInfo);

        Set<ImageUsage> usages = manager.getImagesUsage();

        assertEquals(1, usages.size());
        assertEquals(imgInfo, usages.iterator().next().getImageInfo());
        assertEquals(3, getMatchingContainers(imgInfo, usages).count());
        assertEquals(1, getMatchingContainers(imgInfo, usages).filter(c -> c.getBuildRevision().get() == 1).count());
        assertEquals(2, getMatchingContainers(imgInfo, usages).filter(c -> c.getBuildRevision().get() == 2).count());
    }

    /**
     * Two clusters running the same image in different versions.
     * @throws Exception
     */
    public void testGetContainersUsageMultipleClusters() throws Exception {
        expectGetAllContainers("/srv/salt/kubeconfig1", "local-context", "get_all_containers.cluster1.json");
        expectGetAllContainers("/srv/salt/kubeconfig2", "local-context", "get_all_containers.cluster2.json");
        VirtualHostManager cluster1 = createVirtHostManager("/srv/salt/kubeconfig1");
        VirtualHostManager cluster2 = createVirtHostManager("/srv/salt/kubeconfig2");

        ImageInfo imgInfo = ImageTestUtils.createImageInfo("jocatalin/kubernetes-bootcamp", "v2", user);

        ImageBuildHistory history1 = createImageBuildHistory(imgInfo, 1,
                "jocatalin/kubernetes-bootcamp@sha256:0d6b8ee63bb57c5f5b6156f446b3bc3b3c143d233037f3a2f00e279c8fcc1111");
        ImageBuildHistory history2 = createImageBuildHistory(imgInfo, 2,
                "jocatalin/kubernetes-bootcamp@sha256:0d6b8ee63bb57c5f5b6156f446b3bc3b3c143d233037f3a2f00e279c8fcc2222");

        imgInfo.getBuildHistory().add(history1);
        imgInfo.getBuildHistory().add(history2);
        imgInfo.setRevisionNumber(2);

        TestUtils.saveAndFlush(imgInfo);

        Set<ImageUsage> usages = manager.getImagesUsage();

        assertEquals(1, usages.size());
        assertEquals(imgInfo, usages.iterator().next().getImageInfo());
        assertEquals(6, getMatchingContainers(imgInfo, usages).count());
        assertEquals(3,
                getMatchingContainers(imgInfo, usages).filter(c -> c.getVirtualHostManager().equals(cluster1)).count());
        assertEquals(3,
                getMatchingContainers(imgInfo, usages).filter(c -> c.getVirtualHostManager().equals(cluster2)).count());
        assertEquals(2, getMatchingContainers(imgInfo, usages).filter(c -> c.getBuildRevision().get() == 1).count());
        assertEquals(4, getMatchingContainers(imgInfo, usages).filter(c -> c.getBuildRevision().get() == 2).count());
    }

    /**
     * Externally build image (no Image ID in our db). Get three containers with the same names but different tags:
     * 1) :v1
     * 2) :latest
     * 3) :latest
     * In the db we have a history record for 3). Container 2) is built externally.
     * For :v1 there's not corresponding image in the db.
     * Both :latest should be matched, one by Repo Digest and the other one by repo/name:tag. :v1 should not be matched.
     * @throws Exception
     */
    public void testGetContainersUsageExternalBuild() throws Exception {
        expectGetAllContainers("local-context", "get_all_containers.external_build.json");
        createVirtHostManager();

        ImageStore store = ImageTestUtils.createImageStore("test-docker-registry:5000", user);
        ImageInfo imgInfo = ImageTestUtils.createImageInfo("jocatalin/kubernetes-bootcamp", "latest", user);
        imgInfo.setStore(store);
        ImageBuildHistory history1 = createImageBuildHistory(imgInfo, 1,
                "jocatalin/kubernetes-bootcamp@sha256:0d6b8ee63bb57c5f5b6156f446b3bc3b3c143d233037f3a2f00e279c8fcc1111");

        imgInfo.getBuildHistory().add(history1);
        imgInfo.setRevisionNumber(1);

        TestUtils.saveAndFlush(imgInfo);

        Set<ImageUsage> usages = manager.getImagesUsage();

        assertEquals(1, usages.size());
        assertEquals(imgInfo, usages.iterator().next().getImageInfo());
        assertEquals(2, getMatchingContainers(imgInfo, usages).count());
        assertEquals(1,
                getMatchingContainers(imgInfo, usages).filter(c -> c.getBuildRevision().orElse(0) == 1).count());
        assertEquals(1, getMatchingContainers(imgInfo, usages).filter(c -> !c.getBuildRevision().isPresent()).count());
    }

    /**
     * Test with inactive containers (i.e. container id null)
     * @throws Exception
     */
    public void testGetContainersUsageInactiveContainers() throws Exception {
        expectGetAllContainers("local-context", "get_all_containers.inactive_containers.json");

        VirtualHostManager cluster1 = createVirtHostManager();

        ImageInfo imgInfo = ImageTestUtils.createImageInfo("jocatalin/kubernetes-bootcamp", "v1", user);

        ImageBuildHistory history1 = createImageBuildHistory(imgInfo, 1,
                "jocatalin/kubernetes-bootcamp@sha256:0d6b8ee63bb57c5f5b6156f446b3bc3b3c143d233037f3a2f00e279c8fcc64af");

        imgInfo.getBuildHistory().add(history1);
        imgInfo.setRevisionNumber(1);

        TestUtils.saveAndFlush(imgInfo);

        Set<ImageUsage> usages = manager.getImagesUsage();

        assertEquals(1, usages.size());
        assertEquals(imgInfo, usages.iterator().next().getImageInfo());
        assertEquals(2, getMatchingContainers(imgInfo, usages).count());
    }

    private void expectGetAllContainers(String kubeconfig, String context, String file)
        throws IOException, ClassNotFoundException {
        context().checking(new Expectations() { {
            allowing(saltServiceMock).getAllContainers(with(kubeconfig), with(context));
            will(returnValue(Optional.of(new JsonParser<>(MgrK8sRunner.getAllContainers("", "").getReturnType()).parse(
                    TestUtils.readRelativeFile(this, file)))));
        } });
    }

    private void expectGetAllContainers(String context, String file) throws IOException, ClassNotFoundException {
        expectGetAllContainers("/srv/salt/kubeconfig", context, file);
    }

    private Stream<ContainerInfo> getMatchingContainers(ImageInfo imgInfo, Set<ImageUsage> usages) {
        return usages.stream().filter(usage -> usage.getImageInfo().equals(imgInfo))
                .flatMap(usage -> usage.getContainerInfos().stream());
    }

    private VirtualHostManager createVirtHostManager() {
        return createVirtHostManager("/srv/salt/kubeconfig");
    }

    private VirtualHostManager createVirtHostManager(String kubeconfig) {
        Map<String, String> params = new HashMap<>();

        params.put("kubeconfig", kubeconfig);
        params.put("context", "local-context");

        String label = "K8s_" + TestUtils.randomString();

        VirtualHostManager virtualHostManager = VirtualHostManagerFactory.getInstance().createVirtualHostManager(label,
                user.getOrg(), VirtualHostManagerFactory.KUBERNETES, params);

        TestUtils.saveAndFlush(virtualHostManager);
        return virtualHostManager;
    }

    private ImageBuildHistory createImageBuildHistory(ImageInfo imgInfo, int revision, String digest) {
        ImageBuildHistory history = new ImageBuildHistory();
        history.setRevisionNumber(revision);
        history.setImageInfo(imgInfo);
        ImageRepoDigest digest1 = new ImageRepoDigest();
        digest1.setBuildHistory(history);
        digest1.setRepoDigest(digest);
        history.getRepoDigests().add(digest1);
        return history;
    }
}
