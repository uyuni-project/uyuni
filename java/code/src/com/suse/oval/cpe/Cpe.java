package com.suse.oval.cpe;

import static com.suse.oval.OsFamily.*;

import com.suse.oval.OsFamily;

import java.util.Objects;
import java.util.Optional;

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

    public static Cpe parse(String cpe) {
        return cpeParser.parse(cpe);
    }

    public String asString() {
        String cpe = "cpe:/o:" + vendor + ":" + product + ":" + version + ":" + update;

        // Removing trailing colons ':'
        return cpe.replaceAll(":*$", "");
    }

    public Optional<OsFamily> toOsFamily() {
        if ("redhat".equals(vendor) && "enterprise_linux".equals(product)) {
            return Optional.of(REDHAT_ENTERPRISE_LINUX);
        }
        else if("suse".equals(vendor) && "sles".equals(product)) {
            return Optional.of(SUSE_LINUX_ENTERPRISE_SERVER);
        }
        else if ("suse".equals(vendor) && "sled".equals(product)) {
            return Optional.of(SUSE_LINUX_ENTERPRISE_DESKTOP);
        }
        else if ("opensuse".equals(vendor) && "leap".equals(product)) {
            return Optional.of(openSUSE_LEAP);
        }
        return Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Cpe cpe = (Cpe) o;
        return Objects.equals(vendor, cpe.vendor) && Objects.equals(product, cpe.product) &&
                Objects.equals(version, cpe.version) && Objects.equals(update, cpe.update);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vendor, product, version, update);
    }
}
