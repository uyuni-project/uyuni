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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.action.config;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFormatter;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.config.ConfigFileName;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.manager.action.ActionManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

/**
 * ConfigUpload - Class representing ActionType.TYPE_CONFIGFILES_MTIME_UPLOAD: 15
 */
@Entity
@DiscriminatorValue("15")
public class ConfigUploadAction extends Action {

    @OneToMany(mappedBy = "parentAction", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<ConfigChannelAssociation> configChannelAssociations;

    @OneToMany(mappedBy = "parentAction", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<ConfigFileNameAssociation> configFileNameAssociations;

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
        configChannelAssociations = configChannelAssociationsIn;
    }

    /**
     * @return Returns the config file name associations.
     */
    public Set<ConfigFileNameAssociation> getConfigFileNameAssociations() {
        return configFileNameAssociations;
    }

    /**
     * @param configFileNameAssociationsIn The config file name associations to set.
     */
    public void setConfigFileNameAssociations(
            Set<ConfigFileNameAssociation> configFileNameAssociationsIn) {
        configFileNameAssociations = configFileNameAssociationsIn;
    }

    /**
     * Adds a config channel associated with the given server to the upload action.
     * For config upload actions, for every server action there must exist one and
     * only one config channel.
     * @param channel The config channel to which to upload files.
     * @param server The server from which to upload files.
     */
    public void addConfigChannelAndServer(ConfigChannel channel, Server server) {
        ConfigChannelAssociation newCA = new ConfigChannelAssociation();
        newCA.setConfigChannel(channel);
        newCA.setServer(server);
        newCA.setModified(new Date());
        newCA.setCreated(new Date());
        if (configChannelAssociations == null) {
            configChannelAssociations = new HashSet<>();
        }
        newCA.setParentAction(this);
        configChannelAssociations.add(newCA);
    }

    /**
     * Adds a config file name to the upload action for the given server.
     * @param fileName The config file name to upload.
     * @param server The server to upload the config file from.
     */
    public void addConfigFileName(ConfigFileName fileName, Server server) {
        ConfigFileNameAssociation newFNA = new ConfigFileNameAssociation();
        newFNA.setConfigFileName(fileName);
        newFNA.setServer(server);
        newFNA.setModified(new Date());
        newFNA.setCreated(new Date());
        if (configFileNameAssociations == null) {
            configFileNameAssociations = new HashSet<>();
        }
        newFNA.setParentAction(this);
        configFileNameAssociations.add(newFNA);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionFormatter getFormatter() {
        if (formatter == null) {
            formatter = new ConfigUploadActionFormatter(this);
        }
        return formatter;
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
