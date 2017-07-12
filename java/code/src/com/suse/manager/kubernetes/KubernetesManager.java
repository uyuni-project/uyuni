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

package com.suse.manager.kubernetes;

import com.redhat.rhn.domain.image.ImageBuildHistory;
import com.redhat.rhn.domain.image.ImageInfoFactory;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerFactory;
import com.suse.manager.model.kubernetes.ImageUsage;
import com.suse.manager.model.kubernetes.ContainerInfo;
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
 * Created by matei on 7/11/17.
 */
public class KubernetesManager {

    // Logger
    private static final Logger LOG = Logger.getLogger(KubernetesManager.class);

    private static final String DOCKER_PULLABLE = "docker-pullable://";

    private SaltService saltService;

    public KubernetesManager() {
        this.saltService = SaltService.INSTANCE;
    }

    public Set<ImageUsage> getImagesUsage() {
        List<ImageBuildHistory> buildHistory = ImageInfoFactory.listBuildHistory(); // TODO filter by org ?
        // TODO cache buildHistory ?
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

                Optional<String> currentContext = virtHostMgr.getConfigs().stream()
                        .filter(c -> "currentContext".equals(c.getParameter()))
                        .map(p -> p.getValue())
                        .findFirst();

                if (kubeconfig.isPresent() && currentContext.isPresent()) {
                    MgrK8sRunner.ContainersList containers = saltService.getAllContainers(kubeconfig.get(), currentContext.get());

                    for (MgrK8sRunner.Container container : containers.getContainers()) {
                        if (container.getImageId().startsWith(DOCKER_PULLABLE)) {
                            String imgDigest = StringUtils.removeStart(container.getImageId(), DOCKER_PULLABLE);
                            ImageBuildHistory imgBuildHistory = digestToHistory.get(imgDigest);
                            Optional<Integer> imgBuildRevision = Optional.empty();
                            Optional<ImageUsage> usage = Optional.empty();
                            if (imgBuildHistory != null) {
                                imgBuildRevision = Optional.of(imgBuildHistory.getRevisionNumber());
                                long imgInfoId = imgBuildHistory.getImageInfo().getId();
                                usage = Optional.ofNullable(imgToUsage.get(imgInfoId))
                                    .map(Optional::of)
                                    .orElseGet(() -> {
                                        ImageUsage imgUsage = new ImageUsage(imgBuildHistory.getImageInfo());
                                        imgToUsage.put(imgInfoId, imgUsage);
                                        return Optional.of(imgUsage);
                                    });
                            }
                            else {
                                LOG.warn("Image build history not found for digest: " + imgDigest + " (maybe the image was not built by SUSE Manager).");
                                // TODO match by repository/name:tag
                                continue;
                            }

                            ContainerInfo containerUsage = new ContainerInfo();
                            containerUsage.setContainerId(container.getContainerId());
                            containerUsage.setPodName(container.getPodName());
                            containerUsage.setBuildRevision(imgBuildRevision);
                            containerUsage.setVirtualHostManager(virtHostMgr);
                            usage.ifPresent(u -> {
                                u.getContainerInfos().add(containerUsage);;
                            });
                        } else {
                            // TODO match docker:// prefix by container id ?
                        }

                    }
                } else {
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
