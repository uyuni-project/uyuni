/**
 * Copyright (c) 2020 SUSE LLC
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
package com.suse.manager.caasp;

import com.suse.manager.extensions.PluginSparkTemplateLoader;
import com.suse.manager.extensions.RouterExtensionPoint;
import com.suse.manager.webui.utils.SparkApplicationHelper;
import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.template.TemplateLoader;
import org.pf4j.Extension;
import spark.template.jade.JadeTemplateEngine;

import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.get;

@Extension
public class CaaspRouterExtension implements RouterExtensionPoint {

    @Override
    public JadeTemplateEngine setupTemplateEngine(TemplateLoader parentLoader) {
        var configuration = new JadeConfiguration();
        configuration.setTemplateLoader(new PluginSparkTemplateLoader("com/suse/manager/caasp/templates/", this.getClass().getClassLoader(), parentLoader));
        return SparkApplicationHelper.setup(configuration);
    }

    @Override
    public void addRoutes(JadeTemplateEngine jade) {
        get("/manager/systems/details/packages/caasp-upgrade", withUser(CaaspUpgradeController::get), jade);
    }
}
