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

package com.suse.coco.module;

import java.util.Collections;
import java.util.List;

/**
 * A component that can verify {@link com.suse.coco.model.AttestationResult} and
 */
public interface AttestationModule {

    /**
     * Retrieves the name of the attestation module
     * @return the unique name of this module
     */
    String getName();

    /**
     * Retrieves attestation result type that this module is able to verify.
     * Please refer to uyuni codebase, class com/suse/manager/model/attestation/CoCoResultType.java for a list
     * of possible values
     * @return the supported result type
     */
    int getSupportedType();

    /**
     * Builds a worker to process {@link com.suse.coco.model.AttestationResult}
     * @return a new instance of {@link AttestationWorker}
     */
    AttestationWorker getWorker();

    /**
     * Additional Mybatis mappers needed by this attestation module.
     * @return a list of resources processable by {@link ClassLoader#getSystemResource(String)}
     */
    default List<String> getAdditionalMappers() {
        return Collections.emptyList();
    }
}
