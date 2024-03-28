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

package com.suse.coco.attestation;

import com.suse.coco.model.AttestationResult;
import com.suse.coco.model.AttestationStatus;
import com.suse.coco.module.AttestationWorker;
import com.suse.common.database.DatabaseSessionFactory;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Service class to handle {@link AttestationResult} in the database
 */
public class AttestationResultService {

    private static final Logger LOGGER = LogManager.getLogger(AttestationResultService.class);

    private final SqlSessionFactory sessionFactory;

    /**
     * Default constructor.
     */
    public AttestationResultService() {
        this(DatabaseSessionFactory.getSessionFactory());
    }

    /**
     * Build a services with the given session factory.
     * @param sessionFactoryIn the sql session factory
     */
    public AttestationResultService(SqlSessionFactory sessionFactoryIn) {
        this.sessionFactory = Objects.requireNonNull(sessionFactoryIn);
    }

    /**
     * Retrieve the ids of the available attestation results with the given state and result type.
     * @param resultTypeList a list of possible result types to match
     * @param batchSize the number of results to fetch at max
     * @return the ids of the attestation results matching the criteria
     */
    public List<Long> getPendingResultByType(Collection<Integer> resultTypeList, int batchSize) {
        try (SqlSession session = sessionFactory.openSession()) {
            return session.selectList(
                "AttestationResult.listPendingForResultType",
                Map.of("supportedTypes", resultTypeList, "batchSize", batchSize)
            );
        }
    }

    /**
     * Process an attestation result. The result is extracted from the database and locked for update.
     * @param id the id of the attestation result
     * @param worker the worker processing the attestation result
     */
    public void processAttestationResult(long id, AttestationWorker worker) {
        SqlSession session = sessionFactory.openSession();

        try {
            AttestationResult result = lockAttestationResult(session, id);
            if (result == null) {
                LOGGER.info("AttestationResult with id {} already taken - skipping", id);
                session.rollback();
                return;
            }

            LOGGER.info("AttestationResult with id {} selected for processing", id);
            boolean success = worker.process(session, result);
            if (success) {
                result.setStatus(AttestationStatus.SUCCEEDED);
                result.setAttested(OffsetDateTime.now());
            }
            else {
                result.setStatus(AttestationStatus.FAILED);
                result.setAttested(null);
            }

            session.update("AttestationResult.update", result);
            session.commit();
            LOGGER.info("AttestationResult with id {} completed - result is {}", id, result.getStatus());
        }
        catch (Exception ex) {
            LOGGER.debug("Unexpected error while processing of result {}", id, ex);
            session.rollback();
        }
        finally {
            session.close();
        }
    }

    private static AttestationResult lockAttestationResult(SqlSession session, long id) {
        try {
            return session.selectOne("AttestationResult.selectForUpdate", id);
        }
        catch (PersistenceException ex) {
            // If the error was due to the row being lock, just return null no need
            if (ex.getMessage() != null && ex.getMessage().contains("could not obtain lock on row")) {
                return null;
            }

            // Something different happened, better propagate the exception for logging purposes
            throw ex;
        }
    }
}
