/*
 * Copyright (c) 2021 SUSE LLC
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

package com.redhat.rhn.manager.errata;

import com.redhat.rhn.common.localization.LocalizationService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Retracted Errata Exception
 */
public class RetractedErrataException extends RuntimeException  {

    /**
     * Constructor
     * @param errataIdsIn The retracted errata ids.
     */
    public RetractedErrataException(List<Long> errataIdsIn) {
        super(LocalizationService.getInstance().
                getMessage("api.errata.retractederrata",
                        errataIdsIn.stream().map(Object::toString).collect(Collectors.joining(","))));
    }
}
