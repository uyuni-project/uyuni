/*
 * Copyright (c) 2025 SUSE LLC
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
package com.redhat.rhn.domain.rpm;



import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * SourceRpm
 */
@Entity
@Table(name = "rhnSourceRpm")
public class SourceRpm implements Serializable {

    @Serial
    private static final long serialVersionUID = 6588701748928726469L;

    @Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RHN_SOURCERPM_ID_SEQ")
	@SequenceGenerator(name = "RHN_SOURCERPM_ID_SEQ", sequenceName = "RHN_SOURCERPM_ID_SEQ", allocationSize = 1)
    private Long id;

    @Column
    private String name;

    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }
    /**
     * @param i The id to set.
     */
    protected void setId(Long i) {
        this.id = i;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param n The name to set.
     */
    public void setName(String n) {
        this.name = n;
    }
}
