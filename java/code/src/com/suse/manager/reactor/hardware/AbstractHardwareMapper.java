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

import com.redhat.rhn.domain.server.MinionServer;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.services.SaltService;
import org.apache.log4j.Logger;

/**
 * Base mapper that processes hardware info from a minion and stores it in the SUMA db.
 * @param <T> the return type of the map method.
 */
public abstract class AbstractHardwareMapper<T> {

    // Logger for this class
    private static final Logger LOG = Logger
            .getLogger(AbstractHardwareMapper.class);

    protected final SaltService SALT_SERVICE;

    /**
     * The constructor.
     * @param saltService a {@link SaltService} instance
     */
    public AbstractHardwareMapper(SaltService saltService) {
        this.SALT_SERVICE = saltService;
    }

    /**
     * Get the hardware information from the minion and store it in our db.
     * @param server the {@link MinionServer} bean
     * @param grains the Salt grains
     * @return the persisted bean(s)
     */
    public T map(MinionServer server, ValueMap grains) {
        try {
            return doMap(server, grains);
        } catch (Exception e) {
            LOG.error("Error executing mapper " + getClass().getName(), e);
        }
        return null;
    }

    protected abstract T doMap(MinionServer server, ValueMap grains);

}
