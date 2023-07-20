package com.suse.oval.db;


import com.redhat.rhn.domain.errata.Cve;
import com.redhat.rhn.domain.rhnpackage.PackageEvrFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.frontend.dto.PackageListItem;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.manager.audit.CVEAuditManager;
import com.redhat.rhn.manager.rhnpackage.PackageManager;
import com.suse.oval.OVALDefinitionSource;
import com.suse.oval.TestEvaluator;
import com.suse.oval.ovaltypes.CriteriaType;
import com.suse.oval.ovaltypes.DefinitionClassEnum;
import com.suse.oval.vulnerablepkgextractor.AbstractVulnerablePackagesExtractor;
import com.suse.oval.vulnerablepkgextractor.ProductVulnerablePackages;
import com.suse.oval.vulnerablepkgextractor.SUSEVulnerablePackageExtractor;
import com.suse.oval.vulnerablepkgextractor.VulnerablePackage;
import com.vladmihalcea.hibernate.type.json.JsonType;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;
import java.util.*;

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
    private OVALDefinitionSource source;
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
    @Column(name = "source")
    public OVALDefinitionSource getSource() {
        return source;
    }

    public void setSource(OVALDefinitionSource source) {
        this.source = source;
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
        AbstractVulnerablePackagesExtractor vulnerablePackagesExtractor =
                new SUSEVulnerablePackageExtractor(this);

        Set<VulnerablePackage> productVulnerablePackages = new HashSet<>();

        List<ProductVulnerablePackages> productToVulnerablePackagesMappings = vulnerablePackagesExtractor.extract();

        for (ProductVulnerablePackages productToVulnerablePackagesMapping : productToVulnerablePackagesMappings) {
            if (Objects.equals(productToVulnerablePackagesMapping.getProductCpe(), productCpe)) {
                productVulnerablePackages.addAll(productToVulnerablePackagesMapping.getVulnerablePackages());
            }
        }

        return productVulnerablePackages;
    }

    /**
     * Evaluate the given {@code clientServer} vulnerability state against this definition's {@code criteriaTree}
     *
     * @param clientServer The client server to evaluate its vulnerability state
     * @param clientProductVulnerablePackages the set of vulnerable packages that made client product vulnerable to this vulnerability definition
     *
     * @return {@code True} of clientServer is in a vulnerable state and {@code False} if it's not.
     * Also returns {@code False} if definition doesn't have a criteria tree
     *
     * */
    public boolean evaluate(Server clientServer, Set<VulnerablePackage> clientProductVulnerablePackages) {
        if (criteriaTree == null) {
            return false;
        }

        // TODO: Only load packages that are vulnerable
        List<PackageListItem> allInstalledPackages = new ArrayList<>();
        for (VulnerablePackage vulnerablePackage : clientProductVulnerablePackages) {
            LOG.error("Client Product Package {}, {}", vulnerablePackage.getName(), vulnerablePackage.getFixVersion());
            allInstalledPackages.addAll(PackageManager.systemPackagesWithName(clientServer.getId(), vulnerablePackage.getName()));
        }

        allInstalledPackages.forEach(installed -> {
            installed.setEvr(PackageEvrFactory.lookupPackageEvrById(installed.getEvrId()).toUniversalEvrString());
            LOG.error("Installed Package {}, {}, {}", installed.getName(), installed.getEvr(), installed.getSummary());
        });

        TestEvaluator testEvaluator = new TestEvaluator(allInstalledPackages, clientServer.getPackageType());

        return criteriaTree.evaluate(testEvaluator);
    }
}
