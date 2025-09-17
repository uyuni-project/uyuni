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

/**
 * Run command result DTO.
 */
public class MinionCommandResultEventDto extends AbstractSaltEventDto {

    private String out;

    /**
     * @param minionIdIn monion id
     * @param outIn command stdout
     */
    public MinionCommandResultEventDto(String minionIdIn, String outIn) {
        super("runResult", minionIdIn);
        this.out = outIn;
    }

    /**
     * @return command stdout
     */
    public String getOut() {
        return out;
    }

    /**
     * @param outIn command stdout
     */
    public void setOut(String outIn) {
        this.out = outIn;
    }
}
