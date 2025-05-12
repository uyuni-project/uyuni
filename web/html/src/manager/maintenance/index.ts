export default {
  "maintenance/maintenance-windows-list": () => import("./list/maintenance-windows-list"),
  "maintenance/maintenance-window-create-edit": () => import("./edit/maintenance-window-create-edit"),
  "maintenance/system-assignment": () => import("./ssm/system-assignment"),
};
