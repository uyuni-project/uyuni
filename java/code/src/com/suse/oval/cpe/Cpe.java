package com.suse.oval.cpe;

public class Cpe {
    private static final SimpleCpeParser cpeParser = new SimpleCpeParser();

    private String vendor;
    private String product;
    private String version;
    private String update;

    public Cpe() {
        vendor = "";
        product = "";
        version = "";
        update = "";
    }

    public String getVendor() {
        return vendor;
    }

    void setVendor(String vendor) {
        if (vendor != null) {
            this.vendor = vendor;
        }
    }

    public String getProduct() {
        return product;
    }

    void setProduct(String product) {
        if (product != null) {
            this.product = product;
        }
    }

    public String getVersion() {
        return version;
    }

    void setVersion(String version) {
        if (version != null) {
            this.version = version;
        }
    }

    public String getUpdate() {
        return update;
    }

    void setUpdate(String update) {
        if (update != null) {
            this.update = update;
        }
    }

    public Cpe parse(String cpe) {
        return cpeParser.parse(cpe);
    }

    public String asString() {
        String cpe = "cpe:/o:" + vendor + ":" + product + ":" + version + ":" + update;
        // Removing trailing colons ':'
        return cpe.replaceAll(":*$", "");
    }
}
