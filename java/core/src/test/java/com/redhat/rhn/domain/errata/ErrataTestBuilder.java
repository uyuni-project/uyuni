/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.errata;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageTest;
import com.redhat.rhn.testing.TestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Builder for creating test Errata instances with sensible defaults.
 * Allows flexible customization while maintaining current default behavior.
 */
public class ErrataTestBuilder {

    private static final AtomicLong ADVISORY_SEQ = new AtomicLong(1);

    private Errata errata;
    private Long orgId;
    private Org org;
    private String advisory;
    private String advisoryType = ErrataFactory.ERRATA_TYPE_BUG;
    private String product = "Red Hat Linux";
    private String description = "Test desc ..";
    private String synopsis = "Test synopsis";
    private String solution = "Test solution";
    private String notes = "Test notes for test errata";
    private String rights = "Copyright for test errata";
    private String topic = "test topic";
    private String refersTo = "rhn unit tests";
    private Date updateDate = new Date();
    private Date issueDate = new Date();
    private String advisoryName;
    private Long advisoryRel = 2L;
    private String errataFrom = "maint-coord@suse.de";
    private Boolean locallyModified = Boolean.FALSE;
    private String keyword = "keyword";
    private boolean addDummyPackage = false;
    private Integer severityId = 1;
    private List<Package> packages = new ArrayList<>();
    private List<Channel> channels = new ArrayList<>();
    private List<Cve> cves = new ArrayList<>();
    private List<Bug> bugs = new ArrayList<>();

    public ErrataTestBuilder() {
        this.errata = new Errata();
    }

    public ErrataTestBuilder orgId(Long orgIdIn) {
        this.orgId = orgIdIn;
        return this;
    }

    public ErrataTestBuilder advisory(String advisoryIn) {
        this.advisory = advisoryIn;
        return this;
    }

    public ErrataTestBuilder advisoryType(String typeIn) {
        this.advisoryType = typeIn;
        return this;
    }

    public ErrataTestBuilder product(String productIn) {
        this.product = productIn;
        return this;
    }

    public ErrataTestBuilder description(String descIn) {
        this.description = descIn;
        return this;
    }

    public ErrataTestBuilder synopsis(String synopsisIn) {
        this.synopsis = synopsisIn;
        return this;
    }

    public ErrataTestBuilder solution(String solutionIn) {
        this.solution = solutionIn;
        return this;
    }

    public ErrataTestBuilder notes(String notesIn) {
        this.notes = notesIn;
        return this;
    }

    public ErrataTestBuilder rights(String rightsIn) {
        this.rights = rightsIn;
        return this;
    }

    public ErrataTestBuilder topic(String topicIn) {
        this.topic = topicIn;
        return this;
    }

    public ErrataTestBuilder refersTo(String refersToIn) {
        this.refersTo = refersToIn;
        return this;
    }

    public ErrataTestBuilder updateDate(Date dateIn) {
        this.updateDate = dateIn;
        return this;
    }

    public ErrataTestBuilder issueDate(Date dateIn) {
        this.issueDate = dateIn;
        return this;
    }

    public ErrataTestBuilder advisoryRel(Long relIn) {
        this.advisoryRel = relIn;
        return this;
    }

    public ErrataTestBuilder errataFrom(String fromIn) {
        this.errataFrom = fromIn;
        return this;
    }

    public ErrataTestBuilder locallyModified(Boolean modifiedIn) {
        this.locallyModified = modifiedIn;
        return this;
    }

    public ErrataTestBuilder keyword(String keywordIn) {
        this.keyword = keywordIn;
        return this;
    }

    public ErrataTestBuilder severityId(Integer severityIdIn) {
        this.severityId = severityIdIn;
        return this;
    }

    public ErrataTestBuilder addDummyPackage(boolean addDummyPackageIn) {
        this.addDummyPackage = addDummyPackageIn;
        return this;
    }

    /**
     * Add a package to the errata
     * @param pkg the package to add
     * @return this builder
     */
    public ErrataTestBuilder addPackage(Package pkg) {
        this.packages.add(pkg);
        return this;
    }

    /**
     * Add multiple packages to the errata
     * @param pkgs the packages to add
     * @return this builder
     */
    public ErrataTestBuilder addPackages(Package... pkgs) {
        this.packages.addAll(Arrays.asList(pkgs));
        return this;
    }

    /**
     * Add a channel to the errata (will use channel.addErrata)
     * @param channel the channel to add
     * @return this builder
     */
    public ErrataTestBuilder addChannel(Channel channel) {
        this.channels.add(channel);
        return this;
    }

    /**
     * Add multiple channels to the errata
     * @param channelsIn the channels to add
     * @return this builder
     */
    public ErrataTestBuilder addChannels(Channel... channelsIn) {
        this.channels.addAll(Arrays.asList(channelsIn));
        return this;
    }

    /**
     * Add a CVE to the errata
     * @param cve the CVE to add
     * @return this builder
     */
    public ErrataTestBuilder addCve(Cve cve) {
        this.cves.add(cve);
        return this;
    }

    /**
     * Add multiple CVEs to the errata
     * @param cvesIn the CVEs to add
     * @return this builder
     */
    public ErrataTestBuilder addCves(Cve... cvesIn) {
        this.cves.addAll(Arrays.asList(cvesIn));
        return this;
    }

    /**
     * Add a bug to the errata
     * @param bug the bug to add
     * @return this builder
     */
    public ErrataTestBuilder addBug(Bug bug) {
        this.bugs.add(bug);
        return this;
    }

    /**
     * Add multiple bugs to the errata
     * @param bugsIn the bugs to add
     * @return this builder
     */
    public ErrataTestBuilder addBugs(Bug... bugsIn) {
        this.bugs.addAll(Arrays.asList(bugsIn));
        return this;
    }

    /**
     * Build the Errata instance (not yet persisted)
     * @return the configured Errata
     */
    public Errata build() {
        // Generate advisory name if not provided
        String name = (advisory != null) ? advisory : "JAVA-Test-" + ADVISORY_SEQ.getAndIncrement();
        if (advisoryName == null) {
            advisoryName = name;
        }

        // Lookup org if needed
        if (orgId != null && org == null) {
            org = OrgFactory.lookupById(orgId);
        }

        // Set basic fields
        if (org != null) {
            errata.setOrg(org);
        }
        errata.setAdvisory(name);
        errata.setAdvisoryType(advisoryType);
        errata.setProduct(product);
        errata.setDescription(description);
        errata.setSynopsis(synopsis);
        errata.setSolution(solution);
        errata.setNotes(notes);
        errata.setRights(rights);
        errata.setTopic(topic);
        errata.setRefersTo(refersTo);
        errata.setUpdateDate(updateDate);
        errata.setIssueDate(issueDate);
        errata.setAdvisoryName(advisoryName);
        errata.setAdvisoryRel(advisoryRel);
        errata.setErrataFrom(errataFrom);
        errata.setLocallyModified(locallyModified);

        // Add keyword
        if (keyword != null) {
            errata.addKeyword(keyword);
        }

        // Add package and file if requested
        if (addDummyPackage) {
            Package testPackage = PackageTest.createTestPackage(org);

            if (addDummyPackage) {
                errata.addPackage(testPackage);
            }

            Set<Package> errataFilePackages = new HashSet<>();
            errataFilePackages.add(testPackage);
            ErrataFile ef = ErrataFactory.createErrataFile(
                ErrataFactory.lookupErrataFileType("RPM"),
                "SOME FAKE CHECKSUM: 123456789012",
                "test errata file" + TestUtils.randomString(),
                errataFilePackages
            );
            errata.addFile(ef);
        }

        // Set severity
        if (severityId != null) {
            errata.setSeverity(Severity.getById(severityId));
        }

        // Add additional packages
        for (Package pkg : packages) {
            errata.addPackage(pkg);
        }

        // Add channels (using channel.addErrata to maintain proper ownership)
        for (Channel channel : channels) {
            channel.addErrata(errata);
        }

        // Add CVEs
        for (Cve cve : cves) {
            errata.getCves().add(cve);
        }

        // Add bugs
        for (Bug bug : bugs) {
            errata.addBug(bug);
        }

        return errata;
    }

    /**
     * Build and persist the Errata instance
     * @return the persisted Errata
     */
    public Errata buildAndSave() {
        Errata e = build();
        return ErrataFactory.save(e);
    }
}
