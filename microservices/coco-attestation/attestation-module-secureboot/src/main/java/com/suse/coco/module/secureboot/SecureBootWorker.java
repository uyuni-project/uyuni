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
package com.suse.coco.module.secureboot;

import com.suse.coco.model.AttestationResult;
import com.suse.coco.module.AttestationWorker;

import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SecureBootWorker implements AttestationWorker {
    private static final Logger LOGGER = LogManager.getLogger(SecureBootWorker.class);

    @Override
    public boolean process(SqlSession session, AttestationResult result) {
        try {
            LOGGER.debug("Processing attestation result {}", result.getId());

            String secureBootMsg = session.selectOne("SecureBootModule.retrieveReport", result.getReportId());
            if (secureBootMsg == null) {
                LOGGER.error("Unable to retrieve attestation report for result {}", result.getId());
                return false;
            }

            result.setDetails(secureBootMsg);
            String bootMsgLowerCase = secureBootMsg.toLowerCase();
            // got these messages from mokutil source code
            return bootMsgLowerCase.contains("secureboot enabled") &&
                    !bootMsgLowerCase.contains("secureboot disabled") &&
                    !bootMsgLowerCase.contains("is disabled in shim") &&
                    !bootMsgLowerCase.contains("cannot determine secure boot state") &&
                    !bootMsgLowerCase.contains("failed to read") &&
                    !bootMsgLowerCase.contains("efi variables are not supported on this system");
        }
        catch (Exception ex) {
            LOGGER.error("Unable to process attestation result {}", result.getId(), ex);
        }
        return false;
    }
}
