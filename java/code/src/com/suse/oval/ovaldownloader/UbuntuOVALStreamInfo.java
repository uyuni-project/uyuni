package com.suse.oval.ovaldownloader;

import java.util.Map;
import java.util.Objects;

public class UbuntuOVALStreamInfo extends OVALStreamInfo {
    private static final Map<String, String> VERSION_TO_CODENAME_MAPPING =
            Map.of("18.04", "bionic",
                    "20.04", "focal",
                    "22.04", "jammy");
    private final String osVersion;
    private final String codename;

    public UbuntuOVALStreamInfo(String osVersion) {
        this.osVersion = Objects.requireNonNull(osVersion);
        this.codename = VERSION_TO_CODENAME_MAPPING.get(osVersion);
    }

    @Override
    String localFileName() {
        return String.format("ubuntu-%s-%s", osVersion, codename);
    }

    @Override
    String remoteFileUrl() {
        return String.format("https://security-metadata.canonical.com/oval/com.ubuntu.%s.cve.oval.xml.bz2", codename);
    }

    @Override
    OVALCompressionMethod getCompressionMethod() {
        return OVALCompressionMethod.BZIP2;
    }
}
