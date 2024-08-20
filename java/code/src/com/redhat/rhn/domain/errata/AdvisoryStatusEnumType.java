/*
 * Copyright (c) 2021 SUSE LLC
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
package com.redhat.rhn.domain.errata;

import org.hibernate.engine.spi.SharedSessionContractImplementor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * AdvisoryStatusEnumType
 */
public class AdvisoryStatusEnumType extends CustomEnumType<AdvisoryStatus, String> {
    /**
     * Constructor
     */
    public AdvisoryStatusEnumType() {
        super(AdvisoryStatus.class, String.class, AdvisoryStatus::getMetadataValue,
            s -> AdvisoryStatus.fromMetadata(s).orElse(null));
    }

    @Override
    public String nullSafeGet(ResultSet var1, int var2,
            SharedSessionContractImplementor var3, @Deprecated Object var4)
        throws SQLException {
        // TODO Hibernate 6
        return null;
    };

    @Override
    public void nullSafeSet(PreparedStatement var1, Object var2, int var3,
            SharedSessionContractImplementor var4)
        throws SQLException {
    };
}
