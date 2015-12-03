package com.suse.manager.webui.utils.gson;

public class PackageStateDto {

    private final String name;
    private final String evr;
    private final String arch;
    private final Integer packageStateId;
    private final Integer versionConstraintId;

    /**
     * @param nameIn the package name
     * @param evrIn the package evr
     * @param archIn the package arch
     * @param packageStateIdIn the state type id
     * @param versionConstraintIdIn the version constraint id
     */
    public PackageStateDto(String nameIn, String evrIn, String archIn,
            Integer packageStateIdIn, Integer versionConstraintIdIn) {
        this.name = nameIn;
        this.evr = evrIn;
        this.arch = archIn;
        this.packageStateId = packageStateIdIn;
        this.versionConstraintId = versionConstraintIdIn;
    }

    public String getName() {
        return name;
    }

    public String getEvr() {
        return evr;
    }

    public String getArch() {
        return arch;
    }

    /**
     * @return the state type id
     */
    public int getPackageStateId() {
        return packageStateId;
    }

    public int getVersionConstraintId() {
        return versionConstraintId;
    }
}
