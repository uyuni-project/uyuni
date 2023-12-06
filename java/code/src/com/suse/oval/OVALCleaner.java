/*
 * Copyright (c) 2023 SUSE LLC
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

package com.suse.oval;


import com.suse.oval.ovaltypes.Advisory;
import com.suse.oval.ovaltypes.AdvisoryCveType;
import com.suse.oval.ovaltypes.BaseCriteria;
import com.suse.oval.ovaltypes.CriteriaType;
import com.suse.oval.ovaltypes.CriterionType;
import com.suse.oval.ovaltypes.DefinitionClassEnum;
import com.suse.oval.ovaltypes.DefinitionType;
import com.suse.oval.ovaltypes.ObjectType;
import com.suse.oval.ovaltypes.OvalRootType;
import com.suse.oval.ovaltypes.StateType;
import com.suse.oval.ovaltypes.TestType;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is responsible for cleaning OVAL resources and filling up missing data. It acts as an adapter that takes
 * OVAL data from multiple sources and make changes to it to have a more predictable format.
 */
public class OVALCleaner {
    private OVALCleaner() {
    }

    /**
     * Cleanup the given {@code root} based on {@code osFamily} and {@code osVersion}
     *
     * @param root the OVAL root to clean up
     * @param osFamily the osFamily of the OVAL
     * @param osVersion the osVersion of the OVAL
     * */
    public static void cleanup(OvalRootType root, OsFamily osFamily, String osVersion) {
        root.setOsFamily(osFamily);
        root.setOsVersion(osVersion);

        if (osFamily == OsFamily.REDHAT_ENTERPRISE_LINUX) {
            root.getDefinitions().removeIf(def -> def.getId().contains("unaffected"));
        }

        if (osFamily == OsFamily.DEBIAN || osFamily == OsFamily.SUSE_LINUX_ENTERPRISE_SERVER ||
                osFamily == OsFamily.SUSE_LINUX_ENTERPRISE_DESKTOP || osFamily == OsFamily.openSUSE_LEAP) {
            // For the above OS families, we only need OVAL vulnerability definitions
            root.getDefinitions().removeIf(def -> def.getDefinitionClass() != DefinitionClassEnum.VULNERABILITY);
        }

        // Although it's rare, but it's possible to get null criteria trees.
        root.getDefinitions().removeIf(def -> def.getCriteria() == null);

        root.getDefinitions().forEach(definition -> doCleanupDefinition(definition, osFamily, osVersion));
        root.getTests().forEach(test -> doCleanupTest(test, osFamily, osVersion));
        root.getStates().forEach(state -> doCleanupState(state, osFamily, osVersion));
        root.getObjects().forEach(object -> doCleanupObject(object, osFamily, osVersion));
    }

    private static void doCleanupDefinition(DefinitionType definition, OsFamily osFamily, String osVersion) {
        fillCves(definition, osFamily);
        fillOsFamily(definition, osFamily);
        fillOsVersion(definition, osVersion);

        if (osFamily == OsFamily.DEBIAN) {
            convertDebianTestRefs(definition.getCriteria(), osVersion);
        }
    }

    private static void fillCves(DefinitionType definition, OsFamily osFamily) {
        switch (osFamily) {
            case REDHAT_ENTERPRISE_LINUX:
            case openSUSE_LEAP:
            case SUSE_LINUX_ENTERPRISE_SERVER:
            case SUSE_LINUX_ENTERPRISE_DESKTOP:
                List<String> cves =
                        definition.getMetadata().getAdvisory().map(Advisory::getCveList)
                                .orElse(Collections.emptyList())
                                .stream().map(AdvisoryCveType::getCve).collect(Collectors.toList());
                definition.setCves(cves);
                break;
            case DEBIAN:
                definition.setSingleCve(definition.getMetadata().getTitle().split("\\s+")[0]);
                break;
            default:
                throw new NotImplementedException("Cannot extract cve from '" + osFamily + "' OVAL definitions");
        }

        List<String> cleanCves = definition.getCves().stream().map(OVALCleaner::removeWhitespaceChars)
                .collect(Collectors.toList());

        definition.setCves(cleanCves);
    }

    private static String removeWhitespaceChars(String s) {
        return StringUtils.deleteWhitespace(s);
    }

    private static void fillOsFamily(DefinitionType definition, OsFamily osFamily) {
        definition.setOsFamily(osFamily);
    }

    private static void fillOsVersion(DefinitionType definition, String osVersion) {
        definition.setOsVersion(osVersion);
    }

    private static void doCleanupTest(TestType test, OsFamily osFamily, String osVersion) {
        if (osFamily == OsFamily.DEBIAN) {
            test.setId(convertDebianId(test.getId(), osVersion));
            test.setObjectRef(convertDebianId(test.getObjectRef(), osVersion));
            if (test.getStateRef().isPresent()) {
                test.setStateRef(convertDebianId(test.getStateRef().get(), osVersion));
            }
        }
    }

    private static void doCleanupState(StateType state, OsFamily osFamily, String osVersion) {
        if (osFamily == OsFamily.DEBIAN) {
            state.setId(convertDebianId(state.getId(), osVersion));
        }
    }

    private static void doCleanupObject(ObjectType object, OsFamily osFamily, String osVersion) {
        if (osFamily == OsFamily.DEBIAN) {
            object.setId(convertDebianId(object.getId(), osVersion));
        }
    }

    /**
     * Debian Ids are not unique among different versions, so it's possible to have OVAL constructs that have the
     * same id but different content for different versions of Debian.
     * <p>
     * To work around this, we insert the codename of the version into the id string
     */
    private static void convertDebianTestRefs(BaseCriteria root, String osVersion) {
        if (root instanceof CriteriaType) {
            for (BaseCriteria criteria : ((CriteriaType) root).getChildren()) {
                convertDebianTestRefs(criteria, osVersion);
            }
        }
        else {
            CriterionType criterionType = (CriterionType) root;
            criterionType.setTestRef(convertDebianId(criterionType.getTestRef(), osVersion));
        }
    }

    /**
     * Debian Ids are not unique among different versions, so it's possible to have OVAL constructs that have the
     * same id but different content for different versions of Debian.
     * <p>
     * To work around this, we insert the codename of the version into the id string
     */
    private static String convertDebianId(String id, String osVersion) {
        String codename;
        if ("10.0".equals(osVersion) || "10".equals(osVersion)) {
            codename = "buster";
        }
        else if ("11.0".equals(osVersion) || "11".equals(osVersion)) {
            codename = "bullseye";
        }
        else if ("12.0".equals(osVersion) || "12".equals(osVersion)) {
            codename = "bookworm";
        }
        else {
            throw new IllegalArgumentException("Invalid debian version: " + osVersion);
        }
        return id.replaceAll("debian", "debian-" + codename);
    }
}
