package com.suse.oval.ovaldownloader;

public class RedHatOVALStreamInfo extends OVALStreamInfo {
    private final String osVersion;

    public RedHatOVALStreamInfo(String osVersion) {
        this.osVersion = osVersion;
    }

    @Override
    String localFileName() {
        return String.format("rhel-%s", osVersion);
    }

    @Override
    String remoteFileUrl() {
        return String.format(
                "https://www.redhat.com/security/data/oval/v2/RHEL%s/rhel-%s-including-unpatched.oval.xml.bz2",
                osVersion, osVersion);
    }

    @Override
    OVALCompressionMethod getCompressionMethod() {
        return OVALCompressionMethod.BZIP2;
    }
}
