/*
 * Copyright (c) 2017 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.manager.kubernetes;

import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.ImageInfoFactory;
import com.redhat.rhn.domain.image.ImageRepoDigest;
import com.redhat.rhn.domain.image.ImageStoreFactory;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerConfig;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerFactory;

import com.suse.manager.model.kubernetes.ContainerInfo;
import com.suse.manager.model.kubernetes.ImageUsage;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.services.impl.runner.MgrK8sRunner;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Queries a Kubernetes cluster via the API and matches containers running in the cluster
 * to images known to SUSE Manager.
 */
public class KubernetesManager {

    // Logger
    private static final Logger LOG = Logger.getLogger(KubernetesManager.class);
    private static final String DOCKER_PULLABLE = "docker-pullable://";

    private final SaltApi saltApi;

    /**
     * No arg constructor.
     * Configures this with the default {@link SaltService} instance.
     * @param saltApiIn instance for getting information from a system.
     */
    public KubernetesManager(SaltApi saltApiIn) {
        this.saltApi = saltApiIn;
    }

    /**
     * Queries all configured Kubernetes clusters and tries to match the containers to all
     * images know to SUSE Manager.
     *
     * @return a set of {@link ImageUsage} objects, one object for each matched
     * {@link ImageInfo}
     */
    public Set<ImageUsage> getImagesUsage() {
        return getImagesUsage(null);
    }

    /**
     * Queries the specified Kubernetes cluster and tries to match the containers to all
     * images know to SUSE Manager.
     *
     * @param virtualHostManager the virtual host manager of type "kubernetes"
     * @return a set of {@link ImageUsage} objects, one object for each matched
     * {@link ImageInfo}
     */
    public Set<ImageUsage> getImagesUsage(VirtualHostManager virtualHostManager) {
        List<ImageRepoDigest> imageRepoDigests = ImageInfoFactory.listImageRepoDigests();
        Map<String, ImageInfo> digestToInfo =
                imageRepoDigests.stream().collect(Collectors.toMap(ImageRepoDigest::getRepoDigest,
                                ImageRepoDigest::getImageInfo));

        Map<Long, ImageUsage> imgToUsage = new HashMap<>();

        Consumer<VirtualHostManager> processCluster = virtHostMgr -> {
            if (VirtualHostManagerFactory.KUBERNETES
                    .equals(virtHostMgr.getGathererModule())) {
                Optional<String> kubeconfig = virtHostMgr.getConfigs().stream()
                        .filter(c -> "kubeconfig".equals(c.getParameter()))
                        .map(VirtualHostManagerConfig::getValue)
                        .findFirst();

                Optional<String> context = virtHostMgr.getConfigs().stream()
                        .filter(c -> "context".equals(c.getParameter()))
                        .map(VirtualHostManagerConfig::getValue)
                        .findFirst();

                if (kubeconfig.isPresent() && context.isPresent()) {
                    Optional<List<MgrK8sRunner.Container>> containers =
                            saltApi.getAllContainers(kubeconfig.get(), context.get());

                    if (!containers.isPresent()) {
                        LOG.error("No container info returned by runner call " +
                                "[mgrk8s.get_all_containers]");
                        return;
                    }

                    // Loop through 'running' containers (with container id present)
                    containers.get().stream()
                            .filter(c -> c.getContainerId().isPresent()).forEach(container -> {
                        String imgDigest = container.getImageId();
                        if (imgDigest.startsWith(DOCKER_PULLABLE)) {
                            imgDigest = StringUtils.removeStart(container.getImageId(), DOCKER_PULLABLE);
                        }
                        ImageInfo imageInfo = digestToInfo.get(imgDigest);
                        Optional<Integer> imgBuildRevision = Optional.empty();
                        Optional<ImageUsage> usage;
                        if (imageInfo != null) {
                            imgBuildRevision = Optional.of(imageInfo.getRevisionNumber());
                            usage = Optional.of(imgToUsage.computeIfAbsent(
                                    imageInfo.getId(),
                                    (infoId) -> new ImageUsage(imageInfo)));
                        }
                        else {
                            LOG.debug("Image build history not found for digest: " +
                                    imgDigest + " (maybe the image was not built " +
                                    "by SUSE Manager).");
                            String[] tokens =
                                    StringUtils.split(container.getImage(), "/", 2);
                            if (tokens.length < 2) {
                                LOG.debug("No repository available in the image name '" +
                                        container.getImage() +
                                        "'. Ignoring the image.");
                                return;
                            }

                            String repo = tokens[0];
                            String[] imgTag = StringUtils.split(tokens[1], ":", 2);
                            String name = imgTag[0];
                            String tag = imgTag.length > 1 ? imgTag[1] : "latest";

                            Optional<ImageInfo> imgByRepoNameTag =
                                    ImageStoreFactory.lookupBylabelAndOrg(repo, virtHostMgr.getOrg())
                                            .flatMap(st -> ImageInfoFactory.lookupByName(name, tag, st.getId()));
                            usage = imgByRepoNameTag
                                    .map(imgInfo -> imgToUsage.get(imgInfo.getId()))
                                    .map(Optional::of).orElseGet(() -> imgByRepoNameTag
                                            .map(ImageUsage::new)
                                            .map(usg -> {
                                                imgToUsage
                                                        .put(usg.getImageInfo().getId(),
                                                                usg);
                                                return usg;
                                            }));
                            if (!usage.isPresent()) {
                                LOG.debug("Usage of the image not found, exiting");
                                return;
                            }
                        }

                        ContainerInfo containerUsage = new ContainerInfo();
                        containerUsage.setContainerId(container.getContainerId().get());
                        containerUsage.setPodName(container.getPodName());
                        containerUsage.setPodNamespace(container.getPodNamespace());
                        containerUsage.setBuildRevision(imgBuildRevision);
                        containerUsage.setVirtualHostManager(virtHostMgr);
                        usage.ifPresent(u -> u.getContainerInfos().add(containerUsage));
                    });
                }
                else {
                    LOG.debug("VirtualHostManager " + virtHostMgr.getLabel() +
                            " lacks 'kubeconfig' and/or 'currentContext'" +
                            " config parameters.");
                }
            }
        };

        if (virtualHostManager != null) {
            //Process single cluster
            processCluster.accept(virtualHostManager);
        }
        else {
            //Process all registered clusters
            VirtualHostManagerFactory.getInstance().listVirtualHostManagers()
                    .forEach(processCluster);
        }

        return imgToUsage.values().stream().collect(Collectors.toSet());
    }

}
