/*
 * Copyright (c) 2024 SUSE LLC
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

package com.suse.coco.module.snpguest;

import com.suse.coco.model.AttestationResult;
import com.suse.coco.module.snpguest.model.AttestationReport;
import com.suse.coco.modules.AttestationWorker;

import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;

public class SNPGuestWorker implements AttestationWorker {

    private static final Logger LOGGER = LogManager.getLogger(SNPGuestWorker.class);

    @Override
    public boolean process(SqlSession session, AttestationResult attestationResult) {

        try {
            LOGGER.info("Processing attestation result {}", attestationResult);

            // Placeholder for the real logic
            Thread.sleep(Duration.ofSeconds(10).toMillis());

            AttestationReport report = session.selectOne("SNPGuestModule.retrieveReport", attestationResult.getReportId());
            LOGGER.info("Loaded report {}", report);

            return true;
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        catch (Exception ex) {
            LOGGER.error("Something went wrong", ex);
        }

        return false;
    }
}
