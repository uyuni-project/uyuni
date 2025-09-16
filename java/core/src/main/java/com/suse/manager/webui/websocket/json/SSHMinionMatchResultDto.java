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
package com.suse.manager.webui.websocket.json;

import java.util.List;

/**
 * salt-ssh minions match result DTO.
 */
public class SSHMinionMatchResultDto extends AbstractSaltEventDto  {

    private List<String> minions;

    /**
     * @param minionsIn minions that matched
     */
    public SSHMinionMatchResultDto(List<String> minionsIn) {
        super("matchSSH");
        this.minions = minionsIn;
    }

    /**
     * @return minions that matched
     */
    public List<String> getMinions() {
        return minions;
    }
}
