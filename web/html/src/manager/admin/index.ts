export default {
  "admin/config/monitoring": () => import("./config/monitoring.renderer"),
  "admin/setup/products/products": () => import("./setup/products/products"),
  "admin/task-engine-status/taskotop": () => import("./task-engine-status/taskotop"),
  "admin/setup/list-payg": () => import("./list-payg/list-payg.renderer"),
  "admin/setup/payg": () => import("./payg/payg.renderer"),
  "admin/setup/create-payg": () => import("./create-payg/create-payg.renderer"),
  "admin/config/password-policy": () => import("./password-policy/password-policy.renderer")
};
