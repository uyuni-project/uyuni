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

/**
 * This is a "hack" and should only be used by the main entry points of
 * tomcat and taskomatic and those places that don't have a good way yet to
 * get dependencies passed via constructor.
 */
public class GlobalInstanceHolder {
    private static final SaltService SALT_SERVICE = new SaltService();
    public static final SystemQuery SYSTEM_QUERY = SALT_SERVICE;
    public static final SaltApi SALT_API = SALT_SERVICE;
    private static final ServerGroupManager SERVER_GROUP_MANAGER = ServerGroupManager.getInstance();
    public static final FormulaManager FORMULA_MANAGER = new FormulaManager(SALT_API);
    public static final ClusterManager CLUSTER_MANAGER = new ClusterManager(
            SALT_API, SYSTEM_QUERY, SERVER_GROUP_MANAGER, FORMULA_MANAGER
    );
    public static final SaltUtils SALT_UTILS = new SaltUtils(SYSTEM_QUERY, SALT_API,
            CLUSTER_MANAGER, FORMULA_MANAGER);
    public static final SaltServerActionService SALT_SERVER_ACTION_SERVICE = new SaltServerActionService(
            SYSTEM_QUERY, SALT_UTILS, CLUSTER_MANAGER, FORMULA_MANAGER);
    public static final Access ACCESS = new Access(CLUSTER_MANAGER);
    public static final AclFactory ACL_FACTORY = new AclFactory(ACCESS);
    public static final MenuTree MENU_TREE = new MenuTree(ACL_FACTORY);
    public static final RenderUtils RENDER_UTILS = new RenderUtils(ACL_FACTORY);
    public static final MinionActionUtils MINION_ACTION_UTILS = new MinionActionUtils(
            SALT_SERVER_ACTION_SERVICE, SYSTEM_QUERY, SALT_UTILS);
    public static final KubernetesManager KUBERNETES_MANAGER = new KubernetesManager(SYSTEM_QUERY);
    public static final VirtManager VIRT_MANAGER = new VirtManagerSalt(SALT_API);
    public static final RegularMinionBootstrapper REGULAR_MINION_BOOTSTRAPPER =
            new RegularMinionBootstrapper(SYSTEM_QUERY);
    public static final SSHMinionBootstrapper SSH_MINION_BOOTSTRAPPER = new SSHMinionBootstrapper(SYSTEM_QUERY);
    public static final MonitoringManager MONITORING_MANAGER = new FormulaMonitoringManager();
    public static final SystemEntitlementManager SYSTEM_ENTITLEMENT_MANAGER = new SystemEntitlementManager(
            new SystemUnentitler(VIRT_MANAGER, MONITORING_MANAGER),
            new SystemEntitler(GlobalInstanceHolder.SYSTEM_QUERY, VIRT_MANAGER, MONITORING_MANAGER)
    );
}
