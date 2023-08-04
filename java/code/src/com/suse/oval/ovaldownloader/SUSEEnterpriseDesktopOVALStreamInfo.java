package com.suse.oval.ovaldownloader;

public class SUSEEnterpriseDesktopOVALStreamInfo extends OVALStreamInfo {
    private final String osVersion;

    public SUSEEnterpriseDesktopOVALStreamInfo(String osVersion) {
        this.osVersion = osVersion;
    }

    @Override
    String localFileName() {
        return "suse.linux.enterprise.desktop-" + osVersion;
    }

    @Override
    String remoteFileUrl() {
        return String.format("https://ftp.suse.com/pub/projects/security/oval/suse.linux.enterprise.desktop.%s-affected.xml.gz", osVersion);
    }

    @Override
    OVALCompressionMethod getCompressionMethod() {
        return OVALCompressionMethod.GZIP;
    }
}
