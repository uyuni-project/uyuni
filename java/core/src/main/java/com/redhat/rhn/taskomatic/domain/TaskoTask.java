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

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * TaskoTask
 */
@Entity
@Table(name = "rhnTaskoTask")
public class TaskoTask extends BaseDomainHelper {

    @Id
    @GeneratedValue(generator = "RHN_TASKO_TASK_SEQ")
    @GenericGenerator(
            name = "RHN_TASKO_TASK_SEQ",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "RHN_TASKO_TASK_ID_SEQ"),
                    @Parameter(name = "increment_size", value = "1")
            })
    private Long id;

    @Column
    private String name;
    @Column(name = "class")
    private String taskClass;

    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }


    /**
     * @param idIn The id to set.
     */
    protected void setId(Long idIn) {
        this.id = idIn;
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
     * @return Returns the taskClass.
     */
    public String getTaskClass() {
        return taskClass;
    }


    /**
     * @param taskClassIn The taskClass to set.
     */
    public void setTaskClass(String taskClassIn) {
        this.taskClass = taskClassIn;
    }
}
