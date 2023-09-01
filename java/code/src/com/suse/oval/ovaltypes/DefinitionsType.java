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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;

/**
 * The DefinitionsType complex type is a container for one or more definition elements.
 * Each definition element describes a single OVAL Definition.
 * <p>
 * Please refer to the description of the {@link DefinitionType} for more information about an individual definition.
 * </p>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DefinitionsType", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
public class DefinitionsType {

    @XmlElement(name = "definition", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5", required = true)
    protected List<DefinitionType> definitions;

    /**
     * Gets the list of definitions
     *
     * @return the list of contained OVAL definitions.
     */
    public List<DefinitionType> getDefinitions() {
        if (definitions == null) {
            definitions = new ArrayList<>();
        }
        return this.definitions;
    }

}
