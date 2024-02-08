/*
 * Copyright (c) 2024 SUSE LLC
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
package com.suse.manager.model.attestation;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AttestationFactory extends HibernateFactory {

    private static final Logger LOG = LogManager.getLogger(AttestationFactory.class);

    /**
     * Save a {@link ServerCoCoAttestationConfig} object
     * @param cnf object to save
     */
    public void save(ServerCoCoAttestationConfig cnf) {
        saveObject(cnf);
    }

    /**
     * @param serverId the server id
     * @return returns the optional attestation config for the selected system
     */
    public Optional<ServerCoCoAttestationConfig> lookupConfigByServerId(long serverId) {
        return getSession()
                .createQuery("FROM ServerCoCoAttestationConfig WHERE server_id = :serverId",
                        ServerCoCoAttestationConfig.class)
                .setParameter("serverId", serverId)
                .uniqueResultOptional();
    }
    /**
     * Create a Confidential Compute Attestation Config for a given Server ID
     * @param serverIn the server
     * @param typeIn the environment type
     * @param enabledIn enabled status
     * @return returns the Confidential Compute Attestation Config
     */
    public ServerCoCoAttestationConfig createConfigForServer(Server serverIn, CoCoEnvironmentType typeIn,
                                                             boolean enabledIn) {
        ServerCoCoAttestationConfig cnf = new ServerCoCoAttestationConfig();
        cnf.setServer(serverIn);
        cnf.setEnvironmentType(typeIn);
        cnf.setEnabled(enabledIn);
        save(cnf);
        serverIn.setCocoAttestationConfig(cnf);
        return cnf;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
