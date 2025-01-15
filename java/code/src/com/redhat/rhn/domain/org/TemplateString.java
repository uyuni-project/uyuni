/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.domain.org;

import com.redhat.rhn.domain.BaseDomainHelper;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;



/**
 * TemplateString
 */
@Entity
@Table(name = "RHNTEMPLATESTRING")
public class TemplateString extends BaseDomainHelper {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "template_string_seq")
    @SequenceGenerator(name = "template_string_seq", sequenceName = "RHN_TEMPLATE_STR_ID_SEQ", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "label", length = 64)
    private String label;

    @Column(name = "value", length = 4000)
    private String value;

    @Column(name = "description", length = 512)
    private String description;

    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id", nullable = false, insertable = false,
            updatable = false)
    private TemplateCategory category;

    /**
     * @return Returns the category.
     */
    public TemplateCategory getCategory() {
        return category;
    }
    /**
     * @param categoryIn The category to set.
     */
    public void setCategory(TemplateCategory categoryIn) {
        this.category = categoryIn;
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
     * @return Returns the label.
     */
    public String getLabel() {
        return label;
    }
    /**
     * @param labelIn The label to set.
     */
    public void setLabel(String labelIn) {
        this.label = labelIn;
    }
    /**
     * @return Returns the value.
     */
    public String getValue() {
        return value;
    }
    /**
     * @param valueIn The value to set.
     */
    public void setValue(String valueIn) {
        this.value = valueIn;
    }
}
