package com.redhat.rhn.domain.image;

import com.redhat.rhn.domain.org.Org;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class OSImageStoreUtils {

    private static String osImageStorePath = "/srv/www/os-images";
    private static String osImageWWWDirectory = "os-images";

    public static String getOSImageStorePathForOrg(Org org) {
        return osImageStorePath + org.getId() + "/";
    }

    public static String getOsImageStorePath() {
        return osImageStorePath;
    }

    public static String getOSImageStoreURI() {
        String suseManagerHostname = "<suse-manager>";
        try {
            suseManagerHostname = InetAddress.getLocalHost().getCanonicalHostName();
        }
        catch (UnknownHostException ignored) { }

        return "https://" + suseManagerHostname + "/" + osImageWWWDirectory + "/";
    }

    public static String getOSImageStoreURIForOrg(Org org) {
        return getOSImageStoreURI() + org.getId() + "/";
    }
}
