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
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "OperatorEnumeration", namespace = "http://oval.mitre.org/XMLSchema/oval-common-5")
@XmlEnum
public enum LogicOperatorType {


    /**
     * The AND operator produces a true result if every argument is true. If one or more arguments are false,
     * the result of the AND is false. If one or more of the arguments are unknown, and if none of the arguments
     * are false, then the AND operator produces a result of unknown.
     */
    AND,

    /**
     * The ONE operator produces a true result if one and only one argument is true. If there are more than argument
     * is true (or if there are no true arguments), the result of the ONE is false. If one or more of the arguments
     * are unknown, then the ONE operator produces a result of unknown.
     */
    ONE,

    /**
     * The OR operator produces a true result if one or more arguments is true. If every argument is false, the result
     * of the OR is false. If one or more of the arguments are unknown and if none of arguments are true, then the
     * OR operator produces a result of unknown.
     */
    OR,

    /**
     * XOR is defined to be true if an odd number of its arguments are true, and false otherwise. If any of the
     * arguments are unknown, then the XOR operator produces a result of unknown.
     */
    XOR;

    public String value() {
        return name();
    }

    public static LogicOperatorType fromValue(String v) {
        return valueOf(v);
    }

}
