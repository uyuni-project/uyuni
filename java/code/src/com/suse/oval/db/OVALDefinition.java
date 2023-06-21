package com.suse.oval.db;


import javax.persistence.*;
import java.util.List;

@Entity
public class OVALDefinition {
    private String id;
    private List<OVALReference> references;
    private List<OVALPlatform> affectedPlatforms;

    @Id
    @Column(name = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
            joinColumns = { @JoinColumn(name = "definition_id") },
            inverseJoinColumns = { @JoinColumn(name = "platform_id") })
    public List<OVALPlatform> getAffectedPlatforms() {
        return affectedPlatforms;
    }

    public void setAffectedPlatforms(List<OVALPlatform> affectedPlatforms) {
        this.affectedPlatforms = affectedPlatforms;
    }
}
