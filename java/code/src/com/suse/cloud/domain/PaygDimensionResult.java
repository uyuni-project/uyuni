/*
 * Copyright (c) 2023--2025 SUSE LLC
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

package com.suse.cloud.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;

import java.util.StringJoiner;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "susepaygdimensionresult")
public class PaygDimensionResult {

    private Long id;

    private PaygDimensionComputation computation;

    private BillingDimension dimension;

    private Long count;

    /**
     * Default constructor
     */
    public PaygDimensionResult() {
    }

    /**
     * Build an instance with the specified values
     * @param dimensionIn the billing dimension
     * @param computationIn the computation that this result is part of
     * @param countIn the count computed for this dimension
     */
    public PaygDimensionResult(BillingDimension dimensionIn, PaygDimensionComputation computationIn, Long countIn) {
        this.dimension = dimensionIn;
        this.computation = computationIn;
        this.count = countIn;
    }

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "paygDimensionResult_seq")
    @SequenceGenerator(name = "paygDimensionResult_seq", sequenceName = "susePaygDimensionResult_id_seq",
            allocationSize = 1)
    public Long getId() {
        return id;
    }

    public void setId(Long idIn) {
        this.id = idIn;
    }

    @ManyToOne()
    @JoinColumn(name = "computation_id", nullable = false)
    public PaygDimensionComputation getComputation() {
        return computation;
    }

    public void setComputation(PaygDimensionComputation computationIn) {
        this.computation = computationIn;
    }

    @Transient
    public Long getComputationId() {
        return this.computation != null ? this.computation.getId() : null;
    }

    @Column(name = "dimension")
    @Type(value = com.suse.cloud.domain.BillingDimensionEnumType.class)
    public BillingDimension getDimension() {
        return dimension;
    }

    public void setDimension(BillingDimension dimensionIn) {
        this.dimension = dimensionIn;
    }

    @Column(name = "count")
    public Long getCount() {
        return count;
    }

    public void setCount(Long countIn) {
        this.count = countIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PaygDimensionResult that = (PaygDimensionResult) o;

        return new EqualsBuilder().append(id, that.id)
                                  .append(getComputationId(), that.getComputationId())
                                  .append(dimension, that.dimension)
                                  .append(count, that.count)
                                  .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id)
                                          .append(getComputationId())
                                          .append(dimension).append(count)
                                          .toHashCode();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PaygDimensionResult.class.getSimpleName() + "[", "]")
            .add("id=" + id)
            .add("computationId=" + getComputationId())
            .add("dimension=" + dimension)
            .add("count=" + count)
            .toString();
    }

}
