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

package com.suse.oval.ovaltypes;

import com.suse.utils.Opt;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
public class Advisory {
    @XmlElement(name = "affected_cpe_list", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
    private AffectedCpeList affectedCpeList;

    @XmlElement(name = "cve", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
    private List<AdvisoryCveType> cveList;

    @XmlElement(name = "affected", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
    private AdvisoryAffectedType affected;

    public void setAffectedCpeList(AffectedCpeList affectedCpeList) {
        this.affectedCpeList = affectedCpeList;
    }

    public List<String> getAffectedCpeList() {
        return Optional.ofNullable(affectedCpeList)
                .map(AffectedCpeList::getCpeList)
                .orElse(Collections.emptyList());
    }

    public List<String> getAffectedComponents() {
        return Optional.ofNullable(affected).map(AdvisoryAffectedType::getResolution)
                .map(AdvisoryResolutionType::getAffectedComponents).orElse(Collections.emptyList());
    }

    public List<AdvisoryCveType> getCveList() {
        return Optional.ofNullable(cveList).orElse(Collections.emptyList());
    }

    public void setCveList(List<AdvisoryCveType> cveList) {
        this.cveList = cveList;
    }

    public void setAffected(AdvisoryAffectedType affected) {
        this.affected = affected;
    }
}
