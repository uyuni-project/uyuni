/*
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

/**
 * TaskoTask
 */
public class TaskoTask extends BaseDomainHelper {

    private Long id;
    private String name;
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
    public void setId(Long idIn) {
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
