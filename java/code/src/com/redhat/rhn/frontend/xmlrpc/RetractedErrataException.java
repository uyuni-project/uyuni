/**
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

import java.util.List;
import java.util.stream.Collectors;

/**
 * Retracted Errata Exception
 *
 * @version $Rev$
 */
public class RetractedErrataException extends FaultException  {

    /**
     * Constructor
     * @param errataIds The retracted errata ids.
     */
    public RetractedErrataException(List<Long> errataIds) {
        super(2602, "retractedErrata" , LocalizationService.getInstance().
                getMessage("api.errata.retractederrata",
                        errataIds.stream().map(Object::toString).collect(Collectors.joining(","))));
    }

    /**
     * Constructor
     * @param errataIds The retracted errata ids.
     * @param cause the cause
     */
    public RetractedErrataException(List<Long> errataIds, Throwable cause) {
        super(2602, "retractedErrata" , LocalizationService.getInstance().
                getMessage("api.errata.retractederrata",
                        errataIds.stream().map(Object::toString).collect(Collectors.joining(","))),
                cause);
    }

}
