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
package com.redhat.rhn.domain.channel;

import com.redhat.rhn.domain.BaseDomainHelper;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * ContentSourceFilter
 */
@Entity
@Table(name = "rhnContentSourceFilter")
public class ContentSourceFilter extends BaseDomainHelper {

    @Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "csf_seq")
	@SequenceGenerator(name = "csf_seq", sequenceName = "rhn_csf_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "source_id")
    private Long sourceId;

    @Column
    private String flag;

    @Column
    private String filter;

    @Column(name = "sort_order")
    private int sortOrder;

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
     * @return Returns the sourceId.
     */
    public Long getSourceId() {
        return sourceId;
    }

    /**
     * @param sourceIdIn The sourceId to set.
     */
    public void setSourceId(Long sourceIdIn) {
        this.sourceId = sourceIdIn;
    }

    /**
     * @return Returns the flag.
     */
    public String getFlag() {
        return flag;
    }

    /**
     * @param flagIn The flag to set.
     */
    public void setFlag(String flagIn) {
        this.flag = flagIn;
    }

    /**
     * @return Returns the filter.
     */
    public String getFilter() {
        return filter;
    }

    /**
     * @param filterIn The filter to set.
     */
    public void setFilter(String filterIn) {
        this.filter = filterIn;
    }

    /**
     * @return Returns the sortOrder.
     */
    public int getSortOrder() {
        return sortOrder;
    }

    /**
     * @param sortOrderIn The sortOrder to set.
     */
    public void setSortOrder(int sortOrderIn) {
        this.sortOrder = sortOrderIn;
    }
}
