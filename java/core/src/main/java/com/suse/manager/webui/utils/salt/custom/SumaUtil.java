/*
 * Copyright (c) 2015 SUSE LLC
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
package com.suse.manager.webui.utils.salt.custom;

import com.google.gson.annotations.SerializedName;

import java.util.Optional;

/**
 * Custom Salt module sumautil.
 */
public class SumaUtil {

    /**
     * IP protocol version
     */
    public enum IPVersion {
        @SerializedName("IPv4")
        IPV4,
        @SerializedName("IPv6")
        IPV6
    }

    /**
     * IP route information
     */
    public static class IPRoute {

        private String destination;
        private Optional<String> gateway;
        @SerializedName("interface")
        private String netInterface;
        private String source;

        /**
         * The destination IP.
         * @return an IPv4 or IPv6 address
         */
        public String getDestination() {
            return destination;
        }

        /**
         * Gateway, if any.
         * @return an IPv4 or IPv6 address
         */
        public Optional<String> getGateway() {
            return gateway;
        }

        /**
         * The source network interface
         * @return the name of the network interface
         */
        public String getInterface() {
            return netInterface;
        }

        /**
         * The source IP.
         * @return an IPv4 or IPv6 address
         */
        public String getSource() {
            return source;
        }

    }

    /**
     * The result of 'sumautil.cat' method.
     */
    public static class CatResult {

        private long retcode;

        private String stderr;

        private String stdout;

        /**
         * @return the cat command ret code
         */
        public long getRetcode() {
            return retcode;
        }

        /**
         * @return the stderr
         */
        public String getStderr() {
            return stderr;
        }

        /**RegisterMinionEventMessageAction
         * @return the stdout
         */
        public String getStdout() {
            return stdout;
        }
    }

    /**
     * Result of sumautil.instance_flavor
     */
    public enum PublicCloudInstanceFlavor {
        PAYG,
        BYOS,
        UNKNOWN
    }

    private SumaUtil() { }

}
