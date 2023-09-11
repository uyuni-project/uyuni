package com.suse.oval.vulnerablepkgextractor;

import com.suse.utils.Opt;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class VulnerablePackage {
    private String name;
    private String fixVersion;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Optional<String> getFixVersion() {
        if (StringUtils.isEmpty(fixVersion)) {
            return Optional.empty();
        }
        return Optional.ofNullable(fixVersion);
    }

    public void setFixVersion(String fixedVersion) {
        this.fixVersion = fixedVersion;
    }

    @Override
    public String toString() {
        return name + "-" + fixVersion;
    }
}
