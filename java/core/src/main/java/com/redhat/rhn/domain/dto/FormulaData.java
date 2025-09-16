/*
 * Copyright (c) 2020 SUSE LLC
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

package com.redhat.rhn.domain.dto;

import java.util.Map;

/**
 * Class for representing the formula data of a minion system
 */
public class FormulaData {

    private Long systemID;
    private String minionID;
    private Map<String, Object> formulaValues;

    /**
     * Instantiates a new formula data.
     *
     * @param serverIDIn the server ID
     * @param minionIDIn the minion ID
     * @param formulaValuesIn the formula values
     */
    public FormulaData(Long serverIDIn, String minionIDIn, Map<String, Object> formulaValuesIn) {
        super();
        this.systemID = serverIDIn;
        this.minionID = minionIDIn;
        this.formulaValues = formulaValuesIn;
    }

    /**
     * Gets the system ID.
     *
     * @return the system ID
     */
    public Long getSystemID() {
        return systemID;
    }

    /**
     * Gets the minion ID.
     *
     * @return the minion ID
     */
    public String getMinionID() {
        return minionID;
    }

    /**
     * Gets the formula values.
     *
     * @return the formula values
     */
    public Map<String, Object> getFormulaValues() {
        return formulaValues;
    }

    @Override
    public String toString() {
        return "FormulaData [systemID=" + systemID + ", minionID=" + minionID + ", formulaValues=" + formulaValues +
                "]";
    }

}
