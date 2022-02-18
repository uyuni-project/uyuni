/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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

package com.redhat.rhn.frontend.xmlrpc;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.common.localization.LocalizationService;

/**
 * Invalid Package Exception
 *
 */
public class IpRangeConflictException extends FaultException  {

    /**
     * Constructor
     * @param range reason the system couldn't be subscribed
     */
    public IpRangeConflictException(String range) {
        super(2785, "ipRange" , LocalizationService.getInstance().
                getMessage("api.kickstart.iprange.duplicate",
                        new Object [] {range}));
    }

    /**
     * Constructor
     * @param range reason the system couldn't be subscribed
     * @param cause the cause
     */
    public IpRangeConflictException(String range, Throwable cause) {
        super(2785, "ipRange" , LocalizationService.getInstance().
                getMessage("api.kickstart.iprange.duplicate",
                        new Object [] {range}), cause);
    }

}
