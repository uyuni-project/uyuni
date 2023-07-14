package com.suse.oval;

import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.frontend.dto.IdComboDto;

public class SystemPackage extends IdComboDto {
    private Long packageId;
    private Long id;
    private String name;
    private String evr;
    private String arch;
    private String epoch;
    private String version;
    private String release;
    private String type;

    public SystemPackage() {
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
     *
     * @return Returns the packageId
     */
    public Long getPackageId() {
        return packageId;
    }
    /**
     *
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
