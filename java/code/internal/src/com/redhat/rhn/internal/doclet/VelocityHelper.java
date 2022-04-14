/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Properties;

/**
 *
 * VelocityHelper
 */
public class VelocityHelper {

    private final VelocityEngine ve;
    private final VelocityContext context;

    /**
     * Constructor to be used when using a template file
     * @param templateDir the template directory
     */
    public VelocityHelper(String templateDir) {
        Properties p = new Properties();
        if (templateDir != null) {
            p.setProperty("file.resource.loader.path", templateDir);
        }
        p.setProperty(RuntimeConstants.VM_PERM_ALLOW_INLINE_REPLACE_GLOBAL, "true");

        // We want to avoid logging the INFO since it costs time
        Configurator.setLevel("org.apache.velocity", Level.WARN);
        ve = new VelocityEngine(p);
        context = new VelocityContext();
    }

    /**
     * Constructor used when the template will be passed in
     */
    public VelocityHelper() {
        this(null);
    }

    /**
     * adds a template match to the helper
     * @param key what to find
     * @param value what to replace with
     */
    public void addMatch(String key, Object value) {
        context.put(key, value);
    }

    /**
     * render the template according to a template file
     * @param fileName the filename
     * @return the rendered template
     */
    public String renderTemplateFile(String fileName) {
        Template t = ve.getTemplate(fileName);
        StringWriter writer = new StringWriter();
        Calendar cal = Calendar.getInstance();

        String date = (cal.get(Calendar.MONTH) + 1) + "/" +
                 cal.get(Calendar.DAY_OF_MONTH) +
                 "/" + cal.get(Calendar.YEAR);

        context.put("current_date", date);
        t.merge(context, writer);
        return writer.toString();
    }

    /**
     * render the template according to what we've added to addMatch
     * @param template the template
     * @return the rendered template
     */
    public String renderTemplate(String template) {
        StringWriter writer = new StringWriter();
        StringReader reader = new StringReader(template);

        ve.evaluate(context, writer, "a", reader);

        return writer.toString();
    }
}
