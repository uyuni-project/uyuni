/**
 * Copyright (c) 2020 SUSE LLC
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
package com.suse.manager.webui.services.iface;

import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.event.EventStream;

import java.util.Optional;

/**
 * Interface for interacting with salt.
 */
public interface SaltApi {

    /**
     * Return the stream of events happening in salt.
     *
     * @return the event stream
     */
    EventStream getEventStream();

    /**
     * Synchronously executes a salt function on a single minion.
     * If a SaltException is thrown, re-throw a RuntimeException.
     *
     * @param call salt function to call
     * @param minionId minion id to target
     * @param <R> result type of the salt function
     * @return Optional holding the result of the function
     * or empty if the minion did not respond.
     */
    <R> Optional<R> callSync(LocalCall<R> call, String minionId);

}
