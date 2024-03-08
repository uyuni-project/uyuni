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

package com.suse.coco.modules;

import com.suse.coco.model.AttestationResult;

import org.apache.ibatis.session.SqlSession;

/**
 * A worker to process and verify attestation results.
 */
@FunctionalInterface
public interface AttestationWorker {

    /**
     * Process the given attestation result.
     * @param session the active mybatis database session
     * @param attestationResult the attestation result to process.
     * @return <code>true</code> if the processing succeeded, <code>false</code> otherwise.
     */
    boolean process(SqlSession session, AttestationResult attestationResult);
}
