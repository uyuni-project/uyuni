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

package com.suse.manager.webui.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonMap;

/**
 * Represents a system reboot state run.
 */
public class SaltSystemReboot extends AbstractSaltRequisites implements IdentifiableSaltState {

    private String id;
    private Optional<Integer> minutes = Optional.empty();

    /**
     * Standard constructor.
     * @param idIn state ID
     * @param minutesIn minutes before rebooting
     */
    public SaltSystemReboot(String idIn, int minutesIn) {
        this.id = idIn;
        this.minutes = Optional.of(minutesIn);
    }

    /**
     * Minimal constructor.
     * @param idIn state ID
     */
    public SaltSystemReboot(String idIn) {
        this.id = idIn;
    }

    @Override
    public Map<String, Object> getData() {
        List<Map<String, ?>> args = new ArrayList<>();

        args.add(singletonMap("name", "system.reboot"));
        minutes.map(min -> singletonMap("at_time", min))
                .ifPresent(time -> args.add(time));

        addRequisites(args);

        return singletonMap(id,
                singletonMap("module.run", args)
        );
    }

    /**
     * @return id to get
     */
    public String getId() {
        return id;
    }

    /**
     * @param idIn to set
     */
    public void setId(String idIn) {
        this.id = idIn;
    }
}
