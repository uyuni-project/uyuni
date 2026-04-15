/*
 * Copyright (c) 2023 SUSE LLC
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

package com.redhat.rhn.domain.credentials;

import com.redhat.rhn.domain.cloudpayg.PaygSshData;

public interface CloudCredentials extends Credentials {

    /**
     * Retrieves the cloud authentication data
     * @return the cloud authentication data
     */
    byte[] getExtraAuthData();

    /**
     * Sets the cloud authentication data
     * @param extraAuthDataIn the cloud authentication data
     */
    void setExtraAuthData(byte[] extraAuthDataIn);

    /**
     * Retrieves the ssh configuration to access the PAYG client
     * @return the SSH PAYG configuration
     */
    PaygSshData getPaygSshData();

    /**
     * Sets the ssh configuration to access the PAYG client
     * @param paygSshDataIn the SSH PAYG configuration
     */
    void setPaygSshData(PaygSshData paygSshDataIn);
}
