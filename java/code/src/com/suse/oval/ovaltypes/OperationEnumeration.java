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


@XmlType(name = "OperationEnumeration", namespace = "http://oval.mitre.org/XMLSchema/oval-common-5")
@XmlEnum
public enum OperationEnumeration {


    /**
     * The 'equals' operation returns true if the actual value on the system is equal to the stated entity.  When the specified datatype is a string, this results in a case-sensitive comparison.
     * 
     */
    @XmlEnumValue("equals")
    EQUALS("equals"),

    /**
     * The 'not equal' operation returns true if the actual value on the system is not equal to the stated entity.  When the specified datatype is a string, this results in a case-sensitive comparison.
     * 
     */
    @XmlEnumValue("not equal")
    NOT_EQUAL("not equal"),

    /**
     * The 'case insensitive equals' operation is meant for string data and returns true if the actual value on the system is equal (using a case insensitive comparison) to the stated entity.
     * 
     */
    @XmlEnumValue("case insensitive equals")
    CASE_INSENSITIVE_EQUALS("case insensitive equals"),

    /**
     * The 'case insensitive not equal' operation is meant for string data and returns true if the actual value on the system is not equal (using a case insensitive comparison) to the stated entity.
     * 
     */
    @XmlEnumValue("case insensitive not equal")
    CASE_INSENSITIVE_NOT_EQUAL("case insensitive not equal"),

    /**
     * The 'greater than' operation returns true if the actual value on the system is greater than the stated entity.
     * 
     */
    @XmlEnumValue("greater than")
    GREATER_THAN("greater than"),

    /**
     * The 'less than' operation returns true if the actual value on the system is less than the stated entity.
     * 
     */
    @XmlEnumValue("less than")
    LESS_THAN("less than"),

    /**
     * The 'greater than or equal' operation returns true if the actual value on the system is greater than or equal to the stated entity.
     * 
     */
    @XmlEnumValue("greater than or equal")
    GREATER_THAN_OR_EQUAL("greater than or equal"),

    /**
     * The 'less than or equal' operation returns true if the actual value on the system is less than or equal to the stated entity.
     * 
     */
    @XmlEnumValue("less than or equal")
    LESS_THAN_OR_EQUAL("less than or equal"),

    /**
     * The 'bitwise and' operation is used to determine if a specific bit is set. It returns true if performing a BITWISE AND with the binary representation of the stated entity against the binary representation of the actual value on the system results in a binary value that is equal to the binary representation of the stated entity. For example, assuming a datatype of 'int', if the actual integer value of the setting on your machine is 6 (same as 0110 in binary), then performing a 'bitwise and' with the stated integer 4 (0100) returns 4 (0100). Since the result is the same as the state mask, then the test returns true. If the actual value on your machine is 1 (0001), then the 'bitwise and' with the stated integer 4 (0100) returns 0 (0000). Since the result is not the same as the stated mask, then the test fails.
     * 
     */
    @XmlEnumValue("bitwise and")
    BITWISE_AND("bitwise and"),

    /**
     * The 'bitwise or' operation is used to determine if a specific bit is not set. It returns true if performing a BITWISE OR with the binary representation of the stated entity against the binary representation of the actual value on the system results in a binary value that is equal to the binary representation of the stated entity. For example, assuming a datatype of 'int', if the actual integer value of the setting on your machine is 6 (same as 0110 in binary), then performing a 'bitwise or' with the stated integer 14 (1110) returns 14 (1110). Since the result is the same as the state mask, then the test returns true. If the actual value on your machine is 1 (0001), then the 'bitwise or' with the stated integer 14 (1110) returns 15 (1111). Since the result is not the same as the stated mask, then the test fails.
     * 
     */
    @XmlEnumValue("bitwise or")
    BITWISE_OR("bitwise or"),

    /**
     * The 'pattern match' operation allows an item to be tested against a regular expression. When used by an entity in an OVAL Object, the regular expression represents the unique set of matching items on the system.  OVAL supports a common subset of the regular expression character classes, operations, expressions and other lexical tokens defined within Perl 5's regular expression specification. For more information on the supported regular expression syntax in OVAL see: http://oval.mitre.org/language/about/re_support_5.6.html
     * 
     */
    @XmlEnumValue("pattern match")
    PATTERN_MATCH("pattern match"),

    /**
     * The 'subset of' operation returns true if the actual set on the system is a subset of the set defined by the stated entity.
     * 
     */
    @XmlEnumValue("subset of")
    SUBSET_OF("subset of"),

    /**
     * The 'superset of' operation returns true if the actual set on the system is a superset of the set defined by the stated entity.
     * 
     */
    @XmlEnumValue("superset of")
    SUPERSET_OF("superset of");
    private final String value;

    OperationEnumeration(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static OperationEnumeration fromValue(String v) {
        for (OperationEnumeration c: OperationEnumeration.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
