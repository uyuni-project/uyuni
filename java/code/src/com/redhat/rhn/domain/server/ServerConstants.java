/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
 * Copyright (c) 2013--2021 SUSE LLC
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
package com.redhat.rhn.domain.server;


/**
 * ServerConstants
 */
public class ServerConstants {
    /**
     * Feature constant for kickstarts
     * This corresponds to osfullname from `salt-call --local grains.items`
     */
    public static final String FEATURE_KICKSTART = "ftr_kickstart";
    public static final String SLES = "SLES";
    public static final String SLEMICRO = "SLE Micro";
    public static final String LEAP = "Leap";
    public static final String LEAPMICRO = "openSUSE Leap Micro";
    public static final String OPENSUSEMICROOS = "openSUSE MicroOS";
    public static final String UBUNTU = "Ubuntu";
    public static final String DEBIAN = "Debian";
    public static final String REDHAT = "RedHat";
    public static final String ALIBABA = "Alibaba Cloud Linux (Aliyun Linux)";
    public static final String ALMA = "AlmaLinux";
    public static final String AMAZON = "Amazon Linux";
    public static final String ROCKY = "Rocky";
    public static final String SLED = "SLED";
    public static final String RHEL = "Red Hat Enterprise Linux";

    private ServerConstants() {

    }

    /**
     * The constant representing the i686 ServerArch
     * @return ServerArch
     */
    public static final ServerArch getArchI686() {
        return ServerFactory.lookupServerArchByLabel("i686-redhat-linux");
    }
    /**
     * The constant representing the athlon ServerArch
     * @return ServerArch
     */
    public static final ServerArch getArchATHLON() {
        return ServerFactory.lookupServerArchByLabel("athlon-redhat-linux");
    }

    /**
     * Static representing the enterprise_entitled ServerGroup
     * @return ServerGroupType
     */
    public static final ServerGroupType getServerGroupTypeEnterpriseEntitled() {
       return  ServerFactory.lookupServerGroupTypeByLabel("enterprise_entitled");
    }

    /**
     * Static representing the provisioning entitled server group type
     * @return ServerGroupType
     */
    public static final ServerGroupType getServerGroupTypeVirtualizationEntitled() {
        return ServerFactory.lookupServerGroupTypeByLabel("virtualization_host");
    }

    /**
     * Static representing the provisioning entitled server group type
     * @return ServerGroupType
     */
    public static final ServerGroupType getServerGroupTypeBootstrapEntitled() {
        return ServerFactory.lookupServerGroupTypeByLabel("bootstrap_entitled");
    }

    /**
     * Static representing the salt entitled server group type
     * @return ServerGroupType
     */
    public static final ServerGroupType getServerGroupTypeSaltEntitled() {
        return ServerFactory.lookupServerGroupTypeByLabel("salt_entitled");
    }

    /**
     * Static representing the foreign entitled server group type
     * @return ServerGroupType
     */
    public static final ServerGroupType getServerGroupTypeForeignEntitled() {
        return ServerFactory.lookupServerGroupTypeByLabel("foreign_entitled");
    }

    /**
     * Static representing the container build host entitled server group type
     * @return ServerGroupType
     */
    public static final ServerGroupType getServerGroupTypeContainerBuildHostEntitled() {
        return ServerFactory.lookupServerGroupTypeByLabel("container_build_host");
    }

    /**
     * Static representing the OS Image build host entitled server group type
     * @return ServerGroupType
     */
    public static final ServerGroupType getServerGroupTypeOSImageBuildHostEntitled() {
        return ServerFactory.lookupServerGroupTypeByLabel("osimage_build_host");
    }
}
