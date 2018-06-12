/**
 * Copyright (c) 2015 SUSE LLC
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
package com.redhat.rhn.frontend.action.help;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.frontend.struts.RhnHelper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * EULA action page.
 */
public class EulaAction extends org.apache.struts.action.Action {
    private static final File EULA_PATH = new File("/srv/www/htdocs/help/eula.html");

    /** {@inheritDoc} */
    @Override
    public ActionForward execute(ActionMapping mapping,
                                  ActionForm formIn,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
        request.setAttribute("isUyuni", ConfigDefaults.get().isUyuni());
        String attr = "EulaText";
        if (EULA_PATH.canRead()) {
            try {
                request.setAttribute(attr, new String(
                        Files.readAllBytes(Paths.get(EULA_PATH.toURI())),
                        StandardCharsets.UTF_8));
            }
            catch (IOException ex) {
                Logger.getLogger(EulaAction.class.getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException(ex);
            }
        }
        else {
            request.setAttribute(attr, String.format("EULA text file '%s' was not found.",
                                                     EULA_PATH.getAbsolutePath()));
        }

        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }
}
