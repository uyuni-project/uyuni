/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.attestation;

import com.suse.manager.model.attestation.CoCoEnvironmentType;

public class AttestationInputDataValidatorFactory {

    private AttestationInputDataValidatorFactory() {
        // Prevent Instantiation
    }

    /**
     * Retrieves an {@link InputDataValidator} suitable for the specified environment.
     * @param environment the attestation environment
     * @return the correct instance of {@link InputDataValidator} for the given environment.
     */
    public static InputDataValidator forEnvironment(CoCoEnvironmentType environment) {
        return switch (environment) {
            case KVM_IBM_Z16, KVM_IBM_Z17 -> new IbmInputDataValidator();
            default -> new AlwaysValidInputDataValidator();
        };
    }

}
