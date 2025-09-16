/*
 * Copyright (c) 2022 SUSE LLC
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
package com.redhat.rhn.frontend.servlets.ajax.dto;

/**
 * DTO class used by {@link com.redhat.rhn.frontend.servlets.ajax.AjaxHandlerServlet}
 * when processing requests addressed to
 * {@link com.redhat.rhn.frontend.action.renderers.setupwizard.MirrorCredentialsRenderer#saveCredentials}
 */
public class SaveMirrorCredentialsDto {
    private String id;
    private String user;
    private String password;

    public String getId() {
        return id;
    }

    public Long getIdValue() {
        return id == null || id.isEmpty() ? null : Long.parseLong(id);
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
