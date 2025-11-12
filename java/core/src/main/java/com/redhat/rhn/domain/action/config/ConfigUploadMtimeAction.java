/*
 * Copyright (c) 2025 SUSE LLC
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.domain.action.config;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.manager.action.ActionManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

/**
 * ConfigUploadMtimeAction - Class representing ActionType.TYPE_CONFIGFILES_MTIME_UPLOAD: 23
 */
@Entity
@DiscriminatorValue("23")
public class ConfigUploadMtimeAction extends Action {

    @OneToMany(mappedBy = "parentAction", cascade = CascadeType.ALL)
    private Set<ConfigDateFileAction> configDateFileActions;

    @OneToMany(mappedBy = "parentAction", cascade = CascadeType.ALL)
    private Set<ConfigChannelAssociation> configChannelAssociations;

    @OneToOne(mappedBy = "parentAction", cascade = CascadeType.ALL)
    private ConfigDateDetails configDateDetails;

    /**
     * @return Returns the configDateFileActions.
     */
    public Set<ConfigDateFileAction> getConfigDateFileActions() {
        return configDateFileActions;
    }

    /**
     * @param configDateFileActionsIn The configDateFileActions to set.
     */
    public void setConfigDateFileActions(Set<ConfigDateFileAction> configDateFileActionsIn) {
        this.configDateFileActions = configDateFileActionsIn;
    }

    /**
     * Add a ConfigDateFileAction to the collection.
     * @param cdIn the ConfigDateFileAction to add
     */
    public void addConfigDateFileAction(ConfigDateFileAction cdIn) {
        if (configDateFileActions == null) {
            configDateFileActions = new HashSet<>();
        }
        cdIn.setParentAction(this);
        configDateFileActions.add(cdIn);
    }


    /**
     * @return Returns the configChannels associated with this Action
     */
    public ConfigChannel[] getConfigChannels() {
        Iterator<ConfigChannelAssociation> i = configChannelAssociations.iterator();
        Set<ConfigChannel> retval = new HashSet<>();
        while (i.hasNext()) {
            ConfigChannelAssociation ca = i.next();
            retval.add(ca.getConfigChannel());
        }
        return retval.toArray(new ConfigChannel[0]);
    }

    /**
     * @return Returns the servers associated with this Action
     */
    public Server[] getServers() {
        Iterator<ConfigChannelAssociation> i = configChannelAssociations.iterator();
        Set<Server> retval = new HashSet<>();
        while (i.hasNext()) {
            ConfigChannelAssociation ca = i.next();
            retval.add(ca.getServer());
        }
        return retval.toArray(new Server[0]);
    }

    /**
     * Add a ConfigChannel and a Server to this action.  They must be added in pairs.
     *
     * @param ccIn the ConfigChannel we want to asssociate with this Action
     * @param serverIn the Server we want to associate with this Action
     */
    public void addConfigChannelAndServer(ConfigChannel ccIn, Server serverIn) {
        ConfigChannelAssociation newCA = new ConfigChannelAssociation();
        newCA.setConfigChannel(ccIn);
        newCA.setServer(serverIn);
        newCA.setModified(new Date());
        newCA.setCreated(new Date());
        if (configChannelAssociations == null) {
            configChannelAssociations = new HashSet<>();
        }
        newCA.setParentAction(this);
        configChannelAssociations.add(newCA);
    }

    /**
     * @return Returns the config channel associations.
     */
    public Set<ConfigChannelAssociation> getConfigChannelAssociations() {
        return configChannelAssociations;
    }

    /**
     * @param configChannelAssociationsIn The config channel associations to set.
     */
    public void setConfigChannelAssociations(
            Set<ConfigChannelAssociation> configChannelAssociationsIn) {
        this.configChannelAssociations = configChannelAssociationsIn;
    }

    /**
     * @return Returns the configDateDetails.
     */
    public ConfigDateDetails getConfigDateDetails() {
        return configDateDetails;
    }
    /**
     * @param configDateDetailsIn The configDateDetails to set.
     */
    public void setConfigDateDetails(ConfigDateDetails configDateDetailsIn) {
        this.configDateDetails = configDateDetailsIn;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, String>> createActionSpecificDetails(ServerAction serverAction) {
        final List<Map<String, String>> additionalInfo = new ArrayList<>();
        // retrieve the details associated with the action...
        DataResult<Row> files = ActionManager.getConfigFileUploadList(getId());
        for (Row file : files) {
            Map<String, String> info = new HashMap<>();
            info.put("detail", (String) file.get("path"));
            String error = (String) file.get("failure_reason");
            if (error != null) {
                info.put("result", error);
            }
            additionalInfo.add(info);
        }
        return additionalInfo;
    }
}
