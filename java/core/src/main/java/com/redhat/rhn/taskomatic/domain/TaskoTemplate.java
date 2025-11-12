/*
 * Copyright (c) 2025 SUSE LLC
 * Copyright (c) 2010 Red Hat, Inc.
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
package com.redhat.rhn.taskomatic.domain;

import com.redhat.rhn.domain.BaseDomainHelper;


import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;


/**
 * TaskoTemplate
 */
@Entity
@Table(name = "rhnTaskoTemplate")
public class TaskoTemplate extends BaseDomainHelper {
    @Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tasko_template_seq")
	@SequenceGenerator(name = "tasko_template_seq", sequenceName = "RHN_TASKO_TEMPLATE_ID_SEQ", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bunch_id")
    private TaskoBunch bunch;

    @ManyToOne(fetch = FetchType.EAGER) // eager loading since `lazy=false` in XML
    @JoinColumn(name = "task_id")
    private TaskoTask task;

    @Column(name = "ordering")
    private Long ordering;

    @Column(name = "start_if")
    private String startIf;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TaskoRun> runHistory;
    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * @param idIn The id to set.
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @return Returns the bunch.
     */
    public TaskoBunch getBunch() {
        return bunch;
    }

    /**
     * @param bunchIn The bunch to set.
     */
    public void setBunch(TaskoBunch bunchIn) {
        this.bunch = bunchIn;
    }

    /**
     * @return Returns the task.
     */
    public TaskoTask getTask() {
        return task;
    }

    /**
     * @param taskIn The task to set.
     */
    public void setTask(TaskoTask taskIn) {
        this.task = taskIn;
    }

    /**
     * @return Returns the ordering.
     */
    public Long getOrdering() {
        return ordering;
    }

    /**
     * @param orderingIn The ordering to set.
     */
    public void setOrdering(Long orderingIn) {
        this.ordering = orderingIn;
    }

    /**
     * @return Returns the startIf.
     */
    public String getStartIf() {
        return startIf;
    }

    /**
     * @param startIfIn The startIf to set.
     */
    public void setStartIf(String startIfIn) {
        this.startIf = startIfIn;
    }


    /**
     * @return Returns the runHistory.
     */
    public Set<TaskoRun> getRunHistory() {
        return runHistory;
    }


    /**
     * @param runHistoryIn The runHistory to set.
     */
    public void setRunHistory(Set<TaskoRun> runHistoryIn) {
        this.runHistory = runHistoryIn;
    }
}
