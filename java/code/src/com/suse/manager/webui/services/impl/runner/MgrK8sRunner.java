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

package com.suse.manager.webui.services.impl.runner;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.suse.salt.netapi.calls.RunnerCall;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by matei on 7/11/17.
 */
public class MgrK8sRunner {

    public static class Container {

        @SerializedName("image")
        private String image;

        @SerializedName("image_id")
        private String imageId;

        @SerializedName("container_id")
        private String containerId;

        @SerializedName("pod_name")
        private String podName;

        public String getImage() {
            return image;
        }

        /**
         * @param imageIn to set
         */
        public void setImage(String imageIn) {
            this.image = imageIn;
        }

        public String getImageId() {
            return imageId;
        }

        /**
         * @param imageIdIn to set
         */
        public void setImageId(String imageIdIn) {
            this.imageId = imageIdIn;
        }

        public String getContainerId() {
            return containerId;
        }

        /**
         * @param containerIdIn to set
         */
        public void setContainerId(String containerIdIn) {
            this.containerId = containerIdIn;
        }

        public String getPodName() {
            return podName;
        }

        /**
         * @param podNameIn to set
         */
        public void setPodName(String podNameIn) {
            this.podName = podNameIn;
        }
    }

    public static class ContainersList {

        @SerializedName("containers")
        private List<Container> containers;

        public List<Container> getContainers() {
            return containers;
        }

        /**
         * @param containersIn to set
         */
        public void setContainers(List<Container> containersIn) {
            this.containers = containersIn;
        }
    }

    /**
     * @param kubeconfig path
     * @return the execution result
     */
    public static RunnerCall<ContainersList> getAllContainers(String kubeconfig, String context) {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("kubeconfig", kubeconfig);
        args.put("context", context);
        RunnerCall<ContainersList> call =
                new RunnerCall<>("mgrk8s.get_all_containers", Optional.of(args),
                        new TypeToken<ContainersList>() { });
        return call;
    }

}
