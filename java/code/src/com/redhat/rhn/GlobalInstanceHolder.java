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
package com.redhat.rhn;

import com.redhat.rhn.common.security.acl.Access;
import com.redhat.rhn.common.security.acl.AclFactory;
import com.redhat.rhn.frontend.taglibs.helpers.RenderUtils;
import com.redhat.rhn.manager.formula.FormulaManager;
import com.redhat.rhn.manager.formula.FormulaMonitoringManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.suse.manager.clusters.ClusterManager;
import com.suse.manager.kubernetes.KubernetesManager;
import com.suse.manager.utils.SaltKeyUtils;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.virtualization.VirtManagerSalt;
import com.suse.manager.webui.controllers.utils.RegularMinionBootstrapper;
import com.suse.manager.webui.controllers.utils.SSHMinionBootstrapper;
import com.suse.manager.webui.menu.MenuTree;
import com.suse.manager.webui.services.SaltServerActionService;
import com.suse.manager.webui.services.iface.MonitoringManager;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.MinionActionUtils;
import com.suse.manager.webui.utils.UserPreferenceUtils;
import com.suse.manager.webui.utils.ViewHelper;

/**
 * This class only exists to have a single place for initializing objects
 * and share a single consistent instance in multiple parts of tomcat/taskomatic.
 * These instances should only be referenced from entry points of the program and
 * places that cant receive them otherwise though the constructor (i.e because of reflection)
 */
public class GlobalInstanceHolder {

    private GlobalInstanceHolder() {
    }

    private static final SaltService SALT_SERVICE = new SaltService();
    public static final SystemQuery SYSTEM_QUERY = SALT_SERVICE;
    public static final SaltApi SALT_API = SALT_SERVICE;
    public static final ServerGroupManager SERVER_GROUP_MANAGER = new ServerGroupManager();
    public static final FormulaManager FORMULA_MANAGER = new FormulaManager(SALT_API);
    public static final ClusterManager CLUSTER_MANAGER = new ClusterManager(
            SALT_API, SYSTEM_QUERY, SERVER_GROUP_MANAGER, FORMULA_MANAGER
    );
    public static final SaltUtils SALT_UTILS = new SaltUtils(SYSTEM_QUERY, SALT_API,
            CLUSTER_MANAGER, FORMULA_MANAGER, SERVER_GROUP_MANAGER);
    public static final SaltKeyUtils SALT_KEY_UTILS = new SaltKeyUtils(SALT_API);
    public static final SaltServerActionService SALT_SERVER_ACTION_SERVICE = new SaltServerActionService(
            SALT_API, SALT_UTILS, CLUSTER_MANAGER, FORMULA_MANAGER, SALT_KEY_UTILS);
    public static final Access ACCESS = new Access(CLUSTER_MANAGER);
    public static final AclFactory ACL_FACTORY = new AclFactory(ACCESS);
    // Referenced from JSP
    public static final MenuTree MENU_TREE = new MenuTree(ACL_FACTORY);
    public static final UserPreferenceUtils USER_PREFERENCE_UTILS = new UserPreferenceUtils(ACL_FACTORY);
    public static final RenderUtils RENDER_UTILS = new RenderUtils(ACL_FACTORY);
    public static final MinionActionUtils MINION_ACTION_UTILS = new MinionActionUtils(
            SALT_SERVER_ACTION_SERVICE, SALT_API, SALT_UTILS);
    public static final KubernetesManager KUBERNETES_MANAGER = new KubernetesManager(SALT_API);
    public static final VirtManager VIRT_MANAGER = new VirtManagerSalt(SALT_API);
    public static final RegularMinionBootstrapper REGULAR_MINION_BOOTSTRAPPER =
            new RegularMinionBootstrapper(SYSTEM_QUERY, SALT_API);
    public static final SSHMinionBootstrapper SSH_MINION_BOOTSTRAPPER =
            new SSHMinionBootstrapper(SYSTEM_QUERY, SALT_API);
    public static final MonitoringManager MONITORING_MANAGER = new FormulaMonitoringManager();
    public static final SystemEntitlementManager SYSTEM_ENTITLEMENT_MANAGER = new SystemEntitlementManager(
            new SystemUnentitler(VIRT_MANAGER, MONITORING_MANAGER, SERVER_GROUP_MANAGER),
            new SystemEntitler(SALT_API, VIRT_MANAGER, MONITORING_MANAGER,
                    SERVER_GROUP_MANAGER)
    );

    public static final ViewHelper VIEW_HELPER = ViewHelper.getInstance();
}
