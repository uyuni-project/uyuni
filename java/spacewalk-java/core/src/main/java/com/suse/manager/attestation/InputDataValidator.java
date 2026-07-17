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

import java.util.List;
import java.util.Map;

public interface InputDataValidator {

    /**
     * Validate the given input data
     * @param inputData the input data of the attestation configuration
     * @return an empty list if the input data is valid, a list of localized error messages otherwise
     */
    List<String> validate(Map<String, Object> inputData);
}
