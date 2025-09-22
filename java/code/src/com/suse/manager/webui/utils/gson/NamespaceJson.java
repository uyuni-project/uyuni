/*
 * Copyright (c) 2025 SUSE LLC
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

package com.suse.manager.webui.utils.gson;

import com.redhat.rhn.frontend.dto.BaseTupleDto;

import javax.persistence.Tuple;

/**
 * Simple JSON representation of {@link com.redhat.rhn.domain.access.Namespace} for listing roles
 */
public class NamespaceJson extends BaseTupleDto {

    /**
     * Constructor used to populate using DTO projection from the data of query that list namespaces
     *
     * @param tuple JPA tuple
     */
    public NamespaceJson(Tuple tuple) {
        setId(getTupleValue(tuple, "id" , Number.class).map(Number::longValue).orElse(null));
        setNamespace(getTupleValue(tuple, "namespace" , String.class).orElse("-"));
        setDescription(getTupleValue(tuple, "description" , String.class).orElse("-"));
        setAccessMode(getTupleValue(tuple, "access_mode" , String.class).orElse("-"));
        setView(getTupleValue(tuple, "view", Boolean.class).orElse(false));
        setModify(getTupleValue(tuple, "modify", Boolean.class).orElse(false));
    }

    private Long id;
    private String namespace;
    private String description;
    private String accessMode;
    private Boolean view;
    private Boolean modify;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long idIn) {
        id = idIn;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespaceIn) {
        namespace = namespaceIn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String descriptionIn) {
        description = descriptionIn;
    }

    public String getAccessMode() {
        return accessMode;
    }

    public void setAccessMode(String accessModeIn) {
        accessMode = accessModeIn;
    }

    public Boolean getView() {
        return view;
    }

    public void setView(Boolean viewIn) {
        view = viewIn;
    }

    public Boolean getModify() {
        return modify;
    }

    public void setModify(Boolean modifyIn) {
        modify = modifyIn;
    }
}
