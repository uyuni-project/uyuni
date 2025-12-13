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

package com.suse.manager.maintenance.rescheduling;

public enum RescheduleStrategyType {

    CANCEL("Cancel"),
    FAIL("Fail");

    private String label;

    RescheduleStrategyType(String labelIn) {
        this.label = labelIn;
    }

    /**
     * Get type from label string
     * @param l the label
     * @return the {@link RescheduleStrategyType}
     */
    public static RescheduleStrategyType fromLabel(String l) {
        for (RescheduleStrategyType type : values()) {
            if (type.label.equals(l)) {
                return type;
            }
        }
        throw new IllegalArgumentException(String.format("Reschedule Strategy '%s' does not exist.", l));
    }

    /**
     * Create a {@link RescheduleStrategy} from type
     * @return RescheduleStrategy
     */
    public RescheduleStrategy createInstance() {
        switch (this) {
            case CANCEL:
                return new CancelRescheduleStrategy();
            case FAIL:
                return new FailRescheduleStrategy();
            default:
                throw new IllegalArgumentException(String.format("Don't know how to create '%s'.", this));
        }
    }

    /**
     * Gets the label.
     *
     * @return label
     */
    public String getLabel() {
        return label;
    }
}
