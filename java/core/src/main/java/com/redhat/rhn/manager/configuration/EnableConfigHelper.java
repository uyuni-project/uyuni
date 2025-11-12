/*
 * Copyright (c) 2009--2015 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.manager.configuration;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ConfigSystemDto;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import java.util.Date;

/**
 * Due to the complicated nature of enabling configuration, this class is
 * used as a way to separate out the logic.
 */
public class EnableConfigHelper {

    private User user;

    protected EnableConfigHelper(User userIn) {
        user = userIn;
    }

    /**
     * Enable the set of systems given for configuration management.
     * @param setLabel The label for the set that contains systems selected for enablement
     * @param earliestIn The earliest time package actions will be scheduled.
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public void enableSystems(String setLabel, Date earliestIn)
        throws TaskomaticApiException {
        ConfigurationManager cm = ConfigurationManager.getInstance();
        //Get the list of systems and what we need to do to them.
        DataResult<ConfigSystemDto> dr = cm.listNonManagedSystemsInSetElaborate(user, setLabel);

        /*
         * The set going to store the system ids and an error code
         * for any problems we run into.  The element_two column will
         * be used for the error code.
         * I realize that this is cheating with the element_two column,
         * but the other option was to have a separate set for every
         * error condition and then union the sets when we wish to display them.
         *
         * The problem we are solving by using RhnSet is remembering what
         * systems ran into what problems across page requests (pagination especially)
         *
         * TODO: currently any single system will only have one error
         *       condition.  We should probably tell the user multiple
         *       errors if we can.
         */
        RhnSet set = RhnSetDecl.CONFIG_ENABLE_SYSTEMS.create(user);

        //iterate through the dataresult and perform actions
        for (ConfigSystemDto dto : dr) {
            Long sid = dto.getId();
            Server current = SystemManager.lookupByIdAndUser(sid, user);
            set.addElement(dto.getId(),
                    (long) enableSystem(dto, current, earliestIn));
        }

        //save the results
        RhnSetManager.store(set);
    }

    private int enableSystem(ConfigSystemDto dto, Server current, Date earliest) {
        //subscribe the system to Client Tools child channel
        if (!dto.isRhnTools() && ChannelManager.subscribeToChildChannelWithPackageName(user, current,
                ChannelManager.TOOLS_CHANNEL_PACKAGE_NAME) == null) {
            return ConfigurationManager.ENABLE_ERROR_RHNTOOLS;
        }

        return ConfigurationManager.ENABLE_SUCCESS;
    }
}
