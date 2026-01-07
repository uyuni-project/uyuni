/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.model.hub;

import java.util.Objects;

public class ServerInfoJson {

    private final boolean isHub;

    private final boolean isPeripheral;

    /**
     * Default constructor
     */
    public ServerInfoJson() {
        isHub = false;
        isPeripheral = false;
    }

    /**
     * Constructor
     *
     * @param isHubIn true if server is registered to a hub
     * @param isPeripheralIn true if server has registered peripherals
     */
    public ServerInfoJson(boolean isHubIn, boolean isPeripheralIn) {
        isHub = isHubIn;
        isPeripheral = isPeripheralIn;
    }

    public boolean isHub() {
        return isHub;
    }

    public boolean isPeripheral() {
        return isPeripheral;
    }

    @Override
    public boolean equals(Object oIn) {
        if (!(oIn instanceof ServerInfoJson that)) {
            return false;
        }
        return isHub() == that.isHub() && isPeripheral() == that.isPeripheral();
    }

    @Override
    public int hashCode() {
        return Objects.hash(isHub(), isPeripheral());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ServerInfoJson{");
        sb.append("isHub=").append(isHub);
        sb.append(", isPeripheral=").append(isPeripheral);
        sb.append('}');
        return sb.toString();
    }
}
