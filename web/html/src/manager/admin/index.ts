export default {
  "admin/config/monitoring": () => import("./config/monitoring.renderer"),
  "admin/setup/products/products": () => import("./setup/products/products"),
  "admin/task-engine-status/taskotop": () => import("./task-engine-status/taskotop"),
  "admin/setup/list-payg": () => import("./list-payg/list-payg.renderer"),
  "admin/setup/payg": () => import("./payg/payg.renderer"),
  "admin/setup/create-payg": () => import("./create-payg/create-payg.renderer"),
  "admin/setup/proxy": () => import("./setup/proxy/proxy.renderer"),
  "admin/iss/hub": () => import("./hub/iss-hubs.renderer"),
  "admin/iss/peripheral": () => import("./hub/iss-peripherals.renderer"),
  "admin/iss/add/hub": () => import("./hub/add/add-iss-hub.renderer"),
  "admin/iss/add/peripheral": () => import("./hub/add/add-iss-peripheral.renderer"),
};
