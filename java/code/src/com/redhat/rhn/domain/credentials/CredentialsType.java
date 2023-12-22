/*
 * Copyright (c) 2012 SUSE LLC
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
package com.redhat.rhn.domain.credentials;

import java.util.Arrays;

/**
 * The type of credentials
 */
public enum CredentialsType {
    SCC(Label.SCC),
    VIRT_HOST_MANAGER(Label.VIRT_HOST_MANAGER),
    REGISTRY(Label.REGISTRY),
    CLOUD_RMT(Label.CLOUD_RMT),
    REPORT_DATABASE(Label.REPORT_DATABASE),
    RHUI(Label.RHUI);

    private final String label;

    CredentialsType(String labelIn) {
        this.label = labelIn;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Get a credential type instance by label
     * @param label the label
     * @return the {@link CredentialsType} instance
     * @throws IllegalArgumentException if label is not found
     */
    public static CredentialsType byLabel(String label) {
        return Arrays.stream(CredentialsType.values())
            .filter(type -> type.getLabel().equals(label))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Invalid label " + label));
    }

    // This class specifies the labels used by the database and by the mapping annotations
    public static class Label {
        public static final String SCC = "scc";
        public static final String VIRT_HOST_MANAGER = "vhm";
        public static final String REGISTRY = "registrycreds";
        public static final String CLOUD_RMT = "cloudrmt";
        public static final String REPORT_DATABASE = "reportcreds";
        public static final String RHUI = "rhui";

        private Label() {
        }
    }
}
