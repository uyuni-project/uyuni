/**
 * Copyright (c) 2020 SUSE LLC
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
package com.suse.manager.webui.services.iface;

import java.util.Optional;

/**
 * Information to determine redhat product.
 */
public class RedhatProductInfo {

    private final Optional<String> centosReleaseContent;
    private final Optional<String> rhelReleaseContent;
    private final Optional<String> whatProvidesRes;

    /**
     * @param centosReleaseContentIn centos release content
     * @param rhelReleaseContentIn rhel release content
     * @param whatProvidesResIn what provides res result
     */
    public RedhatProductInfo(Optional<String> centosReleaseContentIn, Optional<String> rhelReleaseContentIn,
                             Optional<String> whatProvidesResIn) {
        this.centosReleaseContent = centosReleaseContentIn;
        this.rhelReleaseContent = rhelReleaseContentIn;
        this.whatProvidesRes = whatProvidesResIn;
    }


    /**
     * @return centos release content
     */
    public Optional<String> getCentosReleaseContent() {
        return centosReleaseContent;
    }

    /**
     * @return rhel release content
     */
    public Optional<String> getRhelReleaseContent() {
        return rhelReleaseContent;
    }

    /**
     * @return what provides res result
     */
    public Optional<String> getWhatProvidesRes() {
        return whatProvidesRes;
    }
}
