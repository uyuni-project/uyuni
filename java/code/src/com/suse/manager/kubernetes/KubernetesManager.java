/**
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

import com.redhat.rhn.domain.image.ImageBuildHistory;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.ImageInfoFactory;
import com.redhat.rhn.domain.image.ImageStoreFactory;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerFactory;
import com.suse.manager.model.kubernetes.ContainerInfo;
import com.suse.manager.model.kubernetes.ImageUsage;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.services.impl.runner.MgrK8sRunner;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Queries a Kubernetes cluster via the API and matches containers running in the cluster
 * to images known to SUSE Manager.
 *
 */
public class KubernetesManager {

    // Logger
    private static final Logger LOG = Logger.getLogger(KubernetesManager.class);

    private static final String DOCKER_PULLABLE = "docker-pullable://";

    private SaltService saltService;

    /**
     * No arg constructor.
     * Configures this with the default {@link SaltService} instance.
     */
    public KubernetesManager() {
        this.saltService = SaltService.INSTANCE;
    }

    /**
     * Queries all configured Kubernetes clusters and tries to match the containers to all
     * images know to SUSE Manager.
     *
     * @return a set of {@link ImageUsage} objects, one object for each matched {@link ImageInfo}
     */
    public Set<ImageUsage> getImagesUsage() {
        List<ImageBuildHistory> buildHistory = ImageInfoFactory.listBuildHistory();
        Map<String, ImageBuildHistory> digestToHistory = buildHistory
                .stream()
                .flatMap(build -> build.getRepoDigests().stream())
                .collect(Collectors.toMap(d -> d.getRepoDigest(), d -> d.getBuildHistory()));

        Map<Long, ImageUsage> imgToUsage = new HashMap<>();

        for (VirtualHostManager virtHostMgr : VirtualHostManagerFactory.getInstance().listVirtualHostManagers()) {
            if ("kubernetes".equals(virtHostMgr.getGathererModule())) {
                Optional<String> kubeconfig = virtHostMgr.getConfigs().stream()
                        .filter(c -> "kubeconfig".equals(c.getParameter()))
                        .map(p -> p.getValue())
                        .findFirst();

                Optional<String> context = virtHostMgr.getConfigs().stream()
                        .filter(c -> "context".equals(c.getParameter()))
                        .map(p -> p.getValue())
                        .findFirst();

                if (kubeconfig.isPresent() && context.isPresent()) {
                    MgrK8sRunner.ContainersList containers = saltService.getAllContainers(kubeconfig.get(), context.get());

                    containers.getContainers().stream().forEach(container -> {
                        if (container.getImageId().startsWith(DOCKER_PULLABLE)) {
                            String imgDigest = StringUtils.removeStart(container.getImageId(), DOCKER_PULLABLE);
                            ImageBuildHistory imgBuildHistory = digestToHistory.get(imgDigest);
                            Optional<Integer> imgBuildRevision = Optional.empty();
                            Optional<ImageUsage> usage;
                            if (imgBuildHistory != null) {
                                imgBuildRevision = Optional.of(imgBuildHistory.getRevisionNumber());
                                usage = Optional.of(
                                        imgToUsage.computeIfAbsent(imgBuildHistory.getImageInfo().getId(),
                                                (infoId) ->
                                                    new ImageUsage(imgBuildHistory.getImageInfo())
                                ));
                            }
                            else {
                                LOG.debug("Image build history not found for digest: " +
                                        imgDigest + " (maybe the image was not built by SUSE Manager).");
                                String[] tokens = StringUtils.split(container.getImage(), "/", 2);
                                String repo = tokens[0];
                                String[] imgTag = StringUtils.split(tokens[1], ":", 2);
                                String name = imgTag[0];
                                String tag = imgTag[1];

                                Optional<ImageInfo> imgByRepoNameTag =
                                        ImageStoreFactory.lookupBylabel(repo)
                                        .flatMap(st -> ImageInfoFactory.lookupByName(name, tag, st.getId()));
                                usage = imgByRepoNameTag
                                        .map(imgInfo -> imgToUsage.get(imgInfo.getId()))
                                        .map(Optional::of)
                                        .orElseGet(() -> imgByRepoNameTag
                                            .map(imgInfo -> new ImageUsage(imgInfo))
                                            .map(usg -> {
                                                imgToUsage.put(usg.getImageInfo().getId(), usg);
                                                return usg;
                                            }));
                                if (!usage.isPresent()) {
                                    return;
                                }
                            }

                            ContainerInfo containerUsage = new ContainerInfo();
                            containerUsage.setContainerId(container.getContainerId());
                            containerUsage.setPodName(container.getPodName());
                            containerUsage.setBuildRevision(imgBuildRevision);
                            containerUsage.setVirtualHostManager(virtHostMgr);
                            usage.ifPresent(u ->
                                u.getContainerInfos().add(containerUsage)
                            );
                        }
                        else {
                            // TODO match docker:// prefix by container id ?
                        }

                    });
                }
                else {
                    LOG.debug("VirtualHostManager " + virtHostMgr.getLabel() + " lacks 'kubeconfig' and/or 'currentContext' config parameters.");
                }
            }
        }
        return imgToUsage.values().stream().collect(Collectors.toSet());
    }

    /**
     * @param saltServiceIn to set
     */
    public void setSaltService(SaltService saltServiceIn) {
        this.saltService = saltServiceIn;
    }
}
