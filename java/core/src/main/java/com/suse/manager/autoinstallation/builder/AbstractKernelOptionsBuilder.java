/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.autoinstallation.builder;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.autoinstallation.KernelOptionsList;

import org.cobbler.Profile;
import org.cobbler.SystemRecord;

import java.util.stream.Stream;

/**
 * Shared logic for the per-OS-class kernel options builders: URL helpers and
 * the installer-updates child channel resolution.
 */
public abstract class AbstractKernelOptionsBuilder implements KernelOptionsBuilder {

    private static final String HTTP_PREFIX = "http://";
    private static final String DIST_PATH = "/ks/dist/";
    private static final String DIST_CHILD_PATH = "/ks/dist/child/";
    private static final String AUTOINSTALL_SYSTEM_PATH = "/cblr/svc/op/autoinstall/system/";
    private static final String SELF_UPDATE_DISABLED = "0";

    private final String serverFqdn;
    private final User user;

    /**
     * Constructor
     * @param serverFqdnIn the kickstart host, not always the local server
     * @param userIn optional user context scoping the installer-updates
     *               child channel lookup, null for org-global lookup
     */
    protected AbstractKernelOptionsBuilder(String serverFqdnIn, User userIn) {
        this.serverFqdn = serverFqdnIn;
        this.user = userIn;
    }

    /**
     * {@inheritDoc}
     *
     * Empty until the Cobbler exit: the installer-selection option on PXE
     * entries is owned by Cobbler's tftpgen, so it must not be duplicated
     * on profile records.
     */
    @Override
    public KernelOptionsList profileOptions(Profile profile) {
        return new KernelOptionsList();
    }

    /**
     * Resolves the installer-updates option value for a tree: the URL of the
     * tree's installer-updates child channel if one is accessible, else the
     * literal "0" disabling the installer self update. With a user context
     * the lookup is user-scoped (legacy distro/system path behaviour),
     * otherwise global (vendor channels).
     *
     * @param tree the distribution tree
     * @return the installer-updates URL or "0"
     */
    protected String installerUpdatesValue(KickstartableTree tree) {
        Channel parent = require(tree, "tree").getChannel();
        Stream<Channel> children;
        if (this.user != null) {
            children = parent.getAccessibleChildrenFor(this.user).stream();
        }
        else {
            children = listAllChildren(parent);
        }
        return children.filter(Channel::isInstallerUpdates)
                .findFirst()
                .map(child -> HTTP_PREFIX + this.serverFqdn + DIST_CHILD_PATH +
                        child.getLabel() + "/" + tree.getLabel())
                .orElse(SELF_UPDATE_DISABLED);
    }

    /**
     * All children of a channel, regardless of the user (org-global lookup,
     * used when no user context is available). Isolated into a hook so unit
     * tests can stub the static factory call.
     *
     * @param parent the parent channel
     * @return the child channels
     */
    protected Stream<Channel> listAllChildren(Channel parent) {
        return ChannelFactory.listAllChildrenForChannel(parent).stream();
    }

    /**
     * URL of the distribution tree.
     * @param tree the distribution tree
     * @return http://&lt;fqdn&gt;/ks/dist/&lt;treeLabel&gt;
     */
    protected String distTreeUrl(KickstartableTree tree) {
        return HTTP_PREFIX + this.serverFqdn + DIST_PATH + require(tree, "tree").getLabel();
    }

    /**
     * Cobbler autoinstall service URL of a system record.
     * @param system the system record
     * @return http://&lt;fqdn&gt;/cblr/svc/op/autoinstall/system/&lt;systemName&gt;
     */
    protected String cobblerSystemAutoinstallUrl(SystemRecord system) {
        return HTTP_PREFIX + this.serverFqdn + AUTOINSTALL_SYSTEM_PATH + require(system, "system").getName();
    }

    /**
     * Validates an input, throwing a KernelOptionsBuilderException for null.
     * @param <T> the input type
     * @param value the value to check
     * @param name the parameter name for the error message
     * @return the checked value
     */
    protected static <T> T require(T value, String name) {
        if (value == null) {
            throw new KernelOptionsBuilderException(name + " must not be null");
        }
        return value;
    }
}
