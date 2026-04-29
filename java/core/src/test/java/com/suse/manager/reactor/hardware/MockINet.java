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
package com.suse.manager.reactor.hardware;

import com.suse.salt.netapi.calls.modules.Network;

import java.util.Optional;

/**
 * Mock implementation of Network.INet for testing IPv4 address synchronization.
 */
class MockINet extends Network.INet {
    private final String address;
    private final String netmask;
    private final String broadcast;

    MockINet(String addressIn, String netmaskIn, String broadcastIn) {
        address = addressIn;
        netmask = netmaskIn;
        broadcast = broadcastIn;
    }

    @Override
    public Optional<String> getAddress() {
        return Optional.ofNullable(address);
    }

    @Override
    public Optional<String> getNetmask() {
        return Optional.ofNullable(netmask);
    }

    @Override
    public Optional<String> getBroadcast() {
        return Optional.ofNullable(broadcast);
    }
}
