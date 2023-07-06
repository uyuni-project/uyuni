package com.suse.oval.cpe;

/**
 * A simple and minimal implementation of a CPE parser to parse CPE URI bindings included in OVAL files.
 */
public class SimpleCpeParser {


    /**
     * <p>
     * Example 1:
     * <br>
     * URI: cpe:/a:microsoft:internet_explorer:8.%02:sp%01
     * <br>Unbinds to this WFN:
     * <p>
     * wfn:[part="a",vendor="microsoft",product="internet_explorer",
     * version="8\.*",update="sp?",edition=ANY,language=ANY]
     *
     * </p>
     */
    public Cpe parse(String cpeURI) {
        if (!cpeURI.startsWith("cpe:/o:")) {
            throw new IllegalArgumentException("CPE is expected to be in URI format and for operating systems");
        }

        String[] parts = cpeURI.split(":");

        if (parts.length < 3) {
            throw new IllegalArgumentException("CPE is expected to at least have vendor, product and version");
        }

        Cpe cpe = new Cpe();
        cpe.setVendor(parts[0]);
        cpe.setProduct(parts[1]);
        cpe.setVersion(parts[2]);

        if (parts.length > 3) {
            cpe.setUpdate(parts[3]);
        }

        return cpe;
    }
}
