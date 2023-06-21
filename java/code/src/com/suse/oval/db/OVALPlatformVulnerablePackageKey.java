package com.suse.oval.db;

import com.redhat.rhn.domain.errata.Cve;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

public class OVALPlatformVulnerablePackageKey implements Serializable {
    private OVALPlatform platform;
    private Cve cve;
    private OVALVulnerablePackage vulnerablePackage;

    public OVALPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(OVALPlatform platform) {
        this.platform = platform;
    }

    public Cve getCve() {
        return cve;
    }

    public void setCve(Cve cve) {
        this.cve = cve;
    }

    public OVALVulnerablePackage getVulnerablePackage() {
        return vulnerablePackage;
    }

    public void setVulnerablePackage(OVALVulnerablePackage vulnerablePackage) {
        this.vulnerablePackage = vulnerablePackage;
    }

/*    *//**
     * {@inheritDoc}
     *//*
    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder()
                .append(platformId)
                .append(cveId)
                .append(vulnerablePkgId);
        return builder.toHashCode();

    }

    *//**
     * {@inheritDoc}
     *//*
    @Override
    public boolean equals(Object other) {
        if (other instanceof OVALPlatformVulnerablePackageKey) {
            OVALPlatformVulnerablePackageKey otherKey = (OVALPlatformVulnerablePackageKey) other;
            return new EqualsBuilder()
                    .append(this.getPlatformId(), otherKey.getPlatformId())
                    .append(this.getCveId(), otherKey.getCveId())
                    .append(this.getVulnerablePkgId(), otherKey.getVulnerablePkgId())
                    .isEquals();
        } else {
            return false;
        }
    }*/
}
