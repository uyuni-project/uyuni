/*
 * Copyright (c) 2023 SUSE LLC
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

package com.suse.cloud.domain;

import com.redhat.rhn.domain.errata.CustomEnumType;

import org.hibernate.engine.spi.SharedSessionContractImplementor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Maps the {@link BillingDimension} enum to its integer id
 */
public class BillingDimensionEnumType extends CustomEnumType<BillingDimension, String> {

    /**
     * Default Constructor
     */
    public BillingDimensionEnumType() {
        super(BillingDimension.class, String.class, BillingDimension::getLabel, v -> BillingDimension.byLabel(v));
    }

    @Override
    public int getSqlType() {
        // Returning other, as this is mapped to a PostgreSQL enum, not to a VARCHAR
        return Types.OTHER;
    }

    @Override
    public String nullSafeGet(ResultSet var1, int var2,
            SharedSessionContractImplementor var3, @Deprecated Object var4)
            throws SQLException {
        return null;
    }

    @Override
    public void nullSafeSet(PreparedStatement var1, Object var2, int var3,
            SharedSessionContractImplementor var4)
            throws SQLException {
        return;
    }
}
