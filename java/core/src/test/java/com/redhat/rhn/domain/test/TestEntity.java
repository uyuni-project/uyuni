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

package com.redhat.rhn.domain.test;


import com.redhat.rhn.domain.BaseDomainHelper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 */
@Entity
@Table(name = "PERSIST_TEST")
public class TestEntity extends BaseDomainHelper implements TestInterface {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PERSIST_SEQUENCE")
    @SequenceGenerator(name = "PERSIST_SEQUENCE", sequenceName = "PERSIST_SEQUENCE", allocationSize = 1)
    private Long id;

    @Column(name = "test_column")
    private String testColumn;

    @Column
    private String foobar;

    @Column
    private String hidden;

    @Column
    private Integer pin;

    @Transient
    private String noColumnField;

    public Long getId() {
        return id;
    }

    public void setId(Long idIn) {
        id = idIn;
    }

    public String getTestColumn() {
        return testColumn;
    }

    public void setTestColumn(String testColumnIn) {
        testColumn = testColumnIn;
    }

    public String getFoobar() {
        return foobar;
    }

    public void setFoobar(String foobarIn) {
        foobar = foobarIn;
    }

    public String getHidden() {
        return hidden;
    }

    public void setHidden(String hiddenIn) {
        hidden = hiddenIn;
    }

    public Integer getPin() {
        return pin;
    }

    public void setPin(Integer pinIn) {
        pin = pinIn;
    }

    public String getNoColumnField() {
        return noColumnField;
    }

    public void setNoColumnField(String noColumnFieldIn) {
        noColumnField = noColumnFieldIn;
    }
}
