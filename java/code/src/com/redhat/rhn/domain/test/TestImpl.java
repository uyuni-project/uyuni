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


import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 */
@Entity
@Table(name = "PERSIST_TEST")
public class TestImpl implements TestInterface {

    @Column(name = "test_column")
    private String testColumn;
    @Column
    private String foobar;
    @Column
    private String hidden;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PERSIST_SEQUENCE")
    @SequenceGenerator(name = "PERSIST_SEQUENCE", sequenceName = "PERSIST_SEQUENCE", allocationSize = 1)
    private Long id;
    @Column
    private Integer pin;
    @Column(updatable = false, insertable = false)
    private Date created;

    @Transient
    private String noColumnField;

    @Override
    public void setId(Long i) {
        id = i;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setPin(Integer i) {
        pin = i;
    }

    @Override
    public Integer getPin() {
        return pin;
    }

    @Override
    public void setFoobar(String foo) {
        foobar = foo;
    }

    @Override
    public String getFoobar() {
        return foobar;
    }

    public void setHidden(String hideIn) {
        hidden = hideIn;
    }

    public String getHidden() {
        return hidden;
    }

    @Override
    public void setTestColumn(String foo) {
        testColumn = foo;
    }

    @Override
    public String getTestColumn() {
        return testColumn;
    }

    @Override
    public void setNoColumnField(String foo) {
        this.noColumnField = foo;
    }

    @Override
    public String getNoColumnField() {
        return this.noColumnField;
    }

    @Override
    public void setCreated(Date d) {
        created = d;
    }

    @Override
    public Date getCreated() {
        return created;
    }
}
