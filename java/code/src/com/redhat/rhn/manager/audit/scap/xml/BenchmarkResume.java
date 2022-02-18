/*
 * Copyright (c) 2017 SUSE LLC
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
package com.redhat.rhn.manager.audit.scap.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Bean used to unmarshall an intermediary SCAP report.
 */
@Root(name = "benchmark-resume", strict = false)
public class BenchmarkResume {

    @Attribute
    private String id;

    @Attribute
    private String version;

    @Element(name = "profile")
    private Profile profile;

    @Element(name = "TestResult")
    private TestResult testResult;

    /**
     * @return id to get
     */
    public String getId() {
        return id;
    }

    /**
     * @param idIn to set
     */
    public void setId(String idIn) {
        this.id = idIn;
    }

    /**
     * @return version to get
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param versionIn to set
     */
    public void setVersion(String versionIn) {
        this.version = versionIn;
    }

    /**
     * @return profile to get
     */
    public Profile getProfile() {
        return profile;
    }

    /**
     * @param profileIn to set
     */
    public void setProfile(Profile profileIn) {
        this.profile = profileIn;
    }

    /**
     * @return testResult to get
     */
    public TestResult getTestResult() {
        return testResult;
    }

    /**
     * @param testResultIn to set
     */
    public void setTestResult(TestResult testResultIn) {
        this.testResult = testResultIn;
    }
}

