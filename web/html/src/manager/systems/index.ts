export default {
  "systems/activation-key/activation-key-channels": () => import("./activation-key/activation-key-channels.renderer"),
  "systems/bootstrap/bootstrap-minions": () => import("./bootstrap/bootstrap-minions"),
  "systems/ssm/ssm-subscribe-channels": () => import("./ssm/ssm-subscribe-channels"),
  "systems/subscribe-channels/subscribe-channels": () => import("./subscribe-channels/subscribe-channels.renderer"),
  "systems/virtualhostmanager/virtualhostmanager": () => import("./virtualhostmanager/virtualhostmanager"),
  "systems/delete-system-confirm": () => import("./delete-system-confirm"),
  "systems/duplicate-systems-compare-delete": () => import("./duplicate-systems-compare-delete"),
  "systems/list/virtual": () => import("./virtual-list"),
};
