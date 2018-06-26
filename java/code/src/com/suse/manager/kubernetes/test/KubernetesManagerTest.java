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
import java.util.function.Predicate;
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
     * Containers use the same image version.
     * @throws Exception
     */
    public void testGetContainersUsage_sameVersion() throws Exception {
        expectGetAllContainers("local-context", "get_all_containers.same_version.json");

        VirtualHostManager cluster1 = createVirtHostManager();

        ImageInfo imgInfo = ImageTestUtils.createImageInfo("jocatalin/kubernetes-bootcamp", "v1", user);

        ImageBuildHistory history1 = createImageBuildHistory(imgInfo, 1, "jocatalin/kubernetes-bootcamp@sha256:0d6b8ee63bb57c5f5b6156f446b3bc3b3c143d233037f3a2f00e279c8fcc64af");

        imgInfo.getBuildHistory().add(history1);
        imgInfo.setRevisionNumber(1);

        TestUtils.saveAndFlush(imgInfo);

        Set<ImageUsage> usages = manager.getImagesUsage();

        assertEquals(1, usages.size());
        assertTrue(usages.stream().filter(usage -> usage.getImageInfo().getName().equals(imgInfo.getName())).findFirst().isPresent());
        assertEquals(3,
                matchContainers(imgInfo, usages, container -> container.getBuildRevision().get() == 1)
                        .count());
        assertEquals(3,
                matchContainers(imgInfo, usages, container -> container.getVirtualHostManager().getId() == cluster1.getId())
                        .count());
    }

    /**
     * Containers have two different versions of the same image.
     * @throws Exception
     */
    public void testGetContainersUsage_multipleVersions() throws Exception {
        expectGetAllContainers("local-context", "get_all_containers.multiple_versions.json");

        createVirtHostManager();

        ImageInfo imgInfo = ImageTestUtils.createImageInfo("jocatalin/kubernetes-bootcamp", "v2", user);

        ImageBuildHistory history1 = createImageBuildHistory(imgInfo, 1, "jocatalin/kubernetes-bootcamp@sha256:0d6b8ee63bb57c5f5b6156f446b3bc3b3c143d233037f3a2f00e279c8fcc1111");
        ImageBuildHistory history2 = createImageBuildHistory(imgInfo, 2, "jocatalin/kubernetes-bootcamp@sha256:0d6b8ee63bb57c5f5b6156f446b3bc3b3c143d233037f3a2f00e279c8fcc2222");

        imgInfo.getBuildHistory().add(history1);
        imgInfo.getBuildHistory().add(history2);
        imgInfo.setRevisionNumber(2);

        TestUtils.saveAndFlush(imgInfo);

        Set<ImageUsage> usages = manager.getImagesUsage();

        assertEquals(1, usages.size());
        assertTrue(usages.stream().filter(usage -> usage.getImageInfo().getName().equals(imgInfo.getName())).findFirst().isPresent());
        assertEquals(3,
                matchContainers(imgInfo, usages, container -> true)
                        .count());
        assertEquals(1,
                matchContainers(imgInfo, usages, container -> container.getBuildRevision().get() == 1)
                        .count());
        assertEquals(2,
                matchContainers(imgInfo, usages, container -> container.getBuildRevision().get() == 2)
                        .count());
    }

    /**
     * Two clusters running the same image in different versions.
     * @throws Exception
     */
    public void testGetContainersUsage_multipleClusters() throws Exception {
        expectGetAllContainers("/srv/salt/kubeconfig1", "local-context", "get_all_containers.cluster1.json");
        expectGetAllContainers("/srv/salt/kubeconfig2", "local-context", "get_all_containers.cluster2.json");
        VirtualHostManager cluster1 = createVirtHostManager("/srv/salt/kubeconfig1");
        VirtualHostManager cluster2 = createVirtHostManager("/srv/salt/kubeconfig2");

        ImageInfo imgInfo = ImageTestUtils.createImageInfo("jocatalin/kubernetes-bootcamp", "v2", user);

        ImageBuildHistory history1 = createImageBuildHistory(imgInfo, 1, "jocatalin/kubernetes-bootcamp@sha256:0d6b8ee63bb57c5f5b6156f446b3bc3b3c143d233037f3a2f00e279c8fcc1111");
        ImageBuildHistory history2 = createImageBuildHistory(imgInfo, 2, "jocatalin/kubernetes-bootcamp@sha256:0d6b8ee63bb57c5f5b6156f446b3bc3b3c143d233037f3a2f00e279c8fcc2222");

        imgInfo.getBuildHistory().add(history1);
        imgInfo.getBuildHistory().add(history2);
        imgInfo.setRevisionNumber(2);

        TestUtils.saveAndFlush(imgInfo);

        Set<ImageUsage> usages = manager.getImagesUsage();

        assertEquals(1, usages.size());
        assertTrue(usages.stream().filter(usage -> usage.getImageInfo().getName().equals(imgInfo.getName())).findFirst().isPresent());
        assertEquals(6,
                matchContainers(imgInfo, usages, container -> true)
                        .count());
        assertEquals(3,
                matchContainers(imgInfo, usages, container -> container.getVirtualHostManager().equals(cluster1))
                        .count());
        assertEquals(3,
                matchContainers(imgInfo, usages, container -> container.getVirtualHostManager().equals(cluster2))
                        .count());
        assertEquals(2,
                matchContainers(imgInfo, usages, container -> container.getBuildRevision().get() == 1)
                        .count());
        assertEquals(4,
                matchContainers(imgInfo, usages, container -> container.getBuildRevision().get() == 2)
                        .count());
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
    public void testGetContainersUsage_externalBuild() throws Exception {
        expectGetAllContainers("local-context", "get_all_containers.external_build.json");

        VirtualHostManager cluster1 = createVirtHostManager();

        ImageStore store = ImageTestUtils.createImageStore("test-docker-registry:5000", user);
        ImageInfo imgInfo = ImageTestUtils.createImageInfo("jocatalin/kubernetes-bootcamp", "latest", user);
        imgInfo.setStore(store);
        ImageBuildHistory history1 = createImageBuildHistory(imgInfo, 1, "jocatalin/kubernetes-bootcamp@sha256:0d6b8ee63bb57c5f5b6156f446b3bc3b3c143d233037f3a2f00e279c8fcc1111");

        imgInfo.getBuildHistory().add(history1);
        imgInfo.setRevisionNumber(1);

        TestUtils.saveAndFlush(imgInfo);

        Set<ImageUsage> usages = manager.getImagesUsage();

        assertEquals(1, usages.size());
        assertTrue(usages.stream().filter(usage -> usage.getImageInfo().getName().equals(imgInfo.getName())).findFirst().isPresent());
        assertEquals(2,
                matchContainers(imgInfo, usages, container -> true)
                        .count());
        assertEquals(1,
                matchContainers(imgInfo, usages, container -> container.getBuildRevision().orElse(0) == 1)
                        .count());
        assertEquals(1,
                matchContainers(imgInfo, usages, container -> !container.getBuildRevision().isPresent())
                        .count());
    }

    /**
     * Test with inactive containers (i.e. container id null)
     * @throws Exception
     */
    public void testGetContainersUsage_inactiveContainers() throws Exception {
        expectGetAllContainers("local-context", "get_all_containers.inactive_containers.json");

        VirtualHostManager cluster1 = createVirtHostManager();

        ImageInfo imgInfo = ImageTestUtils.createImageInfo("jocatalin/kubernetes-bootcamp", "v1", user);

        ImageBuildHistory history1 = createImageBuildHistory(imgInfo, 1, "jocatalin/kubernetes-bootcamp@sha256:0d6b8ee63bb57c5f5b6156f446b3bc3b3c143d233037f3a2f00e279c8fcc64af");

        imgInfo.getBuildHistory().add(history1);
        imgInfo.setRevisionNumber(1);

        TestUtils.saveAndFlush(imgInfo);

        Set<ImageUsage> usages = manager.getImagesUsage();

        assertEquals(1, usages.size());
        assertTrue(usages.stream().filter(usage -> usage.getImageInfo().getName().equals(imgInfo.getName())).findFirst().isPresent());
        assertEquals(2, matchContainers(imgInfo, usages).count());
    }


    private void expectGetAllContainers(String kubeconfig, String context, String file) throws IOException, ClassNotFoundException {
        context().checking(new Expectations() { {
            allowing(saltServiceMock).getAllContainers(with(kubeconfig), with(context));
            will(returnValue(Optional.of(new JsonParser<>(MgrK8sRunner.getAllContainers("", "").getReturnType()).parse(
                    TestUtils.readRelativeFile(this, file)))));
        } });
    }

    private void expectGetAllContainers(String context, String file) throws IOException, ClassNotFoundException {
        expectGetAllContainers("/srv/salt/kubeconfig", context, file);
    }

    private Stream<ContainerInfo> matchContainers(ImageInfo imgInfo, Set<ImageUsage> usages) {
        return matchContainers(imgInfo, usages, null);
    }

    private Stream<ContainerInfo> matchContainers(ImageInfo imgInfo, Set<ImageUsage> usages, Predicate<? super ContainerInfo> filter) {
        Stream<ContainerInfo> stream = usages.stream()
                .filter(usage -> usage.getImageInfo().getName().equals(imgInfo.getName()))
                .flatMap(usage -> usage.getContainerInfos().stream());

        if (filter != null) {
            stream = stream.filter(filter);
        }

        return stream;
    }

    private VirtualHostManager createVirtHostManager(String... kubeconfig) {
        Map params = new HashMap<>();
        if (kubeconfig.length > 0) {
            params.put("kubeconfig", kubeconfig[0]);
        } else {
            params.put("kubeconfig", "/srv/salt/kubeconfig");
        }
        params.put("context", "local-context");

        String label = "K8s_" + TestUtils.randomString();

        VirtualHostManager virtualHostManager = VirtualHostManagerFactory.getInstance().createVirtualHostManager(
                label,
                user.getOrg(),
                VirtualHostManagerFactory.KUBERNETES,
                params
        );
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
