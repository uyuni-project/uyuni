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
package com.redhat.rhn.domain.user;

import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_ONLY;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.Immutable;

import java.io.Serial;
import java.io.Serializable;
import java.util.TimeZone;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * TimeZone
 */
@Entity
@Table(name = "rhnTimeZone")
@Immutable
@Cache(usage = READ_ONLY)
public class RhnTimeZone implements Serializable {

    @Serial
    private static final long serialVersionUID = 5662180343397557513L;

    @Id
    @Column(name = "id")
    private int timeZoneId;

    @Column(name = "olson_name")
    private String olsonName;

    /**
     * @return Returns the olsonName.
     */
    public String getOlsonName() {
        return olsonName;
    }

    /**
     * @param o The olsonName to set.
     */
    public void setOlsonName(String o) {
        this.olsonName = o;
    }

    /**
     * @return Returns the timeZoneId.
     */
    public int getTimeZoneId() {
        return timeZoneId;
    }

    /**
     * @param t The timeZoneId to set.
     */
    public void setTimeZoneId(int t) {
        this.timeZoneId = t;
    }

    /**
     * @return Returns the timeZone.
     */
    public TimeZone getTimeZone() {
        if (null == olsonName) {
            return null;
        }
        return TimeZone.getTimeZone(olsonName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        boolean answer = false;
        if (o instanceof RhnTimeZone && this.hashCode() == o.hashCode()) {
                answer = true;
            }

        return answer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = 17;
        result += 37 * timeZoneId;
        result += 37 * (olsonName == null ? 0 : olsonName.hashCode());
        return result;
    }
}
