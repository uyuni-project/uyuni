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
package com.redhat.rhn.manager.kickstart.tree;

import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerDistroCreateCommand;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * TreeCreateCommand
 */
public class TreeCreateOperation extends BaseTreeEditOperation {

    /**
     * Constructor
     * @param userIn to associate
     */
    public TreeCreateOperation(User userIn) {
        super(userIn);
        this.tree = new KickstartableTree();
        this.tree.setCreated(new Date());
        this.tree.setTreeType(KickstartFactory.TREE_TYPE_EXTERNAL);
        this.tree.setOrg(this.user.getOrg());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CobblerCommand getCobblerCommand() {
        return new CobblerDistroCreateCommand(this.tree, this.user);
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public ValidatorError store() {
        KickstartableTree existingTree = KickstartFactory.lookupKickstartTreeByLabel(
                        this.getTree().getLabel(), this.getUser().getOrg());
        if (existingTree != null) {
            return new ValidatorError("distribution.tree.exists", existingTree.getLabel());
        }

        String kopts = this.tree.getKernelOptions();
        StringJoiner kOptsJoiner = new StringJoiner(" ");
        kOptsJoiner.add(kopts);

        if (this.tree.getInstallType().isSUSE()) {
            if (!kopts.contains("install=")) {
                kOptsJoiner.add("install=http://" + getServerFqdn() + "/ks/dist/" + this.tree.getLabel());
            }
            // Configure URL for YaST self update or disable it
            if (!kopts.contains("self_update=")) {
                Optional<Channel> installerUpdates = tree.getChannel()
                        .getAccessibleChildrenFor(getUser())
                        .stream()
                        .filter(c -> c.isInstallerUpdates())
                        .findFirst();
                if (installerUpdates.isPresent()) {
                    kOptsJoiner.add("self_update=http://" + getServerFqdn() + "/ks/dist/child/" +
                            installerUpdates.get().getLabel() + "/" + this.tree.getLabel());
                }
                else {
                    kOptsJoiner.add("self_update=0");
                }
            }
        }
        else if (this.tree.getInstallType().isRhel8OrGreater()) {
            if (!kopts.contains("inst.repo=")) {
                kOptsJoiner.add("inst.repo=http://" + getServerFqdn() + "/ks/dist/" + this.tree.getLabel());
            }
        }

        this.tree.setKernelOptions(kOptsJoiner.toString());
        return super.store();

    }

    private String getServerFqdn() {
        String localhost = this.getServerName();
        try {
            // Find the FQDN of localhost
            return InetAddress.getByName(localhost).getCanonicalHostName();
        }
        catch (UnknownHostException e) {
            // Fall back to the local name in this case
            return localhost;
        }
    }
}
