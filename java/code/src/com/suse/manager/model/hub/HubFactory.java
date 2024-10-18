/*
 * Copyright (c) 2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.model.hub;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

public class HubFactory extends HibernateFactory {

    private static final Logger LOG = LogManager.getLogger(HubFactory.class);

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    /**
     * Save a {@link IssHub} object
     * @param issHubIn object to save
     */
    public void save(IssHub issHubIn) {
        saveObject(issHubIn);
    }

    /**
     * Save a {@link IssPeripheral} object
     * @param issPeripheralIn object to save
     */
    public void save(IssPeripheral issPeripheralIn) {
        saveObject(issPeripheralIn);
    }

    /**
     * Save a {@link IssPeripheralChannels} object
     * @param issPeripheralChannelsIn object to save
     */
    public void save(IssPeripheralChannels issPeripheralChannelsIn) {
        saveObject(issPeripheralChannelsIn);
    }

    /**
     * Lookup {@link IssHub} object by its FQDN
     * @param fqdnIn the fqdn
     * @return return {@link IssHub} with the given FQDN or empty
     */
    public Optional<IssHub> lookupIssHubByFqdn(String fqdnIn) {
        return getSession().createQuery("FROM IssHub WHERE fqdn = :fqdn", IssHub.class)
                .setParameter("fqdn", fqdnIn)
                .uniqueResultOptional();
    }

    /**
     * Lookup {@link IssPeripheral} object by its FQDN
     * @param fqdnIn the fqdn
     * @return return {@link IssPeripheral} with the given FQDN or empty
     */
    public Optional<IssPeripheral> lookupIssPeripheralByFqdn(String fqdnIn) {
        return getSession().createQuery("FROM IssPeripheral WHERE fqdn = :fqdn", IssPeripheral.class)
                .setParameter("fqdn", fqdnIn)
                .uniqueResultOptional();
    }

    /**
     * List {@link IssPeripheralChannels} objects which reference the given {@link Channel}
     * @param channelIn the channel
     * @return return the list of {@link IssPeripheralChannels} objects
     */
    public List<IssPeripheralChannels> listIssPeripheralChannelsByChannels(Channel channelIn) {
        return getSession()
                .createQuery("FROM IssPeripheralChannels WHERE channel = :channel", IssPeripheralChannels.class)
                .setParameter("channel", channelIn)
                .list();
    }
}
