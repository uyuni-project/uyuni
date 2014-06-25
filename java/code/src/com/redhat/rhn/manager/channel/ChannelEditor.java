/**
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
package com.redhat.rhn.manager.channel;

import com.redhat.rhn.common.db.datasource.CallableMode;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.rhnpackage.PackageManager;
import com.redhat.rhn.manager.user.UserManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * ChannelEditor
 * @version $Rev$
 */
public class ChannelEditor {

    // private instance
    private static ChannelEditor editor = new ChannelEditor();

    // private constructor
    private ChannelEditor() {
    }

    /**
     * @return Returns the running instance of ChannelEditor
     */
    public static ChannelEditor getInstance() {
        return editor;
    }

    /**
     * Adds a list of packages to a channel.
     * @param user The user requesting the package additions
     * @param channel The channel to add the packages to
     * @param packageIds A list containing the ids of packages to add.
     */
    public void addPackages(User user, Channel channel, Collection packageIds) {
        ChannelFactory.lock(channel);
        changePackages(user, channel, packageIds, true);
    }

    /**
     * Removes a list of packages from a channel
     * @param user The user requesting the package removals
     * @param channel The channel to remove the packages from
     * @param packageIds A list containing the ids of packages to remove.
     */
    public void removePackages(User user, Channel channel, Collection packageIds) {
        changePackages(user, channel, packageIds, false);
    }

    /*
     * This is kind of hokey, but I didn't want to replicate all of this code twice.
     * @param add If true, we are adding the list of packages to the channel. Otherwise,
     * remove the list of packages from the channel.
     */
    private void changePackages(User user, Channel channel,
                                Collection packageIds, boolean add) {
        //Make sure the person adding packages is a channel admin
        if (!UserManager.verifyChannelAdmin(user, channel)) {
            StringBuilder msg = new StringBuilder("User: ");
            msg.append(user.getLogin());
            msg.append(" does not have channel admin access to channel: ");
            msg.append(channel.getLabel());

            //Throw an exception with a nice error message so the user
            //knows what went wrong.
            LocalizationService ls = LocalizationService.getInstance();
            PermissionException pex = new PermissionException(msg.toString());
            pex.setLocalizedTitle(ls.getMessage("permission.jsp.title.channel"));
            pex.setLocalizedSummary(ls.getMessage("permission.jsp.summary.channel"));
            throw pex;
        }

        // make sure we work with long ids
        List<Long> longPackageIds = new ArrayList();
        for (Iterator it = packageIds.iterator(); it.hasNext();) {
            longPackageIds.add(new Long(((Number)it.next()).longValue()));
        }


        PackageManager.verifyPackagesChannelArchCompatAndOrgAccess(user,
                channel, longPackageIds, add);

        if (add) {
            ChannelManager.addPackages(channel, longPackageIds, user);
        }
        else {
            ChannelManager.removePackages(channel, longPackageIds, user);
        }

        // Mark the affected channel to have it smetadata evaluated, where necessary
        // (RHEL5+, mostly)
        ChannelManager.queueChannelChange(channel.getLabel(), "java::changePackages", null);

        ChannelFactory.save(channel);
        //call update_channel stored proc
        updateChannel(channel);
    }

    /**
     * Calls the rhn_channel.update_channel stored proc
     * @param channel The channel to update
     */
    private void updateChannel(Channel channel) {
        CallableMode m = ModeFactory.getCallableMode("Package_queries", "update_channel");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("cid", channel.getId());
        m.execute(params, new HashMap());
    }
 }
