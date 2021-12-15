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
package com.suse.manager.errata.test;

import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.testing.RhnBaseTestCase;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Base class for testing implementations of {@link com.suse.manager.errata.VendorSpecificErrataParser}
 */
abstract class BaseErrataTestCase extends RhnBaseTestCase {

    /**
     * Builds a test errata object
     */
    protected Errata createErrata(String advisory, String type, LocalDate issedDate, Long release) {
        final Errata errata = new Errata();

        if (issedDate != null) {
            errata.setIssueDate(Date.from(issedDate.atStartOfDay(ZoneOffset.systemDefault()).toInstant()));
        }

        errata.setAdvisory(advisory);
        errata.setAdvisoryRel(release);
        errata.setAdvisoryType(type);
        return errata;
    }


}
