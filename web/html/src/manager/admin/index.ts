export default {
  "admin/config/monitoring": () => import("./config/monitoring.renderer"),
  "admin/setup/products/products": () => import("./setup/products/products"),
  "admin/task-engine-status/taskotop": () => import("./task-engine-status/taskotop"),
  "admin/setup/list-payg": () => import("./list-payg/list-payg.renderer"),
  "admin/setup/payg": () => import("./payg/payg.renderer"),
  "admin/setup/create-payg": () => import("./create-payg/create-payg.renderer"),
  "admin/config/password-policy": () => import("./password-policy/password-policy.renderer"),
  "admin/setup/proxy": () => import("./setup/proxy/proxy.renderer"),
  "admin/hub/hub-details": () => import("./hub/hub-details.renderer"),
  "admin/hub/peripherals": () => import("./hub/peripherals.renderer"),
  "admin/hub/peripheral/create": () => import("./hub/add/add-peripheral.renderer"),
  "admin/hub/peripheral/update": () => import("./hub/details/peripheral-details.renderer"),
};
