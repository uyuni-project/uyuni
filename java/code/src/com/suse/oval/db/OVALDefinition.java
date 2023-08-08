package com.suse.oval.db;


import static java.util.stream.Collectors.groupingBy;

import com.redhat.rhn.domain.errata.Cve;
import com.redhat.rhn.domain.server.Server;

import com.suse.oval.OsFamily;
import com.suse.oval.SystemPackage;
import com.suse.oval.TestEvaluator;
import com.suse.oval.ovaltypes.CriteriaType;
import com.suse.oval.ovaltypes.DefinitionClassEnum;
import com.suse.oval.vulnerablepkgextractor.VulnerablePackage;

import com.vladmihalcea.hibernate.type.json.JsonType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "suseOVALDefinition")
@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonType.class)
})
@NamedQueries({
        @NamedQuery(name = "OVALDefinition.getVulnerabilityDefinitionByCve",
                query = "SELECT def FROM OVALDefinition def JOIN def.cve cve WHERE cve.name = :cve")
})
@DynamicUpdate
public class OVALDefinition {
    private static Logger LOG = LogManager.getLogger(OVALDefinition.class);
    private String id;
    private DefinitionClassEnum defClass;
    private String title;
    private String description;
    private List<OVALReference> references;
    private List<OVALPlatform> affectedPlatforms;
    private Cve cve;
    private OsFamily osFamily;
    private String osVersion;
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

    @OneToMany(mappedBy = "definition", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<OVALReference> getReferences() {
        return references;
    }

    public void setReferences(List<OVALReference> references) {
        this.references = references;
    }

    @ManyToMany(fetch = FetchType.LAZY)
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
    @Column(name = "os_family")
    public OsFamily getOsFamily() {
        return osFamily;
    }

    public void setOsFamily(OsFamily osFamily) {
        this.osFamily = osFamily;
    }

    @Column(name = "os_version")
    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    @Type(type = "json")
    @Column(name = "criteria_tree")
    public CriteriaType getCriteriaTree() {
        return criteriaTree;
    }

    public void setCriteriaTree(CriteriaType criteriaTree) {
        this.criteriaTree = criteriaTree;
    }

    /**
     * Extract the list of vulnerable packages from the OVAL definition that matches the given product cpe
     *
     * @param productCpe the cpe of the product to get vulnerable packages for
     * @return The list of vulnerable packages that made systems of {@code cpe} vulnerable to this vulnerability definition
     */
    @Transient
    public Set<VulnerablePackage> extractVulnerablePackages(String productCpe) {
/*
        Optional<OsFamily> osFamilyOpt = Cpe.parse(productCpe).toOsFamily();

        if (osFamilyOpt.isEmpty()) {
            throw new IllegalArgumentException("Product CPE doesn't belong to any of the supported products: " + productCpe);
        }

        AbstractVulnerablePackagesExtractor vulnerablePackagesExtractor =
                VulnerablePackagesExtractors.create(this, osFamilyOpt.get());

        Set<VulnerablePackage> productVulnerablePackages = new HashSet<>();

        List<ProductVulnerablePackages> productToVulnerablePackagesMappings = vulnerablePackagesExtractor.extract();

        for (ProductVulnerablePackages productToVulnerablePackagesMapping : productToVulnerablePackagesMappings) {
            if (Objects.equals(productToVulnerablePackagesMapping.getProduct(), productCpe)) {
                productVulnerablePackages.addAll(productToVulnerablePackagesMapping.getVulnerablePackages());
            }
        }
*/

        return Collections.emptySet();
    }

    /**
     * Evaluate the given {@code clientServer} vulnerability state against this definition's {@code criteriaTree}
     *
     * @param clientServer The client server to evaluate its vulnerability state
     *
     * @return {@code True} of clientServer is in a vulnerable state and {@code False} if it's not.
     * Also returns {@code False} if definition doesn't have a criteria tree
     * */
    public boolean evaluate(Server clientServer, List<SystemPackage> allInstalledPackages) {
        if (criteriaTree == null) {
            return false;
        }

        Map<String, List<SystemPackage>> allInstalledPackagesByName = allInstalledPackages
                .stream().collect(groupingBy(SystemPackage::getName));

        TestEvaluator testEvaluator = new TestEvaluator(allInstalledPackagesByName, clientServer.getPackageType());

        return criteriaTree.evaluate(testEvaluator);
    }
}
