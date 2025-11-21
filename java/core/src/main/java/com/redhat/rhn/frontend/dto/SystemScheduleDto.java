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

package com.redhat.rhn.frontend.dto;

/**
 * A DTO of basic system data and their assigned maintenance schedules
 */
public class SystemScheduleDto {
    private Long id;
    private String name;
    private Long scheduleId;
    private String scheduleName;

    public Long getId() {
        return id;
    }

    public void setId(Long idIn) {
        id = idIn;
    }

    public String getName() {
        return name;
    }

    public void setName(String nameIn) {
        name = nameIn;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleIdIn) {
        scheduleId = scheduleIdIn;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleNameIn) {
        scheduleName = scheduleNameIn;
    }
}
