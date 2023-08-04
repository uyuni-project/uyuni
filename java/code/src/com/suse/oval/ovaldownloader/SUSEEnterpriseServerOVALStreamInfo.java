package com.suse.oval.ovaldownloader;

public class SUSEEnterpriseServerOVALStreamInfo extends OVALStreamInfo {
    private final String osVersion;

    public SUSEEnterpriseServerOVALStreamInfo(String osVersion) {
        this.osVersion = osVersion;
    }

    @Override
    String localFileName() {
        return "suse.linux.enterprise.server-" + osVersion;
    }

    @Override
    String remoteFileUrl() {
        return String.format("https://ftp.suse.com/pub/projects/security/oval/suse.linux.enterprise.server.%s-affected.xml.gz", osVersion);
    }

    @Override
    OVALCompressionMethod getCompressionMethod() {
        return OVALCompressionMethod.GZIP;
    }
}
