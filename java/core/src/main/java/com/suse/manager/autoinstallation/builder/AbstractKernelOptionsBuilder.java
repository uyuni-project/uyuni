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

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.autoinstallation.KernelOptionsList;

import org.cobbler.SystemRecord;

import java.util.Optional;

/**
 * Shared abstract base builder holding common logic and configuration.
 */
public abstract class AbstractKernelOptionsBuilder implements KernelOptionsBuilder {

    private String serverFqdn = ConfigDefaults.get().getJavaHostname();
    private Optional<User> user = Optional.empty();

    /**
     * Override per OS class: "http" or "https".
     * @return the url scheme
     */
    protected abstract String urlScheme();

    /**
     * Optional query suffix, e.g. "?ssl_verify=no" for Agama, "" otherwise.
     * @return the ssl verify suffix
     */
    protected String sslVerifySuffix() {
        return "";
    }

    /**
     * Sets the public facing server FQDN.
     * @param serverFqdnIn the server FQDN
     */
    public void setServerFqdn(String serverFqdnIn) {
        this.serverFqdn = serverFqdnIn;
    }

    /**
     * Gets the public facing server FQDN.
     * @return the server FQDN
     */
    protected String getServerFqdn() {
        return serverFqdn;
    }

    /**
     * Sets the user context.
     * @param userIn the user
     */
    public void setUser(User userIn) {
        this.user = Optional.ofNullable(userIn);
    }

    /**
     * Gets the user context.
     * @return the user context
     */
    protected Optional<User> getUser() {
        return user;
    }

    /**
     * Builds the base URL using the scheme and FQDN.
     * @return the base URL
     */
    protected String baseUrl() {
        return urlScheme() + "://" + serverFqdn;
    }

    /**
     * URL to the distro tree on this server.
     * @param tree the kickstart tree
     * @return the URL
     */
    protected String distTreeUrl(KickstartableTree tree) {
        return baseUrl() + "/ks/dist/" + tree.getLabel() + sslVerifySuffix();
    }

    /**
     * URL to an installer-updates child channel for self_update.
     * @param tree the kickstart tree
     * @return the URL, or empty if no child channel is an installer-update
     */
    protected Optional<String> installerUpdatesUrl(KickstartableTree tree) {
        if (user.isPresent()) {
            return tree.getChannel().getAccessibleChildrenFor(user.get()).stream()
                    .filter(Channel::isInstallerUpdates)
                    .findFirst()
                    .map(ch -> baseUrl() + "/ks/dist/child/" + ch.getLabel() +
                            "/" + tree.getLabel() + sslVerifySuffix());
        }
        else {
            return ChannelFactory.listAllChildrenForChannel(tree.getChannel()).stream()
                    .filter(Channel::isInstallerUpdates)
                    .findFirst()
                    .map(ch -> baseUrl() + "/ks/dist/child/" + ch.getLabel() +
                            "/" + tree.getLabel() + sslVerifySuffix());
        }
    }

    /**
     * URL for system info (nopxe).
     * @param system the system record
     * @return the URL
     */
    protected String infoUrl(SystemRecord system) {
        return baseUrl() + "/cblr/svc/op/nopxe/system/" + system.getName();
    }

    /**
     * Cobbler autoinstall URL for a system.
     * @param system the system record
     * @return the URL
     */
    protected String autoinstallSystemUrl(SystemRecord system) {
        return baseUrl() + "/cblr/svc/op/autoinstall/system/" + system.getName();
    }

    @Override
    public KernelOptionsList networkBoot(KickstartableTree ksTree, SystemRecord system) {
        requireNonNull(ksTree, system);
        return distroOptions(ksTree)
                .addOptions(profileOptions(system.getProfile()))
                .addOptions(systemOptions(system))
                .setOptionIfNotPresent("info", infoUrl(system));
    }

    @Override
    public KernelOptionsList localBoot(KickstartableTree ksTree, SystemRecord system) {
        requireNonNull(ksTree, system);
        return distroOptions(ksTree)
                .addOptions(profileOptions(system.getProfile()))
                .addOptions(systemOptions(system));
    }

    /**
     * Validates that the tree and system are not null.
     * @param tree the kickstart tree
     * @param system the system record
     */
    protected static void requireNonNull(KickstartableTree tree, SystemRecord system) {
        if (system == null) {
            throw new KernelOptionsBuilderException("System record cannot be null");
        }
        if (tree == null) {
            throw new KernelOptionsBuilderException("Kickstartable tree cannot be null");
        }
    }
}
