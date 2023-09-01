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

package com.suse.oval.ovaltypes;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "ExistenceEnumeration", namespace = "http://oval.mitre.org/XMLSchema/oval-common-5")
@XmlEnum
public enum ExistenceEnum {


    /**
     * A value of 'all_exist' means that every object defined by the description exists on the system.
     */
    @XmlEnumValue("all_exist")
    ALL_EXIST("all_exist"),

    /**
     * A value of 'any_exist' means that zero or more objects defined by the description exist on the system.
     */
    @XmlEnumValue("any_exist")
    ANY_EXIST("any_exist"),

    /**
     * A value of 'at_least_one_exists' means that at least one object defined by the description exists on the system.
     */
    @XmlEnumValue("at_least_one_exists")
    AT_LEAST_ONE_EXISTS("at_least_one_exists"),

    /**
     * A value of 'none_exist' means that none of the objects defined by the description exist on the system.
     */
    @XmlEnumValue("none_exist")
    NONE_EXIST("none_exist"),

    /**
     * A value of 'only_one_exists' means that only one object defined by the description exists on the system.
     */
    @XmlEnumValue("only_one_exists")
    ONLY_ONE_EXISTS("only_one_exists");
    private final String value;

    ExistenceEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ExistenceEnum fromValue(String v) {
        for (ExistenceEnum c: ExistenceEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
