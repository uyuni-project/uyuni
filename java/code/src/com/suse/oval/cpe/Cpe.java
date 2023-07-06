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

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }

    public String toURI() {
        return "cpe/o:" + vendor + ":" + product + ":" + vendor + ":" + update;
    }

    public Cpe parse(String cpe) {
        return cpeParser.parse(cpe);
    }
}
