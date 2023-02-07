/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.configuration.files;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.config.ConfigChannelType;
import com.redhat.rhn.domain.config.ConfigFile;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.configuration.ConfigActionHelper;
import com.redhat.rhn.frontend.dto.ConfigChannelDto;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.manager.configuration.ConfigurationManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;

/**
 * CopyFileCentralAction
 */
public class CopyFileCentralAction  extends BaseCopyConfigFileAction {

    @Override
    protected DataResult<ConfigChannelDto> getDataResult(RequestContext rctxIn, PageControl pcIn) {
        User user = rctxIn.getCurrentUser();
        ConfigFile file = ConfigActionHelper.getFile(rctxIn.getRequest());
        ConfigurationManager cm = ConfigurationManager.getInstance();
        String channelTypeLabel = file.getConfigChannel().getConfigChannelType().getLabel();
        return cm.listChannelsForFileCopy(user, file, channelTypeLabel, pcIn);
    }

    @Override
    protected String getLabel() {
        return ConfigChannelType.normal().getLabel();
    }

    @Override
    protected String getType() {
        return BaseCopyConfigFileAction.CENTRAL_TYPE;
    }

    /**
     * Only config-admins get to copy files into central channels
     * {@inheritDoc}
     */
    @Override
    protected String checkPreConditions(RequestContext rctxIn) {
        User user = rctxIn.getCurrentUser();
        if (!user.hasRole(RoleFactory.CONFIG_ADMIN)) {
            throw new PermissionException("Must be a config admin.");
        }
        return null;
    }

    @Override
    protected String getFilterAttr() {
        return BaseCopyConfigFileAction.CHANNEL_FILTER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RhnSetDecl getSetDecl() {
        return RhnSetDecl.CONFIG_CHANNELS;
    }


}
