/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.redhat.rhn.domain.errata;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.io.Serializable;

public class KeywordId implements Serializable {

    @Serial
    private static final long serialVersionUID = 9157195810433775756L;

    private Errata errata;

    private String keyword;

    /**
     * Constructor
     */
    public KeywordId() {
    }

    /**
     * Constructor
     *
     * @param errataIn  the input errata
     * @param keywordIn the input keyword
     */
    public KeywordId(Errata errataIn, String keywordIn) {
        errata = errataIn;
        keyword = keywordIn;
    }

    public Errata getErrata() {
        return errata;
    }

    public void setErrata(Errata errataIn) {
        errata = errataIn;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keywordIn) {
        keyword = keywordIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (!(oIn instanceof KeywordId that)) {
            return false;
        }

        return new EqualsBuilder()
                .append(errata, that.errata)
                .append(keyword, that.keyword)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(errata)
                .append(keyword)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "KeywordId{" +
                "errata=" + errata +
                ", keyword='" + keyword + '\'' +
                '}';
    }
}
