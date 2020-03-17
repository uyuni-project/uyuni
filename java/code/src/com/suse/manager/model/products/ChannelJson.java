/**
 * Copyright (c) 2018 SUSE LLC
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

package com.suse.manager.model.products;

import com.redhat.rhn.frontend.dto.SetupWizardProductDto;
import com.redhat.rhn.frontend.dto.SetupWizardProductDto.SyncStatus.SyncStage;

/**
 * ChannelJson
 */
public class ChannelJson {

    private final Long id;
    private final String name;
    private final String label;
    private final String summary;
    private final boolean optional;
    private final SetupWizardProductDto.SyncStatus.SyncStage status;

    /**
     * Constructor
     *
     * @param idIn the id of the channel, if present
     * @param nameIn the name of the channel
     * @param labelIn the label of the channel
     * @param summaryIn the summary of the channel
     * @param optionalIn if the channel is mandatory or optional
     * @param statusIn the status of the channel
     */
    public ChannelJson(Long idIn, String nameIn, String labelIn,
                       String summaryIn, boolean optionalIn,
                       SetupWizardProductDto.SyncStatus.SyncStage statusIn) {
        this.id = idIn;
        this.name = nameIn;
        this.label = labelIn;
        this.summary = summaryIn;
        this.optional = optionalIn;
        this.status = statusIn;
    }

    /**
     * @return the {@link SyncStage} channel status
     */
    public SetupWizardProductDto.SyncStatus.SyncStage getStatus() {
        return status;
    }

    /**
     * @return the id of the channel
     */
    public Long getId() { return id; }

    /**
     * @return the label of the channel
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return the name of the channel
     */
    public String getName() {
        return name;
    }

    /**
     * @return the summary of the channel
     */
    public String getSummary() {
        return summary;
    }

    /**
     * @return the optional flag of the channel
     */
    public boolean isOptional() {
        return optional;
    }
}
