package com.suse.oval.ovaltypes;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


@XmlType(name = "ClassEnumeration", namespace = "http://oval.mitre.org/XMLSchema/oval-common-5")
@XmlEnum
public enum DefinitionClassEnum {

    /**
     * A patch definition details the machine state of whether a patch executable should be installed.
     * <p>
     * A definition of this class will evaluate to true when the specified patch is missing from the system.
     * Another way of thinking about this is that a patch definition is stating "the patch should be installed if ...". Note that word SHOULD is intended to mean more than just CAN the patch executable be installed. In other words, if a more recent patch is already installed then the specified patch might not need to be installed.
     */
    @XmlEnumValue("patch")
    PATCH("patch"),

    /**
     * A vulnerability definition describes the conditions under which a machine is vulnerable.
     * <p>
     * A definition of this class will evaluate to true when the system is found to be vulnerable with the stated issue.
     * Another way of thinking about this is that a vulnerability definition is stating "the system is vulnerable if ...".
     */
    @XmlEnumValue("vulnerability")
    VULNERABILITY("vulnerability");
    private final String value;

    DefinitionClassEnum(String v) {
        value = v;
    }

    public static DefinitionClassEnum fromValue(String v) {
        for (DefinitionClassEnum c : DefinitionClassEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    /**
     * Gets the string value
     *
     * @return the string value
     * */
    public String value() {
        return value;
    }

}
