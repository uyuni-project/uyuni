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

package com.redhat.rhn.frontend.action.renderers;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.listview.PageControl;

import com.suse.manager.utils.DBDiskCheckHelper;
import com.suse.manager.utils.DiskCheckHelper;
import com.suse.manager.utils.DiskCheckSeverity;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Renders YourRhn fragment for disk check warning
 */
public class DiskCheckWarningRenderer extends BaseFragmentRenderer {

    private static final String SYSTEM_DISK_WARNING = "systemdiskwarning";
    private static final String DB_DISK_WARNING = "dbdiskwarning";

    @Override
    protected void render(User user, PageControl pc, HttpServletRequest request) {
        DiskCheckSeverity systemSeverity = new DiskCheckHelper().executeDiskCheck();
        request.setAttribute(SYSTEM_DISK_WARNING, systemSeverity.needsAttention());

        DiskCheckSeverity dbSeverity = new DBDiskCheckHelper().executeDiskCheck();
        request.setAttribute(DB_DISK_WARNING, dbSeverity.needsAttention());

        RendererHelper.setTableStyle(request, null);
    }

    @Override
    protected String getPageUrl() {
        return "/WEB-INF/pages/common/fragments/yourrhn/diskcheckwarn.jsp";
    }
}
