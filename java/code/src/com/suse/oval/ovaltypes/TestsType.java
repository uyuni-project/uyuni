package com.suse.oval.ovaltypes;

import com.suse.oval.ovaltypes.linux.DpkginfoTest;
import com.suse.oval.ovaltypes.linux.RpminfoTest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * The TestsType complex type is a container for one or more test child elements.
 * <p>
 * Each test element describes a single OVAL Test. Please refer to the description of the TestType for more information
 * about an individual test.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TestsType", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
public class TestsType {

    @XmlElements({
            @XmlElement(name = "rpminfo_test", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5#linux", type = RpminfoTest.class),
            @XmlElement(name = "dpkginfo_test", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5#linux", type = DpkginfoTest.class),
            @XmlElement(name = "test", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5", type = TestType.class)
    })
    protected List<TestType> tests;

    /**
     * Gets the value of the contained tests.
     */
    public List<TestType> getTests() {
        if (tests == null) {
            tests = new ArrayList<>();
        }
        return this.tests;
    }

}
