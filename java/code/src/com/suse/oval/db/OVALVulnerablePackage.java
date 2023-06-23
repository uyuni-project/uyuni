package com.suse.oval.db;

import javax.persistence.*;

@Entity
@Table(name = "suseOVALVulnerablePackage")
public class OVALVulnerablePackage {
    private Long id;
    private String name;
    private String fixVersion;

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "suse_oval_vulnerable_pkg_id_seq")
    public Long getId() {
        return id;
    }

    @Column(name = "name", nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "fix_version")
    public String getFixVersion() {
        return fixVersion;
    }

    public void setFixVersion(String fixVersion) {
        this.fixVersion = fixVersion;
    }
}
