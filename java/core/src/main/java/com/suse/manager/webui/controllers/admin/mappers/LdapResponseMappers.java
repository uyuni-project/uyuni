/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.webui.controllers.admin.mappers;

import com.suse.manager.model.ldap.LdapAuthServer;
import com.suse.manager.webui.utils.ViewHelper;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Maps {@link LdapAuthServer} entities to JSON-friendly structures for the admin UI.
 *
 * <p>Bind passwords are never included. Clients receive {@code hasBindPassword} instead and may
 * send a new password only when changing it.</p>
 */
public final class LdapResponseMappers {

    private LdapResponseMappers() {
    }

    /**
     * Maps directory rows to the summary objects shown in the Setup Wizard list.
     *
     * @param servers directory records
     * @return resume maps for the list table
     */
    public static List<Map<String, Object>> mapResumeFromDB(List<LdapAuthServer> servers) {
        return servers.stream()
                .map(LdapResponseMappers::mapResumeFromDB)
                .collect(Collectors.toList());
    }

    /**
     * @param server directory record
     * @return resume map for one list row
     */
    public static Map<String, Object> mapResumeFromDB(LdapAuthServer server) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", server.getId());
        map.put("label", server.getLabel());
        map.put("host", server.getHost());
        map.put("port", server.getPort());
        map.put("transport", server.getTransport() == null ? null : server.getTransport().name());
        map.put("serverType", server.getServerType() == null ? null : server.getServerType().name());
        map.put("enabled", server.isEnabled());
        map.put("priority", server.getPriority());
        map.put("provisioningMode",
                server.getProvisioningMode() == null ? null : server.getProvisioningMode().name());
        map.put("modified", server.getModified() == null ? null :
                ViewHelper.formatDateTimeToISO(server.getModified()));
        return map;
    }

    /**
     * Maps a directory row to the full detail object used by the create/edit form.
     * Never includes the bind password.
     *
     * @param server directory record
     * @return full detail map
     */
    public static Map<String, Object> mapFullFromDB(LdapAuthServer server) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", server.getId());
        map.put("label", server.getLabel());
        map.put("enabled", server.isEnabled());
        map.put("priority", server.getPriority());
        map.put("serverType", server.getServerType() == null ? null : server.getServerType().name());
        map.put("host", server.getHost());
        map.put("port", server.getPort());
        map.put("transport", server.getTransport() == null ? null : server.getTransport().name());
        map.put("connectTimeout", server.getConnectTimeout().orElse(null));
        map.put("responseTimeout", server.getResponseTimeout().orElse(null));
        map.put("bindDn", server.getBindDn());
        map.put("hasBindPassword", server.getCredentials() != null &&
                StringUtils.isNotEmpty(server.getCredentials().getPassword()));
        map.put("userBaseDn", server.getUserBaseDn());
        map.put("userFilter", server.getUserFilter());
        map.put("loginAttribute", server.getLoginAttribute());
        map.put("firstNameAttribute", server.getFirstNameAttribute());
        map.put("lastNameAttribute", server.getLastNameAttribute());
        map.put("emailAttribute", server.getEmailAttribute());
        map.put("groupBaseDn", server.getGroupBaseDn());
        map.put("groupFilter", server.getGroupFilter());
        map.put("groupNameAttribute", server.getGroupNameAttribute());
        map.put("useMemberOf", server.isUseMemberOf());
        map.put("provisioningMode",
                server.getProvisioningMode() == null ? null : server.getProvisioningMode().name());
        map.put("defaultOrgId", server.getDefaultOrg() == null ? null : server.getDefaultOrg().getId());
        map.put("autoJoinRegularUser", server.isAutoJoinRegularUser());
        map.put("rootCa", server.getRootCa());
        map.put("hasRootCa", StringUtils.isNotBlank(server.getRootCa()));
        map.put("modified", server.getModified() == null ? null :
                ViewHelper.formatDateTimeToISO(server.getModified()));
        return map;
    }
}
