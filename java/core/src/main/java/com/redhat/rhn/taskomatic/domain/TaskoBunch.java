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


import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * TaskoBunch
 */
@Entity
@Table(name = "rhnTaskoBunch")
public class TaskoBunch extends BaseDomainHelper {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tasko_bunch_seq")
    @SequenceGenerator(name = "tasko_bunch_seq", sequenceName = "RHN_TASKO_BUNCH_ID_SEQ", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "org_bunch")
    private String orgBunch;

    @OneToMany(mappedBy = "bunch", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "ordering")
    private List<TaskoTemplate> templates;

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
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param descriptionIn The description to set.
     */
    public void setDescription(String descriptionIn) {
        this.description = descriptionIn;
    }

    /**
     * @return Returns the templates.
     */
    public List<TaskoTemplate> getTemplates() {
        return templates;
    }

    /**
     * @param templatesIn The templates to set.
     */
    public void setTemplates(List<TaskoTemplate> templatesIn) {
        this.templates = templatesIn;
    }

    /**
     * @return Returns the orgBunch.
     */
    public String getOrgBunch() {
        return orgBunch;
    }

    /**
     * @param orgBunchIn The orgBunch to set.
     */
    public void setOrgBunch(String orgBunchIn) {
        this.orgBunch = orgBunchIn;
    }
}
