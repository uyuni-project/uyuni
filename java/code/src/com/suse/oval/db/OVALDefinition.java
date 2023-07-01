package com.suse.oval.db;


import com.redhat.rhn.domain.errata.Cve;
import com.suse.oval.OVALDefinitionSource;
import com.suse.oval.ovaltypes.CriteriaType;
import com.suse.oval.ovaltypes.DefinitionClassEnum;
import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "suseOVALDefinition")
@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonType.class)
})
@NamedQueries({
        @NamedQuery(name = "OVALDefinition.getVulnerabilityDefinitionByCve",
                query = "SELECT def FROM OVALDefinition def JOIN def.cve cve WHERE cve.name = :cve")
})
public class OVALDefinition {
    private String id;
    private DefinitionClassEnum defClass;
    private String title;
    private String description;
    private List<OVALReference> references;
    private List<OVALPlatform> affectedPlatforms;
    private Cve cve;
    private OVALDefinitionSource source;
    private CriteriaType criteriaTree;

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

    @OneToMany(mappedBy = "definition", cascade = CascadeType.ALL)
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

    @OneToOne
    @JoinColumn(name = "cve_id")
    public Cve getCve() {
        return cve;
    }

    public void setCve(Cve cve) {
        this.cve = cve;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "source")
    public OVALDefinitionSource getSource() {
        return source;
    }

    public void setSource(OVALDefinitionSource source) {
        this.source = source;
    }

    @Type(type = "json")
    @Column(name = "criteria_tree")
    public CriteriaType getCriteriaTree() {
        return criteriaTree;
    }

    public void setCriteriaTree(CriteriaType criteriaTree) {
        this.criteriaTree = criteriaTree;
    }
}
