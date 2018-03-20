/**
 * Copyright (c) 2018 SUSE LLC
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
package com.suse.manager.webui.utils.gson;

/**
 * VirtualGuestsBaseAction represents most of the virtual guests actions
 * requests Json data.
 */
public class VirtualGuestSetterActionJson extends VirtualGuestsBaseActionJson {

    private Long value;

    /**
     * @return the value to set.
     */
    public Long getValue() {
        return value;
    }

    /**
     * @param settingValue the value to set. Only needed for setMemory and setVcpu actions.
     */
    public void setValue(Long settingValue) {
        this.value = settingValue;
    }
}
