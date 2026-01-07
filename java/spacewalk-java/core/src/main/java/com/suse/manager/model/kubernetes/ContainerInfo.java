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

package com.suse.manager.model.kubernetes;

import static com.suse.manager.model.kubernetes.ImageUsage.RUNTIME_OUTOFDATE;
import static com.suse.manager.model.kubernetes.ImageUsage.RUNTIME_UNKNOWN;
import static com.suse.manager.model.kubernetes.ImageUsage.RUNTIME_UPTODATE;

import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;

import java.util.Optional;

/**
 * Information about the usage of a Docker container in
 * a Kubernetes cluster.
 */
public class ContainerInfo {

    private String containerId;
    private String podName;
    private String podNamespace;
    private Optional<Integer> buildRevision;
    private VirtualHostManager virtualHostManager;

    /**
     * @return the container id.
     */
    public String getContainerId() {
        return containerId;
    }

    /**
     * @param containerIdIn to set
     */
    public void setContainerId(String containerIdIn) {
        this.containerId = containerIdIn;
    }

    /**
     * @return the pod name.
     */
    public String getPodName() {
        return podName;
    }

    /**
     * @param podNameIn to set
     */
    public void setPodName(String podNameIn) {
        this.podName = podNameIn;
    }

    /**
     * @return the pod namespace
     */
    public String getPodNamespace() {
        return podNamespace;
    }

    /**
     * @param podNamespaceIn the pod namespace
     */
    public void setPodNamespace(String podNamespaceIn) {
        this.podNamespace = podNamespaceIn;
    }

    /**
     * @return the build revision.
     */
    public Optional<Integer> getBuildRevision() {
        return buildRevision;
    }

    /**
     * @param buildRevisionIn to set
     */
    public void setBuildRevision(Optional<Integer> buildRevisionIn) {
        this.buildRevision = buildRevisionIn;
    }

    /**
     * @return the virtual host manager.
     */
    public VirtualHostManager getVirtualHostManager() {
        return virtualHostManager;
    }

    /**
     * @param virtualHostManagerIn to set
     */
    public void setVirtualHostManager(VirtualHostManager virtualHostManagerIn) {
        this.virtualHostManager = virtualHostManagerIn;
    }

    /**
     * Gets container runtime currentness status, relative to the current revision number.
     *
     * @param currentRevision the current revision number
     * @return the runtime status
     */
    public int getRuntimeStatus(int currentRevision) {
        return getBuildRevision().map(
                r -> r < currentRevision ? RUNTIME_OUTOFDATE : RUNTIME_UPTODATE)
                .orElse(RUNTIME_UNKNOWN);
    }
}
