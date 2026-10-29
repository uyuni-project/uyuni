/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.autoinstallation.builder;

import com.suse.manager.autoinstallation.KernelOptionsList;

import org.cobbler.SystemRecord;

/**
 * Shared kernel options helpers for the boot lines Uyuni composes itself
 * and the cobbler service URLs used there.
 */
public final class KernelOptionsHelper {

    private static final String HTTP_PREFIX = "http://";
    private static final String NOPXE_SYSTEM_PATH = "/cblr/svc/op/nopxe/system/";

    private KernelOptionsHelper() {
    }

    /**
     * Adapts resolved kernel options for a SUSE boot line: SUSE installers
     * consume textmode, not the plain text option.
     *
     * @param options the resolved options to adapt in place
     */
    public static void applySuseTextmode(KernelOptionsList options) {
        if (options.hasOption("textmode")) {
            options.removeOption("text");
        }
        else if (options.hasOption("text")) {
            options.removeOption("text");
            options.setOption("textmode", "1", true);
        }
    }

    /**
     * Cobbler nopxe signaling URL of a system record, used as the info=
     * option of a boot line Uyuni composes itself.
     *
     * @param serverFqdn the kickstart host
     * @param system the system record to signal
     * @return http://&lt;fqdn&gt;/cblr/svc/op/nopxe/system/&lt;systemName&gt;
     */
    public static String cobblerSystemNopxeUrl(String serverFqdn, SystemRecord system) {
        if (system == null) {
            throw new KernelOptionsBuilderException("system must not be null");
        }
        return HTTP_PREFIX + serverFqdn + NOPXE_SYSTEM_PATH + system.getName();
    }
}
