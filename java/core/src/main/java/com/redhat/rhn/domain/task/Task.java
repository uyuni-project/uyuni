/*
 * Copyright (c) 2025 SUSE LLC
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.domain.task;

import com.redhat.rhn.domain.org.Org;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Task
 */
@Entity
@Table(name = "rhnTaskQueue")
public class Task implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "task_queue_seq")
    @SequenceGenerator(name = "task_queue_seq", sequenceName = "rhn_task_queue_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "task_name", nullable = false, length = 64)
    private String name;

    @Column(name = "task_data")
    private Long data;

    @Column
    private int priority;

    @Column(nullable = false)
    private Date earliest;

    @ManyToOne(optional = false)
    @JoinColumn(name = "org_id")
    private Org org;

    /**
     * @return Returns the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     * @param idIn - the id
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    /**
     * @return Returns the data.
     */
    public Long getData() {
        return data;
    }

    /**
     * @param dataIn The data to set.
     */
    public void setData(Long dataIn) {
        this.data = dataIn;
    }

    /**
     * @return Returns the earliest.
     */
    public Date getEarliest() {
        return earliest;
    }

    /**
     * @param earliestIn The earliest to set.
     */
    public void setEarliest(Date earliestIn) {
        this.earliest = earliestIn;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param nameIn The name to set.
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * @return Returns the org.
     */
    public Org getOrg() {
        return org;
    }

    /**
     * @param orgIn The org to set.
     */
    public void setOrg(Org orgIn) {
        this.org = orgIn;
    }

    /**
     * @return Returns the priority.
     */
    public int getPriority() {
        return priority;
    }

    /**
     * @param priorityIn The priority to set.
     */
    public void setPriority(int priorityIn) {
        this.priority = priorityIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Task castOther)) {
            return false;
        }
        return new EqualsBuilder().append(org, castOther.org)
                                  .append(name, castOther.name)
                                  .append(data, castOther.data)
                                  .append(priority, castOther.priority)
                                  .append(earliest, castOther.earliest)
                                  .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(org)
                                    .append(name)
                                    .append(data)
                                    .append(priority)
                                    .append(earliest)
                                    .toHashCode();
    }
}
