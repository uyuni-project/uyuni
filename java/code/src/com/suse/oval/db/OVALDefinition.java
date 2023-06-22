package com.suse.oval.db;


import com.redhat.rhn.domain.errata.Cve;
import com.suse.oval.ovaltypes.DefinitionClassEnum;

import javax.persistence.*;
import java.util.List;

@Entity
public class OVALDefinition {
    private String id;
    private DefinitionClassEnum defClass;
    private String title;
    private String description;
    private List<OVALReference> references;
    private List<OVALPlatform> affectedPlatforms;
    private List<Cve> cves;

    @Id
    @Column(name = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "class")
    public DefinitionClassEnum getDefClass() {
        return defClass;
    }

    public void setDefClass(DefinitionClassEnum defClass) {
        this.defClass = defClass;
    }

    @Column(name = "title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    @Column(name = "description", length = 10_000)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @OneToMany(mappedBy = "definition")
    public List<OVALReference> getReferences() {
        return references;
    }

    public void setReferences(List<OVALReference> references) {
        this.references = references;
    }

    @ManyToMany
    @JoinTable(name = "suseOVALDefinitionAffectedPlatform",
            joinColumns = {@JoinColumn(name = "definition_id")},
            inverseJoinColumns = {@JoinColumn(name = "platform_id")})
    public List<OVALPlatform> getAffectedPlatforms() {
        return affectedPlatforms;
    }

    public void setAffectedPlatforms(List<OVALPlatform> affectedPlatforms) {
        this.affectedPlatforms = affectedPlatforms;
    }

    @SuppressWarnings("JpaAttributeTypeInspection")
    @ManyToMany
    @JoinTable(name = "suseOVALDefinitionCve",
            joinColumns = {@JoinColumn(name = "definition_id")},
            inverseJoinColumns = {@JoinColumn(name = "cve_id")})
    public List<Cve> getCves() {
        return cves;
    }

    public void setCves(List<Cve> cves) {
        this.cves = cves;
    }
}
