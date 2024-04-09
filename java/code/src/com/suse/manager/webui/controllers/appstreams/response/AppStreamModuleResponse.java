/*
 * Copyright (c) 2024 SUSE LLC
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
package com.suse.manager.webui.controllers.appstreams.response;

import com.redhat.rhn.domain.channel.AppStream;
import com.redhat.rhn.domain.server.Server;

public class AppStreamModuleResponse {

    /**
     * Constructs an AppStreamModuleResponse object based on the provided AppStream and Server objects.
     *
     * @param appStreamIn The AppStream object to extract information from.
     * @param serverIn The Server object to check for module enablement.
     */
    public AppStreamModuleResponse(AppStream appStreamIn, Server serverIn) {
        this.name = appStreamIn.getName();
        this.stream = appStreamIn.getStream();
        this.version = appStreamIn.getVersion();
        this.context = appStreamIn.getContext();
        this.arch = appStreamIn.getArch();
        this.enabled = serverIn.hasAppStreamModuleEnabled(this.name, this.stream);
    }

    private String name;
    private String stream;
    private String version;
    private String context;
    private String arch;
    private boolean enabled;
}
