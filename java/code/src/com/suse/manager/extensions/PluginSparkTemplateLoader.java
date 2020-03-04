/**
 * Copyright (c) 2016 SUSE LLC
 * <p>
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 * <p>
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.extensions;

import de.neuland.jade4j.template.TemplateLoader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class PluginSparkTemplateLoader implements TemplateLoader {

    private ClassLoader classLoader;
    private String templateRoot;
    private TemplateLoader parentLoader;
    private String encoding = "UTF-8";

    public PluginSparkTemplateLoader(String templateRoot, ClassLoader classLoader, TemplateLoader parentLoader) {
        this.templateRoot = templateRoot;
        this.classLoader = classLoader;
        this.parentLoader = parentLoader;
    }

    @Override
    public long getLastModified(String s) throws IOException {
        return -1L;
    }

    @Override
    public Reader getReader(String name) throws IOException {
        if (!name.endsWith(".jade")) {
            name = name + ".jade";
        }

        var is = classLoader.getResourceAsStream(templateRoot + name);
        if (is != null) {
            return new InputStreamReader(is, this.getEncoding());
        }
        else {
            // delegate to parent loader
            return parentLoader.getReader(name);
        }
    }

    /**
     * @return encoding to get
     */
    public String getEncoding() {
        return encoding;
    }
}
