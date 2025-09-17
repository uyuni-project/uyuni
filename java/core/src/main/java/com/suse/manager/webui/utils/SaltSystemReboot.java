/*
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

import static java.util.Collections.singletonMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a system reboot state run.
 */
public class SaltSystemReboot extends AbstractSaltRequisites implements IdentifiableSaltState, ActionSaltState {

    private String id;
    private long actionId;
    private Optional<Integer> minutes = Optional.empty();

    /**
     * Standard constructor.
     * @param idIn state ID
     * @param actionIdIn reboot action id
     * @param minutesIn minutes before rebooting
     */
    public SaltSystemReboot(String idIn, long actionIdIn, int minutesIn) {
        this.id = idIn;
        this.minutes = Optional.of(minutesIn);
        this.actionId = actionIdIn;
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
                .ifPresent(args::add);

        addRequisites(args);

        return singletonMap(id,
                singletonMap("mgrcompat.module_run", args)
        );
    }

    /**
     * @return id to get
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * @param idIn to set
     */
    @Override
    public void setId(String idIn) {
        this.id = idIn;
    }

    /**
     * @return actionId to get
     */
    @Override
    public long getActionId() {
        return actionId;
    }
}
