package com.suse.manager.reactor.utils.test;

import com.suse.manager.reactor.utils.RhelUtils;
import junit.framework.TestCase;

import java.util.Optional;

/**
 * Test for {@link com.suse.manager.reactor.utils.RhelUtils}
 */
public class RhelUtilsTest extends TestCase {

    private static String PLAIN_REDHAT_RELEASE =
            "Red Hat Enterprise Linux Server release 6.8 (Santiago)";
    private static String RES_REDHAT_RELEASE =
            "Red Hat Enterprise Linux Server release 6.8 (Santiago)\n" +
            "# This is a \"SLES Expanded Support platform release 6.8\"\n" +
            "# The above \"Red Hat Enterprise Linux Server\" string is only used to \n" +
            "# keep software compatibility.";
    private static String CENTOS_REDHAT_RELEASE =
            "CentOS Linux release 7.2.1511 (Core)";

    public void testParseReleaseFileRedHat() {
        Optional<RhelUtils.ReleaseFile> os = RhelUtils.parseReleaseFile(PLAIN_REDHAT_RELEASE);
        assertTrue(os.isPresent());
        assertEquals("RedHatEnterpriseServer", os.get().getName());
        assertEquals("6", os.get().getMajorVersion());
        assertEquals("8", os.get().getMinorVersion());
        assertEquals("Santiago", os.get().getRelease());
    }

    public void testParseReleaseFileRES() {
        Optional<RhelUtils.ReleaseFile> os = RhelUtils.parseReleaseFile(RES_REDHAT_RELEASE);
        assertTrue(os.isPresent());
        assertEquals("RedHatEnterpriseServer", os.get().getName());
        assertEquals("6", os.get().getMajorVersion());
        assertEquals("8", os.get().getMinorVersion());
        assertEquals("Santiago", os.get().getRelease());
    }

    public void testParseReleaseFileCentos() {
        Optional<RhelUtils.ReleaseFile> os = RhelUtils.parseReleaseFile(CENTOS_REDHAT_RELEASE);
        assertTrue(os.isPresent());
        assertEquals("CentOS", os.get().getName());
        assertEquals("7", os.get().getMajorVersion());
        assertEquals("2.1511", os.get().getMinorVersion());
        assertEquals("Core", os.get().getRelease());
    }

    public void testParseReleaseFileNonMatching() {
        Optional<RhelUtils.ReleaseFile> os = RhelUtils.parseReleaseFile("GarbageOS 1.0 (Trash can)");
        assertFalse(os.isPresent());
    }

}
