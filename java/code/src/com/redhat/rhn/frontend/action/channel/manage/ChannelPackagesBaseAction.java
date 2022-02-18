/*
 * Copyright (c) 2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.channel.manage;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.SelectableChannel;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.manager.user.UserManager;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ChannelPackagesBaseAction
 */
public class ChannelPackagesBaseAction extends RhnAction {

    protected final String listName = "packageList";

    protected List<SelectableChannel> findChannels(User user, Long selectedChan) {
        return ChannelFactory.findAllByUserOrderByChild(user).stream()
                .map(c -> setSelected(c, selectedChan))
                .collect(Collectors.toList());
    }

    protected boolean canAccessChannel(User user, Channel channel) {
        return UserManager.verifyChannelSubscribable(user, channel) ||
            UserManager.verifyChannelAdmin(user, channel);
    }

    private SelectableChannel setSelected(Channel chan, Long selectedChan) {
        SelectableChannel selChan = new SelectableChannel(chan);
        if (selChan.getChannel().getId().equals(selectedChan)) {
            selChan.setSelected(true);
        }
        return selChan;
    }

}
