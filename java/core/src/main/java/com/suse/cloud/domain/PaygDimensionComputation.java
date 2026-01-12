/*
 * Copyright (c) 2023--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.cloud.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * Entity that represent the result of computation made by
 * {@link com.redhat.rhn.taskomatic.task.payg.PaygComputeDimensionsTask}.
 */
@Entity
@Table(name = "susepaygdimensioncomputation")
public class PaygDimensionComputation {

    private Long id;

    private Date timestamp;

    private boolean success;

    private Set<PaygDimensionResult> dimensionResults;

    /**
     * Default constructor.
     */
    public PaygDimensionComputation() {
        this.timestamp = new Date();
        this.dimensionResults = new HashSet<>();
    }

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "paygDimensionComputation_seq")
    @SequenceGenerator(
            name = "paygDimensionComputation_seq",
            sequenceName = "susePaygDimensionComputation_id_seq",
            allocationSize = 1
    )
    public Long getId() {
        return id;
    }

    public void setId(Long idIn) {
        this.id = idIn;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "timestamp")
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestampIn) {
        this.timestamp = timestampIn;
    }

    @Column(name = "success")
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean successIn) {
        this.success = successIn;
    }

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "computation")
    public Set<PaygDimensionResult> getDimensionResults() {
        return dimensionResults;
    }

    public void setDimensionResults(Set<PaygDimensionResult> dimensionResultsIn) {
        this.dimensionResults = dimensionResultsIn;
    }

    /**
     * Add the result for a specific dimension
     * @param dimension the computed dimension
     * @param count the number of instances for this dimension
     */
    public void addDimensionResult(BillingDimension dimension, long count) {
        if (dimension == null) {
            return;
        }

        if (dimensionResults == null) {
            dimensionResults = new HashSet<>();
        }
        else {
            removeDimension(dimension);
        }

        dimensionResults.add(new PaygDimensionResult(dimension, this, count));
    }

    /**
     * Removes the result for the specified dimension
     * @param dimension the dimension to remove from the stored results
     * @return true if the dimension existed, false otherwise
     */
    public boolean removeDimension(BillingDimension dimension) {
        if (dimensionResults == null || dimension == null) {
            return false;
        }

        return dimensionResults.removeIf(result -> dimension.equals(result.getDimension()));
    }

    /**
     * Returns the result corresponding to the given dimension
     * @param dimension the dimension to retrieve
     * @return the result if present or null
     */
    public Optional<PaygDimensionResult> getResultForDimension(BillingDimension dimension) {
        return dimensionResults.stream()
                               .filter(result -> dimension.equals(result.getDimension()))
                               .findFirst();
    }

    /**
     * Removes all dimensions results previously added.
     */
    public void clearDimensions() {
        dimensionResults = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof PaygDimensionComputation that)) {
            return false;
        }

        return new EqualsBuilder().append(success, that.success).append(id, that.id)
                                  .append(timestamp, that.timestamp)
                                  .append(dimensionResults, that.dimensionResults)
                                  .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).append(timestamp).append(success).append(dimensionResults)
                                          .toHashCode();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PaygDimensionComputation.class.getSimpleName() + "[", "]")
            .add("id=" + id)
            .add("timestamp=" + timestamp)
            .add("success=" + success)
            .add("dimensionResults=" + dimensionResults)
            .toString();
    }
}
