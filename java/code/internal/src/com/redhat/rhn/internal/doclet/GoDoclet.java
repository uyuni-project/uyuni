/*
 * Copyright (c) 2024 SUSE LLC
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
package com.redhat.rhn.internal.doclet;

import jdk.javadoc.doclet.DocletEnvironment;

/**
 * Generate mgrctl client go code for the API
 */
public class GoDoclet extends ApiDoclet {
    @Override
    public DocWriter getWriter(String outputFolder, String templateFolder, String product,
                               String apiVersion, boolean debug) {
        return new GoWriter(outputFolder, templateFolder, debug);
    }

    @Override
    public String getName() {
        return "Go doclet";
    }

    @Override
    public boolean run(DocletEnvironment docletEnvironmentIn) {
        return run(docletEnvironmentIn, "go");
    }
}
