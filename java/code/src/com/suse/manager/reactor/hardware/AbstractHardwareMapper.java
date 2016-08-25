/**
 * Copyright (c) 2015 SUSE LLC
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
package com.suse.manager.reactor.hardware;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.suse.manager.reactor.utils.ValueMap;
import org.apache.log4j.Logger;

import java.util.Optional;

/**
 * Base mapper that processes hardware info from a minion and stores it in the SUMA db.
 * @param <T> the return type of the map method.
 */
public abstract class AbstractHardwareMapper<T> {

    // Logger for this class
    private static final Logger LOG = Logger
            .getLogger(AbstractHardwareMapper.class);

    protected SaltServiceInvoker saltInvoker;

    private Optional<String> error = Optional.empty();

    /**
     * The constructor.
     * @param saltInvokerIn a {@link SaltServiceInvoker} instance
     */
    public AbstractHardwareMapper(SaltServiceInvoker saltInvokerIn) {
        this.saltInvoker = saltInvokerIn;
    }

    /**
     * Get the hardware information from the minion and store it in our db.
     * @param serverId the id of the {@link MinionServer}
     * @param grains the Salt grains
     */
    public void map(Long serverId, ValueMap grains) {
        String minionId = null;
        try {
            HibernateFactory.getSession().beginTransaction();
            Optional<MinionServer> optionalServer = MinionServerFactory
                    .lookupById(serverId);
            if (!optionalServer.isPresent()) {
                LOG.warn("Minion server not found: " + serverId);
            }
            else {
                doMap(optionalServer.get(), grains);
                minionId = optionalServer.get().getMinionId();
            }

            HibernateFactory.commitTransaction();
        }
        catch (Exception e) {
            LOG.error(String.format("Rolling back transaction. " +
                    "Error executing mapper %s for minionId=%s, serverId=%d",
                    getClass().getName(), minionId, serverId), e);
            HibernateFactory.rollbackTransaction();
            setError("An error occurred: " + e.getMessage());
        }
        finally {
            HibernateFactory.closeSession();
        }
    }

    protected abstract T doMap(MinionServer server, ValueMap grains);

    /**
     * @return error messages
     */
    public Optional<String> getError() {
        return error;
    }

    /**
     * @param errorIn error messages
     */
    protected void setError(String errorIn) {
        this.error = Optional.ofNullable(errorIn);
    }
}
