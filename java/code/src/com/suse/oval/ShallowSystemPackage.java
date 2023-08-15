package com.suse.oval;

import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageType;
import com.redhat.rhn.frontend.dto.IdComboDto;

public class ShallowSystemPackage extends IdComboDto {
    private Long packageId;
    private Long id;
    private String name;
    private String evr;
    private String arch;
    private String epoch;
    private String version;
    private String release;
    private String type;

    public ShallowSystemPackage() {
    }

    public ShallowSystemPackage(String name, PackageType type, String evr, String arch) {
        this.name = name;
        this.type = type.getDbString();
        this.arch = arch;

        PackageEvr packageEvr = PackageEvr.parsePackageEvr(type, evr);
        this.epoch = packageEvr.getEpoch();
        this.version = packageEvr.getVersion();
        this.release = packageEvr.getRelease();
    }

    public ShallowSystemPackage(String name, PackageType type, String evr) {
        this(name, type, evr, "noarch");
    }

    public ShallowSystemPackage(String name, String evr) {
        this(name, PackageType.RPM, evr);
    }
    public ShallowSystemPackage(String name, String evr, String arch) {
        this(name, PackageType.RPM, evr, arch);
    }


    /**
     * @return Returns the Id.
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * @param idIn The Id to set.
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEvr() {
        return evr;
    }

    public void setEvr(String evr) {
        this.evr = evr;
    }

    /**
     * @return Returns the packageId
     */
    public Long getPackageId() {
        return packageId;
    }

    /**
     * @param packageIdIn The packageId to set
     */
    public void setPackageId(Long packageIdIn) {
        packageId = packageIdIn;
    }

    public String getArch() {
        return arch;
    }

    public void setArch(String arch) {
        this.arch = arch;
    }

    public String getEpoch() {
        return epoch;
    }

    public void setEpoch(String epoch) {
        this.epoch = epoch;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public PackageEvr getPackageEVR() {
        return new PackageEvr(epoch, version, release, type);
    }
}
