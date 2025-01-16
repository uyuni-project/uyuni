/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.common.hibernate.converter;

import org.hibernate.type.StandardBooleanConverter;
import org.hibernate.type.descriptor.java.BooleanJavaType;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.java.StringJavaType;

public class YesNoConverterString  implements StandardBooleanConverter<String> {
    @Override
    public String convertToDatabaseColumn(Boolean attribute) {
        return toRelationalValue(attribute);
    }

    @Override
    public Boolean convertToEntityAttribute(String dbData) {
        return toDomainValue(dbData);
    }

    @Override
    public Boolean toDomainValue(String relationalForm) {
        if (relationalForm == null) {
            return null;
        }

        switch (relationalForm) {
            case "Y":
                return true;
            case "N":
                return false;
            default:
                return null;
        }
    }

    @Override
    public String toRelationalValue(Boolean domainForm) {
        if (domainForm == null) {
            return null;
        }

        return domainForm ? "Y" : "N";
    }

    @Override
    public JavaType<Boolean> getDomainJavaType() {
        return BooleanJavaType.INSTANCE;
    }

    @Override
    public JavaType<String> getRelationalJavaType() {
        return StringJavaType.INSTANCE;
    }

}
