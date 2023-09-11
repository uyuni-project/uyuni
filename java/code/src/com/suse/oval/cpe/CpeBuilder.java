package com.suse.oval.cpe;

public final class CpeBuilder {
    Cpe cpe = new Cpe();

    public CpeBuilder withVendor(String vendor) {
        cpe.setVendor(vendor);
        return this;
    }

    public CpeBuilder withProduct(String product) {
        cpe.setProduct(product);
        return this;
    }

    public CpeBuilder withVersion(String version) {
        cpe.setVersion(version);
        return this;
    }

    public CpeBuilder withUpdate(String update) {
        cpe.setUpdate(update);
        return this;
    }

    public Cpe build() {
        return cpe;
    }
}
