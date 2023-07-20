package com.suse.oval;

public enum OVALDefinitionSource {
    openSUSE("openSUSE", "opensuse", "opensuse"),
    openSUSE_LEAP("openSUSE Leap", "leap", "opensuse"),
    SUSE_LINUX_ENTERPRISE_SERVER("SUSE Linux Enterprise Server", "sles", "suse"),
    SUSE_LINUX_ENTERPRISE_DESKTOP("SUSE Linux Enterprise Desktop", "sled", "suse"),
    REDHAT_ENTERPRISE_LINUX("Red Hat Enterprise Linux", "enterprise_linux", "redhat"),
    UBUNTU("Ubuntu", "ubuntu", "canonical"),
    DEBIAN("Debian", "debian", "debian");

    private final String vendor;
    private final String fullname;
    // Should consist of all lower case characters
    private final String shortname;


    OVALDefinitionSource(String fullname, String shortname, String vendor) {
        this.fullname = fullname;
        this.shortname = shortname;
        this.vendor = vendor;
    }
    OVALDefinitionSource(String fullname, String vendor) {
        this(fullname, fullname.toLowerCase(), vendor);
    }


    public String fullname() {
        return fullname;
    }

    public String shortname() {
        return shortname;
    }

    public String vendor() {
        return vendor;
    }
}
