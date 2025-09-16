/*
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
package com.redhat.rhn.frontend.action.configuration.channel;

import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.frontend.action.configuration.BaseAddFilesAction;
import com.redhat.rhn.frontend.action.configuration.ConfigActionHelper;
import com.redhat.rhn.frontend.struts.RequestContext;

import javax.servlet.http.HttpServletRequest;

/**
 * ChannelAddFilesAction extends RhnAction
 */
public class ChannelAddFilesAction extends BaseAddFilesAction {

    /**
     * {@inheritDoc}
     */
    @Override
    protected ConfigChannel getConfigChannel(HttpServletRequest requestIn) {
        return ConfigActionHelper.getChannel(requestIn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void processRequest(HttpServletRequest requestIn) {
        ConfigActionHelper.setupRequestAttributes(new RequestContext(requestIn),
                getConfigChannel(requestIn));
    }

}
