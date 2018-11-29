/**
 * Copyright (c) 2016 SUSE LLC
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
package com.redhat.rhn.domain.action.salt;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.ConstructorException;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ApplyStatesActionResult
 */
public class ApplyStatesActionResult implements Serializable {

    private Long serverId;
    private Long actionApplyStatesId;
    private Long returnCode;
    private byte[] output;

    private ApplyStatesActionDetails parentActionDetails;

    /**
     * @return the serverId
     */
    public Long getServerId() {
        return serverId;
    }

    /**
     * @param sid serverId to set
     */
    public void setServerId(Long sid) {
        this.serverId = sid;
    }

    /**
     * @return the actionApplyStatesId
     */
    public Long getActionApplyStatesId() {
        return actionApplyStatesId;
    }

    /**
     * @param actionId the actionApplyStatesId to set.
     */
    public void setActionApplyStatesId(Long actionId) {
        this.actionApplyStatesId = actionId;
    }

    /**
     * @return the return code
     */
    public Long getReturnCode() {
        return returnCode;
    }

    /**
     * @param returnCodeIn the return code to set
     */
    public void setReturnCode(Long returnCodeIn) {
        this.returnCode = returnCodeIn;
    }

    /**
     * @return the parentActionDetails
     */
    public ApplyStatesActionDetails getParentScriptActionDetails() {
        return parentActionDetails;
    }

    /**
     * @param parentActionDetailsIn the parentActionDetails to set
     */
    public void setParentScriptActionDetails(
            ApplyStatesActionDetails parentActionDetailsIn) {
        this.parentActionDetails = parentActionDetailsIn;
    }

    /**
     * @return the output
     */
    public byte[] getOutput() {
        return output;
    }

    /**
     * @param outputIn the output
     */
    public void setOutput(byte[] outputIn) {
        this.output = outputIn;
    }

    /**
     * @return String version of the Script contents
     */
    public String getOutputContents() {
        return HibernateFactory.getByteArrayContents(getOutput());
    }

    /**
     * @return Optional with list of state results or empty
     */
    public Optional<List<StateResult>> getResult() {
        Yaml yaml = new Yaml();
        List<StateResult> result = new LinkedList<>();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> payload = yaml.loadAs(getOutputContents(), Map.class);
            payload.entrySet().stream().forEach(e -> {
                result.add(new StateResult(e));
            });
        }
        catch (ConstructorException ce) {
            return Optional.empty();
        }
        return Optional.of(result);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ApplyStatesActionResult)) {
            return false;
        }

        ApplyStatesActionResult result = (ApplyStatesActionResult) obj;

        return new EqualsBuilder()
                .append(this.getActionApplyStatesId(), result.getActionApplyStatesId())
                .append(this.getServerId(), result.getServerId())
                .append(this.getReturnCode(), result.getReturnCode())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getActionApplyStatesId())
                .append(getServerId())
                .append(getReturnCode())
                .toHashCode();
    }
}
