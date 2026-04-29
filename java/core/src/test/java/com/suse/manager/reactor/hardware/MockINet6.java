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

/**
 * Mock implementation of Network.INet6 for testing IPv6 address synchronization.
 */
class MockINet6 extends Network.INet6 {
    private final String address;
    private final String prefixlen;
    private final String scope;

    MockINet6(String addressIn, String prefixlenIn, String scopeIn) {
        address = addressIn;
        prefixlen = prefixlenIn;
        scope = scopeIn;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public String getPrefixlen() {
        return prefixlen;
    }

    @Override
    public String getScope() {
        return scope;
    }
}
